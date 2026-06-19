package com.parentalmonitor.ui.child

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.Constants
import kotlinx.coroutines.delay

class PairingWaitActivity : ComponentActivity() {

    private var pairingCode: String = ""
    private var deviceId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pairingCode = intent.getStringExtra("pairing_code") ?: ""
        deviceId = intent.getStringExtra("device_id") ?: ""

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
                PairingWaitScreen(
                    code = pairingCode,
                    onCheckStatus = { checkPairingStatus() }
                )
            }
        }

        if (pairingCode.isNotEmpty()) {
            checkPairingStatus()
        }
    }

    private fun checkPairingStatus() {
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_PAIRING_CODES)
            .document(pairingCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val parentUid = snapshot.getString("parentUid")
                if (!parentUid.isNullOrEmpty()) {
                    val deviceRef = db.collection(Constants.COLLECTION_DEVICES).document(deviceId)
                    deviceRef.update(
                        mapOf(
                            "pairedAt" to Timestamp.now(),
                            "parentUid" to parentUid,
                            "isActive" to true
                        )
                    )
                    getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)
                        .edit()
                        .putBoolean(Constants.PREF_IS_PAIRED, true)
                        .apply()
                    Toast.makeText(this, "Berhasil terhubung dengan HP orang tua!", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
    }
}

@Composable
fun PairingWaitScreen(
    code: String,
    onCheckStatus: () -> Unit
) {
    var secondsLeft by remember { mutableStateOf(600) }
    var paired by remember { mutableStateOf(false) }

    LaunchedEffect(code) {
        while (secondsLeft > 0 && !paired) {
            delay(1000)
            secondsLeft--
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (paired) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (paired) "Terhubung!" else "Menunggu Pairing",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!paired) {
                Text(
                    text = "Masukkan kode ini di HP orang tua:",
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = code,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        color = DesignColors.Primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = formatCountdown(secondsLeft),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kode berlaku 10 menit",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "Monitoring akan berjalan di background. Aplikasi ini akan tersembunyi dari launcher.",
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun formatCountdown(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}