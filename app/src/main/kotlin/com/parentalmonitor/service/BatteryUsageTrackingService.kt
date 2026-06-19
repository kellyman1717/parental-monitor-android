package com.parentalmonitor.service

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.BatteryStatus
import com.parentalmonitor.data.model.BatteryUsageData
import java.text.SimpleDateFormat
import java.util.*

object BatteryUsageTrackingService {

    /**
     * Mendapatkan status baterai saat ini (level, charging, temperature, dll)
     */
    fun getBatteryStatus(context: Context): BatteryStatus {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return batteryIntent?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level != -1 && scale != -1) {
                (level * 100 / scale.toFloat()).toInt()
            } else 0

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargingSource = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Tidak terisi"
            }

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)

            val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val health = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Baik"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Panas berlebih"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Rusak"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Voltase berlebih"
                BatteryManager.BATTERY_HEALTH_COLD -> "Dingin"
                else -> "Tidak diketahui"
            }

            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

            BatteryStatus(
                level = batteryPct,
                isCharging = isCharging,
                chargingSource = chargingSource,
                temperature = temperature,
                voltage = voltage,
                health = health,
                technology = technology,
                timestamp = Timestamp.now()
            )
        } ?: BatteryStatus()
    }

    /**
     * Mendapatkan daftar aplikasi yang paling banyak mengonsumsi baterai
     * Berdasarkan waktu foreground usage (korelasi langsung dengan drain)
     */
    fun getBatteryConsumingApps(context: Context): List<BatteryUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager
        val batteryStatus = getBatteryStatus(context)

        // Hitung total drain yang bisa diatribusikan ke apps
        // Asumsi: 100% - batteryLevel sekarang = total drain sejak full charge
        val totalDrain = (100 - batteryStatus.level).coerceAtLeast(0)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val usageStats: List<UsageStats> = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            emptyList()
        }

        // Filter hanya app yang benar-benar digunakan
        val activeApps = usageStats.filter { it.totalTimeInForeground > 0 }

        // Hitung total foreground time untuk proporsi drain
        val totalForegroundMs = activeApps.sumOf { it.totalTimeInForeground }

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return activeApps
            .map { stats ->
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stats.packageName
                }

                // Estimasi drain baterai berdasarkan proporsi foreground time
                val drainEstimate = if (totalForegroundMs > 0) {
                    (stats.totalTimeInForeground.toDouble() / totalForegroundMs) * totalDrain
                } else 0.0

                BatteryUsageData(
                    packageName = stats.packageName,
                    appName = appName,
                    foregroundTimeMs = stats.totalTimeInForeground,
                    lastUsed = Timestamp(Date(stats.lastTimeUsed)),
                    openCount = getOpenCount(stats),
                    batteryDrainEstimate = drainEstimate,
                    date = date
                )
            }
            .sortedByDescending { it.foregroundTimeMs }
    }

    /**
     * Mendapatkan ringkasan baterai untuk hari ini
     */
    fun getBatterySummary(context: Context): Map<String, Any> {
        val status = getBatteryStatus(context)
        val apps = getBatteryConsumingApps(context)

        val totalForegroundMs = apps.sumOf { it.foregroundTimeMs }
        val totalDrainEstimate = apps.sumOf { it.batteryDrainEstimate }

        // Hitung battery drain rate (drain per jam)
        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        val hoursElapsed = ((now - startOfDay) / (1000.0 * 60 * 60)).coerceAtLeast(1.0)
        val drainRate = (100 - status.level) / hoursElapsed

        return mapOf(
            "currentLevel" to status.level,
            "isCharging" to status.isCharging,
            "chargingSource" to status.chargingSource,
            "temperature" to status.temperature,
            "health" to status.health,
            "totalForegroundHours" to String.format("%.1f", totalForegroundMs / (1000.0 * 60 * 60)),
            "totalDrainEstimate" to String.format("%.1f", totalDrainEstimate),
            "drainRatePerHour" to String.format("%.1f", drainRate),
            "topConsumer" to (apps.firstOrNull()?.appName ?: "N/A"),
            "appsTracked" to apps.size
        )
    }

    private fun getOpenCount(stats: UsageStats): Int {
        return try {
            val events = stats.javaClass.getDeclaredField("mEvents")
            events.isAccessible = true
            val eventList = events.get(stats) as? List<*>
            eventList?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
