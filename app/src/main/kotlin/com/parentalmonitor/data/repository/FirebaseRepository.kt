package com.parentalmonitor.data.repository

import android.content.Context
import android.os.Build
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parentalmonitor.data.model.AppUsageData
import com.parentalmonitor.data.model.BatteryStatus
import com.parentalmonitor.data.model.BatteryUsageData
import com.parentalmonitor.data.model.CallData
import com.parentalmonitor.data.model.DeviceInfo
import com.parentalmonitor.data.model.GeofenceEvent
import com.parentalmonitor.data.model.HomeGeofence
import com.parentalmonitor.data.model.LocationData
import com.parentalmonitor.data.model.SmsData
import com.parentalmonitor.data.model.WhatsAppData
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val deviceId: String by lazy {
        DeviceIdProvider.childDeviceId(context)
    }

    private fun deviceRef() = firestore
        .collection(Constants.COLLECTION_DEVICES)
        .document(deviceId)

    // ========== DEVICE INFO ==========

    suspend fun registerDevice(name: String): Result<Unit> {
        return try {
            val info = DeviceInfo(
                deviceId = deviceId,
                name = name,
                model = "${Build.MANUFACTURER} ${Build.MODEL}",
                registeredAt = Timestamp.now(),
                isActive = true
            )
            deviceRef().set(info.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeviceInfo(): Result<DeviceInfo?> {
        return try {
            val doc = deviceRef().get().await()
            if (doc.exists()) {
                Result.success(DeviceInfo.fromMap(doc.data ?: emptyMap(), deviceId))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== LOCATION ==========

    suspend fun uploadLocation(location: LocationData): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_LOCATIONS)
                .add(location.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLocations(limit: Long = 100): Result<List<LocationData>> {
        return try {
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_LOCATIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val locations = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { LocationData.fromMap(it, doc.id) }
            }
            Result.success(locations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestLocation(): Result<LocationData?> {
        return try {
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_LOCATIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val location = snapshot.documents.firstOrNull()?.data?.let {
                LocationData.fromMap(it, snapshot.documents.first().id)
            }
            Result.success(location)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== SMS ==========

    suspend fun uploadSms(sms: SmsData): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_MESSAGES)
                .add(sms.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadSmsBatch(smsList: List<SmsData>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = deviceRef().collection(Constants.SUBCOLLECTION_MESSAGES)
            smsList.forEach { sms ->
                batch.set(collection.document(), sms.toMap())
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(limit: Long = 100, appFilter: String? = null): Result<List<SmsData>> {
        return try {
            var query = deviceRef()
                .collection(Constants.SUBCOLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            if (appFilter != null) {
                query = query.whereEqualTo("app", appFilter)
            }

            val snapshot = query.get().await()
            val messages = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { SmsData.fromMap(it, doc.id) }
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== CALLS ==========

    suspend fun uploadCall(call: CallData): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_CALLS)
                .add(call.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCalls(limit: Long = 100): Result<List<CallData>> {
        return try {
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_CALLS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val calls = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { CallData.fromMap(it, doc.id) }
            }
            Result.success(calls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== APP USAGE ==========

    suspend fun uploadAppUsage(usage: AppUsageData): Result<Unit> {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val docId = "${usage.packageName}_$date"
            deviceRef()
                .collection(Constants.SUBCOLLECTION_APP_USAGE)
                .document(docId)
                .set(usage.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppUsage(date: String? = null): Result<List<AppUsageData>> {
        return try {
            val targetDate = date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_APP_USAGE)
                .whereEqualTo("date", targetDate)
                .orderBy("durationMs", Query.Direction.DESCENDING)
                .get()
                .await()
            val usage = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { AppUsageData.fromMap(it, doc.id) }
            }
            Result.success(usage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GEOFENCE ==========

    suspend fun uploadGeofenceEvent(event: GeofenceEvent): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_GEOFENCE_EVENTS)
                .add(event.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadHomeGeofence(geofence: HomeGeofence): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_SETTINGS)
                .document("home_geofence")
                .set(geofence.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHomeGeofence(): Result<HomeGeofence?> {
        return try {
            val doc = deviceRef()
                .collection(Constants.SUBCOLLECTION_SETTINGS)
                .document("home_geofence")
                .get()
                .await()
            if (doc.exists()) {
                Result.success(HomeGeofence.fromMap(doc.data ?: emptyMap()))
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== BATTERY ==========

    suspend fun uploadBatteryUsage(usage: BatteryUsageData): Result<Unit> {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val docId = "${usage.packageName}_$date"
            deviceRef()
                .collection(Constants.SUBCOLLECTION_BATTERY)
                .document(docId)
                .set(usage.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadBatteryStatus(status: BatteryStatus): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_BATTERY_STATUS)
                .add(status.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBatteryUsage(date: String? = null): Result<List<BatteryUsageData>> {
        return try {
            val targetDate = date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_BATTERY)
                .whereEqualTo("date", targetDate)
                .orderBy("foregroundTimeMs", Query.Direction.DESCENDING)
                .get()
                .await()
            val usage = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { BatteryUsageData.fromMap(it, doc.id) }
            }
            Result.success(usage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLatestBatteryStatus(): Result<BatteryStatus?> {
        return try {
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_BATTERY_STATUS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            val status = snapshot.documents.firstOrNull()?.data?.let {
                BatteryStatus.fromMap(it)
            }
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== WHATSAPP ==========

    suspend fun uploadWhatsApp(data: WhatsAppData): Result<Unit> {
        return try {
            deviceRef()
                .collection(Constants.SUBCOLLECTION_WHATSAPP)
                .add(data.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWhatsAppMessages(limit: Long = 100): Result<List<WhatsAppData>> {
        return try {
            val snapshot = deviceRef()
                .collection(Constants.SUBCOLLECTION_WHATSAPP)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            val messages = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { WhatsAppData.fromMap(it, doc.id) }
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== AUTH ==========

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentEmail(): String? = auth.currentUser?.email
}
