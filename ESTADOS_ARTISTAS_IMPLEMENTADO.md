# 📸 Estados de Artistas Implementado

## ¿Qué se agregó?

Se implementó un sistema de **Estados** (como Instagram Stories) que aparece en la pantalla "Tu Música" y muestra los estados de los artistas a los que **les diste like** (no de los que sigues).

## Características

### 1. **Estados de 24 Horas**
- Los estados expiran automáticamente después de 24 horas
- Se muestran solo los estados activos (no expirados)
- Cada estado muestra el tiempo restante

### 2. **Solo de Artistas a los que les Diste Like**
- Los estados solo aparecen si le diste like a alguna canción del artista
- Se cargan automáticamente desde Firebase
- Se actualizan en tiempo real
- Muestra estados de todos los artistas cuyas canciones te gustaron

### 3. **Indicador Visual**
- **Borde amarillo**: Estado no visto
- **Borde gris**: Estado ya visto
- Avatar del artista en el círculo

### 4. **Visor de Estados**
- Pantalla completa para ver el estado
- Tap izquierda: Estado anterior
- Tap derecha: Estado siguiente
- Barras de progreso en la parte superior
- Info del artista y tiempo restante
- Caption en la parte inferior

### 5. **Contador de Vistas**
- Se marca automáticamente como visto al abrir
- Incrementa el contador de vistas del estado
- Guarda qué estados ya viste

## Estructura de Datos

### Modelo: `ArtistStory`
```kotlin
data class ArtistStory(
    val id: String,
    val artistId: String,
    val artistName: String,
    val artistImageUrl: String,
    val mediaUrl: String,
    val mediaType: String, // "image" o "video"
    val caption: String,
    val timestamp: Long,
    val expiresAt: Long, // 24 horas después
    val views: Int,
    val isViewed: Boolean
)
```

### Colecciones en Firebase

#### `stories/`
```
stories/
  └── {storyId}/
      ├── artistId: "user123"
      ├── artistName: "Luna Beats"
      ├── artistImageUrl: "https://..."
      ├── mediaUrl: "https://..."
      ├── mediaType: "image"
      ├── caption: "Nueva canción! 🎵"
      ├── timestamp: 1234567890
      ├── expiresAt: 1234654290
      └── views: 42
```

#### `users/{userId}/viewedStories/`
```
users/
  └── {userId}/
      └── viewedStories/
          ├── {storyId1}/
          │   └── timestamp: 1234567890
          └── {storyId2}/
              └── timestamp: 1234567891
```

## Funciones Agregadas

### En `FirebaseManager.kt`

```kotlin
// Subir un estado
suspend fun uploadStory(
    artistId: String,
    artistName: String,
    artistImageUrl: String,
    mediaUri: Uri,
    mediaType: String,
    caption: String,
    onProgress: (Int) -> Unit
): String

// Obtener estados de artistas que sigue el usuario
suspend fun getStoriesFromFollowing(userId: String): List<ArtistStory>

// Marcar estado como visto
suspend fun markStoryAsViewed(storyId: String, userId: String)

// Limpiar estados expirados
suspend fun cleanupExpiredStories()
```

### En `MainActivity.kt`

```kotlin
// Componente del círculo de estado
@Composable
fun StoryCircle(story: ArtistStory, onClick: () -> Unit)

// Visor de estados en pantalla completa
@Composable
fun StoryViewerScreen(
    stories: List<ArtistStory>,
    startIndex: Int,
    userId: String,
    onDismiss: () -> Unit,
    onStoryViewed: (String) -> Unit
)
```

## Ubicación en la App

### Pantalla "Tu Música"
```
┌─────────────────────────────┐
│  TU MÚSICA              🔍  │
├─────────────────────────────┤
│  Canciones que te gustaron  │
├─────────────────────────────┤
│  ○ ○ ○ ○ ○  ← Estados      │
│  👤 👤 👤 👤 👤              │
├─────────────────────────────┤
│  🎵 Canción 1               │
│  🎵 Canción 2               │
│  🎵 Canción 3               │
└─────────────────────────────┘
```

