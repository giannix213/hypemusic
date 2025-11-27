package com.metu.hypematch

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// Pantalla para ver un Live
@Composable
fun LiveViewerScreen(concert: Concert, onBack: () -> Unit) {
    val context = LocalContext.current
    var isLive by remember { mutableStateOf(true) }
    var viewerCount by remember { mutableStateOf((100..5000).random()) }
    var showComments by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        // Aquí iría el reproductor de video
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PopArtColors.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "📹",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "LIVE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Pink
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    concert.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.White
                )
            }
        }
        
        // Header con info del live
        Column(
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
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de volver
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PopArtColors.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                
                // Indicador de LIVE
                if (isLive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(PopArtColors.Pink, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "LIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                
                // Contador de viewers
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(PopArtColors.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Viewers",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        viewerCount.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // Controles en la parte inferior
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PopArtColors.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de like
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(56.dp)
                    .background(PopArtColors.Pink, CircleShape)
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = Color.White
                )
            }
            
            // Botón de comentarios
            IconButton(
                onClick = { showComments = !showComments },
                modifier = Modifier
                    .size(56.dp)
                    .background(PopArtColors.Yellow, CircleShape)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = "Comentarios",
                    tint = PopArtColors.Black
                )
            }
            
            // Botón de compartir
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(56.dp)
                    .background(PopArtColors.Cyan, CircleShape)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = PopArtColors.Black
                )
            }
        }
    }
}

// Pantalla para grabar un Live
@Composable
fun LiveRecordingScreen(onBack: () -> Unit, onVideoRecorded: (Uri) -> Unit) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }
    var permissionsDenied by remember { mutableStateOf(false) }
    var showGalleryPicker by remember { mutableStateOf(false) }
    
    // Launcher para seleccionar video de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            android.util.Log.d("LivesScreen", "📸 Video seleccionado de galería: $uri")
            onVideoRecorded(uri)
        } else {
            android.util.Log.d("LivesScreen", "❌ No se seleccionó ningún video")
        }
        showGalleryPicker = false
    }
    
    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        
        hasPermissions = cameraGranted && audioGranted
        
        if (!hasPermissions) {
            permissionsDenied = true
        } else {
            permissionsDenied = false
        }
    }
    
    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        val cameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        val audioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        
        hasPermissions = cameraPermission && audioPermission
        
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        if (!hasPermissions) {
            // Pantalla de permisos simplificada
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📹", fontSize = 80.sp)
                Spacer(Modifier.height(24.dp))
                Text(
                    "Permisos Necesarios",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Yellow
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Para grabar videos necesitamos acceso a la cámara y micrófono",
                    fontSize = 16.sp,
                    color = PopArtColors.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "CONCEDER PERMISOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Black
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onBack) {
                    Text("Cancelar", color = PopArtColors.White)
                }
            }
        } else {
            // Usar la pantalla de cámara
            CameraRecordingScreen(
                onBack = onBack,
                onVideoRecorded = { uri ->
                    android.util.Log.d("LivesScreen", "📹 Video grabado exitosamente: $uri")
                    onVideoRecorded(uri)
                },
                onOpenGallery = {
                    android.util.Log.d("LivesScreen", "📸 Abriendo selector de galería")
                    galleryLauncher.launch("video/*")
                }
            )
        }
    }
}

