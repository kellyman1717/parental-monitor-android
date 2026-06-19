package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

/**
 * Data geofence rumah - lokasi yang dianggap "rumah"
 */
data class HomeGeofence(
    val id: String = "home",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 200f, // dalam meter
    val isSet: Boolean = false,
    val address: String = "",
    val updatedAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "latitude" to latitude,
        "longitude" to longitude,
        "radius" to radius,
        "isSet" to isSet,
        "address" to address,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any>): HomeGeofence = HomeGeofence(
            id = map["id"] as? String ?: "home",
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
            radius = (map["radius"] as? Number)?.toFloat() ?: 200f,
            isSet = map["isSet"] as? Boolean ?: false,
            address = map["address"] as? String ?: "",
            updatedAt = map["updatedAt"] as? Timestamp ?: Timestamp.now()
        )
    }
}

/**
 * Status apakah anak sedang di rumah atau di luar
 */
enum class GeofenceStatus {
    INSIDE,    // Di dalam radius rumah
    OUTSIDE,   // Di luar radius rumah
    UNKNOWN    // Belum ada data atau GPS belum siap
}

/**
 * Event log saat status berubah (masuk/keluar rumah)
 */
data class GeofenceEvent(
    val id: String = "",
    val status: GeofenceStatus = GeofenceStatus.UNKNOWN,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distanceFromHome: Float = 0f,
    val timestamp: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "status" to status.name,
        "latitude" to latitude,
        "longitude" to longitude,
        "distanceFromHome" to distanceFromHome,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = ""): GeofenceEvent = GeofenceEvent(
            id = id,
            status = runCatching { GeofenceStatus.valueOf(map["status"] as? String ?: "UNKNOWN") }.getOrDefault(GeofenceStatus.UNKNOWN),
            latitude = (map["latitude"] as? Number)?.toDouble() ?: 0.0,
            longitude = (map["longitude"] as? Number)?.toDouble() ?: 0.0,
            distanceFromHome = (map["distanceFromHome"] as? Number)?.toFloat() ?: 0f,
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now()
        )
    }
}
