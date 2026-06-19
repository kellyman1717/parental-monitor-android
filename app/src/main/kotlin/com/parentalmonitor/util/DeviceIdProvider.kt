package com.parentalmonitor.util

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Single source of truth for deviceId across the app.
 *
 * - On the child device, [childDeviceId] is a per-install UUID (stable for this install,
 *   different from any other phone including the parent's). Used as the Firestore doc id
 *   under [Constants.COLLECTION_DEVICES].
 * - On the parent device, [parentDeviceId] returns the deviceId that was paired via the
 *   6-digit code entered in the Dashboard. Stored in SharedPreferences.
 *
 * Why not Build.MANUFACTURER + Build.MODEL + Build.ID anymore?
 *   That value is identical on two phones of the same model + OS build, so the parent's
 *   dashboard would read the wrong device. Pairing is the correct fix.
 */
object DeviceIdProvider {

    private const val PREF_KEY_CHILD_UUID = "child_device_uuid"

    fun childDeviceId(context: Context): String {
        val prefs = context.applicationContext
            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        var uuid = prefs.getString(PREF_KEY_CHILD_UUID, null)
        if (uuid == null) {
            // ANDROID_ID is per-app-signing-key + per-device on Android 8+, so it's stable
            // across reinstalls on the same device/account and unique across devices.
            // Falls back to a random UUID if unavailable.
            uuid = try {
                Settings.Secure.getString(
                    context.applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )?.takeIf { it.isNotBlank() }
                    ?: java.util.UUID.randomUUID().toString()
            } catch (e: Exception) {
                java.util.UUID.randomUUID().toString()
            }
            // Tag with hardware for human-readable debugging in Firestore
            val hardware = "${Build.MANUFACTURER}_${Build.MODEL}".replace(" ", "_")
            uuid = "${hardware}__$uuid"
            prefs.edit().putString(PREF_KEY_CHILD_UUID, uuid).apply()
        }
        return uuid
    }

    fun parentDeviceId(context: Context): String? {
        return context.applicationContext
            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            .getString(Constants.PREF_DEVICE_ID, null)
    }

    fun saveParentDeviceId(context: Context, deviceId: String) {
        context.applicationContext
            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(Constants.PREF_DEVICE_ID, deviceId)
            .apply()
    }

    fun clearParentDeviceId(context: Context) {
        context.applicationContext
            .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(Constants.PREF_DEVICE_ID)
            .apply()
    }
}