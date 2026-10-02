package com.fyrefly.fireflycollege.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.fyrefly.fireflycollege.data.dao.AssignmentDao
import com.fyrefly.fireflycollege.data.dao.CourseDao
import com.fyrefly.fireflycollege.data.database.entity.AssignmentEntity
import com.fyrefly.fireflycollege.data.database.entity.CourseEntity
import com.fyrefly.fireflycollege.data.model.Priority

class EnumConverters {

    @TypeConverter
    fun priorityToString(priority: Priority): String = priority.name

    @TypeConverter
    fun stringToPriority(value: String): Priority =
        runCatching { Priority.valueOf(value) }.getOrDefault(Priority.MEDIUM)
}

@Database(
    entities = [CourseEntity::class, AssignmentEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(EnumConverters::class)
abstract class FireflyDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun assignmentDao(): AssignmentDao

    companion object {
        const val NAME = "fireflycollege.db"

        /** One-shot instance for receivers/backup — open, use, close. */
        fun create(context: android.content.Context): FireflyDatabase =
            androidx.room.Room.databaseBuilder(
                context.applicationContext,
                FireflyDatabase::class.java,
                NAME
            ).build()
    }
}
