package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class AppUsageData(
    val id: String = "",
    val packageName: String = "",
    val appName: String = "",
    val durationMs: Long = 0,
    val lastUsed: Timestamp = Timestamp.now(),
    val openCount: Int = 0,
    val date: String = "" // yyyy-MM-dd
) {
    fun toMap(): Map<String, Any> = mapOf(
        "packageName" to packageName,
        "appName" to appName,
        "durationMs" to durationMs,
        "lastUsed" to lastUsed,
        "openCount" to openCount,
        "date" to date
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = AppUsageData(
            id = id,
            packageName = map["packageName"] as? String ?: "",
            appName = map["appName"] as? String ?: "",
            durationMs = (map["durationMs"] as? Number)?.toLong() ?: 0,
            lastUsed = map["lastUsed"] as? Timestamp ?: Timestamp.now(),
            openCount = (map["openCount"] as? Number)?.toInt() ?: 0,
            date = map["date"] as? String ?: ""
        )
    }
}
