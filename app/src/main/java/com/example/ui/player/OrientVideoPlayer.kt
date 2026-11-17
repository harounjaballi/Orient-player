package com.example.ui.player

import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientEmerald
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientTurquoise
import com.example.ui.viewmodel.ActivePlaybackItem
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(UnstableApi::class)
@Composable
fun OrientFullscreenVideoPlayer(
    item: ActivePlaybackItem,
    onClose: (positionMs: Long, durationMs: Long) -> Unit,
    onZapChannel: (delta: Int) -> Unit,
    onNextEpisode: () -> Unit
) {
    val context = LocalContext.current

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var autoReconnectCount by remember { mutableIntStateOf(0) }
    var showControls by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableLongStateOf(item.startPositionMs) }
    var totalDurMs by remember { mutableLongStateOf(0L) }
    var volumeLevel by remember { mutableFloatStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }

    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var selectedQualityLabel by remember { mutableStateOf("Auto (UHD/FHD)") }
    var selectedAudioLabel by remember { mutableStateOf("Audio Principal (Stéréo/Dolby)") }
    var selectedSubtitleLabel by remember { mutableStateOf("Désactivés") }

    var showQualityDialog by remember { mutableStateOf(false) }
    var showAudioDialog by remember { mutableStateOf(false) }
    var showSubtitleDialog by remember { mutableStateOf(false) }

    var availableAudioTracks by remember { mutableStateOf(listOf("Audio Principal (Original)", "Français (Dolby Digital)", "English (AAC 2.0)")) }
    var availableSubtitles by remember { mutableStateOf(listOf("Désactivés", "Français (Complet)", "English (CC)", "العربية")) }

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10_000L)
            .setSeekForwardIncrementMs(10_000L)
            .build()
            .apply {
                playWhenReady = true
            }
    }

    // Load or switch stream when item changes
    LaunchedEffect(item.streamUrl, item.startPositionMs) {
        errorMessage = null
        isBuffering = true
        autoReconnectCount = 0
        val mediaItem = MediaItem.fromUri(item.streamUrl)
        exoPlayer.setMediaItem(mediaItem)
        if (!item.isLiveChannel && item.startPositionMs > 0L) {
            exoPlayer.seekTo(item.startPositionMs)
        }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // Attach Player.Listener for buffering, errors, auto-reconnect, tracks, and auto-next episode
    DisposableEffect(exoPlayer, item) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    totalDurMs = exoPlayer.duration.coerceAtLeast(0L)
                } else if (playbackState == Player.STATE_ENDED && !item.isLiveChannel && item.nextEpisode != null) {
                    onNextEpisode()
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onTracksChanged(tracks: Tracks) {
                val audioNames = mutableListOf<String>()
                val textNames = mutableListOf("Désactivés")
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            val label = format.label ?: format.language?.uppercase(Locale.ROOT) ?: "Piste Audio #${audioNames.size + 1}"
                            val channels = if (format.channelCount > 2) "5.1" else "Stéréo"
                            audioNames.add("$label ($channels)")
                        }
                    } else if (group.type == C.TRACK_TYPE_TEXT) {
                        for (i in 0 until group.length) {
                            val format = group.getTrackFormat(i)
                            val label = format.label ?: format.language?.uppercase(Locale.ROOT) ?: "Sous-titre #${textNames.size}"
                            textNames.add(label)
                        }
                    }
                }
                if (audioNames.isNotEmpty()) availableAudioTracks = audioNames.distinct()
                if (textNames.size > 1) availableSubtitles = textNames.distinct()
            }

            override fun onPlayerError(error: PlaybackException) {
                if (autoReconnectCount < 3) {
                    autoReconnectCount++
                    errorMessage = "Reconnexion automatique au flux (${autoReconnectCount}/3)…"
                    exoPlayer.prepare()
                    exoPlayer.play()
                } else {
                    errorMessage = "Flux temporairement indisponible (${error.errorCodeName})"
                    isBuffering = false
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    // Release ExoPlayer when fullscreen player leaves composition
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Poll playback position
    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            totalDurMs = exoPlayer.duration.coerceAtLeast(0L)
            delay(500L)
        }
    }

    // Auto-hide controls after 5 seconds when playing
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && errorMessage == null) {
            delay(5500L)
            showControls = false
        }
    }

    BackHandler {
        onClose(exoPlayer.currentPosition.coerceAtLeast(0L), exoPlayer.duration.coerceAtLeast(0L))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    showControls = true
                    when (event.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                            true
                        }
                        KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_DPAD_UP -> {
                            if (item.isLiveChannel) {
                                onZapChannel(-1)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (item.isLiveChannel) {
                                onZapChannel(1)
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (!item.isLiveChannel) {
                                exoPlayer.seekBack()
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (!item.isLiveChannel) {
                                exoPlayer.seekForward()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("fullscreen_video_player")
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    this.resizeMode = resizeMode
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = exoPlayer
                view.resizeMode = resizeMode
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering / Reconnecting Indicator
        if (isBuffering) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = OrientTurquoise,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "Chargement rapide du flux Media3…",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        // Error Recovery Banner
        if (errorMessage != null && !isBuffering) {
            Surface(
                color = OrientMidnightDark.copy(alpha = 0.92f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .border(1.dp, OrientCoralRed, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Erreur de flux",
                        tint = OrientCoralRed,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Erreur de lecture",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            autoReconnectCount = 0
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                        modifier = Modifier.testTag("player_reconnect_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reconnecter maintenant")
                    }
                }
            }
        }

        // Custom OSD Overlay Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.82f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            ) {
                // TOP BAR: Back, Stream Title, Live Badge, Quality/Audio/Subtitles/Ratio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            onClose(exoPlayer.currentPosition.coerceAtLeast(0L), exoPlayer.duration.coerceAtLeast(0L))
                        },
                        modifier = Modifier
                            .background(OrientMidnightDark.copy(alpha = 0.7f), CircleShape)
                            .testTag("player_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quitter le lecteur",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isLiveChannel) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(OrientCoralRed)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = OrientTurquoise,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Track & Aspect Ratio Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showAudioDialog = true },
                            modifier = Modifier.testTag("player_audio_button")
                        ) {
                            Icon(Icons.Default.Audiotrack, contentDescription = "Audio", tint = OrientTurquoise, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audio", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showSubtitleDialog = true },
                            modifier = Modifier.testTag("player_subtitle_button")
                        ) {
                            Icon(Icons.Default.ClosedCaption, contentDescription = "Sous-titres", tint = OrientTurquoise, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CC", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = { showQualityDialog = true },
                            modifier = Modifier.testTag("player_quality_button")
                        ) {
                            Icon(Icons.Default.Hd, contentDescription = "Qualité", tint = OrientEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(selectedQualityLabel.substringBefore(" "), color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }

                        IconButton(
                            onClick = {
                                resizeMode = when (resizeMode) {
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            },
                            modifier = Modifier.testTag("player_aspect_button")
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = "Format d'écran", tint = Color.White)
                        }
                    }
                }

                // CENTER CONTROLS: Play/Pause, Seek -10/+10s OR Rapid Channel Zapping CH-/CH+
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isLiveChannel) {
                        Button(
                            onClick = { onZapChannel(-1) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientMidnightDark.copy(alpha = 0.8f)),
                            modifier = Modifier.testTag("player_zap_prev_button")
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Chaîne précédente", tint = OrientTurquoise)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CH -", color = Color.White)
                        }
                    } else {
                        IconButton(
                            onClick = { exoPlayer.seekBack() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                                .testTag("player_replay10_button")
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "Reculer 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }

                    IconButton(
                        onClick = {
                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(OrientElectricBlue, CircleShape)
                            .testTag("player_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Lecture",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    if (item.isLiveChannel) {
                        Button(
                            onClick = { onZapChannel(1) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientMidnightDark.copy(alpha = 0.8f)),
                            modifier = Modifier.testTag("player_zap_next_button")
                        ) {
                            Text("CH +", color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.SkipNext, contentDescription = "Chaîne suivante", tint = OrientTurquoise)
                        }
                    } else {
                        IconButton(
                            onClick = { exoPlayer.seekForward() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                                .testTag("player_forward10_button")
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "Avancer 10s", tint = Color.White, modifier = Modifier.size(30.dp))
                        }
                    }
                }

                // BOTTOM BAR: Seekbar (for VOD/Series), Volume Slider, and Next Episode button
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    if (!item.isLiveChannel && totalDurMs > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDurationMs(currentPosMs),
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = currentPosMs.toFloat(),
                                onValueChange = { newPos ->
                                    currentPosMs = newPos.toLong()
                                    exoPlayer.seekTo(currentPosMs)
                                },
                                valueRange = 0f..totalDurMs.toFloat().coerceAtLeast(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = OrientTurquoise,
                                    activeTrackColor = OrientElectricBlue
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                            )
                            Text(
                                text = formatDurationMs(totalDurMs),
                                fontFamily = JetBrainsMonoFontFamily,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Volume Control
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(220.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    exoPlayer.volume = if (isMuted) 0f else volumeLevel
                                }
                            ) {
                                Icon(
                                    imageVector = if (isMuted || volumeLevel == 0f) {
                                        Icons.AutoMirrored.Filled.VolumeOff
                                    } else {
                                        Icons.AutoMirrored.Filled.VolumeUp
                                    },
                                    contentDescription = "Volume",
                                    tint = OrientTurquoise
                                )
                            }
                            Slider(
                                value = if (isMuted) 0f else volumeLevel,
                                onValueChange = { v ->
                                    isMuted = false
                                    volumeLevel = v
                                    exoPlayer.volume = v
                                },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = OrientTurquoise,
                                    activeTrackColor = OrientTurquoise
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Next Episode Button for Series
                        if (item.nextEpisode != null) {
                            Button(
                                onClick = onNextEpisode,
                                colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                                modifier = Modifier.testTag("player_next_episode_button")
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Épisode suivant : S${item.nextEpisode.seasonNumber}E${item.nextEpisode.episodeNumber}")
                            }
                        } else {
                            Text(
                                text = "Moteur AndroidX Media3 • HLS / MPEG-TS / MP4 • Audio: $selectedAudioLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Quality Selection Dialog
    if (showQualityDialog) {
        val qualities = listOf(
            "Auto (UHD/FHD)" to Int.MAX_VALUE,
            "1080p Full HD (8 Mbps)" to 1080,
            "720p HD (4 Mbps)" to 720,
            "480p Économie de données" to 480
        )
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Qualité Vidéo du Flux") },
            text = {
                Column {
                    qualities.forEach { (label, maxHeight) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedQualityLabel = label
                                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                        .buildUpon()
                                        .setMaxVideoSize(Int.MAX_VALUE, maxHeight)
                                        .build()
                                    showQualityDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedQualityLabel == label,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Audio Track Selection Dialog
    if (showAudioDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDialog = false },
            title = { Text("Sélection de la Piste Audio") },
            text = {
                Column {
                    availableAudioTracks.forEach { trackLabel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAudioLabel = trackLabel
                                    showAudioDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = selectedAudioLabel == trackLabel, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(trackLabel)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Subtitle Selection Dialog
    if (showSubtitleDialog) {
        AlertDialog(
            onDismissRequest = { showSubtitleDialog = false },
            title = { Text("Sous-titres (DVB / WebVTT / SRT)") },
            text = {
                Column {
                    availableSubtitles.forEach { subLabel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubtitleLabel = subLabel
                                    val disableText = subLabel == "Désactivés"
                                    exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
                                        .buildUpon()
                                        .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, disableText)
                                        .build()
                                    showSubtitleDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = selectedSubtitleLabel == subLabel, onClick = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(subLabel)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSubtitleDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun OrientMiniPlayerPreview(
    streamUrl: String,
    channelName: String,
    programTitle: String,
    onOpenFullscreen: () -> Unit,
    onZapChannel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }

    val miniPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            volume = 0.85f
        }
    }

    LaunchedEffect(streamUrl) {
        isBuffering = true
        miniPlayer.setMediaItem(MediaItem.fromUri(streamUrl))
        miniPlayer.prepare()
        miniPlayer.playWhenReady = true
    }

    DisposableEffect(miniPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
            }
        }
        miniPlayer.addListener(listener)
        onDispose {
            miniPlayer.removeListener(listener)
            miniPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background( Color.Black)
            .border(1.dp, OrientElectricBlue.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onOpenFullscreen() }
            .testTag("mini_player_preview")
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = miniPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            update = { view ->
                view.player = miniPlayer
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isBuffering) {
            CircularProgressIndicator(
                color = OrientTurquoise,
                strokeWidth = 2.5.dp,
                modifier = Modifier
                    .size(34.dp)
                    .align(Alignment.Center)
            )
        }

        // Top Live Badge & Fullscreen Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OrientEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "APERÇU DIRECT • $channelName",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onOpenFullscreen,
                modifier = Modifier
                    .size(32.dp)
                    .background(OrientElectricBlue.copy(alpha = 0.85f), CircleShape)
                    .testTag("mini_player_fullscreen_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Mode plein écran",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Zapping & Program Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = programTitle,
                style = MaterialTheme.typography.bodySmall,
                color = OrientTurquoise,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = OrientMidnightDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onZapChannel(-1) }
                ) {
                    Text(
                        text = "CH-",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = OrientMidnightDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onZapChannel(1) }
                ) {
                    Text(
                        text = "CH+",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrientTurquoise,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }
}
