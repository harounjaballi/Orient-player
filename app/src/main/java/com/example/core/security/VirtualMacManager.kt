package com.example.core.security

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

data class DeviceIdentity(
    val installationId: String,
    val virtualMac: String,
    val activationCode: String,
    val deviceKey: String,
    val securityToken: String,
    val deviceModel: String,
    val deviceType: String
)

object VirtualMacManager {
    private const val PREFS_NAME = "orient_player_secure_identity"
    private const val KEY_INSTALLATION_ID = "installation_id"
    private const val KEY_VIRTUAL_MAC = "virtual_mac"
    private const val KEY_ACTIVATION_CODE = "activation_code"
    private const val KEY_DEVICE_KEY = "device_key"
    private const val HMAC_SECRET = "ORIENT_PLAYER_IPTV_HMAC_2026_SECURE_SALT"

    fun getOrCreateDeviceIdentity(context: Context): DeviceIdentity {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var installationId = prefs.getString(KEY_INSTALLATION_ID, null)
        var virtualMac = prefs.getString(KEY_VIRTUAL_MAC, null)
        var activationCode = prefs.getString(KEY_ACTIVATION_CODE, null)
        var deviceKey = prefs.getString(KEY_DEVICE_KEY, null)

        if (installationId == null || virtualMac == null || activationCode == null || deviceKey == null) {
            val androidId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
            } catch (_: Exception) {
                ""
            }
            val seed = if (androidId.length >= 8 && androidId != "9774d56d682e549c") {
                "orient-$androidId-${Build.BOARD}-${Build.MODEL}"
            } else {
                "orient-${UUID.randomUUID()}"
            }

            val hash = sha256Bytes(seed)
            installationId = UUID.nameUUIDFromBytes(seed.toByteArray()).toString().uppercase(Locale.ROOT)

            // Use 00:1A:79 prefix (standard IPTV / Stalker Portal compatible virtual MAC OUI)
            val b3 = String.format(Locale.ROOT, "%02X", hash[0].toInt() and 0xFF)
            val b4 = String.format(Locale.ROOT, "%02X", hash[1].toInt() and 0xFF)
            val b5 = String.format(Locale.ROOT, "%02X", hash[2].toInt() and 0xFF)
            virtualMac = "00:1A:79:$b3:$b4:$b5"

            // 6-digit numerical activation code & 8-char device key
            val codeInt = ((hash[3].toInt() and 0xFF) shl 16) or
                ((hash[4].toInt() and 0xFF) shl 8) or
                (hash[5].toInt() and 0xFF)
            val sixDigits = (codeInt % 900000) + 100000
            activationCode = "${sixDigits / 1000}-${String.format(Locale.ROOT, "%03d", sixDigits % 1000)}"

            val keyInt = ((hash[6].toInt() and 0xFF) shl 16) or
                ((hash[7].toInt() and 0xFF) shl 8) or
                (hash[8].toInt() and 0xFF)
            deviceKey = String.format(Locale.ROOT, "%06d", (keyInt % 900000) + 100000)

            prefs.edit()
                .putString(KEY_INSTALLATION_ID, installationId)
                .putString(KEY_VIRTUAL_MAC, virtualMac)
                .putString(KEY_ACTIVATION_CODE, activationCode)
                .putString(KEY_DEVICE_KEY, deviceKey)
                .apply()
        }

        val securityToken = computeHmacToken(virtualMac, activationCode, installationId)
        val deviceType = detectDeviceType(context)
        val deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

        return DeviceIdentity(
            installationId = installationId,
            virtualMac = virtualMac,
            activationCode = activationCode,
            deviceKey = deviceKey,
            securityToken = securityToken,
            deviceModel = deviceModel,
            deviceType = deviceType
        )
    }

    fun regenerateIdentity(context: Context): DeviceIdentity {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val randomSeed = "orient-reset-${UUID.randomUUID()}-${System.currentTimeMillis()}"
        val hash = sha256Bytes(randomSeed)
        val installationId = UUID.randomUUID().toString().uppercase(Locale.ROOT)
        val b3 = String.format(Locale.ROOT, "%02X", hash[0].toInt() and 0xFF)
        val b4 = String.format(Locale.ROOT, "%02X", hash[1].toInt() and 0xFF)
        val b5 = String.format(Locale.ROOT, "%02X", hash[2].toInt() and 0xFF)
        val virtualMac = "00:1A:79:$b3:$b4:$b5"

        val codeInt = ((hash[3].toInt() and 0xFF) shl 16) or
            ((hash[4].toInt() and 0xFF) shl 8) or
            (hash[5].toInt() and 0xFF)
        val sixDigits = (codeInt % 900000) + 100000
        val activationCode = "${sixDigits / 1000}-${String.format(Locale.ROOT, "%03d", sixDigits % 1000)}"
        val deviceKey = String.format(Locale.ROOT, "%06d", ((hash[6].toInt() and 0xFF) * 3137) % 900000 + 100000)

        prefs.edit()
            .putString(KEY_INSTALLATION_ID, installationId)
            .putString(KEY_VIRTUAL_MAC, virtualMac)
            .putString(KEY_ACTIVATION_CODE, activationCode)
            .putString(KEY_DEVICE_KEY, deviceKey)
            .apply()

        return getOrCreateDeviceIdentity(context)
    }

    fun computeHmacToken(mac: String, code: String, installId: String): String {
        return try {
            val macAlgo = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(HMAC_SECRET.toByteArray(Charsets.UTF_8), "HmacSHA256")
            macAlgo.init(secretKey)
            val digest = macAlgo.doFinal("$mac|$code|$installId".toByteArray(Charsets.UTF_8))
            digest.take(12).joinToString("") { String.format(Locale.ROOT, "%02x", it) }
        } catch (_: Exception) {
            "tok_${mac.replace(":", "").lowercase(Locale.ROOT)}"
        }
    }

    private fun detectDeviceType(context: Context): String {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return if (uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
            "Android TV / Box"
        } else {
            val screenLayout = context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
            if (screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                "Android Box / Tablette"
            } else {
                "Android Smartphone"
            }
        }
    }

    private fun sha256Bytes(input: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
    }
}
