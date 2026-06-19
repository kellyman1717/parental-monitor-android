package com.parentalmonitor.ui.child

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parentalmonitor.ui.theme.DesignColors
import com.parentalmonitor.util.PermissionHelper

class StatusActivity : ComponentActivity() {
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
                StatusScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status Monitoring") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DesignColors.Success.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Shield,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = DesignColors.Success
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Monitoring Aktif", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("HP ini sedang dipantau oleh orang tua", color = DesignColors.TextMuted)
                }
            }
        }
    }
}
