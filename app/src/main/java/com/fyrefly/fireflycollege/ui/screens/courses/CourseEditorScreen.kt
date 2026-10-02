package com.fyrefly.fireflycollege.ui.screens.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.CoursePalette
import com.fyrefly.fireflycollege.viewmodel.CourseEditorViewModel

@Composable
fun CourseEditorScreen(
    viewModel: CourseEditorViewModel,
    onBack: () -> Unit
) {
    val draft by viewModel.draft.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.saved.collect { if (it) onBack() }
    }

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
                text = if (draft.existingId == null) "New course" else "Edit course",
                style = MaterialTheme.typography.titleLarge
            )
        }

        OutlinedTextField(
            value = draft.name,
            onValueChange = { value -> viewModel.update { it.copy(name = value) } },
            label = { Text("Course name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.lecturer,
            onValueChange = { value -> viewModel.update { it.copy(lecturer = value) } },
            label = { Text("Lecturer (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = draft.room,
            onValueChange = { value -> viewModel.update { it.copy(room = value) } },
            label = { Text("Room (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Color", style = MaterialTheme.typography.labelLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CoursePalette.colors.forEach { color ->
                val selected = draft.colorArgb == color
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = Color(color), shape = CircleShape)
                        .border(
                            width = if (selected) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        )
                        .clickable { viewModel.setColor(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = viewModel::save,
            enabled = draft.isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Save course")
        }
    }
}
