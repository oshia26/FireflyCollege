package com.fyrefly.fireflycollege.ui.screens.calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fyrefly.fireflycollege.ui.components.FireflyEmptyState

@Composable
fun CalendarScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp
        )
    ) {
        item {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        item {
            FireflyEmptyState(
                title = "Stargazing soon",
                message = "The month grid arrives in the next stage — deadlines will land on their rightful dates."
            )
        }
    }
}
