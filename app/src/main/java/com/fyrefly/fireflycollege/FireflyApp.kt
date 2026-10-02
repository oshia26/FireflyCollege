package com.fyrefly.fireflycollege

import android.app.Application
import com.fyrefly.fireflycollege.data.AppContainer

class FireflyApp : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
