package com.fyrefly.fireflycollege.data.model

data class Course(
    val id: Long = 0,
    val name: String,
    val lecturer: String? = null,
    val room: String? = null,
    val colorArgb: Long,
    val createdAt: Long
)
