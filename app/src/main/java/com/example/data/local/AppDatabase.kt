package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.StudyBizDao
import com.example.data.local.model.BusinessProductEntity
import com.example.data.local.model.ChapterEntity
import com.example.data.local.model.FocusSessionEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.SubjectEntity
import com.example.data.local.model.TaskEntity
import com.example.data.local.model.TestScoreEntity
import com.example.data.local.model.UserSettingsEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        SubjectEntity::class,
        ChapterEntity::class,
        BusinessProductEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        TestScoreEntity::class,
        GoalEntity::class,
        UserSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyBizDao(): StudyBizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_biz_os_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

        suspend fun seedInitialData(dao: StudyBizDao) {
            val existingSettings = dao.getUserSettingsDirect()
            if (existingSettings != null) return // Already seeded

            // 1. User Settings
            dao.saveUserSettings(
                UserSettingsEntity(
                    id = 1,
                    studentName = "মুবাশ্বির",
                    gradeLevel = "নবম শ্রেণি (SSC প্রস্তুতি)",
                    dailyStudyHourTarget = 4.5,
                    dailyBusinessHourCap = 1.5,
                    dailySleepHoursGoal = 8.0,
                    studyFocusMinutes = 50,
                    studyBreakMinutes = 10,
                    businessFocusMinutes = 30,
                    businessBreakMinutes = 5,
                    streakCount = 7,
                    customGeminiApiKey = "",
                    schoolStartTime = "08:00 AM",
                    schoolEndTime = "03:40 PM",
                    coachingStartTime = "06:00 PM",
                    coachingEndTime = "09:30 PM",
                    wakeUpBeforeFajrHours = 2.5
                )
            )

            // 2. Class 9 SSC Subjects (বাংলায়)
            val mathId = dao.insertSubject(
                SubjectEntity(
                    name = "সাধারণ গণিত",
                    colorHex = "#2563EB",
                    totalChapters = 12,
                    completedChapters = 5,
                    isWeak = true,
                    targetScore = 95
                )
            )

            val scienceId = dao.insertSubject(
                SubjectEntity(
                    name = "বিজ্ঞান (পদার্থ, রসায়ন, জীব)",
                    colorHex = "#059669",
                    totalChapters = 14,
                    completedChapters = 7,
                    isWeak = false,
                    targetScore = 92
                )
            )

            val englishId = dao.insertSubject(
                SubjectEntity(
                    name = "ইংরেজি (English)",
                    colorHex = "#7C3AED",
                    totalChapters = 10,
                    completedChapters = 6,
                    isWeak = false,
                    targetScore = 90
                )
            )

            val socialId = dao.insertSubject(
                SubjectEntity(
                    name = "বাংলাদেশ ও বিশ্বপরিচয়",
                    colorHex = "#D97706",
                    totalChapters = 15,
                    completedChapters = 6,
                    isWeak = false,
                    targetScore = 88
                )
            )

            val itId = dao.insertSubject(
                SubjectEntity(
                    name = "তথ্য ও যোগাযোগ প্রযুক্তি (ICT)",
                    colorHex = "#0284C7",
                    totalChapters = 6,
                    completedChapters = 4,
                    isWeak = false,
                    targetScore = 98
                )
            )

            // 3. Chapters
            dao.insertChapters(
                listOf(
                    // গণিত
                    ChapterEntity(subjectId = mathId, title = "বাস্তব সংখ্যা (অধ্যায় ১)", chapterNumber = 1, status = "COMPLETED", completionPercent = 100),
                    ChapterEntity(subjectId = mathId, title = "বীজগাণিতিক রাশি (অধ্যায় ৩)", chapterNumber = 2, status = "COMPLETED", completionPercent = 100),
                    ChapterEntity(subjectId = mathId, title = "সূচক ও লগারিদম (অধ্যায় ৪)", chapterNumber = 3, status = "COMPLETED", completionPercent = 100),
                    ChapterEntity(subjectId = mathId, title = "দুই চলকবিশিষ্ট সরল সহসমীকরণ", chapterNumber = 4, status = "IN_PROGRESS", completionPercent = 65, isWeak = true, needsRevision = true),
                    ChapterEntity(subjectId = mathId, title = "ত্রিভুজ ও উপপাদ্য (অধ্যায় ৬)", chapterNumber = 5, status = "IN_PROGRESS", completionPercent = 40, isWeak = true),
                    ChapterEntity(subjectId = mathId, title = "ব্যবহারিক জ্যামিতি (অধ্যায় ৭)", chapterNumber = 6, status = "NOT_STARTED", completionPercent = 0),
                    ChapterEntity(subjectId = mathId, title = "পরিমিতি ও পরিমাপ", chapterNumber = 7, status = "NOT_STARTED", completionPercent = 0, isWeak = true),
                    // বিজ্ঞান
                    ChapterEntity(subjectId = scienceId, title = "বল ও গতির সূত্রাবলি", chapterNumber = 1, status = "COMPLETED", completionPercent = 100, needsRevision = true),
                    ChapterEntity(subjectId = scienceId, title = "পদার্থের অবস্থা ও গঠন", chapterNumber = 2, status = "COMPLETED", completionPercent = 100),
                    ChapterEntity(subjectId = scienceId, title = "জীবকোষ ও টিস্যু", chapterNumber = 3, status = "COMPLETED", completionPercent = 100),
                    ChapterEntity(subjectId = scienceId, title = "কাজ, ক্ষমতা ও শক্তি", chapterNumber = 4, status = "IN_PROGRESS", completionPercent = 50),
                    ChapterEntity(subjectId = scienceId, title = "মহাকর্ষ ও অভিকর্ষ", chapterNumber = 5, status = "NOT_STARTED", completionPercent = 0)
                )
            )

            // 4. Dropshipping Products (AliExpress Research DB)
            dao.insertProduct(
                BusinessProductEntity(
                    name = "Portable Ultrasonic Desk Humidifier",
                    supplierName = "AliExpress Global",
                    supplierUrl = "https://aliexpress.com/item/example1",
                    costPrice = 3.80,
                    sellingPrice = 19.99,
                    estimatedMargin = 16.19,
                    status = "WINNER",
                    category = "Home & Living",
                    notes = "ফেসবুক ও রিলসে বেশ ট্রেন্ডিং। শিপিং খরচ কম এবং হালকা।"
                )
            )
            dao.insertProduct(
                BusinessProductEntity(
                    name = "Ergonomic Neck Cloud Traction Pillow",
                    supplierName = "AliExpress Direct",
                    supplierUrl = "https://aliexpress.com/item/example2",
                    costPrice = 5.20,
                    sellingPrice = 24.99,
                    estimatedMargin = 19.79,
                    status = "TESTING",
                    category = "Health & Wellness",
                    notes = "ঘাড়ের ব্যথার ভিডিও ক্রিয়েটিভ বানাতে হবে।"
                )
            )
            dao.insertProduct(
                BusinessProductEntity(
                    name = "Magnetic Cable Organizer Pods",
                    supplierName = "AliExpress Sourcing",
                    supplierUrl = "https://aliexpress.com/item/example3",
                    costPrice = 1.90,
                    sellingPrice = 12.99,
                    estimatedMargin = 11.09,
                    status = "READY_TO_LAUNCH",
                    category = "Gadgets",
                    notes = "সহজে বান্ডেল অফার ও আপসেল করার মতো গ্যাজেট।"
                )
            )

            // 5. Initial Tasks for Today (ফজরের আগে, স্কুল ও নাইট কোচিং ব্যালান্স করে)
            val today = getTodayDateString()
            dao.insertTasks(
                listOf(
                    TaskEntity(
                        title = "ফজরের আগে গণিত: দুই চলকবিশিষ্ট সরল সমীকরণ রিভিশন",
                        category = "STUDY",
                        priority = "WEAK_ACADEMIC",
                        durationMinutes = 60,
                        dateString = today,
                        timeBlock = "04:15 AM - 05:15 AM",
                        isCompleted = false,
                        subjectOrArea = "সাধারণ গণিত",
                        notes = "দুর্বল অধ্যায়: বোর্ড প্রশ্নের গাণিতিক সমস্যা সমাধান।"
                    ),
                    TaskEntity(
                        title = "বিজ্ঞান: বল ও গতির সূত্রের সৃজনশীল প্রস্তুতি",
                        category = "STUDY",
                        priority = "DAILY_STUDY_TARGET",
                        durationMinutes = 50,
                        dateString = today,
                        timeBlock = "04:30 PM - 05:20 PM",
                        isCompleted = false,
                        subjectOrArea = "বিজ্ঞান",
                        notes = "স্কুল থেকে ফিরে ফ্রেশ হয়ে কোচিংয়ের আগে শেষ করতে হবে।"
                    ),
                    TaskEntity(
                        title = "ইংরেজি ১ম পত্র: Passage Reading & Vocabulary",
                        category = "STUDY",
                        priority = "DAILY_STUDY_TARGET",
                        durationMinutes = 30,
                        dateString = today,
                        timeBlock = "05:25 PM - 05:55 PM",
                        isCompleted = true,
                        subjectOrArea = "ইংরেজি",
                        notes = "মডেল টেস্টের অনুচ্ছেদ পড়া সম্পন্ন।"
                    ),
                    TaskEntity(
                        title = "AliExpress ড্রপশিপিং: ৩টি উইনিং প্রোডাক্ট যাচাই",
                        category = "BUSINESS",
                        priority = "IMPORTANT_BUSINESS",
                        durationMinutes = 35,
                        dateString = today,
                        timeBlock = "09:45 PM - 10:20 PM",
                        isCompleted = false,
                        subjectOrArea = "প্রোডাক্ট রিসার্চ",
                        notes = "কোচিং শেষ করে ডিনারের পর। মার্জিন ১৫ ডলারের বেশি হতে হবে।"
                    ),
                    TaskEntity(
                        title = "ক্যানভায় ফেসবুক বিজ্ঞাপনের ২টি ভিডিও হুক ডিজাইন",
                        category = "BUSINESS",
                        priority = "OPTIONAL_BUSINESS",
                        durationMinutes = 20,
                        dateString = today,
                        timeBlock = "10:20 PM - 10:40 PM",
                        isCompleted = false,
                        subjectOrArea = "ক্রিয়েটিভ তৈরি",
                        notes = "শুধুমাত্র পড়াশোনা শেষ হলে করব, ঘুমাতে যাওয়ার আগে সময় পেলে।"
                    )
                )
            )

            // 6. Focus Sessions
            dao.insertFocusSession(
                FocusSessionEntity(
                    sessionType = "STUDY",
                    targetDurationMinutes = 50,
                    actualDurationMinutes = 50,
                    dateString = today,
                    associatedTopic = "গণিত - সমীকরণ সমাধান",
                    completedSuccessfully = true
                )
            )

            // 7. Goals
            dao.insertGoal(
                GoalEntity(
                    title = "গণিত: সরল সমীকরণ ও ত্রিভুজ উপপাদ্য পুরোপুরি শেষ করা",
                    timeframe = "WEEKLY",
                    category = "STUDY",
                    targetValue = 2.0,
                    currentValue = 1.0,
                    unit = "অধ্যায়",
                    isCompleted = false
                )
            )
            dao.insertGoal(
                GoalEntity(
                    title = "এই সপ্তাহে মোট ২৫ ঘণ্টা একাডেমিক পড়াশোনা",
                    timeframe = "WEEKLY",
                    category = "STUDY",
                    targetValue = 25.0,
                    currentValue = 18.5,
                    unit = "ঘণ্টা",
                    isCompleted = false
                )
            )
            dao.insertGoal(
                GoalEntity(
                    title = "৫টি ভালো মার্জিনের ড্রপশিপিং প্রোডাক্ট নির্বাচন",
                    timeframe = "WEEKLY",
                    category = "BUSINESS",
                    targetValue = 5.0,
                    currentValue = 3.0,
                    unit = "টি প্রোডাক্ট",
                    isCompleted = false
                )
            )
            dao.insertGoal(
                GoalEntity(
                    title = "এসএসসি মডেল টেস্টে ৯০%+ নম্বর অর্জন",
                    timeframe = "MONTHLY",
                    category = "STUDY",
                    targetValue = 90.0,
                    currentValue = 84.0,
                    unit = "%",
                    isCompleted = false
                )
            )
        }
    }
}
