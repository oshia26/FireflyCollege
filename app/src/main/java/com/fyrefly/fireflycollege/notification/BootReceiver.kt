package com.fyrefly.fireflycollege.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import com.fyrefly.fireflycollege.data.database.entity.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-arms all deadline reminders after reboot, time change or timezone change.
 * Non-exported — the system itself delivers these protected broadcasts.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> Unit
            else -> return
        }
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val coordinator = ReminderCoordinator(appContext)
                val db = FireflyDatabase.create(appContext)
                try {
                    val now = System.currentTimeMillis()
                    db.assignmentDao().getAll()
                        .filter { !it.isCompleted && (it.dueAt > now || it.dueAt - ReminderCoordinator.TWENTY_FOUR_HOURS > now) }
                        .forEach { coordinator.refreshFor(it.toDomain()) }
                } finally {
                    db.close()
                }
            } finally {
                result.finish()
            }
        }
    }
}
