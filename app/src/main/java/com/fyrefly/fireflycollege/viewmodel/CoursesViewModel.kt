package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.CourseSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CoursesUiState(
    val isLoading: Boolean = true,
    val summaries: List<CourseSummary> = emptyList()
)

class CoursesViewModel(container: AppContainer) : ViewModel() {

    private val courseRepo = container.courseRepository
    private val assignmentRepo = container.assignmentRepository

    val state: StateFlow<CoursesUiState> = combine(
        courseRepo.observeAll(),
        assignmentRepo.observeAll()
    ) { courses, assignments ->
        CoursesUiState(
            isLoading = false,
            summaries = courses.map { course ->
                val own = assignments.filter { it.courseId == course.id }
                CourseSummary(
                    course = course,
                    total = own.size,
                    completed = own.count { it.isCompleted }
                )
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CoursesUiState()
    )

    fun delete(courseId: Long) {
        viewModelScope.launch { courseRepo.delete(courseId) }
    }
}
