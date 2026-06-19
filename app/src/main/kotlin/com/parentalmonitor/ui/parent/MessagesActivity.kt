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
import com.parentalmonitor.data.model.SmsData
import com.parentalmonitor.data.model.WhatsAppData
import com.parentalmonitor.util.Constants
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.DeviceIdProvider
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MessagesActivity : ComponentActivity() {
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
                MessagesScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var smsMessages by remember { mutableStateOf<List<SmsData>>(emptyList()) }
    var whatsappMessages by remember { mutableStateOf<List<WhatsAppData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val deviceId = DeviceIdProvider.parentDeviceId(context) ?: return@LaunchedEffect
        try {
            val firestore = FirebaseFirestore.getInstance()
            val deviceRef = firestore.collection(Constants.COLLECTION_DEVICES).document(deviceId)

            val smsSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            smsMessages = smsSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { SmsData.fromMap(it, doc.id) }
            }

            val waSnapshot = deviceRef.collection(Constants.SUBCOLLECTION_WHATSAPP)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            whatsappMessages = waSnapshot.documents.mapNotNull { doc ->
                doc.data?.let { WhatsAppData.fromMap(it, doc.id) }
            }

            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💬 Pesan") },
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
            // Tab selector
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📱 SMS") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🟢 WhatsApp") }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> SmsList(smsMessages)
                    1 -> WhatsAppList(whatsappMessages)
                }
            }
        }
    }
}

@Composable
fun SmsList(messages: List<SmsData>) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Message, null, modifier = Modifier.size(64.dp), tint = DesignColors.TextMuted)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum ada data SMS", color = DesignColors.TextMuted)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { sms ->
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (sms.type == "inbox") DesignColors.Info.copy(alpha = 0.1f)
                                            else DesignColors.Success.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (sms.type == "inbox") Icons.Default.CallReceived else Icons.Default.CallMade,
                                        null,
                                        tint = if (sms.type == "inbox") DesignColors.Info else DesignColors.Success,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sms.sender, fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(sms.timestamp.toDate()),
                                fontSize = 12.sp,
                                color = DesignColors.TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(sms.content, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppList(messages: List<WhatsAppData>) {
    if (messages.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(64.dp), tint = DesignColors.TextMuted)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Belum ada data WhatsApp", color = DesignColors.TextMuted)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { wa ->
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(DesignColors.Success.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (wa.isGroup) Icons.Default.Group else Icons.Default.Person,
                                        null,
                                        tint = DesignColors.Success,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(wa.sender, fontWeight = FontWeight.SemiBold)
                                    if (wa.isGroup && wa.groupName.isNotEmpty()) {
                                        Text("Grup: ${wa.groupName}", fontSize = 12.sp, color = DesignColors.TextMuted)
                                    }
                                }
                            }
                            Text(
                                SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(wa.timestamp.toDate()),
                                fontSize = 12.sp,
                                color = DesignColors.TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(wa.content, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
