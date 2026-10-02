package com.fyrefly.fireflycollege.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.fyrefly.fireflycollege.ui.components.AssignmentCard
import com.fyrefly.fireflycollege.ui.components.ConfirmDialog
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState
import com.fyrefly.fireflycollege.ui.components.SectionHeader
import com.fyrefly.fireflycollege.viewmodel.CourseDetailViewModel
import java.time.LocalDate

@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel,
    today: LocalDate = LocalDate.now(),
    onBack: () -> Unit,
    onEditCourse: (Long) -> Unit,
    onAddAssignment: (Long) -> Unit,
    onOpenAssignment: (Long) -> Unit
) {
    val course by viewModel.course.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    var confirmDeleteCourse by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<com.fyrefly.fireflycollege.data.model.AssignmentItem?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            course?.let { c ->
                IconButton(onClick = { onEditCourse(c.id) }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit course",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { confirmDeleteCourse = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete course",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val current = course
        val activeColor = current?.colorArgb
        if (current != null && activeColor != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Column {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 6.dp)
                                .background(color = Color(activeColor), shape = RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = current.name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                        Row(modifier = Modifier.padding(top = 4.dp)) {
                            current.lecturer?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            current.room?.takeIf { it.isNotBlank() }?.let {
                                if (current.lecturer?.isNotBlank() == true) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = "Room $it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = "Assignments",
                        trailing = {
                            IconButton(
                                onClick = { onAddAssignment(current.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = "Add assignment",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }

                if (assignments.isEmpty()) {
                    item {
                        FireflyEmptyState(
                            title = "No assignments here",
                            message = "Nothing is due from ${current.name} yet."
                        )
                    }
                } else {
                    items(assignments.size) { index ->
                        val item = assignments[index]
                        AssignmentCard(
                            item = item,
                            today = today,
                            onClick = { onOpenAssignment(item.assignment.id) },
                            onToggle = { viewModel.toggleCompleted(item.assignment) },
                            onDelete = { pendingDelete = item }
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmDeleteCourse) {
        ConfirmDialog(
            title = "Delete course?",
            message = "Deleting \"${course?.name.orEmpty()}\" removes its assignments permanently.",
            onConfirm = {
                confirmDeleteCourse = false
                viewModel.deleteCourse(onDone = onBack)
            },
            onDismiss = { confirmDeleteCourse = false }
        )
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
