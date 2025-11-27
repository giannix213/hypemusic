package com.metu.hypematch

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

// ============================================
// COMPONENTES PARA TU MÚSICA
// ============================================

// Componente de video de fondo para el ecualizador
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoEqualizerBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    videoResId: Int = R.raw.ecualizador_video // Nombre de tu video en res/raw/
) {
    val context = LocalContext.current
    
    // Crear ExoPlayer para el video
    val videoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Configurar para loop infinito
            repeatMode = Player.REPEAT_MODE_ONE
            // Sin audio (solo visual)
            volume = 0f
            
            // Cargar video desde recursos
            val videoUri = Uri.parse("android.resource://${context.packageName}/$videoResId")
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    
    // Controlar reproducción según estado
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            videoPlayer.play()
        } else {
            videoPlayer.pause()
        }
    }
    
    // Limpiar cuando se destruye
    DisposableEffect(Unit) {
        onDispose {
            videoPlayer.release()
        }
    }
    
    // Vista del video
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = videoPlayer
                useController = false // Sin controles
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Llenar el espacio
                
                // Ocultar elementos de UI
                hideController()
            }
        },
        modifier = modifier
    )
}

// Componente de ecualizador animado simple
@Composable
fun AnimatedEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    color: Color = PopArtColors.Yellow
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "equalizer_$index")
            val height by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (300..600).random(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isPlaying) (20.dp * height) else 4.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

// Ecualizador tipo Spotify con efecto de onda
@Composable
fun SpotifyStyleEqualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 40,
    color: Color = PopArtColors.Yellow,
    maxHeight: Float = 60f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "spotify_eq_$index")
            
            // Crear efecto de onda con diferentes fases
            val phase = (index.toFloat() / barCount) * 2 * Math.PI.toFloat()
            
            val height by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = (400 + (index % 5) * 100),
                        easing = FastOutSlowInEasing,
                        delayMillis = (index * 20) % 200
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_height_$index"
            )
            
            // Altura con efecto de onda sinusoidal
            val waveHeight = if (isPlaying) {
                (kotlin.math.sin(phase + height * Math.PI.toFloat()) * 0.5f + 0.5f) * height
            } else {
                0.1f
            }
            
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((maxHeight * waveHeight.coerceIn(0.1f, 1f)).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
    }
}

// Efecto de onda de fondo animado
@Composable
fun AnimatedWaveBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_bg")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    if (isPlaying) {
        Box(
            modifier = modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PopArtColors.Pink.copy(alpha = 0.1f),
                            PopArtColors.Yellow.copy(alpha = 0.1f),
                            PopArtColors.Cyan.copy(alpha = 0.1f)
                        ),
                        start = androidx.compose.ui.geometry.Offset(offset, offset),
                        end = androidx.compose.ui.geometry.Offset(offset + 500f, offset + 500f)
                    )
                )
        )
    }
}

