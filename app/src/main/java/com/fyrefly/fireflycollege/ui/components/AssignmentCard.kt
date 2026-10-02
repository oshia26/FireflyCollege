package com.fyrefly.fireflycollege.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fyrefly.fireflycollege.data.model.AssignmentItem
import com.fyrefly.fireflycollege.data.model.Course
import com.fyrefly.fireflycollege.data.model.Priority
import com.fyrefly.fireflycollege.data.model.label
import com.fyrefly.fireflycollege.util.TimeFormats
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun dueTint(dueAt: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Color {
    val dueDate = TimeFormats.localDateOf(dueAt, zone)
    return when {
        dueDate.isBefore(today) -> MaterialTheme.colorScheme.error
        dueDate == today -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun AssignmentCard(
    item: AssignmentItem,
    today: LocalDate,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assignment = item.assignment
    val completed = assignment.isCompleted
    val now = System.currentTimeMillis()
    val zone = ZoneId.systemDefault()
    val tint = if (completed) MaterialTheme.colorScheme.onSurfaceVariant else dueTint(assignment.dueAt, today, zone)

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = if (completed) Icons.Rounded.CheckCircleOutline else Icons.Rounded.Radio,
                    contentDescription = if (completed) "Mark as not completed" else "Mark as completed",
                    tint = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.course?.let { course ->
                        ColorDot(color = Color(course.colorArgb))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = TimeFormats.dueLabel(assignment.dueAt, today, zone),
                        style = MaterialTheme.typography.labelMedium,
                        color = tint
                    )
                }
                if (!completed) {
                    val countdown = TimeFormats.countdown(assignment.dueAt, now)
                    if (countdown.startsWith("in ") && countdown != "in") {
                        Text(
                            text = countdown,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Priority: " + assignment.priority.label,
                tint = when (assignment.priority) {
                    Priority.HIGH -> Color(0xFFD1495B)
                    Priority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                    Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier.size(18.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete assignment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
