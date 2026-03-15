package com.weighttracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.weighttracker.data.local.SettingsDataStore
import com.weighttracker.data.local.WeightDatabase
import com.weighttracker.data.repository.WeightRepository

class WeightTrackerApp : Application() {
    lateinit var database: WeightDatabase
        private set

    lateinit var repository: WeightRepository
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = WeightDatabase.getDatabase(this)
        repository = WeightRepository(database.weightRecordDao())
        settingsDataStore = SettingsDataStore(this)

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "体重提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日体重记录提醒"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "weight_reminder_channel"

        @Volatile
        private var instance: WeightTrackerApp? = null

        fun getInstance(): WeightTrackerApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }
}
