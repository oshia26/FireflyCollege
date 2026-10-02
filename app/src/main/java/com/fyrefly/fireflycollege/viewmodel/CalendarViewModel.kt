package com.fyrefly.fireflycollege.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.model.Assignment
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.util.TimeFormats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CalendarDayInfo(
    val date: LocalDate,
    val dotColors: List<Long>
)

data class CalendarUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val days: List<CalendarDayInfo> = emptyList(),
    val selectedItems: List<AssignmentItem> = emptyList()
)

class CalendarViewModel(container: AppContainer) : ViewModel() {

    private val assignmentRepo = container.assignmentRepository
    private val courseRepo = container.courseRepository

    private val _month = MutableStateFlow(YearMonth.now())
    val month: StateFlow<YearMonth> = _month.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val state: StateFlow<CalendarUiState> = combine(
        assignmentRepo.observeAll(),
        courseRepo.observeAll(),
        _month,
        _selectedDate
    ) { assignments, courses, month, selected ->
        buildState(assignments, courses, month, selected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState()
    )

    fun previousMonth() {
        _month.update { it.minusMonths(1) }
        _selectedDate.update { date ->
            if (date.year == _month.value.year && date.month == _month.value.month) date else _month.value.atDay(1)
        }
    }

    fun nextMonth() {
        _month.update { it.plusMonths(1) }
        _selectedDate.update { date ->
            if (date.year == _month.value.year && date.month == _month.value.month) date else _month.value.atDay(1)
        }
    }

    fun select(date: LocalDate) {
        _selectedDate.value = date
        if (date.year != _month.value.year || date.month != _month.value.month) {
            _month.value = YearMonth.of(date.year, date.month)
        }
    }

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

    private fun buildState(
        assignments: List<Assignment>,
        courses: List<Course>,
        month: YearMonth,
        selected: LocalDate
    ): CalendarUiState {
        val zone = java.time.ZoneId.systemDefault()
        val courseById = courses.associateBy { it.id }

        val byDay = assignments
            // dots driven by active deadlines only; a completed one shows only in its day list
            .filter { !it.isCompleted }
            .groupBy { TimeFormats.localDateOf(it.dueAt, zone) }

        val first = month.atDay(1)
        val last = month.atEndOfMonth()
        val days = (0 until last.dayOfMonth).map { offset ->
            val date = first.plusDays(offset.toLong())
            val props = byDay[date].orEmpty()
                .mapNotNull { courseById[it.courseId]?.colorArgb }
                .distinct()
                .take(3)
            CalendarDayInfo(date = date, dotColors = props)
        }

        val items = assignments
            .filter { TimeFormats.localDateOf(it.dueAt, zone) == selected }
            .sortedWith(compareBy({ it.isCompleted }, { it.dueAt }))
            .map { AssignmentItem(it, courseById[it.courseId]) }

        return CalendarUiState(
            isLoading = false,
            month = month,
            selectedDate = selected,
            days = days,
            selectedItems = items
        )
    }
}
