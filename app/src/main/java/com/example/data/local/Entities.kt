package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceStatus(val label: String) {
    ACTIVE("Actif"),
    EXPIRED("Expiré"),
    BLOCKED("Bloqué"),
    PENDING("En attente")
}

enum class SubscriptionPlan(val label: String, val durationDays: Int) {
    PERMANENT("Activation Permanente", 36500),
    MONTH_1("Abonnement 1 Mois", 30),
    MONTH_3("Abonnement 3 Mois", 90),
    MONTH_6("Abonnement 6 Mois", 180),
    MONTH_12("Abonnement 12 Mois", 365),
    TRIAL_7D("Essai 7 Jours", 7)
}

enum class PlaylistProtocol(val label: String) {
    M3U("M3U / M3U8"),
    XTREAM("Xtream Codes API"),
    STALKER("Stalker Portal / Ministra")
}

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val virtualMac: String,
    val installationId: String,
    val activationCode: String,
    val deviceKey: String,
    val clientName: String,
    val deviceModel: String,
    val deviceType: String,
    val status: String, // ACTIVE, EXPIRED, BLOCKED
    val subscriptionPlan: String, // PERMANENT, MONTH_1, MONTH_3, MONTH_6, MONTH_12
    val startDateMillis: Long,
    val expirationDateMillis: Long,
    val lastConnectionMillis: Long,
    val securityToken: String
)

@Entity(tableName = "playlists")
data class PlaylistConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceMac: String,
    val name: String,
    val protocol: String, // M3U, XTREAM, STALKER
    val serverUrl: String,
    val username: String = "",
    val password: String = "",
    val stalkerMac: String = "",
    val autoUpdateEnabled: Boolean = true,
    val isActive: Boolean = true,
    val channelCount: Int = 0,
    val vodCount: Int = 0,
    val seriesCount: Int = 0,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_channels")
data class LiveChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long = 1,
    val channelNumber: Int,
    val name: String,
    val category: String,
    val logoUrl: String,
    val streamUrl: String,
    val streamFormat: String = "HLS", // HLS, MPEG-TS, MP4
    val epgChannelId: String = "",
    val currentProgramTitle: String,
    val currentProgramDesc: String,
    val currentProgramStart: String,
    val currentProgramEnd: String,
    val currentProgramProgress: Float,
    val nextProgramTitle: String,
    val nextProgramTime: String,
    val hasCatchUp: Boolean = false,
    val catchUpDays: Int = 0,
    val isAdult: Boolean = false,
    val isFavorite: Boolean = false,
    val lastWatchedMillis: Long = 0L
)

@Entity(tableName = "vod_movies")
data class VodMovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long = 1,
    val title: String,
    val category: String,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val year: Int,
    val genre: String,
    val rating: Double,
    val durationMinutes: Int,
    val director: String,
    val cast: String,
    val description: String,
    val isFavorite: Boolean = false,
    val resumePositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val lastWatchedMillis: Long = 0L
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: Long = 1,
    val title: String,
    val category: String,
    val posterUrl: String,
    val backdropUrl: String,
    val year: Int,
    val genre: String,
    val rating: Double,
    val seasonsCount: Int,
    val description: String,
    val isFavorite: Boolean = false,
    val lastWatchedEpisodeId: Long = 0L,
    val lastWatchedMillis: Long = 0L
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String,
    val overview: String,
    val durationMinutes: Int,
    val thumbnailUrl: String,
    val streamUrl: String,
    val resumePositionMs: Long = 0L,
    val totalDurationMs: Long = 0L
)

@Entity(tableName = "catchup_programs")
data class CatchUpProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long,
    val channelName: String,
    val channelLogo: String,
    val category: String,
    val title: String,
    val description: String,
    val dayLabel: String, // "Aujourd'hui", "Hier", "Il y a 2 jours"
    val timeSlot: String,
    val durationMinutes: Int,
    val streamUrl: String
)

@Entity(tableName = "activation_logs")
data class ActivationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceMac: String,
    val actionType: String, // ACTIVATION, RENEWAL, BLOCK, PLAYLIST_SYNC, CONNEXION
    val description: String,
    val performedBy: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
