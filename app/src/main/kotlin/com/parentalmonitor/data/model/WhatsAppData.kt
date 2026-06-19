package com.parentalmonitor.data.model

import com.google.firebase.Timestamp

data class WhatsAppData(
    val id: String = "",
    val sender: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isGroup: Boolean = false,
    val groupName: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "sender" to sender,
        "content" to content,
        "timestamp" to timestamp,
        "isGroup" to isGroup,
        "groupName" to groupName
    )

    companion object {
        fun fromMap(map: Map<String, Any>, id: String = "") = WhatsAppData(
            id = id,
            sender = map["sender"] as? String ?: "",
            content = map["content"] as? String ?: "",
            timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
            isGroup = map["isGroup"] as? Boolean ?: false,
            groupName = map["groupName"] as? String ?: ""
        )
    }
}
