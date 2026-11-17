package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.core.security.DeviceIdentity
import com.example.core.security.VirtualMacManager
import com.example.data.local.ActivationLogEntity
import com.example.data.local.CatchUpProgramEntity
import com.example.data.local.DeviceEntity
import com.example.data.local.DeviceStatus
import com.example.data.local.EpisodeEntity
import com.example.data.local.LiveChannelEntity
import com.example.data.local.OrientDatabase
import com.example.data.local.PlaylistConfigEntity
import com.example.data.local.PlaylistProtocol
import com.example.data.local.SeriesEntity
import com.example.data.local.SubscriptionPlan
import com.example.data.local.VodMovieEntity
import com.example.data.remote.SupabaseService
import com.example.data.repository.OrientRepository
import com.example.ui.theme.OrientThemeStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OrientNavTab(val titleFr: String, val titleEn: String, val titleAr: String) {
    HOME_HUB("Accueil", "Home", "الرئيسية"),
    LIVE_TV("TV en direct", "Live TV", "البث المباشر"),
    MOVIES("Films", "Movies", "الأفلام"),
    SERIES("Séries", "Series", "المسلسلات"),
    RADIOS("Radios", "Radios", "الراديو"),
    FAVORITES("Favoris", "Favorites", "المفضلة"),
    SEARCH("Recherche", "Search", "البحث"),
    CATCH_UP("Catch Up", "Catch Up", "الإعادة"),
    ADMIN("Admin Web", "Web Admin", "لوحة التحكم"),
    SETTINGS("Paramètres", "Settings", "الإعدادات")
}

enum class AppLanguage(val code: String, val label: String) {
    FR("fr", "Français"),
    EN("en", "English"),
    AR("ar", "العربية")
}

data class ActivePlaybackItem(
    val title: String,
    val subtitle: String,
    val streamUrl: String,
    val logoOrPosterUrl: String,
    val isLiveChannel: Boolean,
    val channelId: Long? = null,
    val movieId: Long? = null,
    val seriesId: Long? = null,
    val episodeId: Long? = null,
    val nextEpisode: EpisodeEntity? = null,
    val startPositionMs: Long = 0L
)

data class RecommendedMediaItem(
    val id: String,
    val title: String,
    val categoryBadge: String,
    @DrawableRes val posterRes: Int,
    val streamUrl: String,
    val isLive: Boolean,
    val subtitle: String
)

data class RadioStationItem(
    val id: Long,
    val name: String,
    val genre: String,
    val frequencyOrBitrate: String,
    val currentTrack: String,
    val logoUrl: String,
    val streamUrl: String
)

class OrientViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("orient_settings_prefs", Context.MODE_PRIVATE)
    private val db = OrientDatabase.getInstance(context)
    private val repository = OrientRepository(context, db.orientDao())

    private val _deviceIdentity = MutableStateFlow(VirtualMacManager.getOrCreateDeviceIdentity(context))
    val deviceIdentity: StateFlow<DeviceIdentity> = _deviceIdentity.asStateFlow()

    // Default to HOME_HUB so the app launches on the exact Orient Player TV Streaming interface
    private val _currentTab = MutableStateFlow(OrientNavTab.HOME_HUB)
    val currentTab: StateFlow<OrientNavTab> = _currentTab.asStateFlow()

    private val _selectedHomeCardIndex = MutableStateFlow(0)
    val selectedHomeCardIndex: StateFlow<Int> = _selectedHomeCardIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLiveCategory = MutableStateFlow("Toutes")
    val selectedLiveCategory: StateFlow<String> = _selectedLiveCategory.asStateFlow()

    private val _selectedMovieCategory = MutableStateFlow("Tous")
    val selectedMovieCategory: StateFlow<String> = _selectedMovieCategory.asStateFlow()

    private val _selectedSeriesCategory = MutableStateFlow("Toutes")
    val selectedSeriesCategory: StateFlow<String> = _selectedSeriesCategory.asStateFlow()

    private val _selectedChannel = MutableStateFlow<LiveChannelEntity?>(null)
    val selectedChannel: StateFlow<LiveChannelEntity?> = _selectedChannel.asStateFlow()

    private val _selectedMovieDetail = MutableStateFlow<VodMovieEntity?>(null)
    val selectedMovieDetail: StateFlow<VodMovieEntity?> = _selectedMovieDetail.asStateFlow()

    private val _selectedSeriesDetail = MutableStateFlow<SeriesEntity?>(null)
    val selectedSeriesDetail: StateFlow<SeriesEntity?> = _selectedSeriesDetail.asStateFlow()

    private val _fullscreenPlayback = MutableStateFlow<ActivePlaybackItem?>(null)
    val fullscreenPlayback: StateFlow<ActivePlaybackItem?> = _fullscreenPlayback.asStateFlow()

    private val _showMacActivationModal = MutableStateFlow(false)
    val showMacActivationModal: StateFlow<Boolean> = _showMacActivationModal.asStateFlow()

    private val _showQuickMenuModal = MutableStateFlow(false)
    val showQuickMenuModal: StateFlow<Boolean> = _showQuickMenuModal.asStateFlow()

    private val _themeStyle = MutableStateFlow(
        OrientThemeStyle.entries.find { it.name == prefs.getString("theme_style", null) }
            ?: OrientThemeStyle.MIDNIGHT_BLUE
    )
    val themeStyle: StateFlow<OrientThemeStyle> = _themeStyle.asStateFlow()

    private val _language = MutableStateFlow(
        AppLanguage.entries.find { it.code == prefs.getString("app_lang", "fr") } ?: AppLanguage.FR
    )
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _parentalPin = MutableStateFlow(prefs.getString("parental_pin", "0000") ?: "0000")
    val parentalPin: StateFlow<String> = _parentalPin.asStateFlow()

    private val _parentalEnabled = MutableStateFlow(prefs.getBoolean("parental_enabled", true))
    val parentalEnabled: StateFlow<Boolean> = _parentalEnabled.asStateFlow()

    private val _isAdultUnlockedSession = MutableStateFlow(false)
    val isAdultUnlockedSession: StateFlow<Boolean> = _isAdultUnlockedSession.asStateFlow()

    private val _autoUpdatePlaylist = MutableStateFlow(prefs.getBoolean("auto_update_playlist", true))
    val autoUpdatePlaylist: StateFlow<Boolean> = _autoUpdatePlaylist.asStateFlow()

    private val _cacheSizeLabel = MutableStateFlow("Calcul en cours…")
    val cacheSizeLabel: StateFlow<String> = _cacheSizeLabel.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    private val _supabaseStatus = MutableStateFlow(
        if (SupabaseService.isConfigured()) "Serveur connecté (Supabase)" else "Serveur connecté"
    )
    val supabaseStatus: StateFlow<String> = _supabaseStatus.asStateFlow()

    // The 8 exact "Recommandé pour vous" items from the reference UI
    val recommendedItems: List<RecommendedMediaItem> = listOf(
        RecommendedMediaItem(
            id = "casa_de_papel",
            title = "La Casa de Papel",
            categoryBadge = "Séries",
            posterRes = R.drawable.rec_casa_de_papel,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            isLive = false,
            subtitle = "Série Originale • Saison 1 Épisode 1 • 4K HDR"
        ),
        RecommendedMediaItem(
            id = "mandalorian",
            title = "The Mandalorian",
            categoryBadge = "Séries",
            posterRes = R.drawable.rec_mandalorian,
            streamUrl = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8",
            isLive = false,
            subtitle = "Science-Fiction • Saison 3 • Dolby Vision"
        ),
        RecommendedMediaItem(
            id = "dune",
            title = "Dune : Deuxième Partie",
            categoryBadge = "Films",
            posterRes = R.drawable.rec_dune,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            isLive = false,
            subtitle = "Film Épique • 2025 • Ultra HD 4K"
        ),
        RecommendedMediaItem(
            id = "ucl",
            title = "UEFA Champions League",
            categoryBadge = "Sport",
            posterRes = R.drawable.rec_champions_league,
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
            isLive = true,
            subtitle = "EN DIRECT • Multiplex Soirée Ligue des Champions UHD"
        ),
        RecommendedMediaItem(
            id = "naruto",
            title = "Naruto Shippuden",
            categoryBadge = "Dessins Animés",
            posterRes = R.drawable.rec_naruto,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            isLive = false,
            subtitle = "Animation & Action • Épisode Spécial HD"
        ),
        RecommendedMediaItem(
            id = "got",
            title = "Game of Thrones",
            categoryBadge = "Séries",
            posterRes = R.drawable.rec_got,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            isLive = false,
            subtitle = "Fantastique & Drame • Intégrale 4K"
        ),
        RecommendedMediaItem(
            id = "top14",
            title = "Top 14 Rugby Direct",
            categoryBadge = "Sport",
            posterRes = R.drawable.rec_top14,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            isLive = true,
            subtitle = "EN DIRECT • Grand Match du Soir • Orient Sport Arena"
        ),
        RecommendedMediaItem(
            id = "the_voice",
            title = "The Voice : La Grande Finale",
            categoryBadge = "Divertissement",
            posterRes = R.drawable.rec_the_voice,
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            isLive = true,
            subtitle = "EN DIRECT • Prime Time Musical Dolby Audio"
        )
    )

    val radioStations: List<RadioStationItem> = listOf(
        RadioStationItem(
            id = 1,
            name = "ORIENT FM LIVE",
            genre = "Hits & Musique Orientale",
            frequencyOrBitrate = "104.5 FM • 320 kbps AAC",
            currentTrack = "Session Live : Nuits de Méditerranée",
            logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400&q=80",
            streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
        ),
        RadioStationItem(
            id = 2,
            name = "MONTE CARLO DOUALIYA",
            genre = "Actualités & Culture",
            frequencyOrBitrate = "98.8 FM • Direct 24/7",
            currentTrack = "Journal International & Débats",
            logoUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=400&q=80",
            streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
        ),
        RadioStationItem(
            id = 3,
            name = "ORIENT SPORT RADIO",
            genre = "Sport & Multiplex Foot",
            frequencyOrBitrate = "101.2 FM • Dolby Audio",
            currentTrack = "Multiplex Ligue des Champions & L1",
            logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=400&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        ),
        RadioStationItem(
            id = 4,
            name = "LOUNGE & CHILL ARABIA",
            genre = "Deep House & Oriental Chill",
            frequencyOrBitrate = "HD Lossless • 24/7",
            currentTrack = "Sunset Rooftop Mix",
            logoUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=400&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
        ),
        RadioStationItem(
            id = 5,
            name = "ANDALOUSE CLASSIQUE",
            genre = "Musique Classique & Patrimoine",
            frequencyOrBitrate = "92.4 FM • Hi-Fi",
            currentTrack = "Orchestre Symphonique d'Orient",
            logoUrl = "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?w=400&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
        ),
        RadioStationItem(
            id = 6,
            name = "URBAN BEATS & RAP FM",
            genre = "Urban, Hip-Hop & R&B",
            frequencyOrBitrate = "107.7 FM • 320 kbps",
            currentTrack = "Top 40 Urban Non-Stop",
            logoUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400&q=80",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
        )
    )

    val devices: StateFlow<List<DeviceEntity>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistConfigEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<LiveChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val movies: StateFlow<List<VodMovieEntity>> = repository.allMovies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seriesList: StateFlow<List<SeriesEntity>> = repository.allSeries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEpisodes: StateFlow<List<EpisodeEntity>> = repository.allEpisodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catchUpPrograms: StateFlow<List<CatchUpProgramEntity>> = repository.catchUpPrograms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activationLogs: StateFlow<List<ActivationLogEntity>> = repository.activationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val result = repository.initializeAndSync(_deviceIdentity.value)
            _supabaseStatus.value = if (result.isCloudConnected) "Serveur connecté (Supabase)" else "Serveur connecté"
            _cacheSizeLabel.value = repository.getCacheSizeFormatted()
        }
        viewModelScope.launch {
            repository.allChannels.collect { list ->
                if (_selectedChannel.value == null && list.isNotEmpty()) {
                    _selectedChannel.value = list.first()
                } else if (_selectedChannel.value != null) {
                    val updated = list.find { it.id == _selectedChannel.value?.id }
                    if (updated != null) _selectedChannel.value = updated
                }
            }
        }
    }

    fun selectTab(tab: OrientNavTab) {
        _currentTab.value = tab
    }

    fun selectHomeCardIndex(index: Int) {
        _selectedHomeCardIndex.value = index.coerceIn(0, 5)
    }

    fun activateSelectedHomeCard() {
        val targetTab = when (_selectedHomeCardIndex.value) {
            0 -> OrientNavTab.LIVE_TV
            1 -> OrientNavTab.MOVIES
            2 -> OrientNavTab.SERIES
            3 -> OrientNavTab.RADIOS
            4 -> OrientNavTab.FAVORITES
            5 -> OrientNavTab.SEARCH
            else -> OrientNavTab.LIVE_TV
        }
        _currentTab.value = targetTab
    }

    fun playRecommendedItem(item: RecommendedMediaItem) {
        _fullscreenPlayback.value = ActivePlaybackItem(
            title = item.title,
            subtitle = item.subtitle,
            streamUrl = item.streamUrl,
            logoOrPosterUrl = "",
            isLiveChannel = item.isLive
        )
    }

    fun playRadioStation(station: RadioStationItem) {
        _fullscreenPlayback.value = ActivePlaybackItem(
            title = "RADIO LIVE • ${station.name}",
            subtitle = "${station.frequencyOrBitrate} • ${station.currentTrack}",
            streamUrl = station.streamUrl,
            logoOrPosterUrl = station.logoUrl,
            isLiveChannel = true
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLiveCategory(cat: String) {
        _selectedLiveCategory.value = cat
    }

    fun selectMovieCategory(cat: String) {
        _selectedMovieCategory.value = cat
    }

    fun selectSeriesCategory(cat: String) {
        _selectedSeriesCategory.value = cat
    }

    fun setShowMacActivationModal(show: Boolean) {
        _showMacActivationModal.value = show
    }

    fun setShowQuickMenuModal(show: Boolean) {
        _showQuickMenuModal.value = show
    }

    fun dismissBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun postMessage(msg: String) {
        _statusBannerMessage.value = msg
    }

    // --- LIVE TV PLAYBACK & RAPID ZAPPING ---
    fun selectChannel(channel: LiveChannelEntity, openFullscreen: Boolean = false) {
        _selectedChannel.value = channel
        viewModelScope.launch {
            repository.markChannelWatched(channel.id)
        }
        if (openFullscreen) {
            _fullscreenPlayback.value = ActivePlaybackItem(
                title = "${channel.channelNumber}. ${channel.name}",
                subtitle = "EN DIRECT • ${channel.currentProgramTitle} (${channel.currentProgramStart} - ${channel.currentProgramEnd})",
                streamUrl = channel.streamUrl,
                logoOrPosterUrl = channel.logoUrl,
                isLiveChannel = true,
                channelId = channel.id
            )
        }
    }

    fun zapChannel(delta: Int) {
        val currentList = channels.value.filter { !it.isAdult || !_parentalEnabled.value || _isAdultUnlockedSession.value }
        if (currentList.isEmpty()) return
        val currentId = _selectedChannel.value?.id ?: currentList.first().id
        val currentIndex = currentList.indexOfFirst { it.id == currentId }.coerceAtLeast(0)
        val nextIndex = (currentIndex + delta + currentList.size) % currentList.size
        val target = currentList[nextIndex]
        _selectedChannel.value = target
        viewModelScope.launch {
            repository.markChannelWatched(target.id)
        }
        if (_fullscreenPlayback.value?.isLiveChannel == true) {
            _fullscreenPlayback.value = ActivePlaybackItem(
                title = "${target.channelNumber}. ${target.name}",
                subtitle = "EN DIRECT • ${target.currentProgramTitle} (${target.currentProgramStart} - ${target.currentProgramEnd})",
                streamUrl = target.streamUrl,
                logoOrPosterUrl = target.logoUrl,
                isLiveChannel = true,
                channelId = target.id
            )
        }
    }

    fun toggleChannelFavorite(channel: LiveChannelEntity) {
        viewModelScope.launch {
            repository.toggleChannelFavorite(channel)
        }
    }

    // --- MOVIES PLAYBACK & DETAILS ---
    fun openMovieDetail(movie: VodMovieEntity?) {
        _selectedMovieDetail.value = movie
    }

    fun playMovie(movie: VodMovieEntity, resume: Boolean) {
        val startPos = if (resume && movie.resumePositionMs > 5_000L) movie.resumePositionMs else 0L
        _fullscreenPlayback.value = ActivePlaybackItem(
            title = movie.title,
            subtitle = "${movie.year} • ${movie.genre} • ★ ${movie.rating}",
            streamUrl = movie.streamUrl,
            logoOrPosterUrl = movie.posterUrl,
            isLiveChannel = false,
            movieId = movie.id,
            startPositionMs = startPos
        )
    }

    fun toggleMovieFavorite(movie: VodMovieEntity) {
        viewModelScope.launch {
            repository.toggleMovieFavorite(movie)
            if (_selectedMovieDetail.value?.id == movie.id) {
                _selectedMovieDetail.value = movie.copy(isFavorite = !movie.isFavorite)
            }
        }
    }

    // --- SERIES & AUTO-NEXT EPISODE PLAYBACK ---
    fun openSeriesDetail(series: SeriesEntity?) {
        _selectedSeriesDetail.value = series
    }

    fun playEpisode(series: SeriesEntity, episode: EpisodeEntity, resume: Boolean) {
        val seriesEpisodes = allEpisodes.value
            .filter { it.seriesId == series.id }
            .sortedWith(compareBy<EpisodeEntity> { it.seasonNumber }.thenBy { it.episodeNumber })

        val currentIdx = seriesEpisodes.indexOfFirst { it.id == episode.id }
        val nextEp = if (currentIdx != -1 && currentIdx + 1 < seriesEpisodes.size) {
            seriesEpisodes[currentIdx + 1]
        } else {
            null
        }

        val startPos = if (resume && episode.resumePositionMs > 5_000L) episode.resumePositionMs else 0L
        _fullscreenPlayback.value = ActivePlaybackItem(
            title = "${series.title} - S${episode.seasonNumber}E${episode.episodeNumber}",
            subtitle = episode.title,
            streamUrl = episode.streamUrl,
            logoOrPosterUrl = series.posterUrl,
            isLiveChannel = false,
            seriesId = series.id,
            episodeId = episode.id,
            nextEpisode = nextEp,
            startPositionMs = startPos
        )
    }

    fun toggleSeriesFavorite(series: SeriesEntity) {
        viewModelScope.launch {
            repository.toggleSeriesFavorite(series)
            if (_selectedSeriesDetail.value?.id == series.id) {
                _selectedSeriesDetail.value = series.copy(isFavorite = !series.isFavorite)
            }
        }
    }

    // --- CATCH-UP PLAYBACK ---
    fun playCatchUpProgram(program: CatchUpProgramEntity) {
        _fullscreenPlayback.value = ActivePlaybackItem(
            title = "REPLAY • ${program.title}",
            subtitle = "${program.channelName} • ${program.dayLabel} (${program.timeSlot})",
            streamUrl = program.streamUrl,
            logoOrPosterUrl = program.channelLogo,
            isLiveChannel = false
        )
    }

    // --- SAVE PROGRESS & CLOSE PLAYER ---
    fun savePlaybackProgressAndClose(positionMs: Long, durationMs: Long) {
        val active = _fullscreenPlayback.value
        _fullscreenPlayback.value = null
        if (active == null || active.isLiveChannel) return

        viewModelScope.launch {
            if (active.movieId != null && durationMs > 0) {
                repository.updateMovieProgress(active.movieId, positionMs, durationMs)
            } else if (active.seriesId != null && active.episodeId != null && durationMs > 0) {
                repository.updateEpisodeProgress(active.seriesId, active.episodeId, positionMs, durationMs)
            }
        }
    }

    fun triggerNextEpisodeIfAvailable() {
        val active = _fullscreenPlayback.value ?: return
        val nextEp = active.nextEpisode ?: return
        val series = seriesList.value.find { it.id == active.seriesId } ?: return
        playEpisode(series, nextEp, resume = false)
    }

    // --- ADMIN & PLAYLIST ACTIONS ---
    fun syncWithBackendNow() {
        viewModelScope.launch {
            val result = repository.initializeAndSync(_deviceIdentity.value)
            _supabaseStatus.value = if (result.isCloudConnected) "Serveur connecté (Supabase)" else "Serveur connecté"
            _statusBannerMessage.value = "Synchronisation Backend : ${result.statusMessage}"
        }
    }

    fun adminSaveDevice(
        virtualMac: String,
        clientName: String,
        deviceModel: String,
        deviceType: String,
        plan: SubscriptionPlan,
        status: DeviceStatus
    ) {
        viewModelScope.launch {
            repository.addOrUpdateDeviceFromAdmin(
                virtualMac = virtualMac,
                clientName = clientName,
                deviceModel = deviceModel,
                deviceType = deviceType,
                plan = plan,
                status = status
            )
            _statusBannerMessage.value = "Appareil $virtualMac enregistré et synchronisé (${plan.label})"
        }
    }

    fun adminChangeDeviceStatus(device: DeviceEntity, newStatus: DeviceStatus) {
        viewModelScope.launch {
            repository.changeDeviceStatus(device, newStatus)
            _statusBannerMessage.value = "Appareil ${device.virtualMac} → ${newStatus.label}"
        }
    }

    fun adminRenewSubscription(device: DeviceEntity, plan: SubscriptionPlan) {
        viewModelScope.launch {
            repository.renewDeviceSubscription(device, plan)
            _statusBannerMessage.value = "Abonnement ${plan.label} activé pour ${device.virtualMac}"
        }
    }

    fun addPlaylistConfig(
        deviceMac: String,
        name: String,
        protocol: PlaylistProtocol,
        serverUrl: String,
        username: String = "",
        password: String = "",
        stalkerMac: String = "",
        autoUpdate: Boolean = true
    ) {
        viewModelScope.launch {
            _statusBannerMessage.value = "Synchronisation de la playlist ${protocol.label} en cours…"
            val msg = repository.addAndSyncPlaylist(
                deviceMac = deviceMac,
                name = name,
                protocol = protocol,
                serverUrl = serverUrl,
                username = username,
                password = password,
                stalkerMac = stalkerMac,
                autoUpdate = autoUpdate
            )
            _statusBannerMessage.value = msg
        }
    }

    fun importM3uFile(uri: Uri, name: String) {
        viewModelScope.launch {
            val msg = repository.importLocalM3uUri(_deviceIdentity.value.virtualMac, uri, name)
            _statusBannerMessage.value = msg
        }
    }

    fun deletePlaylist(playlist: PlaylistConfigEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            _statusBannerMessage.value = "Playlist « ${playlist.name} » supprimée"
        }
    }

    // --- SETTINGS & PARENTAL CONTROL ---
    fun setThemeStyle(style: OrientThemeStyle) {
        _themeStyle.value = style
        prefs.edit().putString("theme_style", style.name).apply()
    }

    fun setAppLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.code).apply()
        _statusBannerMessage.value = "Langue définie sur : ${lang.label}"
    }

    fun setAutoUpdatePlaylist(enabled: Boolean) {
        _autoUpdatePlaylist.value = enabled
        prefs.edit().putBoolean("auto_update_playlist", enabled).apply()
    }

    fun verifyParentalPin(inputPin: String): Boolean {
        val match = inputPin == _parentalPin.value
        if (match) {
            _isAdultUnlockedSession.value = true
            _statusBannerMessage.value = "Contrôle parental déverrouillé pour cette session"
        }
        return match
    }

    fun updateParentalControl(enabled: Boolean, newPin: String) {
        _parentalEnabled.value = enabled
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            _parentalPin.value = newPin
            prefs.edit().putString("parental_pin", newPin).apply()
        }
        prefs.edit().putBoolean("parental_enabled", enabled).apply()
        if (enabled) _isAdultUnlockedSession.value = false
        _statusBannerMessage.value = "Paramètres du contrôle parental mis à jour"
    }

    fun clearCache() {
        viewModelScope.launch {
            _cacheSizeLabel.value = repository.clearAppCache()
            _statusBannerMessage.value = "Cache vidéo, images et EPG nettoyé avec succès"
        }
    }

    fun resetEntireApplication() {
        viewModelScope.launch {
            val newId = repository.resetApplicationData()
            _deviceIdentity.value = newId
            _isAdultUnlockedSession.value = false
            _statusBannerMessage.value = "Application réinitialisée • Nouvelle MAC virtuelle : ${newId.virtualMac}"
        }
    }
}
