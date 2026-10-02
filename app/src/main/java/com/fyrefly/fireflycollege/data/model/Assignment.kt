package com.fyrefly.fireflycollege.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Assignment(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val courseId: Long,
    val createdAt: Long,
    val dueAt: Long,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String? = null
)