// Círculo de historia de artista
@Composable
fun StoryCircle(
    story: ArtistStory,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anillo de gradiente si no ha visto la historia
            if (!story.isViewed) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PopArtColors.Pink,
                                    PopArtColors.Yellow,
                                    PopArtColors.Cyan
                                )
                            )
                        )
                )
            }
            
            // Foto del artista
            if (story.artistImageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = story.artistImageUrl,
                    contentDescription = story.artistName,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(PopArtColors.White),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(PopArtColors.Cyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        story.artistName.firstOrNull()?.toString() ?: "?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Black
                    )
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            story.artistName.take(8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PopArtColors.White,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

// Función auxiliar para formatear tiempo
fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}

// Tarjeta de canción estilo Spotify
@Composable
fun SpotifyStyleSongCard(
    song: ArtistCard,
    isPlaying: Boolean,
    size: androidx.compose.ui.unit.Dp,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val cardScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "spotify_card_scale"
    )
    
    Column(
        modifier = Modifier
            .width(size)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        // Portada cuadrada con ecualizador
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(8.dp))
                .background(PopArtColors.Black),
            contentAlignment = Alignment.Center
        ) {
            if (song.imageUrl.isNotEmpty()) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(context)
                        .data(song.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = song.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PopArtColors.MulticolorGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", fontSize = (size.value / 3).sp)
                }
            }
            
            // Overlay oscuro cuando está reproduciendo
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PopArtColors.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedEqualizer(
                        isPlaying = true,
                        barCount = 5,
                        color = PopArtColors.Yellow
                    )
                }
            }
            
            // Botón de play flotante en la esquina inferior derecha
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(40.dp)
                        .background(PopArtColors.Yellow, CircleShape)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PopArtColors.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Nombre de la canción
        Text(
            song.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.White,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(2.dp))
        
        // Género y ubicación
        Text(
            "${song.genre} • ${song.location}",
            fontSize = 11.sp,
            color = PopArtColors.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// PANTALLA: TU MÚSICA
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMusicScreen(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val player = rememberMusicPlayer(context, pauseOnBackground = false)
    val firebaseManager = remember { FirebaseManager() }
    val songLikesManager = remember { SongLikesManager() }
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()
    
    val userId = authManager.getUserId() ?: ""
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Favoritos, 1 = Siguiendo
    var likedSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var followingSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var filteredSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var currentPlayingIndex by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var stories by remember { mutableStateOf<List<ArtistStory>>(emptyList()) }
    var showStoryViewer by remember { mutableStateOf(false) }
    var selectedStoryIndex by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Función para refrescar datos
    suspend fun refreshData() {
        if (userId.isNotEmpty()) {
            try {
                // Cargar canciones con like
                likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
                // Cargar canciones de artistas que sigue
                followingSongs = firebaseManager.getSongsFromFollowing(userId)
                // Cargar estados
                stories = firebaseManager.getStoriesFromLikedArtists(userId, songLikesManager)
                
                android.util.Log.d("MyMusicScreen", "✅ Datos actualizados")
                filteredSongs = if (selectedTab == 0) likedSongs else followingSongs
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error actualizando: ${e.message}")
            }
        }
    }
    

    
    // Cargar canciones que le gustaron al usuario desde Firebase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                isLoading = true
                // Cargar canciones con like
                likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
                // Cargar canciones de artistas que sigue
                followingSongs = firebaseManager.getSongsFromFollowing(userId)
                
                android.util.Log.d("MyMusicScreen", "✅ Canciones favoritas: ${likedSongs.size}")
                android.util.Log.d("MyMusicScreen", "✅ Canciones de siguiendo: ${followingSongs.size}")
                
                filteredSongs = if (selectedTab == 0) likedSongs else followingSongs
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error cargando canciones: ${e.message}")
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    // Cargar estados de artistas a los que les diste like
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                stories = firebaseManager.getStoriesFromLikedArtists(userId, songLikesManager)
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error cargando estados: ${e.message}")
            }
        }
    }
    
    // Actualizar canciones filtradas cuando cambia la pestaña
    LaunchedEffect(selectedTab, likedSongs, followingSongs) {
        filteredSongs = if (selectedTab == 0) likedSongs else followingSongs
    }
    
    // Filtrar canciones según búsqueda
    LaunchedEffect(searchQuery, selectedTab, likedSongs, followingSongs) {
        val sourceSongs = if (selectedTab == 0) likedSongs else followingSongs
        filteredSongs = if (searchQuery.isEmpty()) {
            sourceSongs
        } else {
            sourceSongs.filter { song ->
                song.name.contains(searchQuery, ignoreCase = true) ||
                song.genre.contains(searchQuery, ignoreCase = true) ||
                song.location.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Actualizar posición y duración
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = player.currentPosition
            duration = player.duration
            kotlinx.coroutines.delay(100)
        }
    }

    DisposableEffectWithLifecycle(player)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
        // Header con título y botón de búsqueda
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "TU MÚSICA",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow
            )
            
            // Botón de búsqueda
            IconButton(
                onClick = { isSearching = !isSearching },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSearching) PopArtColors.Yellow else PopArtColors.White,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = if (isSearching) PopArtColors.Black else PopArtColors.Yellow,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Pestañas: Favoritos / Siguiendo con animación
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pestaña Favoritos
            val favoritosScale by animateFloatAsState(
                targetValue = if (selectedTab == 0) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "favoritos_scale"
            )
            
            Button(
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = favoritosScale
                        scaleY = favoritosScale
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (selectedTab == 0) 8.dp else 0.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Favoritos",
                        fontSize = if (selectedTab == 0) 16.sp else 14.sp,
                        fontWeight = if (selectedTab == 0) FontWeight.Black else FontWeight.Bold,
                        color = if (selectedTab == 0) PopArtColors.Black else PopArtColors.White.copy(alpha = 0.7f)
                    )
                    if (selectedTab == 0 && likedSongs.isNotEmpty()) {
                        Text(
                            "${likedSongs.size} canciones",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Pestaña Siguiendo
            val siguiendoScale by animateFloatAsState(
                targetValue = if (selectedTab == 1) 1.05f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "siguiendo_scale"
            )
            
            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = siguiendoScale
                        scaleY = siguiendoScale
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (selectedTab == 1) 8.dp else 0.dp
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Siguiendo",
                        fontSize = if (selectedTab == 1) 16.sp else 14.sp,
                        fontWeight = if (selectedTab == 1) FontWeight.Black else FontWeight.Bold,
                        color = if (selectedTab == 1) PopArtColors.Black else PopArtColors.White.copy(alpha = 0.7f)
                    )
                    if (selectedTab == 1 && followingSongs.isNotEmpty()) {
                        Text(
                            "${followingSongs.size} canciones",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        // Barra de búsqueda (se muestra cuando isSearching es true)
        if (isSearching) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text("Buscar por artista, género o ubicación...", fontSize = 13.sp, color = colors.text.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = colors.text)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.text,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
        } else {
            Text(
                if (selectedTab == 0) "Canciones que te gustaron 🎵" else "Canciones de artistas que sigues 👥",
                fontSize = 15.sp,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Sección de Estados de artistas que sigues
        if (stories.isNotEmpty() && !isSearching) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(stories.size) { index ->
                    val story = stories[index]
                    StoryCircle(
                        story = story,
                        onClick = {
                            selectedStoryIndex = index
                            showStoryViewer = true
                        }
                    )
                }
            }
        }

        // Visor de estados
        if (showStoryViewer && stories.isNotEmpty()) {
            StoryViewerScreen(
                stories = stories,
                startIndex = selectedStoryIndex,
                userId = userId,
                onDismiss = { showStoryViewer = false },
                onStoryViewed = { storyId ->
                    scope.launch {
                        try {
                            firebaseManager.markStoryAsViewed(storyId, userId)
                        } catch (e: Exception) {
                            android.util.Log.e("MyMusicScreen", "Error marcando estado: ${e.message}")
                        }
                    }
                }
            )
        }

        // Contenido
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if ((selectedTab == 0 && likedSongs.isEmpty()) || (selectedTab == 1 && followingSongs.isEmpty())) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (selectedTab == 0) "💔" else "👥",
                        fontSize = 64.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (selectedTab == 0) "No tienes favoritos aún" else "No sigues a nadie aún",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (selectedTab == 0) 
                            "Dale ❤️ a las canciones que te gusten en Descubre"
                        else
                            "Sigue a artistas para ver sus canciones aquí",
                        fontSize = 13.sp,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else if (filteredSongs.isEmpty()) {
            // No hay resultados de búsqueda
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", fontSize = 64.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No se encontraron resultados",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Yellow
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Intenta con otra búsqueda",
                        fontSize = 13.sp,
                        color = PopArtColors.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Vista tipo Spotify con carruseles horizontales
            val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
            
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    scope.launch {
                        isRefreshing = true
                        refreshData()
                        isRefreshing = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                // Sección: Tus mixes más escuchados
                item {
                    Column {
                        Text(
                            "Tus mixes más escuchados",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(filteredSongs.take(6).size) { index ->
                                val song = filteredSongs[index]
                                val actualIndex = if (selectedTab == 0) likedSongs.indexOf(song) else followingSongs.indexOf(song)
                                val isCurrentlyPlaying = currentPlayingIndex == actualIndex && isPlaying
                                
                                SpotifyStyleSongCard(
                                    song = song,
                                    isPlaying = isCurrentlyPlaying,
                                    size = 160.dp,
                                    context = context,
                                    onClick = {
                                        if (currentPlayingIndex == actualIndex) {
                                            if (isPlaying) {
                                                player.pause()
                                                isPlaying = false
                                            } else {
                                                player.play()
                                                isPlaying = true
                                            }
                                        } else {
                                            player.stop()
                                            player.clearMediaItems()
                                            val mediaItem = MediaItem
                                                .Builder()
                                                .setUri(Uri.parse(song.songUrl))
                                                .build()
                                            player.setMediaItem(mediaItem)
                                            player.prepare()
                                            player.play()
                                            currentPlayingIndex = actualIndex
                                            isPlaying = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Sección: Recientes
                if (filteredSongs.size > 6) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Recientes",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopArtColors.White
                                )
                                Text(
                                    "Mostrar todo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PopArtColors.Yellow,
                                    modifier = Modifier.clickable { /* TODO: Mostrar todas */ }
                                )
                            }
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredSongs.drop(6).take(8).size) { index ->
                                    val song = filteredSongs[index + 6]
                                    val actualIndex = if (selectedTab == 0) likedSongs.indexOf(song) else followingSongs.indexOf(song)
                                    val isCurrentlyPlaying = currentPlayingIndex == actualIndex && isPlaying
                                    
                                    SpotifyStyleSongCard(
                                        song = song,
                                        isPlaying = isCurrentlyPlaying,
                                        size = 120.dp,
                                        context = context,
                                        onClick = {
                                            if (currentPlayingIndex == actualIndex) {
                                                if (isPlaying) {
                                                    player.pause()
                                                    isPlaying = false
                                                } else {
                                                    player.play()
                                                    isPlaying = true
                                                }
                                            } else {
                                                player.stop()
                                                player.clearMediaItems()
                                                val mediaItem = MediaItem
                                                    .Builder()
                                                    .setUri(Uri.parse(song.songUrl))
                                                    .build()
                                                player.setMediaItem(mediaItem)
                                                player.prepare()
                                                player.play()
                                                currentPlayingIndex = actualIndex
                                                isPlaying = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Sección: Los éxitos del momento
                if (filteredSongs.size > 14) {
                    item {
                        Column {
                            Text(
                                "Los éxitos del momento",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.White,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(filteredSongs.drop(14).size) { index ->
                                    val song = filteredSongs[index + 14]
                                    val actualIndex = if (selectedTab == 0) likedSongs.indexOf(song) else followingSongs.indexOf(song)
                                    val isCurrentlyPlaying = currentPlayingIndex == actualIndex && isPlaying
                                    
                                    SpotifyStyleSongCard(
                                        song = song,
                                        isPlaying = isCurrentlyPlaying,
                                        size = 160.dp,
                                        context = context,
                                        onClick = {
                                            if (currentPlayingIndex == actualIndex) {
                                                if (isPlaying) {
                                                    player.pause()
                                                    isPlaying = false
                                                } else {
                                                    player.play()
                                                    isPlaying = true
                                                }
                                            } else {
                                                player.stop()
                                                player.clearMediaItems()
                                                val mediaItem = MediaItem
                                                    .Builder()
                                                    .setUri(Uri.parse(song.songUrl))
                                                    .build()
                                                player.setMediaItem(mediaItem)
                                                player.prepare()
                                                player.play()
                                                currentPlayingIndex = actualIndex
                                                isPlaying = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }

            // Barra de reproducción flotante mejorada
            if (currentPlayingIndex != null) {
                val currentSongs = if (selectedTab == 0) likedSongs else followingSongs
                if (currentPlayingIndex!! < currentSongs.size) {
                    Spacer(Modifier.height(12.dp))
                    EnhancedMusicPlayerBar(
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        onPlayPause = {
                            if (isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.play()
                                isPlaying = true
                            }
                        },
                        onSeek = { position ->
                            player.seekTo(position)
                            currentPosition = position
                        },
                        song = currentSongs[currentPlayingIndex!!],
                        context = context
                    )
                }
            }
        }
        }
    }
}

// Barra de reproducción mejorada con ecualizador tipo Spotify
@Composable
fun EnhancedMusicPlayerBar(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    song: ArtistCard,
    context: android.content.Context
) {
    // Animación de escala para el botón play/pause
    val playButtonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "play_button_scale"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PopArtColors.Yellow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Box {
            // Fondo animado de onda
            AnimatedWaveBackground(
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Portada con ecualizador
                    Box(
                        modifier = Modifier.size(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.imageUrl.isNotEmpty()) {
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(context)
                                    .data(song.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = song.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PopArtColors.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎵", fontSize = 28.sp)
                            }
                        }
                        
                        // Ecualizador superpuesto
                        if (isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PopArtColors.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedEqualizer(
                                    isPlaying = true,
                                    barCount = 5,
                                    color = PopArtColors.Yellow
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Información de la canción
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            song.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            song.genre,
                            fontSize = 13.sp,
                            color = PopArtColors.Black.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            song.location,
                            fontSize = 11.sp,
                            color = PopArtColors.Black.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    // Botón play/pause grande con animación
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer {
                                scaleX = playButtonScale
                                scaleY = playButtonScale
                            }
                            .background(PopArtColors.Black, CircleShape)
                            .clickable(onClick = onPlayPause),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlaying) {
                            Text("⏸", fontSize = 28.sp, color = PopArtColors.Yellow)
                        } else {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = PopArtColors.Yellow,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Video de ecualizador (barra completa)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PopArtColors.Black.copy(alpha = 0.2f))
                ) {
                    // Video de fondo
                    VideoEqualizerBackground(
                        isPlaying = isPlaying,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Overlay opcional para mejor contraste
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PopArtColors.Black.copy(alpha = 0.1f))
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Barra de progreso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatTime(currentPosition),
                        fontSize = 12.sp,
                        color = PopArtColors.Black,
                        fontWeight = FontWeight.Black
                    )

                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() else 0f,
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = PopArtColors.Black,
                            activeTrackColor = PopArtColors.Black,
                            inactiveTrackColor = PopArtColors.Black.copy(alpha = 0.2f)
                        )
                    )

                    Text(
                        formatTime(duration),
                        fontSize = 12.sp,
                        color = PopArtColors.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
