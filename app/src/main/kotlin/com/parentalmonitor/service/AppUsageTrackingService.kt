package com.parentalmonitor.service

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.AppUsageData
import java.text.SimpleDateFormat
import java.util.*

object AppUsageTrackingService {

    fun getTodayUsage(context: Context): List<AppUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager

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

        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        return usageStats
            .filter { it.totalTimeInForeground > 0 }
            .map { stats ->
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stats.packageName
                }

                AppUsageData(
                    packageName = stats.packageName,
                    appName = appName,
                    durationMs = stats.totalTimeInForeground,
                    lastUsed = Timestamp(Date(stats.lastTimeUsed)),
                    openCount = getOpenCount(stats),
                    date = date
                )
            }
            .sortedByDescending { it.durationMs }
    }

    private fun getOpenCount(stats: UsageStats): Int {
        // UsageStats doesn't directly provide open count,
        // but we can estimate from the number of separate usage periods
        return try {
            val events = stats.javaClass.getDeclaredField("mEvents")
            events.isAccessible = true
            val eventList = events.get(stats) as? List<*>
            eventList?.size ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getUsageForDate(context: Context, date: Date): List<AppUsageData> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager

        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = calendar.apply { add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis

        val usageStats = try {
            usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } catch (e: Exception) {
            emptyList()
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        return usageStats
            .filter { it.totalTimeInForeground > 0 }
            .map { stats ->
                val appName = try {
                    val appInfo = packageManager.getApplicationInfo(stats.packageName, 0)
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stats.packageName
                }

                AppUsageData(
                    packageName = stats.packageName,
                    appName = appName,
                    durationMs = stats.totalTimeInForeground,
                    lastUsed = Timestamp(Date(stats.lastTimeUsed)),
                    openCount = 0,
                    date = dateStr
                )
            }
            .sortedByDescending { it.durationMs }
    }
}
