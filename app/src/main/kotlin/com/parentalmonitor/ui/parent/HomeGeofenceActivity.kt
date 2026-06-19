package com.parentalmonitor.ui.parent

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.parentalmonitor.ui.theme.DesignColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Timestamp
import com.parentalmonitor.data.model.GeofenceStatus
import com.parentalmonitor.data.model.HomeGeofence
import com.parentalmonitor.data.repository.FirebaseRepository
import com.parentalmonitor.service.GeofenceService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class HomeGeofenceActivity : ComponentActivity() {

    @Inject
    lateinit var firebaseRepository: FirebaseRepository

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Permission lokasi diperlukan", Toast.LENGTH_LONG).show()
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
                HomeGeofenceScreen(
                    onBack = { finish() },
                    onRequestLocation = { requestLocationPermission() },
                    onSaveHome = { lat, lng, radius, address ->
                        saveHomeLocation(lat, lng, radius, address)
                    }
                )
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }

        val fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(this, "Lokasi: ${location.latitude}, ${location.longitude}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveHomeLocation(lat: Double, lng: Double, radius: Float, address: String) {
        val geofence = HomeGeofence(
            latitude = lat,
            longitude = lng,
            radius = radius,
            isSet = true,
            address = address,
            updatedAt = Timestamp.now()
        )

        // Save local
        val localGeofence = GeofenceService(this)
        localGeofence.setHomeLocation(lat, lng, radius, address)

        // Save ke Firebase
        lifecycleScope.launch {
            firebaseRepository.uploadHomeGeofence(geofence)
        }

        Toast.makeText(this, "✅ Lokasi rumah tersimpan!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGeofenceScreen(
    onBack: () -> Unit,
    onRequestLocation: () -> Unit,
    onSaveHome: (Double, Double, Float, String) -> Unit
) {
    val context = LocalContext.current
    val geofenceService = remember { GeofenceService(context) }
    val existingGeofence = remember { geofenceService.getHomeGeofence() }

    var latitude by remember { mutableStateOf(existingGeofence.latitude.toString()) }
    var longitude by remember { mutableStateOf(existingGeofence.longitude.toString()) }
    var radius by remember { mutableStateOf(existingGeofence.radius) }
    var address by remember { mutableStateOf(existingGeofence.address) }
    var isGettingLocation by remember { mutableStateOf(false) }
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏠 Setup Rumah") },
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
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DesignColors.SurfaceAlt)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ℹ️ Cara Kerja", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Set lokasi rumah & radius (dalam meter)")
                    Text("• Jika HP keluar dari radius → bunyi beep 3x berulang")
                    Text("• Bunyi berhenti otomatis saat HP masuk kembali ke rumah")
                }
            }

            // Get current location button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📍 Dapatkan Lokasi Saat Ini", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                isGettingLocation = true
                                fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                    .addOnSuccessListener { location ->
                                        isGettingLocation = false
                                        location?.let {
                                            latitude = it.latitude.toString()
                                            longitude = it.longitude.toString()
                                            Toast.makeText(context, "✅ Lokasi didapat", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .addOnFailureListener {
                                        isGettingLocation = false
                                        Toast.makeText(context, "Gagal dapat lokasi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                onRequestLocation()
                            }
                        },
                        enabled = !isGettingLocation,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGettingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mengambil lokasi...")
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gunakan Lokasi Saat Ini")
                        }
                    }
                }
            }

            // Manual input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✏️ Input Manual", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Radius slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📏 Radius: ${radius.toInt()} meter", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("HP dianggap 'di rumah' jika dalam radius ini", fontSize = 12.sp, color = DesignColors.TextMuted)
                    Slider(
                        value = radius,
                        onValueChange = { radius = it },
                        valueRange = 50f..1000f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50m", fontSize = 12.sp, color = DesignColors.TextMuted)
                        Text("500m", fontSize = 12.sp, color = DesignColors.TextMuted)
                        Text("1000m", fontSize = 12.sp, color = DesignColors.TextMuted)
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    val lat = latitude.toDoubleOrNull()
                    val lng = longitude.toDoubleOrNull()
                    if (lat == null || lng == null) {
                        Toast.makeText(context, "Latitude/Longitude tidak valid", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onSaveHome(lat, lng, radius, address)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Lokasi Rumah", fontSize = 16.sp)
            }

            // Clear button (if set)
            if (existingGeofence.isSet) {
                OutlinedButton(
                    onClick = {
                        geofenceService.clearHomeLocation()
                        Toast.makeText(context, "Lokasi rumah dihapus", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hapus Lokasi Rumah")
                }
            }

            // Status info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (GeofenceService.currentStatus) {
                        GeofenceStatus.INSIDE -> DesignColors.Success.copy(alpha = 0.12f)
                        GeofenceStatus.OUTSIDE -> DesignColors.Danger.copy(alpha = 0.12f)
                        GeofenceStatus.UNKNOWN -> DesignColors.Warning.copy(alpha = 0.12f)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Status Saat Ini", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        when (GeofenceService.currentStatus) {
                            GeofenceStatus.INSIDE -> "✅ Di rumah"
                            GeofenceStatus.OUTSIDE -> "🚨 Di luar rumah (ALARM AKTIF)"
                            GeofenceStatus.UNKNOWN -> "❓ Belum ada data"
                        },
                        color = when (GeofenceService.currentStatus) {
                            GeofenceStatus.INSIDE -> DesignColors.Success
                            GeofenceStatus.OUTSIDE -> DesignColors.Danger
                            GeofenceStatus.UNKNOWN -> DesignColors.Warning
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
