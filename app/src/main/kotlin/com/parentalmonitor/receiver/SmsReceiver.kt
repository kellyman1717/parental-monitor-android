package com.parentalmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.SmsData
import com.parentalmonitor.data.repository.FirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            messages?.forEach { smsMessage ->
                val sender = smsMessage.displayOriginatingAddress ?: "Unknown"
                val content = smsMessage.messageBody ?: ""
                val timestamp = Timestamp(Date(smsMessage.timestampMillis))

                Log.d(TAG, "New SMS from $sender: ${content.take(50)}...")

                // Upload to Firebase in background
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    try {
                        val firestore = FirebaseFirestore.getInstance()
                        val auth = FirebaseAuth.getInstance()

                        val smsData = SmsData(
                            sender = sender,
                            content = content,
                            timestamp = timestamp,
                            type = "inbox",
                            app = "sms"
                        )

                        // Get device ID
                        val deviceId = DeviceIdProvider.childDeviceId(context)

                        firestore.collection("devices")
                            .document(deviceId)
                            .collection("messages")
                            .add(smsData.toMap())

                        Log.d(TAG, "Real-time SMS uploaded to Firebase")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to upload SMS", e)
                    }
                }
            }
        }
    }
}