// Pantalla para ver videos de concurso
@Composable
fun ContestVideosScreen(onBack: () -> Unit) {
    val firebaseManager = remember { FirebaseManager() }
    var contestEntries by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        try {
            contestEntries = firebaseManager.getAllContestEntries()
            isLoading = false
        } catch (e: Exception) {
            android.util.Log.e("ContestVideos", "Error cargando videos: ${e.message}")
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = PopArtColors.Yellow
                )
            }
            Text(
                "Videos del Concurso",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.weight(1f)
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PopArtColors.Yellow)
            }
        } else if (contestEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎬", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No hay videos aún",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.White
                    )
                    Text(
                        "¡Sé el primero en participar!",
                        fontSize = 14.sp,
                        color = PopArtColors.White.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(contestEntries) { entry ->
                    ContestVideoCard(
                        entry = entry,
                        onClick = {
                            // TODO: Abrir reproductor de video
                            android.util.Log.d("ContestVideos", "Reproducir video: ${entry.id}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContestVideoCard(entry: ContestEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = PopArtColors.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PopArtColors.Black, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = PopArtColors.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    entry.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Black
                )
                Text(
                    entry.title,
                    fontSize = 14.sp,
                    color = PopArtColors.Black.copy(alpha = 0.7f),
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = PopArtColors.Pink,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            entry.likes.toString(),
                            fontSize = 12.sp,
                            color = PopArtColors.Black
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Views",
                            tint = PopArtColors.Cyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            entry.views.toString(),
                            fontSize = 12.sp,
                            color = PopArtColors.Black
                        )
                    }
                }
            }
        }
    }
}

