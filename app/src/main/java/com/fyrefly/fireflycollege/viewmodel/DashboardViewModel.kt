package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.Assignment
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.data.model.CourseSummary
import com.fyrefly.fireflycollege.util.TimeFormats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class DashboardUiState(
    val isLoading: Boolean = true,
    val overdue: List<AssignmentItem> = emptyList(),
    val dueToday: List<AssignmentItem> = emptyList(),
    val dueSoon: List<AssignmentItem> = emptyList(),
    val completedRecently: List<AssignmentItem> = emptyList(),
    val courseSummaries: List<CourseSummary> = emptyList(),
    val hasAnyData: Boolean = false
)

class DashboardViewModel(container: AppContainer) : ViewModel() {

    private val assignmentsRepo = container.assignmentRepository
    private val courseRepo = container.courseRepository

    val state: StateFlow<DashboardUiState> = combine(
        assignmentsRepo.observeAll(),
        courseRepo.observeAll()
    ) { assignments, courses ->
        buildState(assignments, courses)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun toggleCompleted(assignment: Assignment) {
        viewModelScope.launch {
            val updated = assignment.copy(
                isCompleted = !assignment.isCompleted,
                completedAt = if (!assignment.isCompleted) System.currentTimeMillis() else null
            )
            assignmentsRepo.save(updated)
        }
    }

    fun delete(assignment: Assignment) {
        viewModelScope.launch { assignmentsRepo.delete(assignment.id) }
    }

    private fun buildState(
        assignments: List<Assignment>,
        courses: List<Course>
    ): DashboardUiState {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val dayStart = TimeFormats.startOfDay(today, zone)
        val dayEnd = TimeFormats.endOfDay(today, zone)
        val weekEnd = TimeFormats.endOfDay(today.plusDays(6), zone)

        val courseById = courses.associateBy { it.id }
        fun items(list: List<Assignment>) = list.map { AssignmentItem(it, courseById[it.courseId]) }

        val active = assignments.filter { !it.isCompleted }
        val overdue = items(active.filter { it.dueAt < dayStart })
        val dueToday = items(active.filter { it.dueAt in dayStart..dayEnd })
        val dueSoon = items(active.filter { it.dueAt in (dayEnd + 1)..weekEnd })
            .sortedBy { it.assignment.dueAt }

        val completed = items(assignments.filter { it.isCompleted })
            .sortedWith(compareByDescending { it.assignment.completedAt ?: 0L })

        val summaries = courses.map { course ->
            val courseAssignments = assignments.filter { it.courseId == course.id }
            CourseSummary(
                course = course,
                total = courseAssignments.size,
                completed = courseAssignments.count { it.isCompleted }
            )
        }.sortedByDescending { it.total }

        return DashboardUiState(
            isLoading = false,
            overdue = overdue,
            dueToday = dueToday,
            dueSoon = dueSoon,
            completedRecently = completed,
            courseSummaries = summaries,
            hasAnyData = assignments.isNotEmpty() || courses.isNotEmpty()
        )
    }
}
