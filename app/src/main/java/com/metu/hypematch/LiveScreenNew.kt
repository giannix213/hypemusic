package com.metu.hypematch

import android.content.Context
import android.net.Uri
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import coil.compose.AsyncImage
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch

// ============ SISTEMA DE CACHÉ PARA VIDEOS ============

object ExoPlayerCache {
    private var simpleCache: SimpleCache? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private var databaseProvider: StandaloneDatabaseProvider? = null
    
    // Tamaño máximo de la caché: 200MB para videos
    private const val MAX_CACHE_SIZE = 200 * 1024 * 1024L
    
    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        if (cacheDataSourceFactory == null) {
            android.util.Log.d("ExoPlayerCache", "🗄️ Inicializando caché de videos (${MAX_CACHE_SIZE / 1024 / 1024}MB)")
            
            val cacheDir = File(context.cacheDir, "video_cache")
            
            // Estrategia: Eliminar videos menos usados cuando se alcance el límite
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
            
            // DatabaseProvider para gestionar la base de datos de la caché
            databaseProvider = StandaloneDatabaseProvider(context)
            
            // Inicializar caché con DatabaseProvider
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider!!)
            
            // DataSource de red
            val upstreamFactory = DefaultHttpDataSource.Factory()
            
            // CacheDataSource: Lee de caché primero, si no está, descarga de red
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache!!)
                .setUpstreamDataSourceFactory(upstreamFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            
            android.util.Log.d("ExoPlayerCache", "✅ Caché inicializada en: ${cacheDir.absolutePath}")
        }
        return cacheDataSourceFactory!!
    }
    
    fun release() {
        simpleCache?.release()
        simpleCache = null
        cacheDataSourceFactory = null
        databaseProvider = null
        android.util.Log.d("ExoPlayerCache", "🧹 Caché liberada")
    }
}

