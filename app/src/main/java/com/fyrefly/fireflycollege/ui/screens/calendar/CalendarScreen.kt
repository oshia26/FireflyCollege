package com.fyrefly.fireflycollege.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.CourseSummary
import com.fyrefly.fireflycollege.ui.components.AssignmentCard
import com.fyrefly.fireflycollege.ui.components.ConfirmDialog
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState
import com.fyrefly.fireflycollege.ui.components.SectionHeader
import com.fyrefly.fireflycollege.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

private val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenAssignment: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<AssignmentItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous month"
                    )
                }
                Text(
                    text = monthTitle(state.month),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next month"
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Monday-first grid: blank cells before day 1
                val leadingBlanks = state.days.firstOrNull()?.date?.dayOfWeek?.value?.minus(1) ?: 0
                val rows = (leadingBlanks + state.days.size + 6) / 7
                repeat(rows) { rowIndex ->
                    Row {
                        repeat(7) { columnIndex ->
                            val index = rowIndex * 7 + columnIndex
                            val day = (index - leadingBlanks).let {
                                if (it >= 0 && it < state.days.size) state.days[it] else null
                            }
                            MonthCell(
                                day = day?.date,
                                isToday = day?.date == LocalDate.now(),
                                isSelected = day?.date == state.selectedDate,
                                dotColors = day?.dotColors ?: emptyList(),
                                onClick = { day?.date?.let(viewModel::select) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = dayTitle(state.selectedDate))
        }

        if (state.selectedItems.isEmpty()) {
            item {
                FireflyEmptyState(
                    title = "Nothing lands here",
                    message = "No deadlines on ${state.selectedDate.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.getDefault())} yet."
                )
            }
        } else {
            items(state.selectedItems.size) { index ->
                val item = state.selectedItems[index]
                AssignmentCard(
                    item = item,
                    today = LocalDate.now(),
                    onClick = { onOpenAssignment(item.assignment.id) },
                    onToggle = { viewModel.toggleCompleted(item.assignment) },
                    onDelete = { pendingDelete = item }
                )
            }
        }
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "Delete assignment?",
            message = "\"${item.assignment.title}\" will be removed.",
            onConfirm = {
                viewModel.delete(item.assignment)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun MonthCell(
    day: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean,
    dotColors: List<Long>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = day != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day?.dayOfMonth?.toString() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (dotColors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    dotColors.forEach { argb ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(color = Color(argb), shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

private fun monthTitle(month: YearMonth): String =
    month.month.getDisplayName(JavaTextStyle.FULL, Locale.getDefault()) + " " + month.year

private fun dayTitle(date: LocalDate): String =
    date.format(java.time.format.DateTimeFormatter.ofPattern("EEE, d MMM"))
