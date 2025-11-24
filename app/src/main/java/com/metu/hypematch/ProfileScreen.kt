



















package com.metu.hypematch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showUploadScreen by remember { mutableStateOf(false) }
    var showGalleryPicker by remember { mutableStateOf(false) }
    var isUploadingMedia by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var uploadType by remember { mutableStateOf("") } // "story", "profile", "cover", "gallery"
    var showStoryCamera by remember { mutableStateOf(false) }
    var showStoryOptions by remember { mutableStateOf(false) }
    var songMediaUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    var userStories by remember { mutableStateOf<List<ArtistStory>>(emptyList()) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showStoryViewer by remember { mutableStateOf(false) }
    var currentStoryIndex by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pressedStoryUrl by remember { mutableStateOf<String?>(null) }
    
    val isAnonymous = remember { authManager.isAnonymous() }
    val userId = authManager.getUserId() ?: ""
    
    // Función para recargar todos los datos
    suspend fun refreshProfile() {
        if (userId.isNotEmpty() && !isAnonymous) {
            try {
                android.util.Log.d("ProfileScreen", "🔄 Recargando perfil...")
                userProfile = firebaseManager.getFullUserProfile(userId)
                songMediaUrls = firebaseManager.getUserSongMedia(userId)
                userStories = firebaseManager.getUserStories(userId)
                android.util.Log.d("ProfileScreen", "✅ Perfil recargado")
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "❌ Error recargando: ${e.message}")
            }
        }
    }
    
    // Cargar perfil completo, medios de canciones e historias
    LaunchedEffect(userId) {
        if (userId.isNotEmpty() && !isAnonymous) {
            isLoading = true
            try {
                android.util.Log.d("ProfileScreen", "📝 Cargando perfil...")
                userProfile = firebaseManager.getFullUserProfile(userId)
                
                android.util.Log.d("ProfileScreen", "🎵 Cargando medios...")
                songMediaUrls = firebaseManager.getUserSongMedia(userId)
                
                android.util.Log.d("ProfileScreen", "📸 Cargando historias...")
                userStories = firebaseManager.getUserStories(userId)
                android.util.Log.d("ProfileScreen", "✅ ${userStories.size} historias cargadas")
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "❌ Error cargando datos: ${e.message}")
            } finally {
                isLoading = false
            }
        } else if (isAnonymous) {
            userProfile = UserProfile(username = "Invitado")
            isLoading = false
        }
    }
    
    // Función para recargar historias manualmente
    fun reloadStories() {
        scope.launch {
            try {
                android.util.Log.d("ProfileScreen", "�u Recargando historias...")
                val stories = firebaseManager.getUserStories(userId)
                userStories = stories
                android.util.Log.d("ProfileScreen", "✅ ${stories.size} historias cargadas")
                android.widget.Toast.makeText(context, "Historias: ${stories.size}", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("ProfileScreen", "❌ Error: ${e.message}")
                android.widget.Toast.makeText(context, "Error al cargar historias", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Launcher para imagen de perfil
    val profileImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploadingMedia = true
                    uploadType = "profile"
                    uploadProgress = 0
                    val imageUrl = firebaseManager.uploadProfileImage(it, userId, "profile") { progress ->
                        uploadProgress = progress
                    }
                    firebaseManager.updateProfileImage(userId, imageUrl)
                    userProfile = userProfile?.copy(profileImageUrl = imageUrl)
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "Error: ${e.message}")
                } finally {
                    isUploadingMedia = false
                    uploadType = ""
                }
            }
        }
    }
    
    // Launcher para imagen de portada
    val coverImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploadingMedia = true
                    uploadType = "cover"
                    uploadProgress = 0
                    val imageUrl = firebaseManager.uploadProfileImage(it, userId, "cover") { progress ->
                        uploadProgress = progress
                    }
                    firebaseManager.updateCoverImage(userId, imageUrl)
                    userProfile = userProfile?.copy(coverImageUrl = imageUrl)
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "Error: ${e.message}")
                } finally {
                    isUploadingMedia = false
                    uploadType = ""
                }
            }
        }
    }
    
    // Launcher para galería (fotos/videos)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploadingMedia = true
                    uploadType = "gallery"
                    uploadProgress = 0
                    val mediaUrl = firebaseManager.uploadGalleryMedia(it, userId, false) { progress ->
                        uploadProgress = progress
                    }
                    val updatedPhotos = userProfile?.galleryPhotos.orEmpty() + mediaUrl
                    firebaseManager.updateUserProfile(userId, galleryPhotos = updatedPhotos)
                    userProfile = userProfile?.copy(galleryPhotos = updatedPhotos)
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "Error: ${e.message}")
                } finally {
                    isUploadingMedia = false
                    uploadType = ""
                }
            }
        }
    }
    
    // Launcher para seleccionar imagen para historia
    val storyImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    isUploadingMedia = true
                    uploadType = "story"
                    uploadProgress = 0
                    
                    android.util.Log.d("ProfileScreen", "🚀 ===== SUBIENDO HISTORIA DESDE GALERÍA =====")
                    android.util.Log.d("ProfileScreen", "👤 Usuario: $userId - ${userProfile?.username}")
                    
                    // PASO 1: Subir el archivo de imagen a Firebase Storage (carpeta stories/)
                    android.util.Log.d("ProfileScreen", "📤 Paso 1: Subiendo archivo de imagen...")
                    val mediaUrl = firebaseManager.uploadStoryMedia(
                        uri = it,
                        userId = userId,
                        isVideo = false,
                        onProgress = { progress -> 
                            uploadProgress = progress
                            android.util.Log.d("ProfileScreen", "📊 Progreso: $progress%")
                        }
                    )
                    android.util.Log.d("ProfileScreen", "✅ Archivo subido exitosamente")
                    android.util.Log.d("ProfileScreen", "🔗 URL del archivo: $mediaUrl")
                    
                    // PASO 2: Crear el objeto ArtistStory con la metadata
                    android.util.Log.d("ProfileScreen", "📝 Paso 2: Creando objeto ArtistStory...")
                    val newStory = ArtistStory(
                        artistId = userId,
                        artistName = userProfile?.username ?: "Usuario",
                        artistImageUrl = userProfile?.profileImageUrl ?: "",
                        mediaUrl = mediaUrl,
                        mediaType = "image",
                        caption = "",
                        timestamp = System.currentTimeMillis(),
                        expiresAt = System.currentTimeMillis() + 86400000 // 24 horas
                    )
                    android.util.Log.d("ProfileScreen", "✅ Objeto ArtistStory creado")
                    
                    // PASO 3: Guardar la metadata en Firestore
                    android.util.Log.d("ProfileScreen", "💾 Paso 3: Guardando metadata en Firestore...")
                    val storyId = firebaseManager.uploadStoryMetadata(newStory)
                    
                    android.util.Log.d("ProfileScreen", "✅ ===== HISTORIA GUARDADA EXITOSAMENTE =====")
                    android.util.Log.d("ProfileScreen", "🆔 ID de la historia: $storyId")
                    
                    // Esperar y recargar historias con reintentos más agresivos
                    kotlinx.coroutines.delay(3000)
                    
                    var attempts = 0
                    var newStories: List<ArtistStory> = emptyList()
                    val maxAttempts = 5
                    
                    do {
                        attempts++
                        android.util.Log.d("ProfileScreen", "🔄 ===== INTENTO $attempts/$maxAttempts =====")
                        android.util.Log.d("ProfileScreen", "�  Buscando historias para: $userId")
                        android.util.Log.d("ProfileScreen", "📊 Historias actuales en UI: ${userStories.size}")
                        
                        newStories = firebaseManager.getUserStories(userId)
                        
                        android.util.Log.d("ProfileScreen", "📊 Historias obtenidas de Firebase: ${newStories.size}")
                        
                        if (newStories.isNotEmpty()) {
                            android.util.Log.d("ProfileScreen", "📸 Listado de historias:")
                            newStories.forEachIndexed { index, story ->
                                android.util.Log.d("ProfileScreen", "  [$index] ${story.id} - ${story.mediaUrl}")
                            }
                        }
                        
                        if (newStories.size > userStories.size) {
                            android.util.Log.d("ProfileScreen", "✅ ¡Nueva historia detectada! ${userStories.size} → ${newStories.size}")
                            break
                        }
                        
                        if (attempts < maxAttempts) {
                            android.util.Log.w("ProfileScreen", "⚠️ No se detectó cambio, esperando 2 segundos...")
                            kotlinx.coroutines.delay(2000)
                        }
                    } while (attempts < maxAttempts)
                    
                    val previousSize = userStories.size
                    userStories = newStories
                    android.util.Log.d("ProfileScreen", "📚 Estado actualizado: $previousSize → ${userStories.size}")
                    
                    android.widget.Toast.makeText(
                        context,
                        "✓ Historia publicada (${userStories.size})",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    
                } catch (e: Exception) {
                    android.util.Log.e("ProfileScreen", "❌ Error: ${e.message}", e)
                    android.widget.Toast.makeText(
                        context,
                        "Error: ${e.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isUploadingMedia = false
                    uploadType = ""
                    uploadProgress = 0
                }
            }
        }
    }

    // BottomSheet de opciones
    if (showStoryOptions) {
        ModalBottomSheet(
            onDismissRequest = { showStoryOptions = false },
            containerColor = PopArtColors.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Text(
                    "Opciones",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Opción: Tomar Foto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PopArtColors.Yellow.copy(alpha = 0.1f))
                        .border(2.dp, PopArtColors.Yellow, RoundedCornerShape(16.dp))
                        .clickable {
                            showStoryOptions = false
                            showStoryCamera = true
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PopArtColors.Yellow, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Tomar Foto",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black
                        )
                        Text(
                            "Abre la cámara para una nueva historia",
                            fontSize = 13.sp,
                            color = PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Opción: Galería
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PopArtColors.Cyan.copy(alpha = 0.1f))
                        .border(2.dp, PopArtColors.Cyan, RoundedCornerShape(16.dp))
                        .clickable {
                            showStoryOptions = false
                            storyImageLauncher.launch("image/*")
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PopArtColors.Cyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Seleccionar de Galería",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black
                        )
                        Text(
                            "Elige una foto existente",
                            fontSize = 13.sp,
                            color = PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Opción: Cambiar Foto de Perfil
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PopArtColors.Pink.copy(alpha = 0.1f))
                        .border(2.dp, PopArtColors.Pink, RoundedCornerShape(16.dp))
                        .clickable {
                            showStoryOptions = false
                            profileImageLauncher.launch("image/*")
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PopArtColors.Pink, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Cambiar Foto de Perfil",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black
                        )
                        Text(
                            "Actualiza tu imagen de perfil",
                            fontSize = 13.sp,
                            color = PopArtColors.Black.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }
    }
    
    if (showStoryCamera) {
        android.util.Log.d("ProfileScreen", "📷 StoryCamera está visible")
        StoryCamera(
            onBack = { 
                android.util.Log.d("ProfileScreen", "⬅️ onBack llamado")
                showStoryCamera = false
                isUploadingMedia = false
            },
            onPhotoTaken = { uri ->
                android.util.Log.d("ProfileScreen", "📸 ===== onPhotoTaken LLAMADO =====")
                android.util.Log.d("ProfileScreen", "📸 Foto capturada/seleccionada: $uri")
                
                // Cerrar la pantalla de cámara
                showStoryCamera = false
                
                // Iniciar subida inmediatamente
                scope.launch {
                    try {
                        isUploadingMedia = true
                        uploadType = "story"
                        uploadProgress = 0
                        
                        android.util.Log.d("ProfileScreen", "🚀 ===== INICIANDO SUBIDA DE HISTORIA =====")
                        android.util.Log.d("ProfileScreen", "👤 Usuario: $userId - ${userProfile?.username}")
                        
                        // PASO 1: Subir el archivo de imagen a Firebase Storage (carpeta stories/)
                        android.util.Log.d("ProfileScreen", "📤 Paso 1: Subiendo archivo de imagen...")
                        val mediaUrl = firebaseManager.uploadStoryMedia(
                            uri = uri,
                            userId = userId,
                            isVideo = false,
                            onProgress = { progress -> 
                                uploadProgress = progress
                                android.util.Log.d("ProfileScreen", "📊 Progreso de subida: $progress%")
                            }
                        )
                        android.util.Log.d("ProfileScreen", "✅ Archivo subido exitosamente")
                        android.util.Log.d("ProfileScreen", "🔗 URL del archivo: $mediaUrl")
                        
                        // PASO 2: Crear el objeto ArtistStory con la metadata
                        android.util.Log.d("ProfileScreen", "📝 Paso 2: Creando objeto ArtistStory...")
                        val newStory = ArtistStory(
                            artistId = userId,
                            artistName = userProfile?.username ?: "Usuario",
                            artistImageUrl = userProfile?.profileImageUrl ?: "",
                            mediaUrl = mediaUrl,
                            mediaType = "image",
                            caption = "",
                            timestamp = System.currentTimeMillis(),
                            expiresAt = System.currentTimeMillis() + 86400000 // 24 horas
                        )
                        android.util.Log.d("ProfileScreen", "✅ Objeto ArtistStory creado")
                        
                        // PASO 3: Guardar la metadata en Firestore
                        android.util.Log.d("ProfileScreen", "💾 Paso 3: Guardando metadata en Firestore...")
                        val storyId = firebaseManager.uploadStoryMetadata(newStory)
                        
                        android.util.Log.d("ProfileScreen", "✅ ===== HISTORIA GUARDADA EXITOSAMENTE =====")
                        android.util.Log.d("ProfileScreen", "🆔 ID de la historia: $storyId")
                        
                        // Esperar 3 segundos para que Firestore procese completamente
                        android.util.Log.d("ProfileScreen", "⏳ Esperando 3 segundos para que Firestore procese...")
                        kotlinx.coroutines.delay(3000)
                        
                        // Recargar historias con reintentos más agresivos
                        var attempts = 0
                        var newStories: List<ArtistStory> = emptyList()
                        val maxAttempts = 5
                        
                        do {
                            attempts++
                            android.util.Log.d("ProfileScreen", "🔄 ===== INTENTO $attempts/$maxAttempts =====")
                            android.util.Log.d("ProfileScreen", "� IBuscando historias para: $userId")
                            android.util.Log.d("ProfileScreen", "📊 Historias actuales en UI: ${userStories.size}")
                            
                            newStories = firebaseManager.getUserStories(userId)
                            
                            android.util.Log.d("ProfileScreen", "📊 Historias obtenidas de Firebase: ${newStories.size}")
                            
                            if (newStories.isNotEmpty()) {
                                android.util.Log.d("ProfileScreen", "📸 Listado de historias:")
                                newStories.forEachIndexed { index, story ->
                                    android.util.Log.d("ProfileScreen", "  [$index] ${story.id} - ${story.mediaUrl}")
                                }
                            }
                            
                            if (newStories.size > userStories.size) {
                                android.util.Log.d("ProfileScreen", "✅ ¡Nueva historia detectada! ${userStories.size} → ${newStories.size}")
                                break
                            }
                            
                            if (attempts < maxAttempts) {
                                android.util.Log.w("ProfileScreen", "⚠️ No se detectó cambio, esperando 2 segundos...")
                                kotlinx.coroutines.delay(2000)
                            }
                        } while (attempts < maxAttempts)
                        
                        // Actualizar el estado SIEMPRE
                        val previousSize = userStories.size
                        userStories = newStories
                        android.util.Log.d("ProfileScreen", "📚 Estado actualizado: $previousSize → ${userStories.size}")
                        
                        // Mostrar mensaje de éxito
                        android.widget.Toast.makeText(
                            context,
                            "✓ Historia publicada (${userStories.size})",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "❌ Error subiendo historia: ${e.message}", e)
                        e.printStackTrace()
                        android.widget.Toast.makeText(
                            context,
                            "Error: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isUploadingMedia = false
                        uploadType = ""
                        uploadProgress = 0
                    }
                }
            }
        )
    } else if (showUploadScreen) {
        UploadMusicScreen(onBack = { showUploadScreen = false })
    } else if (showEditDialog) {
        EditProfileDialog(
            currentBio = userProfile?.bio ?: "",
            currentCountry = userProfile?.socialLinks?.get("country") ?: "",
            currentGenres = userProfile?.socialLinks?.get("genres") ?: "",
            currentSocialLinks = userProfile?.socialLinks?.filterKeys { 
                it in listOf("instagram", "tiktok", "youtube")
            } ?: emptyMap(),
            onDismiss = { showEditDialog = false },
            onSave = { bio, country, genres, socialLinks ->
                scope.launch {
                    try {
                        val updatedLinks = userProfile?.socialLinks.orEmpty().toMutableMap()
                        updatedLinks["country"] = country
                        updatedLinks["genres"] = genres
                        updatedLinks.putAll(socialLinks)
                        
                        firebaseManager.updateUserProfile(userId, bio = bio, socialLinks = updatedLinks)
                        userProfile = userProfile?.copy(bio = bio, socialLinks = updatedLinks)
                        showEditDialog = false
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Error: ${e.message}")
                    }
                }
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // Header con menú
            HypeHeader(
                onMenuClick = onMenuClick,
                isDarkMode = isDarkMode,
                colors = colors,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    focusManager.clearFocus()
                                })
                            }
                    ) {
                    // Portada con sombra
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clickable { if (!isAnonymous) coverImageLauncher.launch("image/*") }
                        ) {
                            if (userProfile?.coverImageUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = userProfile?.coverImageUrl,
                                    contentDescription = "Portada",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(PopArtColors.MulticolorGradient)
                                )
                            }
                            
                            // Gradiente oscuro en la parte inferior para mejor contraste
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.4f)
                                            )
                                        )
                                    )
                            )
                            
                            if (!isAnonymous) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(16.dp)
                                        .size(40.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar portada",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Foto de perfil y nombre
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-60).dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    focusManager.clearFocus()
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box {
                                // Borde con gradiente si hay historias
                                if (userStories.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(140.dp)
                                            .background(
                                                brush = Brush.sweepGradient(
                                                    colors = listOf(
                                                        PopArtColors.Pink,
                                                        PopArtColors.Purple,
                                                        PopArtColors.Cyan,
                                                        PopArtColors.Yellow,
                                                        PopArtColors.Orange,
                                                        PopArtColors.Pink
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .size(140.dp)
                                        .then(
                                            if (userStories.isEmpty()) {
                                                Modifier.border(5.dp, PopArtColors.Yellow, CircleShape)
                                            } else {
                                                Modifier.padding(5.dp)
                                            }
                                        )
                                        .clip(CircleShape)
                                        .background(PopArtColors.Cyan)
                                        .then(
                                            // Solo hacer clickable si hay historias
                                            if (userStories.isNotEmpty()) {
                                                Modifier.clickable { 
                                                    android.util.Log.d("ProfileScreen", "👆 Click en foto de perfil - Abriendo visor")
                                                    currentStoryIndex = 0
                                                    showStoryViewer = true
                                                }
                                            } else {
                                                Modifier // No clickable si no hay historias
                                            }
                                        )
                                ) {
                                    if (userProfile?.profileImageUrl?.isNotEmpty() == true) {
                                        AsyncImage(
                                            model = userProfile?.profileImageUrl,
                                            contentDescription = "Perfil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            userProfile?.username?.firstOrNull()?.toString() ?: "?",
                                            fontSize = 56.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PopArtColors.Black,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                                
                                // Botón de compartir (más a la derecha, encima del +)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 18.dp, y = (-48).dp)
                                        .size(42.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(PopArtColors.Yellow, PopArtColors.Orange)
                                            ),
                                            shape = CircleShape
                                        )
                                        .border(3.dp, PopArtColors.Black, CircleShape)
                                        .clickable { showShareDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_share),
                                        contentDescription = "Compartir perfil",
                                        tint = PopArtColors.Black,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                
                                if (!isAnonymous) {
                                    // Icono de opciones (abajo a la derecha)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .size(42.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(PopArtColors.Pink, PopArtColors.Purple)
                                                ),
                                                shape = CircleShape
                                            )
                                            .border(3.dp, PopArtColors.Black, CircleShape)
                                            .clickable { 
                                                android.util.Log.d("ProfileScreen", "➕ Click en botón + - Abriendo opciones")
                                                showStoryOptions = true 
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Opciones",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Nombre con ícono de editar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    userProfile?.username ?: "Usuario",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text
                                )
                                
                                // Ícono de editar nombre
                                if (!isAnonymous) {
                                    var showEditNameDialog by remember { mutableStateOf(false) }
                                    
                                    IconButton(
                                        onClick = { showEditNameDialog = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_edit),
                                            contentDescription = "Editar nombre",
                                            tint = colors.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    // Diálogo para editar nombre
                                    if (showEditNameDialog) {
                                        EditNameDialog(
                                            currentName = userProfile?.username ?: "",
                                            onDismiss = { showEditNameDialog = false },
                                            onSave = { newName ->
                                                scope.launch {
                                                    try {
                                                        firebaseManager.updateUsername(userId, newName)
                                                        userProfile = userProfile?.copy(username = newName)
                                                        showEditNameDialog = false
                                                        android.util.Log.d("ProfileScreen", "✅ Nombre actualizado: $newName")
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("ProfileScreen", "❌ Error actualizando nombre: ${e.message}")
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            
                            if (userProfile?.isArtist == true) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(PopArtColors.Yellow, PopArtColors.Orange)
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .border(2.dp, PopArtColors.Black, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "🎤 Artista",
                                        fontSize = 14.sp,
                                        color = PopArtColors.Black,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(20.dp))
                            
                            // Estadísticas con fondo
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .background(
                                        color = colors.background.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .border(2.dp, PopArtColors.Yellow.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                    .padding(vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ProfileStatItem("${userProfile?.followers ?: 0}", "Seguidores", colors)
                                    ProfileStatItem("${userProfile?.following ?: 0}", "Siguiendo", colors)
                                    ProfileStatItem("${userProfile?.totalSongs ?: 0}", "Canciones", colors)
                                    ProfileStatItem(formatPlays(userProfile?.totalPlays ?: 0), "Plays", colors)
                                }
                            }
                        }
                    }
                    
                    // Mis Historias
                    if (userStories.isNotEmpty() || !isAnonymous) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 0.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        focusManager.clearFocus()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Mis Historias",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.text
                                    )
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                // Mensaje de debug si no hay historias
                                if (userStories.isEmpty() && !isAnonymous) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                            .background(
                                                color = PopArtColors.Pink.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(2.dp, PopArtColors.Pink.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "📸 No tienes historias aún",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.text,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "Toca el botón + para agregar tu primera historia",
                                                fontSize = 12.sp,
                                                color = colors.text.copy(alpha = 0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Botón para agregar nueva historia (primero si no hay historias)
                                    if (!isAnonymous) {
                                        item {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(85.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(
                                                                    PopArtColors.Yellow.copy(alpha = 0.2f),
                                                                    PopArtColors.Orange.copy(alpha = 0.2f)
                                                                )
                                                            )
                                                        )
                                                        .border(3.dp, PopArtColors.Yellow, CircleShape)
                                                        .clickable { 
                                                            android.util.Log.d("ProfileScreen", "➕ Abriendo opciones de historia")
                                                            showStoryOptions = true 
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Agregar historia",
                                                        tint = PopArtColors.Yellow,
                                                        modifier = Modifier.size(36.dp)
                                                    )
                                                }
                                                
                                                Spacer(Modifier.height(6.dp))
                                                
                                                Text(
                                                    if (userStories.isEmpty()) "Agregar" else "Nueva",
                                                    fontSize = 12.sp,
                                                    color = colors.text,
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    
                                    items(userStories.take(10)) { story ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(85.dp)
                                        ) {
                                            Box {
                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(CircleShape)
                                                        .border(3.dp, PopArtColors.Cyan, CircleShape)
                                                        .pointerInput(story.id) {
                                                            detectTapGestures(
                                                                onPress = {
                                                                    pressedStoryUrl = story.mediaUrl
                                                                    tryAwaitRelease()
                                                                    pressedStoryUrl = null
                                                                }
                                                            )
                                                        }
                                                ) {
                                                    AsyncImage(
                                                        model = story.mediaUrl,
                                                        contentDescription = "Historia",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    
                                                    // Indicador de video si es video
                                                    if (story.mediaType == "video") {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .background(Color.Black.copy(alpha = 0.4f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Default.PlayArrow,
                                                                contentDescription = "Video",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(28.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                // Botón de eliminar (fuera del círculo para mejor visibilidad)
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .offset(x = 4.dp, y = (-4).dp)
                                                        .size(28.dp)
                                                        .background(PopArtColors.Pink, CircleShape)
                                                        .border(2.dp, PopArtColors.Black, CircleShape)
                                                        .clickable {
                                                            scope.launch {
                                                                try {
                                                                    android.util.Log.d("ProfileScreen", "🗑️ Eliminando historia: ${story.id}")
                                                                    firebaseManager.deleteStory(story.id, userId)
                                                                    userStories = firebaseManager.getUserStories(userId)
                                                                    android.util.Log.d("ProfileScreen", "✅ Historia eliminada")
                                                                    android.widget.Toast.makeText(
                                                                        context,
                                                                        "Historia eliminada",
                                                                        android.widget.Toast.LENGTH_SHORT
                                                                    ).show()
                                                                } catch (e: Exception) {
                                                                    android.util.Log.e("ProfileScreen", "Error eliminando historia: ${e.message}")
                                                                    android.widget.Toast.makeText(
                                                                        context,
                                                                        "Error al eliminar",
                                                                        android.widget.Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_trash),
                                                        contentDescription = "Eliminar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(Modifier.height(6.dp))
                                            
                                            Text(
                                                story.caption.ifEmpty { "Historia" },
                                                fontSize = 12.sp,
                                                color = colors.text,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Sección de Highlights (Historias Destacadas)
                    item {
                        var highlightedStories by remember { mutableStateOf<List<Story>>(emptyList()) }
                        
                        LaunchedEffect(userId) {
                            if (userId.isNotEmpty() && !isAnonymous) {
                                highlightedStories = firebaseManager.getUserHighlightedStories(userId)
                                android.util.Log.d("ProfileScreen", "⭐ Highlights cargados: ${highlightedStories.size}")
                            }
                        }
                        
                        if (highlightedStories.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 8.dp)
                            ) {
                                Text(
                                    "⭐ Highlights",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(highlightedStories) { story ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(80.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(70.dp)
                                                    .clip(CircleShape)
                                                    .border(3.dp, PopArtColors.Yellow, CircleShape)
                                                    .clickable {
                                                        // Abrir visor de historia destacada
                                                        currentStoryIndex = 0
                                                        showStoryViewer = true
                                                    }
                                            ) {
                                                if (story.imageUrl.isNotEmpty()) {
                                                    AsyncImage(
                                                        model = story.imageUrl,
                                                        contentDescription = "Highlight",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } else if (story.videoUrl.isNotEmpty()) {
                                                    // Mostrar thumbnail del video o icono
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(PopArtColors.Cyan),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.PlayArrow,
                                                            contentDescription = "Video",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
                                                }
                                                
                                                // Icono de estrella en la esquina
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .size(20.dp)
                                                        .background(PopArtColors.Yellow, CircleShape)
                                                        .border(2.dp, PopArtColors.Black, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_star),
                                                        contentDescription = "Destacado",
                                                        tint = PopArtColors.Black,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(Modifier.height(4.dp))
                                            
                                            Text(
                                                formatTimeAgo(story.timestamp),
                                                fontSize = 11.sp,
                                                color = colors.text,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Línea divisoria con gradiente
                    if (!isAnonymous) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                PopArtColors.Yellow,
                                                PopArtColors.Orange,
                                                PopArtColors.Pink
                                            )
                                        )
                                    )
                            )
                        }
                    }
                    
                    // Botón Editar Perfil
                    if (!isAnonymous) {
                        item {
                            var showEditProfileDialog by remember { mutableStateOf(false) }
                            
                            Button(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 0.dp)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PopArtColors.Yellow
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_edit),
                                    contentDescription = null,
                                    tint = PopArtColors.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Editar Perfil",
                                    color = PopArtColors.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                            
                            // Diálogo para editar perfil completo
                            if (showEditProfileDialog) {
                                EditProfileDialog(
                                    currentBio = userProfile?.bio ?: "",
                                    currentCountry = userProfile?.socialLinks?.get("country") ?: "",
                                    currentGenres = userProfile?.socialLinks?.get("genres") ?: "",
                                    currentSocialLinks = userProfile?.socialLinks?.filterKeys { 
                                        it in listOf("instagram", "tiktok", "youtube")
                                    } ?: emptyMap(),
                                    onDismiss = { showEditProfileDialog = false },
                                    onSave = { bio, country, genres, socialLinks ->
                                        scope.launch {
                                            try {
                                                // Actualizar biografía, país, géneros y redes sociales
                                                val updatedLinks = userProfile?.socialLinks.orEmpty().toMutableMap()
                                                updatedLinks["country"] = country
                                                updatedLinks["genres"] = genres
                                                updatedLinks.putAll(socialLinks)
                                                
                                                firebaseManager.updateUserProfile(
                                                    userId,
                                                    bio = bio,
                                                    socialLinks = updatedLinks
                                                )
                                                
                                                userProfile = userProfile?.copy(
                                                    bio = bio,
                                                    socialLinks = updatedLinks
                                                )
                                                showEditProfileDialog = false
                                                android.util.Log.d("ProfileScreen", "✅ Perfil actualizado")
                                            } catch (e: Exception) {
                                                android.util.Log.e("ProfileScreen", "❌ Error: ${e.message}")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // Bio con fondo
                    if (userProfile?.bio?.isNotEmpty() == true) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .background(
                                        color = colors.background.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = PopArtColors.Cyan.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        focusManager.clearFocus()
                                    }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    userProfile?.bio ?: "",
                                    fontSize = 15.sp,
                                    color = colors.text,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                    
                    // País de origen
                    val country = userProfile?.socialLinks?.get("country")
                    if (!country.isNullOrEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                                    .background(
                                        color = PopArtColors.Yellow.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_mundo),
                                        contentDescription = "País",
                                        tint = PopArtColors.Yellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        country,
                                        fontSize = 15.sp,
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    // Géneros musicales favoritos
                    val genres = userProfile?.socialLinks?.get("genres")
                    if (!genres.isNullOrEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 6.dp)
                                    .background(
                                        color = PopArtColors.Cyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_genres),
                                        contentDescription = "Géneros",
                                        tint = PopArtColors.Cyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        genres,
                                        fontSize = 15.sp,
                                        color = colors.text,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    // Redes sociales
                    if (userProfile?.socialLinks?.isNotEmpty() == true) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    "Redes Sociales",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                userProfile?.socialLinks?.forEach { (platform, handle) ->
                                    // Solo mostrar las redes sociales principales, no country ni genres
                                    if (platform !in listOf("country", "genres")) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .background(
                                                    color = when (platform.lowercase()) {
                                                        "instagram" -> PopArtColors.Pink.copy(alpha = 0.1f)
                                                        "tiktok" -> PopArtColors.Cyan.copy(alpha = 0.1f)
                                                        "youtube" -> Color.Red.copy(alpha = 0.1f)
                                                        else -> colors.background.copy(alpha = 0.3f)
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = when (platform.lowercase()) {
                                                        "instagram" -> PopArtColors.Pink.copy(alpha = 0.3f)
                                                        "tiktok" -> PopArtColors.Cyan.copy(alpha = 0.3f)
                                                        "youtube" -> Color.Red.copy(alpha = 0.3f)
                                                        else -> colors.primary.copy(alpha = 0.3f)
                                                    },
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    openSocialLink(context, platform, handle)
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Usar iconos de drawable
                                                val iconRes = when (platform.lowercase()) {
                                                    "instagram" -> R.drawable.ic_instagram
                                                    "tiktok" -> R.drawable.ic_tiktok
                                                    "youtube" -> R.drawable.ic_youtube
                                                    else -> null
                                                }
                                                
                                                if (iconRes != null) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = iconRes),
                                                        contentDescription = platform,
                                                        tint = when (platform.lowercase()) {
                                                            "instagram" -> PopArtColors.Pink
                                                            "tiktok" -> PopArtColors.Cyan
                                                            "youtube" -> Color.Red
                                                            else -> colors.primary
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        getSocialIcon(platform),
                                                        fontSize = 24.sp
                                                    )
                                                }
                                                
                                                Spacer(Modifier.width(12.dp))
                                                
                                                Text(
                                                    if (handle.startsWith("@")) handle else "@$handle",
                                                    fontSize = 15.sp,
                                                    color = colors.text,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Galería
                    if (userProfile?.galleryPhotos?.isNotEmpty() == true) {
                        item {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    "Galería",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.text,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(userProfile?.galleryPhotos.orEmpty()) { photoUrl ->
                                        Box(
                                            modifier = Modifier.size(140.dp)
                                        ) {
                                            AsyncImage(
                                                model = photoUrl,
                                                contentDescription = "Foto",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .border(3.dp, PopArtColors.Yellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                                    .clickable { expandedImageUrl = photoUrl },
                                                contentScale = ContentScale.Crop
                                            )
                                            
                                            // Botón de eliminar (solo si no es anónimo)
                                            if (!isAnonymous) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(36.dp)
                                                        .background(
                                                            brush = Brush.radialGradient(
                                                                colors = listOf(
                                                                    PopArtColors.Pink,
                                                                    PopArtColors.Pink.copy(alpha = 0.8f)
                                                                )
                                                            ),
                                                            shape = CircleShape
                                                        )
                                                        .border(2.dp, Color.White, CircleShape)
                                                        .clickable {
                                                            scope.launch {
                                                                try {
                                                                    val updatedPhotos = userProfile?.galleryPhotos.orEmpty()
                                                                        .filter { it != photoUrl }
                                                                    firebaseManager.updateUserProfile(
                                                                        userId,
                                                                        galleryPhotos = updatedPhotos
                                                                    )
                                                                    userProfile = userProfile?.copy(galleryPhotos = updatedPhotos)
                                                                    android.util.Log.d("ProfileScreen", "✅ Foto eliminada")
                                                                } catch (e: Exception) {
                                                                    android.util.Log.e("ProfileScreen", "Error eliminando foto: ${e.message}")
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_trash),
                                                        contentDescription = "Eliminar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (!isAnonymous) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .size(140.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(
                                                                PopArtColors.Yellow.copy(alpha = 0.2f),
                                                                PopArtColors.Orange.copy(alpha = 0.2f)
                                                            )
                                                        )
                                                    )
                                                    .border(3.dp, PopArtColors.Yellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                                    .clickable { galleryLauncher.launch("image/*") },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = "Agregar",
                                                    tint = PopArtColors.Yellow,
                                                    modifier = Modifier.size(56.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!isAnonymous) {
                        item {
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Agregar fotos a la galería",
                                    color = PopArtColors.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    // Carrusel de Videos y Fotos de "Subir Música"
                    if (songMediaUrls.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Videos y Fotos Musicales",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.text
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(PopArtColors.Yellow, PopArtColors.Orange)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .border(2.dp, PopArtColors.Black, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "${songMediaUrls.size}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PopArtColors.Black
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.height(16.dp))
                                
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(songMediaUrls) { mediaUrl ->
                                        Box(
                                            modifier = Modifier.size(140.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(PopArtColors.Black)
                                                    .border(
                                                        3.dp,
                                                        PopArtColors.Cyan,
                                                        RoundedCornerShape(16.dp)
                                                    )
                                                    .clickable { 
                                                        // Solo expandir imágenes, no videos
                                                        if (!mediaUrl.contains(".mp4") && !mediaUrl.contains("video")) {
                                                            expandedImageUrl = mediaUrl
                                                        }
                                                    }
                                            ) {
                                                AsyncImage(
                                                    model = mediaUrl,
                                                    contentDescription = "Media",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                
                                                // Indicador de video
                                                if (mediaUrl.contains(".mp4") || mediaUrl.contains("video")) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .size(56.dp)
                                                            .background(
                                                                brush = Brush.radialGradient(
                                                                    colors = listOf(
                                                                        Color.Black.copy(alpha = 0.8f),
                                                                        Color.Black.copy(alpha = 0.6f)
                                                                    )
                                                                ),
                                                                shape = CircleShape
                                                            )
                                                            .border(2.dp, PopArtColors.Yellow, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            Icons.Default.PlayArrow,
                                                            contentDescription = "Video",
                                                            tint = PopArtColors.Yellow,
                                                            modifier = Modifier.size(36.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // Botón de eliminar (solo si no es anónimo)
                                            if (!isAnonymous) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(36.dp)
                                                        .background(
                                                            brush = Brush.radialGradient(
                                                                colors = listOf(
                                                                    PopArtColors.Pink,
                                                                    PopArtColors.Pink.copy(alpha = 0.8f)
                                                                )
                                                            ),
                                                            shape = CircleShape
                                                        )
                                                        .border(2.dp, Color.White, CircleShape)
                                                        .clickable {
                                                            scope.launch {
                                                                try {
                                                                    // Eliminar la canción que contiene este media
                                                                    firebaseManager.deleteSongByMediaUrl(userId, mediaUrl)
                                                                    
                                                                    // Recargar los medios
                                                                    songMediaUrls = firebaseManager.getUserSongMedia(userId)
                                                                    android.util.Log.d("ProfileScreen", "✅ Media eliminado")
                                                                } catch (e: Exception) {
                                                                    android.util.Log.e("ProfileScreen", "Error eliminando media: ${e.message}")
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_trash),
                                                        contentDescription = "Eliminar",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Botón Subir Música (solo para artistas)
                    if (!isAnonymous && userProfile?.isArtist == true) {
                        item {
                            Button(
                                onClick = { showUploadScreen = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PopArtColors.Pink
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 2.dp
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Subir Música",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    
                    item { Spacer(Modifier.height(100.dp)) }
                    }
                    
                    // Botón flotante de recarga
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                refreshProfile()
                                isRefreshing = false
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        containerColor = PopArtColors.Yellow,
                        contentColor = PopArtColors.Black
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PopArtColors.Black,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Recargar",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            
            // Indicador de carga
            if (isUploadingMedia) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PopArtColors.Yellow)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            when (uploadType) {
                                "story" -> "Subiendo historia..."
                                "profile" -> "Subiendo foto de perfil..."
                                "cover" -> "Subiendo portada..."
                                "gallery" -> "Subiendo foto..."
                                else -> "Subiendo..."
                            },
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "$uploadProgress%",
                            color = PopArtColors.Yellow,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            // Diálogo de imagen expandida
            if (expandedImageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { expandedImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Botón cerrar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { expandedImageUrl = null },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(PopArtColors.Yellow, CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = PopArtColors.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        // Imagen expandida
                        AsyncImage(
                            model = expandedImageUrl,
                            contentDescription = "Imagen expandida",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(16.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                        
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
            
            // Diálogo de compartir perfil
            if (showShareDialog) {
                ShareProfileDialog(
                    username = userProfile?.username ?: "Usuario",
                    userId = userId,
                    onDismiss = { showShareDialog = false }
                )
            }
            
            // Visor de historias
            if (showStoryViewer && userStories.isNotEmpty()) {
                StoryViewer(
                    stories = userStories,
                    initialIndex = currentStoryIndex,
                    onDismiss = { showStoryViewer = false },
                    onStoryChange = { index -> currentStoryIndex = index }
                )
            }
            
            // Preview de historia al mantener presionado
            if (pressedStoryUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable { pressedStoryUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = pressedStoryUrl,
                        contentDescription = "Historia",
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun StoryViewer(
    stories: List<ArtistStory>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    onStoryChange: (Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    val currentStory = stories.getOrNull(currentIndex)
    var isPaused by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var isImageLoaded by remember(currentIndex) { mutableStateOf(false) }
    
    // Animación suave de transición entre historias
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isImageLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "imageAlpha"
    )
    
    // Animación de progreso suave (60fps)
    LaunchedEffect(currentIndex, isPaused, isImageLoaded) {
        if (!isPaused && isImageLoaded) {
            progress = 0f
            val duration = 5000L
            val startTime = System.currentTimeMillis()
            
            while (progress < 1f && !isPaused && isImageLoaded) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                kotlinx.coroutines.delay(16) // ~60fps
            }
            
            // Avanzar automáticamente
            if (progress >= 1f && !isPaused) {
                if (currentIndex < stories.size - 1) {
                    isImageLoaded = false
                    currentIndex++
                    onStoryChange(currentIndex)
                } else {
                    onDismiss()
                }
            }
        }
    }
    
    // Precarga de imágenes (siguiente y anterior)
    val prevStory = stories.getOrNull(currentIndex - 1)
    val nextStory = stories.getOrNull(currentIndex + 1)
    
    nextStory?.let {
        AsyncImage(
            model = it.mediaUrl,
            contentDescription = null,
            modifier = Modifier.size(1.dp)
        )
    }
    
    prevStory?.let {
        AsyncImage(
            model = it.mediaUrl,
            contentDescription = null,
            modifier = Modifier.size(1.dp)
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        currentStory?.let { story ->
            // Imagen con transición suave
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder mientras carga
                if (!isImageLoaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = PopArtColors.Yellow,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                "Cargando...",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Imagen principal con fade in suave
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = "Historia",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = animatedAlpha),
                    contentScale = ContentScale.Fit,
                    onLoading = { 
                        isImageLoaded = false
                        isPaused = true
                    },
                    onSuccess = { 
                        isImageLoaded = true
                        isPaused = false
                    },
                    onError = { 
                        isImageLoaded = true
                        isPaused = false
                        // Auto-avanzar en caso de error
                        kotlinx.coroutines.GlobalScope.launch {
                            kotlinx.coroutines.delay(1000)
                            if (currentIndex < stories.size - 1) {
                                isImageLoaded = false
                                currentIndex++
                                onStoryChange(currentIndex)
                            } else {
                                onDismiss()
                            }
                        }
                    }
                )
            }
            
            // Áreas de tap sin feedback visual (más limpio)
            Row(modifier = Modifier.fillMaxSize()) {
                // Área izquierda - retroceder
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (currentIndex > 0) {
                                isImageLoaded = false
                                currentIndex--
                                onStoryChange(currentIndex)
                            }
                        }
                )
                
                // Área derecha - avanzar
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (currentIndex < stories.size - 1) {
                                isImageLoaded = false
                                currentIndex++
                                onStoryChange(currentIndex)
                            } else {
                                onDismiss()
                            }
                        }
                )
            }
            
            // Barra de progreso animada en la parte superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                stories.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        // Barra de progreso animada
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(
                                    when {
                                        index < currentIndex -> 1f
                                        index == currentIndex -> progress
                                        else -> 0f
                                    }
                                )
                                .background(Color.White, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
            
            // Información del usuario en la parte superior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 20.dp)
                    .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil pequeña
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PopArtColors.Cyan)
                ) {
                    if (story.artistImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = story.artistImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column {
                    Text(
                        story.artistName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        story.getTimeRemaining(),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                // Botón cerrar
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Caption si existe
            if (story.caption.isNotEmpty()) {
                Text(
                    story.caption,
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .padding(bottom = 40.dp)
                )
            }
        }
    }
}

// Función para generar código QR
fun generateQRCode(text: String, size: Int = 512): android.graphics.Bitmap {
    val writer = com.google.zxing.qrcode.QRCodeWriter()
    val bitMatrix = writer.encode(text, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
    
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
fun ShareProfileDialog(
    username: String,
    userId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profileUrl = "https://hypematch.app/profile/$userId" // URL del perfil
    
    // Generar código QR
    val qrBitmap = remember(profileUrl) {
        generateQRCode(profileUrl, 512)
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Compartir Perfil",
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Código QR real
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(4.dp, PopArtColors.Yellow, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Código QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Text(
                    "Escanea para ver el perfil",
                    fontSize = 14.sp,
                    color = PopArtColors.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                // Botones de acción
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Compartir perfil
                    Button(
                        onClick = {
                            val shareIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, 
                                    "¡Mira el perfil de $username en HypeMatch! $profileUrl")
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Compartir perfil"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Compartir Perfil", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                    }
                    
                    // Copiar enlace
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Profile URL", profileUrl)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Enlace copiado", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Cyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = PopArtColors.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Copiar Enlace", color = PopArtColors.White, fontWeight = FontWeight.Bold)
                    }
                    
                    // Descargar QR (por ahora solo muestra un mensaje)
                    Button(
                        onClick = {
                            android.widget.Toast.makeText(context, "Función de descarga próximamente", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Pink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = PopArtColors.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Descargar QR", color = PopArtColors.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = PopArtColors.White
    )
}

@Composable
private fun ProfileStatItem(value: String, label: String, colors: AppColors = getAppColors(false)) {
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
            color = colors.text,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatPlays(plays: Long): String {
    return when {
        plays >= 1_000_000 -> String.format("%.1fM", plays / 1_000_000.0)
        plays >= 1_000 -> String.format("%.1fK", plays / 1_000.0)
        else -> plays.toString()
    }
}

fun getSocialIcon(platform: String): String {
    return when (platform.lowercase()) {
        "instagram" -> "📷"
        "youtube" -> "▶️"
        "tiktok" -> "🎵"
        "twitter", "x" -> "🐦"
        "facebook" -> "👥"
        "spotify" -> "🎧"
        "soundcloud" -> "☁️"
        else -> "🔗"
    }
}

fun openSocialLink(context: android.content.Context, platform: String, handle: String) {
    val url = when (platform.lowercase()) {
        "instagram" -> "https://instagram.com/${handle.removePrefix("@")}"
        "youtube" -> "https://youtube.com/@${handle.removePrefix("@")}"
        "tiktok" -> "https://tiktok.com/@${handle.removePrefix("@")}"
        "twitter", "x" -> "https://twitter.com/${handle.removePrefix("@")}"
        "facebook" -> "https://facebook.com/${handle.removePrefix("@")}"
        "spotify" -> "https://open.spotify.com/artist/$handle"
        "soundcloud" -> "https://soundcloud.com/${handle.removePrefix("@")}"
        else -> handle // Si ya es una URL completa
    }
    
    try {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("ProfileScreen", "Error al abrir enlace: ${e.message}")
    }
}


@Composable
fun EditProfileDialog(
    currentBio: String,
    currentCountry: String,
    currentGenres: String,
    currentSocialLinks: Map<String, String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Map<String, String>) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    var country by remember { mutableStateOf(currentCountry) }
    var genres by remember { mutableStateOf(currentGenres) }
    var instagram by remember { mutableStateOf(currentSocialLinks["instagram"] ?: "") }
    var tiktok by remember { mutableStateOf(currentSocialLinks["tiktok"] ?: "") }
    var youtube by remember { mutableStateOf(currentSocialLinks["youtube"] ?: "") }
    var showCountryMenu by remember { mutableStateOf(false) }
    
    val countryOptions = listOf(
        "Perú", "Argentina", "Chile", "Colombia", "México",
        "España", "Estados Unidos", "Brasil", "Ecuador",
        "Venezuela", "Uruguay", "Paraguay", "Bolivia"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Perfil",
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column {
                        Text(
                            "Biografía",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Cuéntanos sobre ti...") },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                cursorColor = PopArtColors.Yellow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                item {
                    Column {
                        Text(
                            "País de origen",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Box {
                            OutlinedTextField(
                                value = country,
                                onValueChange = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCountryMenu = true },
                                enabled = false,
                                placeholder = { Text("Selecciona tu país") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = PopArtColors.Black,
                                    disabledBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                    disabledLabelColor = PopArtColors.Black
                                ),
                                trailingIcon = {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_mundo),
                                        contentDescription = "Seleccionar país",
                                        tint = PopArtColors.Yellow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            DropdownMenu(
                                expanded = showCountryMenu,
                                onDismissRequest = { showCountryMenu = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(PopArtColors.White)
                            ) {
                                countryOptions.forEach { countryOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                countryOption,
                                                color = PopArtColors.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            country = countryOption
                                            showCountryMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    Column {
                        Text(
                            "Géneros musicales favoritos",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = genres,
                            onValueChange = { genres = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ej: Rock, Pop, Jazz") },
                            trailingIcon = {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_genres),
                                    contentDescription = "Géneros musicales",
                                    tint = PopArtColors.Yellow,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                cursorColor = PopArtColors.Yellow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                item {
                    Column {
                        Text(
                            "Redes Sociales",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PopArtColors.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Instagram
                        OutlinedTextField(
                            value = instagram,
                            onValueChange = { instagram = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("@usuario") },
                            label = { Text("Instagram") },
                            leadingIcon = {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    tint = PopArtColors.Pink,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                cursorColor = PopArtColors.Yellow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // TikTok
                        OutlinedTextField(
                            value = tiktok,
                            onValueChange = { tiktok = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("@usuario") },
                            label = { Text("TikTok") },
                            leadingIcon = {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_tiktok),
                                    contentDescription = "TikTok",
                                    tint = PopArtColors.Cyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                cursorColor = PopArtColors.Yellow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        // YouTube
                        OutlinedTextField(
                            value = youtube,
                            onValueChange = { youtube = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("@canal") },
                            label = { Text("YouTube") },
                            leadingIcon = {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_youtube),
                                    contentDescription = "YouTube",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                                cursorColor = PopArtColors.Yellow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val socialLinks = mutableMapOf<String, String>()
                    if (instagram.isNotBlank()) socialLinks["instagram"] = instagram
                    if (tiktok.isNotBlank()) socialLinks["tiktok"] = tiktok
                    if (youtube.isNotBlank()) socialLinks["youtube"] = youtube
                    onSave(bio, country, genres, socialLinks)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow)
            ) {
                Text("Guardar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PopArtColors.Black)
            }
        },
        containerColor = PopArtColors.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSocialLinkDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf("Instagram") }
    var handle by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val platforms = listOf(
        "Instagram", "YouTube", "TikTok", "Twitter", "Facebook", 
        "Spotify", "SoundCloud", "Apple Music", "Otro"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Agregar Red Social",
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector de plataforma
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPlatform,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plataforma") },
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopArtColors.Yellow,
                            unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        platforms.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform) },
                                onClick = {
                                    selectedPlatform = platform
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Campo de usuario/handle
                OutlinedTextField(
                    value = handle,
                    onValueChange = { handle = it },
                    label = { Text("Usuario/Handle") },
                    placeholder = { Text("@usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PopArtColors.Yellow,
                        unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.3f),
                        cursorColor = PopArtColors.Yellow
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (handle.isNotEmpty()) {
                        onAdd(selectedPlatform, handle)
                    }
                },
                enabled = handle.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow)
            ) {
                Text("Agregar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PopArtColors.Black)
            }
        },
        containerColor = PopArtColors.White
    )
}

// Componente de cámara para historias
@Composable
fun StoryCamera(
    onBack: () -> Unit,
    onPhotoTaken: (Uri) -> Unit
) {
    val context = LocalContext.current
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var cameraLaunched by remember { mutableStateOf(false) }
    
    // Launcher para tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        android.util.Log.d("StoryCamera", "📸 Resultado de cámara: success=$success, uri=$capturedImageUri")
        if (success && capturedImageUri != null) {
            android.util.Log.d("StoryCamera", "✅ Foto capturada exitosamente")
            onPhotoTaken(capturedImageUri!!)
        } else {
            android.util.Log.w("StoryCamera", "⚠️ Foto no capturada o cancelada")
            cameraLaunched = false
        }
    }
    
    // Launcher para seleccionar de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        android.util.Log.d("StoryCamera", "🖼️ Resultado de galería: uri=$uri")
        uri?.let {
            android.util.Log.d("StoryCamera", "✅ Imagen seleccionada de galería")
            onPhotoTaken(it)
        } ?: android.util.Log.w("StoryCamera", "⚠️ No se seleccionó ninguna imagen")
    }
    
    // Pantalla con opciones
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black),
        contentAlignment = Alignment.Center
    ) {
        if (cameraLaunched) {
            // Mientras se abre la cámara
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = PopArtColors.Yellow)
                Text(
                    "Abriendo cámara...",
                    color = PopArtColors.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Opciones: Cámara o Galería
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(40.dp)
            ) {
                Text(
                    "Agregar Historia",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Yellow
                )
                
                Spacer(Modifier.height(20.dp))
                
                // Botón Cámara
                Button(
                    onClick = {
                        android.util.Log.d("StoryCamera", "📷 Botón Tomar Foto presionado")
                        cameraLaunched = true
                        try {
                            val photoFile = java.io.File(
                                context.cacheDir,
                                "story_${System.currentTimeMillis()}.jpg"
                            )
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                photoFile
                            )
                            capturedImageUri = uri
                            android.util.Log.d("StoryCamera", "📁 Archivo temporal creado: $uri")
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            android.util.Log.e("StoryCamera", "❌ Error abriendo cámara: ${e.message}", e)
                            cameraLaunched = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_camara),
                        contentDescription = null,
                        tint = PopArtColors.Black,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Tomar Foto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Black
                    )
                }
                
                // Botón Galería
                Button(
                    onClick = { 
                        android.util.Log.d("StoryCamera", "🖼️ Botón Galería presionado")
                        galleryLauncher.launch("image/*") 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Cyan),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = PopArtColors.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "Seleccionar de Galería",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.White
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                // Botón Cancelar
                TextButton(onClick = onBack) {
                    Text(
                        "Cancelar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Yellow
                    )
                }
            }
        }
    }
}


// Diálogo para editar nombre de usuario
@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var errorMessage by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Nombre",
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        errorMessage = when {
                            it.isBlank() -> "El nombre no puede estar vacío"
                            it.length < 3 -> "El nombre debe tener al menos 3 caracteres"
                            it.length > 20 -> "El nombre no puede tener más de 20 caracteres"
                            else -> ""
                        }
                    },
                    label = { Text("Nuevo nombre") },
                    singleLine = true,
                    isError = errorMessage.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PopArtColors.Yellow,
                        focusedLabelColor = PopArtColors.Yellow
                    )
                )
                
                if (errorMessage.isNotEmpty()) {
                    Text(
                        errorMessage,
                        color = PopArtColors.Pink,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && newName.length >= 3 && newName.length <= 20) {
                        onSave(newName)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                enabled = newName.isNotBlank() && newName.length >= 3 && newName.length <= 20
            ) {
                Text("Guardar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PopArtColors.Black)
            }
        },
        containerColor = PopArtColors.White
    )
}


// Esta función ya no se usa - se reemplazó por la versión completa arriba
