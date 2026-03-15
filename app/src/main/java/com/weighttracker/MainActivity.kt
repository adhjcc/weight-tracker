package com.weighttracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.weighttracker.data.local.SettingsDataStore
import com.weighttracker.data.repository.WeightRepository
import com.weighttracker.receiver.ReminderScheduler
import com.weighttracker.ui.screens.*
import com.weighttracker.ui.theme.WeightTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as WeightTrackerApp
        val viewModel = WeightViewModel(app.repository, app.settingsDataStore)

        setContent {
            WeightTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val settings by app.settingsDataStore.settings.collectAsStateWithLifecycle(
                        initialValue = com.weighttracker.data.local.UserSettings()
                    )

                    LaunchedEffect(Unit) {
                        if (settings.reminderEnabled) {
                            ReminderScheduler.scheduleReminder(
                                this@MainActivity,
                                settings.reminderHour,
                                settings.reminderMinute
                            )
                        }
                    }

                    if (settings.isFirstLaunch) {
                        FirstLaunchScreen(
                            settingsDataStore = app.settingsDataStore,
                            onComplete = {
                                restartActivity()
                            }
                        )
                    } else {
                        MainScreen(
                            viewModel = viewModel,
                            settings = settings,
                            settingsDataStore = app.settingsDataStore
                        )
                    }
                }
            }
        }
    }

    private fun restartActivity() {
        finish()
        startActivity(Intent(this, MainActivity::class.java))
    }
}
