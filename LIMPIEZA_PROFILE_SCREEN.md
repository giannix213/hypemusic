# 🧹 Limpieza de ProfileScreen.kt

## 🚨 Problemas Detectados

1. **Archivo muy grande**: 3056 líneas (debería ser ~1500-2000)
2. **Se queda cargando**: Posiblemente por `getUserStories()` colgándose
3. **Botón + no funciona**: Problema con `showStoryCamera`
4. **Historias no se registran**: Aunque el archivo se sube a Firebase

## ✅ Soluciones Aplicadas

### 1. Simplificado LaunchedEffect
```kotlin
LaunchedEffect(userId) {
    if (userId.isNotEmpty() && !isAnonymous) {
        isLoading = true
        try {
            userProfile = firebaseManager.getFullUserProfile(userId)
            songMediaUrls = firebaseManager.getUserSongMedia(userId)
            userStories = firebaseManager.getUserStories(userId)
        } catch (e: Exception) {
            android.util.Log.e("ProfileScreen", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }
}
```

### 2. Simplificado reloadStories()
```kotlin
fun reloadStories() {
    scope.launch {
        try {
            val stories = firebaseManager.getUserStories(userId)
            userStories = stories
            Toast.makeText(context, "Historias: ${stories.size}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar historias", Toast.LENGTH_SHORT).show()
        }
    }
}
```

### 3. Cambiado a uploadStoryMedia()
Ahora usa `uploadStoryMedia()` en lugar de `uploadImageFile()` para guardar en la carpeta correcta `stories/`.

## 🔧 Pasos Manuales Necesarios

### Paso 1: Verificar Firestore
1. Abre Firebase Console
2. Ve a Firestore Database
3. Busca la colección `stories`
4. Verifica que existan documentos
5. Verifica que tengan estos campos:
   - `artistId`
   - `mediaUrl`
   - `timestamp`
   - `expiresAt` (debe ser > timestamp actual)

### Paso 2: Verificar getUserStories()
El problema puede estar en FirebaseManager.getUserStories(). Verifica que:
- No tenga un query que se quede colgado
- Maneje errores correctamente
- No tenga un loop infinito

### Paso 3: Simplificar Lógica de Subida
Elimina los reintentos excesivos (5 intentos con delays de 2s). Usa:

```kotlin
// Después de uploadStoryMetadata
kotlinx.coroutines.delay(2000)
userStories = firebaseManager.getUserStories(userId)
Toast.makeText(context, "✓ Historia publicada", Toast.LENGTH_SHORT).show()
```

### Paso 4: Verificar StoryCamera
El botón + debe abrir `showStoryCamera = true`. Verifica que:
```kotlin
// En el botón +
.clickable { 
    showStoryCamera = true 
}

// Y que StoryCamera esté definido
if (showStoryCamera) {
    StoryCamera(
        onBack = { showStoryCamera = false },
        onPhotoTaken = { uri -> /* ... */ }
    )
}
```

## 🐛 Debug

### Ver logs en Logcat:
```
ProfileScreen - Ver carga inicial
HISTORIAS_FIREBASE - Ver query de historias
UPLOAD_STORY_METADATA - Ver guardado
```

### Comandos útiles:
```kotlin
// En LaunchedEffect
android.util.Log.d("ProfileScreen", "userId: $userId, isAnonymous: $isAnonymous")

// En getUserStories
android.util.Log.d("ProfileScreen", "Historias obtenidas: ${userStories.size}")

// En onPhotoTaken
android.util.Log.d("ProfileScreen", "onPhotoTaken llamado con URI: $uri")
```

## 📋 Checklist

- [ ] Firestore tiene colección `stories` con documentos
- [ ] Documentos tienen `expiresAt` > timestamp actual
- [ ] `getUserStories()` no se queda colgado
- [ ] Botón + abre la cámara
- [ ] `uploadStoryMedia()` guarda en `stories/`
- [ ] `uploadStoryMetadata()` guarda en Firestore
- [ ] Logs muestran el flujo completo

## 🎯 Próximos Pasos

1. **Reinicia la app completamente**
2. **Verifica logs** al abrir el perfil
3. **Toca el botón +** y verifica que abra la cámara
4. **Toma una foto** y verifica logs de subida
5. **Verifica en Firebase** que se creó el documento
6. **Recarga** tocando el badge de historias

---

**Nota**: Si el problema persiste, el issue probablemente está en `FirebaseManager.getUserStories()` que puede tener un query mal formado o que se queda esperando indefinidamente.
