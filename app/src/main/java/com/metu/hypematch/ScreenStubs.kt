package com.metu.hypematch

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// PANTALLA 2: TU MÚSICA
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
    
    var likedSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var userSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var allSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var artistsWithStories by remember { mutableStateOf<List<ArtistWithStory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var currentlyPlayingIndex by remember { mutableStateOf(-1) }
    var isPlaying by remember { mutableStateOf(false) }
    var showStoryViewer by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf<ArtistCard?>(null) }

    // Cargar canciones del usuario y canciones que le gustaron desde Firebase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                isLoading = true
                
                // Cargar canciones propias del usuario
                userSongs = firebaseManager.getUserSongs(userId)
                android.util.Log.d("MyMusicScreen", "✅ Canciones propias cargadas: ${userSongs.size}")
                
                // Cargar canciones que le gustaron
                likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
                android.util.Log.d("MyMusicScreen", "✅ Canciones con like cargadas: ${likedSongs.size}")
                
                // Combinar ambas listas (canciones propias primero)
                allSongs = (userSongs + likedSongs).distinctBy { it.id }
                
                // Cargar historias de los artistas favoritos
                val artistIds = allSongs.map { it.userId }.filter { it.isNotEmpty() }.distinct()
                artistsWithStories = artistIds.mapNotNull { artistId ->
                    val artist = allSongs.find { it.userId == artistId }
                    val hasStory = firebaseManager.artistHasActiveStory(artistId)
                    artist?.let {
                        ArtistWithStory(
                            artist = it,
                            hasActiveStory = hasStory
                        )
                    }
                }.distinctBy { it.artist.userId }
                
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error cargando canciones: ${e.message}")
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }



    // Mostrar visor de historias si está activo
    if (showStoryViewer && selectedArtist != null) {
        var artistStories by remember { mutableStateOf<List<ArtistStory>>(emptyList()) }
        
        LaunchedEffect(selectedArtist) {
            if (selectedArtist != null) {
                artistStories = firebaseManager.getUserStories(selectedArtist!!.userId)
                android.util.Log.d("MyMusicScreen", "📚 Historias del artista cargadas: ${artistStories.size}")
            }
        }
        
        StoryViewerScreen(
            artist = selectedArtist!!,
            stories = artistStories,
            onBack = { 
                showStoryViewer = false
                selectedArtist = null
            },
            onDeleteStory = { story ->
                scope.launch {
                    try {
                        firebaseManager.deleteStory(story.id, story.artistId)
                        artistStories = artistStories.filter { it.id != story.id }
                        android.util.Log.d("MyMusicScreen", "✅ Historia eliminada: ${story.id}")
                    } catch (e: Exception) {
                        android.util.Log.e("MyMusicScreen", "❌ Error eliminando historia: ${e.message}")
                    }
                }
            },
            onHighlightStory = { story ->
                scope.launch {
                    try {
                        val newHighlightState = !story.isHighlighted
                        firebaseManager.toggleStoryHighlight(story.id, newHighlightState)
                        artistStories = artistStories.map {
                            if (it.id == story.id) it.copy(isHighlighted = newHighlightState) else it
                        }
                        android.util.Log.d("MyMusicScreen", "✅ Historia ${if (newHighlightState) "destacada" else "desmarcada"}: ${story.id}")
                    } catch (e: Exception) {
                        android.util.Log.e("MyMusicScreen", "❌ Error destacando historia: ${e.message}")
                    }
                }
            },
            onShareStory = { story ->
                // TODO: Implementar compartir con Android Share Sheet
                android.util.Log.d("MyMusicScreen", "📤 Compartir historia: ${story.id}")
            },
            colors = colors
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // Header
            HypeHeader(
                onMenuClick = onMenuClick,
                isDarkMode = isDarkMode,
                colors = colors,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        
        // Contenido
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (allSongs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎵", fontSize = 80.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Tu biblioteca está vacía",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Sube tu música o dale ❤️ a canciones en Descubre",
                    fontSize = 16.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, bottom = 100.dp)
            ) {
                // Barra de historias de artistas
                if (artistsWithStories.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                "Historias de Artistas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.text,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )
                            
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                items(artistsWithStories.size) { index ->
                                    val artistWithStory = artistsWithStories[index]
                                    ArtistStoryBubble(
                                        artist = artistWithStory.artist,
                                        hasActiveStory = artistWithStory.hasActiveStory,
                                        colors = colors,
                                        onClick = {
                                            selectedArtist = artistWithStory.artist
                                            showStoryViewer = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Sección de canciones propias
                if (userSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Tus Canciones",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.text,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(userSongs.size) { index ->
                        val song = userSongs[index]
                        val isCurrentlyPlaying = currentlyPlayingIndex == index
                        
                        MusicCard(
                            song = song,
                            isCurrentlyPlaying = isCurrentlyPlaying,
                            isPlaying = isPlaying,
                            colors = colors,
                            onPlayPause = {
                                if (isCurrentlyPlaying) {
                                    if (isPlaying) {
                                        player.pause()
                                        isPlaying = false
                                    } else {
                                        player.play()
                                        isPlaying = true
                                    }
                                } else {
                                    scope.launch {
                                        try {
                                            player.stop()
                                            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(song.songUrl))
                                            player.prepare()
                                            player.play()
                                            currentlyPlayingIndex = index
                                            isPlaying = true
                                            firebaseManager.incrementPlays(song.id)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MyMusicScreen", "Error: ${e.message}")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                
                // Sección de canciones favoritas
                if (likedSongs.isNotEmpty()) {
                    item {
                        Text(
                            "Canciones Favoritas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.text,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    items(likedSongs.size) { index ->
                        val song = likedSongs[index]
                        val actualIndex = userSongs.size + index
                        val isCurrentlyPlaying = currentlyPlayingIndex == actualIndex
                        
                        MusicCard(
                            song = song,
                            isCurrentlyPlaying = isCurrentlyPlaying,
                            isPlaying = isPlaying,
                            colors = colors,
                            onPlayPause = {
                                if (isCurrentlyPlaying) {
                                    if (isPlaying) {
                                        player.pause()
                                        isPlaying = false
                                    } else {
                                        player.play()
                                        isPlaying = true
                                    }
                                } else {
                                    scope.launch {
                                        try {
                                            player.stop()
                                            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(song.songUrl))
                                            player.prepare()
                                            player.play()
                                            currentlyPlayingIndex = actualIndex
                                            isPlaying = true
                                            firebaseManager.incrementPlays(song.id)
                                        } catch (e: Exception) {
                                            android.util.Log.e("MyMusicScreen", "Error: ${e.message}")
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        }
        
        DisposableEffect(Unit) {
            onDispose {
                player.release()
            }
        }
    }
}

// Componente reutilizable para tarjetas de música
@Composable
fun MusicCard(
    song: ArtistCard,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    colors: AppColors,
    onPlayPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onPlayPause() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) colors.primary.copy(alpha = 0.2f) else colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(colors.primary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(song.emoji, fontSize = 28.sp)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    song.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.text
                )
                Text(
                    "${song.genre} • ${song.location}",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { onPlayPause() }
            ) {
                if (isCurrentlyPlaying && isPlaying) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_pause),
                        contentDescription = "Pausar",
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_play),
                        contentDescription = "Reproducir",
                        tint = colors.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Componente para la burbuja de historia de artista
@Composable
fun ArtistStoryBubble(
    artist: ArtistCard,
    hasActiveStory: Boolean,
    colors: AppColors,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            // Anillo de color si tiene historia activa
            if (hasActiveStory) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    PopArtColors.Pink,
                                    PopArtColors.Yellow,
                                    PopArtColors.Cyan
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }
            
            // Foto del artista
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(PopArtColors.Cyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    artist.emoji,
                    fontSize = 36.sp
                )
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            artist.name.take(10),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            maxLines = 1
        )
    }
}

// Data class para artista con historia
data class ArtistWithStory(
    val artist: ArtistCard,
    val hasActiveStory: Boolean
)

// Visor de historias con menú hamburguesa
@Composable
fun StoryViewerScreen(
    artist: ArtistCard,
    stories: List<ArtistStory>,
    currentStoryIndex: Int = 0,
    onBack: () -> Unit,
    onDeleteStory: (ArtistStory) -> Unit = {},
    onHighlightStory: (ArtistStory) -> Unit = {},
    onShareStory: (ArtistStory) -> Unit = {},
    colors: AppColors = getAppColors(false)
) {
    var currentIndex by remember { mutableStateOf(currentStoryIndex) }
    var showMenu by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    val currentStory = if (stories.isNotEmpty() && currentIndex < stories.size) {
        stories[currentIndex]
    } else null
    
    // Auto-avanzar historia cada 5 segundos
    LaunchedEffect(currentIndex) {
        progress = 0f
        val duration = 5000L // 5 segundos
        val steps = 50
        val stepDuration = duration / steps
        
        for (i in 0..steps) {
            kotlinx.coroutines.delay(stepDuration)
            progress = i.toFloat() / steps
            
            if (i == steps) {
                // Avanzar a la siguiente historia
                if (currentIndex < stories.size - 1) {
                    currentIndex++
                } else {
                    onBack()
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -100) {
                        // Swipe izquierda - siguiente historia
                        if (currentIndex < stories.size - 1) {
                            currentIndex++
                        } else {
                            onBack()
                        }
                    } else if (dragAmount > 100) {
                        // Swipe derecha - historia anterior
                        if (currentIndex > 0) {
                            currentIndex--
                        } else {
                            onBack()
                        }
                    }
                }
            }
    ) {
        // Contenido de la historia
        if (currentStory != null) {
            // Mostrar imagen o video según mediaType
            if (currentStory.mediaType == "video") {
                // TODO: Reproducir video
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "🎥",
                        fontSize = 120.sp,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            } else {
                // Mostrar imagen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "📷",
                        fontSize = 120.sp,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }
        }
        
        // Overlay oscuro para mejor legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            PopArtColors.Black.copy(alpha = 0.6f),
                            androidx.compose.ui.graphics.Color.Transparent,
                            PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        // Barra de progreso de historias
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            stories.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                ) {
                    if (index < currentIndex) {
                        // Historia completada
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Color.White,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    } else if (index == currentIndex) {
                        // Historia actual con progreso
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    androidx.compose.ui.graphics.Color.White,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }
        
        // Header con info del artista y menú hamburguesa
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 56.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de cerrar
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Foto del artista
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PopArtColors.Cyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(artist.emoji, fontSize = 20.sp)
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Nombre del artista
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    artist.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                if (currentStory != null) {
                    Text(
                        formatTimeAgo(currentStory.timestamp),
                        fontSize = 12.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Menú hamburguesa
            Box {
                IconButton(
                    onClick = { showMenu = !showMenu },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menú",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                // Dropdown menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(colors.surface, RoundedCornerShape(12.dp))
                ) {
                    if (currentStory != null) {
                        // Opción: Eliminar historia
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = PopArtColors.Pink
                                    )
                                    Text(
                                        "Eliminar historia",
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onDeleteStory(currentStory)
                                // Avanzar a la siguiente o cerrar
                                if (currentIndex < stories.size - 1) {
                                    currentIndex++
                                } else {
                                    onBack()
                                }
                            }
                        )
                        
                        // Opción: Destacar historia
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = PopArtColors.Yellow
                                    )
                                    Text(
                                        if (currentStory.isHighlighted) "Quitar de destacados" else "Destacar historia",
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onHighlightStory(currentStory)
                            }
                        )
                        
                        // Opción: Compartir historia
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = PopArtColors.Cyan
                                    )
                                    Text(
                                        "Compartir historia",
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onShareStory(currentStory)
                            }
                        )
                    }
                }
            }
        }
        
        // Botón de destacar en la parte inferior (solo para historias propias)
        if (currentStory != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                FloatingActionButton(
                    onClick = { onHighlightStory(currentStory) },
                    containerColor = if (currentStory.isHighlighted) 
                        PopArtColors.Yellow 
                    else 
                        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_star),
                        contentDescription = if (currentStory.isHighlighted) "Quitar de destacados" else "Destacar",
                        tint = if (currentStory.isHighlighted) 
                            PopArtColors.Black 
                        else 
                            androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Controles de navegación (tap izquierda/derecha)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp)
        ) {
            // Tap izquierda - historia anterior
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (currentIndex > 0) {
                            currentIndex--
                        } else {
                            onBack()
                        }
                    }
            )
            
            // Tap derecha - siguiente historia
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        if (currentIndex < stories.size - 1) {
                            currentIndex++
                        } else {
                            onBack()
                        }
                    }
            )
        }
    }
}

// Función auxiliar para formatear tiempo
fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        else -> "${diff / 86400000}d"
    }
}