// NUEVA PANTALLA DE LIVES - CARRUSEL DE VIDEOS DE CONCURSOS
@Composable
fun LiveScreenNew(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {}  // Callback para navegar al perfil de un usuario
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager() }
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()
    
    var showContestDetail by remember { mutableStateOf(false) }
    var selectedContest by remember { mutableStateOf<Contest?>(null) }
    var showLiveRecording by remember { mutableStateOf(false) }
    var showVideoPreview by remember { mutableStateOf(false) }
    var recordedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showGallery by remember { mutableStateOf(false) }
    var showCatalog by remember { mutableStateOf(false) }
    var showLiveStreams by remember { mutableStateOf(false) } // Nueva: pantalla de transmisiones en vivo
    var showLiveLauncher by remember { mutableStateOf(false) } // Nueva: pantalla de inicio de Live
    var currentVideoIndex by remember { mutableStateOf(0) }
    
    // NUEVAS VARIABLES PARA NAVEGACIÓN DE LIVE STREAMING
    var showBroadcasterScreen by remember { mutableStateOf(false) } // Pantalla de transmisión (streamer)
    var showViewerScreen by remember { mutableStateOf(false) } // Pantalla de visualización (espectador)
    var liveSessionId by remember { mutableStateOf("") }
    var liveChannelName by remember { mutableStateOf("") }
    var liveAgoraToken by remember { mutableStateOf("") }
    var liveStreamerName by remember { mutableStateOf("") }
    
    // Videos de concursos - CARGADOS DESDE FIREBASE
    var contestVideos by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var isLoadingVideos by remember { mutableStateOf(true) }
    
    // Cargar videos de concursos desde Firebase
    LaunchedEffect(Unit) {
        try {
            isLoadingVideos = true
            android.util.Log.d("LiveScreen", "🎬 ===== CARGANDO VIDEOS DE CONCURSOS =====")
            android.util.Log.d("LiveScreen", "📍 Colección: contest_entries")
            
            // Obtener todos los videos de concursos
            contestVideos = firebaseManager.getAllContestEntries()
            
            android.util.Log.d("LiveScreen", "✅ Videos cargados: ${contestVideos.size}")
            
            if (contestVideos.isEmpty()) {
                android.util.Log.w("LiveScreen", "⚠️ No se encontraron videos en Firestore")
                android.util.Log.w("LiveScreen", "💡 Verifica que existan documentos en 'contest_entries'")
            } else {
                android.util.Log.d("LiveScreen", "📋 Lista de videos:")
                contestVideos.forEachIndexed { index, video ->
                    android.util.Log.d("LiveScreen", "  ${index + 1}. Video ID: ${video.id}")
                    android.util.Log.d("LiveScreen", "     👤 Username: '${video.username}' ${if (video.username.isEmpty()) "⚠️ VACÍO" else "✅"}")
                    android.util.Log.d("LiveScreen", "     📝 Título: '${video.title}' ${if (video.title.isEmpty()) "⚠️ VACÍO" else "✅"}")
                    android.util.Log.d("LiveScreen", "     💬 Descripción: '${video.description}' ${if (video.description.isEmpty()) "⚠️ VACÍO" else "✅"}")
                    android.util.Log.d("LiveScreen", "     🏆 Concurso: '${video.contestId}' ${if (video.contestId.isEmpty()) "⚠️ VACÍO" else "✅"}")
                    android.util.Log.d("LiveScreen", "     🎬 VideoURL: ${video.videoUrl.take(50)}...")
                    android.util.Log.d("LiveScreen", "     ❤️ Likes: ${video.likes} | 👁️ Views: ${video.views}")
                }
            }
            
            isLoadingVideos = false
        } catch (e: Exception) {
            android.util.Log.e("LiveScreen", "❌ ===== ERROR CARGANDO VIDEOS =====")
            android.util.Log.e("LiveScreen", "❌ Mensaje: ${e.message}")
            android.util.Log.e("LiveScreen", "❌ Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("LiveScreen", "❌ Stack trace:", e)
            isLoadingVideos = false
            contestVideos = emptyList()
        }
    }
    
    // ViewModel para observar Lives en tiempo real
    val liveListViewModel = remember { 
        android.util.Log.d("LiveScreen", "🎬 CREANDO LiveListViewModel")
        LiveListViewModel(firebaseManager) 
    }
    val liveSessionsFlow by liveListViewModel.liveSessions.collectAsState()
    
    // Variable para mostrar debug en pantalla
    var debugInfo by remember { mutableStateOf("Inicializando...") }
    
    // Convertir LiveSession a LiveStream para compatibilidad con el código existente
    val activeLives = remember(liveSessionsFlow) {
        android.util.Log.d("LiveScreen", "🔄 ACTUALIZANDO LISTA DE LIVES: ${liveSessionsFlow.size}")
        debugInfo = "Lives encontrados: ${liveSessionsFlow.size}\n" +
                    "Última actualización: ${System.currentTimeMillis()}"
        
        liveSessionsFlow.forEach { session ->
            android.util.Log.d("LiveScreen", "  📡 ${session.username} - isActive: ${session.isActive}")
            debugInfo += "\n- ${session.username} (${if (session.isActive) "ACTIVO" else "INACTIVO"})"
        }
        
        liveSessionsFlow.map { session ->
            LiveStream(
                id = session.sessionId,
                name = "${session.username} en Vivo 🔴",
                artistName = session.username,
                location = session.title,
                emoji = "🎤",
                viewers = session.viewerCount,
                isLive = session.isActive,
                startTime = session.startTime
            )
        }
    }
    
    // Log cuando cambia la lista de Lives
    LaunchedEffect(activeLives.size) {
        android.util.Log.d("LiveScreen", "🔴 Lives activos actualizados: ${activeLives.size}")
        activeLives.forEach { live ->
            android.util.Log.d("LiveScreen", "  📡 ${live.artistName} - ${live.viewers} espectadores")
        }
    }
    
    // MOSTRAR DEBUG EN PANTALLA (temporal para diagnóstico)
    var showDebugOverlay by remember { mutableStateOf(true) }
    
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
    
    // Concursos Rápidos (Semanal/Mensual)
    val fastContests = remember {
        listOf(
            Contest(
                name = "Mejor Cover de la Semana",
                prize = "🎬 Netflix por 1 mes",
                deadline = "Termina en 5 días",
                emoji = "🎤",
                color = PopArtColors.Pink,
                type = ContestType.FAST,
                category = "Cover"
            ),
            Contest(
                name = "Talento Emergente del Mes",
                prize = "🎵 Spotify Premium por 1 mes",
                deadline = "Termina en 12 días",
                emoji = "⭐",
                color = PopArtColors.Yellow,
                type = ContestType.FAST,
                category = "Talento Emergente"
            ),
            Contest(
                name = "Mejor Producción Semanal",
                prize = "🎬 HBO por 1 mes",
                deadline = "Termina en 6 días",
                emoji = "🎧",
                color = PopArtColors.Cyan,
                type = ContestType.FAST,
                category = "Producción"
            ),
            Contest(
                name = "El Más Creativo",
                prize = "🎪 Disney+ por 1 mes",
                deadline = "Termina en 8 días",
                emoji = "🎨",
                color = PopArtColors.Purple,
                type = ContestType.FAST,
                category = "Creatividad"
            )
        )
    }
    
    // Concursos de Alto Impacto (Cada 3-6 meses)
    val highImpactContests = remember {
        listOf(
            Contest(
                name = "Mejor Video Musical 2025",
                prize = "🎬 Producción de Video Musical por Hype Music",
                deadline = "Termina en 85 días",
                emoji = "🎥",
                color = PopArtColors.Pink,
                type = ContestType.HIGH_IMPACT,
                category = "Video Musical"
            ),
            Contest(
                name = "Mejor Letra Original",
                prize = "🎵 Producción de Canción Completa",
                deadline = "Termina en 90 días",
                emoji = "✍️",
                color = PopArtColors.Orange,
                type = ContestType.HIGH_IMPACT,
                category = "Letra Original"
            ),
            Contest(
                name = "El Más Creativo del Año",
                prize = "📸 Sesión de Fotos Profesional + Press Kit",
                deadline = "Termina en 95 días",
                emoji = "🌟",
                color = PopArtColors.Yellow,
                type = ContestType.HIGH_IMPACT,
                category = "Creatividad"
            ),
            Contest(
                name = "Mejor Producción del Año",
                prize = "🎤 Campaña de Marketing Digital por Hype Music",
                deadline = "Termina en 100 días",
                emoji = "🏆",
                color = PopArtColors.Cyan,
                type = ContestType.HIGH_IMPACT,
                category = "Producción"
            )
        )
    }
    
    // Combinar todos los concursos
    val allContests = remember(fastContests, highImpactContests) {
        fastContests + highImpactContests
    }
    
    // Manejo de pantallas secundarias
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
                            
                            android.util.Log.d("LiveScreen", "🎬 ===== SUBIENDO VIDEO A CONCURSO =====")
                            android.util.Log.d("LiveScreen", "👤 Usuario: $username ($userId)")
                            android.util.Log.d("LiveScreen", "🏆 Concurso: ${selectedContest?.name}")
                            android.util.Log.d("LiveScreen", "📎 URI local: $uri")
                            
                            // Paso 1: Subir video a Firebase Storage
                            android.util.Log.d("LiveScreen", "📤 Paso 1: Subiendo video a Storage...")
                            val videoUrl = firebaseManager.uploadContestVideo(
                                uri = uri,
                                userId = userId,
                                onProgress = { progress ->
                                    android.util.Log.d("LiveScreen", "📊 Progreso: $progress%")
                                }
                            )
                            android.util.Log.d("LiveScreen", "✅ Video subido a Storage")
                            android.util.Log.d("LiveScreen", "🔗 URL: $videoUrl")
                            
                            // Paso 2: Obtener foto de perfil del usuario
                            android.util.Log.d("LiveScreen", "� Paso 22: Obteniendo foto de perfil...")
                            val userProfile = firebaseManager.getUserProfile(userId)
                            val profilePictureUrl = userProfile?.profileImageUrl ?: ""
                            android.util.Log.d("LiveScreen", "👤 Foto de perfil: ${if (profilePictureUrl.isNotEmpty()) "✅ Encontrada" else "⚠️ No disponible"}")
                            
                            // Paso 3: Crear entrada en Firestore
                            android.util.Log.d("LiveScreen", "📝 Paso 3: Creando entrada en Firestore...")
                            val entryId = firebaseManager.createContestEntry(
                                userId = userId,
                                username = username,
                                videoUrl = videoUrl,
                                title = "Video de ${selectedContest?.name ?: "Concurso"}",
                                description = "Participación en ${selectedContest?.name}",
                                contestId = selectedContest?.name ?: "",
                                profilePictureUrl = profilePictureUrl
                            )
                            
                            android.util.Log.d("LiveScreen", "✅ ===== VIDEO PUBLICADO EXITOSAMENTE =====")
                            android.util.Log.d("LiveScreen", "🆔 ID de entrada: $entryId")
                            android.util.Log.d("LiveScreen", "🎉 El video ahora aparecerá en el carrusel de Live")
                            
                            showVideoPreview = false
                            recordedVideoUri = null
                            showContestDetail = false
                        } catch (e: Exception) {
                            android.util.Log.e("LiveScreen", "❌ ===== ERROR SUBIENDO VIDEO =====")
                            android.util.Log.e("LiveScreen", "❌ Mensaje: ${e.message}")
                            android.util.Log.e("LiveScreen", "❌ Tipo: ${e.javaClass.simpleName}")
                            android.util.Log.e("LiveScreen", "❌ Stack trace:", e)
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
        showLiveStreams -> {
            android.util.Log.d("LiveScreen", "📡 Mostrando pantalla de Lives activos")
            android.util.Log.d("LiveScreen", "   Lives disponibles: ${activeLives.size}")
            // Pantalla de transmisiones en vivo (secundaria)
            if (activeLives.isEmpty()) {
                android.util.Log.d("LiveScreen", "⚠️ No hay Lives activos, mostrando NoLivesScreen")
                NoLivesScreen(
                    colors = colors,
                    onMenuClick = onMenuClick,
                    onSwipeLeft = { 
                        android.util.Log.d("LiveScreen", "⬅️ Swipe left detectado, abriendo catálogo")
                        showCatalog = true 
                    },
                    onSwipeRight = { onMenuClick() },
                    onBack = { 
                        android.util.Log.d("LiveScreen", "⬅️ Volviendo de NoLivesScreen")
                        showLiveStreams = false 
                    }
                )
            } else {
                android.util.Log.d("LiveScreen", "✅ Mostrando ${activeLives.size} Lives activos")
                LiveStreamViewerScreen(
                    lives = activeLives,
                    colors = colors,
                    onBack = { showLiveStreams = false },
                    onSwipeLeft = { showCatalog = true },
                    onSwipeRight = { onMenuClick() }
                )
            }
        }
        showCatalog -> {
            android.util.Log.d("LiveScreen", "📋 Mostrando catálogo de Lives y Concursos")
            // Pantalla de catálogo (Lives y Concursos)
            LiveCatalogScreen(
                lives = lives,
                fastContests = fastContests,
                highImpactContests = highImpactContests,
                colors = colors,
                onBack = { 
                    android.util.Log.d("LiveScreen", "⬅️ Volviendo del catálogo")
                    showCatalog = false 
                },
                onContestClick = { contest ->
                    android.util.Log.d("LiveScreen", "🏆 Concurso seleccionado: ${contest.name}")
                    selectedContest = contest
                    showContestDetail = true
                },
                onLiveClick = { liveIndex ->
                    android.util.Log.d("LiveScreen", "🔴 Live seleccionado: índice $liveIndex")
                    // Ir a la pantalla de transmisiones en vivo
                    showLiveStreams = true
                    showCatalog = false
                },
                onStartLive = {
                    android.util.Log.d("LiveScreen", "🎥 Iniciar transmisión en vivo")
                    showLiveLauncher = true
                    showCatalog = false
                }
            )
        }
        else -> {
            // PANTALLA PRINCIPAL: Carrusel inmersivo de videos de concursos
            if (isLoadingVideos) {
                // Mostrar indicador de carga
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = colors.primary)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Cargando videos...",
                            fontSize = 16.sp,
                            color = colors.text
                        )
                    }
                }
            } else {
                ContestVideoCarouselScreen(
                    videos = contestVideos,
                    colors = colors,
                    currentIndex = currentVideoIndex,
                    onIndexChange = { currentVideoIndex = it },
                    onSwipeLeft = { showCatalog = true },
                    onSwipeRight = { onMenuClick() },
                    onStartLive = { showLiveStreams = true },
                    onNavigateToProfile = onNavigateToProfile
                )
            }
        }
    }
    
    // Mostrar LiveLauncher cuando se active (para iniciar transmisión propia)
    if (showLiveLauncher) {
        LiveLauncherScreen(
            onClose = { showLiveLauncher = false },
            onStartBroadcast = { sessionId, channelName, token ->
                // Cuando el usuario inicia una transmisión
                liveSessionId = sessionId
                liveChannelName = channelName
                liveAgoraToken = token
                showLiveLauncher = false
                showBroadcasterScreen = true
            }
        )
    }
    
    // NUEVA: Pantalla de transmisión en vivo (Streamer/Broadcaster)
    if (showBroadcasterScreen) {
        // Crear sesión INMEDIATAMENTE cuando se muestra la pantalla
        LaunchedEffect(liveSessionId) {
            if (liveSessionId.isNotEmpty()) {
                try {
                    val userId = authManager.getUserId() ?: ""
                    val username = authManager.getUserName()
                    
                    android.util.Log.d("LiveScreen", "========================================")
                    android.util.Log.d("LiveScreen", "📝 CREANDO SESIÓN DE LIVE")
                    android.util.Log.d("LiveScreen", "   SessionId: $liveSessionId")
                    android.util.Log.d("LiveScreen", "   UserId: $userId")
                    android.util.Log.d("LiveScreen", "   Username: $username")
                    android.util.Log.d("LiveScreen", "   ChannelName: $liveChannelName")
                    android.util.Log.d("LiveScreen", "========================================")
                    
                    firebaseManager.createLiveSession(
                        sessionId = liveSessionId,
                        userId = userId,
                        username = username,
                        channelName = liveChannelName,
                        title = "Live de $username"
                    )
                    
                    android.util.Log.d("LiveScreen", "✅ Sesión creada exitosamente")
                } catch (e: Exception) {
                    android.util.Log.e("LiveScreen", "❌ Error creando sesión: ${e.message}", e)
                }
            }
        }
        
        LiveRecordingScreen(
            sessionId = liveSessionId,
            channelName = liveChannelName,
            agoraToken = liveAgoraToken,
            onStreamStarted = {
                android.util.Log.d("LiveScreen", "✅ Transmisión iniciada (Agora conectado)")
            },
            onStreamEnded = {
                android.util.Log.d("LiveScreen", "🛑 Transmisión finalizada")
                // Actualizar Firestore: Live terminó
                scope.launch {
                    try {
                        firebaseManager.endLiveSession(liveSessionId)
                    } catch (e: Exception) {
                        android.util.Log.e("LiveScreen", "Error finalizando sesión: ${e.message}")
                    }
                }
                showBroadcasterScreen = false
                liveSessionId = ""
                liveChannelName = ""
                liveAgoraToken = ""
            }
        )
    }
    
    // NUEVA: Pantalla de visualización de Live (Espectador/Viewer)
    if (showViewerScreen) {
        LiveStreamViewerScreen(
            sessionId = liveSessionId,
            channelName = liveChannelName,
            agoraToken = liveAgoraToken,
            streamerName = liveStreamerName,
            onExit = {
                android.util.Log.d("LiveScreen", "🚪 Saliendo del Live como espectador")
                showViewerScreen = false
                liveSessionId = ""
                liveChannelName = ""
                liveAgoraToken = ""
                liveStreamerName = ""
            }
        )
    }
    
    // OVERLAY DE DEBUG (temporal - mostrar info en pantalla)
    if (showDebugOverlay && !showBroadcasterScreen && !showViewerScreen && !showLiveLauncher) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🔍 DEBUG INFO",
                            color = PopArtColors.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        TextButton(
                            onClick = { showDebugOverlay = false }
                        ) {
                            Text("Cerrar", color = Color.White, fontSize = 10.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        debugInfo,
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

// ============ REPRODUCTOR DE VIDEO CON EXOPLAYER ============

@Composable
fun VideoPlayerComp(
    player: ExoPlayer,
    videoUrl: String,
    isPaused: Boolean,
    onVideoEnded: () -> Unit = {}
) {
    // 1. Verificar si el video ya está cargado (para evitar recargas innecesarias)
    LaunchedEffect(videoUrl) {
        val mediaItem = MediaItem.fromUri(videoUrl)
        val currentMediaItem = player.currentMediaItem?.localConfiguration?.uri?.toString()
        
        // Si el video actual del reproductor no coincide o está inactivo, cargarlo.
        if (currentMediaItem != videoUrl || player.playbackState == Player.STATE_IDLE) {
            android.util.Log.d("VideoPlayerComp", "🎬 Cargando/Configurando video (Force): $videoUrl")
            player.setMediaItem(mediaItem, true)
            player.prepare()
            player.repeatMode = Player.REPEAT_MODE_OFF
        } else {
            // Si el video ya está cargado (incluso si fue precargado), solo reiniciar desde 0.
            player.seekTo(0)
            player.repeatMode = Player.REPEAT_MODE_OFF
            android.util.Log.d("VideoPlayerComp", "✅ Video ya configurado. Reiniciando a 0ms.")
        }
    }
    
    // 2. Listener para detectar cuando el video termina
    DisposableEffect(player, videoUrl) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    android.util.Log.d("VideoPlayerComp", "🏁 Video terminado, avanzando al siguiente")
                    onVideoEnded()
                }
            }
        }
        
        player.addListener(listener)
        
        onDispose {
            player.removeListener(listener)
        }
    }
    
    // 3. Manejo del estado de pausa
    LaunchedEffect(isPaused) {
        player.playWhenReady = !isPaused
        player.volume = 1f // Asegurar que el volumen esté al máximo
        android.util.Log.d("VideoPlayerComp", "⏯️ PlayWhenReady: ${!isPaused}")
    }
    
    // 3. Integración del PlayerView de Android con Compose
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        }
    )
}

