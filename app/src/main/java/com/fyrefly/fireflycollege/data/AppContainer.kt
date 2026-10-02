package com.fyrefly.fireflycollege.data

import android.content.Context
import androidx.room.Room
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import com.fyrefly.fireflycollege.data.repository.AssignmentRepository
import com.fyrefly.fireflycollege.data.repository.BackupRepository
import com.fyrefly.fireflycollege.data.repository.CourseRepository
import com.fyrefly.fireflycollege.data.settings.SettingsStore
import com.fyrefly.fireflycollege.notification.ReminderCoordinator

/** Manual dependency container — the single place that wires the data layer together. */
class AppContainer(context: Context) {

    val database: FireflyDatabase = Room.databaseBuilder(
        context.applicationContext,
        FireflyDatabase::class.java,
        FireflyDatabase.NAME
    ).build()

    val courseRepository: CourseRepository = CourseRepository(database.courseDao())
    val assignmentRepository: AssignmentRepository = AssignmentRepository(database.assignmentDao())
    val backupRepository: BackupRepository = BackupRepository(context.applicationContext, database)
    val settingsStore: SettingsStore = SettingsStore(context.applicationContext)
    val reminderCoordinator: ReminderCoordinator = ReminderCoordinator(context.applicationContext)
}
