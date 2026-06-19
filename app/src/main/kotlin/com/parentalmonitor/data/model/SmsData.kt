package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class SmsData(
    val id: String = "",
    val sender: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: String = "inbox", // inbox or sent
    val app: String = "sms" // sms or whatsapp
) {
    fun toMap(): Map<String, Any> = mapOf(
        "sender" to sender,
        "content" to content,
        "timestamp" to timestamp,
        "type" to type,
        "app" to app
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = SmsData(
            id = id,
            sender = map["sender"] as? String ?: "",
            content = map["content"] as? String ?: "",
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
            type = map["type"] as? String ?: "inbox",
            app = map["app"] as? String ?: "sms"
        )
    }
}
