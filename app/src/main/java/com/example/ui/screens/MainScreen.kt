package com.example.ui.screens

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.DeviceStatus
import com.example.ui.player.OrientFullscreenVideoPlayer
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientEmerald
import com.example.ui.theme.OrientGradientButton
import com.example.ui.theme.OrientGradientPill
import com.example.ui.theme.OrientGradients
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.OrientNavTab
import com.example.ui.viewmodel.OrientViewModel

@Composable
fun OrientMainScreen(viewModel: OrientViewModel) {
    val identity by viewModel.deviceIdentity.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedHomeCardIndex by viewModel.selectedHomeCardIndex.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedLiveCat by viewModel.selectedLiveCategory.collectAsStateWithLifecycle()
    val selectedMovieCat by viewModel.selectedMovieCategory.collectAsStateWithLifecycle()
    val selectedSeriesCat by viewModel.selectedSeriesCategory.collectAsStateWithLifecycle()
    val selectedChannel by viewModel.selectedChannel.collectAsStateWithLifecycle()
    val selectedMovieDetail by viewModel.selectedMovieDetail.collectAsStateWithLifecycle()
    val selectedSeriesDetail by viewModel.selectedSeriesDetail.collectAsStateWithLifecycle()
    val fullscreenPlayback by viewModel.fullscreenPlayback.collectAsStateWithLifecycle()
    val showMacModal by viewModel.showMacActivationModal.collectAsStateWithLifecycle()
    val showQuickMenu by viewModel.showQuickMenuModal.collectAsStateWithLifecycle()

    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val movies by viewModel.movies.collectAsStateWithLifecycle()
    val seriesList by viewModel.seriesList.collectAsStateWithLifecycle()
    val allEpisodes by viewModel.allEpisodes.collectAsStateWithLifecycle()
    val catchUpPrograms by viewModel.catchUpPrograms.collectAsStateWithLifecycle()
    val activationLogs by viewModel.activationLogs.collectAsStateWithLifecycle()

    val themeStyle by viewModel.themeStyle.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val parentalEnabled by viewModel.parentalEnabled.collectAsStateWithLifecycle()
    val parentalPin by viewModel.parentalPin.collectAsStateWithLifecycle()
    val isAdultUnlocked by viewModel.isAdultUnlockedSession.collectAsStateWithLifecycle()
    val autoUpdatePlaylist by viewModel.autoUpdatePlaylist.collectAsStateWithLifecycle()
    val cacheSizeLabel by viewModel.cacheSizeLabel.collectAsStateWithLifecycle()
    val supabaseStatus by viewModel.supabaseStatus.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val currentDeviceRecord = devices.find { it.virtualMac == identity.virtualMac }

    if (fullscreenPlayback == null && currentTab != OrientNavTab.HOME_HUB) {
        BackHandler {
            viewModel.selectTab(OrientNavTab.HOME_HUB)
        }
    }

    if (fullscreenPlayback != null) {
        OrientFullscreenVideoPlayer(
            item = fullscreenPlayback!!,
            onClose = { posMs, durMs -> viewModel.savePlaybackProgressAndClose(posMs, durMs) },
            onZapChannel = { delta -> viewModel.zapChannel(delta) },
            onNextEpisode = { viewModel.triggerNextEpisodeIfAvailable() }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OrientGradients.BackgroundVerticalBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (currentTab == OrientNavTab.HOME_HUB) {
                                viewModel.selectHomeCardIndex((selectedHomeCardIndex - 1 + 6) % 6)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (currentTab == OrientNavTab.HOME_HUB) {
                                viewModel.selectHomeCardIndex((selectedHomeCardIndex + 1) % 6)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                            if (currentTab == OrientNavTab.HOME_HUB) {
                                viewModel.activateSelectedHomeCard()
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_MENU -> {
                            viewModel.setShowQuickMenuModal(true)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
    ) {
        if (currentTab != OrientNavTab.HOME_HUB) {
            OrientSubScreenTopBar(
                currentTab = currentTab,
                language = language,
                virtualMac = identity.virtualMac,
                onBackToHome = { viewModel.selectTab(OrientNavTab.HOME_HUB) },
                onSelectTab = { viewModel.selectTab(it) },
                onOpenMacDialog = { viewModel.setShowMacActivationModal(true) }
            )
        }

        AnimatedVisibility(visible = bannerMessage != null) {
            Surface(
                color = OrientCardNavy,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .border(1.dp, OrientGradients.ActiveBorderBrush, RoundedCornerShape(10.dp))
                    .clickable { viewModel.dismissBannerMessage() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bannerMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Fermer",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrientTurquoise,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val isDeviceSuspended = currentDeviceRecord != null &&
            currentDeviceRecord.status != DeviceStatus.ACTIVE.name &&
            currentTab != OrientNavTab.HOME_HUB &&
            currentTab != OrientNavTab.ADMIN &&
            currentTab != OrientNavTab.SETTINGS

        Box(modifier = Modifier.weight(1f)) {
            if (isDeviceSuspended) {
                SuspendedDeviceNotice(
                    virtualMac = identity.virtualMac,
                    activationCode = identity.activationCode,
                    status = currentDeviceRecord?.status ?: "EXPIRED",
                    onOpenMacModal = { viewModel.setShowMacActivationModal(true) },
                    onOpenAdminDashboard = { viewModel.selectTab(OrientNavTab.ADMIN) }
                )
            } else {
                when (currentTab) {
                    OrientNavTab.HOME_HUB -> HomeHubScreen(
                        selectedCardIndex = selectedHomeCardIndex,
                        recommendedItems = viewModel.recommendedItems,
                        virtualMac = identity.virtualMac,
                        onSelectCard = { index, targetTab ->
                            viewModel.selectHomeCardIndex(index)
                            viewModel.selectTab(targetTab)
                        },
                        onPlayRecommended = { item -> viewModel.playRecommendedItem(item) },
                        onSeeAllRecommended = { viewModel.selectTab(OrientNavTab.MOVIES) },
                        onOpenSettings = { viewModel.selectTab(OrientNavTab.SETTINGS) },
                        onOpenMacDialog = { viewModel.setShowMacActivationModal(true) }
                    )

                    OrientNavTab.LIVE_TV -> LiveTvScreen(
                        channels = channels,
                        selectedCategory = selectedLiveCat,
                        selectedChannel = selectedChannel,
                        searchQuery = searchQuery,
                        parentalEnabled = parentalEnabled,
                        isAdultUnlocked = isAdultUnlocked,
                        onSelectCategory = { viewModel.selectLiveCategory(it) },
                        onSelectChannel = { ch, fullscreen -> viewModel.selectChannel(ch, fullscreen) },
                        onToggleFavorite = { viewModel.toggleChannelFavorite(it) },
                        onZapChannel = { viewModel.zapChannel(it) },
                        onVerifyParentalPin = { viewModel.verifyParentalPin(it) }
                    )

                    OrientNavTab.MOVIES -> MoviesScreen(
                        movies = movies,
                        selectedCategory = selectedMovieCat,
                        searchQuery = searchQuery,
                        selectedMovieDetail = selectedMovieDetail,
                        onSelectCategory = { viewModel.selectMovieCategory(it) },
                        onOpenMovieDetail = { viewModel.openMovieDetail(it) },
                        onPlayMovie = { movie, resume -> viewModel.playMovie(movie, resume) },
                        onToggleFavorite = { viewModel.toggleMovieFavorite(it) }
                    )

                    OrientNavTab.SERIES -> SeriesScreen(
                        seriesList = seriesList,
                        allEpisodes = allEpisodes,
                        selectedCategory = selectedSeriesCat,
                        searchQuery = searchQuery,
                        selectedSeriesDetail = selectedSeriesDetail,
                        onSelectCategory = { viewModel.selectSeriesCategory(it) },
                        onOpenSeriesDetail = { viewModel.openSeriesDetail(it) },
                        onPlayEpisode = { series, ep, resume -> viewModel.playEpisode(series, ep, resume) },
                        onToggleFavorite = { viewModel.toggleSeriesFavorite(it) }
                    )

                    OrientNavTab.RADIOS -> RadiosScreen(
                        stations = viewModel.radioStations,
                        searchQuery = searchQuery,
                        onPlayStation = { viewModel.playRadioStation(it) }
                    )

                    OrientNavTab.FAVORITES -> FavoritesScreen(
                        channels = channels,
                        movies = movies,
                        seriesList = seriesList,
                        onPlayChannel = { viewModel.selectChannel(it, true) },
                        onOpenMovie = { viewModel.openMovieDetail(it) },
                        onOpenSeries = { viewModel.openSeriesDetail(it) },
                        onToggleChannelFav = { viewModel.toggleChannelFavorite(it) },
                        onToggleMovieFav = { viewModel.toggleMovieFavorite(it) },
                        onToggleSeriesFav = { viewModel.toggleSeriesFavorite(it) }
                    )

                    OrientNavTab.SEARCH -> GlobalSearchScreen(
                        searchQuery = searchQuery,
                        channels = channels,
                        movies = movies,
                        seriesList = seriesList,
                        radios = viewModel.radioStations,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onPlayChannel = { viewModel.selectChannel(it, true) },
                        onOpenMovie = {
                            viewModel.selectTab(OrientNavTab.MOVIES)
                            viewModel.openMovieDetail(it)
                        },
                        onOpenSeries = {
                            viewModel.selectTab(OrientNavTab.SERIES)
                            viewModel.openSeriesDetail(it)
                        },
                        onPlayRadio = { viewModel.playRadioStation(it) }
                    )

                    OrientNavTab.CATCH_UP -> CatchUpScreen(
                        programs = catchUpPrograms,
                        searchQuery = searchQuery,
                        onPlayCatchUp = { viewModel.playCatchUpProgram(it) }
                    )

                    OrientNavTab.ADMIN -> AdminDashboardScreen(
                        currentIdentity = identity,
                        devices = devices,
                        playlists = playlists,
                        logs = activationLogs,
                        supabaseStatus = supabaseStatus,
                        onSyncSupabase = { viewModel.syncWithBackendNow() },
                        onSaveDevice = { mac, name, model, type, plan, status ->
                            viewModel.adminSaveDevice(mac, name, model, type, plan, status)
                        },
                        onChangeDeviceStatus = { dev, st -> viewModel.adminChangeDeviceStatus(dev, st) },
                        onRenewSubscription = { dev, pl -> viewModel.adminRenewSubscription(dev, pl) },
                        onAddPlaylist = { mac, name, proto, url, user, pass, stalkerMac, autoUpd ->
                            viewModel.addPlaylistConfig(mac, name, proto, url, user, pass, stalkerMac, autoUpd)
                        },
                        onImportLocalM3u = { uri, name -> viewModel.importM3uFile(uri, name) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onPostMessage = { viewModel.postMessage(it) }
                    )

                    OrientNavTab.SETTINGS -> SettingsScreen(
                        identity = identity,
                        currentLanguage = language,
                        currentTheme = themeStyle,
                        autoUpdatePlaylist = autoUpdatePlaylist,
                        parentalEnabled = parentalEnabled,
                        parentalPin = parentalPin,
                        cacheSizeLabel = cacheSizeLabel,
                        supabaseStatus = supabaseStatus,
                        onSelectLanguage = { viewModel.setAppLanguage(it) },
                        onSelectTheme = { viewModel.setThemeStyle(it) },
                        onToggleAutoUpdate = { viewModel.setAutoUpdatePlaylist(it) },
                        onUpdateParental = { en, pin -> viewModel.updateParentalControl(en, pin) },
                        onRefreshPlaylists = { viewModel.syncWithBackendNow() },
                        onClearCache = { viewModel.clearCache() },
                        onResetApp = { viewModel.resetEntireApplication() }
                    )
                }
            }
        }

        // BOTTOM STATUS & REMOTE CONTROL BAR
        OrientTvBottomStatusBar(
            serverStatusText = supabaseStatus,
            isServerActive = (currentDeviceRecord?.status ?: "ACTIVE") == "ACTIVE",
            onOkSelect = {
                if (currentTab == OrientNavTab.HOME_HUB) {
                    viewModel.activateSelectedHomeCard()
                }
            },
            onReturnBack = {
                if (currentTab != OrientNavTab.HOME_HUB) {
                    viewModel.selectTab(OrientNavTab.HOME_HUB)
                } else {
                    viewModel.setShowMacActivationModal(true)
                }
            },
            onOpenMenu = { viewModel.setShowQuickMenuModal(true) }
        )
    }

    if (showQuickMenu) {
        OrientQuickMenuDialog(
            virtualMac = identity.virtualMac,
            activationCode = identity.activationCode,
            onDismiss = { viewModel.setShowQuickMenuModal(false) },
            onNavigateTab = { tab ->
                viewModel.setShowQuickMenuModal(false)
                viewModel.selectTab(tab)
            },
            onOpenMacModal = {
                viewModel.setShowQuickMenuModal(false)
                viewModel.setShowMacActivationModal(true)
            }
        )
    }

    if (showMacModal) {
        ActivationMacDialog(
            identity = identity,
            deviceRecord = currentDeviceRecord,
            supabaseStatus = supabaseStatus,
            onDismiss = { viewModel.setShowMacActivationModal(false) },
            onSyncBackend = { viewModel.syncWithBackendNow() },
            onOpenAdminPanel = { viewModel.selectTab(OrientNavTab.ADMIN) },
            onCopyFeedback = { viewModel.postMessage(it) }
        )
    }
}

@Composable
private fun OrientTvBottomStatusBar(
    serverStatusText: String,
    isServerActive: Boolean,
    onOkSelect: () -> Unit,
    onReturnBack: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(OrientMidnightDark)
    ) {
        HorizontalDivider(color = OrientBorderBlue, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("footer_server_status")
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (isServerActive) OrientEmerald else OrientCoralRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = serverStatusText,
                    color = if (isServerActive) OrientEmerald else OrientCoralRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // (OK) Sélectionner with Electric Blue -> Turquoise gradient badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOkSelect() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("footer_ok_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(OrientGradients.PrimaryButtonBrush)
                            .border(1.dp, OrientTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sélectionner",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // (↩) Retour
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onReturnBack() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("footer_retour_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(1.2.dp, OrientTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Retour",
                            tint = OrientTurquoise,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Retour",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // (≡) Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenMenu() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("footer_menu_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(1.2.dp, OrientTurquoise, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = OrientTurquoise,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Menu",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrientSubScreenTopBar(
    currentTab: OrientNavTab,
    language: AppLanguage,
    virtualMac: String,
    onBackToHome: () -> Unit,
    onSelectTab: (OrientNavTab) -> Unit,
    onOpenMacDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(OrientMidnightDark)
            .border(width = 1.dp, color = OrientBorderBlue)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onBackToHome() }
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .testTag("subscreen_back_home_btn")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour à l'accueil",
                tint = OrientTurquoise,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_orient_brand_logo),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append("Orient ")
                    }
                    withStyle(
                        SpanStyle(
                            brush = OrientGradients.PrimaryButtonBrush,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    ) {
                        append("Player")
                    }
                },
                fontSize = 16.sp
            )
        }

        // Scrollable Module Tabs with Electric Blue -> Turquoise Gradient Active Pill
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(OrientNavTab.entries) { tab ->
                val isSelected = tab == currentTab
                val label = tabTitleForLang(tab, language)
                OrientGradientPill(
                    selected = isSelected,
                    onClick = { onSelectTab(tab) },
                    modifier = Modifier.testTag("top_nav_chip_${tab.name}")
                ) {
                    Icon(
                        imageVector = iconForTab(tab),
                        contentDescription = null,
                        tint = if (isSelected) Color.White else OrientTextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else OrientTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Virtual MAC QR Button with Active Gradient Border
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(OrientGradients.CardActiveSurfaceBrush)
                .border(1.dp, OrientGradients.ActiveBorderBrush, RoundedCornerShape(8.dp))
                .clickable { onOpenMacDialog() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("subscreen_mac_qr_btn")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = "MAC QR",
                    tint = OrientTurquoise,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = virtualMac,
                    fontFamily = JetBrainsMonoFontFamily,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrientQuickMenuDialog(
    virtualMac: String,
    activationCode: String,
    onDismiss: () -> Unit,
    onNavigateTab: (OrientNavTab) -> Unit,
    onOpenMacModal: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = OrientDeepNavy,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .border(
                    width = 1.5.dp,
                    brush = OrientGradients.ActiveBorderBrush,
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MENU PRINCIPAL ORIENT PLAYER",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MAC: $virtualMac",
                        fontFamily = JetBrainsMonoFontFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = OrientTurquoise
                    )
                }

                HorizontalDivider(color = OrientBorderBlue)

                QuickMenuOptionButton(
                    icon = Icons.Default.Home,
                    title = "Accueil Orient Player",
                    subtitle = "Interface principale TV en direct, Films, Séries, Radios",
                    onClick = { onNavigateTab(OrientNavTab.HOME_HUB) }
                )

                QuickMenuOptionButton(
                    icon = Icons.Default.QrCode2,
                    title = "Adresse MAC Virtuelle & QR Code",
                    subtitle = "MAC : $virtualMac • Code : $activationCode",
                    onClick = onOpenMacModal
                )

                QuickMenuOptionButton(
                    icon = Icons.Default.History,
                    title = "Catch Up TV / Replay",
                    subtitle = "Revoir les programmes TV diffusés ces derniers jours",
                    onClick = { onNavigateTab(OrientNavTab.CATCH_UP) }
                )

                QuickMenuOptionButton(
                    icon = Icons.Default.AdminPanelSettings,
                    title = "Panneau Administrateur Web & Playlists",
                    subtitle = "Gérer les appareils MAC, Xtream Codes, M3U, Stalker & Supabase",
                    onClick = { onNavigateTab(OrientNavTab.ADMIN) }
                )

                QuickMenuOptionButton(
                    icon = Icons.Default.Settings,
                    title = "Paramètres & Contrôle Parental",
                    subtitle = "Langue, Thème, Cache, Mise à jour playlist et Code PIN",
                    onClick = { onNavigateTab(OrientNavTab.SETTINGS) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Fermer", color = OrientTurquoise)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMenuOptionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = OrientCardNavy,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrientGradients.PrimaryButtonBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OrientTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SuspendedDeviceNotice(
    virtualMac: String,
    activationCode: String,
    status: String,
    onOpenMacModal: () -> Unit,
    onOpenAdminDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = OrientDeepNavy,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.5.dp, OrientCoralRed, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (status == "BLOCKED") "APPAREIL BLOQUÉ PAR L'ADMINISTRATEUR" else "ABONNEMENT EXPIRÉ — ACTIVATION REQUISE",
                    style = MaterialTheme.typography.titleLarge,
                    color = OrientCoralRed,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Veuillez communiquer votre adresse MAC virtuelle et votre code d'activation à votre administrateur ou réactiver l'appareil depuis le Dashboard Admin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrientTextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "MAC : $virtualMac   |   CODE : $activationCode",
                    fontFamily = JetBrainsMonoFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrientTurquoise
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OrientGradientButton(onClick = onOpenMacModal) {
                        Text("Afficher QR Code & MAC", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    OrientGradientButton(onClick = onOpenAdminDashboard) {
                        Text("Réactiver dans Admin Web", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun iconForTab(tab: OrientNavTab): ImageVector {
    return when (tab) {
        OrientNavTab.HOME_HUB -> Icons.Default.Home
        OrientNavTab.LIVE_TV -> Icons.Default.LiveTv
        OrientNavTab.MOVIES -> Icons.Default.Movie
        OrientNavTab.SERIES -> Icons.Default.VideoLibrary
        OrientNavTab.RADIOS -> Icons.Default.Radio
        OrientNavTab.FAVORITES -> Icons.Default.Star
        OrientNavTab.SEARCH -> Icons.Default.Search
        OrientNavTab.CATCH_UP -> Icons.Default.History
        OrientNavTab.ADMIN -> Icons.Default.AdminPanelSettings
        OrientNavTab.SETTINGS -> Icons.Default.Settings
    }
}

private fun tabTitleForLang(tab: OrientNavTab, lang: AppLanguage): String {
    return when (lang) {
        AppLanguage.FR -> tab.titleFr
        AppLanguage.EN -> tab.titleEn
        AppLanguage.AR -> tab.titleAr
    }
}
