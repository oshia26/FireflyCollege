package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.Assignment
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.data.model.Priority
import com.fyrefly.fireflycollege.util.TimeFormats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class AssignmentDraft(
    val existingId: Long? = null,
    val title: String = "",
    val description: String = "",
    val notes: String = "",
    val courseId: Long? = null,
    val dueDate: LocalDate = LocalDate.now().plusDays(1),
    val hour: Int = 23,
    val minute: Int = 59,
    val priority: Priority = Priority.MEDIUM,
    // carried along so edits don't wipe history
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
) {
    val isValid: Boolean get() = title.isNotBlank() && courseId != null
}

class AssignmentEditorViewModel(
    private val container: AppContainer,
    assignmentId: Long?,
    preselectedCourseId: Long?
) : ViewModel() {

    private val assignmentRepo = container.assignmentRepository
    private val courseRepo = container.courseRepository
    private val coordinator = container.reminderCoordinator

    val courses: StateFlow<List<Course>> = courseRepo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _draft = MutableStateFlow(AssignmentDraft())
    val draft: StateFlow<AssignmentDraft> = _draft.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        viewModelScope.launch {
            if (assignmentId != null && assignmentId > 0) {
                assignmentRepo.getById(assignmentId)?.let { a ->
                    val time = TimeFormats.hourMinuteOf(a.dueAt)
                    _draft.value = AssignmentDraft(
                        existingId = a.id,
                        title = a.title,
                        description = a.description.orEmpty(),
                        notes = a.notes.orEmpty(),
                        courseId = a.courseId,
                        dueDate = TimeFormats.localDateOf(a.dueAt),
                        hour = time.hour,
                        minute = time.minute,
                        priority = a.priority,
                        createdAt = a.createdAt,
                        isCompleted = a.isCompleted,
                        completedAt = a.completedAt
                    )
                }
            } else if (preselectedCourseId != null && preselectedCourseId > 0) {
                _draft.update { it.copy(courseId = preselectedCourseId) }
            }
        }
    }

    fun update(transform: (AssignmentDraft) -> AssignmentDraft) {
        _draft.update(transform)
    }

    fun save() {
        val current = _draft.value
        if (!current.isValid) return
        viewModelScope.launch {
            val dueAt = TimeFormats.toEpochMillis(
                date = current.dueDate,
                time = LocalTime.of(current.hour, current.minute),
                zone = ZoneId.systemDefault()
            )
            val assignment = Assignment(
                id = current.existingId ?: 0,
                title = current.title.trim(),
                description = current.description.trim().takeIf { it.isNotEmpty() },
                notes = current.notes.trim().takeIf { it.isNotEmpty() },
                courseId = current.courseId!!,
                createdAt = current.createdAt,
                dueAt = dueAt,
                priority = current.priority,
                isCompleted = current.isCompleted,
                completedAt = current.completedAt
            )
            assignmentRepo.save(assignment)
            coordinator.refreshFor(assignment)
            _saved.value = true
        }
    }
}
