package ai.antigem.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// ============================================================================
// Voice Subsystem - STT + TTS
// ============================================================================

data class VoiceState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val language: String = "en-US",
    val available: Boolean = false
)

class VoiceManager(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var recognitionListener: RecognitionListener? = null

    private val _voiceState = MutableStateFlow(VoiceState())
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private var onSpeechResult: ((String) -> Unit)? = null
    private var onSpeechError: ((String) -> Unit)? = null

    init {
        val isRecognitionAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        _voiceState.update { it.copy(available = isRecognitionAvailable) }

        if (isRecognitionAvailable) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognitionListener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle) {}
                override fun onBeginningOfSpeech() {
                    _voiceState.update { it.copy(isListening = true) }
                }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray) {}
                override fun onEndOfSpeech() {
                    _voiceState.update { it.copy(isListening = false) }
                }
                override fun onError(error: Int) {
                    _voiceState.update { it.copy(isListening = false) }
                    onSpeechError?.invoke("Speech recognition error: $error")
                }
                override fun onResults(results: Bundle) {
                    _voiceState.update { it.copy(isListening = false) }
                    val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        onSpeechResult?.invoke(matches[0])
                    }
                }
                override fun onPartialResults(partialResults: Bundle) {}
                override fun onEvent(eventType: Int, params: Bundle) {}
            }
            speechRecognizer?.setRecognitionListener(recognitionListener)
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // TTS ready
            }
        }
    }

    fun startListening(
        language: String = "en-US",
        onResult: (String) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        this.onSpeechResult = onResult
        this.onSpeechError = onError

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
        speechRecognizer?.startListening(intent)
        _voiceState.update { it.copy(isListening = true, language = language) }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.cancel()
        _voiceState.update { it.copy(isListening = false) }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        tts?.let { tts ->
            _voiceState.update { it.copy(isSpeaking = true) }
            val params = Bundle()
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utteranceId")
            // Simple completion - TTS callback not wired for brevity
            _voiceState.update { it.copy(isSpeaking = false) }
            onComplete?.invoke()
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _voiceState.update { it.copy(isSpeaking = false) }
    }

    fun setLanguage(language: String) {
        tts?.setLanguage(java.util.Locale(language.take(2), language.substringAfter("-").takeIf { it.isNotEmpty() } ?: "US"))
        _voiceState.update { it.copy(language = language) }
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun getAvailableLanguages(): List<String> {
        return listOf("en-US", "en-IN", "hi-IN") // English US, English India, Hindi (Hinglish)
    }

    fun isTtsAvailable(): Boolean = tts != null

    fun shutdown() {
        stopListening()
        stopSpeaking()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
