package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class LocationData(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val timestamp: Timestamp = Timestamp.now(),
    val address: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "accuracy" to accuracy,
        "timestamp" to timestamp,
        "address" to address
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = LocationData(
            id = id,
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
            accuracy = (map["accuracy"] as? Number)?.toFloat() ?: 0f,
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
            address = map["address"] as? String ?: ""
        )
    }
}
