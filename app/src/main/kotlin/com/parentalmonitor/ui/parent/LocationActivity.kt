package com.parentalmonitor.ui.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parentalmonitor.data.model.LocationData
import com.parentalmonitor.util.Constants
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity : ComponentActivity() {
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
                LocationScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var locations by remember { mutableStateOf<List<LocationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val deviceId = DeviceIdProvider.parentDeviceId(context) ?: return@LaunchedEffect
        try {
            val firestore = FirebaseFirestore.getInstance()

            val snapshot = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .collection(Constants.SUBCOLLECTION_LOCATIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            locations = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { LocationData.fromMap(it, doc.id) }
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📍 Riwayat Lokasi") },
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
        } else if (locations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOff, null, modifier = Modifier.size(64.dp), tint = DesignColors.TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada data lokasi", color = DesignColors.TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Latest location card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Lokasi Terakhir", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            locations.firstOrNull()?.let { loc ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = DesignColors.Success)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Latitude: ${String.format("%.6f", loc.latitude)}")
                                        Text("Longitude: ${String.format("%.6f", loc.longitude)}")
                                        Text("Akurasi: ${String.format("%.1f", loc.accuracy)} meter")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Terakhir diupdate: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(loc.timestamp.toDate())}",
                                    color = DesignColors.TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Location history
                item {
                    Text("Riwayat Lokasi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(locations) { location ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = DesignColors.Success,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Akurasi: ${String.format("%.0f", location.accuracy)}m",
                                    fontSize = 12.sp,
                                    color = DesignColors.TextMuted
                                )
                            }
                            Text(
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(location.timestamp.toDate()),
                                color = DesignColors.TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
