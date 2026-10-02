package com.fyrefly.fireflycollege.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val COURSES = "courses"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val SEARCH = "search"

    const val COURSE_DETAIL = "course/{courseId}"
    const val COURSE_EDITOR = "courseEditor?courseId={courseId}"
    const val ASSIGNMENT_EDITOR = "assignmentEditor?assignmentId={assignmentId}&courseId={courseId}"

    fun courseDetail(courseId: Long): String = "course/$courseId"

    fun courseEditor(courseId: Long?): String =
        if (courseId != null) "courseEditor?courseId=$courseId" else "courseEditor"

    fun assignmentEditor(assignmentId: Long?, courseId: Long?): String {
        var route = "assignmentEditor"
        val params = buildList {
            if (assignmentId != null) add("assignmentId=$assignmentId")
            if (courseId != null) add("courseId=$courseId")
        }
        if (params.isNotEmpty()) route += "?" + params.joinToString("&")
        return route
    }
}
