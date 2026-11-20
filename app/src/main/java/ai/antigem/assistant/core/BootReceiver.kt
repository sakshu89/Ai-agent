package ai.antigem.assistant.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == android.content.Intent.ACTION_BOOT_COMPLETED) {
            // Resume any paused tasks on boot
            // Could start TaskProcessorService here
        }
    }
}
