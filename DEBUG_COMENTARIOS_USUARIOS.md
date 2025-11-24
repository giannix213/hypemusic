# 🐛 Debug: Comentarios y Usuarios - GUÍA DE VERIFICACIÓN

## 🔍 Problemas Reportados

1. **No se registran los datos de usuarios que comentan**
2. **Aparece "@usuario" en lugar del nombre real del usuario que subió el video**

## ✅ Correcciones Aplicadas

### 1. Eliminado el símbolo "@" del username
**Antes:**
```kotlin
Text("@${currentVideo.username}", ...)
```

**Después:**
```kotlin
Text(currentVideo.username, ...)
```

### 2. Agregados Logs de Debug Extensivos

#### En LiveScreenNew.kt (al agregar comentario):
```kotlin
android.util.Log.d("Comments", "📝 Agregando comentario:")
android.util.Log.d("Comments", "  UserId: '$userId'")
android.util.Log.d("Comments", "  Username: '$username'")
android.util.Log.d("Comments", "  VideoId: '${video.id}'")
android.util.Log.d("Comments", "  Comentario: '$newComment'")
```

#### En FirebaseManager.kt (al leer comentarios):
```kotlin
android.util.Log.d("FirebaseManager", "📖 Obteniendo comentarios del video: $videoId")
android.util.Log.d("FirebaseManager", "📊 Total de comentarios encontrados: ${snapshot.documents.size}")
android.util.Log.d("FirebaseManager", "💬 Comentario ${doc.id}:")
android.util.Log.d("FirebaseManager", "  UserId: '$userId'")
android.util.Log.d("FirebaseManager", "  Username: '$username'")
```

## 🧪 Pasos para Verificar

### 1. Verificar que el Usuario Esté Autenticado

Ejecuta la app y busca en los logs:
```
Comments: 📝 Agregando comentario:
Comments:   UserId: 'abc123...'
Comments:   Username: 'Juan Pérez'
```

**Si userId está vacío:**
- El usuario no está autenticado correctamente
- Verificar AuthManager.getUserId()
- Verificar que el usuario haya iniciado sesión

**Si username está vacío:**
- El perfil del usuario no tiene nombre configurado
- Verificar AuthManager.getUserName()
- Verificar que el usuario haya completado su perfil

### 2. Verificar que el Comentario se Guarde en Firestore

Busca en los logs:
```
FirebaseManager: 💬 Comentario agregado al video abc123
```

**Si hay error:**
```
Comments: ❌ Error agregando comentario: [mensaje de error]
```

Posibles causas:
- Permisos de Firestore incorrectos
- Conexión a internet
- Video ID inválido

### 3. Verificar que los Comentarios se Lean Correctamente

Busca en los logs:
```
FirebaseManager: 📖 Obteniendo comentarios del video: abc123
FirebaseManager: 📊 Total de comentarios encontrados: 3
FirebaseManager: 💬 Comentario xyz789:
FirebaseManager:   UserId: 'user123'
FirebaseManager:   Username: 'Juan Pérez'
FirebaseManager:   Text: '¡Increíble video!'
```

**Si no hay comentarios:**
- Verificar que se hayan guardado correctamente
- Verificar en Firebase Console: `contest_entries/{videoId}/comments`

### 4. Verificar en Firebase Console

1. Ir a Firebase Console
2. Firestore Database
3. Navegar a: `contest_entries/{videoId}/comments`
4. Verificar que cada comentario tenga:
   ```
   {
     userId: "abc123...",
     username: "Juan Pérez",
     text: "¡Increíble video!",
     timestamp: 1234567890
   }
   ```

## 🔧 Estructura de Datos Esperada

### Comentario en Firestore:
```
contest_entries/
  └── {videoId}/
      └── comments/
          └── {commentId}/
              ├── userId: "user123"
              ├── username: "Juan Pérez"
              ├── text: "¡Increíble video!"
              └── timestamp: 1234567890
```

### Modelo VideoComment:
```kotlin
data class VideoComment(
    val id: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0,
    var isLiked: Boolean = false,
    val replies: List<VideoComment> = emptyList(),
    val isVoiceNote: Boolean = false
)
```

## 🚨 Validaciones Agregadas

### Validación de userId vacío:
```kotlin
if (userId.isEmpty()) {
    android.util.Log.e("Comments", "❌ Error: userId está vacío")
    return@launch
}
```

### Validación de username vacío:
```kotlin
if (username.isEmpty()) {
    android.util.Log.e("Comments", "❌ Error: username está vacío")
    return@launch
}
```

## 📱 Cómo Probar

1. **Ejecutar la app**
2. **Ir a la pantalla de Live**
3. **Ver un video**
4. **Tocar el botón de comentarios** 💬
5. **Escribir un comentario**
6. **Tocar enviar**
7. **Revisar los logs en Logcat**

### Filtros de Logcat Útiles:
```
Comments
FirebaseManager
```

## ✅ Checklist de Verificación

- [ ] El usuario está autenticado (userId no vacío)
- [ ] El usuario tiene nombre configurado (username no vacío)
- [ ] El comentario se guarda en Firestore
- [ ] El comentario aparece en la lista después de enviarlo
- [ ] El username se muestra correctamente (sin @)
- [ ] El timestamp se formatea correctamente
- [ ] Los datos persisten después de cerrar y abrir la app

## 🎯 Resultado Esperado

Después de estas correcciones:
- ✅ El username del video se muestra sin "@"
- ✅ Los comentarios se guardan con userId y username
- ✅ Los logs muestran toda la información de debug
- ✅ Es fácil identificar dónde está el problema si algo falla

## 📝 Próximos Pasos

Si después de revisar los logs encuentras que:

1. **userId está vacío**: Revisar AuthManager y autenticación
2. **username está vacío**: Revisar perfil de usuario
3. **Error al guardar**: Revisar permisos de Firestore
4. **Error al leer**: Verificar estructura de datos en Firestore

**¡Con estos logs detallados podrás identificar exactamente dónde está el problema!** 🔍
