package com.weighttracker.data.local

data class UserSettings(
    val height: Float = 170f,
    val targetWeight: Float = 70f,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val isFirstLaunch: Boolean = true
)
