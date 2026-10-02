package com.fyrefly.fireflycollege

import android.app.Application
import com.fyrefly.fireflycollege.data.AppContainer
import com.fyrefly.fireflycollege.notification.ReminderChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FireflyApp : Application() {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        ReminderChannels.ensure(this)
        appScope.launch { container.reminderCoordinator.refreshAll() }
    }
}
