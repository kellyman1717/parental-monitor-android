package com.parentalmonitor.service

import android.app.Notification
import android.app.Notification.EXTRA_BIG_TEXT
import android.app.Notification.EXTRA_INFO_TEXT
import android.app.Notification.EXTRA_MESSAGES
import android.app.Notification.EXTRA_SUB_TEXT
import android.app.Notification.EXTRA_TEXT
import android.app.Notification.EXTRA_TITLE
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.WhatsAppData
import com.parentalmonitor.data.repository.FirebaseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.Date
import javax.inject.Inject

/**
 * Service untuk menangkap semua notifikasi WhatsApp yang masuk.
 *
 * Cara kerja:
 * 1. Android mengirim notifikasi saat pesan WhatsApp masuk
 * 2. Service ini "mendengarkan" semua notifikasi tersebut
 * 3. Data diekstrak dari Bundle extras notifikasi
 * 4. Data disimpan ke Firebase Firestore
 *
 * Data yang bisa diambil dari notifikasi:
 * - EXTRA_TITLE     → Nama pengirim (personal) atau "Nama @ Grup" (grup)
 * - EXTRA_TEXT      → Isi pesan terakhir
 * - EXTRA_BIG_TEXT  → Isi pesan lengkap (saat notifikasi di-expand)
 * - EXTRA_SUB_TEXT  → Info grup / jumlah chat
 * - EXTRA_INFO_TEXT → Jumlah pesan masuk
 * - EXTRA_MESSAGES  → Array of Message objects (Android N+)
 * - postTime        → Timestamp notifikasi
 * - packageName     → com.whatsapp atau com.whatsapp.w4b
 */
@AndroidEntryPoint
class WhatsAppNotificationService : NotificationListenerService() {

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Deduplication: simpan hash pesan terakhir untuk menghindari duplikat
    private val processedMessages = LinkedHashSet<String>(50) // Max 50 recent hashes

