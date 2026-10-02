package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.data.model.CoursePalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseDraft(
    val existingId: Long? = null,
    val name: String = "",
    val lecturer: String = "",
    val room: String = "",
    val colorArgb: Long = CoursePalette.colors.first()
) {
    val isValid: Boolean get() = name.isNotBlank()

    fun toCourse(createdAt: Long): Course = Course(
        id = existingId ?: 0,
        name = name.trim(),
        lecturer = lecturer.trim().takeIf { it.isNotEmpty() },
        room = room.trim().takeIf { it.isNotEmpty() },
        colorArgb = colorArgb,
        createdAt = createdAt
    )
}

class CourseEditorViewModel(
    private val container: AppContainer,
    courseId: Long?
) : ViewModel() {

    private val courseRepo = container.courseRepository

    private val _draft = MutableStateFlow(CourseDraft())
    val draft: StateFlow<CourseDraft> = _draft.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        if (courseId != null && courseId > 0) {
            viewModelScope.launch {
                courseRepo.getById(courseId)?.let { course ->
                    _draft.value = CourseDraft(
                        existingId = course.id,
                        name = course.name,
                        lecturer = course.lecturer.orEmpty(),
                        room = course.room.orEmpty(),
                        colorArgb = course.colorArgb
                    )
                }
            }
        } else {
            viewModelScope.launch {
                val usedColors = courseRepo.getAll().map { it.colorArgb }
                _draft.update { draft ->
                    if (draft.name.isEmpty()) draft.copy(colorArgb = CoursePalette.nextColor(usedColors)) else draft
                }
            }
        }
    }

    fun update(transform: (CourseDraft) -> CourseDraft) {
        _draft.update(transform)
    }

    fun setColor(color: Long) {
        _draft.update { it.copy(colorArgb = color) }
    }

    fun save() {
        val current = _draft.value
        if (!current.isValid) return
        viewModelScope.launch {
            if (current.existingId == null) {
                courseRepo.create(current.toCourse(createdAt = System.currentTimeMillis()))
            } else {
                val existing = courseRepo.getById(current.existingId!!)
                courseRepo.update(current.toCourse(createdAt = existing?.createdAt ?: System.currentTimeMillis()))
            }
            _saved.value = true
        }
    }
}
