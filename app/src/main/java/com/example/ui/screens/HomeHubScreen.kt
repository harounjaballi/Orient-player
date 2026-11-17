package com.example.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.R
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientGradients
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise
import com.example.ui.theme.OrientTurquoiseGlow
import com.example.ui.viewmodel.OrientNavTab
import com.example.ui.viewmodel.RecommendedMediaItem
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeCategoryCardSpec(
    val index: Int,
    val title: String,
    val subtitle: String,
    val targetTab: OrientNavTab,
    @DrawableRes val artworkRes: Int,
    val glowColor: Color
)

@Composable
fun HomeHubScreen(
    selectedCardIndex: Int,
    recommendedItems: List<RecommendedMediaItem>,
    virtualMac: String,
    onSelectCard: (Int, OrientNavTab) -> Unit,
    onPlayRecommended: (RecommendedMediaItem) -> Unit,
    onSeeAllRecommended: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMacDialog: () -> Unit
) {
    val categoryCards = remember {
        listOf(
            HomeCategoryCardSpec(
                index = 0,
                title = "TV en direct",
                subtitle = "Chaînes TV",
                targetTab = OrientNavTab.LIVE_TV,
                artworkRes = R.drawable.card_art_live_tv,
                glowColor = OrientTurquoise
            ),
            HomeCategoryCardSpec(
                index = 1,
                title = "Films",
                subtitle = "Derniers ajouts",
                targetTab = OrientNavTab.MOVIES,
                artworkRes = R.drawable.card_art_movies,
                glowColor = OrientTurquoise
            ),
            HomeCategoryCardSpec(
                index = 2,
                title = "Séries",
                subtitle = "Vos séries préférées",
                targetTab = OrientNavTab.SERIES,
                artworkRes = R.drawable.card_art_series,
                glowColor = OrientTurquoise
            ),
            HomeCategoryCardSpec(
                index = 3,
                title = "Radios",
                subtitle = "Toutes les radios",
                targetTab = OrientNavTab.RADIOS,
                artworkRes = R.drawable.card_art_radios,
                glowColor = OrientTurquoise
            ),
            HomeCategoryCardSpec(
                index = 4,
                title = "Favoris",
                subtitle = "Vos contenus",
                targetTab = OrientNavTab.FAVORITES,
                artworkRes = R.drawable.card_art_favorites,
                glowColor = OrientTurquoise
            ),
            HomeCategoryCardSpec(
                index = 5,
                title = "Recherche",
                subtitle = "Trouvez facilement",
                targetTab = OrientNavTab.SEARCH,
                artworkRes = R.drawable.card_art_search,
                glowColor = OrientTurquoise
            )
        )
    }

    var currentTimeStr by remember { mutableStateOf("21:45") }
    var currentDateStr by remember { mutableStateOf("Sam. 16 Nov. 2026") }

    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val dateFmt = SimpleDateFormat("EEE d MMM yyyy", Locale.FRANCE)
        while (true) {
            val now = Date()
            currentTimeStr = timeFmt.format(now)
            val formattedDate = dateFmt.format(now).replaceFirstChar { it.uppercase() }
            currentDateStr = formattedDate
            delay(15_000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OrientGradients.BackgroundVerticalBrush)
            .testTag("home_hub_screen")
    ) {
        // Top-Right Cinema Artwork Backdrop blended into the Deep Navy & Obsidian Black background
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.45f)
                .align(Alignment.TopEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_orient_hero_cinema),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0.0f to OrientMidnightDark,
                            0.28f to OrientDeepNavy.copy(alpha = 0.55f),
                            1.0f to Color.Transparent
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Transparent,
                            0.70f to Color.Transparent,
                            1.0f to OrientMidnightDark
                        )
                    )
            )
        }

        // Main Foreground Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP BAR: Orient Player Logo + Tagline on Left | MAC Gradient Pill + Wi-Fi + Clock + Date + Settings on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onOpenMacDialog() }
                        .testTag("brand_logo_header")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_orient_brand_logo),
                        contentDescription = "Orient Player Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    SpanStyle(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append("Orient ")
                                }
                                withStyle(
                                    SpanStyle(
                                        brush = OrientGradients.PrimaryButtonBrush,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontStyle = FontStyle.Italic
                                    )
                                ) {
                                    append("Player")
                                }
                            },
                            fontSize = 24.sp,
                            lineHeight = 26.sp
                        )
                        Text(
                            text = "YOUR MEDIA. YOUR WORLD",
                            color = OrientTextSecondary,
                            fontSize = 9.sp,
                            letterSpacing = 1.6.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right Status Area: Active Electric Blue/Turquoise MAC Badge | Wi-Fi | Clock | Date | Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(OrientGradients.CardActiveSurfaceBrush)
                            .border(
                                width = 1.dp,
                                brush = OrientGradients.ActiveBorderBrush,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onOpenMacDialog() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("home_mac_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "MAC Virtuelle",
                                tint = OrientTurquoise,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = virtualMac,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi Connecté",
                        tint = OrientTurquoise,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = "|",
                        color = OrientBorderBlue,
                        fontSize = 16.sp
                    )

                    Text(
                        text = currentTimeStr,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentDateStr,
                        color = OrientTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrientGradients.PrimaryButtonBrush)
                            .testTag("home_settings_gear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. HERO HEADLINE ("Le meilleur du divertissement" + Electric Blue/Turquoise Gradient Bar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Le meilleur du divertissement",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Films • Séries • TV en direct • Radios",
                    color = OrientTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OrientGradients.PrimaryButtonBrush)
                )
            }

            // 3. THE 6 CATEGORY CARDS ROW (Electric Blue & Turquoise Active Focus Glow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categoryCards.forEach { card ->
                    val isSelected = selectedCardIndex == card.index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.03f else 1.0f,
                        label = "cardScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(if (isSelected) 1.12f else 1f)
                            .fillMaxHeight()
                            .scale(scale)
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(
                                            elevation = 16.dp,
                                            shape = RoundedCornerShape(14.dp),
                                            ambientColor = OrientTurquoise,
                                            spotColor = OrientElectricBlue
                                        )
                                        .border(
                                            width = 2.2.dp,
                                            brush = OrientGradients.ActiveBorderBrush,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                } else {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = OrientBorderBlue,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectCard(card.index, card.targetTab) }
                            .testTag("home_card_${card.targetTab.name}")
                    ) {
                        Image(
                            painter = painterResource(id = card.artworkRes),
                            contentDescription = "${card.title} - ${card.subtitle}",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 4. "Recommandé pour vous" SECTION + Electric Blue/Turquoise Pill "Voir tout >"
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(OrientGradients.PrimaryButtonBrush)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recommandé pour vous",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(OrientGradients.CardActiveSurfaceBrush)
                            .border(1.dp, OrientTurquoiseGlow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onSeeAllRecommended() }
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                            .testTag("see_all_recommended_btn")
                    ) {
                        Text(
                            text = "Voir tout",
                            color = OrientTurquoise,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Voir tout",
                            tint = OrientTurquoise,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 8 Poster Cards Row with High-Contrast Navy/Turquoise Borders
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(106.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(recommendedItems, key = { it.id }) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1.02f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = 1.dp,
                                    color = OrientBorderBlue,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onPlayRecommended(item) }
                                .testTag("rec_poster_${item.id}")
                        ) {
                            Image(
                                painter = painterResource(id = item.posterRes),
                                contentDescription = "${item.title} (${item.categoryBadge})",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
