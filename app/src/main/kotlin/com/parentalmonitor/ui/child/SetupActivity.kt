package com.parentalmonitor.ui.child

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.parentalmonitor.data.model.DeviceInfo
import com.parentalmonitor.service.MainForegroundService
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import com.parentalmonitor.util.PermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SetupActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            checkAndRequestBackgroundLocation()
        } else {
            Toast.makeText(this, "Semua permission diperlukan untuk monitoring", Toast.LENGTH_LONG).show()
        }
    }

    private val requestBackgroundLocationLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            updatePermissionStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = DesignColors.Primary,
                    secondary = DesignColors.Secondary,
                    surface = DesignColors.Surface,
                    background = DesignColors.Bg,
                    onPrimary = DesignColors.OnPrimary,
                    onSurface = DesignColors.Text,
                )
            ) {
                SetupScreen(
                    onGrantPermissions = { requestPermissions() },
                    onOpenAccessibility = { PermissionHelper.openAccessibilitySettings(this) },
                    onOpenNotificationListener = { PermissionHelper.openNotificationListenerSettings(this) },
                    onOpenUsageStats = { PermissionHelper.openUsageStatsSettings(this) },
                    onStartMonitoring = { startMonitoring() },
                    onOpenBatterySettings = { openBatteryOptimizationSettings() }
                )
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun checkAndRequestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    private fun updatePermissionStatus() {
        // This will trigger recomposition
    }

    private fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Buka Settings > Battery > Parental Monitor > Don't optimize", Toast.LENGTH_LONG).show()
        }
    }

    private fun startMonitoring() {
        val sharedPrefs = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(Constants.PREF_IS_SETUP_COMPLETE, true).apply()

        val deviceId = DeviceIdProvider.childDeviceId(this)
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
        val code = (100000..999999).random().toString()
        val now = Timestamp.now()
        val expiresAt = Timestamp(now.seconds + Constants.PAIRING_CODE_EXPIRY_MS / 1000, 0)

        val pairingDoc = hashMapOf(
            "deviceId" to deviceId,
            "deviceName" to deviceName,
            "createdAt" to now,
            "expiresAt" to expiresAt,
            "parentUid" to null
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.COLLECTION_PAIRING_CODES)
            .document(code)
            .set(pairingDoc)
            .addOnSuccessListener {
                val deviceInfo = DeviceInfo(
                    deviceId = deviceId,
                    name = deviceName,
                    model = deviceName,
                    registeredAt = now,
                    isActive = true,
                    pairingCode = code
                )
                FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_DEVICES)
                    .document(deviceId)
                    .set(deviceInfo.toMap())

                sharedPrefs.edit()
                    .putString("current_pairing_code", code)
                    .apply()

                MainForegroundService.start(this)

                val intent = Intent(this, PairingWaitActivity::class.java).apply {
                    putExtra("pairing_code", code)
                    putExtra("device_id", deviceId)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal membuat kode pairing: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onGrantPermissions: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenNotificationListener: () -> Unit,
    onOpenUsageStats: () -> Unit,
    onStartMonitoring: () -> Unit,
    onOpenBatterySettings: () -> Unit
) {
    var permissionsGranted by remember { mutableStateOf(false) }
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var notificationListenerEnabled by remember { mutableStateOf(false) }
    var usageStatsEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Monitoring") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Parental Monitor Setup",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Aplikasi ini akan berjalan di background untuk memantau aktivitas HP anak Anda. Aplikasi tidak akan terlihat di launcher.",
                        color = DesignColors.TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Permissions
            Text("Langkah 1: Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SetupStepCard(
                icon = Icons.Default.Security,
                title = "Grant Permissions",
                description = "Lokasi, SMS, Panggilan, Notifikasi",
                isCompleted = permissionsGranted,
                onClick = onGrantPermissions
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Special Permissions
            Text("Langkah 2: Permission Khusus", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SetupStepCard(
                icon = Icons.Default.TouchApp,
                title = "Accessibility Service",
                description = "Untuk membaca pesan WhatsApp",
                isCompleted = accessibilityEnabled,
                onClick = onOpenAccessibility
            )

            Spacer(modifier = Modifier.height(8.dp))

            SetupStepCard(
                icon = Icons.Default.Notifications,
                title = "Notification Listener",
                description = "Untuk menangkap notifikasi WhatsApp",
                isCompleted = notificationListenerEnabled,
                onClick = onOpenNotificationListener
            )

            Spacer(modifier = Modifier.height(8.dp))

            SetupStepCard(
                icon = Icons.Default.BarChart,
                title = "Usage Stats Access",
                description = "Untuk memantau penggunaan aplikasi",
                isCompleted = usageStatsEnabled,
                onClick = onOpenUsageStats
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Battery
            Text("Langkah 3: Battery Optimization", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SetupStepCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "Disable Battery Optimization",
                description = "Agar service tidak di-kill oleh sistem",
                isCompleted = false,
                onClick = onOpenBatterySettings
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Start Button
            Button(
                onClick = onStartMonitoring,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mulai Monitoring", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚠️ Catatan Penting", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Aplikasi akan tersembunyi dari launcher", fontSize = 14.sp)
                    Text("• Akses kembali via dialer: *#*#1234#*#*", fontSize = 14.sp)
                    Text("• Service akan auto-start setelah restart", fontSize = 14.sp)
                    Text("• Setelah klik Mulai, kode 6-digit akan muncul", fontSize = 14.sp)
                    Text("• Masukkan kode di HP orang tua untuk menghubungkan", fontSize = 14.sp)
                    Text("• Beri tahu anak bahwa HP-nya dipantau", fontSize = 14.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupStepCard(
    icon: ImageVector,
    title: String,
    description: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isCompleted) DesignColors.Success else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, fontSize = 14.sp, color = DesignColors.TextMuted)
            }
            if (isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Done",
                    tint = DesignColors.Success
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next"
                )
            }
        }
    }
}