package com.fyrefly.fireflycollege.ui.screens.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.data.model.CourseSummary
import com.fyrefly.fireflycollege.ui.components.CourseCard
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState
import com.fyrefly.fireflycollege.viewmodel.CoursesViewModel

@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    onOpenCourse: (Long) -> Unit,
    onEditCourse: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Courses",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        if (!state.isLoading && state.summaries.isEmpty()) {
            item {
                FireflyEmptyState(
                    title = "No courses yet",
                    message = "Let there be light — create your first course to start tracking assignments."
                )
            }
        } else {
            items(state.summaries.size) { index ->
                val summary = state.summaries[index]
                CourseCard(
                    summary = summary,
                    onClick = { onOpenCourse(summary.course.id) },
                    onEdit = { onEditCourse(summary.course.id) }
                )
            }
        }
    }
}
