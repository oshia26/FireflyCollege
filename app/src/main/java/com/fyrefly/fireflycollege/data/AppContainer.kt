package com.fyrefly.fireflycollege.data

import android.content.Context
import androidx.room.Room
import com.fyrefly.fireflycollege.data.database.FireflyDatabase
import com.fyrefly.fireflycollege.data.repository.AssignmentRepository
import com.fyrefly.fireflycollege.data.repository.CourseRepository

/** Manual dependency container — the single place that wires the data layer together. */
class AppContainer(context: Context) {

    private val database: FireflyDatabase = Room.databaseBuilder(
        context.applicationContext,
        FireflyDatabase::class.java,
        FireflyDatabase.NAME
    ).build()

    val courseRepository: CourseRepository = CourseRepository(database.courseDao())
    val assignmentRepository: AssignmentRepository = AssignmentRepository(database.assignmentDao())
}
