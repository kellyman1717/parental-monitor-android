package com.parentalmonitor.ui.parent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.parentalmonitor.ui.theme.DesignColors
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parentalmonitor.data.model.*
import com.parentalmonitor.ui.auth.LoginActivity
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.tasks.await

class DashboardActivity : ComponentActivity() {

    private val pairLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newId = result.data?.getStringExtra("paired_device_id")
            if (!newId.isNullOrEmpty()) {
                DeviceIdProvider.saveParentDeviceId(this, newId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

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
                val deviceId = remember { DeviceIdProvider.parentDeviceId(this@DashboardActivity) }
                if (deviceId == null) {
                    NotPairedScreen(
                        onPair = { pairLauncher.launch(Intent(this@DashboardActivity, PairDeviceActivity::class.java)) },
                        onLogout = { signOut() }
                    )
                } else {
                    DashboardScreen(
                        deviceId = deviceId,
                        onUnpair = { unpair() },
                        onPairNew = { pairLauncher.launch(Intent(this@DashboardActivity, PairDeviceActivity::class.java)) }
                    )
                }
            }
        }
    }

    private fun unpair() {
        DeviceIdProvider.clearParentDeviceId(this)
        recreate()
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    deviceId: String,
    onUnpair: () -> Unit,
    onPairNew: () -> Unit
) {
    val context = LocalContext.current
    var latestLocation by remember { mutableStateOf<LocationData?>(null) }
    var recentSms by remember { mutableStateOf<List<SmsData>>(emptyList()) }
    var recentCalls by remember { mutableStateOf<List<CallData>>(emptyList()) }
    var recentWhatsApp by remember { mutableStateOf<List<WhatsAppData>>(emptyList()) }
    var deviceName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showUnpairDialog by remember { mutableStateOf(false) }

    // Load data from Firebase
    LaunchedEffect(deviceId) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val deviceRef = firestore.collection(Constants.COLLECTION_DEVICES).document(deviceId)

            val deviceDoc = deviceRef.get().await()
            deviceName = deviceDoc.getString("name")

            // Get latest location
            val locationSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_LOCATIONS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            latestLocation = locationSnapshot.documents.firstOrNull()?.data?.let {
                LocationData.fromMap(it)
            }

            // Get recent SMS
            val smsSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            recentSms = smsSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { SmsData.fromMap(it, doc.id) }
            }

            // Get recent calls
            val callSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_CALLS)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            recentCalls = callSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { CallData.fromMap(it, doc.id) }
            }

            // Get recent WhatsApp
            val waSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_WHATSAPP)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .await()
            recentWhatsApp = waSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { WhatsAppData.fromMap(it, doc.id) }
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    if (showUnpairDialog) {
        AlertDialog(
            onDismissRequest = { showUnpairDialog = false },
            title = { Text("Putuskan Pairing?") },
            text = { Text("Dashboard tidak akan menampilkan data lagi. Kamu bisa pairing ulang kapan saja.") },
            confirmButton = {
                TextButton(onClick = {
                    showUnpairDialog = false
                    onUnpair()
                }) {
                    Text("Putuskan", color = DesignColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnpairDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Parental Monitor")
                        deviceName?.let {
                            Text(
                                text = "📱 $it",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showUnpairDialog = true }) {
                        Icon(Icons.Default.LinkOff, "Unpair", tint = Color.White)
                    }
                    IconButton(onClick = onPairNew) {
                        Icon(Icons.Default.AddLink, "Pair new", tint = Color.White)
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                }
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
                // Location Card
                item {
                    DashboardCard(
                        icon = Icons.Default.LocationOn,
                        title = "📍 Lokasi Terakhir",
                        subtitle = latestLocation?.let {
                            "Lat: ${String.format("%.4f", it.latitude)}, Lng: ${String.format("%.4f", it.longitude)}"
                        } ?: "Belum ada data lokasi",
                        color = DesignColors.Success,
                        onClick = {
                            context.startActivity(Intent(context, LocationActivity::class.java))
                        }
                    )
                }

                // Quick Stats Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Message,
                            count = recentSms.size,
                            label = "SMS",
                            color = DesignColors.Info
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Call,
                            count = recentCalls.size,
                            label = "Panggilan",
                            color = DesignColors.Warning
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Chat,
                            count = recentWhatsApp.size,
                            label = "WhatsApp",
                            color = DesignColors.Success
                        )
                    }
                }

                // Recent Messages
                item {
                    SectionHeader(title = "💬 Pesan Terbaru") {
                        context.startActivity(Intent(context, MessagesActivity::class.java))
                    }
                }

                items(recentSms.size.coerceAtMost(3)) { index ->
                    val sms = recentSms[index]
                    MessageItem(
                        sender = sms.sender,
                        preview = sms.content.take(80),
                        time = sms.timestamp.toDate().toString().take(16),
                        type = sms.type
                    )
                }

                // Recent Calls
                item {
                    SectionHeader(title = "📞 Panggilan Terbaru") {
                        context.startActivity(Intent(context, CallsActivity::class.java))
                    }
                }

                items(recentCalls.size.coerceAtMost(3)) { index ->
                    val call = recentCalls[index]
                    CallItem(
                        number = call.contactName.ifEmpty { call.number },
                        type = call.type,
                        duration = "${call.duration}s",
                        time = call.timestamp.toDate().toString().take(16)
                    )
                }

                // Recent WhatsApp
                item {
                    SectionHeader(title = "🟢 WhatsApp Terbaru") {
                        context.startActivity(Intent(context, MessagesActivity::class.java))
                    }
                }

                items(recentWhatsApp.size.coerceAtMost(3)) { index ->
                    val wa = recentWhatsApp[index]
                    MessageItem(
                        sender = wa.sender,
                        preview = wa.content.take(80),
                        time = wa.timestamp.toDate().toString().take(16),
                        type = if (wa.isGroup) "group" else "personal"
                    )
                }

                // Menu Grid
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Menu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.LocationOn,
                            title = "Lokasi",
                            color = DesignColors.Success
                        ) {
                            context.startActivity(Intent(context, LocationActivity::class.java))
                        }
                        MenuCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Apps,
                            title = "Aplikasi",
                            color = DesignColors.Warning
                        ) {
                            context.startActivity(Intent(context, AppUsageActivity::class.java))
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Message,
                            title = "Pesan",
                            color = DesignColors.Info
                        ) {
                            context.startActivity(Intent(context, MessagesActivity::class.java))
                        }
                        MenuCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.BatteryChargingFull,
                            title = "Baterai",
                            color = DesignColors.Success
                        ) {
                            context.startActivity(Intent(context, BatteryUsageActivity::class.java))
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MenuCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Settings,
                            title = "Pengaturan",
                            color = Color(0xFF9E9E9E)
                        ) {
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun DashboardCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = DesignColors.TextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(count.toString(), fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text(label, color = DesignColors.TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        TextButton(onClick = onSeeAll) {
            Text("Lihat Semua")
        }
    }
}

@Composable
fun MessageItem(sender: String, preview: String, time: String, type: String) {
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
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (type) {
                            "inbox" -> DesignColors.Info.copy(alpha = 0.1f)
                            "sent" -> DesignColors.Success.copy(alpha = 0.1f)
                            else -> DesignColors.Warning.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (type) {
                        "inbox" -> Icons.Default.CallReceived
                        "sent" -> Icons.Default.CallMade
                        else -> Icons.Default.Group
                    },
                    contentDescription = null,
                    tint = when (type) {
                        "inbox" -> DesignColors.Info
                        "sent" -> DesignColors.Success
                        else -> DesignColors.Warning
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sender, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(preview, color = DesignColors.TextMuted, fontSize = 12.sp, maxLines = 1)
            }
            Text(time, color = DesignColors.TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun CallItem(number: String, type: String, duration: String, time: String) {
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
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (type) {
                            "missed" -> DesignColors.Danger.copy(alpha = 0.1f)
                            "incoming" -> DesignColors.Success.copy(alpha = 0.1f)
                            else -> DesignColors.Info.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (type) {
                        "missed" -> Icons.Default.CallMissed
                        "incoming" -> Icons.Default.CallReceived
                        else -> Icons.Default.CallMade
                    },
                    contentDescription = null,
                    tint = when (type) {
                        "missed" -> DesignColors.Danger
                        "incoming" -> DesignColors.Success
                        else -> DesignColors.Info
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(number, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("$duration • $type", color = DesignColors.TextMuted, fontSize = 12.sp)
            }
            Text(time, color = DesignColors.TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun MenuCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotPairedScreen(
    onPair: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Monitor") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignColors.Primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = DesignColors.Primary,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Belum Ada HP Anak Terhubung",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Pairing dulu untuk mulai memantau HP anak. Kode pairing ada di HP anak setelah setup.",
                color = DesignColors.TextMuted,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onPair,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddLink, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pair HP Anak", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📋 Langkah pairing:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Di HP anak, buka dialer & ketik *#*#1234#*#*", fontSize = 13.sp)
                    Text("2. Beri semua permission yang diminta", fontSize = 13.sp)
                    Text("3. Klik 'Mulai Monitoring' di HP anak", fontSize = 13.sp)
                    Text("4. Kode 6-digit akan muncul di HP anak", fontSize = 13.sp)
                    Text("5. Masukkan kode di tombol 'Pair HP Anak' di atas", fontSize = 13.sp)
                }
            }
        }
    }
}
