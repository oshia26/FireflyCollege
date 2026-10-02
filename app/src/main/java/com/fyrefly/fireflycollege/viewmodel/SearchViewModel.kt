package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class SearchUiState(
    val query: String = "",
    val searched: Boolean = false,
    val courseResults: List<Course> = emptyList(),
    val assignmentResults: List<AssignmentItem> = emptyList()
)

class SearchViewModel(container: AppContainer) : ViewModel() {

    private val courseRepo = container.courseRepository
    private val assignmentRepo = container.assignmentRepository
    private val coordinator = container.reminderCoordinator

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val state: StateFlow<SearchUiState> = combine(
        _query,
        assignmentRepo.observeAll(),
        courseRepo.observeAll()
    ) { raw, assignments, courses ->
        val needle = raw.trim().lowercase(Locale.getDefault())
        if (needle.isEmpty()) {
            SearchUiState(query = raw)
        } else {
            val courseById = courses.associateBy { it.id }
            val nameOf: (Long) -> String = { id -> courseById[id]?.name.orEmpty() }
            SearchUiState(
                query = raw,
                searched = true,
                courseResults = courses.filter { it.name.lowercase(Locale.getDefault()).contains(needle) },
                assignmentResults = assignments
                    .filter { assignment ->
                        val courseName = nameOf(assignment.courseId)
                        assignment.title.lowercase(Locale.getDefault()).contains(needle) ||
                            assignment.description.orEmpty().lowercase(Locale.getDefault()).contains(needle) ||
                            assignment.notes.orEmpty().lowercase(Locale.getDefault()).contains(needle) ||
                            courseName.lowercase(Locale.getDefault()).contains(needle)
                    }
                    .sortedWith(compareBy({ it.isCompleted }, { it.dueAt }))
                    .map { AssignmentItem(it, courseById[it.courseId]) }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(value: String) {
        _query.update { value }
    }

    fun toggleCompleted(item: com.fyrefly.fireflycollege.data.model.AssignmentItem) {
        val assignment = item.assignment
        viewModelScope.launch {
            val updated = assignment.copy(
                isCompleted = !assignment.isCompleted,
                completedAt = if (!assignment.isCompleted) System.currentTimeMillis() else null
            )
            assignmentRepo.save(updated)
            coordinator.refreshFor(updated)
        }
    }

    fun delete(item: com.fyrefly.fireflycollege.data.model.AssignmentItem) {
        viewModelScope.launch {
            assignmentRepo.delete(item.assignment.id)
            coordinator.cancelFor(item.assignment.id)
        }
    }
}
