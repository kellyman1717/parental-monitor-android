package com.parentalmonitor.util

object Constants {
    // Secret dialer code to access hidden app: *#*#1234#*#*
    const val SECRET_CODE = "1234"

    // Notification channels
    const val CHANNEL_ID_MONITORING = "monitoring_service"
    const val CHANNEL_ID_ALERTS = "parental_alerts"
    const val NOTIFICATION_ID_FOREGROUND = 1001
    const val NOTIFICATION_ID_ALERT = 1002

    // Location tracking
    const val LOCATION_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
    const val LOCATION_FASTEST_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
    const val LOCATION_MIN_DISTANCE_METERS = 50f

    // Firebase collections
    const val COLLECTION_DEVICES = "devices"
    const val COLLECTION_PAIRING_CODES = "pairing_codes"
    const val SUBCOLLECTION_LOCATIONS = "locations"
    const val SUBCOLLECTION_MESSAGES = "messages"
    const val SUBCOLLECTION_CALLS = "calls"
    const val SUBCOLLECTION_APP_USAGE = "app_usage"
    const val SUBCOLLECTION_WHATSAPP = "whatsapp"
    const val SUBCOLLECTION_ALERTS = "alerts"
    const val SUBCOLLECTION_BATTERY = "battery_usage"
    const val SUBCOLLECTION_BATTERY_STATUS = "battery_status"
    const val SUBCOLLECTION_GEOFENCE_EVENTS = "geofence_events"
    const val SUBCOLLECTION_SETTINGS = "settings"

    // Data sync
    const val SYNC_BATCH_SIZE = 50
    const val SYNC_INTERVAL_MINUTES = 15L

    // Geofencing
    const val GEOFENCE_RADIUS_METERS = 500f
    const val GEOFENCE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours

    // Shared preferences
    const val PREF_NAME = "parental_monitor_prefs"
    const val PREF_DEVICE_ID = "device_id"
    const val PREF_IS_SETUP_COMPLETE = "is_setup_complete"
    const val PREF_PARENT_EMAIL = "parent_email"
    const val PREF_SECRET_CODE = "custom_secret_code"

    // Pairing
    const val PAIRING_CODE_EXPIRY_MS = 10 * 60 * 1000L // 10 minutes
    const val PREF_IS_PAIRED = "is_paired"
    const val PREF_PAIRING_CODE = "pairing_code"
}
