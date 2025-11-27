# 🎵 INSTRUCCIONES PARA IMPLEMENTAR "TU MÚSICA"

## ⚠️ IMPORTANTE
Los cambios automáticos causaron conflictos. Sigue estas instrucciones manualmente en Android Studio.

## 📋 Pasos a Seguir

### 1. Verificar que MyMusicScreen no existe
En `MainActivity.kt`, busca si existe `fun MyMusicScreen`. Si no existe, continúa.

### 2. La pantalla ya está siendo llamada
En la línea ~227 del `MainActivity.kt` ya existe:
```kotlin
AppDestinations.MY_MUSIC -> MyMusicScreen(
    isDarkMode = isDarkMode,
    colors = colors,
    onMenuClick = { scope.launch { drawerState.open() } }
)
```

### 3. Crear la pantalla básica
Agrega esta función ANTES de la línea donde dice `// PANTALLA 3: LIVE`:

```kotlin
// PANTALLA 2: TU MÚSICA
@Composable
fun MyMusicScreen(
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false),
    onMenuClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val songLikesManager = remember { SongLikesManager() }
    
    val userId = authManager.getUserId() ?: ""
    var likedSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Cargar canciones con like
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error: ${e.message}")
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        HypeHeader(
            onMenuClick = onMenuClick,
            isDarkMode = isDarkMode,
            colors = colors
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Tu Música",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow
            )
            
            Spacer(Modifier.height(16.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PopArtColors.Yellow)
                }
            } else if (likedSongs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💔", fontSize = 64.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No tienes favoritos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Yellow
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Dale ❤️ a canciones en Descubre",
                            fontSize = 13.sp,
                            color = PopArtColors.White.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(likedSongs.size) { index ->
                        val song = likedSongs[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PopArtColors.White
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Portada
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PopArtColors.Yellow),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎵", fontSize = 24.sp)
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                
                                // Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        song.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PopArtColors.Black
                                    )
                                    Text(
                                        "${song.genre} • ${song.location}",
                                        fontSize = 12.sp,
                                        color = PopArtColors.Black.copy(alpha = 0.6f)
                                    )
                                }
                                
                                // Botón play
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
    }
}
```

## ✅ Verificación

1. Compila el proyecto (Build > Make Project)
2. Si hay errores, revisa que:
   - Todos los imports estén correctos
   - Las llaves estén balanceadas
   - La función esté en el lugar correcto

## 🎨 Mejoras Opcionales (Para después)

Una vez que funcione la versión básica, puedes agregar:
- Ecualizador animado
- Historias de artistas
- Reproductor de música
- Búsqueda de canciones

## 📝 Notas

- Esta es una versión MÍNIMA y funcional
- No tiene ecualizador ni historias (se agregarán después)
- Solo muestra las canciones con like
- Es más fácil de depurar y mantener

---

**Fecha**: 26/11/2025
**Estado**: Pendiente de implementación manual
