package com.fyrefly.fireflycollege.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fyrefly.fireflycollege.BuildConfig
import com.fyrefly.fireflycollege.data.repository.BackupResult
import com.fyrefly.fireflycollege.data.settings.ThemeMode
import com.fyrefly.fireflycollege.ui.components.ConfirmDialog
import com.fyrefly.fireflycollege.ui.components.SectionHeader
import com.fyrefly.fireflycollege.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val backupFileName =
    "fireflycollege-backup-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now()) + ".json"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backupResult by viewModel.backupResult.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportJson) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::importJson) }

    var confirmImport by remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(backupResult) {
        backupResult?.let { result ->
            val text = when (result) {
                is BackupResult.Exported -> "Backed up ${result.courses} courses and ${result.assignments} assignments."
                is BackupResult.Imported -> "Imported ${result.courses} courses and ${result.assignments} assignments."
                is BackupResult.Failure -> result.message
            }
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            viewModel.consumeBackupResult()
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.displaySmall
        )

        SectionHeader(title = "Appearance")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.themeMode == ThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                label = { Text("System") }
            )
            FilterChip(
                selected = state.themeMode == ThemeMode.DARK,
                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                label = { Text("Dark") }
            )
            FilterChip(
                selected = state.themeMode == ThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                label = { Text("Light") }
            )
        }

        SectionHeader(title = "Backup")
        Text(
            text = "${state.courseCount} courses · ${state.assignmentCount} assignments live on this device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Backups are plain JSON files you can keep anywhere — reinstall-friendly, human-readable, offline.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { exportLauncher.launch(backupFileName) }) {
                Text("Export JSON")
            }
            OutlinedButton(onClick = { confirmImport = true }) {
                Text("Import JSON")
            }
        }

        SectionHeader(title = "Reminders")
        val canExact = remember { viewModel.canScheduleExactAlarms(context) }
        Text(
            text = if (canExact) {
                "Exact alarms are allowed — reminders arrive on time."
            } else {
                "Exact alarm permission is off; reminders may drift in power-saving windows."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!canExact) {
            Button(onClick = { viewModel.requestExactAlarmPermission(context) }) {
                Text("Allow exact reminders")
            }
        }
        Text(
            text = "Oppo tip: then set FireflyCollege as unmanaged in Settings → Battery optimization, so reminders survive ColorOS sleep.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (BuildConfig.DEBUG) {
            OutlinedButton(onClick = viewModel::sendTestReminder) {
                Text("Send test reminder (debug)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "FireflyCollege 0.1.0 — local-first, MIT licensed.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (confirmImport) {
        ConfirmDialog(
            title = "Replace everything?",
            message = "Importing replaces all ${state.courseCount} courses and ${state.assignmentCount} assignments currently on this device with the backup file's contents.",
            confirmLabel = "Import",
            onConfirm = {
                confirmImport = false
                importLauncher.launch(arrayOf("*/*", "application/json"))
            },
            onDismiss = { confirmImport = false }
        )
    }
}
