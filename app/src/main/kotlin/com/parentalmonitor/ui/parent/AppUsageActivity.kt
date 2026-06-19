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
import com.parentalmonitor.data.model.AppUsageData
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import com.parentalmonitor.ui.theme.DesignColors
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AppUsageActivity : ComponentActivity() {
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
                AppUsageScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var appUsage by remember { mutableStateOf<List<AppUsageData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf(Date()) }

    LaunchedEffect(selectedDate) {
        val deviceId = DeviceIdProvider.parentDeviceId(context) ?: return@LaunchedEffect
        isLoading = true
        try {
            val firestore = FirebaseFirestore.getInstance()
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)

            val snapshot = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .collection(Constants.SUBCOLLECTION_APP_USAGE)
                .whereEqualTo("date", dateStr)
                .orderBy("durationMs", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            appUsage = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { AppUsageData.fromMap(it, doc.id) }
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📱 Penggunaan Aplikasi") },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Date selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply { time = selectedDate; add(Calendar.DAY_OF_MONTH, -1) }
                        selectedDate = cal.time
                    }) {
                        Icon(Icons.Default.ChevronLeft, "Previous day")
                    }

                    Text(
                        SimpleDateFormat("dd MMMM yyyy", Locale("id")).format(selectedDate),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        val cal = Calendar.getInstance().apply { time = selectedDate; add(Calendar.DAY_OF_MONTH, 1) }
                        if (cal.time.before(Date())) selectedDate = cal.time
                    }) {
                        Icon(Icons.Default.ChevronRight, "Next day")
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (appUsage.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Apps, null, modifier = Modifier.size(64.dp), tint = DesignColors.TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Belum ada data penggunaan", color = DesignColors.TextMuted)
                    }
                }
            } else {
                // Total usage summary
                val totalDuration = appUsage.sumOf { it.durationMs }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Penggunaan Hari Ini", color = DesignColors.TextMuted)
                        Text(
                            formatDurationMs(totalDuration),
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("${appUsage.size} aplikasi digunakan", color = DesignColors.TextMuted, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(appUsage) { usage ->
                        val maxDuration = appUsage.maxOfOrNull { it.durationMs } ?: 1
                        val progress = usage.durationMs.toFloat() / maxDuration

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(DesignColors.Warning.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Apps,
                                                null,
                                                tint = DesignColors.Warning,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(usage.appName, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                usage.packageName,
                                                fontSize = 11.sp,
                                                color = DesignColors.TextMuted
                                            )
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatDurationMs(usage.durationMs),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        if (usage.openCount > 0) {
                                            Text(
                                                "${usage.openCount}x dibuka",
                                                fontSize = 11.sp,
                                                color = DesignColors.TextMuted
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
                                    color = when {
                                        progress > 0.8f -> DesignColors.Danger
                                        progress > 0.5f -> DesignColors.Warning
                                        else -> DesignColors.Success
                                    },
                                    trackColor = Color(0xFFE0E0E0)
                                )
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

fun formatDurationMs(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60

    return when {
        hours > 0 -> "${hours}j ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
