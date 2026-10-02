package com.fyrefly.fireflycollege.data.model

/** Shared presentation models: assignment joined with its course, and course progress. */
data class AssignmentItem(
    val assignment: Assignment,
    val course: Course?
)

data class CourseSummary(
    val course: Course,
    val total: Int,
    val completed: Int
) {
    val remaining: Int get() = total - completed
    val fraction: Float get() = if (total == 0) 0f else completed.toFloat() / total
}
