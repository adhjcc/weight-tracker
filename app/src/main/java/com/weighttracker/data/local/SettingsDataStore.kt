package com.weighttracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    private object PreferencesKeys {
        val HEIGHT = floatPreferencesKey("height")
        val TARGET_WEIGHT = floatPreferencesKey("target_weight")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            height = preferences[PreferencesKeys.HEIGHT] ?: 170f,
            targetWeight = preferences[PreferencesKeys.TARGET_WEIGHT] ?: 70f,
            reminderEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: false,
            reminderHour = preferences[PreferencesKeys.REMINDER_HOUR] ?: 8,
            reminderMinute = preferences[PreferencesKeys.REMINDER_MINUTE] ?: 0,
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
        )
    }

    suspend fun updateHeight(height: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEIGHT] = height
        }
    }

    suspend fun updateTargetWeight(targetWeight: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_WEIGHT] = targetWeight
        }
    }

    suspend fun updateReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_HOUR] = hour
            preferences[PreferencesKeys.REMINDER_MINUTE] = minute
        }
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
}
