package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.LiveChannelEntity
import com.example.ui.player.OrientMiniPlayerPreview
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientCoralRed
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientEmerald
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientGradientButton
import com.example.ui.theme.OrientGradientPill
import com.example.ui.theme.OrientGradients
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientSurfaceNavy
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise

@Composable
fun LiveTvScreen(
    channels: List<LiveChannelEntity>,
    selectedCategory: String,
    selectedChannel: LiveChannelEntity?,
    searchQuery: String,
    parentalEnabled: Boolean,
    isAdultUnlocked: Boolean,
    onSelectCategory: (String) -> Unit,
    onSelectChannel: (LiveChannelEntity, Boolean) -> Unit,
    onToggleFavorite: (LiveChannelEntity) -> Unit,
    onZapChannel: (Int) -> Unit,
    onVerifyParentalPin: (String) -> Boolean
) {
    var pendingLockedChannel by remember { mutableStateOf<LiveChannelEntity?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val categories = remember(channels) {
        val dynamicCats = channels.map { it.category }.distinct()
        listOf("Toutes", "Favoris", "Historique Récent") + dynamicCats
    }

    val filteredChannels = remember(channels, selectedCategory, searchQuery) {
        val byCat = when (selectedCategory) {
            "Toutes" -> channels
            "Favoris" -> channels.filter { it.isFavorite }
            "Historique Récent" -> channels.filter { it.lastWatchedMillis > 0L }.sortedByDescending { it.lastWatchedMillis }
            else -> channels.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        if (searchQuery.isBlank()) {
            byCat
        } else {
            byCat.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.currentProgramTitle.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val activeChannel = selectedChannel ?: filteredChannels.firstOrNull() ?: channels.firstOrNull()

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Bar with Electric Blue -> Turquoise Active Gradient Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                OrientGradientPill(
                    selected = isSelected,
                    onClick = { onSelectCategory(category) },
                    label = category,
                    modifier = Modifier.testTag("live_category_chip_$category")
                )
            }
        }

        // Responsive Split View (Wide TV/Tablet vs Compact Mobile)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            val isWideScreen = maxWidth >= 700.dp

            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Channel List with EPG progress
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.46f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredChannels, key = { it.id }) { channel ->
                            LiveChannelRowCard(
                                channel = channel,
                                isSelected = activeChannel?.id == channel.id,
                                isLocked = channel.isAdult && parentalEnabled && !isAdultUnlocked,
                                onClick = {
                                    if (channel.isAdult && parentalEnabled && !isAdultUnlocked) {
                                        pendingLockedChannel = channel
                                        pinInput = ""
                                        pinError = false
                                    } else {
                                        onSelectChannel(channel, false)
                                    }
                                },
                                onDoubleClickOrPlay = {
                                    if (channel.isAdult && parentalEnabled && !isAdultUnlocked) {
                                        pendingLockedChannel = channel
                                        pinInput = ""
                                        pinError = false
                                    } else {
                                        onSelectChannel(channel, true)
                                    }
                                },
                                onToggleFavorite = { onToggleFavorite(channel) }
                            )
                        }
                    }

                    // Right Column: Mini Player Preview + Full EPG Guide (Current & Next Program)
                    if (activeChannel != null) {
                        LiveChannelEpgDetailPanel(
                            channel = activeChannel,
                            onOpenFullscreen = { onSelectChannel(activeChannel, true) },
                            onZapChannel = onZapChannel,
                            onToggleFavorite = { onToggleFavorite(activeChannel) },
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight()
                        )
                    }
                }
            } else {
                // Compact Portrait Layout: Preview + EPG Header on top, Channel List below
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (activeChannel != null) {
                        LiveChannelCompactPreviewCard(
                            channel = activeChannel,
                            onOpenFullscreen = { onSelectChannel(activeChannel, true) },
                            onZapChannel = onZapChannel
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredChannels, key = { it.id }) { channel ->
                            LiveChannelRowCard(
                                channel = channel,
                                isSelected = activeChannel?.id == channel.id,
                                isLocked = channel.isAdult && parentalEnabled && !isAdultUnlocked,
                                onClick = {
                                    if (channel.isAdult && parentalEnabled && !isAdultUnlocked) {
                                        pendingLockedChannel = channel
                                        pinInput = ""
                                        pinError = false
                                    } else {
                                        onSelectChannel(channel, false)
                                    }
                                },
                                onDoubleClickOrPlay = {
                                    if (channel.isAdult && parentalEnabled && !isAdultUnlocked) {
                                        pendingLockedChannel = channel
                                        pinInput = ""
                                        pinError = false
                                    } else {
                                        onSelectChannel(channel, true)
                                    }
                                },
                                onToggleFavorite = { onToggleFavorite(channel) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Parental PIN Verification Dialog
    if (pendingLockedChannel != null) {
        AlertDialog(
            onDismissRequest = { pendingLockedChannel = null },
            title = { Text("Contrôle Parental ORIENT PLAYER") },
            text = {
                Column {
                    Text(
                        text = "Entrez le code PIN à 4 chiffres pour déverrouiller « ${pendingLockedChannel?.name} » (PIN par défaut : 0000).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("Code PIN Parental") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parental_pin_input")
                    )
                    if (pinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Code PIN incorrect. Veuillez réessayer.",
                            color = OrientCoralRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onVerifyParentalPin(pinInput)) {
                            val unlocked = pendingLockedChannel
                            pendingLockedChannel = null
                            if (unlocked != null) {
                                onSelectChannel(unlocked, false)
                            }
                        } else {
                            pinError = true
                        }
                    },
                    modifier = Modifier.testTag("parental_pin_confirm_button")
                ) {
                    Text("Déverrouiller")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLockedChannel = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun LiveChannelRowCard(
    channel: LiveChannelEntity,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    onDoubleClickOrPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val containerColor = if (isSelected) OrientCardNavy else OrientDeepNavy

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, OrientGradients.ActiveBorderBrush, RoundedCornerShape(14.dp))
                } else {
                    Modifier.border(1.dp, OrientBorderBlue.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                }
            )
            .clickable { onClick() }
            .testTag("live_channel_card_${channel.channelNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(OrientGradients.CardActiveSurfaceBrush)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Number Badge
            Box(
                modifier = Modifier
                    .size(width = 38.dp, height = 34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .then(
                        if (isSelected) {
                            Modifier.background(OrientGradients.PrimaryButtonBrush)
                        } else {
                            Modifier.background(OrientMidnightDark)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", channel.channelNumber),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else OrientTurquoise,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Channel Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrientMidnightDark),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = "Logo ${channel.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Verrouillé",
                            tint = OrientCoralRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Channel Name, Current EPG & Progress Bar
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (channel.hasCatchUp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = OrientTurquoise.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "REPLAY",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrientTurquoise,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${channel.currentProgramStart} • ${channel.currentProgramTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrientTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                LinearProgressIndicator(
                    progress = { channel.currentProgramProgress },
                    color = if (isSelected) OrientTurquoise else OrientElectricBlue,
                    trackColor = OrientMidnightDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Favorite Toggle & Direct Fullscreen Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("fav_channel_btn_${channel.channelNumber}")
            ) {
                Icon(
                    imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favori",
                    tint = if (channel.isFavorite) OrientGold else OrientTextSecondary
                )
            }

            IconButton(
                onClick = onDoubleClickOrPlay,
                modifier = Modifier
                    .size(36.dp)
                    .background(OrientElectricBlue.copy(alpha = 0.2f), CircleShape)
                    .testTag("play_fullscreen_channel_btn_${channel.channelNumber}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Lire en plein écran",
                    tint = OrientTurquoise
                )
            }
        }
    }
}

@Composable
private fun LiveChannelEpgDetailPanel(
    channel: LiveChannelEntity,
    onOpenFullscreen: () -> Unit,
    onZapChannel: (Int) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Live Video Preview Player
        OrientMiniPlayerPreview(
            streamUrl = channel.streamUrl,
            channelName = channel.name,
            programTitle = channel.currentProgramTitle,
            onOpenFullscreen = onOpenFullscreen,
            onZapChannel = onZapChannel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.52f)
        )

        // EPG Program Card (Current & Next Program + Zapping Controls)
        Card(
            colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.48f)
                .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = OrientEmerald.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "EPG EN DIRECT • ${channel.currentProgramStart} - ${channel.currentProgramEnd}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientEmerald,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = channel.streamFormat,
                                style = MaterialTheme.typography.labelSmall,
                                color = OrientTurquoise
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { onZapChannel(-1) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("CH-", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onZapChannel(1) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("CH+", style = MaterialTheme.typography.labelSmall)
                                Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = channel.currentProgramTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = channel.currentProgramDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = OrientTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { channel.currentProgramProgress },
                        color = OrientTurquoise,
                        trackColor = OrientMidnightDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                // Next Program + Fullscreen Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = OrientGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "À SUIVRE (${channel.nextProgramTime}) : ${channel.nextProgramTitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = OrientGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OrientGradientButton(
                        text = "Plein Écran",
                        onClick = onOpenFullscreen,
                        leadingIcon = Icons.Default.Fullscreen,
                        modifier = Modifier.testTag("epg_fullscreen_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveChannelCompactPreviewCard(
    channel: LiveChannelEntity,
    onOpenFullscreen: () -> Unit,
    onZapChannel: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OrientMiniPlayerPreview(
            streamUrl = channel.streamUrl,
            channelName = channel.name,
            programTitle = "${channel.currentProgramStart}-${channel.currentProgramEnd} • ${channel.currentProgramTitle}",
            onOpenFullscreen = onOpenFullscreen,
            onZapChannel = onZapChannel,
            modifier = Modifier
                .fillMaxWidth()
                .height(185.dp)
        )
        Surface(
            color = OrientDeepNavy,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, OrientBorderBlue, RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EN COURS : ${channel.currentProgramTitle}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "À SUIVRE (${channel.nextProgramTime}) : ${channel.nextProgramTitle}",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrientGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onOpenFullscreen,
                    colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Plein écran", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
