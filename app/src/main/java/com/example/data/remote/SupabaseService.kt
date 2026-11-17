package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.ActivationLogEntity
import com.example.data.local.DeviceEntity
import com.example.data.local.PlaylistConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SupabaseSyncResult(
    val isCloudConnected: Boolean,
    val statusMessage: String,
    val syncedDevicesCount: Int = 0,
    val syncedPlaylistsCount: Int = 0
)

object SupabaseService {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim().trimEnd('/')

    val supabaseAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    fun isConfigured(): Boolean {
        return supabaseUrl.startsWith("https://") &&
            !supabaseUrl.contains("your-project-id") &&
            supabaseAnonKey.length > 20 &&
            supabaseAnonKey != "your-supabase-anon-key"
    }

    private fun Request.Builder.addSupabaseHeaders(prefer: String? = null): Request.Builder {
        header("apikey", supabaseAnonKey)
        header("Authorization", "Bearer $supabaseAnonKey")
        header("Accept", "application/json")
        if (prefer != null) {
            header("Prefer", prefer)
        }
        return this
    }

    suspend fun upsertDeviceToCloud(device: DeviceEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val payload = JSONObject().apply {
                put("virtual_mac", device.virtualMac)
                put("installation_id", device.installationId)
                put("activation_code", device.activationCode)
                put("device_key", device.deviceKey)
                put("client_name", device.clientName)
                put("device_model", device.deviceModel)
                put("device_type", device.deviceType)
                put("status", device.status)
                put("subscription_plan", device.subscriptionPlan)
                put("start_date_millis", device.startDateMillis)
                put("expiration_date_millis", device.expirationDateMillis)
                put("last_connection_millis", device.lastConnectionMillis)
                put("security_token", device.securityToken)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/devices?on_conflict=virtual_mac")
                .addSupabaseHeaders(prefer = "resolution=merge-duplicates,return=minimal")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun fetchDevicesFromCloud(): List<DeviceEntity>? = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext null
        try {
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/devices?select=*&order=last_connection_millis.desc")
                .addSupabaseHeaders()
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val arr = JSONArray(bodyStr)
                val list = mutableListOf<DeviceEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DeviceEntity(
                            virtualMac = obj.optString("virtual_mac"),
                            installationId = obj.optString("installation_id"),
                            activationCode = obj.optString("activation_code"),
                            deviceKey = obj.optString("device_key"),
                            clientName = obj.optString("client_name", "Appareil Orient"),
                            deviceModel = obj.optString("device_model", "Android"),
                            deviceType = obj.optString("device_type", "Android TV / Box"),
                            status = obj.optString("status", "ACTIVE"),
                            subscriptionPlan = obj.optString("subscription_plan", "MONTH_12"),
                            startDateMillis = obj.optLong("start_date_millis", System.currentTimeMillis()),
                            expirationDateMillis = obj.optLong("expiration_date_millis", System.currentTimeMillis() + 31536000000L),
                            lastConnectionMillis = obj.optLong("last_connection_millis", System.currentTimeMillis()),
                            securityToken = obj.optString("security_token")
                        )
                    )
                }
                list
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchDeviceByMacFromCloud(mac: String): DeviceEntity? = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext null
        try {
            val encodedMac = URLEncoder.encode(mac, "UTF-8")
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/devices?virtual_mac=eq.$encodedMac&select=*&limit=1")
                .addSupabaseHeaders()
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyStr = response.body?.string() ?: return@withContext null
                val arr = JSONArray(bodyStr)
                if (arr.length() == 0) return@withContext null
                val obj = arr.getJSONObject(0)
                DeviceEntity(
                    virtualMac = obj.optString("virtual_mac"),
                    installationId = obj.optString("installation_id"),
                    activationCode = obj.optString("activation_code"),
                    deviceKey = obj.optString("device_key"),
                    clientName = obj.optString("client_name", "Appareil Orient"),
                    deviceModel = obj.optString("device_model", "Android"),
                    deviceType = obj.optString("device_type", "Android TV / Box"),
                    status = obj.optString("status", "ACTIVE"),
                    subscriptionPlan = obj.optString("subscription_plan", "MONTH_12"),
                    startDateMillis = obj.optLong("start_date_millis", System.currentTimeMillis()),
                    expirationDateMillis = obj.optLong("expiration_date_millis", System.currentTimeMillis()),
                    lastConnectionMillis = obj.optLong("last_connection_millis", System.currentTimeMillis()),
                    securityToken = obj.optString("security_token")
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun upsertPlaylistToCloud(playlist: PlaylistConfigEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val payload = JSONObject().apply {
                put("device_mac", playlist.deviceMac)
                put("name", playlist.name)
                put("protocol", playlist.protocol)
                put("server_url", playlist.serverUrl)
                put("username", playlist.username)
                put("password", playlist.password)
                put("stalker_mac", playlist.stalkerMac)
                put("auto_update_enabled", playlist.autoUpdateEnabled)
                put("is_active", playlist.isActive)
                put("channel_count", playlist.channelCount)
                put("vod_count", playlist.vodCount)
                put("series_count", playlist.seriesCount)
                put("last_updated_millis", playlist.lastUpdatedMillis)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/playlists")
                .addSupabaseHeaders(prefer = "return=minimal")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun insertLogToCloud(log: ActivationLogEntity): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext false
        try {
            val payload = JSONObject().apply {
                put("device_mac", log.deviceMac)
                put("action_type", log.actionType)
                put("description", log.description)
                put("performed_by", log.performedBy)
                put("timestamp_millis", log.timestampMillis)
            }
            val request = Request.Builder()
                .url("$supabaseUrl/rest/v1/activation_logs")
                .addSupabaseHeaders(prefer = "return=minimal")
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    const val SUPABASE_SQL_SCHEMA = """
-- ============================================================
-- ORIENT PLAYER IPTV - SCHÉMA POSTGRESQL SUPABASE COMPLET
-- Exécutez ce script dans l'éditeur SQL de votre projet Supabase
-- ============================================================

CREATE TABLE IF NOT EXISTS public.devices (
    virtual_mac TEXT PRIMARY KEY,
    installation_id TEXT NOT NULL,
    activation_code TEXT NOT NULL,
    device_key TEXT NOT NULL,
    client_name TEXT DEFAULT 'Appareil Orient',
    device_model TEXT DEFAULT 'Android TV',
    device_type TEXT DEFAULT 'Android TV / Box',
    status TEXT DEFAULT 'ACTIVE',
    subscription_plan TEXT DEFAULT 'MONTH_12',
    start_date_millis BIGINT NOT NULL,
    expiration_date_millis BIGINT NOT NULL,
    last_connection_millis BIGINT NOT NULL,
    security_token TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.playlists (
    id BIGSERIAL PRIMARY KEY,
    device_mac TEXT NOT NULL REFERENCES public.devices(virtual_mac) ON DELETE CASCADE,
    name TEXT NOT NULL,
    protocol TEXT NOT NULL, -- M3U, XTREAM, STALKER
    server_url TEXT NOT NULL,
    username TEXT DEFAULT '',
    password TEXT DEFAULT '',
    stalker_mac TEXT DEFAULT '',
    auto_update_enabled BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    channel_count INTEGER DEFAULT 0,
    vod_count INTEGER DEFAULT 0,
    series_count INTEGER DEFAULT 0,
    last_updated_millis BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.activation_logs (
    id BIGSERIAL PRIMARY KEY,
    device_mac TEXT NOT NULL,
    action_type TEXT NOT NULL,
    description TEXT NOT NULL,
    performed_by TEXT NOT NULL,
    timestamp_millis BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_playlists_mac ON public.playlists(device_mac);
CREATE INDEX IF NOT EXISTS idx_logs_mac ON public.activation_logs(device_mac);

ALTER TABLE public.devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.playlists ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.activation_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public API access to devices"
    ON public.devices FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public API access to playlists"
    ON public.playlists FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public API access to activation_logs"
    ON public.activation_logs FOR ALL USING (true) WITH CHECK (true);
"""
}
