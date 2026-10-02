package com.fyrefly.fireflycollege.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "assignments",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val courseId: Long,
    val createdAt: Long,
    val dueAt: Long,
    val priority: String,
    val isCompleted: Boolean,
    val completedAt: Long? = null,
    val notes: String? = null
)
