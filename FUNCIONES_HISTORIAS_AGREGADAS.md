# ✅ Funciones de Historias Agregadas a FirebaseManager

## 🎯 Problema Identificado

Las historias no se mostraban porque faltaba la función `uploadStoryMetadata` en el FirebaseManager.kt, aunque ya existían otras funciones relacionadas con historias.

## 🛠️ Solución Implementada

### Función Agregada: `uploadStoryMetadata`

Se agregó la función que faltaba al final de `FirebaseManager.kt`:

```kotlin
suspend fun uploadStoryMetadata(story: ArtistStory): String
```

Esta función:
- ✅ Guarda la metadata de una historia en la colección `stories` de Firestore
- ✅ Incluye todos los campos necesarios (artistId, mediaUrl, mediaType, timestamp, expiresAt, etc.)
- ✅ Incrementa el contador de historias en el perfil del usuario
- ✅ Incluye logs detallados para debugging
- ✅ Maneja errores apropiadamente

## 📋 Funciones de Historias Disponibles

### 1. **uploadStory** (Ya existía)
Sube el archivo (imagen/video) a Firebase Storage y guarda la metadata en Firestore.
```kotlin
suspend fun uploadStory(
    artistId: String,
    artistName: String,
    artistImageUrl: String,
    mediaUri: Uri,
    mediaType: String,
    caption: String,
    onProgress: (Int) -> Unit
): String
```

### 2. **uploadStoryMetadata** (✨ NUEVA)
Guarda solo la metadata de una historia en Firestore.
```kotlin
suspend fun uploadStoryMetadata(story: ArtistStory): String
```

### 3. **getUserStories** (Ya existía)
Obtiene todas las historias activas (no expiradas) de un usuario.
```kotlin
suspend fun getUserStories(userId: String): List<ArtistStory>
```

### 4. **getUserStoriesOld** (Deprecated)
Versión antigua de getUserStories, mantiene compatibilidad.
```kotlin
suspend fun getUserStoriesOld(userId: String): List<Story>
```

### 5. **getStoriesFromFollowing** (Ya existía)
Obtiene historias de artistas que el usuario sigue.
```kotlin
suspend fun getStoriesFromFollowing(userId: String): List<ArtistStory>
```

### 6. **getStoriesFromLikedArtists** (Ya existía)
Obtiene historias de artistas a los que el usuario dio like.
```kotlin
suspend fun getStoriesFromLikedArtists(userId: String, songLikesManager: SongLikesManager): List<ArtistStory>
```

### 7. **markStoryAsViewed** (Ya existía)
Marca una historia como vista por el usuario.
```kotlin
suspend fun markStoryAsViewed(storyId: String, userId: String)
```

### 8. **createStory** (Ya existía)
Crea una historia simple (versión básica).
```kotlin
suspend fun createStory(
    userId: String,
    username: String,
    imageUrl: String = "",
    videoUrl: String = "",
    isHighlighted: Boolean = false
): String
```

## 🔄 Flujo de Subida de Historias

### Opción 1: Usando `uploadStory` (Recomendado)
```kotlin
// ProfileScreen.kt ya usa este método
val storyId = firebaseManager.uploadStory(
    artistId = userId,
    artistName = username,
    artistImageUrl = profileImageUrl,
    mediaUri = imageUri,
    mediaType = "image",
    caption = "",
    onProgress = { progress -> /* actualizar UI */ }
)
```

### Opción 2: Usando `uploadStoryMetadata` (Manual)
```kotlin
// 1. Subir el archivo primero
val mediaUrl = firebaseManager.uploadImageFile(imageUri) { progress -> 
    /* actualizar UI */ 
}

// 2. Crear el objeto ArtistStory
val newStory = ArtistStory(
    artistId = userId,
    artistName = username,
    artistImageUrl = profileImageUrl,
    mediaUrl = mediaUrl,
    mediaType = "image",
    caption = "",
    timestamp = System.currentTimeMillis(),
    expiresAt = System.currentTimeMillis() + 86400000 // 24 horas
)

// 3. Guardar la metadata
val storyId = firebaseManager.uploadStoryMetadata(newStory)
```

## 📊 Estructura de Datos en Firestore

### Colección: `stories`
```
stories/
  └── {storyId}/
      ├── artistId: String
      ├── artistName: String
      ├── artistImageUrl: String
      ├── mediaUrl: String
      ├── mediaType: String ("image" o "video")
      ├── caption: String
      ├── timestamp: Long
      ├── expiresAt: Long (timestamp + 24 horas)
      ├── views: Int
      └── isHighlighted: Boolean
```

### Colección: `users/{userId}/viewedStories`
```
users/
  └── {userId}/
      └── viewedStories/
          └── {storyId}/
              └── timestamp: Long
```

## 🔍 Logs de Debugging

La función `uploadStoryMetadata` incluye logs detallados:

```
🚀 ===== GUARDANDO METADATA DE HISTORIA =====
👤 artistId: {userId}
📝 artistName: {username}
🔗 mediaUrl: {url}
🎬 mediaType: {type}
💾 Guardando en Firestore...
📊 Documento: {data}
✅ ===== METADATA GUARDADA EXITOSAMENTE =====
🆔 ID del documento: {docId}
📍 Ruta: stories/{docId}
```

## ✅ Estado Actual

- ✅ Función `uploadStoryMetadata` agregada
- ✅ Función `getUserStories` ya existía y funciona
- ✅ Función `uploadStory` ya existía y funciona
- ✅ ProfileScreen.kt ya usa `uploadStory` correctamente
- ✅ Sistema de reintentos implementado en ProfileScreen.kt
- ✅ Logs detallados para debugging

## 🎯 Próximos Pasos

1. **Probar la subida de historias** desde ProfileScreen
2. **Verificar los logs** en Logcat para confirmar que se guardan correctamente
3. **Verificar en Firebase Console** que los documentos se crean en la colección `stories`
4. **Confirmar que las historias se muestran** después de subirlas

## 📝 Notas Importantes

- Las historias expiran automáticamente después de 24 horas
- El sistema filtra historias expiradas al obtenerlas
- Se incrementa el contador `totalStories` en el perfil del usuario
- Los logs incluyen emojis para facilitar la identificación visual
- La función maneja errores y los reporta con detalles

## 🐛 Debugging

Si las historias no se muestran, verificar:

1. **Logs de subida**: Buscar `UPLOAD_STORY_METADATA` en Logcat
2. **Logs de obtención**: Buscar `HISTORIAS_FIREBASE` en Logcat
3. **Firebase Console**: Verificar que existan documentos en `stories`
4. **Permisos**: Verificar reglas de seguridad en Firestore
5. **Timestamp**: Verificar que `expiresAt > now`

## 📱 Uso en ProfileScreen.kt

El código actual en ProfileScreen.kt ya está correcto y usa `uploadStory`:

```kotlin
val storyId = firebaseManager.uploadStory(
    artistId = userId,
    artistName = userProfile?.username ?: "Usuario",
    artistImageUrl = userProfile?.profileImageUrl ?: "",
    mediaUri = it,
    mediaType = "image",
    caption = "",
    onProgress = { progress -> uploadProgress = progress }
)
```

Después de subir, se recargan las historias con reintentos:

```kotlin
var attempts = 0
do {
    attempts++
    newStories = firebaseManager.getUserStories(userId)
    if (newStories.size > userStories.size) break
    if (attempts < maxAttempts) delay(2000)
} while (attempts < maxAttempts)

userStories = newStories
```

---

**Fecha**: 21 de noviembre de 2025
**Estado**: ✅ Implementado y listo para probar
