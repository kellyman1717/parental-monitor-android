package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class CallData(
    val id: String = "",
    val number: String = "",
    val contactName: String = "",
    val duration: Long = 0, // seconds
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "incoming" // incoming, outgoing, missed
) {
    fun toMap(): Map<String, Any> = mapOf(
        "number" to number,
        "contactName" to contactName,
        "duration" to duration,
        "timestamp" to timestamp,
        "type" to type
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = CallData(
            id = id,
            number = map["number"] as? String ?: "",
            contactName = map["contactName"] as? String ?: "",
            duration = (map["duration"] as? Number)?.toLong() ?: 0,
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
            type = map["type"] as? String ?: "incoming"
        )
    }
}