// ============ REPRODUCTOR DE VIDEO CON INDICADOR DE CARGA ============

@Composable
fun VideoPlayerWithLoader(
    player: ExoPlayer,
    videoUrl: String,
    isPaused: Boolean,
    isCurrentPage: Boolean,
    onVideoEnded: () -> Unit = {}
) {
    // Estado para saber cuándo está listo el video
    var isReady by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    
    // Listener para detectar cuando el video se carga y empieza
    DisposableEffect(player, videoUrl) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                // Notifica cuando el primer frame está listo para ser renderizado
                isReady = true
                isBuffering = false
                android.util.Log.d("VideoLoader", "✅ Primer frame renderizado para $videoUrl")
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        isBuffering = true
                        android.util.Log.d("VideoLoader", "⏳ Buffering video...")
                    }
                    Player.STATE_READY -> {
                        isBuffering = false
                        android.util.Log.d("VideoLoader", "✅ Video listo para reproducir")
                    }
                    Player.STATE_ENDED -> {
                        android.util.Log.d("VideoLoader", "🏁 Video terminado")
                        if (isCurrentPage) {
                            onVideoEnded()
                        }
                    }
                    else -> {}
                }
            }
        }
        
        player.addListener(listener)
        
        onDispose {
            player.removeListener(listener)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // El reproductor de video
        VideoPlayerComp(
            player = player,
            videoUrl = videoUrl,
            isPaused = isPaused || !isCurrentPage,
            onVideoEnded = onVideoEnded
        )
        
        // Overlay: Muestra el indicador de carga si no está listo o está buffering
        if (!isReady || isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = PopArtColors.Yellow,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (!isReady) "Cargando video..." else "Buffering...",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// PANTALLA PRINCIPAL: Carrusel de videos de concursos (tipo TikTok/Reels)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContestVideoCarouselScreen(
    videos: List<ContestEntry>,
    colors: AppColors,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onStartLive: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseManager = remember { FirebaseManager() }
    val authManager = remember { AuthManager(context) }
    
    // Obtener el CacheDataSourceFactory (se inicializa una vez)
    val cacheDataSourceFactory = remember {
        ExoPlayerCache.getCacheDataSourceFactory(context)
    }
    
    // Estado del Pager para desplazamiento vertical fluido
    val pagerState = rememberPagerState(
        initialPage = currentIndex,
        pageCount = { videos.size }
    )
    
    // Estado para controlar pausa/reproducción
    var isPaused by remember { mutableStateOf(false) }
    
    // Estados para interacciones (likes por video)
    val likedVideos = remember { mutableStateMapOf<String, Boolean>() }
    val videoLikeCounts = remember { mutableStateMapOf<String, Int>() }
    
    // Estado para mostrar comentarios
    var showComments by remember { mutableStateOf(false) }
    var selectedVideoForComments by remember { mutableStateOf<ContestEntry?>(null) }
    
    // Mantener la pantalla encendida mientras se ven videos
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        android.util.Log.d("LiveCarousel", "🔆 Pantalla mantenida encendida")
        
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            android.util.Log.d("LiveCarousel", "🌙 Pantalla puede apagarse normalmente")
        }
    }
    
    // Cargar estado de likes al cambiar de video
    LaunchedEffect(pagerState.currentPage) {
        if (videos.isNotEmpty() && pagerState.currentPage < videos.size) {
            val currentVideo = videos[pagerState.currentPage]
            val userId = authManager.getUserId() ?: ""
            
            // Verificar si ya dio like
            if (!likedVideos.containsKey(currentVideo.id)) {
                val hasLiked = firebaseManager.hasUserLikedVideo(currentVideo.id, userId)
                likedVideos[currentVideo.id] = hasLiked
            }
            
            // Inicializar contador de likes
            if (!videoLikeCounts.containsKey(currentVideo.id)) {
                videoLikeCounts[currentVideo.id] = currentVideo.likes
            }
            
            // Incrementar vistas
            firebaseManager.incrementVideoViews(currentVideo.id)
        }
    }
    
    // Estado para animación de "fin de lista"
    var showEndOfListIndicator by remember { mutableStateOf(false) }
    
    // Detectar cuando se llega al final
    LaunchedEffect(pagerState.currentPage) {
        showEndOfListIndicator = pagerState.currentPage == videos.size - 1
    }
    
    // Función para avanzar al siguiente video
    val advanceToNextVideo: () -> Unit = {
        scope.launch {
            val nextPage = pagerState.currentPage + 1
            if (nextPage < videos.size) {
                android.util.Log.d("LiveCarousel", "⏭️ Avanzando automáticamente al video ${nextPage + 1}")
                pagerState.animateScrollToPage(nextPage)
            } else {
                android.util.Log.d("LiveCarousel", "🔄 Fin de la lista, volviendo al inicio")
                pagerState.animateScrollToPage(0)
            }
        }
    }
    
    // Pool de reproductores para precarga
    val playerMap = remember { mutableMapOf<Int, ExoPlayer>() }
    
    // Función para obtener o crear un ExoPlayer CON CACHÉ
    val getPlayer: (Int) -> ExoPlayer = remember(cacheDataSourceFactory) {
        { index ->
            playerMap.getOrPut(index) {
                android.util.Log.d("PlayerPool", "✨ Creando Player con caché para índice $index")
                ExoPlayer.Builder(context)
                    .setMediaSourceFactory(
                        androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                            // CRUCIAL: Usar el caché como fuente de datos principal
                            .setDataSourceFactory(cacheDataSourceFactory)
                    )
                    .build()
                    .apply {
                        // CRUCIAL: Silenciar el reproductor al crearlo
                        volume = 0f
                        // CRUCIAL: Preparar el reproductor inmediatamente
                        prepare()
                    }
            }
        }
    }
    
    // Lifecycle observer para pausar cuando la app va a segundo plano
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Pausar todos los reproductores cuando la app va a segundo plano
                    android.util.Log.d("LiveCarousel", "⏸️ App en segundo plano - Pausando videos")
                    playerMap.values.forEach { player ->
                        player.playWhenReady = false
                    }
                    isPaused = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Reanudar solo el video actual cuando la app vuelve
                    android.util.Log.d("LiveCarousel", "▶️ App en primer plano - Reanudando video actual")
                    val currentPlayer = playerMap[pagerState.currentPage]
                    currentPlayer?.playWhenReady = !isPaused
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Detectar cambio de página y notificar al padre
    LaunchedEffect(pagerState.currentPage) {
        isPaused = false
        onIndexChange(pagerState.currentPage)
        android.util.Log.d("LiveCarousel", "📹 Pager cambió a video: ${pagerState.currentPage}")
    }
    
    // ============ REEMPLAZAR ESTE BLOQUE COMPLETO ============
    // Pool de reproductores: Inicialización y liberación de recursos
    val currentPage = pagerState.currentPage
    DisposableEffect(context, videos, currentPage) {
        android.util.Log.d("PlayerPool", "🔄 DisposedEffect activado. Página actual: $currentPage")
        
        // --- Lógica de PRECARGA y PREPARACIÓN ---
        val prefetchRange = 3 // Precarga los siguientes 3 videos para máxima fluidez
        val totalVideos = videos.size
        val pagesToPreload = (currentPage + 1..currentPage + prefetchRange)
            .filter { it < totalVideos }
        
        // Paso 1: Precargar y preparar los reproductores de los videos siguientes
        pagesToPreload.forEach { index ->
            val videoEntry = videos[index]
            if (videoEntry.videoUrl.isNotEmpty()) {
                val player = getPlayer(index)
                val mediaItem = MediaItem.fromUri(videoEntry.videoUrl)
                
                // CRUCIAL: Solo preparar si aún no está en un estado listo.
                // Si el Player no tiene un MediaItem cargado, establecerlo y prepararlo.
                val currentMediaItem = player.currentMediaItem?.localConfiguration?.uri?.toString()
                if (currentMediaItem != videoEntry.videoUrl || player.playbackState == Player.STATE_IDLE) {
                    android.util.Log.d("PlayerPool", "🚀 Preparando video precargado para índice $index: ${videoEntry.videoUrl.take(20)}...")
                    
                    // Usamos seekTo(0) para asegurar que empiece desde el inicio
                    player.setMediaItem(mediaItem, 0)
                    player.prepare()
                    
                    // CRUCIAL: Le decimos al reproductor que esté listo para reproducir (aunque esté pausado por el VideoPlayerWithLoader)
                    player.playWhenReady = true
                    player.volume = 0f // Silenciar para la precarga, solo reproducirá con volumen al activarse
                }
            }
        }
        
        // --- Lógica de LIBERACIÓN (Limpieza) ---
        val pagesToKeep = (currentPage - 1..currentPage + prefetchRange).toSet()
        val playersToRemove = playerMap.keys
            .filter { it !in pagesToKeep }
            .toList() // Hacer una copia para evitar errores de concurrencia al modificar playerMap
        
        playersToRemove.forEach { index ->
            playerMap.remove(index)?.release()
            android.util.Log.d("PlayerPool", "🗑️ Liberando Player para índice $index")
        }
        
        // --- LIMPIEZA FINAL ---
        onDispose {
            android.util.Log.d("PlayerPool", "🧹 Limpieza de recursos al salir del Composable")
            // No liberar la caché aquí si la aplicación sigue viva
            playerMap.values.forEach { it.release() }
            playerMap.clear()
            ExoPlayerCache.release() // Llamar a la liberación de caché al final de la pantalla principal
        }
    }
    // ============ FIN DEL BLOQUE DE REEMPLAZO ============
    
    // Estados para gestos y animaciones
    var showLikeAnimation by remember { mutableStateOf(false) }
    var likeAnimationPosition by remember { mutableStateOf(Offset.Zero) }
    var isLongPressing by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                // Detectar swipe horizontal para catálogo/configuración
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount < -100) {
                        onSwipeLeft()
                    } else if (dragAmount > 100) {
                        onSwipeRight()
                    }
                }
            }
            .pointerInput(Unit) {
                // Detectar gestos: tap, doble tap, long press
                detectTapGestures(
                    onTap = { offset ->
                        // Tap simple: pausar/reanudar
                        isPaused = !isPaused
                        val currentPlayer = playerMap[pagerState.currentPage]
                        currentPlayer?.playWhenReady = !isPaused
                        android.util.Log.d("LiveCarousel", "⏯️ Tap: Pausa -> $isPaused")
                    },
                    onDoubleTap = { offset ->
                        // Doble tap: dar like rápido
                        val currentVideo = videos[pagerState.currentPage]
                        scope.launch {
                            val userId = authManager.getUserId() ?: ""
                            if (userId.isNotEmpty()) {
                                val newLikeState = firebaseManager.toggleLikeContestVideo(currentVideo.id, userId)
                                likedVideos[currentVideo.id] = newLikeState
                                
                                // Actualizar contador local
                                val currentCount = videoLikeCounts[currentVideo.id] ?: currentVideo.likes
                                videoLikeCounts[currentVideo.id] = if (newLikeState) currentCount + 1 else currentCount - 1
                                
                                // Mostrar animación de corazón
                                if (newLikeState) {
                                    likeAnimationPosition = offset
                                    showLikeAnimation = true
                                    android.util.Log.d("LiveCarousel", "❤️ Doble tap: Like dado en posición $offset")
                                }
                            }
                        }
                    },
                    onLongPress = { offset ->
                        // Long press: pausar mientras se mantiene presionado
                        isLongPressing = true
                        isPaused = true
                        val currentPlayer = playerMap[pagerState.currentPage]
                        currentPlayer?.playWhenReady = false
                        android.util.Log.d("LiveCarousel", "⏸️ Long press: Video pausado")
                    },
                    onPress = {
                        // Detectar cuando se suelta el long press
                        tryAwaitRelease()
                        if (isLongPressing) {
                            isLongPressing = false
                            isPaused = false
                            val currentPlayer = playerMap[pagerState.currentPage]
                            currentPlayer?.playWhenReady = true
                            android.util.Log.d("LiveCarousel", "▶️ Long press released: Video reanudado")
                        }
                    }
                )
            }
    ) {
        // Indicador de "Fin de videos" con animación
        if (showEndOfListIndicator) {
            val endIndicatorAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500),
                label = "endIndicatorAlpha"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .graphicsLayer { this.alpha = endIndicatorAlpha }
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🎬",
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Fin de los videos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        // Animación de corazón al hacer doble tap
        if (showLikeAnimation) {
            val likeScale by animateFloatAsState(
                targetValue = if (showLikeAnimation) 1.5f else 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "likeScale",
                finishedListener = {
                    showLikeAnimation = false
                }
            )
            
            val likeAlpha by animateFloatAsState(
                targetValue = if (showLikeAnimation) 0f else 1f,
                animationSpec = tween(durationMillis = 800),
                label = "likeAlpha"
            )
            
            Icon(
                Icons.Default.Favorite,
                contentDescription = "Like",
                tint = PopArtColors.Pink,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            likeAnimationPosition.x.toInt() - 50,
                            likeAnimationPosition.y.toInt() - 50
                        )
                    }
                    .size(100.dp)
                    .scale(likeScale)
                    .alpha(1f - likeAlpha)
            )
        }
        
        if (videos.isEmpty()) {
            // Sin videos de concursos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎬", fontSize = 120.sp)
                Spacer(Modifier.height(24.dp))
                Text(
                    "No hay videos de concursos aún",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Sé el primero en participar",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // VerticalPager para swipe fluido con inercia y animaciones
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentVideo = videos[page]
                
                // Calcular el offset de la página para animaciones
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                
                // Animación de escala: la página actual es más grande
                val scale = 1f - (pageOffset.absoluteValue * 0.1f).coerceIn(0f, 0.1f)
                
                // Animación de alpha: fade in/out durante transición
                val alpha = 1f - (pageOffset.absoluteValue * 0.5f).coerceIn(0f, 0.5f)
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Aplicar transformaciones suaves
                            this.scaleX = scale
                            this.scaleY = scale
                            this.alpha = alpha
                            
                            // Efecto de profundidad: las páginas no activas se alejan
                            this.translationY = pageOffset * 50f
                        }
                ) {
                    // 🎬 REPRODUCTOR DE VIDEO con Player Pool y Loader
                    if (currentVideo.videoUrl.isNotEmpty()) {
                        VideoPlayerWithLoader(
                            player = getPlayer(page),
                            videoUrl = currentVideo.videoUrl,
                            isPaused = isPaused,
                            isCurrentPage = page == pagerState.currentPage,
                            onVideoEnded = {
                                // Solo avanzar si este es el video actual
                                if (page == pagerState.currentPage) {
                                    advanceToNextVideo()
                                }
                            }
                        )
                    } else {
                        // Fallback si no hay URL
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "⚠️",
                                fontSize = 120.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Video no disponible",
                                fontSize = 20.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Overlay oscuro para mejor legibilidad con animación
                    val overlayAlpha by animateFloatAsState(
                        targetValue = if (page == pagerState.currentPage) 1f else 0.5f,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                        label = "overlayAlpha"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f * overlayAlpha),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f * overlayAlpha)
                                    )
                                )
                            )
                    )
                    
                    // Icono "LIVE" clickeable en esquina superior izquierda
                    IconButton(
                        onClick = onStartLive,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_live),
                            contentDescription = "Iniciar Live",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    // Indicador estático de swipe en esquina superior derecha
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "<<<",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
            
            // Información del video en la parte inferior con animación de entrada
            val infoAlpha by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 1f else 0f,
                animationSpec = tween(durationMillis = 400, delayMillis = 100),
                label = "infoAlpha"
            )
            
            val infoTranslationY by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 0f else 50f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "infoTranslationY"
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
                    .fillMaxWidth(0.65f)
                    .graphicsLayer {
                        this.alpha = infoAlpha
                        this.translationY = infoTranslationY
                    }
            ) {
                // Perfil del usuario (foto + nombre) - Clickeable
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        android.util.Log.d("LiveCarousel", "👤 Navegando al perfil de: ${currentVideo.username} (${currentVideo.userId})")
                        onNavigateToProfile(currentVideo.userId)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        // Foto de perfil
                        if (currentVideo.profilePictureUrl.isNotEmpty()) {
                            // Usar AsyncImage de Coil para cargar la imagen
                            AsyncImage(
                                model = currentVideo.profilePictureUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Avatar placeholder con inicial
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = PopArtColors.Pink
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        currentVideo.username.firstOrNull()?.uppercase() ?: "U",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        // Nombre del usuario
                        Text(
                            if (currentVideo.username.isNotEmpty()) currentVideo.username else "Usuario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                
                // Título con sombra
                Text(
                    if (currentVideo.title.isNotEmpty()) currentVideo.title else "Video de concurso",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                
                Spacer(Modifier.height(6.dp))
                
                // Descripción con sombra
                if (currentVideo.description.isNotEmpty()) {
                    Text(
                        currentVideo.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 3f
                            )
                        ),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(10.dp))
                }
                
                // Badge del concurso
                if (currentVideo.contestId.isNotEmpty()) {
                    Surface(
                        color = PopArtColors.Yellow,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            currentVideo.contestId,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                } else {
                    Surface(
                        color = PopArtColors.Yellow.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Concurso",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
            
            // Botones de interacción (lado derecho) con animación escalonada
            val buttonsAlpha by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 1f else 0f,
                animationSpec = tween(durationMillis = 400, delayMillis = 200),
                label = "buttonsAlpha"
            )
            
            val buttonsScale by animateFloatAsState(
                targetValue = if (page == pagerState.currentPage) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "buttonsScale"
            )
            
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .graphicsLayer {
                        this.alpha = buttonsAlpha
                        this.scaleX = buttonsScale
                        this.scaleY = buttonsScale
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Likes
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isLiked = likedVideos[currentVideo.id] ?: false
                    val likeCount = videoLikeCounts[currentVideo.id] ?: currentVideo.likes
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                val userId = authManager.getUserId() ?: ""
                                if (userId.isNotEmpty()) {
                                    val newLikeState = firebaseManager.toggleLikeContestVideo(currentVideo.id, userId)
                                    likedVideos[currentVideo.id] = newLikeState
                                    
                                    // Actualizar contador local
                                    val currentCount = videoLikeCounts[currentVideo.id] ?: currentVideo.likes
                                    videoLikeCounts[currentVideo.id] = if (newLikeState) currentCount + 1 else currentCount - 1
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = if (isLiked) PopArtColors.Pink else Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        formatViewers(likeCount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Comentarios
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            selectedVideoForComments = currentVideo
                            showComments = true
                            isPaused = true // Pausar video al abrir comentarios
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Comentarios",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        "💬",
                        fontSize = 12.sp
                    )
                }
                
                // Compartir
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            // Compartir usando Intent de Android
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Mira este video en HypeMatch")
                                putExtra(android.content.Intent.EXTRA_TEXT, 
                                    "¡Mira este increíble video de ${currentVideo.username}!\n\n" +
                                    "${currentVideo.title}\n\n" +
                                    "Video: ${currentVideo.videoUrl}"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir video"))
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        formatViewers(currentVideo.views),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
                    // Indicador de posición con animación
                    val indicatorAlpha by animateFloatAsState(
                        targetValue = if (page == pagerState.currentPage) 0.7f else 0.3f,
                        animationSpec = tween(durationMillis = 300),
                        label = "indicatorAlpha"
                    )
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 80.dp)
                            .graphicsLayer { this.alpha = indicatorAlpha },
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "${page + 1} / ${videos.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        // Diálogo de comentarios
        if (showComments && selectedVideoForComments != null) {
            CommentsBottomSheet(
                video = selectedVideoForComments!!,
                onDismiss = {
                    showComments = false
                    isPaused = false // Reanudar video al cerrar comentarios
                },
                firebaseManager = firebaseManager,
                authManager = authManager
            )
        }
    }
}

// ============ DIÁLOGO DE COMENTARIOS ============

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    video: ContestEntry,
    onDismiss: () -> Unit,
    firebaseManager: FirebaseManager,
    authManager: AuthManager
) {
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<VideoComment>>(emptyList()) }
    var newComment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }
    
    // Cargar comentarios
    LaunchedEffect(video.id) {
        isLoading = true
        comments = firebaseManager.getVideoComments(video.id)
        isLoading = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .pointerInput(Unit) {
                    detectTapGestures { /* Evitar que el tap cierre el diálogo */ }
                },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Comentarios",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = PopArtColors.Black
                        )
                    }
                }
                
                Divider()
                
                // Lista de comentarios
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PopArtColors.Pink)
                    }
                } else if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💬", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Sé el primero en comentar",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(comments.size) { index ->
                            val comment = comments[index]
                            CommentItem(comment)
                            if (index < comments.size - 1) {
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }
                
                Divider()
                
                // Input de nuevo comentario
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un comentario...") },
                        enabled = !isSending,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopArtColors.Pink,
                            cursorColor = PopArtColors.Pink
                        )
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (newComment.isNotBlank()) {
                                scope.launch {
                                    isSending = true
                                    try {
                                        val userId = authManager.getUserId() ?: ""
                                        val username = authManager.getUserName()
                                        
                                        android.util.Log.d("Comments", "📝 Agregando comentario:")
                                        android.util.Log.d("Comments", "  UserId: '$userId'")
                                        android.util.Log.d("Comments", "  Username: '$username'")
                                        android.util.Log.d("Comments", "  VideoId: '${video.id}'")
                                        android.util.Log.d("Comments", "  Comentario: '$newComment'")
                                        
                                        if (userId.isEmpty()) {
                                            android.util.Log.e("Comments", "❌ Error: userId está vacío")
                                            return@launch
                                        }
                                        
                                        if (username.isEmpty()) {
                                            android.util.Log.e("Comments", "❌ Error: username está vacío")
                                            return@launch
                                        }
                                        
                                        firebaseManager.addCommentToVideo(
                                            videoId = video.id,
                                            userId = userId,
                                            username = username,
                                            comment = newComment
                                        )
                                        
                                        android.util.Log.d("Comments", "✅ Comentario agregado exitosamente")
                                        
                                        // Recargar comentarios
                                        comments = firebaseManager.getVideoComments(video.id)
                                        newComment = ""
                                    } catch (e: Exception) {
                                        android.util.Log.e("Comments", "❌ Error agregando comentario: ${e.message}")
                                        android.util.Log.e("Comments", "Stack trace:", e)
                                    } finally {
                                        isSending = false
                                    }
                                }
                            }
                        },
                        enabled = newComment.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PopArtColors.Pink
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = if (newComment.isNotBlank()) PopArtColors.Pink else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: VideoComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar placeholder
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = PopArtColors.Pink.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    comment.username.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Pink
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                comment.username,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PopArtColors.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                comment.text,  // Usar "text" en lugar de "comment"
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                formatTimestamp(comment.timestamp),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// Función auxiliar para formatear timestamp
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> "${diff / 604800000}sem"
    }
}

