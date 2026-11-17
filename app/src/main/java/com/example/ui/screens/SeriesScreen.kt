package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.EpisodeEntity
import com.example.data.local.SeriesEntity
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientGradientButton
import com.example.ui.theme.OrientGradientPill
import com.example.ui.theme.OrientGradients
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientSurfaceNavy
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise

@Composable
fun SeriesScreen(
    seriesList: List<SeriesEntity>,
    allEpisodes: List<EpisodeEntity>,
    selectedCategory: String,
    searchQuery: String,
    selectedSeriesDetail: SeriesEntity?,
    onSelectCategory: (String) -> Unit,
    onOpenSeriesDetail: (SeriesEntity?) -> Unit,
    onPlayEpisode: (SeriesEntity, EpisodeEntity, Boolean) -> Unit,
    onToggleFavorite: (SeriesEntity) -> Unit
) {
    val categories = remember(seriesList) {
        val dynamic = seriesList.map { it.category }.distinct()
        listOf("Toutes", "Favoris") + dynamic
    }

    val filteredSeries = remember(seriesList, selectedCategory, searchQuery) {
        val byCat = when (selectedCategory) {
            "Toutes" -> seriesList
            "Favoris" -> seriesList.filter { it.isFavorite }
            else -> seriesList.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
        if (searchQuery.isBlank()) {
            byCat
        } else {
            byCat.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.genre.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.testTag("series_category_chip_$category")
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("series_poster_grid")
        ) {
            items(filteredSeries, key = { it.id }) { series ->
                SeriesPosterCard(
                    series = series,
                    onClick = { onOpenSeriesDetail(series) },
                    onToggleFavorite = { onToggleFavorite(series) }
                )
            }
        }
    }

    if (selectedSeriesDetail != null) {
        val episodesForSeries = remember(allEpisodes, selectedSeriesDetail.id) {
            allEpisodes.filter { it.seriesId == selectedSeriesDetail.id }
                .sortedWith(compareBy<EpisodeEntity> { it.seasonNumber }.thenBy { it.episodeNumber })
        }
        SeriesDetailDialog(
            series = selectedSeriesDetail,
            episodes = episodesForSeries,
            onDismiss = { onOpenSeriesDetail(null) },
            onPlayEpisode = { episode, resume ->
                onOpenSeriesDetail(null)
                onPlayEpisode(selectedSeriesDetail, episode, resume)
            },
            onToggleFavorite = { onToggleFavorite(selectedSeriesDetail) }
        )
    }
}

@Composable
fun SeriesPosterCard(
    series: SeriesEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("series_card_${series.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
            ) {
                AsyncImage(
                    model = series.posterUrl,
                    contentDescription = series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OrientElectricBlue,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${series.seasonsCount} Saisons",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(30.dp)
                            .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (series.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favori",
                            tint = if (series.isFavorite) OrientGold else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "★ ${series.rating} • ${series.genre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrientTurquoise,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SeriesDetailDialog(
    series: SeriesEntity,
    episodes: List<EpisodeEntity>,
    onDismiss: () -> Unit,
    onPlayEpisode: (EpisodeEntity, Boolean) -> Unit,
    onToggleFavorite: () -> Unit
) {
    val seasons = remember(episodes) {
        episodes.map { it.seasonNumber }.distinct().sorted().ifEmpty { listOf(1) }
    }
    var selectedSeason by remember { mutableIntStateOf(seasons.first()) }
    val seasonEpisodes = remember(episodes, selectedSeason) {
        episodes.filter { it.seasonNumber == selectedSeason }
    }

    val resumeEpisode = remember(episodes, series.lastWatchedEpisodeId) {
        episodes.find { it.id == series.lastWatchedEpisodeId }
            ?: episodes.find { it.resumePositionMs > 0L }
            ?: episodes.firstOrNull()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 800.dp)
                .height(620.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, OrientGradients.ActiveBorderBrush, RoundedCornerShape(24.dp)),
            color = OrientMidnightDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Backdrop
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    AsyncImage(
                        model = series.backdropUrl,
                        contentDescription = series.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.35f), OrientMidnightDark)
                                )
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                            .testTag("close_series_detail_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "${series.year} • ${series.seasonsCount} Saisons • ★ ${series.rating} • Passage auto à l'épisode suivant actif",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrientTurquoise
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = series.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = series.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = OrientTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Resume + Season Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        seasons.forEach { seasonNum ->
                            OrientGradientPill(
                                selected = selectedSeason == seasonNum,
                                onClick = { selectedSeason = seasonNum },
                                label = "Saison $seasonNum",
                                modifier = Modifier.testTag("season_tab_$seasonNum")
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (resumeEpisode != null) {
                            OrientGradientButton(
                                text = "Reprendre S${resumeEpisode.seasonNumber}E${resumeEpisode.episodeNumber}",
                                onClick = { onPlayEpisode(resumeEpisode, true) },
                                leadingIcon = Icons.Default.PlayArrow,
                                modifier = Modifier.testTag("resume_series_button")
                            )
                        }

                        OutlinedButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (series.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favori",
                                tint = if (series.isFavorite) OrientGold else Color.White
                            )
                        }
                    }
                }

                // Episodes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(seasonEpisodes, key = { it.id }) { ep ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OrientCardNavy),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
                                .clickable { onPlayEpisode(ep, true) }
                                .testTag("episode_card_s${ep.seasonNumber}e${ep.episodeNumber}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(OrientDeepNavy)
                                ) {
                                    AsyncImage(
                                        model = ep.thumbnailUrl,
                                        contentDescription = ep.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .align(Alignment.Center)
                                            .background(OrientElectricBlue.copy(alpha = 0.85f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (ep.resumePositionMs > 0L && ep.totalDurationMs > 0L) {
                                        val progress = (ep.resumePositionMs.toFloat() / ep.totalDurationMs.toFloat()).coerceIn(0.05f, 0.98f)
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            color = OrientTurquoise,
                                            trackColor = Color.Black.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .align(Alignment.BottomCenter)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = ep.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${ep.durationMinutes} min",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrientTurquoise
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ep.overview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrientTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
