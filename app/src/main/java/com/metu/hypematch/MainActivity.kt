package com.metu.hypematch

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.metu.hypematch.ui.theme.HypeMatchTheme
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.e("TEST_HISTORIAS", "========================================")
        android.util.Log.e("TEST_HISTORIAS", "APP INICIADA - LOGS FUNCIONANDO")
        android.util.Log.e("TEST_HISTORIAS", "========================================")
        enableEdgeToEdge()
        setContent {
            HypeMatchTheme {
                HypeMatchApp()
            }
        }
    }
}

@Composable
fun HypeMatchApp() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val themeManager = remember { ThemeManager(context) }
    val scope = rememberCoroutineScope()
    var showWelcome by rememberSaveable { mutableStateOf(true) }
    var isAuthenticated by rememberSaveable { mutableStateOf(authManager.isUserLoggedIn()) }
    var hasUsername by remember { mutableStateOf<Boolean?>(null) }
    var hasSelectedRole by remember { mutableStateOf<Boolean?>(null) }
    var isArtist by remember { mutableStateOf(false) }
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.DISCOVER) }
    
    // Estado para ver perfil de otro usuario
    var viewingUserId by remember { mutableStateOf<String?>(null) }
    var showOtherUserProfile by remember { mutableStateOf(false) }
    
    // Estado del tema
    val isDarkMode by themeManager.isDarkModeFlow.collectAsState(initial = false)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val colors = getAppColors(isDarkMode)
    
    // Cargar perfil del usuario desde Firebase
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            val userId = authManager.getUserId()
            val isAnonymous = authManager.isAnonymous()
            
            if (userId != null) {
                if (isAnonymous) {
                    // Usuario invitado: asignar nombre genérico automáticamente
                    val guestName = "Invitado_${userId.take(6)}"
                    firebaseManager.saveUserProfile(userId, guestName, false)
                    hasUsername = true
                    hasSelectedRole = true
                    isArtist = false
                } else {
                    // Usuario registrado: cargar perfil
                    val profile = firebaseManager.getUserProfile(userId)
                    if (profile != null) {
                        hasUsername = profile.username.isNotEmpty()
                        hasSelectedRole = true // Si el perfil existe, ya seleccionó rol
                        isArtist = profile.isArtist
                    } else {
                        hasUsername = false
                        hasSelectedRole = false
                    }
                }
            }
        }
    }

    if (showWelcome) {
        WelcomeScreen(onContinue = { showWelcome = false })
    } else if (!isAuthenticated) {
        AuthScreen(onAuthSuccess = { isAuthenticated = true })
    } else if (hasUsername == null || hasSelectedRole == null) {
        // Cargando perfil...
        Box(
            modifier = Modifier.fillMaxSize().background(PopArtColors.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PopArtColors.Yellow)
        }
    } else if (hasUsername == false) {
        UsernameSelectionScreen(
            onUsernameSelected = { username ->
                scope.launch {
                    try {
                        val userId = authManager.getUserId()
                        if (userId != null) {
                            firebaseManager.saveUserProfile(userId, username, isArtist)
                            hasUsername = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HypeMatchApp", "Error al guardar username: ${e.message}")
                    }
                }
            }
        )
    } else if (hasSelectedRole == false) {
        RoleSelectionScreen(
            onRoleSelected = { selectedIsArtist ->
                scope.launch {
                    try {
                        val userId = authManager.getUserId()
                        if (userId != null) {
                            isArtist = selectedIsArtist
                            // Actualizar solo el rol, sin tocar el username
                            firebaseManager.updateUserRole(userId, selectedIsArtist)
                            hasSelectedRole = true
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HypeMatchApp", "Error al guardar rol: ${e.message}")
                    }
                }
            }
        )
    } else {
        ModernNavigationDrawer(
            drawerState = drawerState,
            isDarkMode = isDarkMode,
            onThemeToggle = {
                scope.launch {
                    themeManager.toggleTheme()
                }
            },
            onLogout = {
                isAuthenticated = false
                showWelcome = true
            }
        ) {
            NavigationSuiteScaffold(
                navigationSuiteItems = {
                    AppDestinations.entries.forEach {
                        item(
                            icon = {
                                Icon(
                                    it.icon,
                                    contentDescription = it.label,
                                    tint = if (it == currentDestination) PopArtColors.Black else PopArtColors.Black.copy(alpha = 0.6f)
                                )
                            },
                            label = {
                                Text(
                                    it.label,
                                    color = if (it == currentDestination) PopArtColors.Black else PopArtColors.Black.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            selected = it == currentDestination,
                            onClick = { currentDestination = it }
                        )
                    }
                },
                containerColor = PopArtColors.Yellow
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    when (currentDestination) {
                        AppDestinations.DISCOVER -> DiscoverScreen(
                            isDarkMode = isDarkMode,
                            colors = colors,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                        AppDestinations.MY_MUSIC -> MyMusicScreen(
                            isDarkMode = isDarkMode,
                            colors = colors,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                        AppDestinations.LIVE -> LiveScreenNew(
                            isDarkMode = isDarkMode,
                            colors = colors,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onNavigateToProfile = { userId ->
                                viewingUserId = userId
                                showOtherUserProfile = true
                            }
                        )
                        AppDestinations.PROFILE -> ProfileScreen(
                            isDarkMode = isDarkMode,
                            colors = colors,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }
                    
                    // Overlay de perfil de otro usuario
                    if (showOtherUserProfile && viewingUserId != null) {
                        OtherUserProfileScreen(
                            userId = viewingUserId!!,
                            isDarkMode = isDarkMode,
                            colors = colors,
                            onBack = {
                                showOtherUserProfile = false
                                viewingUserId = null
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    DISCOVER("Descubre", Icons.Default.Search),
    MY_MUSIC("Tu Música", Icons.Default.Favorite),
    LIVE("Live", Icons.Default.PlayArrow),
    PROFILE("Perfil", Icons.Default.AccountCircle),
}

// Modelo de datos para canciones
data class Song(
    val title: String,
    val artist: String,
    val emoji: String
)

// ============================================
// PANTALLAS
// ============================================

// PANTALLA DE BIENVENIDA
@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable { onContinue() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            // Logo grande animado
            AnimatedMusicLogo(
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
            )

            // Nombre de la app
            Text(
                "HYPE",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Subtítulo
            Text(
                "Vibra con el hype de nuevos artistas musicales",
                fontSize = 18.sp,
                color = PopArtColors.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botón de continuar
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    "COMENZAR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Black
                )
            }
        }
    }
}

// PANTALLA DE SELECCIÓN DE NOMBRE DE USUARIO
@Composable
fun UsernameSelectionScreen(onUsernameSelected: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            // Logo
            AnimatedMusicLogo(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            // Título
            Text(
                "TU NOMBRE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Subtítulo
            Text(
                "Elige tu nombre de perfil",
                fontSize = 15.sp,
                color = PopArtColors.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Campo de nombre de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    errorMessage = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de usuario") },
                placeholder = { Text("Ej: DJ_Music_Lover", color = PopArtColors.White.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PopArtColors.White,
                    unfocusedTextColor = PopArtColors.White,
                    focusedBorderColor = PopArtColors.Yellow,
                    unfocusedBorderColor = PopArtColors.White,
                    focusedLabelColor = PopArtColors.Yellow,
                    unfocusedLabelColor = PopArtColors.White,
                    cursorColor = PopArtColors.Yellow
                ),
                shape = RoundedCornerShape(12.dp),
                isError = errorMessage.isNotEmpty()
            )

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Text(
                    errorMessage,
                    fontSize = 14.sp,
                    color = PopArtColors.Pink,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // Botón Continuar
            Button(
                onClick = {
                    when {
                        username.isBlank() -> {
                            errorMessage = "Por favor ingresa un nombre"
                        }
                        username.length < 3 -> {
                            errorMessage = "El nombre debe tener al menos 3 caracteres"
                        }
                        username.length > 20 -> {
                            errorMessage = "El nombre no puede tener más de 20 caracteres"
                        }
                        !username.matches("[a-zA-Z0-9_]+".toRegex()) -> {
                            errorMessage = "Solo letras, números y guión bajo (_)"
                        }
                        else -> {
                            onUsernameSelected(username)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                shape = RoundedCornerShape(30.dp),
                enabled = username.isNotEmpty()
            ) {
                Text(
                    "CONTINUAR",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Black
                )
            }

            Spacer(Modifier.height(16.dp))

            // Texto informativo
            Text(
                "Podrás cambiar tu nombre más tarde en el perfil",
                fontSize = 12.sp,
                color = PopArtColors.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// PANTALLA DE SELECCIÓN DE ROL
@Composable
fun RoleSelectionScreen(onRoleSelected: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(40.dp)
        ) {
            // Logo
            AnimatedMusicLogo(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            // Título
            Text(
                "¿QUIÉN ERES?",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Subtítulo
            Text(
                "Selecciona tu rol para personalizar tu experiencia",
                fontSize = 16.sp,
                color = PopArtColors.White,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Botón Artista
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(bottom = 16.dp)
                    .clickable { onRoleSelected(true) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PopArtColors.Yellow)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎤",
                            fontSize = 48.sp
                        )
                        Text(
                            "SOY ARTISTA",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                        Text(
                            "Comparte tu música",
                            fontSize = 14.sp,
                            color = PopArtColors.Black.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Botón Espectador
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onRoleSelected(false) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PopArtColors.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🎧",
                            fontSize = 48.sp
                        )
                        Text(
                            "SOY ESPECTADOR",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                        Text(
                            "Descubre nueva música",
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

// PANTALLA 1: DESCUBRE (Swipe tipo Tinder)
@Composable
fun DiscoverScreen(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val player = rememberMusicPlayer(context, pauseOnBackground = true) // Pausar en segundo plano
    val scope = rememberCoroutineScope()
    val firebaseManager = remember { FirebaseManager() }
    val songLikesManager = remember { SongLikesManager() }
    val favoritesManager = remember { FavoritesManager(context) }
    val authManager = remember { AuthManager(context) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    val userId = authManager.getUserId() ?: ""
    
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var currentArtistIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var artists by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Estado para ver perfil del artista
    var showArtistProfile by remember { mutableStateOf(false) }
    var selectedArtistId by remember { mutableStateOf<String?>(null) }
    
    // Pausar música cuando la app pasa a segundo plano
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    // App en segundo plano - pausar música
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    // App completamente en background - detener música
                    player.stop()
                    isPlaying = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Cargar canciones desde Firebase y filtrar las ya vistas
    LaunchedEffect(Unit) {
        try {
            android.util.Log.d("DiscoverScreen", "🔍 Iniciando carga de canciones...")
            android.util.Log.d("DiscoverScreen", "👤 UserId: $userId")
            
            // Usar la nueva función que filtra automáticamente canciones vistas
            artists = if (userId.isNotEmpty()) {
                val songs = firebaseManager.getDiscoverSongs(userId, songLikesManager)
                android.util.Log.d("DiscoverScreen", "✅ Canciones cargadas (filtradas): ${songs.size}")
                songs
            } else {
                val songs = firebaseManager.getAllSongs()
                android.util.Log.d("DiscoverScreen", "✅ Canciones cargadas (todas): ${songs.size}")
                songs
            }
            
            android.util.Log.d("DiscoverScreen", "📊 Total de canciones a mostrar: ${artists.size}")
            isLoading = false
        } catch (e: Exception) {
            android.util.Log.e("DiscoverScreen", "❌ Error cargando canciones: ${e.message}", e)
            // Si falla, usar datos de ejemplo
            val exampleArtists = listOf(
                ArtistCard(
                    id = "1",
                    name = "Luna Beats",
                    genre = "Indie Pop",
                    location = "CDMX",
                    emoji = "🎸",
                    imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800",
                    songUrl = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Kangaroo_MusiQue_-_The_Neverwritten_Role_Playing_Game.mp3",
                    bio = "Creando música indie desde el corazón de la ciudad. Influencias de rock alternativo y pop experimental.",
                    photos = listOf(
                        "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=400",
                        "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
                        "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=400"
                    ),
                    socialLinks = mapOf("Instagram" to "@lunabeats", "YouTube" to "Luna Beats Official"),
                    stats = ArtistStats("1.2K", 12, "15K")
                ),
                ArtistCard(
                    id = "2",
                    name = "DJ Neon",
                    genre = "Electronic",
                    location = "Guadalajara",
                    emoji = "🎧",
                    imageUrl = "https://images.unsplash.com/photo-1571330735066-03aaa9429d89?w=800",
                    songUrl = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                    bio = "Beats electrónicos que te hacen vibrar. Fusión de house, techno y sonidos latinos.",
                    photos = listOf(
                        "https://images.unsplash.com/photo-1571330735066-03aaa9429d89?w=400",
                        "https://images.unsplash.com/photo-1598387993441-a364f854c3e1?w=400",
                        "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400"
                    ),
                    socialLinks = mapOf("SoundCloud" to "DJ_Neon", "Spotify" to "DJ Neon"),
                    stats = ArtistStats("3.5K", 28, "50K")
                ),
                ArtistCard(
                    id = "3",
                    name = "Los Rebeldes",
                    genre = "Rock",
                    location = "Monterrey",
                    emoji = "🎤",
                    imageUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=800",
                    songUrl = "https://commondatastorage.googleapis.com/codeskulptor-demos/DDR_assets/Sevish_-__nbsp_.mp3",
                    bio = "Rock en español con actitud. Letras que cuentan historias reales de la calle.",
                    photos = listOf(
                        "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=400",
                        "https://images.unsplash.com/photo-1501612780327-45045538702b?w=400",
                        "https://images.unsplash.com/photo-1524650359799-842906ca1c06?w=400"
                    ),
                    socialLinks = mapOf("Facebook" to "Los Rebeldes Band", "Instagram" to "@losrebeldes"),
                    stats = ArtistStats("2.8K", 18, "35K")
                ),
                ArtistCard(
                    id = "4",
                    name = "Sofía Voz",
                    genre = "R&B",
                    location = "Puebla",
                    emoji = "🎵",
                    imageUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=800",
                    songUrl = "https://commondatastorage.googleapis.com/codeskulptor-assets/week7-brrring.m4a",
                    bio = "Voz suave con alma poderosa. R&B contemporáneo con toques de soul y jazz.",
                    photos = listOf(
                        "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=400",
                        "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=400",
                        "https://images.unsplash.com/photo-1445985543470-41fba5c3144a?w=400"
                    ),
                    socialLinks = mapOf("Instagram" to "@sofiavoz", "TikTok" to "@sofiavoz_music"),
                    stats = ArtistStats("5.1K", 22, "80K")
                ),
            )
            // Filtrar canciones que ya fueron vistas
            artists = exampleArtists.filter { !favoritesManager.isAlreadySeen(it.id) }
            isLoading = false
        }
    }

    // Reproducir música del artista actual desde la mitad
    LaunchedEffect(currentArtistIndex, artists.size) {
        try {
            android.util.Log.d("DiscoverScreen", "LaunchedEffect triggered - Index: $currentArtistIndex, Artists: ${artists.size}")
            
            if (currentArtistIndex < artists.size && artists.isNotEmpty()) {
                val artist = artists[currentArtistIndex]
                android.util.Log.d("DiscoverScreen", "Artista: ${artist.name}")
                android.util.Log.d("DiscoverScreen", "URL: ${artist.songUrl}")
                
                if (artist.songUrl.isNotEmpty()) {
                    // Detener reproducción anterior
                    player.stop()
                    player.clearMediaItems()
                    
                    // Configurar nueva canción
                    val mediaItem = MediaItem.Builder()
                        .setUri(Uri.parse(artist.songUrl))
                        .build()
                    
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    
                    // Esperar a que esté listo y obtener duración
                    kotlinx.coroutines.delay(500)
                    
                    // Buscar a la mitad de la canción
                    val duration = player.duration
                    if (duration > 0) {
                        player.seekTo(duration / 2)
                        android.util.Log.d("DiscoverScreen", "Reproduciendo desde la mitad: ${duration / 2}ms de ${duration}ms")
                    }
                    
                    player.play()
                    isPlaying = true
                    
                    android.util.Log.d("DiscoverScreen", "Reproducción iniciada - Estado: ${player.playbackState}, isPlaying: ${player.isPlaying}")
                } else {
                    android.util.Log.e("DiscoverScreen", "URL vacía para ${artist.name}")
                }
            } else {
                android.util.Log.d("DiscoverScreen", "No hay más artistas")
                player.stop()
                isPlaying = false
            }
        } catch (e: Exception) {
            android.util.Log.e("DiscoverScreen", "ERROR CRÍTICO al reproducir música: ${e.message}", e)
            e.printStackTrace()
            isPlaying = false
        }
    }

    DisposableEffectWithLifecycle(player)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Mostrar perfil del artista si está seleccionado
        if (showArtistProfile && selectedArtistId != null) {
            OtherUserProfileScreen(
                userId = selectedArtistId!!,
                isDarkMode = isDarkMode,
                colors = colors,
                onBack = {
                    showArtistProfile = false
                    selectedArtistId = null
                }
            )
        } else {
            // Contenido normal de DiscoverScreen
            DiscoverScreenContent(
                colors = colors,
                isDarkMode = isDarkMode,
                onMenuClick = onMenuClick,
                isLoading = isLoading,
                artists = artists,
                currentArtistIndex = currentArtistIndex,
                offsetX = offsetX,
                offsetY = offsetY,
                isPlaying = isPlaying,
                userId = userId,
                player = player,
                onDrag = { dragAmount ->
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                },
                onDragEnd = {
                    val artist = artists.getOrNull(currentArtistIndex)
                    if (artist != null) {
                        when {
                            offsetX > 300 -> {
                                // Swipe derecha = like
                                favoritesManager.addFavorite(artist, "heart")
                                if (userId.isNotEmpty()) {
                                    scope.launch {
                                        try {
                                            songLikesManager.likeSong(artist.id, userId)
                                            android.util.Log.d("DiscoverScreen", "✅ Like guardado para ${artist.name}")
                                        } catch (e: Exception) {
                                            android.util.Log.e("DiscoverScreen", "❌ Error al guardar like: ${e.message}")
                                        }
                                    }
                                }
                                player.stop()
                                currentArtistIndex++
                                offsetX = 0f
                                offsetY = 0f
                            }
                            offsetX < -300 -> {
                                // Swipe izquierda = dislike
                                if (userId.isNotEmpty()) {
                                    scope.launch {
                                        try {
                                            firebaseManager.markSongAsRejected(userId, artist.id)
                                        } catch (e: Exception) {
                                            android.util.Log.e("DiscoverScreen", "Error al marcar como rechazada: ${e.message}")
                                        }
                                    }
                                }
                                favoritesManager.addRejected(artist.id)
                                player.stop()
                                currentArtistIndex++
                                offsetX = 0f
                                offsetY = 0f
                            }
                            else -> {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
                },
                onDislike = {
                    if (currentArtistIndex < artists.size) {
                        val artist = artists[currentArtistIndex]
                        if (userId.isNotEmpty()) {
                            scope.launch {
                                try {
                                    firebaseManager.markSongAsRejected(userId, artist.id)
                                } catch (e: Exception) {
                                    android.util.Log.e("DiscoverScreen", "Error al marcar como rechazada: ${e.message}")
                                }
                            }
                        }
                        favoritesManager.addRejected(artist.id)
                        player.stop()
                        currentArtistIndex++
                    }
                },
                onLike = {
                    if (currentArtistIndex < artists.size) {
                        val artist = artists[currentArtistIndex]
                        favoritesManager.addFavorite(artist, "heart")
                        if (userId.isNotEmpty()) {
                            scope.launch {
                                try {
                                    songLikesManager.likeSong(artist.id, userId)
                                    android.util.Log.d("DiscoverScreen", "✅ Like guardado para ${artist.name}")
                                } catch (e: Exception) {
                                    android.util.Log.e("DiscoverScreen", "❌ Error al guardar like: ${e.message}")
                                }
                            }
                        }
                        player.stop()
                        currentArtistIndex++
                    }
                },
                onSuperLike = {
                    if (currentArtistIndex < artists.size) {
                        val artist = artists[currentArtistIndex]
                        favoritesManager.addFavorite(artist, "fire")
                        if (userId.isNotEmpty()) {
                            scope.launch {
                                try {
                                    songLikesManager.likeSong(artist.id, userId)
                                    android.util.Log.d("DiscoverScreen", "🔥 Super like guardado para ${artist.name}")
                                } catch (e: Exception) {
                                    android.util.Log.e("DiscoverScreen", "❌ Error al guardar super like: ${e.message}")
                                }
                            }
                        }
                        player.stop()
                        currentArtistIndex++
                    }
                },
                onProfileClick = {
                    if (currentArtistIndex < artists.size) {
                        val artist = artists[currentArtistIndex]
                        if (artist.userId.isNotEmpty()) {
                            selectedArtistId = artist.userId
                            showArtistProfile = true
                            player.pause()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DiscoverScreenContent(
    colors: AppColors,
    isDarkMode: Boolean,
    onMenuClick: () -> Unit,
    isLoading: Boolean,
    artists: List<ArtistCard>,
    currentArtistIndex: Int,
    offsetX: Float,
    offsetY: Float,
    isPlaying: Boolean,
    userId: String,
    player: ExoPlayer,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDislike: () -> Unit,
    onLike: () -> Unit,
    onSuperLike: () -> Unit,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header con menú hamburguesa
        HypeHeader(
            onMenuClick = onMenuClick,
            isDarkMode = isDarkMode,
            colors = colors,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Indicador de carga
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = colors.primary,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Cargando música...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }
            }
        }
        // Mostrar mensaje si no hay canciones
        else if (artists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎵",
                        fontSize = 80.sp
                    )
                    Text(
                        "No hay canciones aún",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                    Text(
                        "Sé el primero en subir música",
                        fontSize = 16.sp,
                        color = colors.text
                    )
                }
            }
        }
        // Artist Card
        else if (currentArtistIndex < artists.size) {
            val artist = artists[currentArtistIndex]

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, bottom = 100.dp)
            ) {
                ArtistCardWithPages(
                    artist = artist,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    isPlaying = isPlaying,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                    onProfileClick = onProfileClick
                )
            }
        } else {
            // No más artistas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "🎉",
                        fontSize = 80.sp
                    )
                    Text(
                        "¡Vuelve pronto!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                    Text(
                        "Descubriste todo por hoy",
                        fontSize = 16.sp,
                        color = colors.text
                    )
                }
            }
        }

        // Botones de acción - Orden cambiado: Dislike, Fire (centro), Like
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton("🤢", PopArtColors.Pink) {
                onDislike()
            }
            ActionButton("🔥", PopArtColors.Orange) {
                onSuperLike()
            }
            ActionButton("❤️", PopArtColors.Yellow) {
                onLike()
            }
        }
        
        // Botón fijo de comentarios en esquina superior derecha (al final para mayor z-index)
        if (currentArtistIndex < artists.size) {
            val artist = artists[currentArtistIndex]
            FixedCommentsButton(artist = artist)
            
            // Burbuja flotante de comentarios
            FloatingCommentsBubble(
                artist = artist,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 100.dp)
            )
        }
    }
}

// Botón de seguir
@Composable
fun FollowButton(artistId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    val currentUserId = authManager.getUserId() ?: ""
    
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Verificar si ya sigue al artista
    LaunchedEffect(artistId, currentUserId) {
        if (currentUserId.isNotEmpty() && artistId.isNotEmpty() && currentUserId != artistId) {
            isFollowing = firebaseManager.isFollowing(currentUserId, artistId)
            isLoading = false
        } else {
            isLoading = false
        }
    }
    
    // No mostrar el botón si es el mismo usuario o si no hay usuario
    if (currentUserId.isEmpty() || artistId.isEmpty() || currentUserId == artistId || isLoading) {
        return
    }
    
    Button(
        onClick = {
            scope.launch {
                try {
                    isFollowing = firebaseManager.toggleFollow(currentUserId, artistId)
                } catch (e: Exception) {
                    android.util.Log.e("FollowButton", "Error: ${e.message}")
                }
            }
        },
        modifier = modifier
            .height(50.dp)
            .width(150.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) PopArtColors.White else PopArtColors.Yellow
        ),
        shape = RoundedCornerShape(25.dp),
        border = if (isFollowing) androidx.compose.foundation.BorderStroke(2.dp, PopArtColors.Yellow) else null
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isFollowing) Icons.Default.Check else Icons.Default.Add,
                contentDescription = if (isFollowing) "Siguiendo" else "Seguir",
                tint = if (isFollowing) PopArtColors.Yellow else PopArtColors.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (isFollowing) "Siguiendo" else "Seguir",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isFollowing) PopArtColors.Yellow else PopArtColors.Black
            )
        }
    }
}

// Botón de seguir compacto (círculo con +)
@Composable
fun CompactFollowButton(artistId: String) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    val currentUserId = authManager.getUserId() ?: ""
    
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Verificar si ya sigue al artista
    LaunchedEffect(artistId, currentUserId) {
        if (currentUserId.isNotEmpty() && artistId.isNotEmpty() && currentUserId != artistId) {
            isFollowing = firebaseManager.isFollowing(currentUserId, artistId)
            isLoading = false
        } else {
            isLoading = false
        }
    }
    
    // No mostrar el botón si es el mismo usuario o si no hay usuario
    if (currentUserId.isEmpty() || artistId.isEmpty() || currentUserId == artistId || isLoading) {
        return
    }
    
    IconButton(
        onClick = {
            scope.launch {
                try {
                    isFollowing = firebaseManager.toggleFollow(currentUserId, artistId)
                } catch (e: Exception) {
                    android.util.Log.e("CompactFollowButton", "Error: ${e.message}")
                }
            }
        },
        modifier = Modifier
            .size(36.dp)
            .background(
                if (isFollowing) PopArtColors.White else PopArtColors.Yellow,
                CircleShape
            )
            .border(
                2.dp,
                if (isFollowing) PopArtColors.Yellow else PopArtColors.Black,
                CircleShape
            )
    ) {
        Icon(
            if (isFollowing) Icons.Default.Check else Icons.Default.Add,
            contentDescription = if (isFollowing) "Siguiendo" else "Seguir",
            tint = if (isFollowing) PopArtColors.Yellow else PopArtColors.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ActionButton(emoji: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(70.dp)
            .border(4.dp, PopArtColors.Black, CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(emoji, fontSize = 32.sp)
    }
}

// Botón fijo de comentarios en esquina superior derecha
@Composable
fun FixedCommentsButton(artist: ArtistCard) {
    var showCommentsDialog by remember { mutableStateOf(false) }
    
    // Botón fijo en esquina superior derecha
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, end = 16.dp)
    ) {
        // Botón con icono de flecha
        IconButton(
            onClick = { showCommentsDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(48.dp)
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = "Comentarios",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
    
    // Diálogo de comentarios estilo TikTok
    if (showCommentsDialog) {
        CommentsDialog(
            artist = artist,
            onDismiss = { showCommentsDialog = false }
        )
    }
}



// Diálogo de comentarios estilo TikTok con Firebase
@Composable
fun CommentsDialog(artist: ArtistCard, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    var commentText by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyingToUsername by remember { mutableStateOf<String?>(null) }
    var comments by remember { mutableStateOf<List<VideoComment>>(emptyList()) }
    var commentsWithReplies by remember { mutableStateOf<Map<String, List<VideoComment>>>(emptyMap()) }
    var userLikes by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val userId = authManager.getUserId() ?: ""
    var username by remember { mutableStateOf("Usuario") }
    
    // Cargar username del usuario
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val profile = firebaseManager.getUserProfile(userId)
            username = profile?.username ?: "Usuario"
        }
    }
    
    // Escuchar comentarios en tiempo real
    LaunchedEffect(artist.id) {
        firebaseManager.getCommentsRealtime(artist.id) { updatedComments ->
            comments = updatedComments
            isLoading = false
            
            // Cargar respuestas para cada comentario
            scope.launch {
                val repliesMap = mutableMapOf<String, List<VideoComment>>()
                updatedComments.forEach { comment ->
                    val replies = firebaseManager.getReplies(artist.id, comment.id)
                    if (replies.isNotEmpty()) {
                        repliesMap[comment.id] = replies
                    }
                }
                commentsWithReplies = repliesMap
                
                // Cargar likes del usuario
                val likes = mutableSetOf<String>()
                updatedComments.forEach { comment ->
                    if (firebaseManager.hasUserLikedComment(artist.id, comment.id, userId)) {
                        likes.add(comment.id)
                    }
                    // Verificar likes en respuestas
                    repliesMap[comment.id]?.forEach { reply ->
                        if (firebaseManager.hasUserLikedComment(artist.id, reply.id, userId, true, comment.id)) {
                            likes.add(reply.id)
                        }
                    }
                }
                userLikes = likes
            }
        }
    }
    
    // Animación de entrada desde abajo
    val slideOffset by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "slide"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures { onDismiss() }
            }
    ) {
        // Panel de comentarios superpuesto a la barra de navegación
        Card(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { /* Evitar que el clic cierre el diálogo */ }
                },
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = PopArtColors.White.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PopArtColors.Black)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Comentarios",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.White
                        )
                        Text(
                            "${comments.size} comentarios",
                            fontSize = 12.sp,
                            color = PopArtColors.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Botón para comentario general
                    IconButton(
                        onClick = { 
                            replyingToCommentId = null
                            replyingToUsername = null
                            showTextInput = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            contentDescription = "Comentar",
                            tint = PopArtColors.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = PopArtColors.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Lista de comentarios
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PopArtColors.Yellow)
                    }
                } else if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Sé el primero en comentar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PopArtColors.Black.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        items(comments.size) { index ->
                            val comment = comments[index]
                            val isLiked = userLikes.contains(comment.id)
                            
                            CommentItem(
                                comment = comment.copy(isLiked = isLiked),
                                currentUserId = userId,
                                onTextReply = { 
                                    replyingToCommentId = comment.id
                                    replyingToUsername = comment.username
                                    showTextInput = true
                                },
                                onLike = {
                                    scope.launch {
                                        try {
                                            firebaseManager.toggleCommentLike(artist.id, comment.id, userId)
                                            // Actualizar estado local
                                            userLikes = if (isLiked) {
                                                userLikes - comment.id
                                            } else {
                                                userLikes + comment.id
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("CommentsDialog", "Error al dar like: ${e.message}")
                                        }
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        try {
                                            // TODO: Implementar eliminación de comentario en FirebaseManager
                                            android.util.Log.d("CommentsDialog", "Eliminar comentario: ${comment.id}")
                                        } catch (e: Exception) {
                                            android.util.Log.e("CommentsDialog", "Error al eliminar: ${e.message}")
                                        }
                                    }
                                }
                            )
                            
                            // Mostrar respuestas
                            commentsWithReplies[comment.id]?.forEachIndexed { replyIndex, reply ->
                                val isReplyLiked = userLikes.contains(reply.id)
                                
                                CommentItem(
                                    comment = reply.copy(isLiked = isReplyLiked),
                                    isReply = true,
                                    currentUserId = userId,
                                    onTextReply = { },
                                    onLike = {
                                        scope.launch {
                                            try {
                                                firebaseManager.toggleCommentLike(artist.id, reply.id, userId, true, comment.id)
                                                // Actualizar estado local
                                                userLikes = if (isReplyLiked) {
                                                    userLikes - reply.id
                                                } else {
                                                    userLikes + reply.id
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("CommentsDialog", "Error al dar like: ${e.message}")
                                            }
                                        }
                                    },
                                    onDelete = {
                                        scope.launch {
                                            try {
                                                // TODO: Implementar eliminación de respuesta en FirebaseManager
                                                android.util.Log.d("CommentsDialog", "Eliminar respuesta: ${reply.id}")
                                            } catch (e: Exception) {
                                                android.util.Log.e("CommentsDialog", "Error al eliminar respuesta: ${e.message}")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Área de input (solo se muestra cuando se hace clic en el icono)
                if (showTextInput) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PopArtColors.White.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (showTextInput) {
                            // Indicador de respuesta
                            if (replyingToCommentId != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Respondiendo a ${replyingToUsername ?: "comentario"}",
                                        fontSize = 12.sp,
                                        color = PopArtColors.Yellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { 
                                            replyingToCommentId = null
                                            replyingToUsername = null
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Cancelar respuesta",
                                            tint = PopArtColors.Black.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            // Campo de texto con botón enviar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    placeholder = { 
                                        Text(
                                            if (replyingToCommentId != null) "Responder a ${replyingToUsername}..." else "Añade un comentario...",
                                            fontSize = 14.sp,
                                            color = PopArtColors.Black.copy(alpha = 0.4f)
                                        ) 
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PopArtColors.Yellow,
                                        unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.2f),
                                        cursorColor = PopArtColors.Yellow,
                                        focusedContainerColor = PopArtColors.Yellow.copy(alpha = 0.05f),
                                        unfocusedContainerColor = PopArtColors.Black.copy(alpha = 0.02f),
                                        focusedTextColor = PopArtColors.Black,
                                        unfocusedTextColor = PopArtColors.Black
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    maxLines = 4
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotEmpty() && userId.isNotEmpty()) {
                                            scope.launch {
                                                try {
                                                    if (replyingToCommentId != null) {
                                                        // Es una respuesta a un comentario
                                                        firebaseManager.addReply(
                                                            songId = artist.id,
                                                            commentId = replyingToCommentId!!,
                                                            userId = userId,
                                                            username = username,
                                                            text = commentText,
                                                            isVoiceNote = false
                                                        )
                                                        replyingToCommentId = null
                                                        replyingToUsername = null
                                                    } else {
                                                        // Es un comentario general
                                                        firebaseManager.addComment(
                                                            songId = artist.id,
                                                            userId = userId,
                                                            username = username,
                                                            text = commentText,
                                                            isVoiceNote = false
                                                        )
                                                    }
                                                    commentText = ""
                                                    showTextInput = false
                                                } catch (e: Exception) {
                                                    android.util.Log.e("CommentsDialog", "Error al enviar comentario: ${e.message}")
                                                }
                                            }
                                        }
                                    },
                                    enabled = commentText.isNotEmpty(),
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (commentText.isNotEmpty()) PopArtColors.Yellow else PopArtColors.Black.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_send),
                                        contentDescription = "Enviar",
                                        tint = if (commentText.isNotEmpty()) PopArtColors.Black else PopArtColors.Black.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                IconButton(
                                    onClick = { 
                                        showTextInput = false
                                        replyingToCommentId = null
                                        replyingToUsername = null
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = PopArtColors.Black.copy(alpha = 0.6f),
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
}

// Item de comentario individual
@Composable
fun CommentItem(
    comment: VideoComment,
    isReply: Boolean = false,
    currentUserId: String = "",
    onTextReply: () -> Unit,
    onLike: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 40.dp else 0.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(if (isReply) 28.dp else 36.dp)
                .background(PopArtColors.Cyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.username.first().toString(),
                fontSize = if (isReply) 12.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        }
        
        Spacer(Modifier.width(8.dp))
        
        // Contenido del comentario
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    comment.username,
                    fontSize = if (isReply) 12.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Black
                )
                Text(
                    "•",
                    fontSize = if (isReply) 10.sp else 12.sp,
                    color = PopArtColors.Black.copy(alpha = 0.4f)
                )
                Text(
                    comment.getFormattedTime(),
                    fontSize = if (isReply) 10.sp else 12.sp,
                    color = PopArtColors.Black.copy(alpha = 0.6f)
                )
            }
            
            Text(
                comment.text,
                fontSize = if (isReply) 12.sp else 14.sp,
                color = PopArtColors.Black,
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )
            
            // Acciones (iconos más pequeños)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like - Corazón que cambia de color
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Me gusta",
                        tint = if (comment.isLiked) PopArtColors.Pink else PopArtColors.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        comment.likes.toString(),
                        fontSize = 12.sp,
                        color = PopArtColors.Black.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Responder con texto
                if (!isReply) {
                    IconButton(
                        onClick = onTextReply,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            contentDescription = "Responder",
                            tint = PopArtColors.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Botón eliminar (solo para comentarios propios)
                if (comment.id.isNotEmpty() && currentUserId.isNotEmpty() && 
                    (comment.username == currentUserId || comment.id.contains(currentUserId))) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = PopArtColors.Pink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
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
                fontSize = 32.sp,
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

        // Pestañas: Favoritos / Siguiendo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pestaña Favoritos
            Button(
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 0) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "❤️",
                        fontSize = 20.sp
                    )
                    Text(
                        "Favoritos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) PopArtColors.Black else PopArtColors.White
                    )
                }
            }
            
            // Pestaña Siguiendo
            Button(
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == 1) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "👥",
                        fontSize = 20.sp
                    )
                    Text(
                        "Siguiendo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) PopArtColors.Black else PopArtColors.White
                    )
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
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar por artista, género o ubicación...", color = colors.text.copy(alpha = 0.6f)) },
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
                shape = RoundedCornerShape(30.dp),
                singleLine = true
            )
        } else {
            Text(
                if (selectedTab == 0) "Canciones que te gustaron 🎵" else "Canciones de artistas que sigues 👥",
                fontSize = 18.sp,
                color = colors.text,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Sección de Estados de artistas que sigues
        var stories by remember { mutableStateOf<List<ArtistStory>>(emptyList()) }
        var showStoryViewer by remember { mutableStateOf(false) }
        var selectedStoryIndex by remember { mutableStateOf(0) }
        
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
        
        // Mostrar estados si hay
        if (stories.isNotEmpty() && !isSearching) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
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
                        fontSize = 80.sp
                    )
                    Text(
                        if (selectedTab == 0) "No tienes favoritos aún" else "No sigues a nadie aún",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.primary
                    )
                    Text(
                        if (selectedTab == 0) 
                            "Dale ❤️ a las canciones que te gusten en Descubre"
                        else
                            "Sigue a artistas para ver sus canciones aquí",
                        fontSize = 14.sp,
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
                    Text("🔍", fontSize = 80.sp)
                    Text(
                        "No se encontraron resultados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Yellow
                    )
                    Text(
                        "Intenta con otra búsqueda",
                        fontSize = 14.sp,
                        color = PopArtColors.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSongs.size) { index ->
                    val song = filteredSongs[index]
                    val actualIndex = likedSongs.indexOf(song)
                    val isCurrentlyPlaying = currentPlayingIndex == actualIndex && isPlaying
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
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
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentlyPlaying) PopArtColors.Yellow else PopArtColors.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Imagen o emoji
                            if (song.imageUrl.isNotEmpty()) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(context)
                                        .data(song.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = song.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .then(
                                            if (isCurrentlyPlaying) {
                                                Modifier.background(PopArtColors.Black)
                                            } else {
                                                Modifier.background(PopArtColors.MulticolorGradient)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("❤️", fontSize = 28.sp)
                                }
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
                                    color = PopArtColors.Black
                                )
                                Text(
                                    "${song.genre} • ${song.location}",
                                    fontSize = 14.sp,
                                    color = PopArtColors.Black.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isCurrentlyPlaying) {
                                Text("⏸", fontSize = 32.sp, color = PopArtColors.Black)
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
                }
            }

            // Barra de reproducción
            if (currentPlayingIndex != null && currentPlayingIndex!! < likedSongs.size) {
                Spacer(Modifier.height(16.dp))
                MusicPlayerBar(
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
                    songName = likedSongs[currentPlayingIndex!!].name
                )
            }
        }
    }
}

@Composable
fun MusicPlayerBar(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    songName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PopArtColors.Yellow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre de la canción
            Text(
                "Reproduciendo: $songName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Barra de progreso
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatTime(currentPosition),
                    fontSize = 12.sp,
                    color = PopArtColors.Black,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = if (duration > 0) currentPosition.toFloat() else 0f,
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = PopArtColors.Black,
                        activeTrackColor = PopArtColors.Black,
                        inactiveTrackColor = PopArtColors.Black.copy(alpha = 0.3f)
                    )
                )

                Text(
                    formatTime(duration),
                    fontSize = 12.sp,
                    color = PopArtColors.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón de play/pause
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onPlayPause,
                    modifier = Modifier.size(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Black),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    if (isPlaying) {
                        Text(
                            "⏸",
                            fontSize = 32.sp,
                            color = PopArtColors.Yellow
                        )
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
        }
    }
}

fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / 1000) / 60
    return String.format("%d:%02d", minutes, seconds)
}

// PANTALLA 3: LIVE (Conciertos y Concursos)
@Composable
fun LiveScreen() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showLiveViewer by remember { mutableStateOf(false) }
    var showLiveRecording by remember { mutableStateOf(false) }
    var showVideoPreview by remember { mutableStateOf(false) }
    var showContestDetail by remember { mutableStateOf(false) }
    var selectedConcert by remember { mutableStateOf<Concert?>(null) }
    var selectedContest by remember { mutableStateOf<Contest?>(null) }
    var recordedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingVideo by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    val tabs = listOf("🎸 Conciertos", "🏆 Concursos")
    
    when {
        showLiveViewer && selectedConcert != null -> {
            LiveViewerScreen(
                concert = selectedConcert!!,
                onBack = { showLiveViewer = false }
            )
        }
        showVideoPreview && recordedVideoUri != null -> {
            Box(modifier = Modifier.fillMaxSize()) {
                VideoPreviewScreen(
                    videoUri = recordedVideoUri!!,
                    onBack = {
                        showVideoPreview = false
                        recordedVideoUri = null
                    },
                    onUpload = { uri ->
                        isUploadingVideo = true
                        uploadProgress = 0
                        scope.launch {
                            try {
                                val userId = authManager.getUserId() ?: ""
                                val username = firebaseManager.getUserProfile(userId)?.username ?: "Usuario"
                                
                                android.util.Log.d("LiveScreen", "📤 Iniciando subida de video...")
                                
                                // Subir video a Firebase
                                val videoUrl = firebaseManager.uploadContestVideo(
                                    uri = uri,
                                    userId = userId,
                                    onProgress = { progress ->
                                        uploadProgress = progress
                                        android.util.Log.d("LiveScreen", "📊 Progreso: $progress%")
                                    }
                                )
                                
                                android.util.Log.d("LiveScreen", "✅ Video subido: $videoUrl")
                                
                                // Crear entrada del concurso
                                val entryId = firebaseManager.createContestEntry(
                                    userId = userId,
                                    username = username,
                                    videoUrl = videoUrl,
                                    title = selectedContest?.name ?: "Mi participación",
                                    contestId = selectedContest?.name ?: "general"
                                )
                                
                                android.util.Log.d("LiveScreen", "✅ Entrada creada: $entryId")
                                
                                isUploadingVideo = false
                                showVideoPreview = false
                                recordedVideoUri = null
                                
                                // Mostrar mensaje de éxito
                                android.widget.Toast.makeText(
                                    context,
                                    "¡Video subido exitosamente! 🎉\nVe a la galería del concurso para verlo",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                
                            } catch (e: Exception) {
                                android.util.Log.e("LiveScreen", "❌ Error subiendo video: ${e.message}", e)
                                isUploadingVideo = false
                                android.widget.Toast.makeText(
                                    context,
                                    "Error al subir video: ${e.message}",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onRetake = {
                        showVideoPreview = false
                        showLiveRecording = true
                    }
                )
                
                // Overlay de progreso de subida
                if (isUploadingVideo) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PopArtColors.Black.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Animación de subida
                            Text(
                                "📤",
                                fontSize = 80.sp,
                                modifier = Modifier.offset(y = (-10).dp)
                            )
                            
                            Text(
                                "Subiendo tu video...",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.Yellow
                            )
                            
                            // Barra de progreso
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .padding(horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    progress = uploadProgress / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = PopArtColors.Yellow,
                                    trackColor = PopArtColors.White.copy(alpha = 0.3f)
                                )
                                
                                Spacer(Modifier.height(12.dp))
                                
                                Text(
                                    "$uploadProgress%",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopArtColors.White
                                )
                            }
                            
                            Text(
                                "No cierres la app",
                                fontSize = 14.sp,
                                color = PopArtColors.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
        showLiveRecording -> {
            LiveRecordingScreen(
                onBack = { 
                    showLiveRecording = false
                    recordedVideoUri = null
                },
                onVideoRecorded = { uri ->
                    android.util.Log.d("LiveScreen", "📹 Video grabado: $uri")
                    recordedVideoUri = uri
                    showLiveRecording = false
                    showVideoPreview = true
                }
            )
        }
        showContestDetail && selectedContest != null -> {
            var showGallery by remember { mutableStateOf(false) }
            
            if (showGallery) {
                ContestGalleryScreen(
                    contest = selectedContest!!,
                    onBack = { showGallery = false }
                )
            } else {
                ContestDetailScreen(
                    contest = selectedContest!!,
                    onBack = { showContestDetail = false },
                    onRecordVideo = {
                        showContestDetail = false
                        showLiveRecording = true
                    },
                    onViewGallery = { showGallery = true }
                )
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PopArtColors.Black)
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    "HYPES/LIVES",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Yellow,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Tabs (más anchos para mostrar texto completo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Button(
                            onClick = { selectedTab = index },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == index) PopArtColors.Yellow else PopArtColors.White.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(
                                tab,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selectedTab == index) PopArtColors.Black else PopArtColors.White,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Contenido según tab seleccionado
                when (selectedTab) {
                    0 -> ConcertsContent(
                        onConcertClick = { concert ->
                            selectedConcert = concert
                            showLiveViewer = true
                        },
                        onStartLive = {
                            showLiveRecording = true
                        }
                    )
                    1 -> ContestsContent(
                        onContestClick = { contest ->
                            selectedContest = contest
                            showContestDetail = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConcertsContent(
    onConcertClick: (Concert) -> Unit = {},
    onStartLive: () -> Unit = {}
) {
    var showLiveDialog by remember { mutableStateOf(false) }
    
    val concerts = remember {
        listOf(
            Concert("Luna Beats en Vivo", "CDMX - Foro Sol", "15 Dic 2024", "🎸", "20:00 hrs"),
            Concert("DJ Neon Festival", "Guadalajara - Arena VFG", "22 Dic 2024", "🎧", "21:00 hrs"),
            Concert("Los Rebeldes Tour", "Monterrey - Auditorio Banamex", "28 Dic 2024", "🎤", "19:30 hrs"),
            Concert("Sofía Voz Acústico", "Puebla - Teatro Principal", "5 Ene 2025", "🎵", "20:30 hrs"),
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(concerts.size) { index ->
            val concert = concerts[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PopArtColors.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(PopArtColors.MulticolorGradient, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(concert.emoji, fontSize = 32.sp)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            concert.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                        Text(
                            concert.location,
                            fontSize = 14.sp,
                            color = PopArtColors.Black.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                concert.date,
                                fontSize = 12.sp,
                                color = PopArtColors.Pink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                concert.time,
                                fontSize = 12.sp,
                                color = PopArtColors.Cyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { onConcertClick(concert) }
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Ver Live",
                            tint = PopArtColors.Yellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        
        // Botón para iniciar un Live
        item {
            Button(
                onClick = { onStartLive() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Pink),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Iniciar Live",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "INICIAR UN LIVE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ContestsContent(
    onContestClick: (Contest) -> Unit = {}
) {
    var showCreateContestDialog by remember { mutableStateOf(false) }
    
    val contests = remember {
        listOf(
            Contest("Batalla de Bandas 2024", "🎁 1 mes de Spotify Premium gratis", "Termina en 15 días", "🏆", PopArtColors.Yellow),
            Contest("Mejor Cover del Mes", "🎬 1 mes de Disney+ gratis", "Termina en 8 días", "🎤", PopArtColors.Pink),
            Contest("DJ Challenge", "🎧 1 mes de YouTube Premium gratis", "Termina en 22 días", "🎧", PopArtColors.Cyan),
            Contest("Compositor Revelación", "📺 1 mes de Netflix gratis", "Termina en 30 días", "✍️", PopArtColors.Purple),
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contests.size) { index ->
            val contest = contests[index]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = contest.color)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            contest.emoji,
                            fontSize = 40.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                contest.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.Black
                            )
                            Text(
                                contest.deadline,
                                fontSize = 12.sp,
                                color = PopArtColors.Black.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        contest.prize,
                        fontSize = 14.sp,
                        color = PopArtColors.Black.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Button(
                        onClick = { onContestClick(contest) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "PARTICIPAR",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.White
                        )
                    }
                }
            }
        }
        
        // Botón para crear concurso
        item {
            Button(
                onClick = { showCreateContestDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Purple),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Crear Concurso",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "CREAR CONCURSO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
    
    // Diálogo para crear concurso
    if (showCreateContestDialog) {
        AlertDialog(
            onDismissRequest = { showCreateContestDialog = false },
            title = {
                Text(
                    "Crear Concurso",
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Black
                )
            },
            text = {
                Column {
                    Text(
                        "Crea un concurso para tu comunidad",
                        fontSize = 14.sp,
                        color = PopArtColors.Black
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "🏆 Define el premio",
                        fontSize = 12.sp,
                        color = PopArtColors.Black.copy(alpha = 0.7f)
                    )
                    Text(
                        "📅 Establece la duración",
                        fontSize = 12.sp,
                        color = PopArtColors.Black.copy(alpha = 0.7f)
                    )
                    Text(
                        "🎯 Elige las reglas",
                        fontSize = 12.sp,
                        color = PopArtColors.Black.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Aquí iría la lógica para crear el concurso
                        showCreateContestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Purple)
                ) {
                    Text("CREAR", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateContestDialog = false }) {
                    Text("Cancelar", color = PopArtColors.Black)
                }
            },
            containerColor = PopArtColors.White
        )
    }
}

// PANTALLA 4: PERFIL - Ver ProfileScreen.kt
// La implementación completa está en ProfileScreen.kt

// Funciones auxiliares de Discover - Ver DiscoverHelpers.kt

// Burbuja flotante de comentarios relevantes - Ahora con comentarios reales de Firebase
@Composable
fun FloatingCommentsBubble(
    artist: ArtistCard,
    modifier: Modifier = Modifier
) {
    val firebaseManager = remember { FirebaseManager() }
    var topComments by remember { mutableStateOf<List<VideoComment>>(emptyList()) }
    var showBubble by remember { mutableStateOf(false) }
    var currentCommentIndex by remember { mutableStateOf(0) }
    
    // Cargar comentarios reales desde Firebase
    LaunchedEffect(artist.id) {
        firebaseManager.getCommentsRealtime(artist.id) { comments ->
            // Filtrar comentarios con al menos 1 like y ordenar por likes
            topComments = comments
                .filter { it.likes > 0 }
                .sortedByDescending { it.likes }
                .take(10) // Tomar los 10 mejores comentarios
            
            android.util.Log.d("FloatingCommentsBubble", "Comentarios cargados: ${topComments.size}")
        }
    }
    
    // Mostrar burbujas de comentarios cada 8 segundos
    LaunchedEffect(topComments.size) {
        if (topComments.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(8000) // Esperar 8 segundos
                showBubble = true
                currentCommentIndex = (currentCommentIndex + 1) % topComments.size
                kotlinx.coroutines.delay(4000) // Mostrar por 4 segundos
                showBubble = false
            }
        }
    }
    
    if (showBubble && topComments.isNotEmpty()) {
        val comment = topComments[currentCommentIndex]
        
        Card(
            modifier = modifier
                .padding(16.dp)
                .clickable { showBubble = false },
            colors = CardDefaults.cardColors(
                containerColor = PopArtColors.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PopArtColors.Cyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        comment.username.firstOrNull()?.toString() ?: "?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Black
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        comment.username,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Black
                    )
                    Text(
                        comment.text,
                        fontSize = 11.sp,
                        color = PopArtColors.Black.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = PopArtColors.Pink,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        comment.likes.toString(),
                        fontSize = 10.sp,
                        color = PopArtColors.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Dropdown menu para el título HYPE
@Composable
fun HypeDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = true }
        ) {
            Text(
                "HYPE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Menú",
                tint = PopArtColors.Yellow,
                modifier = Modifier
                    .size(24.dp)
                    .padding(start = 4.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(PopArtColors.White)
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.


                            Email,
                            contentDescription = "Comentarios",
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Deja tus comentarios sobre la app",
                            color = PopArtColors.Black,
                            fontSize = 14.sp
                        )
                    }
                },
                onClick = {
                    expanded = false
                    // TODO: Abrir pantalla de comentarios/feedback
                }
            )
            
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Configuración",
                            color = PopArtColors.Black,
                            fontSize = 14.sp
                        )
                    }
                },
                onClick = {
                    expanded = false
                    // TODO: Abrir pantalla de configuración
                }
            )
            
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Acerca de",
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Acerca de HYPE",
                            color = PopArtColors.Black,
                            fontSize = 14.sp
                        )
                    }
                },
                onClick = {
                    expanded = false
                    // TODO: Mostrar información de la app
                }
            )
        }
    }
}


// ============ COMPONENTES DE ESTADOS/STORIES ============

@Composable
fun StoryCircle(
    story: ArtistStory,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        ) {
            // Círculo con borde (amarillo si no visto, gris si ya visto)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 3.dp,
                        color = if (story.isViewed) PopArtColors.White.copy(alpha = 0.3f) else PopArtColors.Yellow,
                        shape = CircleShape
                    )
                    .padding(3.dp)
            ) {
                // Imagen del artista
                if (story.artistImageUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(story.artistImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = story.artistName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PopArtColors.Cyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            story.artistName.first().toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.White
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(4.dp))
        
        Text(
            story.artistName,
            fontSize = 12.sp,
            color = PopArtColors.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StoryViewerScreen(
    stories: List<ArtistStory>,
    startIndex: Int,
    userId: String,
    onDismiss: () -> Unit,
    onStoryViewed: (String) -> Unit
) {
    var currentIndex by remember { mutableStateOf(startIndex) }
    val currentStory = stories.getOrNull(currentIndex)
    
    // Marcar como visto cuando se muestra
    LaunchedEffect(currentStory?.id) {
        currentStory?.let {
            if (!it.isViewed) {
                onStoryViewed(it.id)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (currentStory != null) {
            // Contenido del estado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                // Tap izquierda = anterior, derecha = siguiente
                                if (offset.x < size.width / 2) {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                    } else {
                                        onDismiss()
                                    }
                                } else {
                                    if (currentIndex < stories.size - 1) {
                                        currentIndex++
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Imagen o video del estado
                if (currentStory.mediaType == "image") {
                    coil.compose.AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(currentStory.mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Estado",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                } else {
                    // TODO: Implementar reproductor de video
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Video Story",
                            color = PopArtColors.White,
                            fontSize = 20.sp
                        )
                    }
                }
                
                // Overlay superior con info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    PopArtColors.Black.copy(alpha = 0.7f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    // Barras de progreso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        stories.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(
                                        when {
                                            index < currentIndex -> PopArtColors.White
                                            index == currentIndex -> PopArtColors.Yellow
                                            else -> PopArtColors.White.copy(alpha = 0.3f)
                                        },
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Info del artista
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            if (currentStory.artistImageUrl.isNotEmpty()) {
                                coil.compose.AsyncImage(
                                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                        .data(currentStory.artistImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = currentStory.artistName,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(PopArtColors.Cyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        currentStory.artistName.first().toString(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopArtColors.White
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    currentStory.artistName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PopArtColors.White
                                )
                                Text(
                                    currentStory.getTimeRemaining(),
                                    fontSize = 12.sp,
                                    color = PopArtColors.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Botón cerrar
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = PopArtColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                // Caption en la parte inferior
                if (currentStory.caption.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PopArtColors.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            currentStory.caption,
                            fontSize = 16.sp,
                            color = PopArtColors.White,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}


// ============ PANTALLA DE PERFIL DE OTRO USUARIO ============

@Composable
fun OtherUserProfileScreen(
    userId: String,
    isDarkMode: Boolean,
    colors: AppColors,
    onBack: () -> Unit
) {
    val firebaseManager = remember { FirebaseManager() }
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var swipeOffset by remember { mutableStateOf(0f) }
    
    // Cargar perfil del usuario
    LaunchedEffect(userId) {
        isLoading = true
        try {
            android.util.Log.d("OtherUserProfile", "📖 Cargando perfil del usuario: $userId")
            userProfile = firebaseManager.getUserProfile(userId)
            android.util.Log.d("OtherUserProfile", "✅ Perfil cargado: ${userProfile?.username}")
        } catch (e: Exception) {
            android.util.Log.e("OtherUserProfile", "❌ Error cargando perfil: ${e.message}")
        } finally {
            isLoading = false
        }
    }
    
    // Pantalla completa con fondo y gesto de swipe
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .offset { IntOffset(swipeOffset.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Solo permitir swipe hacia la derecha (volver)
                        if (dragAmount.x > 0) {
                            swipeOffset = (swipeOffset + dragAmount.x).coerceAtMost(size.width.toFloat())
                        }
                    },
                    onDragEnd = {
                        // Si el swipe es mayor al 30% del ancho, volver
                        if (swipeOffset > size.width * 0.3f) {
                            onBack()
                        } else {
                            // Volver a la posición original
                            swipeOffset = 0f
                        }
                    }
                )
            }
    ) {
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (userProfile != null) {
            // Perfil cargado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header con botón de regreso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = colors.text
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Perfil de Usuario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Foto de perfil
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    if (userProfile!!.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userProfile!!.profileImageUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = CircleShape,
                            color = PopArtColors.Pink
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    userProfile!!.username.firstOrNull()?.uppercase() ?: "U",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Nombre de usuario
                Text(
                    userProfile!!.username,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.text,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Tipo de usuario
                Surface(
                    color = if (userProfile!!.isArtist) PopArtColors.Pink else PopArtColors.Cyan,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (userProfile!!.isArtist) "🎤 Artista" else "👤 Usuario",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Botón de seguir (placeholder)
                Button(
                    onClick = { /* TODO: Implementar seguir */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PopArtColors.Pink
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Seguir",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Información adicional
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Información",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Este es el perfil de ${userProfile!!.username}",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        } else {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "❌",
                        fontSize = 64.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No se pudo cargar el perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}
