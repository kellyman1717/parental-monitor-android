package com.parentalmonitor.ui.parent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.parentalmonitor.service.MainForegroundService
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.PermissionHelper
import com.parentalmonitor.ui.theme.DesignColors

class SettingsActivity : ComponentActivity() {
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
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    var isMonitoringActive by remember { mutableStateOf(true) }
    var secretCode by remember { mutableStateOf(sharedPrefs.getString(Constants.PREF_SECRET_CODE, "1234") ?: "1234") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👤 Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Email")
                        Text(FirebaseAuth.getInstance().currentUser?.email ?: "-", color = DesignColors.TextMuted)
                    }
                }
            }

            // Geofence control
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏠 Geofence Rumah", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Alarm beep jika HP keluar dari radius rumah",
                        fontSize = 13.sp,
                        color = DesignColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            context.startActivity(Intent(context, HomeGeofenceActivity::class.java))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Setup Lokasi Rumah")
                    }
                }
            }

            // Monitoring control
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔒 Monitoring", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Status Monitoring")
                            Text(
                                if (isMonitoringActive) "Aktif" else "Nonaktif",
                                fontSize = 14.sp,
                                color = if (isMonitoringActive) DesignColors.Success else DesignColors.Danger
                            )
                        }
                        Switch(
                            checked = isMonitoringActive,
                            onCheckedChange = { active ->
                                isMonitoringActive = active
                                if (active) {
                                    MainForegroundService.start(context)
                                    Toast.makeText(context, "Monitoring diaktifkan", Toast.LENGTH_SHORT).show()
                                } else {
                                    MainForegroundService.stop(context)
                                    Toast.makeText(context, "Monitoring dinonaktifkan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Secret code
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔑 Kode Rahasia", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Kode untuk mengakses aplikasi tersembunyi di HP anak",
                        fontSize = 13.sp,
                        color = DesignColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = secretCode,
                        onValueChange = { secretCode = it },
                        label = { Text("Kode Rahasia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        prefix = { Text("*#*#") },
                        suffix = { Text("#*#*") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Akses di dialer: *#*#${secretCode}#*#*",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Permissions check
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 Status Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionStatusItem("Lokasi", PermissionHelper.hasLocationPermission(context))
                    PermissionStatusItem("SMS", PermissionHelper.hasSmsPermission(context))
                    PermissionStatusItem("Panggilan", PermissionHelper.hasCallLogPermission(context))
                    PermissionStatusItem("Usage Stats", PermissionHelper.hasUsageStatsPermission(context))
                    PermissionStatusItem("Accessibility", PermissionHelper.isAccessibilityServiceEnabled(context))
                    PermissionStatusItem("Notification Listener", PermissionHelper.hasNotificationListenerPermission(context))
                }
            }

            // Device info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📱 Info Device", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Model")
                        Text("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", color = DesignColors.TextMuted)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Android")
                        Text(android.os.Build.VERSION.RELEASE, color = DesignColors.TextMuted)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Device ID")
                        Text(
                            "${android.os.Build.MANUFACTURER}_${android.os.Build.MODEL}".take(20),
                            color = DesignColors.TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PermissionStatusItem(name: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name)
        Icon(
            if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
            null,
            tint = if (isGranted) DesignColors.Success else DesignColors.Danger,
            modifier = Modifier.size(20.dp)
        )
    }
}
