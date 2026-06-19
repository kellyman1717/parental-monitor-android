package com.parentalmonitor.ui.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parentalmonitor.data.model.BatteryStatus
import com.parentalmonitor.data.model.BatteryUsageData
import com.parentalmonitor.service.BatteryUsageTrackingService
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import com.parentalmonitor.ui.theme.DesignColors
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class BatteryUsageActivity : ComponentActivity() {
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
                BatteryUsageScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryUsageScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var batteryStatus by remember { mutableStateOf<BatteryStatus?>(null) }
    var batteryApps by remember { mutableStateOf<List<BatteryUsageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val deviceId = DeviceIdProvider.parentDeviceId(context) ?: return@LaunchedEffect
        try {
            // Get live battery status
            batteryStatus = BatteryUsageTrackingService.getBatteryStatus(context)

            // Get battery consuming apps from Firebase
            val firestore = FirebaseFirestore.getInstance()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val snapshot = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .collection(Constants.SUBCOLLECTION_BATTERY)
                .whereEqualTo("date", dateStr)
                .orderBy("foregroundTimeMs", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()

            batteryApps = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { BatteryUsageData.fromMap(it, doc.id) }
            }

            // If no data from Firebase, get live data
            if (batteryApps.isEmpty()) {
                batteryApps = BatteryUsageTrackingService.getBatteryConsumingApps(context)
            }

            isLoading = false
        } catch (e: Exception) {
            // Fallback to live data
            batteryApps = BatteryUsageTrackingService.getBatteryConsumingApps(context)
            batteryStatus = BatteryUsageTrackingService.getBatteryStatus(context)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔋 Penggunaan Baterai") },
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Battery Status Card
                item {
                    batteryStatus?.let { status ->
                        BatteryStatusCard(status)
                    }
                }

                // Tab selector
                item {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("📊 Peringkat") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("📱 Semua App") }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> {
                        // Top Battery Consumers
                        item {
                            Text(
                                "Aplikasi Paling Boros Baterai",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        items(batteryApps.take(10)) { app ->
                            BatteryAppItem(
                                app = app,
                                rank = batteryApps.indexOf(app) + 1,
                                maxForegroundMs = batteryApps.maxOfOrNull { it.foregroundTimeMs } ?: 1
                            )
                        }
                    }
                    1 -> {
                        // All apps
                        item {
                            Text(
                                "Semua Aplikasi (${batteryApps.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        items(batteryApps) { app ->
                            BatteryAppItem(
                                app = app,
                                rank = batteryApps.indexOf(app) + 1,
                                maxForegroundMs = batteryApps.maxOfOrNull { it.foregroundTimeMs } ?: 1
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun BatteryStatusCard(status: BatteryStatus) {
    val batteryColor = when {
        status.level > 60 -> DesignColors.Success
        status.level > 20 -> DesignColors.Warning
        else -> DesignColors.Danger
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Status Baterai", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        if (status.isCharging) "Mengisi (${status.chargingSource})" else "Tidak mengisi",
                        color = DesignColors.TextMuted
                    )
                }

                // Battery level circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(batteryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${status.level}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = batteryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Battery details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryDetailItem(
                    icon = Icons.Default.Thermostat,
                    label = "Suhu",
                    value = "${String.format("%.1f", status.temperature)}°C",
                    color = if (status.temperature > 40) DesignColors.Danger else DesignColors.Info
                )
                BatteryDetailItem(
                    icon = Icons.Default.ElectricBolt,
                    label = "Voltase",
                    value = "${status.voltage} mV",
                    color = DesignColors.Warning
                )
                BatteryDetailItem(
                    icon = Icons.Default.HealthAndSafety,
                    label = "Kesehatan",
                    value = status.health,
                    color = if (status.health == "Baik") DesignColors.Success else DesignColors.Danger
                )
            }

            if (status.technology.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Teknologi: ${status.technology}",
                    fontSize = 12.sp,
                    color = DesignColors.TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Progress bar
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = status.level / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = batteryColor,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
fun BatteryDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = DesignColors.TextMuted)
    }
}

@Composable
fun BatteryAppItem(
    app: BatteryUsageData,
    rank: Int,
    maxForegroundMs: Long
) {
    val progress = if (maxForegroundMs > 0) {
        app.foregroundTimeMs.toFloat() / maxForegroundMs
    } else 0f

    val drainColor = when {
        app.batteryDrainEstimate > 10 -> DesignColors.Danger
        app.batteryDrainEstimate > 5 -> DesignColors.Warning
        app.batteryDrainEstimate > 2 -> Color(0xFFFFC107)
        else -> DesignColors.Success
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> Color(0xFFFFD700) // Gold
                                2 -> Color(0xFFC0C0C0) // Silver
                                3 -> Color(0xFFCD7F32) // Bronze
                                else -> Color(0xFFE0E0E0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$rank",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (rank <= 3) Color.White else DesignColors.TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // App icon placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(drainColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Apps,
                        null,
                        tint = drainColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(app.appName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        app.packageName,
                        fontSize = 11.sp,
                        color = DesignColors.TextMuted,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formatBatteryDuration(app.foregroundTimeMs),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (app.batteryDrainEstimate > 0) {
                        Text(
                            "~${String.format("%.1f", app.batteryDrainEstimate)}%",
                            fontSize = 12.sp,
                            color = drainColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = drainColor,
                trackColor = Color(0xFFE0E0E0)
            )

            // Footer info
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (app.openCount > 0) {
                    Text(
                        "${app.openCount}x dibuka",
                        fontSize = 11.sp,
                        color = DesignColors.TextMuted
                    )
                }
                Text(
                    "Terakhir: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(app.lastUsed.toDate())}",
                    fontSize = 11.sp,
                    color = DesignColors.TextMuted
                )
            }
        }
    }
}

fun formatBatteryDuration(ms: Long): String {
    val hours = ms / (1000 * 60 * 60)
    val minutes = (ms / (1000 * 60)) % 60

    return when {
        hours > 0 -> "${hours}j ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "< 1m"
    }
}
