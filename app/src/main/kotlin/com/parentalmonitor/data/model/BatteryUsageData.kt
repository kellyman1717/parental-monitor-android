package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class BatteryUsageData(
    val id: String = "",
    val packageName: String = "",
    val appName: String = "",
    val foregroundTimeMs: Long = 0,       // waktu aktif di foreground
    val lastUsed: Timestamp = Timestamp.now(),
    val openCount: Int = 0,
    val batteryDrainEstimate: Double = 0.0, // estimasi % baterai yang digunakan
    val date: String = ""                  // yyyy-MM-dd
) {
    fun toMap(): Map<String, Any> = mapOf(
        "packageName" to packageName,
        "appName" to appName,
        "foregroundTimeMs" to foregroundTimeMs,
        "lastUsed" to lastUsed,
        "openCount" to openCount,
        "batteryDrainEstimate" to batteryDrainEstimate,
        "date" to date
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = BatteryUsageData(
            id = id,
            packageName = map["packageName"] as? String ?: "",
            appName = map["appName"] as? String ?: "",
            foregroundTimeMs = (map["foregroundTimeMs"] as? Number)?.toLong() ?: 0,
            lastUsed = map["lastUsed"] as? Timestamp ?: Timestamp.now(),
            openCount = (map["openCount"] as? Number)?.toInt() ?: 0,
            batteryDrainEstimate = (map["batteryDrainEstimate"] as? Number)?.toDouble() ?: 0.0,
            date = map["date"] as? String ?: ""
        )
    }
}

data class BatteryStatus(
    val level: Int = 0,                    // 0-100
    val isCharging: Boolean = false,
    val chargingSource: String = "",       // USB, AC, Wireless
    val temperature: Float = 0f,           // Celsius
    val voltage: Int = 0,                  // mV
    val health: String = "",
    val technology: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "level" to level,
        "isCharging" to isCharging,
        "chargingSource" to chargingSource,
        "temperature" to temperature,
        "voltage" to voltage,
        "health" to health,
        "technology" to technology,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any>) = BatteryStatus(
            level = (map["level"] as? Number)?.toInt() ?: 0,
            isCharging = map["isCharging"] as? Boolean ?: false,
            chargingSource = map["chargingSource"] as? String ?: "",
            temperature = (map["temperature"] as? Number)?.toFloat() ?: 0f,
            voltage = (map["voltage"] as? Number)?.toInt() ?: 0,
            health = map["health"] as? String ?: "",
            technology = map["technology"] as? String ?: "",
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now()
        )
    }
}
