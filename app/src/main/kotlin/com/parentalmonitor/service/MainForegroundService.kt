package com.parentalmonitor.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.parentalmonitor.R
import com.parentalmonitor.data.model.LocationData
import com.parentalmonitor.data.repository.FirebaseRepository
import com.parentalmonitor.ui.child.SetupActivity
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainForegroundService : Service() {

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var locationTrackingJob: Job? = null
    private var smsCallTrackingJob: Job? = null
    private var appUsageTrackingJob: Job? = null
    private var batteryTrackingJob: Job? = null
    private var dataSyncJob: Job? = null
    private lateinit var geofenceService: GeofenceService

    companion object {
        private const val TAG = "MainForegroundService"

        fun start(context: Context) {
            val intent = Intent(context, MainForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MainForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(Constants.NOTIFICATION_ID_FOREGROUND, createNotification())
        geofenceService = GeofenceService(this)
        startAllTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::geofenceService.isInitialized) {
            geofenceService.cleanup()
        }
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, SetupActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CHANNEL_ID_MONITORING)
            .setContentTitle("Parental Monitor")
            .setContentText("Monitoring aktif")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startAllTracking() {
        startLocationTracking()
        startSmsCallTracking()
        startAppUsageTracking()
        startBatteryTracking()
        startDataSync()
    }

    private fun startLocationTracking() {
        locationTrackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    if (PermissionHelper.hasLocationPermission(this@MainForegroundService)) {
                        LocationTrackingService.getLastLocation(this@MainForegroundService)?.let { location ->
                            val locationData = LocationData(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                accuracy = location.accuracy,
                                timestamp = Timestamp.now()
                            )
                            firebaseRepository.uploadLocation(locationData)
                            Log.d(TAG, "Location uploaded: ${location.latitude}, ${location.longitude}")

                            // Cek geofence rumah
                            val geofenceEvent = geofenceService.checkLocation(location.latitude, location.longitude)
                            if (geofenceEvent != null) {
                                // Upload event ke Firebase
                                serviceScope.launch {
                                    firebaseRepository.uploadGeofenceEvent(geofenceEvent)
                                }
                            }
                        }
                    }
                    delay(Constants.LOCATION_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Location tracking error", e)
                    delay(30000) // Retry in 30 seconds on error
                }
            }
        }
    }

    private fun startSmsCallTracking() {
        smsCallTrackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    if (PermissionHelper.hasSmsPermission(this@MainForegroundService)) {
                        val smsList = SmsCallTrackingService.readNewSms(this@MainForegroundService)
                        if (smsList.isNotEmpty()) {
                            firebaseRepository.uploadSmsBatch(smsList)
                            Log.d(TAG, "Uploaded ${smsList.size} SMS messages")
                        }
                    }
                    if (PermissionHelper.hasCallLogPermission(this@MainForegroundService)) {
                        val calls = SmsCallTrackingService.readNewCalls(this@MainForegroundService)
                        calls.forEach { call ->
                            firebaseRepository.uploadCall(call)
                        }
                        Log.d(TAG, "Uploaded ${calls.size} call logs")
                    }
                    delay(Constants.SYNC_INTERVAL_MINUTES * 60 * 1000)
                } catch (e: Exception) {
                    Log.e(TAG, "SMS/Call tracking error", e)
                    delay(60000)
                }
            }
        }
    }

    private fun startAppUsageTracking() {
        appUsageTrackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    if (PermissionHelper.hasUsageStatsPermission(this@MainForegroundService)) {
                        val usageList = AppUsageTrackingService.getTodayUsage(this@MainForegroundService)
                        usageList.forEach { usage ->
                            firebaseRepository.uploadAppUsage(usage)
                        }
                        Log.d(TAG, "Uploaded ${usageList.size} app usage records")
                    }
                    delay(Constants.SYNC_INTERVAL_MINUTES * 60 * 1000)
                } catch (e: Exception) {
                    Log.e(TAG, "App usage tracking error", e)
                    delay(60000)
                }
            }
        }
    }

    private fun startBatteryTracking() {
        batteryTrackingJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Upload current battery status
                    val batteryStatus = BatteryUsageTrackingService.getBatteryStatus(this@MainForegroundService)
                    firebaseRepository.uploadBatteryStatus(batteryStatus)
                    Log.d(TAG, "Battery status uploaded: ${batteryStatus.level}%, charging=${batteryStatus.isCharging}")

                    // Upload per-app battery usage
                    if (PermissionHelper.hasUsageStatsPermission(this@MainForegroundService)) {
                        val batteryApps = BatteryUsageTrackingService.getBatteryConsumingApps(this@MainForegroundService)
                        batteryApps.forEach { app ->
                            firebaseRepository.uploadBatteryUsage(app)
                        }
                        Log.d(TAG, "Uploaded ${batteryApps.size} battery usage records")
                    }

                    // Sync setiap 30 menit
                    delay(30 * 60 * 1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Battery tracking error", e)
                    delay(60000)
                }
            }
        }
    }

    private fun startDataSync() {
        dataSyncJob = serviceScope.launch {
            while (isActive) {
                try {
                    // Ensure device is registered
                    val deviceInfo = firebaseRepository.getDeviceInfo()
                    if (deviceInfo.getOrNull() == null) {
                        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                        val deviceName = android.os.Build.MODEL
                        firebaseRepository.registerDevice(deviceName)
                        sharedPrefs.edit().putBoolean(Constants.PREF_IS_SETUP_COMPLETE, true).apply()
                    }
                    delay(60 * 60 * 1000) // Check every hour
                } catch (e: Exception) {
                    Log.e(TAG, "Data sync error", e)
                    delay(300000)
                }
            }
        }
    }
}
