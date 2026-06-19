package com.parentalmonitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import com.parentalmonitor.util.Constants

@HiltAndroidApp
class ParentalMonitorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Monitoring service channel (persistent)
            val monitoringChannel = NotificationChannel(
                Constants.CHANNEL_ID_MONITORING,
                "Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent notification for background monitoring"
                setShowBadge(false)
            }

            // Alerts channel
            val alertsChannel = NotificationChannel(
                Constants.CHANNEL_ID_ALERTS,
                "Parental Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important alerts about child activity"
                enableVibration(true)
            }

            manager.createNotificationChannel(monitoringChannel)
            manager.createNotificationChannel(alertsChannel)
        }
    }
}
