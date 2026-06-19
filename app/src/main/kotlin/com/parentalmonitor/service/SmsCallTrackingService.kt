package com.parentalmonitor.service

import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.Telephony
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.CallData
import com.parentalmonitor.data.model.SmsData
import java.util.*

object SmsCallTrackingService {

    private var lastSmsTimestamp: Long = 0
    private var lastCallTimestamp: Long = 0

    fun readNewSms(context: Context): List<SmsData> {
        val smsList = mutableListOf<SmsData>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )

        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(lastSmsTimestamp.toString())
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        try {
            context.contentResolver.query(
                uri, projection, selection, selectionArgs, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val typeCol = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

                while (cursor.moveToNext()) {
                    val date = cursor.getLong(dateCol)
                    if (date > lastSmsTimestamp) {
                        lastSmsTimestamp = date
                    }

                    val type = when (cursor.getInt(typeCol)) {
                        Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                        Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                        else -> "inbox"
                    }

                    smsList.add(
                        SmsData(
                            id = cursor.getString(idCol),
                            sender = cursor.getString(addressCol) ?: "Unknown",
                            content = cursor.getString(bodyCol) ?: "",
                            timestamp = Timestamp(Date(date)),
                            type = type,
                            app = "sms"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return smsList
    }

    fun readNewCalls(context: Context): List<CallData> {
        val callList = mutableListOf<CallData>()
        val uri = CallLog.Calls.CONTENT_URI
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )

        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(lastCallTimestamp.toString())
        val sortOrder = "${CallLog.Calls.DATE} ASC"

        try {
            context.contentResolver.query(
                uri, projection, selection, selectionArgs, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
                val numberCol = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                val nameCol = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                val durationCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                val dateCol = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
                val typeCol = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)

                while (cursor.moveToNext()) {
                    val date = cursor.getLong(dateCol)
                    if (date > lastCallTimestamp) {
                        lastCallTimestamp = date
                    }

                    val type = when (cursor.getInt(typeCol)) {
                        CallLog.Calls.INCOMING_TYPE -> "incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "outgoing"
                        CallLog.Calls.MISSED_TYPE -> "missed"
                        else -> "incoming"
                    }

                    callList.add(
                        CallData(
                            id = cursor.getString(idCol),
                            number = cursor.getString(numberCol) ?: "Unknown",
                            contactName = cursor.getString(nameCol) ?: "",
                            duration = cursor.getLong(durationCol),
                            timestamp = Timestamp(Date(date)),
                            type = type
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return callList
    }

    fun readAllSms(context: Context, limit: Int = 200): List<SmsData> {
        val smsList = mutableListOf<SmsData>()
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )
        val sortOrder = "${Telephony.Sms.DATE} DESC LIMIT $limit"

        try {
            context.contentResolver.query(
                uri, projection, null, null, sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addressCol = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyCol = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateCol = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                val typeCol = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)

                while (cursor.moveToNext()) {
                    val type = when (cursor.getInt(typeCol)) {
                        Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
                        Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
                        else -> "inbox"
                    }

                    smsList.add(
                        SmsData(
                            id = cursor.getString(idCol),
                            sender = cursor.getString(addressCol) ?: "Unknown",
                            content = cursor.getString(bodyCol) ?: "",
                            timestamp = Timestamp(Date(cursor.getLong(dateCol))),
                            type = type,
                            app = "sms"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return smsList
    }
}
