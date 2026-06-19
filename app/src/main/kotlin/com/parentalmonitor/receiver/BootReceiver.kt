package com.parentalmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.parentalmonitor.service.MainForegroundService
import com.parentalmonitor.util.Constants

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device boot completed, starting monitoring service")

            val sharedPrefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
            val isSetupComplete = sharedPrefs.getBoolean(Constants.PREF_IS_SETUP_COMPLETE, false)

            if (isSetupComplete) {
                MainForegroundService.start(context)
                Log.d(TAG, "Monitoring service started after boot")
            } else {
                Log.d(TAG, "Setup not complete, skipping service start")
            }
        }
    }
}
