package com.parentalmonitor.service

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.GeofenceEvent
import com.parentalmonitor.data.model.GeofenceStatus
import com.parentalmonitor.data.model.HomeGeofence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Service untuk mendeteksi apakah HP di dalam atau di luar area "rumah".
 *
 * Fitur:
 * - Simpan koordinat rumah di SharedPreferences
 * - Hitung jarak dari rumah setiap ada update lokasi
 * - Trigger alarm beep 3x berulang jika keluar rumah
 * - Stop alarm jika masuk kembali ke rumah
 * - Log event ke Firebase
 */
class GeofenceService(private val context: Context) {

    companion object {
        private const val TAG = "GeofenceService"
        private const val PREF_NAME = "parental_geofence"
        private const val KEY_HOME_LAT = "home_latitude"
        private const val KEY_HOME_LNG = "home_longitude"
        private const val KEY_HOME_RADIUS = "home_radius"
        private const val KEY_HOME_SET = "home_set"
        private const val KEY_HOME_ADDRESS = "home_address"

        // Default radius rumah = 200 meter
        const val DEFAULT_RADIUS = 200f

        // Status saat ini (untuk hindari trigger berulang)
        @Volatile
        var currentStatus: GeofenceStatus = GeofenceStatus.UNKNOWN
            private set
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var alarmJob: Job? = null
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ToneGenerator", e)
        }
    }

    /**
     * Set lokasi rumah
     */
    fun setHomeLocation(latitude: Double, longitude: Double, radius: Float = DEFAULT_RADIUS, address: String = "") {
        prefs.edit()
            .putFloat(KEY_HOME_LAT, latitude.toFloat())
            .putFloat(KEY_HOME_LNG, longitude.toFloat())
            .putFloat(KEY_HOME_RADIUS, radius)
            .putBoolean(KEY_HOME_SET, true)
            .putString(KEY_HOME_ADDRESS, address)
            .apply()
        Log.d(TAG, "Home location set: $latitude, $longitude (radius: ${radius}m)")
    }

    /**
     * Get lokasi rumah yang tersimpan
     */
    fun getHomeGeofence(): HomeGeofence {
        return HomeGeofence(
            latitude = prefs.getFloat(KEY_HOME_LAT, 0f).toDouble(),
            longitude = prefs.getFloat(KEY_HOME_LNG, 0f).toDouble(),
            radius = prefs.getFloat(KEY_HOME_RADIUS, DEFAULT_RADIUS),
            isSet = prefs.getBoolean(KEY_HOME_SET, false),
            address = prefs.getString(KEY_HOME_ADDRESS, "") ?: ""
        )
    }

    /**
     * Hapus lokasi rumah
     */
    fun clearHomeLocation() {
        prefs.edit().clear().apply()
        stopAlarm()
        currentStatus = GeofenceStatus.UNKNOWN
    }

    /**
     * Cek apakah lokasi saat ini di dalam atau luar rumah.
     * Trigger alarm jika keluar, stop jika masuk.
     *
     * @return GeofenceEvent yang menjelaskan status saat ini
     */
    fun checkLocation(latitude: Double, longitude: Double): GeofenceEvent? {
        val home = getHomeGeofence()
        if (!home.isSet) return null

        val currentLocation = Location("current").apply {
            this.latitude = latitude
            this.longitude = longitude
        }
        val homeLocation = Location("home").apply {
            this.latitude = home.latitude
            this.longitude = home.longitude
        }

        val distance = currentLocation.distanceTo(homeLocation)
        val isInside = distance <= home.radius
        val newStatus = if (isInside) GeofenceStatus.INSIDE else GeofenceStatus.OUTSIDE

        // Hanya trigger jika status BERUBAH
        if (newStatus != currentStatus) {
            val previousStatus = currentStatus
            currentStatus = newStatus
            Log.d(TAG, "Geofence status changed: $previousStatus -> $newStatus (distance: ${distance}m)")

            when (newStatus) {
                GeofenceStatus.OUTSIDE -> {
                    // HP keluar rumah → mulai alarm
                    startAlarm()
                }
                GeofenceStatus.INSIDE -> {
                    // HP masuk rumah → stop alarm
                    stopAlarm()
                }
                GeofenceStatus.UNKNOWN -> {
                    // No-op
                }
            }

            return GeofenceEvent(
                status = newStatus,
                latitude = latitude,
                longitude = longitude,
                distanceFromHome = distance
            )
        }

        return null
    }

    /**
     * Mulai alarm beep 3x berulang-ulang.
     * Tone: TONE_CDMA_HIGH_L (high pitch loud beep)
     */
    private fun startAlarm() {
        if (alarmJob?.isActive == true) {
            Log.d(TAG, "Alarm already running")
            return
        }

        Log.d(TAG, "🚨 Starting alarm - HP keluar rumah!")

        alarmJob = serviceScope.launch {
            var beepCount = 0
            while (true) {
                try {
                    // Bunyiin 3x beep
                    repeat(3) { index ->
                        playBeep()
                        if (index < 2) {
                            delay(500) // jeda antar beep (0.5 detik)
                        }
                    }
                    beepCount++

                    // Jeda antar sequence 3 beep
                    delay(3000) // 3 detik

                    // Log setiap 10 siklus (30 detik) agar tidak spam
                    if (beepCount % 10 == 0) {
                        Log.d(TAG, "Alarm still running (cycle $beepCount)")
                    }

                    // Cek apakah masih di luar rumah
                    // (akan di-stop dari luar jika masuk lagi)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in alarm loop", e)
                    delay(5000)
                }
            }
        }
    }

    /**
     * Stop alarm
     */
    private fun stopAlarm() {
        if (alarmJob?.isActive == true) {
            Log.d(TAG, "✅ Stopping alarm - HP masuk rumah")
            alarmJob?.cancel()
            alarmJob = null
        }
    }

    /**
     * Bunyiin 1 beep
     */
    private fun playBeep() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300) // 300ms beep
        } catch (e: Exception) {
            Log.e(TAG, "Error playing beep", e)
        }
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopAlarm()
        serviceScope.cancel()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ToneGenerator", e)
        }
    }

    /**
     * Hitung jarak antara dua koordinat (meter)
     */
    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    /**
     * Cek apakah lokasi di dalam geofence rumah
     */
    fun isInsideHome(latitude: Double, longitude: Double): Boolean {
        val home = getHomeGeofence()
        if (!home.isSet) return false
        return calculateDistance(latitude, longitude, home.latitude, home.longitude) <= home.radius
    }
}
