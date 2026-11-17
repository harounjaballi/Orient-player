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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.example.data.local.LiveChannelEntity
import com.example.data.local.SeriesEntity
import com.example.data.local.VodMovieEntity
import com.example.ui.theme.CardRadiosEnd
import com.example.ui.theme.CardRadiosStart
import com.example.ui.theme.OrientBorderBlue
import com.example.ui.theme.OrientBrandPurple
import com.example.ui.theme.OrientDeepNavy
import com.example.ui.theme.OrientElectricBlue
import com.example.ui.theme.OrientGold
import com.example.ui.theme.OrientMidnightDark
import com.example.ui.theme.OrientTextSecondary
import com.example.ui.theme.OrientTurquoise
import com.example.ui.viewmodel.RadioStationItem

@Composable
fun RadiosScreen(
    stations: List<RadioStationItem>,
    searchQuery: String,
    onPlayStation: (RadioStationItem) -> Unit
) {
    val filtered = remember(stations, searchQuery) {
        if (searchQuery.isBlank()) {
            stations
        } else {
            stations.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                    it.genre.contains(searchQuery, ignoreCase = true) ||
                    it.currentTrack.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("radios_screen")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 260.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.id }) { station ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrientDeepNavy),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OrientGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable { onPlayStation(station) }
                        .testTag("radio_card_${station.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        CardRadiosEnd.copy(alpha = 0.65f),
                                        OrientDeepNavy
                                    )
                                )
                            )
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardRadiosStart),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = station.logoUrl,
                                contentDescription = station.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Radio,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = OrientGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = station.frequencyOrBitrate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OrientGold
                                )
                            }
                            Text(
                                text = station.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = station.genre,
                                style = MaterialTheme.typography.bodySmall,
                                color = OrientTurquoise,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "♪ ${station.currentTrack}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OrientTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onPlayStation(station) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(OrientGold, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Écouter",
                                tint = OrientMidnightDark
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalSearchScreen(
    searchQuery: String,
    channels: List<LiveChannelEntity>,
    movies: List<VodMovieEntity>,
    seriesList: List<SeriesEntity>,
    radios: List<RadioStationItem>,
    onSearchChange: (String) -> Unit,
    onPlayChannel: (LiveChannelEntity) -> Unit,
    onOpenMovie: (VodMovieEntity) -> Unit,
    onOpenSeries: (SeriesEntity) -> Unit,
    onPlayRadio: (RadioStationItem) -> Unit
) {
    val matchedChannels = remember(channels, searchQuery) {
        if (searchQuery.isBlank()) channels.take(6)
        else channels.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.currentProgramTitle.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true)
        }
    }
    val matchedMovies = remember(movies, searchQuery) {
        if (searchQuery.isBlank()) movies
        else movies.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.genre.contains(searchQuery, ignoreCase = true)
        }
    }
    val matchedSeries = remember(seriesList, searchQuery) {
        if (searchQuery.isBlank()) seriesList
        else seriesList.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.genre.contains(searchQuery, ignoreCase = true)
        }
    }
    val matchedRadios = remember(radios, searchQuery) {
        if (searchQuery.isBlank()) radios.take(4)
        else radios.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                it.genre.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("global_search_screen")
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = OrientBrandPurple)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = Color.White)
                    }
                }
            },
            placeholder = {
                Text("Tapez le nom d'une chaîne TV, d'un film, d'une série ou d'une radio…")
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrientBrandPurple,
                unfocusedBorderColor = OrientBorderBlue,
                focusedContainerColor = OrientDeepNavy,
                unfocusedContainerColor = OrientDeepNavy
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_screen_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (matchedChannels.isNotEmpty()) {
                item {
                    Text(
                        text = "TV en direct (${matchedChannels.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(matchedChannels, key = { "s_ch_${it.id}" }) { ch ->
                            Surface(
                                color = OrientDeepNavy,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(220.dp)
                                    .border(1.dp, OrientBorderBlue, RoundedCornerShape(12.dp))
                                    .clickable { onPlayChannel(ch) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ch.logoUrl,
                                        contentDescription = ch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = ch.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = ch.currentProgramTitle,
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

            if (matchedMovies.isNotEmpty()) {
                item {
                    Text(
                        text = "Films (${matchedMovies.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(matchedMovies, key = { "s_mv_${it.id}" }) { movie ->
                            Box(modifier = Modifier.width(145.dp)) {
                                MoviePosterCard(
                                    movie = movie,
                                    onClick = { onOpenMovie(movie) },
                                    onToggleFavorite = {}
                                )
                            }
                        }
                    }
                }
            }

            if (matchedSeries.isNotEmpty()) {
                item {
                    Text(
                        text = "Séries (${matchedSeries.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(matchedSeries, key = { "s_sr_${it.id}" }) { series ->
                            Box(modifier = Modifier.width(150.dp)) {
                                SeriesPosterCard(
                                    series = series,
                                    onClick = { onOpenSeries(series) },
                                    onToggleFavorite = {}
                                )
                            }
                        }
                    }
                }
            }

            if (matchedRadios.isNotEmpty()) {
                item {
                    Text(
                        text = "Radios (${matchedRadios.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(matchedRadios, key = { "s_rd_${it.id}" }) { radio ->
                            Button(
                                onClick = { onPlayRadio(radio) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrientDeepNavy),
                                modifier = Modifier.border(1.dp, OrientGold, RoundedCornerShape(24.dp))
                            ) {
                                Icon(Icons.Default.Radio, contentDescription = null, tint = OrientGold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(radio.name, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
