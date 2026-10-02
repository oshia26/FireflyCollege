package com.fyrefly.fireflycollege.ui.screens.assignments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.label
import com.fyrefly.fireflycollege.util.TimeFormats
import com.fyrefly.fireflycollege.viewmodel.AssignmentEditorViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentEditorScreen(
    viewModel: AssignmentEditorViewModel,
    onBack: () -> Unit
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val courses by viewModel.courses.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saved.collect { if (it) onBack() }
    }

    var courseMenuOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = if (draft.existingId == null) "New assignment" else "Edit assignment",
                style = MaterialTheme.typography.titleLarge
            )
        }

        OutlinedTextField(
            value = draft.title,
            onValueChange = { value -> viewModel.update { it.copy(title = value) } },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.description,
            onValueChange = { value -> viewModel.update { it.copy(description = value) } },
            label = { Text("Description (optional)") },
            minLines = 2,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        if (courses.isEmpty()) {
            Text(
                text = "Create a course first — a deadline needs somewhere to belong.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            ExposedDropdownMenuBox(
                expanded = courseMenuOpen,
                onExpandedChange = { courseMenuOpen = it }
            ) {
                val selectedCourse = courses.firstOrNull { it.id == draft.courseId }
                OutlinedTextField(
                    value = selectedCourse?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course") },
                    trailingIcon = { Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = courseMenuOpen,
                    onDismissRequest = { courseMenuOpen = false }
                ) {
                    courses.forEach { course ->
                        DropdownMenuItem(
                            text = { Text(course.name) },
                            onClick = {
                                viewModel.update { it.copy(courseId = course.id) }
                                courseMenuOpen = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = TimeFormats.formatDateTime(
                TimeFormats.toEpochMillis(draft.dueDate, LocalTime.of(draft.hour, draft.minute))
            ),
            onValueChange = {},
            readOnly = true,
            label = { Text("Deadline") },
            trailingIcon = {
                Row {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = "Pick date")
                    }
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(imageVector = Icons.Rounded.Schedule, contentDescription = "Pick time")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { viewModel.update { it.copy(hour = 23, minute = 59) } }) {
                Text(text = "End of day")
            }
            TextButton(onClick = { viewModel.update { it.copy(hour = 7, minute = 45) } }) {
                Text(text = "Before class")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Priority", style = MaterialTheme.typography.labelLarge)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.fyrefly.fireflycollege.data.model.Priority.entries.forEach { priority ->
                androidx.compose.material3.FilterChip(
                    selected = draft.priority == priority,
                    onClick = { viewModel.update { it.copy(priority = priority) } },
                    label = { Text(priority.label) }
                )
            }
        }

        OutlinedTextField(
            value = draft.notes,
            onValueChange = { value -> viewModel.update { it.copy(notes = value) } },
            label = { Text("Notes (optional)") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.padding(top = 4.dp))
        Button(
            onClick = viewModel::save,
            enabled = draft.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save assignment")
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = TimeFormats.startOfDay(draft.dueDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val zone = java.time.ZoneId.systemDefault()
                            viewModel.update {
                                it.copy(dueDate = TimeFormats.localDateOf(millis, zone))
                            }
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = draft.hour,
            initialMinute = draft.minute,
            is24Hour = true
        )
        TimePickerDialog(
            title = { Text("Time") },
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.update { it.copy(hour = timeState.hour, minute = timeState.minute) }
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timeState)
        }
    }
}
