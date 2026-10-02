package com.fyrefly.fireflycollege.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.CourseSummary
import com.fyrefly.fireflycollege.ui.components.AssignmentCard
import com.fyrefly.fireflycollege.ui.components.ColorDot
import com.fyrefly.fireflycollege.ui.components.ConfirmDialog
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState
import com.fyrefly.fireflycollege.ui.components.SectionHeader
import com.fyrefly.fireflycollege.ui.components.StatChip
import com.fyrefly.fireflycollege.viewmodel.DashboardViewModel
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun greetingFor(hour: Int): String = when (hour) {
    in 4..10 -> "Good morning"
    in 11..15 -> "Good afternoon"
    in 16..21 -> "Good evening"
    else -> "Good night"
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenAssignment: (Long) -> Unit,
    onOpenCourse: (Long) -> Unit,
    onOpenSearch: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = remember { LocalDate.now() }
    var pendingDelete by remember { mutableStateOf<AssignmentItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            val now = remember { LocalDateTime.now() }
            val dateLabel =
                DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault()).format(now)
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingFor(now.hour),
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(
                    value = state.overdue.size.toString(),
                    label = "overdue",
                    tint = MaterialTheme.colorScheme.error
                )
                StatChip(
                    value = state.dueToday.size.toString(),
                    label = "due today",
                    tint = MaterialTheme.colorScheme.primary
                )
                StatChip(
                    value = (state.dueToday.size + state.dueSoon.size).toString(),
                    label = "due this week",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }

        if (!state.hasAnyData) {
            item {
                FireflyEmptyState(
                    title = "The night is quiet",
                    message = "Add a course and schedule your first assignment — the fireflies will keep watch on deadlines."
                )
            }
        } else {
            if (state.overdue.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Overdue",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                items(state.overdue.size) { index ->
                    val item = state.overdue[index]
                    AssignmentCard(
                        item = item,
                        today = today,
                        onClick = { onOpenAssignment(item.assignment.id) },
                        onToggle = { viewModel.toggleCompleted(item.assignment) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }

            if (state.dueToday.isNotEmpty()) {
                item { SectionHeader(title = "Due today") }
                items(state.dueToday.size) { index ->
                    val item = state.dueToday[index]
                    AssignmentCard(
                        item = item,
                        today = today,
                        onClick = { onOpenAssignment(item.assignment.id) },
                        onToggle = { viewModel.toggleCompleted(item.assignment) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }

            if (state.dueSoon.isNotEmpty()) {
                item { SectionHeader(title = "Upcoming") }
                items(state.dueSoon.size) { index ->
                    val item = state.dueSoon[index]
                    AssignmentCard(
                        item = item,
                        today = today,
                        onClick = { onOpenAssignment(item.assignment.id) },
                        onToggle = { viewModel.toggleCompleted(item.assignment) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }

            if (state.completedRecently.isNotEmpty()) {
                item { SectionHeader(title = "Recently completed") }
                items(minOf(state.completedRecently.size, 4)) { index ->
                    val item = state.completedRecently[index]
                    AssignmentCard(
                        item = item,
                        today = today,
                        onClick = { onOpenAssignment(item.assignment.id) },
                        onToggle = { viewModel.toggleCompleted(item.assignment) },
                        onDelete = { pendingDelete = item }
                    )
                }
            }

            if (state.courseSummaries.isNotEmpty()) {
                item { SectionHeader(title = "Your courses") }
                item {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.courseSummaries.forEach { summary ->
                            CoursePill(
                                summary = summary,
                                onClick = { onOpenCourse(summary.course.id) }
                            )
                        }
                    }
                }
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
private fun CoursePill(
    summary: CourseSummary,
    onClick: () -> Unit
) {
    val course = summary.course
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(150.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            ColorDot(color = Color(course.colorArgb), size = 10.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1
                )
                Text(
                    text = "${summary.completed}/${summary.total} done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
