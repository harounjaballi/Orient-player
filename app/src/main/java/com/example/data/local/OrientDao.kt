package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OrientDao {
    // --- DEVICES & SUBSCRIPTIONS ---
    @Query("SELECT * FROM devices ORDER BY lastConnectionMillis DESC")
    fun observeAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE virtualMac = :mac LIMIT 1")
    fun observeDeviceByMac(mac: String): Flow<DeviceEntity?>

    @Query("SELECT * FROM devices WHERE virtualMac = :mac LIMIT 1")
    suspend fun getDeviceByMac(mac: String): DeviceEntity?

    @Query("SELECT COUNT(*) FROM devices")
    suspend fun getDeviceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDevices(devices: List<DeviceEntity>)

    @Query("DELETE FROM devices WHERE virtualMac = :mac")
    suspend fun deleteDevice(mac: String)

    // --- PLAYLISTS ---
    @Query("SELECT * FROM playlists ORDER BY lastUpdatedMillis DESC")
    fun observeAllPlaylists(): Flow<List<PlaylistConfigEntity>>

    @Query("SELECT * FROM playlists WHERE deviceMac = :mac ORDER BY lastUpdatedMillis DESC")
    fun observePlaylistsForMac(mac: String): Flow<List<PlaylistConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistConfigEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistConfigEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    // --- LIVE CHANNELS ---
    @Query("SELECT * FROM live_channels ORDER BY channelNumber ASC")
    fun observeAllChannels(): Flow<List<LiveChannelEntity>>

    @Query("SELECT COUNT(*) FROM live_channels")
    suspend fun getChannelCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<LiveChannelEntity>)

    @Query("UPDATE live_channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun setChannelFavorite(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE live_channels SET lastWatchedMillis = :timestamp WHERE id = :channelId")
    suspend fun markChannelWatched(channelId: Long, timestamp: Long)

    @Query("DELETE FROM live_channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    // --- VOD MOVIES ---
    @Query("SELECT * FROM vod_movies ORDER BY year DESC, rating DESC")
    fun observeAllMovies(): Flow<List<VodMovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<VodMovieEntity>)

    @Query("UPDATE vod_movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun setMovieFavorite(movieId: Long, isFavorite: Boolean)

    @Query("UPDATE vod_movies SET resumePositionMs = :positionMs, totalDurationMs = :durationMs, lastWatchedMillis = :timestamp WHERE id = :movieId")
    suspend fun updateMovieProgress(movieId: Long, positionMs: Long, durationMs: Long, timestamp: Long)

    // --- SERIES & EPISODES ---
    @Query("SELECT * FROM series ORDER BY rating DESC")
    fun observeAllSeries(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM episodes WHERE seriesId = :seriesId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun observeEpisodesForSeries(seriesId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes")
    fun observeAllEpisodes(): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesList(series: List<SeriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("UPDATE series SET isFavorite = :isFavorite WHERE id = :seriesId")
    suspend fun setSeriesFavorite(seriesId: Long, isFavorite: Boolean)

    @Query("UPDATE episodes SET resumePositionMs = :positionMs, totalDurationMs = :durationMs WHERE id = :episodeId")
    suspend fun updateEpisodeProgress(episodeId: Long, positionMs: Long, durationMs: Long)

    @Query("UPDATE series SET lastWatchedEpisodeId = :episodeId, lastWatchedMillis = :timestamp WHERE id = :seriesId")
    suspend fun updateSeriesLastWatched(seriesId: Long, episodeId: Long, timestamp: Long)

    // --- CATCH UP ---
    @Query("SELECT * FROM catchup_programs ORDER BY id ASC")
    fun observeCatchUpPrograms(): Flow<List<CatchUpProgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatchUpPrograms(programs: List<CatchUpProgramEntity>)

    // --- ACTIVATION LOGS ---
    @Query("SELECT * FROM activation_logs ORDER BY timestampMillis DESC LIMIT 100")
    fun observeActivationLogs(): Flow<List<ActivationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivationLog(log: ActivationLogEntity)

    // --- RESET ---
    @Query("DELETE FROM live_channels")
    suspend fun clearAllChannels()

    @Query("DELETE FROM vod_movies")
    suspend fun clearAllMovies()

    @Query("DELETE FROM series")
    suspend fun clearAllSeries()

    @Query("DELETE FROM episodes")
    suspend fun clearAllEpisodes()

    @Query("DELETE FROM playlists")
    suspend fun clearAllPlaylists()
}
