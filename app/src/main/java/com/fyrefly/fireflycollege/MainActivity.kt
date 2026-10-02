package com.fyrefly.fireflycollege

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fyrefly.fireflycollege.ui.navigation.FireflyNavApp
import com.fyrefly.fireflycollege.ui.theme.FireflyCollegeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FireflyCollegeTheme {
                val app = application as FireflyApp
                FireflyNavApp(app = app)
            }
        }
    }
}
