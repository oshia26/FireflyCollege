package com.fyrefly.fireflycollege.data.model

// Curated course colors — firefly-night palette (store as ARGB longs, no Compose dependency).
object CoursePalette {
    val colors: List<Long> = listOf(
        0xFF5AAE88, // firefly green
        0xFF3A7CA5, // night blue
        0xFF7B5AA6, // dusk violet
        0xFFD97757, // lantern orange
        0xFFD1495B, // ember red
        0xFF2E9E8F, // lake teal
        0xFFB08968, // chestnut
        0xFF6A8532 // deep leaf
    )

    /** Picks the least-used color for a new course given the existing ones. */
    fun nextColor(existing: List<Long>): Long =
        colors.firstOrNull { it !in existing } ?: colors[existing.size % colors.size]
}
