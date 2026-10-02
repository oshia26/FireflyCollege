package com.fyrefly.fireflycollege.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object ReminderChannels {

    const val DEADLINES = "deadlines"

    fun ensure(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            DEADLINES,
            "Deadline reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders before college assignment deadlines"
            enableVibration(true)
        }
        // Recreating with same id is a no-op unless settings changed
        manager.createNotificationChannel(channel)
    }
}
