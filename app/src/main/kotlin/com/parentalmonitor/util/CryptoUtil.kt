package com.parentalmonitor.util

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object CryptoUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"

    private fun generateKey(): SecretKeySpec {
        // In production, use a more secure key management
        val key = "ParentalMonitor2024!SecretKey#1234"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(key.toByteArray())
        return SecretKeySpec(hash.copyOf(16), ALGORITHM)
    }

    fun encrypt(data: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, generateKey())
            val encrypted = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            data // fallback to plain text on error
        }
    }

    fun decrypt(encryptedData: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, generateKey())
            val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
            String(cipher.doFinal(decoded))
        } catch (e: Exception) {
            encryptedData // fallback on error
        }
    }
}
