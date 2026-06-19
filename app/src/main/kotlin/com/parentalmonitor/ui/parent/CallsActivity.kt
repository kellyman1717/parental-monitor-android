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
import com.parentalmonitor.data.model.CallData
import com.parentalmonitor.util.Constants
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class CallsActivity : ComponentActivity() {
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
                CallsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var calls by remember { mutableStateOf<List<CallData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val deviceId = DeviceIdProvider.parentDeviceId(context) ?: return@LaunchedEffect
        try {
            val firestore = FirebaseFirestore.getInstance()

            val snapshot = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .collection(Constants.SUBCOLLECTION_CALLS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()

            calls = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { CallData.fromMap(it, doc.id) }
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📞 Riwayat Panggilan") },
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
        } else if (calls.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CallEnd, null, modifier = Modifier.size(64.dp), tint = DesignColors.TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada data panggilan", color = DesignColors.TextMuted)
                }
            }
        } else {
            // Stats summary
            val incomingCount = calls.count { it.type == "incoming" }
            val outgoingCount = calls.count { it.type == "outgoing" }
            val missedCount = calls.count { it.type == "missed" }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stats card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CallStatItem("Masuk", incomingCount, DesignColors.Success)
                            CallStatItem("Keluar", outgoingCount, DesignColors.Info)
                            CallStatItem("Tak Jawab", missedCount, DesignColors.Danger)
                        }
                    }
                }

                // Call list
                items(calls) { call ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (call.type) {
                                            "missed" -> DesignColors.Danger.copy(alpha = 0.1f)
                                            "incoming" -> DesignColors.Success.copy(alpha = 0.1f)
                                            else -> DesignColors.Info.copy(alpha = 0.1f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (call.type) {
                                        "missed" -> Icons.Default.CallMissed
                                        "incoming" -> Icons.Default.CallReceived
                                        else -> Icons.Default.CallMade
                                    },
                                    null,
                                    tint = when (call.type) {
                                        "missed" -> DesignColors.Danger
                                        "incoming" -> DesignColors.Success
                                        else -> DesignColors.Info
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    call.contactName.ifEmpty { call.number },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Row {
                                    Text(
                                        when (call.type) {
                                            "missed" -> "Tak Terjawab"
                                            "incoming" -> "Masuk"
                                            else -> "Keluar"
                                        },
                                        fontSize = 13.sp,
                                        color = when (call.type) {
                                            "missed" -> DesignColors.Danger
                                            "incoming" -> DesignColors.Success
                                            else -> DesignColors.Info
                                        }
                                    )
                                    Text(" • ", fontSize = 13.sp, color = DesignColors.TextMuted)
                                    Text(
                                        formatDuration(call.duration),
                                        fontSize = 13.sp,
                                        color = DesignColors.TextMuted
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(call.timestamp.toDate()),
                                    fontSize = 12.sp,
                                    color = DesignColors.TextMuted
                                )
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(call.timestamp.toDate()),
                                    fontSize = 12.sp,
                                    color = DesignColors.TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallStatItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp, color = color)
        Text(label, fontSize = 12.sp, color = DesignColors.TextMuted)
    }
}

fun formatDuration(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
