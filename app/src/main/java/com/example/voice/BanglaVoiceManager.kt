package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class BanglaVoiceManager(private val context: Context) : RecognitionListener {

    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(false)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    private val _rmsLevel = MutableStateFlow(0f)
    val rmsLevel: StateFlow<Float> = _rmsLevel.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _voiceStatusMessage = MutableStateFlow<String?>(null)
    val voiceStatusMessage: StateFlow<String?> = _voiceStatusMessage.asStateFlow()

    var onSpeechResultListener: ((String) -> Unit)? = null

    init {
        try {
            initTts()
        } catch (_: Exception) {
            isTtsInitialized = false
        }
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                try {
                    if (status == TextToSpeech.SUCCESS) {
                        val banglaBd = Locale.forLanguageTag("bn-BD")
                        val banglaIn = Locale.forLanguageTag("bn-IN")

                        val result = textToSpeech?.setLanguage(banglaBd)
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            textToSpeech?.setLanguage(banglaIn)
                        }

                        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                mainHandler.post {
                                    _isSpeaking.value = true
                                    _voiceStatusMessage.value = "এআই উত্তর দিচ্ছে..."
                                }
                            }

                            override fun onDone(utteranceId: String?) {
                                mainHandler.post {
                                    _isSpeaking.value = false
                                    _voiceStatusMessage.value = null
                                    // If continuous conversation mode is enabled, resume listening automatically
                                    if (_isContinuousMode.value) {
                                        mainHandler.postDelayed({
                                            if (_isContinuousMode.value && !_isSpeaking.value) {
                                                startListeningInternal()
                                            }
                                        }, 450)
                                    }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                mainHandler.post {
                                    _isSpeaking.value = false
                                    if (_isContinuousMode.value) {
                                        mainHandler.postDelayed({
                                            if (_isContinuousMode.value) startListeningInternal()
                                        }, 600)
                                    }
                                }
                            }
                        })

                        isTtsInitialized = true
                    }
                } catch (_: Exception) {
                    isTtsInitialized = false
                }
            }
        } catch (_: Exception) {
            isTtsInitialized = false
        }
    }

    fun toggleContinuousMode(): Boolean {
        val next = !_isContinuousMode.value
        setContinuousMode(next)
        return next
    }

    fun setContinuousMode(enabled: Boolean) {
        _isContinuousMode.value = enabled
        if (enabled) {
            _voiceStatusMessage.value = "কন্টিনিউয়াস মোড চালু হয়েছে। বলুন..."
            if (!_isSpeaking.value && !_isListening.value) {
                startListening()
            }
        } else {
            _voiceStatusMessage.value = "কন্টিনিউয়াস মোড বন্ধ"
            stopListening()
        }
    }

    fun startListening() {
        mainHandler.post {
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _voiceStatusMessage.value = "ডিভাইসে স্পিচ রিকগনিশন সুবিধা পাওয়া যাচ্ছে না"
                return
            }

            stopSpeaking()

            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext).apply {
                    setRecognitionListener(this@BanglaVoiceManager)
                }
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("bn-BD", "bn-IN"))
                putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় বলুন...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _voiceStatusMessage.value = if (_isContinuousMode.value) "কন্টিনিউয়াস মোড: শুনছি..." else "শুনছি... বাংলায় বলুন"
        } catch (e: Exception) {
            _isListening.value = false
            _voiceStatusMessage.value = "মাইক্রোফোন চালু করা যায়নি: ${e.message}"
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                _isListening.value = false
                _rmsLevel.value = 0f
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
        }
    }

    fun speak(text: String) {
        mainHandler.post {
            try {
                if (!isTtsInitialized || text.isBlank()) return@post
                stopListening()
                _isSpeaking.value = true
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "StudyBizUtteranceId")
            } catch (_: Exception) {
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        mainHandler.post {
            try {
                if (_isSpeaking.value) {
                    textToSpeech?.stop()
                    _isSpeaking.value = false
                }
            } catch (_: Exception) {
                _isSpeaking.value = false
            }
        }
    }

    fun stopAll() {
        _isContinuousMode.value = false
        stopSpeaking()
        stopListening()
        _voiceStatusMessage.value = null
    }

    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) {}
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        _voiceStatusMessage.value = if (_isContinuousMode.value) "কন্টিনিউয়াস: এখন বলুন..." else "এখন বাংলায় বলুন..."
    }

    override fun onBeginningOfSpeech() {
        _voiceStatusMessage.value = "শুনছি..."
    }

    override fun onRmsChanged(rmsdB: Float) {
        _rmsLevel.value = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _rmsLevel.value = 0f
        _voiceStatusMessage.value = "প্রসেসিং হচ্ছে..."
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _rmsLevel.value = 0f

        val isTimeoutOrNoMatch = error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
        val msg = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "কথা শোনা যায়নি"
            SpeechRecognizer.ERROR_NETWORK -> "ইন্টারনেট সংযোগ চেক করুন"
            SpeechRecognizer.ERROR_AUDIO -> "অডিও রেকর্ডিং ত্রুটি"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কিছু বলা হয়নি"
            else -> "ভয়েস ইনপুট ত্রুটি ($error)"
        }
        _voiceStatusMessage.value = msg

        // In continuous mode, automatically listen again after a brief pause if silence occurred
        if (_isContinuousMode.value && isTimeoutOrNoMatch) {
            mainHandler.postDelayed({
                if (_isContinuousMode.value && !_isSpeaking.value && !_isListening.value) {
                    startListeningInternal()
                }
            }, 1200)
        }
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _rmsLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val spoken = matches?.firstOrNull() ?: ""
        if (spoken.isNotBlank()) {
            _lastRecognizedText.value = spoken
            _voiceStatusMessage.value = null
            onSpeechResultListener?.invoke(spoken)
        } else if (_isContinuousMode.value) {
            mainHandler.postDelayed({
                if (_isContinuousMode.value && !_isSpeaking.value && !_isListening.value) {
                    startListeningInternal()
                }
            }, 1000)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull() ?: ""
        if (text.isNotBlank()) {
            _lastRecognizedText.value = text
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
