package com.fyrefly.fireflycollege.viewmodel

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.data.repository.BackupResult
import com.fyrefly.fireflycollege.data.settings.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val courseCount: Int = 0,
    val assignmentCount: Int = 0
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val _backupResult = MutableStateFlow<BackupResult?>(null)
    val backupResult: StateFlow<BackupResult?> = _backupResult.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun consumeMessage() {
        _message.value = null
    }

    val state: StateFlow<SettingsUiState> = combine(
        container.settingsStore.themeMode,
        container.courseRepository.observeAll(),
        container.assignmentRepository.observeAll()
    ) { mode, courses, assignments ->
        SettingsUiState(
            isLoading = false,
            themeMode = mode,
            courseCount = courses.size,
            assignmentCount = assignments.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { container.settingsStore.setThemeMode(mode) }
    }

    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            _backupResult.value = container.backupRepository.exportTo(uri)
        }
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            val result = container.backupRepository.importFrom(uri)
            _backupResult.value = result
        }
    }

    fun consumeBackupResult() {
        _backupResult.value = null
    }

    fun canScheduleExactAlarms(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }

    fun sendTestReminder() {
        container.reminderCoordinator.testReminder()
        _message.value = "Test reminder queued — a notification lands in about 10 seconds."
    }
}
