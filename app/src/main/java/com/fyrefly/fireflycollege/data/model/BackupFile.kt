package com.fyrefly.fireflycollege.data.model

import kotlinx.serialization.Serializable

/** Portable backup envelope. version 1: full replace on import. */
@Serializable
data class BackupFile(
    val format: String = "fireflycollege-backup",
    val version: Int = 1,
    val exportedAt: Long,
    val courses: List<Course> = emptyList(),
    val assignments: List<Assignment> = emptyList()
)
