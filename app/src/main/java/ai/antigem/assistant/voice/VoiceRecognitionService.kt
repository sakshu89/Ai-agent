package ai.antigem.assistant.voice

import android.content.Context
import android.content.Intent
import android.service.voice.VoiceInteractionService

// ============================================================================
// Voice Recognition Service - Optional voice interaction integration
// ============================================================================

class VoiceRecognitionService : VoiceInteractionService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onVoiceInteractionStart() {
        // Called when user triggers voice assistant
    }

    override fun onVoiceInteractionService() {
        // Voice interaction started
    }
}
