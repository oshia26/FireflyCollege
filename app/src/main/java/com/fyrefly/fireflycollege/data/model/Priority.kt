package com.fyrefly.fireflycollege.data.model

enum class Priority { LOW, MEDIUM, HIGH }

val Priority.label: String
    get() = when (this) {
        Priority.LOW -> "Low"
        Priority.MEDIUM -> "Medium"
        Priority.HIGH -> "High"
    }