// Pantalla de detalles del concurso con navegación por swipe
@Composable
fun ContestDetailScreen(
    contest: Contest, 
    onBack: () -> Unit, 
    onRecordVideo: () -> Unit, 
    onViewGallery: () -> Unit = {},
    onUploadVideo: (Uri) -> Unit = {} // Nuevo callback para subir video de galería
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    // Estados
    var showParticipants by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) } // 0 = Info, 1 = Galería
    var myVideos by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var allVideos by remember { mutableStateOf<List<ContestEntry>>(emptyList()) }
    var isLoadingVideos by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableStateOf(0f) }
    var isUploadingVideo by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    
    val userId = authManager.getUserId() ?: ""
    
    // Launcher para seleccionar video de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            android.util.Log.d("ContestDetail", "📸 Video seleccionado: $uri")
            // Subir video directamente
            scope.launch {
                try {
                    isUploadingVideo = true
                    uploadProgress = 0
                    
                    val username = authManager.getUserName()
                    
                    android.util.Log.d("ContestDetail", "🎬 Subiendo video a concurso...")
                    
                    // Paso 1: Subir video a Storage
                    val videoUrl = firebaseManager.uploadContestVideo(
                        uri = uri,
                        userId = userId,
                        onProgress = { progress ->
                            uploadProgress = progress
                            android.util.Log.d("ContestDetail", "📊 Progreso: $progress%")
                        }
                    )
                    
                    // Paso 2: Obtener foto de perfil
                    val userProfile = firebaseManager.getUserProfile(userId)
                    val profilePictureUrl = userProfile?.profileImageUrl ?: ""
                    
                    // Paso 3: Crear entrada en Firestore
                    val entryId = firebaseManager.createContestEntry(
                        userId = userId,
                        username = username,
                        videoUrl = videoUrl,
                        title = "Video de ${contest.name}",
                        description = "Participación en ${contest.name}",
                        contestId = contest.name,
                        profilePictureUrl = profilePictureUrl
                    )
                    
                    android.util.Log.d("ContestDetail", "✅ Video publicado: $entryId")
                    
                    // Recargar videos
                    val entries = firebaseManager.getAllContestEntries()
                    val contestEntries = entries.filter { it.contestId == contest.name }
                    allVideos = contestEntries
                    myVideos = contestEntries.filter { it.userId == userId }
                    
                    isUploadingVideo = false
                    
                } catch (e: Exception) {
                    android.util.Log.e("ContestDetail", "❌ Error subiendo video: ${e.message}")
                    isUploadingVideo = false
                }
            }
        }
    }
    
    // Cargar videos del concurso
    LaunchedEffect(contest.name) {
        isLoadingVideos = true
        try {
            val entries = firebaseManager.getAllContestEntries()
            // Filtrar por concurso
            val contestEntries = entries.filter { it.contestId == contest.name }
            allVideos = contestEntries
            myVideos = contestEntries.filter { it.userId == userId }
            android.util.Log.d("ContestDetail", "Videos cargados: ${allVideos.size} total, ${myVideos.size} míos")
        } catch (e: Exception) {
            android.util.Log.e("ContestDetail", "Error cargando videos: ${e.message}")
        } finally {
            isLoadingVideos = false
        }
    }
    
    val participants = remember {
        listOf(
            "🎤 María López - 1,234 votos",
            "🎸 Carlos Ruiz - 987 votos",
            "🎧 Ana García - 856 votos",
            "🎵 Luis Martínez - 654 votos",
            "🎹 Sofia Torres - 543 votos"
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset < -200 && allVideos.isNotEmpty()) {
                            // Swipe left - abrir galería
                            android.util.Log.d("ContestDetail", "⬅️ Swipe detectado, abriendo galería")
                            onViewGallery()
                        }
                        swipeOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        swipeOffset += dragAmount
                    }
                )
            }
    ) {
        // Header fijo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = PopArtColors.Yellow
                )
            }
            Text(
                "Detalles del Concurso",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Contenido con scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Info del concurso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = contest.color)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            contest.emoji,
                            fontSize = 60.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            contest.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            contest.prize,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            contest.deadline,
                            fontSize = 14.sp,
                            color = PopArtColors.Black.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showParticipants = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!showParticipants) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Info",
                            fontWeight = FontWeight.Black,
                            color = if (!showParticipants) PopArtColors.Black else PopArtColors.White
                        )
                    }
                    Button(
                        onClick = { showParticipants = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showParticipants) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            "Participantes",
                            fontWeight = FontWeight.Black,
                            color = if (showParticipants) PopArtColors.Black else PopArtColors.White
                        )
                    }
                }
            }
            
            // Contenido dinámico
            if (showParticipants) {
                // Lista de participantes
                items(participants) { participant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PopArtColors.White)
                    ) {
                        Text(
                            participant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                // Información del concurso
                item {
                    Column {
                        Text(
                            "Reglas:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Yellow
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        val rules = listOf(
                            "• Graba un video de máximo 60 segundos",
                            "• Muestra tu mejor talento musical",
                            "• El video con más votos gana",
                            "• Puedes participar solo una vez",
                            "• Contenido original únicamente",
                            "• Respeta las normas de la comunidad",
                            "• El ganador será anunciado al finalizar el concurso"
                        )
                        
                        rules.forEach { rule ->
                            Text(
                                rule,
                                fontSize = 14.sp,
                                color = PopArtColors.White,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            "Premio:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Yellow
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            contest.prize,
                            fontSize = 16.sp,
                            color = PopArtColors.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Espaciado final para el botón
            item {
                Spacer(Modifier.height(80.dp))
            }
        }
        
        // Botones fijos en la parte inferior
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PopArtColors.Black,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para ver galería
                OutlinedButton(
                    onClick = onViewGallery,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PopArtColors.Yellow
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PopArtColors.Yellow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ver galería",
                        tint = PopArtColors.Yellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "VER GALERÍA DE VIDEOS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Yellow
                        )
                        if (!isLoadingVideos) {
                            Text(
                                "${myVideos.size} tuyos • ${allVideos.size} total",
                                fontSize = 11.sp,
                                color = PopArtColors.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Botón para subir video de galería
                Button(
                    onClick = {
                        android.util.Log.d("ContestDetail", "📸 Abriendo selector de galería")
                        galleryLauncher.launch("video/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Cyan),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploadingVideo
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Subir video",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isUploadingVideo) "SUBIENDO... $uploadProgress%" else "SUBIR MI VIDEO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                
                // Botón para grabar
                Button(
                    onClick = onRecordVideo,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Pink),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploadingVideo
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Grabar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "GRABAR MI VIDEO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
    
    // Diálogo de carga mientras se sube el video
    if (isUploadingVideo) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar mientras sube */ },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PopArtColors.Pink
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Subiendo video...")
                }
            },
            text = {
                Column {
                    Text("Por favor espera mientras se sube tu video al concurso.")
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = PopArtColors.Pink
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$uploadProgress%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {}
        )
    }
}