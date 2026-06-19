package com.parentalmonitor.ui.parent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parentalmonitor.util.Constants
import com.parentalmonitor.util.DeviceIdProvider
import com.parentalmonitor.ui.theme.DesignColors

class PairDeviceActivity : ComponentActivity() {

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
                PairDeviceScreen(
                    onSubmit = { code -> submitCode(code) },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun submitCode(code: String) {
        if (code.length != 6) {
            Toast.makeText(this, "Kode harus 6 digit", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val parentUid = auth.currentUser?.uid
        if (parentUid == null) {
            Toast.makeText(this, "Sesi login habis, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection(Constants.COLLECTION_PAIRING_CODES)
            .document(code)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Kode tidak ditemukan atau sudah kadaluarsa", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val expiresAt = doc.getTimestamp("expiresAt")
                val now = Timestamp.now()
                if (expiresAt != null && expiresAt.seconds < now.seconds) {
                    Toast.makeText(this, "Kode sudah kadaluarsa, minta kode baru di HP anak", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val existingParentUid = doc.getString("parentUid")
                if (!existingParentUid.isNullOrEmpty() && existingParentUid != parentUid) {
                    Toast.makeText(this, "Kode sudah dipakai HP orang tua lain", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val deviceId = doc.getString("deviceId")
                val deviceName = doc.getString("deviceName")
                if (deviceId.isNullOrEmpty()) {
                    Toast.makeText(this, "Data pairing rusak, coba lagi", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection(Constants.COLLECTION_PAIRING_CODES)
                    .document(code)
                    .update(
                        mapOf(
                            "parentUid" to parentUid,
                            "pairedAt" to now
                        )
                    )
                    .addOnSuccessListener {
                        DeviceIdProvider.saveParentDeviceId(this, deviceId)

                        val deviceRef = db.collection(Constants.COLLECTION_DEVICES).document(deviceId)
                        deviceRef.update(
                            mapOf(
                                "parentUid" to parentUid,
                                "pairedAt" to now,
                                "isActive" to true,
                                "parentEmail" to (auth.currentUser?.email ?: "")
                            )
                        )

                        Toast.makeText(
                            this,
                            "Berhasil terhubung dengan ${deviceName ?: "HP anak"}",
                            Toast.LENGTH_LONG
                        ).show()
                        val resultIntent = Intent().apply {
                            putExtra("paired_device_id", deviceId)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal pairing: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal cek kode: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairDeviceScreen(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pairing Device") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Batal", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignColors.Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = DesignColors.Primary,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hubungkan HP Anak",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masukkan kode 6-digit yang muncul di HP anak",
                color = DesignColors.TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) code = it },
                label = { Text("Kode Pairing") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isLoading = true
                    onSubmit(code)
                },
                enabled = code.length == 6 && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Hubungkan", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📱 Cara pairing:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("1. Buka app ini di HP anak (via dialer *#*#1234#*#*)", fontSize = 13.sp)
                    Text("2. Klik Mulai Monitoring", fontSize = 13.sp)
                    Text("3. Kode 6-digit akan muncul di HP anak", fontSize = 13.sp)
                    Text("4. Masukkan kode di sini", fontSize = 13.sp)
                }
            }
        }
    }
}