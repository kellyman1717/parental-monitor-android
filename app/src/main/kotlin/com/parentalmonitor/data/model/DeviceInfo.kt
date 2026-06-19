package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class DeviceInfo(
    val deviceId: String = "",
    val name: String = "",
    val model: String = "",
    val registeredAt: Timestamp = Timestamp.now(),
    val isActive: Boolean = true,
    val pairingCode: String? = null,
    val pairedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "name" to name,
            "model" to model,
            "registeredAt" to registeredAt,
            "isActive" to isActive
        )
        pairingCode?.let { map["pairingCode"] = it }
        pairedAt?.let { map["pairedAt"] = it }
        return map
    }

    companion object {
        fun fromMap(map: Map<String, Any>, deviceId: String = "") = DeviceInfo(
            deviceId = deviceId,
            name = map["name"] as? String ?: "",
            model = map["model"] as? String ?: "",
            registeredAt = map["registeredAt"] as? Timestamp ?: Timestamp.now(),
            isActive = map["isActive"] as? Boolean ?: true,
            pairingCode = map["pairingCode"] as? String,
            pairedAt = map["pairedAt"] as? Timestamp
        )
    }
}