package com.fyrefly.fireflycollege.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority { LOW, MEDIUM, HIGH }

val Priority.label: String
    get() = when (this) {
        Priority.LOW -> "Low"
        Priority.MEDIUM -> "Medium"
        Priority.HIGH -> "High"
    }
