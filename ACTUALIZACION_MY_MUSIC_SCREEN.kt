// PANTALLA 2: TU MÚSICA - VERSIÓN ACTUALIZADA
// Reemplazar la función MyMusicScreen() en MainActivity.kt con esta versión

@Composable
fun MyMusicScreen() {
    val context = LocalContext.current
    val player = rememberMusicPlayer(context, pauseOnBackground = false)
    val firebaseManager = remember { FirebaseManager() }
    val songLikesManager = remember { SongLikesManager() }
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()
    
    val userId = authManager.getUserId() ?: ""
    
    var likedSongs by remember { mutableStateOf<List<ArtistCard>>(emptyList()) }
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
                // Obtener canciones con like del usuario
                likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
                filteredSongs = likedSongs
                isLoading = false
            } catch (e: Exception) {
                android.util.Log.e("MyMusicScreen", "Error cargando canciones: ${e.message}")
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    // Filtrar canciones según búsqueda
    LaunchedEffect(searchQuery, likedSongs) {
        filteredSongs = if (searchQuery.isEmpty()) {
            likedSongs
        } else {
            likedSongs.filter { song ->
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
            .background(PopArtColors.Black)
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

        // Barra de búsqueda
        if (isSearching) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                placeholder = { Text("Buscar por artista, género o ubicación...", color = PopArtColors.White.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = PopArtColors.Yellow)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = PopArtColors.White)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PopArtColors.White,
                    unfocusedTextColor = PopArtColors.White,
                    focusedBorderColor = PopArtColors.Yellow,
                    unfocusedBorderColor = PopArtColors.White,
                    cursorColor = PopArtColors.Yellow
                ),
                shape = RoundedCornerShape(30.dp),
                singleLine = true
            )
        } else {
            Text(
                "Canciones que te gustaron 🎵",
                fontSize = 18.sp,
                color = PopArtColors.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }

        // Contenido
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
                    Text("💔", fontSize = 80.sp)
                    Text(
                        "No tienes favoritos aún",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Yellow
                    )
                    Text(
                        "Dale ❤️ a las canciones que te gusten en Descubre",
                        fontSize = 14.sp,
                        color = PopArtColors.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp)
                    )
                }
            }
        } else if (filteredSongs.isEmpty()) {
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
                                        .background(
                                            if (isCurrentlyPlaying) PopArtColors.Black 
                                            else PopArtColors.MulticolorGradient,
                                            RoundedCornerShape(12.dp)
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
                                    color = if (isCurrentlyPlaying) PopArtColors.Black else PopArtColors.Black
                                )
                                Text(
                                    "${song.genre} • ${song.location}",
                                    fontSize = 14.sp,
                                    color = if (isCurrentlyPlaying) 
                                        PopArtColors.Black.copy(alpha = 0.7f) 
                                    else 
                                        PopArtColors.Black.copy(alpha = 0.7f),
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
