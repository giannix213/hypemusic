# 🐛 Problema: Canciones de Otros Usuarios Aparecen en "Tu Música"

## El Problema

Cuando un usuario nuevo entra a la app:
- En **"Descubre"**: Ve TODAS las canciones (incluyendo las que otros usuarios subieron)
- En **"Tu Música"**: Aparecen canciones sin que les haya dado like

## Causa del Problema

El sistema actual usa `FavoritesManager` que guarda los favoritos **localmente** en el dispositivo usando `SharedPreferences`. Esto significa:

1. Los favoritos NO se sincronizan entre dispositivos
2. Los favoritos NO se guardan en Firebase
3. Cuando un usuario nuevo instala la app, el `FavoritesManager` está vacío
4. Pero `getAllSongs()` devuelve TODAS las canciones de Firebase

## Solución

Necesitamos implementar un sistema de likes en Firebase para que:

### 1. En "Descubre"
- Mostrar solo canciones que el usuario NO ha visto
- Filtrar canciones que ya le dio like o dislike

### 2. En "Tu Música"  
- Mostrar SOLO canciones que el usuario le dio like
- Obtener los likes desde Firebase, no desde el almacenamiento local

## Implementación

### Paso 1: Sistema de Likes en Firebase

Ya creé `FirebaseManager_Likes.kt` con:
- `toggleSongLike()` - Dar/quitar like a una canción
- `hasUserLikedSong()` - Verificar si el usuario dio like
- `getUserLikedSongs()` - Obtener IDs de canciones con like
- `getUserLikedSongsDetails()` - Obtener detalles completos de canciones con like

### Paso 2: Actualizar DiscoverScreen

Filtrar canciones que el usuario ya vio:
```kotlin
// En DiscoverScreen, después de cargar canciones
val allSongs = firebaseManager.getAllSongs()
val likedSongIds = songLikesManager.getUserLikedSongs(userId)
val seenSongIds = favoritesManager.getSeenSongIds() // IDs de canciones vistas

// Filtrar canciones no vistas
artists = allSongs.filter { song ->
    song.id !in likedSongIds && song.id !in seenSongIds
}
```

### Paso 3: Actualizar MyMusicScreen

Mostrar solo canciones con like del usuario:
```kotlin
// En MyMusicScreen
val songLikesManager = remember { SongLikesManager() }
val firebaseManager = remember { FirebaseManager() }
val authManager = remember { AuthManager(context) }
val userId = authManager.getUserId() ?: ""

LaunchedEffect(userId) {
    if (userId.isNotEmpty()) {
        // Obtener canciones que le gustaron al usuario desde Firebase
        val likedSongs = songLikesManager.getUserLikedSongsDetails(userId, firebaseManager)
        favorites = likedSongs.map { song ->
            FavoriteSong(
                id = song.id,
                artistName = song.name,
                genre = song.genre,
                location = song.location,
                songUrl = song.songUrl,
                reactionType = "❤️"
            )
        }
        filteredFavorites = favorites
    }
}
```

### Paso 4: Actualizar Acciones de Like/Dislike

Cuando el usuario da like o dislike:
```kotlin
// En DiscoverScreen, al hacer swipe
val songLikesManager = remember { SongLikesManager() }

// Al dar like (swipe derecha)
scope.launch {
    songLikesManager.toggleSongLike(artist.id, userId)
    favoritesManager.addFavorite(artist, "❤️")
}

// Al dar dislike (swipe izquierda)
scope.launch {
    // Solo marcar como visto, no guardar en favoritos
    favoritesManager.markAsSeen(artist.id)
}
```

## Beneficios de la Solución

✅ **Sincronización**: Los likes se guardan en Firebase y se sincronizan entre dispositivos

✅ **Separación clara**: 
- "Descubre" = Canciones nuevas que no has visto
- "Tu Música" = Solo canciones que te gustaron

✅ **Escalabilidad**: Funciona con múltiples usuarios sin conflictos

✅ **Persistencia**: Los likes no se pierden al desinstalar la app

## Archivos a Modificar

1. ✅ `FirebaseManager_Likes.kt` - Ya creado
2. ⏳ `MainActivity.kt` - Actualizar `DiscoverScreen` y `MyMusicScreen`
3. ⏳ `FavoritesManager.kt` - Agregar método `markAsSeen()` y `getSeenSongIds()`

## Próximos Pasos

1. Actualizar `FavoritesManager` para distinguir entre "visto" y "gustado"
2. Modificar `DiscoverScreen` para usar el nuevo sistema
3. Modificar `MyMusicScreen` para cargar desde Firebase
4. Probar con múltiples usuarios
