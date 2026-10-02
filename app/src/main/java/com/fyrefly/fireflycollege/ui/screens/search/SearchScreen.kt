package com.fyrefly.fireflycollege.ui.screens.search

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.ui.components.AssignmentCard
import com.fyrefly.fireflycollege.ui.components.ColorDot
import com.fyrefly.fireflycollege.ui.components.ConfirmDialog
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState
import com.fyrefly.fireflycollege.ui.components.SectionHeader
import com.fyrefly.fireflycollege.viewmodel.SearchViewModel
import java.time.LocalDate

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    today: LocalDate = LocalDate.now(),
    onBack: () -> Unit,
    onOpenAssignment: (Long) -> Unit,
    onOpenCourse: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<AssignmentItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Search assignments, courses, notes…") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }
        }

        if (state.courseResults.isNotEmpty()) {
            item { SectionHeader(title = "Courses") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.courseResults.forEach { course ->
                        CourseRow(course = course, onClick = { onOpenCourse(course.id) })
                    }
                }
            }
        }

        if (state.assignmentResults.isNotEmpty()) {
            item { SectionHeader(title = "Assignments") }
            items(state.assignmentResults.size) { index ->
                val item = state.assignmentResults[index]
                AssignmentCard(
                    item = item,
                    today = today,
                    onClick = { onOpenAssignment(item.assignment.id) },
                    onToggle = { viewModel.toggleCompleted(item) },
                    onDelete = { pendingDelete = item }
                )
            }
        }

        if (!state.searched) {
            item {
                FireflyEmptyState(
                    title = "Wander the dark",
                    message = "Type to search assignments, courses and notes."
                )
            }
        } else if (state.courseResults.isEmpty() && state.assignmentResults.isEmpty()) {
            item {
                FireflyEmptyState(
                    title = "One day it will find light",
                    message = "Nothing matches \"${state.query}\" yet — try another word?"
                )
            }
        }
    }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = "Delete assignment?",
            message = "\"${item.assignment.title}\" will be removed.",
            onConfirm = {
                viewModel.delete(item)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun CourseRow(course: Course, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            ColorDot(color = Color(course.colorArgb))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                val meta = listOfNotNull(
                    course.lecturer?.takeIf { it.isNotBlank() },
                    course.room?.takeIf { it.isNotBlank() }
                )
                if (meta.isNotEmpty()) {
                    Text(
                        text = meta.joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
