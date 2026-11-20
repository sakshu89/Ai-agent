package ai.antigem.assistant.agent

import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.IBinder

class TaskProcessorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Foreground service for task processing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_channel",
                "Task Processing",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)

            val notification = Notification.Builder(this, "task_channel")
                .setContentTitle("AntiGem Assistant")
                .setContentText("Processing background tasks")
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .build()
            startForeground(1001, notification)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