## Flujo de Usuario

### Ver Estados
1. Usuario abre "Tu Música"
2. Ve círculos de estados en la parte superior
3. Hace clic en un círculo
4. Se abre el visor de estados en pantalla completa
5. Puede navegar entre estados con taps
6. El estado se marca automáticamente como visto

### Subir Estado (Para Artistas)
```kotlin
// Ejemplo de cómo un artista subiría un estado
scope.launch {
    try {
        val storyId = firebaseManager.uploadStory(
            artistId = userId,
            artistName = username,
            artistImageUrl = profileImageUrl,
            mediaUri = selectedImageUri,
            mediaType = "image",
            caption = "Nueva canción! 🎵",
            onProgress = { progress ->
                // Mostrar progreso
            }
        )
        // Estado subido exitosamente
    } catch (e: Exception) {
        // Manejar error
    }
}
```

## Características Técnicas

### Optimizaciones
- Solo carga estados de artistas que sigues
- Filtra estados expirados automáticamente
- Usa caché para estados ya vistos
- Limita consultas con `whereIn` (máximo 10 artistas)

### Seguridad
- Solo los artistas pueden subir estados
- Los estados se eliminan automáticamente después de 24 horas
- Las vistas se registran por usuario

### Performance
- Carga lazy de imágenes con Coil
- Estados se cargan en segundo plano
- No bloquea la UI principal

## Próximas Mejoras

### Funcionalidad Adicional
- [ ] Soporte para videos en estados
- [ ] Responder a estados con mensajes
- [ ] Reacciones rápidas (❤️, 🔥, 😮)
- [ ] Música de fondo en estados
- [ ] Filtros y stickers para crear estados
- [ ] Ver quién vio tu estado (para artistas)

### UI/UX
- [ ] Animación de transición entre estados
- [ ] Barra de progreso animada
- [ ] Gestos de swipe para navegar
- [ ] Zoom en imágenes
- [ ] Compartir estados

### Backend
- [ ] Notificaciones cuando un artista sube estado
- [ ] Estadísticas de vistas para artistas
- [ ] Destacar estados más populares
- [ ] Archivar estados favoritos

## Integración con Perfil

Los estados también pueden mostrarse en el perfil del artista:

```kotlin
// En ProfileScreen, mostrar estados del artista
LaunchedEffect(artistId) {
    val artistStories = firebaseManager.getStoriesFromArtist(artistId)
    // Mostrar en el perfil
}
```

## Limpieza Automática

Para mantener la base de datos limpia, ejecutar periódicamente:

```kotlin
// En MainActivity o en un Worker
scope.launch {
    firebaseManager.cleanupExpiredStories()
}
```

## Notas Importantes

1. **Expiración**: Los estados expiran exactamente 24 horas después de ser creados
2. **Privacidad**: Solo los seguidores ven los estados
3. **Almacenamiento**: Las imágenes se guardan en Firebase Storage
4. **Límite**: Firebase `whereIn` limita a 10 artistas por consulta

## Testing

### Probar Estados
1. Crear dos cuentas (Artista y Fan)
2. Fan sigue al Artista
3. Artista sube un estado
4. Fan abre "Tu Música"
5. ✅ Verificar que aparece el estado del artista
6. Fan hace clic en el estado
7. ✅ Verificar que se abre el visor
8. ✅ Verificar que se marca como visto (borde cambia a gris)

## Resumen

✅ Estados de 24 horas implementados  
✅ Solo muestra artistas que sigues  
✅ Indicador visual de visto/no visto  
✅ Visor de pantalla completa  
✅ Contador de vistas  
✅ Limpieza automática de expirados  

¡Los estados están listos para usar! 🎉
