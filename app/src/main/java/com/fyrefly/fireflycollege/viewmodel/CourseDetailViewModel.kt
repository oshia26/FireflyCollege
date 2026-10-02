package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.Assignment
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    container: AppContainer,
    courseId: Long
) : ViewModel() {

    private val courseRepo = container.courseRepository
    private val assignmentRepo = container.assignmentRepository

    val course: StateFlow<Course?> = courseRepo
        .observeById(courseId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val assignments: StateFlow<List<AssignmentItem>> = assignmentRepo
        .observeForCourse(courseId)
        .map { list -> list.map { AssignmentItem(it, course.value) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun toggleCompleted(assignment: Assignment) {
        viewModelScope.launch {
            val updated = assignment.copy(
                isCompleted = !assignment.isCompleted,
                completedAt = if (!assignment.isCompleted) System.currentTimeMillis() else null
            )
            assignmentRepo.save(updated)
        }
    }

    fun delete(assignment: Assignment) {
        viewModelScope.launch { assignmentRepo.delete(assignment.id) }
    }

    fun deleteCourse(onDone: () -> Unit) {
        viewModelScope.launch {
            course.value?.let { courseRepo.delete(it.id) }
            onDone()
        }
    }
}