// PANTALLA 3: LIVE - Lives/Hypies y Concursos
@Composable
fun LiveScreen(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager() }
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(0) } // 0 = Lives, 1 = Concursos
    var showContestDetail by remember { mutableStateOf(false) }
    var selectedContest by remember { mutableStateOf<Contest?>(null) }
    var showLiveRecording by remember { mutableStateOf(false) }
    var showVideoPreview by remember { mutableStateOf(false) }
    var recordedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showGallery by remember { mutableStateOf(false) }
    var showCatalog by remember { mutableStateOf(false) }
    
    // Simular lives activos (en producción, esto vendría de Firebase)
    val activeLives = remember {
        listOf(
            LiveStream(
                id = "1",
                name = "Luna Beats en Vivo",
                artistName = "Luna Beats",
                location = "CDMX",
                emoji = "🎸",
                viewers = 1234,
                isLive = true,
                startTime = System.currentTimeMillis()
            ),
            LiveStream(
                id = "2",
                name = "DJ Neon Set",
                artistName = "DJ Neon",
                location = "Guadalajara",
                emoji = "🎧",
                viewers = 856,
                isLive = true,
                startTime = System.currentTimeMillis()
            ),
            LiveStream(
                id = "3",
                name = "Los Rebeldes Rock",
                artistName = "Los Rebeldes",
                location = "Monterrey",
                emoji = "🎤",
                viewers = 542,
                isLive = true,
                startTime = System.currentTimeMillis()
            )
        )
    }
    
    val lives = remember {
        listOf(
            Concert(
                name = "Luna Beats en Vivo",
                location = "CDMX",
                date = "Hoy",
                time = "20:00",
                emoji = "🎸"
            ),
            Concert(
                name = "DJ Neon Set",
                location = "Guadalajara",
                date = "Mañana",
                time = "21:00",
                emoji = "🎧"
            ),
            Concert(
                name = "Los Rebeldes Rock",
                location = "Monterrey",
                date = "Viernes",
                time = "19:00",
                emoji = "🎤"
            )
        )
    }
    
    val contests = remember {
        listOf(
            Contest(
                name = "Mejor Cover 2024",
                prize = "🏆 $1,000 + Grabación Profesional",
                deadline = "Termina en 15 días",
                emoji = "🎤",
                color = PopArtColors.Pink
            ),
            Contest(
                name = "Talento Emergente",
                prize = "🎸 Equipo Musical + Promoción",
                deadline = "Termina en 30 días",
                emoji = "⭐",
                color = PopArtColors.Yellow
            ),
            Contest(
                name = "Mejor Producción",
                prize = "🎧 Software + Masterclass",
                deadline = "Termina en 45 días",
                emoji = "🎵",
                color = PopArtColors.Cyan
            )
        )
    }
    
    when {
        showGallery && selectedContest != null -> {
            ContestGalleryScreen(
                contest = selectedContest!!,
                onBack = { showGallery = false }
            )
        }
        showVideoPreview && recordedVideoUri != null -> {
            VideoPreviewScreen(
                videoUri = recordedVideoUri!!,
                onBack = {
                    showVideoPreview = false
                    recordedVideoUri = null
                },
                onUpload = { uri ->
                    scope.launch {
                        try {
                            val userId = authManager.getUserId() ?: ""
                            val username = authManager.getUserName()
                            
                            android.util.Log.d("LiveScreen", "📤 Subiendo video...")
                            android.util.Log.d("LiveScreen", "URI: $uri")
                            android.util.Log.d("LiveScreen", "Usuario: $username")
                            android.util.Log.d("LiveScreen", "Concurso: ${selectedContest?.name}")
                            
                            // Crear entrada de concurso
                            firebaseManager.createContestEntry(
                                userId = userId,
                                username = username,
                                videoUrl = uri.toString(),
                                title = "Video de ${selectedContest?.name ?: "Concurso"}",
                                contestId = selectedContest?.name ?: ""
                            )
                            
                            android.util.Log.d("LiveScreen", "✅ Video subido exitosamente")
                            
                            showVideoPreview = false
                            recordedVideoUri = null
                            showContestDetail = false
                        } catch (e: Exception) {
                            android.util.Log.e("LiveScreen", "❌ Error subiendo video: ${e.message}")
                        }
                    }
                },
                onRetake = {
                    showVideoPreview = false
                    recordedVideoUri = null
                    showLiveRecording = true
                }
            )
        }
        showLiveRecording -> {
            LiveRecordingScreen(
                onBack = { showLiveRecording = false },
                onVideoRecorded = { uri ->
                    recordedVideoUri = uri
                    showLiveRecording = false
                    showVideoPreview = true
                }
            )
        }
        showContestDetail && selectedContest != null -> {
            ContestDetailScreen(
                contest = selectedContest!!,
                onBack = { showContestDetail = false },
                onRecordVideo = { showLiveRecording = true },
                onViewGallery = { showGallery = true }
            )
        }
        else -> {
            var swipeOffset by remember { mutableStateOf(0f) }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
            ) {
                // Header
                HypeHeader(
                    onMenuClick = onMenuClick,
                    isDarkMode = isDarkMode,
                    colors = colors,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (swipeOffset < -200 && currentTab == 1 && contests.isNotEmpty()) {
                                        // Swipe izquierda en tab de concursos - abrir galería del primer concurso
                                        android.util.Log.d("LiveScreen", "⬅️ Swipe detectado, abriendo galería")
                                        selectedContest = contests.first()
                                        showGallery = true
                                    }
                                    swipeOffset = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    swipeOffset += dragAmount
                                }
                            )
                        }
                ) {
                    // Tabs para cambiar entre Lives y Concursos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { currentTab = 0 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTab == 0) colors.primary else colors.surface
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "LIVES",
                                fontWeight = FontWeight.Black,
                                color = if (currentTab == 0) PopArtColors.Black else colors.text
                            )
                        }
                        Button(
                            onClick = { currentTab = 1 },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentTab == 1) colors.primary else colors.surface
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                "CONCURSOS",
                                fontWeight = FontWeight.Black,
                                color = if (currentTab == 1) PopArtColors.Black else colors.text
                            )
                        }
                    }
                    
                    // Contenido según el tab seleccionado
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (currentTab == 0) {
                            // Tab de Lives/Hypies
                            item {
                                Text(
                                    "LIVES / HYPIES",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Conciertos y eventos en vivo 🎵",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary
                                )
                            }
                            
                            items(lives.size) { index ->
                                val live = lives[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            // TODO: Abrir pantalla de live viewer
                                            android.util.Log.d("LiveScreen", "Ver live: ${live.name}")
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colors.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(colors.primary, RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(live.emoji, fontSize = 32.sp)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(PopArtColors.Pink, CircleShape)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    "EN VIVO",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = PopArtColors.Pink
                                                )
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                live.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.text
                                            )
                                            Text(
                                                "${live.location} • ${live.date} ${live.time}",
                                                fontSize = 12.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Ver",
                                            tint = colors.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Tab de Concursos
                            item {
                                Text(
                                    "CONCURSOS",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Participa y gana premios increíbles 🏆",
                                    fontSize = 14.sp,
                                    color = colors.textSecondary
                                )
                            }
                            
                            items(contests.size) { index ->
                                val contest = contests[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedContest = contest
                                            showContestDetail = true
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = contest.color
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            contest.emoji,
                                            fontSize = 48.sp
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                contest.name,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                color = PopArtColors.Black
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                contest.prize,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PopArtColors.Black.copy(alpha = 0.8f)
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                contest.deadline,
                                                fontSize = 12.sp,
                                                color = PopArtColors.Black.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Icon(
                                            Icons.Default.KeyboardArrowRight,
                                            contentDescription = "Ver detalles",
                                            tint = PopArtColors.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Espaciado final
                        item {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}
