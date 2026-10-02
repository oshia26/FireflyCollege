package com.fyrefly.fireflycollege.notification

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fyrefly.fireflycollege.MainActivity
import com.fyrefly.fireflycollege.R
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isTest = intent.action == ACTION_TEST_REMINDER
        val assignmentId = intent.getLongExtra(EXTRA_ASSIGNMENT_ID, -1L)
        val slot = intent.getIntExtra(EXTRA_SLOT, ReminderCoordinator.SLOT_AT_DEADLINE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (!isTest && assignmentId <= 0) return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isTest) {
                    post(
                        context,
                        "Test reminder",
                        "Deadlines will appear like this: 24 hours before, and at the deadline.",
                        ReminderCoordinator.SLOT_AT_DEADLINE
                    )
                } else {
                    val db = FireflyDatabase.create(context)
                    try {
                        val assignment = db.assignmentDao().getById(assignmentId)
                        if (assignment != null && !assignment.isCompleted) {
                            val course = db.courseDao().getById(assignment.courseId)
                            val courseName = course?.name.orEmpty()
                            val title = if (slot == ReminderCoordinator.SLOT_DAY_BEFORE) {
                                "Due tomorrow"
                            } else {
                                "Deadline now"
                            }
                            post(
                                context = context,
                                title = title,
                                body = sequenceOf(
                                    assignment.title,
                                    courseName.takeIf { it.isNotBlank() }?.let { "· $it" }
                                ).filterNotNull().joinToString(" ")
                            , slot = slot
                            )
                        }
                    } finally {
                        db.close()
                    }
                }
            } finally {
                result.finish()
            }
        }
    }

    private fun post(context: Context, title: String, body: String, slot: Int) {
        val openApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            slot,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderChannels.DEADLINES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setColor(0xFF8AF0B8.toInt())
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        NotificationManagerCompat.from(context).notify(slot, notification)
    }

    companion object {
        const val ACTION_ASSIGNMENT_REMINDER = "com.fyrefly.fireflycollege.REMINDER_FIRE"
        const val ACTION_TEST_REMINDER = "com.fyrefly.fireflycollege.REMINDER_TEST"
        const val EXTRA_ASSIGNMENT_ID = "assignmentId"
        const val EXTRA_SLOT = "slot"
    }
}
