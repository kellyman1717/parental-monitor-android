package com.parentalmonitor.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.parentalmonitor.ui.parent.DashboardActivity
import com.parentalmonitor.ui.theme.DesignColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If already logged in, go to dashboard
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
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
                LoginScreen(
                    onLoginSuccess = {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Title
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Parental Monitor",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isRegisterMode) "Buat Akun Orang Tua" else "Login Orang Tua",
                fontSize = 16.sp,
                color = DesignColors.TextMuted
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login/Register button
            Button(
                onClick = {
                    if (email.isBlank() || password.length < 6) {
                        Toast.makeText(
                            context,
                            "Email tidak boleh kosong dan password minimal 6 karakter",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        try {
                            if (isRegisterMode) {
                                auth.createUserWithEmailAndPassword(email, password).await()
                            } else {
                                auth.signInWithEmailAndPassword(email, password).await()
                            }
                            onLoginSuccess()
                        } catch (e: Exception) {
                            Log.e("LoginActivity", "Auth failed (mode=${if (isRegisterMode) "register" else "login"})", e)
                            val message = friendlyAuthError(e)
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && email.isNotBlank() && password.length >= 6,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        if (isRegisterMode) "Daftar" else "Login",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle register/login
            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                Text(
                    if (isRegisterMode) "Sudah punya akun? Login" else "Belum punya akun? Daftar",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Maps raw Firebase Auth exception messages to something a parent can actually read.
// Falls back to the raw message when we don't recognize the error code.
private fun friendlyAuthError(e: Exception): String {
    val raw = e.message ?: "Terjadi kesalahan yang tidak diketahui"
    return when {
        raw.contains("API key not valid", ignoreCase = true) ->
            "Konfigurasi Firebase belum valid. Hubungi developer."
        raw.contains("The email address is already in use", ignoreCase = true) ->
            "Email ini sudah terdaftar. Silakan login atau pakai email lain."
        raw.contains("The email address is badly formatted", ignoreCase = true) ->
            "Format email tidak valid."
        raw.contains("Password should be at least 6 characters", ignoreCase = true) ->
            "Password minimal 6 karakter."
        raw.contains("There is no user record corresponding to this identifier", ignoreCase = true) ||
        raw.contains("The supplied auth credential is incorrect", ignoreCase = true) ||
        raw.contains("invalid credential", ignoreCase = true) ->
            "Email atau password salah."
        raw.contains("The user account has been disabled", ignoreCase = true) ->
            "Akun ini dinonaktifkan."
        raw.contains("network error", ignoreCase = true) ||
        raw.contains("unable to resolve host", ignoreCase = true) ||
        raw.contains("timeout", ignoreCase = true) ->
            "Tidak ada koneksi internet. Coba lagi."
        else -> raw
    }
}
