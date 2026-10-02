package com.fyrefly.fireflycollege.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import com.fyrefly.fireflycollege.data.database.entity.toDomain
import com.fyrefly.fireflycollege.data.model.Assignment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Schedules the two standard reminders for every incomplete assignment:
 *   slot 0 — 24 hours before the deadline ("due tomorrow")
 *   slot 1 — at the deadline ("due now")
 * Receiver has to resolve the fresh state when it fires (assignment may be
 * completed / deleted in between), so scheduling here stays dumb and cheap.
 */
class ReminderCoordinator(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun testReminder() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TEST_REMINDER
        }
        val pending = PendingIntent.getBroadcast(
            context,
            REQUEST_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10_000,
                pending
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10_000,
                pending
            )
        }
    }

    fun refreshFor(assignment: Assignment) {
        // Always clear both slots first — cheap and idempotent.
        cancelFor(assignment.id)
        if (assignment.isCompleted) return
        val now = System.currentTimeMillis()
        val targets = listOfNotNull(
            if (assignment.dueAt - TWENTY_FOUR_HOURS > now) assignment.dueAt - TWENTY_FOUR_HOURS to SLOT_DAY_BEFORE else null,
            if (assignment.dueAt > now) assignment.dueAt to SLOT_AT_DEADLINE else null
        )
        targets.forEach { (time, slot) -> schedule(assignment.id, time, slot) }
    }

    fun cancelFor(assignmentId: Long) {
        forEachSlot(assignmentId) { slot ->
            intentFor(assignmentId, slot, AlarmReceiver.ACTION_ASSIGNMENT_REMINDER)
                .let { pendingFor(it, requestCode(assignmentId, slot)) }
                .let(alarmManager::cancel)
        }
    }

    fun cancelAssignments(ids: List<Long>) {
        ids.forEach { cancelFor(it) }
    }

    /** Reschedules every incomplete future assignment. Called on app start + after boot. */
    fun refreshAll() {
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val db = FireflyDatabase.create(appContext)
            try {
                val now = System.currentTimeMillis()
                val pending = db.assignmentDao().getAll()
                    .filter { !it.isCompleted && (it.dueAt > now || it.dueAt - TWENTY_FOUR_HOURS > now) }
                    .map { it.toDomain() }
                pending.forEach { refreshFor(it) }
            } finally {
                db.close()
            }
        }
    }

    private fun schedule(assignmentId: Long, atTime: Long, slot: Int) {
        val pending = intentFor(assignmentId, slot, AlarmReceiver.ACTION_ASSIGNMENT_REMINDER)
            .let { pendingFor(it, requestCode(assignmentId, slot)) }
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atTime, pending)
        } else {
            // Permission not granted — deliver within maintenance windows instead of crashing.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atTime, pending)
        }
    }

    private fun intentFor(assignmentId: Long, slot: Int, action: String): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmReceiver.EXTRA_ASSIGNMENT_ID, assignmentId)
            putExtra(AlarmReceiver.EXTRA_SLOT, slot)
        }

    private fun pendingFor(intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun forEachSlot(assignmentId: Long, block: (Int) -> Unit) {
        block(SLOT_DAY_BEFORE)
        block(SLOT_AT_DEADLINE)
    }

    private fun requestCode(assignmentId: Long, slot: Int): Int = (assignmentId * 10 + slot).toInt()

    companion object {
        const val SLOT_DAY_BEFORE = 0
        const val SLOT_AT_DEADLINE = 1
        const val REQUEST_TEST = 1
        const val TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000L
    }
}
