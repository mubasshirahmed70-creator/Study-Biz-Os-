package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.local.model.BusinessProductEntity
import com.example.data.local.model.ChapterEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.SubjectEntity
import com.example.data.local.model.TaskEntity
import com.example.data.local.model.UserSettingsEntity
import com.example.data.remote.GeminiApiClient
import com.example.data.repository.StudyBizRepository
import com.example.voice.BanglaVoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class AiChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestampEpoch: Long = System.currentTimeMillis()
)

class StudyBizViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StudyBizRepository(db.studyBizDao())

    val voiceManager = BanglaVoiceManager(application)

    init {
        viewModelScope.launch {
            AppDatabase.seedInitialData(db.studyBizDao())
        }

        voiceManager.onSpeechResultListener = { spokenText ->
            processVoiceInput(spokenText)
        }
    }

    val todayDateString: String = AppDatabase.getTodayDateString()

    // 1. Reactive Streams
    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<ChapterEntity>> = repository.allChapters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<BusinessProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<TaskEntity>> = repository.getTasksForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            settings ?: UserSettingsEntity()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

    // 2. Priority Guard & Workload Warning
    val priorityGuardNotice: StateFlow<String?> = combine(todayTasks, userSettings) { tasks, settings ->
        val pendingAcademic = tasks.filter { it.category == "STUDY" && !it.isCompleted }
        val businessTasks = tasks.filter { it.category == "BUSINESS" }
        val businessMinutes = businessTasks.sumOf { it.durationMinutes }
        val businessCapMinutes = (settings.dailyBusinessHourCap * 60).toInt()

        if (businessMinutes > businessCapMinutes) {
            "সতর্কতা: ব্যবসার কাজ ($businessMinutes মিনিট) আপনার নির্ধারিত সীমা ($businessCapMinutes মিনিট) অতিক্রম করেছে। ক্লাস ৯-এর পড়াশোনার সময় সুরক্ষিত রাখুন!"
        } else if (pendingAcademic.any { it.priority == "EXAM_PREP" || it.priority == "WEAK_ACADEMIC" } && businessTasks.any { it.isCompleted }) {
            "একাডেমিক গার্ড: গুরুত্বপূর্ণ বিষয়ের পড়াশোনা (পরীক্ষা প্রস্তুতি বা দুর্বল অধ্যায়) এখনও বাকি। স্কুল ও এসএসসির কাজ শেষ না করে ড্রপশিপিং কাজ করবেন না।"
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 3. Focus Timer Engine
    private val _timerMode = MutableStateFlow("STUDY")
    val timerMode: StateFlow<String> = _timerMode.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(50 * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(50 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerTopic = MutableStateFlow("সাধারণ গণিত - এসএসসি প্রস্তুতি")
    val timerTopic: StateFlow<String> = _timerTopic.asStateFlow()

    private val _timerCompletedMessage = MutableStateFlow<String?>(null)
    val timerCompletedMessage: StateFlow<String?> = _timerCompletedMessage.asStateFlow()

    private var timerJob: Job? = null

    fun selectTimerMode(mode: String, minutes: Int? = null) {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _timerMode.value = mode

        val durationMin = minutes ?: when (mode) {
            "STUDY" -> userSettings.value.studyFocusMinutes
            "BREAK" -> userSettings.value.studyBreakMinutes
            "BUSINESS" -> userSettings.value.businessFocusMinutes
            else -> 25
        }

        _totalDurationSeconds.value = durationMin * 60
        _remainingSeconds.value = durationMin * 60
        _timerTopic.value = when (mode) {
            "STUDY" -> "একাডেমিক পড়াশোনা সেশন"
            "BUSINESS" -> "ড্রপশিপিং প্রোডাক্ট / ক্রিয়েটিভ"
            else -> "বিশ্রাম ও স্বাস্থ্য বিরতি"
        }
    }

    fun setTimerTopic(topic: String) {
        _timerTopic.value = topic
    }

    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _isTimerRunning.value = true
        _timerCompletedMessage.value = null
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value == 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _remainingSeconds.value = _totalDurationSeconds.value
    }

    private fun onTimerFinished() {
        _isTimerRunning.value = false
        val durationMins = _totalDurationSeconds.value / 60
        val sessionType = if (_timerMode.value == "BUSINESS") "BUSINESS" else "STUDY"

        if (_timerMode.value != "BREAK") {
            viewModelScope.launch {
                repository.logFocusSession(
                    FocusSessionEntity(
                        sessionType = sessionType,
                        targetDurationMinutes = durationMins,
                        actualDurationMinutes = durationMins,
                        dateString = todayDateString,
                        associatedTopic = _timerTopic.value,
                        completedSuccessfully = true
                    )
                )
            }
            val msg = "অভিনন্দন! আপনার $durationMins মিনিটের $sessionType ফোকাস সেশন ডেটাবেসে যুক্ত হয়েছে।"
            _timerCompletedMessage.value = msg
            voiceManager.speak(msg)
        } else {
            val msg = "বিরতি শেষ! পড়াশোনায় ফিরে আসার জন্য আপনি প্রস্তুত।"
            _timerCompletedMessage.value = msg
            voiceManager.speak(msg)
        }
    }

    fun dismissTimerMessage() {
        _timerCompletedMessage.value = null
    }

    // 4. Tasks Actions
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAtEpoch = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
        }
    }

    fun addTask(
        title: String,
        category: String,
        priority: String,
        durationMinutes: Int,
        timeBlock: String?,
        subjectOrArea: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    category = category,
                    priority = priority,
                    durationMinutes = durationMinutes,
                    dateString = todayDateString,
                    timeBlock = timeBlock,
                    subjectOrArea = subjectOrArea,
                    notes = notes
                )
            )
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // 5. Chapter & Subject Actions
    fun updateChapterStatus(chapter: ChapterEntity, newStatus: String, percent: Int) {
        viewModelScope.launch {
            repository.updateChapter(
                chapter.copy(
                    status = newStatus,
                    completionPercent = percent,
                    lastStudiedEpoch = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleChapterWeak(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(isWeak = !chapter.isWeak))
        }
    }

    fun toggleChapterRevision(chapter: ChapterEntity) {
        viewModelScope.launch {
            repository.updateChapter(chapter.copy(needsRevision = !chapter.needsRevision))
        }
    }

    fun addChapter(subjectId: Long, title: String, number: Int) {
        viewModelScope.launch {
            repository.insertChapter(
                ChapterEntity(
                    subjectId = subjectId,
                    title = title,
                    chapterNumber = number,
                    status = "NOT_STARTED",
                    completionPercent = 0
                )
            )
        }
    }

    fun addSubject(name: String, colorHex: String, totalChapters: Int, isWeak: Boolean) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    name = name,
                    colorHex = colorHex,
                    totalChapters = totalChapters,
                    isWeak = isWeak
                )
            )
        }
    }

    // 6. Business Product Actions
    fun addProduct(
        name: String,
        supplierName: String,
        supplierUrl: String,
        costPrice: Double,
        sellingPrice: Double,
        status: String,
        category: String,
        notes: String
    ) {
        viewModelScope.launch {
            val margin = (sellingPrice - costPrice).coerceAtLeast(0.0)
            repository.insertProduct(
                BusinessProductEntity(
                    name = name,
                    supplierName = supplierName,
                    supplierUrl = supplierUrl,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    estimatedMargin = margin,
                    status = status,
                    category = category,
                    notes = notes
                )
            )
        }
    }

    fun updateProductStatus(product: BusinessProductEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(status = newStatus))
        }
    }

    fun deleteProduct(product: BusinessProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    // 7. Goal Actions
    fun addGoal(title: String, timeframe: String, category: String, target: Double, unit: String) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    title = title,
                    timeframe = timeframe,
                    category = category,
                    targetValue = target,
                    currentValue = 0.0,
                    unit = unit,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleGoalCompletion(goal: GoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(
                goal.copy(
                    isCompleted = !goal.isCompleted,
                    currentValue = if (!goal.isCompleted) goal.targetValue else 0.0
                )
            )
        }
    }

    // 8. Settings & In-App API Key Actions
    fun saveInAppApiKey(apiKey: String) {
        viewModelScope.launch {
            repository.saveUserSettings(
                userSettings.value.copy(customGeminiApiKey = apiKey.trim())
            )
        }
    }

    fun updateSettings(
        studentName: String,
        studyTarget: Double,
        businessCap: Double,
        sleepGoal: Double,
        studyFocus: Int,
        studyBreak: Int,
        businessFocus: Int,
        businessBreak: Int,
        apiKey: String
    ) {
        viewModelScope.launch {
            repository.saveUserSettings(
                userSettings.value.copy(
                    studentName = studentName,
                    dailyStudyHourTarget = studyTarget,
                    dailyBusinessHourCap = businessCap,
                    dailySleepHoursGoal = sleepGoal,
                    studyFocusMinutes = studyFocus,
                    studyBreakMinutes = studyBreak,
                    businessFocusMinutes = businessFocus,
                    businessBreakMinutes = businessBreak,
                    customGeminiApiKey = apiKey.trim()
                )
            )
        }
    }

    fun resetAndReseedData() {
        viewModelScope.launch {
            repository.resetToSeed()
            AppDatabase.seedInitialData(db.studyBizDao())
        }
    }

    // 9. AI Voice & Chat Assistant Engine
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(
        listOf(
            AiChatMessage(
                sender = "AI",
                text = "আসসালামু আলাইকুম মুবাশ্বির! আমি আপনার StudyBiz OS বাংলা সহকারী। ক্লাস ৯ এসএসসি প্রস্তুতি আপনার প্রধান অগ্রাধিকার, সাথে ৮ ঘণ্টা ঘুম নিশ্চিত রাখব। বলুন, আজকের রুটিন বা পড়ার বিষয়ে কীভাবে সাহায্য করতে পারি?"
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun clearAiChat() {
        voiceManager.stopAll()
        _aiMessages.value = listOf(
            AiChatMessage(
                sender = "AI",
                text = "আসসালামু আলাইকুম মুবাশ্বির! আমি আপনার StudyBiz OS বাংলা পার্সোনাল সহকারী। ক্লাস ৯ এসএসসি প্রস্তুতি আপনার ১ নম্বর প্রায়োরিটি, সাথে ৮ ঘণ্টা ঘুম সুরক্ষিত রেখে ড্রপশিপিং ব্যালান্স করব। আপনি যেকোনো বিষয়ের নাম বলতে পারেন বা অবিরাম মোডে কথা চালিয়ে যেতে পারেন।"
            )
        )
    }

    fun processVoiceInput(spokenText: String) {
        sendAiPrompt(spokenText, speakAloud = true)
    }

    fun sendAiPrompt(userPrompt: String, speakAloud: Boolean = false) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) return

        _aiMessages.value = _aiMessages.value + AiChatMessage(sender = "USER", text = trimmed)
        _isAiLoading.value = true

        viewModelScope.launch {
            // First check if there is an intelligent action / reschedule command
            val actionResult = handleIntelligentVoiceAction(trimmed)

            val replyText = if (actionResult != null) {
                actionResult
            } else {
                // Call Gemini API with injected context including recent dialogue history
                val systemPrompt = buildBengaliSystemPrompt()
                val effectiveApiKey = userSettings.value.customGeminiApiKey.ifBlank { BuildConfig.GEMINI_API_KEY }

                // Build context with recent conversation turns for continuous multi-turn flow
                val recentHistory = _aiMessages.value.takeLast(6).joinToString("\n") {
                    "${if (it.sender == "USER") "ব্যবহারকারী" else "সহকারী"}: ${it.text}"
                }
                val enrichedPrompt = "সাম্প্রতিক আলাপ:\n$recentHistory\n\nবর্তমান ব্যবহারকারীর কথা: $trimmed"

                val result = GeminiApiClient.callGemini(
                    apiKey = effectiveApiKey,
                    prompt = enrichedPrompt,
                    systemPrompt = systemPrompt
                )

                result.getOrElse {
                    generateLocalBengaliAdvice(trimmed)
                }
            }

            _aiMessages.value = _aiMessages.value + AiChatMessage(sender = "AI", text = replyText)
            _isAiLoading.value = false

            if (speakAloud) {
                voiceManager.speak(replyText)
            }
        }
    }

    private suspend fun handleIntelligentVoiceAction(text: String): String? {
        val lower = text.lowercase(Locale.ROOT)

        // Common subject extraction list
        val subjectMap = listOf(
            listOf("গণিত", "mathematics", "ম্যাথ", "অংক") to "সাধারণ গণিত",
            listOf("উচ্চতর গণিত", "higher math", "হায়ার ম্যাথ") to "উচ্চতর গণিত",
            listOf("ইংরেজি", "english", "ইংলিশ") to "ইংরেজি",
            listOf("বাংলা", "bangla", "বাংলা ১ম", "বাংলা ২য়") to "বাংলা",
            listOf("বিজ্ঞান", "science") to "বিজ্ঞান",
            listOf("পদার্থবিজ্ঞান", "physics", "পদার্থ") to "পদার্থবিজ্ঞান",
            listOf("রসায়ন", "chemistry", "কেমিস্ট্রি") to "রসায়ন",
            listOf("জীববিজ্ঞান", "biology", "বায়োলজি") to "জীববিজ্ঞান",
            listOf("আইসিটি", "ict", "তথ্য ও যোগাযোগ প্রযুক্তি", "কম্পিউটার") to "আইসিটি",
            listOf("বাংলাদেশ ও বিশ্বপরিচয়", "বিজিএস", "bgs") to "বাংলাদেশ ও বিশ্বপরিচয়",
            listOf("ইসলাম ও নৈতিক শিক্ষা", "ধর্ম", "ইসলাম শিক্ষা") to "ইসলাম শিক্ষা",
            listOf("হিসাববিজ্ঞান", "accounting", "একাউন্টিং") to "হিসাববিজ্ঞান",
            listOf("ফিন্যান্স", "finance") to "ফিন্যান্স ও ব্যাংকিং",
            listOf("ব্যবসায় উদ্যোগ", "ব্যবসায়", "বিজনেস স্টাডিজ") to "ব্যবসায় উদ্যোগ",
            listOf("কৃষি শিক্ষা", "কৃষি") to "কৃষি শিক্ষা"
        )

        val detectedSubject = subjectMap.firstOrNull { (_, aliases) ->
            aliases.any { lower.contains(it) }
        }?.second

        // Case 0: Full routine generation request
        if (lower.contains("রুটিন") && (lower.contains("তৈরি") || lower.contains("বানাও") || lower.contains("বানিয়ে") || lower.contains("সাজাও") || lower.contains("করো") || lower.contains("দাও") || lower.contains("প্ল্যান"))) {
            val mainSub1 = detectedSubject ?: "সাধারণ গণিত"
            val mainSub2 = if (mainSub1 == "বিজ্ঞান") "সাধারণ গণিত" else "বিজ্ঞান"

            addTask(
                title = "ভোরের $mainSub1: গভীর ফোকাস সেশন",
                category = "STUDY",
                priority = "WEAK_ACADEMIC",
                durationMinutes = 60,
                timeBlock = "04:15 AM - 05:15 AM",
                subjectOrArea = mainSub1,
                notes = "ফজরের আগের নিস্তব্ধ সময়ে সর্বোচ্চ মনোযোগ দিয়ে $mainSub1 অনুশীলন।"
            )
            addTask(
                title = "বিকালের $mainSub2: দ্রুত প্রস্তুতি ও রিভিশন",
                category = "STUDY",
                priority = "DAILY_STUDY_TARGET",
                durationMinutes = 50,
                timeBlock = "04:30 PM - 05:20 PM",
                subjectOrArea = mainSub2,
                notes = "স্কুল থেকে ফিরে ফ্রেশ হয়ে কোচিংয়ে যাওয়ার আগের স্টাডি ব্লক।"
            )
            addTask(
                title = "রাতের ড্রপশিপিং: আলিয়েক্সপ্রেস প্রোডাক্ট রিসার্চ",
                category = "BUSINESS",
                priority = "IMPORTANT_BUSINESS",
                durationMinutes = 35,
                timeBlock = "09:45 PM - 10:20 PM",
                subjectOrArea = "প্রোডাক্ট রিসার্চ",
                notes = "কোচিং শেষে ডিনারের পর সীমিত ৩৫ মিনিট বিজনেস সেশন।"
            )
            return "হ্যালো মুবাশ্বির! আপনার স্কুল ও নাইট কোচিংয়ের সময়ের সাথে মিলিয়ে আজকের পুরো রুটিন তৈরি করে দিয়েছি। ভোরে $mainSub1, বিকালে $mainSub2 এবং নাইট কোচিং শেষে ৩৫ মিনিট ড্রপশিপিং রাখা হয়েছে। রাত ১০:৩০ এ ঘুমাতে গিয়ে ৮ ঘণ্টা ঘুম পূরণ করবেন।"
        }

        // Case 1: Any Subject missed / not completed
        if (detectedSubject != null && (lower.contains("পড়া হয়নি") || lower.contains("পড়তে পারিনি") || lower.contains("মিস") || lower.contains("পড়া হয় নাই") || lower.contains("বাকি আছে") || lower.contains("শেষ হয়নি"))) {
            addTask(
                title = "$detectedSubject রিকভারি সেশন",
                category = "STUDY",
                priority = "WEAK_ACADEMIC",
                durationMinutes = 35,
                timeBlock = "09:45 PM - 10:20 PM",
                subjectOrArea = detectedSubject,
                notes = "স্বয়ংক্রিয় এআই রিকভারি: কোচিং শেষ করে ৩৫ মিনিট ফোকাসড রিভিশন।"
            )
            return "কোনো সমস্যা নেই মুবাশ্বির। রাত ৯:৩০ এ কোচিং শেষ হওয়ার পর ৯:৪৫ থেকে ১০:২০ পর্যন্ত $detectedSubject-এর একটি নিবিড় ফোকাসড সেশন রুটিনে যোগ করে দিয়েছি। রাত ১০:৩০ এ ঘুমাতে গিয়ে ৮ ঘণ্টা ঘুম বজায় রাখবেন।"
        }

        // Case 2: Specific subject focus request
        if (detectedSubject != null && (lower.contains("পড়ব") || lower.contains("পড়তে চাই") || lower.contains("সময় দাও") || lower.contains("রিভিশন"))) {
            addTask(
                title = "$detectedSubject বিশেষ স্টাডি সেশন",
                category = "STUDY",
                priority = "DAILY_STUDY_TARGET",
                durationMinutes = 45,
                timeBlock = "04:30 PM - 05:15 PM",
                subjectOrArea = detectedSubject,
                notes = "ব্যবহারকারীর অনুরোধে নির্ধারিত $detectedSubject স্টাডি ব্লক।"
            )
            return "ঠিক আছে! আপনার জন্য $detectedSubject বিষয়ের একটি ৪৫ মিনিটের ডেডিকেটেড স্টাডি ব্লক রুটিনে তৈরি করা হয়েছে। শান্ত মাথায় পড়া শুরু করুন।"
        }

        // Case 3: Late return from school
        if (lower.contains("দেরি হবে") || lower.contains("দেরী হবে") || lower.contains("দেরি হয়েছে")) {
            addTask(
                title = "স্কুলপরবর্তী বিজ্ঞান দ্রুত রিভিশন",
                category = "STUDY",
                priority = "DAILY_STUDY_TARGET",
                durationMinutes = 35,
                timeBlock = "05:15 PM - 05:50 PM",
                subjectOrArea = "বিজ্ঞান",
                notes = "দেরিতে বাড়ি ফেরায় বিকেলের স্টাডি টাইম সংক্ষেপ করা হয়েছে।"
            )
            return "ঠিক আছে মুবাশ্বির। আপনার স্কুল থেকে ফিরতে দেরি হওয়ার কারণে বিকেলের পড়াশোনা সংক্ষেপ করে সন্ধ্যা ৬:০০ টার কোচিংয়ের আগে মানিয়ে দেওয়া হয়েছে। রাত ৯:৩০ এর পর বাকিটা কাভার করব।"
        }

        // Case 4: School off / holiday
        if (lower.contains("স্কুল নেই") || lower.contains("ছুটি") || lower.contains("বন্ধ")) {
            addTask(
                title = "ছুটির দিনের বিশেষ গণিত ও বিজ্ঞান প্রস্তুতি",
                category = "STUDY",
                priority = "EXAM_PREP",
                durationMinutes = 90,
                timeBlock = "09:00 AM - 10:30 AM",
                subjectOrArea = "সাধারণ গণিত",
                notes = "স্কুল ছুটি থাকায় সকালের শান্ত সময়ে বড় স্টাডি সেশন।"
            )
            return "স্কুল বন্ধ থাকায় সকাল ৯:০০ টা থেকে ১০:৩০ টা পর্যন্ত গণিতের বিশেষ ৯০ মিনিটের একটি বড় স্টাডি ব্লক রুটিনে তৈরি করে দেওয়া হয়েছে। বাকি সময় হালকা ড্রপশিপিং ও বিশ্রাম নেওয়া যাবে।"
        }

        // Case 5: Limited available time
        if (lower.contains("দুই ঘণ্টা") || lower.contains("২ ঘণ্টা") || lower.contains("২ ঘন্টা") || lower.contains("দুই ঘন্টা")) {
            addTask(
                title = "জরুরি এসএসসি রিভিশন ব্লক",
                category = "STUDY",
                priority = "EXAM_PREP",
                durationMinutes = 75,
                timeBlock = "04:30 PM - 05:45 PM",
                subjectOrArea = "গণিত ও বিজ্ঞান",
                notes = "সীমিত ২ ঘণ্টার মধ্যে সর্বোচ্চ একাডেমিক গুরুত্ব।"
            )
            return "আপনার হাতে থাকা ২ ঘণ্টার মধ্যে ১ ঘণ্টা ১৫ মিনিট সবচেয়ে দুর্বল বিষয় গণিত ও বিজ্ঞানের জন্য বরাদ্দ করা হয়েছে। বাকি ৪৫ মিনিট বিশ্রাম ও ডিনারে থাকবে, আজ ড্রপশিপিং স্থগিত রাখাই বুদ্ধিমানের কাজ।"
        }

        // Case 6: Dropshipping business more time request
        if ((lower.contains("business") || lower.contains("ব্যবসা") || lower.contains("ড্রপশিপিং")) && (lower.contains("বেশি") || lower.contains("করতে চাই"))) {
            val pendingStudy = todayTasks.value.filter { it.category == "STUDY" && !it.isCompleted }
            return if (pendingStudy.isNotEmpty()) {
                "ক্লাস ৯-এর এসএসসি পড়াশোনা আপনার ১ নম্বর অগ্রাধিকার। এখনও আপনার ${pendingStudy.size}টি একাডেমিক টাস্ক বাকি আছে। সেগুলো শেষ করার পর সর্বোচ্চ ৪৫ মিনিট ড্রপশিপিংয়ের জন্য সময় দেওয়া যেতে পারে।"
            } else {
                "চমৎকার! আজকের একাডেমিক পড়ার টার্গেট শেষ থাকায় আপনি ৪৫ মিনিট আলিয়েক্সপ্রেস প্রোডাক্ট রিসার্চ ও ক্রিয়েটিভ নিয়ে কাজ করতে পারেন।"
            }
        }

        return null
    }

    private fun buildBengaliSystemPrompt(): String {
        val currentTasks = todayTasks.value
        val weakChapters = chapters.value.filter { it.isWeak }
        val settings = userSettings.value

        return """
            তুমি হলে StudyBiz OS বাংলা পার্সোনাল এআই সহকারী (Personal AI Coach)।
            ব্যবহারকারী: মুবাশ্বির (ক্লাস ৯ এসএসসি পরীক্ষার্থী, সাথে শপিফাই ড্রপশিপিং শিখছে)।
            
            কঠোর মূলনীতি:
            ১. একাডেমিক পড়াশোনা (Class 9 SSC) = ১ নম্বর প্রায়োরিটি।
            ২. ড্রপশিপিং ব্যবসা = ২ নম্বর প্রায়োরিটি (এটি কেবল শেখার প্ল্যাটফর্ম, কোনো অলৌকিক আয়ের নিশ্চয়তা দেওয়া যাবে না)।
            ৩. ঘুম ও স্বাস্থ্য = বাধ্যতামূলক (${settings.dailySleepHoursGoal} ঘণ্টা ঘুম কোনোভাবেই কমানো যাবে না)।
            
            দৈনন্দিন নির্ধারিত সময়সূচি:
            - সাধারণত ফজরের ২-৩ ঘণ্টা আগে ঘুম থেকে ওঠা (গভীর মনোযোগের স্টাডি সময়)
            - সকাল ৭:৩০ টায় স্কুলের উদ্দেশ্যে রওনা, স্কুল: সকাল ৮:০০টা - বিকাল ৩:৪০টা
            - নাইট কোচিং: সন্ধ্যা ৬:০০টা - রাত ৯:৩০টা
            - রাতের ঘুম: রাত ১০:৩০ বা ১১:০০ টার মধ্যে ঘুমানো বাধ্যতামূলক
            
            বর্তমান অবস্থা:
            - দুর্বল বিষয়/অধ্যায়: ${weakChapters.joinToString { it.title }}
            - বাকি পড়াশোনা: ${currentTasks.filter { it.category == "STUDY" && !it.isCompleted }.joinToString { it.title }}
            
            নির্দেশনা:
            সবসময় খাঁটি, মিষ্টি ও সহায়ক বাংলাদেশি বাংলায় উত্তর দেবে। কোনো অবস্থাতেই পড়াশোনা বা ঘুম নষ্ট করে ড্রপশিপিং করতে উৎসাহ দেবে না। ছোট ও বুলেট পয়েন্টে সুনির্দিষ্ট রুটিন দেবে।
        """.trimIndent()
    }

    private fun generateLocalBengaliAdvice(prompt: String): String {
        val lower = prompt.lowercase(Locale.ROOT)
        return when {
            lower.contains("physics") || lower.contains("পদার্থ") || lower.contains("বিজ্ঞান") -> {
                "পদার্থবিজ্ঞান ও বিজ্ঞানের জন্য আজ রাতের কোচিংয়ের পর ২০ মিনিট শুধু সূত্রের গাণিতিক সমাধান প্র্যাকটিস করুন। বাকিটা কাল ফজরের আগের ফ্রেশ মাইন্ডে রিভিশন করুন।"
            }
            lower.contains("রুটিন") || lower.contains("routine") || lower.contains("প্ল্যান") -> {
                "আজকের রুটিন:\n• ভোর ৪:১৫ - ৫:১৫: গণিত দুর্বল অধ্যায়\n• সকাল ৮:০০ - ৩:৪০: স্কুল\n• বিকাল ৪:৩০ - ৫:২০: বিজ্ঞান\n• সন্ধ্যা ৬:০০ - ৯:৩০: নাইট কোচিং\n• রাত ৯:৪৫ - ১০:১৫: হালকা ড্রপশিপিং রিসার্চ\n• রাত ১০:৩০: ঘুম।"
            }
            else -> {
                "মুবাশ্বির, আমি আপনার সম্পূর্ণ রুটিন ও পড়ার তালিকা পর্যবেক্ষণ করছি। ক্লাস ৯-এর এসএসসি পড়াশোনা ও পর্যাপ্ত ঘুম আগে বজায় রাখুন, ড্রপশিপিং অবসর সময়ে করুন।"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