    companion object {
        private const val TAG = "WhatsAppNotif"
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        private const val MAX_PROCESSED_CACHE = 100
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        when (sbn.packageName?.toString()) {
            WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE -> {
                processWhatsAppNotification(sbn)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Tidak perlu action saat notifikasi dihapus
    }

    private fun processWhatsAppNotification(sbn: StatusBarNotification) {
        try {
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            // Skip notifikasi summary (group bundle)
            if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) {
                Log.d(TAG, "Skipping group summary notification")
                return
            }

            // ========================================
            // EKSTRAK DATA DARI NOTIFICATION EXTRAS
            // ========================================

            val title = extras.getCharSequence(EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(EXTRA_BIG_TEXT)?.toString()
            val subText = extras.getCharSequence(EXTRA_SUB_TEXT)?.toString()
            val infoText = extras.getCharSequence(EXTRA_INFO_TEXT)?.toString()

            // Timestamp dari notifikasi
            val postTime = sbn.postTime

            // Deteksi WhatsApp Business
            val isBusiness = sbn.packageName == WHATSAPP_BUSINESS_PACKAGE

            // ========================================
            // DETEKSI TIPE PESAN (Personal vs Grup)
            // ========================================

            // Cek dari title format: "Nama" (personal) atau "Nama @ NamaGrup" (grup)
            // Atau dari subText yang berisi nama grup
            val isGroup = detectIfGroup(title, subText, extras)

            // ========================================
            // EKSTRAK NAMA PENGIRIM & ISI PESAN
            // ========================================

            val sender: String
            val groupName: String
            val messageContent: String

            if (isGroup) {
                // Format grup: title = "NamaPengirim @ NamaGrup"
                // atau title = "NamaPengirim" dengan subText = "NamaGrup"
                val groupInfo = parseGroupInfo(title, subText)
                sender = groupInfo.first
                groupName = groupInfo.second
                messageContent = extractMessageContent(text, bigText, sender)
            } else {
                // Personal chat: title = nama pengirim
                sender = title
                groupName = ""
                messageContent = bigText ?: text
            }

            // ========================================
            // CEK APAKAH INI NOTIFIKASI "X PESAN BARU"
            // ========================================

            // WhatsApp kadang mengirim notifikasi ringkasan seperti "3 pesan baru"
            // Kita skip ini karena tidak ada isi pesan spesifik
            if (isSummaryNotification(text, infoText)) {
                Log.d(TAG, "Skipping summary notification: $text")
                return
            }

            // ========================================
            // DEDUPLIKASI
            // ========================================

            val messageHash = "$sender:$messageContent:$groupName".hashCode().toString()
            if (processedMessages.contains(messageHash)) {
                Log.d(TAG, "Duplicate message detected, skipping")
                return
            }

            // Tambahkan ke cache & bersihkan jika terlalu besar
            processedMessages.add(messageHash)
            if (processedMessages.size > MAX_PROCESSED_CACHE) {
                val iterator = processedMessages.iterator()
                repeat(20) { // Hapus 20 terlama
                    if (iterator.hasNext()) {
                        iterator.next()
                        iterator.remove()
                    }
                }
            }

            // ========================================
            // BUAT DATA & UPLOAD KE FIREBASE
            // ========================================

            if (messageContent.isNotEmpty() && sender.isNotEmpty()) {
                val whatsappData = WhatsAppData(
                    sender = sender,
                    content = messageContent.trim(),
                    timestamp = Timestamp(Date(postTime)),
                    isGroup = isGroup,
                    groupName = groupName
                )

                serviceScope.launch {
                    try {
                        firebaseRepository.uploadWhatsApp(whatsappData)
                        Log.d(TAG, "✅ WhatsApp saved: [$sender] ${messageContent.take(50)}")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to save WhatsApp notification", e)
                    }
                }
            } else {
                Log.d(TAG, "Empty message or sender, skipping")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing WhatsApp notification", e)
        }
    }

    /**
     * Deteksi apakah pesan berasal dari grup
     */
    private fun detectIfGroup(title: String, subText: String?, extras: Bundle): Boolean {
        // Method 1: Title mengandung "@" (format WhatsApp: "Nama @ Grup")
        if (title.contains(" @ ")) return true

        // Method 2: SubText berisi nama grup
        if (!subText.isNullOrEmpty() && subText != title) return true

        // Method 3: Cek conversation style (Android 11+)
        try {
            val messages = extras.getParcelableArray(EXTRA_MESSAGES)
            if (messages != null && messages.size > 0) {
                // Jika ada multiple messages, kemungkinan grup
                return false // Tidak bisa pastikan dari sini saja
            }
        } catch (_: Exception) {}

        // Method 4: Title mengandung karakter khas grup WhatsApp
        // Grup biasanya format: "Pengirim di NamaGrup" atau "Pengirim - NamaGrup"
        if (title.contains(" di ") || title.contains(" dalam ")) return true

        return false
    }

    /**
     * Parse info grup dari title dan subText
     * Return: Pair(senderName, groupName)
     */
    private fun parseGroupInfo(title: String, subText: String?): Pair<String, String> {
        // Format 1: "Pengirim @ NamaGrup"
        if (title.contains(" @ ")) {
            val parts = title.split(" @ ", limit = 2)
            return Pair(parts[0].trim(), parts[1].trim())
        }

        // Format 2: "Pengirim di NamaGrup"
        if (title.contains(" di ")) {
            val parts = title.split(" di ", limit = 2)
            return Pair(parts[0].trim(), parts[1].trim())
        }

        // Format 3: "Pengirim dalam NamaGrup"
        if (title.contains(" dalam ")) {
            val parts = title.split(" dalam ", limit = 2)
            return Pair(parts[0].trim(), parts[1].trim())
        }

        // Format 4: title = sender, subText = groupName
        if (!subText.isNullOrEmpty()) {
            return Pair(title, subText)
        }

        // Fallback
        return Pair(title, "")
    }

    /**
     * Ekstrak isi pesan dari text
     * Untuk grup, text biasanya format: "Pengirim: IsiPesan"
     */
    private fun extractMessageContent(text: String, bigText: String?, senderName: String): String {
        val content = bigText ?: text

        // Untuk grup, WhatsApp format: "Nama: Pesan" atau "Nama Pengirim: Pesan"
        // Kita perlu strip nama pengirim dari awal text
        if (senderName.isNotEmpty() && content.startsWith("$senderName:")) {
            return content.removePrefix("$senderName:").trim()
        }

        // Format alternatif: "Pengirim : Pesan" (dengan spasi sebelum colon)
        if (senderName.isNotEmpty()) {
            val patterns = listOf(
                "$senderName:",
                "$senderName :",
                "$senderName: "
            )
            for (pattern in patterns) {
                if (content.startsWith(pattern)) {
                    return content.removePrefix(pattern).trim()
                }
            }
        }

        // Jika bigText tersedia, gunakan itu (lebih lengkap)
        return bigText ?: text
    }

    /**
     * Deteksi notifikasi ringkasan seperti "3 pesan baru dari 2 chat"
     */
    private fun isSummaryNotification(text: String, infoText: String?): Boolean {
        val lowerText = text.lowercase()

        // Pattern: "X pesan baru", "X new messages", "X messages from Y chats"
        if (lowerText.matches(Regex("\\d+\\s*(pesan|new)?\\s*(messages?|chat).*"))) return true
        if (lowerText.matches(Regex("\\d+\\s*messages?\\s*from.*"))) return true

        // Pattern: missed calls
        val missedPatterns = listOf(
            "panggilan tidak terjawab",
            "missed call",
            "missed video call",
            "missed voice call",
            "panggilan video tidak terjawab",
            "panggilan suara tidak terjawab"
        )
        if (missedPatterns.any { lowerText.contains(it) }) return true

        // Info text juga bisa mendeteksi summary: "3 messages from 2 chats"
        if (infoText != null) {
            val lowerInfo = infoText.lowercase()
            if (lowerInfo.contains("messages from") || lowerInfo.contains("pesan dari")) return true
        }

        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
