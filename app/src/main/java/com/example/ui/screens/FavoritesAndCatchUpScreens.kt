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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.CatchUpProgramEntity
import com.example.data.local.LiveChannelEntity
import com.example.data.local.SeriesEntity
import com.example.data.local.VodMovieEntity
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientCardNavy
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientSurfaceNavy
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise

@Composable
fun FavoritesScreen(
    channels: List<LiveChannelEntity>,
    movies: List<VodMovieEntity>,
    seriesList: List<SeriesEntity>,
    onPlayChannel: (LiveChannelEntity) -> Unit,
    onOpenMovie: (VodMovieEntity) -> Unit,
    onOpenSeries: (SeriesEntity) -> Unit,
    onToggleChannelFav: (LiveChannelEntity) -> Unit,
    onToggleMovieFav: (VodMovieEntity) -> Unit,
    onToggleSeriesFav: (SeriesEntity) -> Unit
) {
    val favChannels = remember(channels) { channels.filter { it.isFavorite } }
    val favMovies = remember(movies) { movies.filter { it.isFavorite } }
    val favSeries = remember(seriesList) { seriesList.filter { it.isFavorite } }
    val resumeMovies = remember(movies) { movies.filter { it.resumePositionMs > 0L } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("favorites_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section 1: Favorite Live TV Channels
        item {
            SectionHeader(
                title = "Chaînes TV Favorites (${favChannels.size})",
                subtitle = "Accès direct à vos chaînes en direct préférées"
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (favChannels.isEmpty()) {
                EmptyFavoriteCard("Aucune chaîne favorite. Cliquez sur l'étoile ★ dans LIVE TV pour en ajouter.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(favChannels, key = { it.id }) { channel ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .width(230.dp)
                                .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
                                .clickable { onPlayChannel(channel) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = channel.logoUrl,
                                    contentDescription = channel.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = channel.currentProgramTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OrientTurquoise,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Resume Watching (VOD)
        if (resumeMovies.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Reprise de Lecture (${resumeMovies.size})",
                    subtitle = "Reprenez vos films exactement là où vous vous êtes arrêté"
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(resumeMovies, key = { it.id }) { movie ->
                        Box(modifier = Modifier.width(160.dp)) {
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onOpenMovie(movie) },
                                onToggleFavorite = { onToggleMovieFav(movie) }
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Favorite Movies
        item {
            SectionHeader(
                title = "Films Favoris (${favMovies.size})",
                subtitle = "Votre sélection cinéma Ultra HD"
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (favMovies.isEmpty()) {
                EmptyFavoriteCard("Aucun film favori sélectionné.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(favMovies, key = { it.id }) { movie ->
                        Box(modifier = Modifier.width(160.dp)) {
                            MoviePosterCard(
                                movie = movie,
                                onClick = { onOpenMovie(movie) },
                                onToggleFavorite = { onToggleMovieFav(movie) }
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Favorite Series
        item {
            SectionHeader(
                title = "Séries Favorites (${favSeries.size})",
                subtitle = "Vos séries suivies avec passage automatique à l'épisode suivant"
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (favSeries.isEmpty()) {
                EmptyFavoriteCard("Aucune série favorite sélectionnée.")
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(favSeries, key = { it.id }) { series ->
                        Box(modifier = Modifier.width(165.dp)) {
                            SeriesPosterCard(
                                series = series,
                                onClick = { onOpenSeries(series) },
                                onToggleFavorite = { onToggleSeriesFav(series) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatchUpScreen(
    programs: List<CatchUpProgramEntity>,
    searchQuery: String,
    onPlayCatchUp: (CatchUpProgramEntity) -> Unit
) {
    val dayFilters = listOf("Tous les jours", "Aujourd'hui", "Hier", "Il y a 2 jours")
    var selectedDay by remember { mutableStateOf("Tous les jours") }

    val filtered = remember(programs, selectedDay, searchQuery) {
        val byDay = if (selectedDay == "Tous les jours") {
            programs
        } else {
            programs.filter { it.dayLabel.equals(selectedDay, ignoreCase = true) }
        }
        if (searchQuery.isBlank()) {
            byDay
        } else {
            byDay.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                    it.channelName.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("catchup_screen")
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dayFilters) { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    label = { Text(day, style = MaterialTheme.typography.labelLarge) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrientElectricBlue,
                        selectedLabelColor = Color.White,
                        containerColor = OrientSurfaceNavy,
                        labelColor = OrientTextSecondary
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { prog ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrientBorderBlue, RoundedCornerShape(16.dp))
                        .clickable { onPlayCatchUp(prog) }
                        .testTag("catchup_card_${prog.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = prog.channelLogo,
                            contentDescription = prog.channelName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = OrientTurquoise.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${prog.dayLabel} • ${prog.timeSlot}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrientTurquoise,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prog.channelName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OrientGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prog.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = prog.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onPlayCatchUp(prog) },
                            colors = ButtonDefaults.buttonColors(containerColor = OrientElectricBlue),
                            modifier = Modifier.testTag("play_catchup_btn_${prog.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replay")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Star, contentDescription = null, tint = OrientGold, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
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

@Composable
private fun EmptyFavoriteCard(message: String) {
    Surface(
        color = OrientCardNavy.copy(alpha = 0.6f),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, OrientBorderBlue, RoundedCornerShape(14.dp))
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = OrientTextSecondary,
            modifier = Modifier.padding(16.dp)
        )
    }
}
