package com.metu.hypematch

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

// Music Player Manager
@Composable
fun rememberMusicPlayer(context: Context, pauseOnBackground: Boolean = true): ExoPlayer {
    return remember {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .build(),
                pauseOnBackground
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                volume = 1.0f
                
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val state = when (playbackState) {
                            Player.STATE_IDLE -> "IDLE"
                            Player.STATE_BUFFERING -> "BUFFERING"
                            Player.STATE_READY -> "READY"
                            Player.STATE_ENDED -> "ENDED"
                            else -> "UNKNOWN"
                        }
                        android.util.Log.d("ExoPlayer", "Estado: $state, isPlaying: $isPlaying")
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        android.util.Log.d("ExoPlayer", "isPlaying cambió a: $isPlaying")
                    }
                    
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        android.util.Log.e("ExoPlayer", "Error de reproducción: ${error.message}", error)
                        android.util.Log.e("ExoPlayer", "Error code: ${error.errorCode}")
                    }
                })
            }
    }
}

@Composable
fun DisposableEffectWithLifecycle(player: ExoPlayer) {
    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }
}

// Card del artista con páginas navegables (tipo Tinder)
@Composable
fun ArtistCardWithPages(
    artist: ArtistCard,
    offsetX: Float,
    offsetY: Float,
    isPlaying: Boolean,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 4 // Principal, Galería, Info, Redes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .rotate(offsetX / 20f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (offset.x > size.width * 0.6f) {
                            if (currentPage < totalPages - 1) currentPage++
                        } else if (offset.x < size.width * 0.4f) {
                            if (currentPage > 0) currentPage--
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDragEnd() }
                    ) { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PopArtColors.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentPage) {
                    0 -> MainPage(artist, isPlaying)
                    1 -> GalleryPage(artist)
                    2 -> InfoPage(artist)
                    3 -> SocialPage(artist)
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(totalPages) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 8.dp else 6.dp)
                                .background(
                                    if (index == currentPage) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        )
                    }
                }

                if (offsetX > 50) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(100.dp)
                            .background(PopArtColors.Yellow.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("❤️", fontSize = 50.sp)
                    }
                } else if (offsetX < -50) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(100.dp)
                            .background(PopArtColors.Pink.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤢", fontSize = 50.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MainPage(artist: ArtistCard, isPlaying: Boolean, onProfileClick: () -> Unit = {}) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (artist.imageUrl.isNotEmpty()) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(artist.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = artist.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onProfileClick() }
                        )
                    },
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .background(PopArtColors.MulticolorGradient)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onProfileClick() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(artist.emoji, fontSize = 120.sp)
            }
        }
        
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(PopArtColors.Black.copy(alpha = 0.7f), CircleShape)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Playing",
                        tint = PopArtColors.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Reproduciendo",
                        color = PopArtColors.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val context = androidx.compose.ui.platform.LocalContext.current
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(PopArtColors.Black)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        artist.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Yellow,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    if (artist.userId.isNotEmpty()) {
                        ArtistFollowButton(artistId = artist.userId)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${artist.genre} • ${artist.location}",
                        fontSize = 16.sp,
                        color = PopArtColors.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    IconButton(
                        onClick = { shareArtist(context, artist) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(PopArtColors.Yellow, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryPage(artist: ArtistCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "GALERÍA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.Yellow,
            modifier = Modifier.padding(top = 40.dp, bottom = 8.dp)
        )

        artist.photos.forEach { photo ->
            if (photo.startsWith("http")) {
                coil.compose.AsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(photo)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de ${artist.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(PopArtColors.MulticolorGradient, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(photo, fontSize = 60.sp)
                }
            }
        }
    }
}

@Composable
fun InfoPage(artist: ArtistCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .padding(20.dp)
    ) {
        Text(
            "SOBRE ${artist.name.uppercase()}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.Yellow,
            modifier = Modifier.padding(top = 40.dp, bottom = 16.dp)
        )

        Text(
            artist.bio,
            fontSize = 16.sp,
            color = PopArtColors.White,
            lineHeight = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            "ESTADÍSTICAS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.Yellow,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("${artist.stats.followers}", "Seguidores")
            StatItem("${artist.stats.songs}", "Canciones")
            StatItem("${artist.stats.plays}", "Plays")
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.Yellow
        )
        Text(
            label,
            fontSize = 12.sp,
            color = PopArtColors.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SocialPage(artist: ArtistCard) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "REDES SOCIALES",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.Yellow,
            modifier = Modifier.padding(top = 40.dp, bottom = 16.dp)
        )

        artist.socialLinks.forEach { (platform, handle) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PopArtColors.Yellow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (platform) {
                            "Instagram" -> "📷"
                            "YouTube" -> "▶️"
                            "SoundCloud" -> "☁️"
                            "Spotify" -> "🎵"
                            "Facebook" -> "👥"
                            "TikTok" -> "🎬"
                            else -> "🔗"
                        },
                        fontSize = 28.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            platform,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                        Text(
                            handle,
                            fontSize = 14.sp,
                            color = PopArtColors.Black.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun shareArtist(context: Context, artist: ArtistCard) {
    val shareText = """
        🎵 ¡Descubre a ${artist.name}! 🎵
        
        Género: ${artist.genre}
        Ubicación: ${artist.location}
        
        ${artist.bio}
        
        ¡Escúchalo en HYPE! 🔥
    """.trimIndent()
    
    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartir artista")
    context.startActivity(shareIntent)
}

// Botón compacto de seguir para la tarjeta del artista
@Composable
fun ArtistFollowButton(artistId: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val currentUserId = authManager.getCurrentUser()?.uid
    
    // Verificar si ya sigue al artista
    LaunchedEffect(artistId, currentUserId) {
        if (currentUserId != null && artistId.isNotEmpty()) {
            try {
                isFollowing = firebaseManager.isFollowing(currentUserId, artistId)
            } catch (e: Exception) {
                android.util.Log.e("ArtistFollowButton", "Error verificando follow: ${e.message}")
            }
        }
    }
    
    // No mostrar el botón si es el mismo usuario
    if (currentUserId == artistId || currentUserId == null) {
        return
    }
    
    Button(
        onClick = {
            if (!isLoading) {
                isLoading = true
                scope.launch {
                    try {
                        val newFollowState = firebaseManager.toggleFollow(currentUserId, artistId)
                        isFollowing = newFollowState
                        isLoading = false
                    } catch (e: Exception) {
                        android.util.Log.e("ArtistFollowButton", "Error en toggle follow: ${e.message}")
                        isLoading = false
                    }
                }
            }
        },
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) PopArtColors.White.copy(alpha = 0.2f) else PopArtColors.Yellow
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = PopArtColors.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = if (isFollowing) "Siguiendo" else "Seguir",
                    tint = if (isFollowing) PopArtColors.White else PopArtColors.Black,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = if (isFollowing) "Siguiendo" else "Seguir",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFollowing) PopArtColors.White else PopArtColors.Black
                )
            }
        }
    }
}
