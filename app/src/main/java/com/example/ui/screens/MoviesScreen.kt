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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.VodMovieEntity
import com.example.ui.theme.OrientBorderBlue
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
fun MoviesScreen(
    movies: List<VodMovieEntity>,
    selectedCategory: String,
    searchQuery: String,
    selectedMovieDetail: VodMovieEntity?,
    onSelectCategory: (String) -> Unit,
    onOpenMovieDetail: (VodMovieEntity?) -> Unit,
    onPlayMovie: (VodMovieEntity, Boolean) -> Unit,
    onToggleFavorite: (VodMovieEntity) -> Unit
) {
    val categories = remember(movies) {
        val dynamic = movies.map { it.category }.distinct()
        listOf("Tous", "Reprise de lecture", "Favoris") + dynamic
    }

    val filteredMovies = remember(movies, selectedCategory, searchQuery) {
        val byCat = when (selectedCategory) {
            "Tous" -> movies
            "Favoris" -> movies.filter { it.isFavorite }
            "Reprise de lecture" -> movies.filter { it.resumePositionMs > 0L }
            else -> movies.filter { it.category.equals(selectedCategory, ignoreCase = true) }
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
                    modifier = Modifier.testTag("movie_category_chip_$category")
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 155.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("movies_poster_grid")
        ) {
            items(filteredMovies, key = { it.id }) { movie ->
                MoviePosterCard(
                    movie = movie,
                    onClick = { onOpenMovieDetail(movie) },
                    onToggleFavorite = { onToggleFavorite(movie) }
                )
            }
        }
    }

    if (selectedMovieDetail != null) {
        MovieDetailDialog(
            movie = selectedMovieDetail,
            onDismiss = { onOpenMovieDetail(null) },
            onPlayFromStart = {
                onOpenMovieDetail(null)
                onPlayMovie(selectedMovieDetail, false)
            },
            onResumePlayback = {
                onOpenMovieDetail(null)
                onPlayMovie(selectedMovieDetail, true)
            },
            onToggleFavorite = { onToggleFavorite(selectedMovieDetail) }
        )
    }
}

@Composable
fun MoviePosterCard(
    movie: VodMovieEntity,
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
            .testTag("movie_card_${movie.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f)
            ) {
                AsyncImage(
                    model = movie.posterUrl,
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Rating & Favorite
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OrientMidnightDark.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = OrientGold, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${movie.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(30.dp)
                            .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (movie.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favori",
                            tint = if (movie.isFavorite) OrientGold else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Resume Progress Bar if partially watched
                if (movie.resumePositionMs > 0L && movie.totalDurationMs > 0L) {
                    val progress = (movie.resumePositionMs.toFloat() / movie.totalDurationMs.toFloat()).coerceIn(0.05f, 0.98f)
                    LinearProgressIndicator(
                        progress = { progress },
                        color = OrientTurquoise,
                        trackColor = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .align(Alignment.BottomCenter)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${movie.year} • ${movie.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = OrientTurquoise
                )
            }
        }
    }
}

@Composable
private fun MovieDetailDialog(
    movie: VodMovieEntity,
    onDismiss: () -> Unit,
    onPlayFromStart: () -> Unit,
    onResumePlayback: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 740.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, OrientGradients.ActiveBorderBrush, RoundedCornerShape(24.dp)),
            color = OrientMidnightDark
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Hero Backdrop Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = movie.backdropUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        OrientMidnightDark
                                    )
                                )
                            )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(OrientMidnightDark.copy(alpha = 0.75f), CircleShape)
                            .testTag("close_movie_detail_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = OrientElectricBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${movie.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "★ ${movie.rating} • ${movie.durationMinutes} min • ${movie.genre}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OrientTurquoise
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Synopsis, Metadata & Playback Buttons
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        text = movie.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Réalisateur : ${movie.director}  •  Distribution : ${movie.cast}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OrientTextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (movie.resumePositionMs > 5_000L) {
                            val resumeMin = (movie.resumePositionMs / 60000L).coerceAtLeast(1L)
                            Button(
                                onClick = onResumePlayback,
                                colors = ButtonDefaults.buttonColors(containerColor = OrientTurquoise),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("resume_movie_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OrientMidnightDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reprendre ($resumeMin min)",
                                    color = OrientMidnightDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onPlayFromStart,
                                modifier = Modifier.testTag("restart_movie_button")
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Depuis le début", color = Color.White)
                            }
                        } else {
                            OrientGradientButton(
                                text = "Regarder le Film (UHD)",
                                onClick = onPlayFromStart,
                                leadingIcon = Icons.Default.PlayArrow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("play_movie_button")
                            )
                        }

                        OutlinedButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.testTag("favorite_movie_detail_button")
                        ) {
                            Icon(
                                imageVector = if (movie.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favoris",
                                tint = if (movie.isFavorite) OrientGold else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (movie.isFavorite) "Favori" else "Ajouter", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
