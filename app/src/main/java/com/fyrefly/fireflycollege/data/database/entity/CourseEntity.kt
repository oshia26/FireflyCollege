package com.fyrefly.fireflycollege.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lecturer: String? = null,
    val room: String? = null,
    @ColumnInfo(name = "colorArgb") val colorArgb: Long,
    val createdAt: Long
)