// Pantalla de transmisiones en vivo (secundaria)
@Composable
fun LiveStreamViewerScreen(
    lives: List<LiveStream>,
    colors: AppColors,
    onBack: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var currentLiveIndex by remember { mutableStateOf(0) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LiveViewerScreen(
            live = lives[currentLiveIndex],
            colors = colors,
            onMenuClick = onBack,
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            onSwipeUp = {
                if (currentLiveIndex < lives.size - 1) {
                    currentLiveIndex++
                }
            },
            onSwipeDown = {
                if (currentLiveIndex > 0) {
                    currentLiveIndex--
                }
            }
        )
    }
}

// Pantalla cuando no hay lives activos
@Composable
fun NoLivesScreen(
    colors: AppColors,
    onMenuClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onBack: () -> Unit = {}
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            swipeOffset < -200 -> onSwipeLeft() // Swipe izquierda -> Catálogo
                            swipeOffset > 200 -> onSwipeRight() // Swipe derecha -> Configuración
                        }
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeOffset += dragAmount
                    }
                )
            }
    ) {
        // Botón de regreso
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = colors.text
            )
        }
        
        // Contenido central
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "📡",
                fontSize = 120.sp
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Actualmente no hay transmisiones en vivo",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = colors.text,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Desliza a la izquierda para ver el catálogo de eventos y categorías",
                fontSize = 16.sp,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Botón para ir al catálogo
            Button(
                onClick = {
                    android.util.Log.d("NoLivesScreen", "🔘 Botón 'Ver Catálogo' presionado")
                    onSwipeLeft()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PopArtColors.Pink
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Ver Catálogo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Indicador visual de swipe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "o desliza a la izquierda",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

// Pantalla de visualización de live
@Composable
fun LiveViewerScreen(
    live: LiveStream,
    colors: AppColors,
    onMenuClick: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    var verticalSwipeOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            swipeOffset < -200 -> onSwipeLeft() // Swipe izquierda -> Catálogo
                            swipeOffset > 200 -> onSwipeRight() // Swipe derecha -> Configuración
                        }
                        swipeOffset = 0f
                        verticalSwipeOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        swipeOffset += dragAmount
                        
                        // Detectar swipe vertical también
                        verticalSwipeOffset += change.position.y - change.previousPosition.y
                        
                        if (verticalSwipeOffset < -200) {
                            onSwipeUp()
                            verticalSwipeOffset = 0f
                        } else if (verticalSwipeOffset > 200) {
                            onSwipeDown()
                            verticalSwipeOffset = 0f
                        }
                    }
                )
            }
    ) {
        // Simulación de video en vivo (fondo con emoji grande)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                live.emoji,
                fontSize = 200.sp,
                modifier = Modifier.alpha(0.3f)
            )
        }
        
        // Overlay oscuro para mejor legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        
        // Header con info del live
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de cerrar
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Badge de EN VIVO
                Surface(
                    color = PopArtColors.Pink,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "EN VIVO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Contador de espectadores
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            formatViewers(live.viewers),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        // Info del artista en la parte inferior
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(20.dp)
        ) {
            Text(
                live.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    live.location,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Indicador de swipe
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Desliza para ver catálogo",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Pantalla de catálogo (Lives y Concursos)
@Composable
fun LiveCatalogScreen(
    lives: List<Concert>,
    fastContests: List<Contest>,
    highImpactContests: List<Contest>,
    colors: AppColors,
    onBack: () -> Unit,
    onContestClick: (Contest) -> Unit,
    onLiveClick: (Int) -> Unit,
    onStartLive: () -> Unit
) {
    var currentTab by remember { mutableStateOf(0) } // 0 = Lives, 1 = Concursos
    var contestSubTab by remember { mutableStateOf(0) } // 0 = Rápidos, 1 = Alto Impacto
    var swipeOffset by remember { mutableStateOf(0f) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset > 200) {
                            onBack() // Swipe derecha -> Volver al live
                        }
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeOffset += dragAmount
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header sin botón X
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Catálogo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.text,
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Spacer(Modifier.weight(1f))
                
                // Indicador de swipe para volver
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Desliza",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
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
            
            // Contenido
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (currentTab == 0) {
                    // Lives
                    item {
                        Text(
                            "Próximos eventos en vivo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(lives.size) { index ->
                        val live = lives[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLiveClick(index) },
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
                    // Concursos - Sub-tabs
                    item {
                        Column {
                            Text(
                                "Participa y gana premios",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Sub-tabs para tipos de concursos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { contestSubTab = 0 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (contestSubTab == 0) colors.primary else colors.surface
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "🚀 Rápidos",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (contestSubTab == 0) PopArtColors.Black else colors.text
                                    )
                                }
                                Button(
                                    onClick = { contestSubTab = 1 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (contestSubTab == 1) colors.primary else colors.surface
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        "🌟 Alto Impacto",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (contestSubTab == 1) PopArtColors.Black else colors.text
                                    )
                                }
                            }
                        }
                    }
                    
                    // Mostrar concursos según el sub-tab seleccionado
                    val contestsToShow = if (contestSubTab == 0) fastContests else highImpactContests
                    
                    // Descripción del tipo de concurso
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                if (contestSubTab == 0) {
                                    Text(
                                        "⚡ Concursos Rápidos",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.text
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Participa semanalmente y gana suscripciones premium",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "♾️",
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Participaciones ilimitadas",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                } else {
                                    Text(
                                        "🏆 Concursos de Alto Impacto",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.text
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Impulsa tu carrera con premios profesionales cada 3-6 meses",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "♾️",
                                            fontSize = 14.sp
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Participaciones ilimitadas",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    items(contestsToShow.size) { index ->
                        val contest = contestsToShow[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContestClick(contest) },
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
                                    // Badge de categoría
                                    Surface(
                                        color = PopArtColors.Black.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            contest.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PopArtColors.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        contest.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopArtColors.Black
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        contest.prize,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PopArtColors.Black.copy(alpha = 0.8f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        contest.deadline,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PopArtColors.Black.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Ver",
                                    tint = PopArtColors.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
                
                // Botón "Iniciar Live" al final
                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onStartLive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PopArtColors.Pink
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "INICIAR TRANSMISIÓN EN VIVO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// Función auxiliar para formatear números de viewers/likes
fun formatViewers(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}
