package com.metu.hypematch

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun TikTokStyleVideoPlayer(
    videos: List<ContestEntry>,
    initialIndex: Int = 0,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var dragOffset by remember { mutableStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }
    
    // Crear player
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = true
        }
    }
    
    // Mantener pantalla encendida
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            player.release()
        }
    }
    
    // Cargar video actual
    LaunchedEffect(currentIndex) {
        if (videos.isNotEmpty() && currentIndex in videos.indices) {
            val video = videos[currentIndex]
            android.util.Log.d("TikTokPlayer", "📹 Cargando video: ${video.videoUrl}")
            
            try {
                player.setMediaItem(MediaItem.fromUri(Uri.parse(video.videoUrl)))
                player.prepare()
                player.play()
            } catch (e: Exception) {
                android.util.Log.e("TikTokPlayer", "Error cargando video: ${e.message}")
            }
        }
    }
    
    // Auto-ocultar controles
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (abs(dragOffset) > 100) {
                            if (dragOffset > 0 && currentIndex > 0) {
                                // Swipe down - video anterior
                                currentIndex--
                                android.util.Log.d("TikTokPlayer", "⬇️ Video anterior: $currentIndex")
                            } else if (dragOffset < 0 && currentIndex < videos.size - 1) {
                                // Swipe up - siguiente video
                                currentIndex++
                                android.util.Log.d("TikTokPlayer", "⬆️ Siguiente video: $currentIndex")
                            }
                        }
                        dragOffset = 0f
                        showControls = true
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        // Reproductor de video
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    useController = false
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay con información del video
        if (videos.isNotEmpty() && currentIndex in videos.indices) {
            val currentVideo = videos[currentIndex]
            
            // Controles superiores
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                PopArtColors.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp)
                    .alpha(if (showControls) 1f else 0f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(PopArtColors.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                
                Text(
                    "${currentIndex + 1} / ${videos.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(PopArtColors.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Información del video (parte inferior)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PopArtColors.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(20.dp)
                    .alpha(if (showControls) 1f else 0f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "@${currentVideo.username}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    if (currentVideo.userId == (context as? android.app.Activity)?.let {
                        AuthManager(it).getUserId()
                    }) {
                        Box(
                            modifier = Modifier
                                .background(PopArtColors.Yellow, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "TÚ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.Black
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    currentVideo.title,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = PopArtColors.Pink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            currentVideo.likes.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Views",
                            tint = PopArtColors.Cyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            currentVideo.views.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Botones de acción lateral (estilo TikTok)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp)
                    .alpha(if (showControls) 1f else 0f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón de like
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            // TODO: Implementar like
                            android.util.Log.d("TikTokPlayer", "❤️ Like video: ${currentVideo.id}")
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(PopArtColors.Pink.copy(alpha = 0.9f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        currentVideo.likes.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Botón de compartir
                IconButton(
                    onClick = {
                        // TODO: Implementar compartir
                        android.util.Log.d("TikTokPlayer", "📤 Compartir video: ${currentVideo.id}")
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(PopArtColors.Yellow.copy(alpha = 0.9f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = PopArtColors.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Indicador de swipe
            if (showControls) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentIndex > 0) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Anterior",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(200.dp))
                    
                    if (currentIndex < videos.size - 1) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Siguiente",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
