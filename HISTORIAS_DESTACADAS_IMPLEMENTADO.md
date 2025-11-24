# ⭐ Sistema de Historias Destacadas (Highlights) - Implementación Completa

## ✅ Funcionalidad Implementada

Se ha implementado un **sistema completo de historias destacadas** siguiendo la propuesta, permitiendo a los usuarios marcar historias como favoritas y mostrarlas permanentemente en su perfil.

---

## 🎯 1. Lógica de Marcado (Acción del Usuario)

### Botón de Destacar en el Visor de Historias

**Ubicación:** Parte inferior central del visor de historias

**Características:**
- **FloatingActionButton** con icono de estrella (`ic_star`)
- **Estado visual dinámico:**
  - ⭐ **Destacada:** Fondo amarillo (`PopArtColors.Yellow`) con icono negro
  - 🌟 **No destacada:** Fondo blanco semi-transparente con icono blanco
- **Acción:** Al hacer clic, marca/desmarca la historia como destacada
- **Feedback:** El cambio es inmediato y se refleja en el menú hamburguesa

### Menú Hamburguesa (⋮)

También incluye la opción "Destacar historia" / "Quitar de destacados" con:
- Icono de estrella amarilla
- Texto dinámico según el estado actual
- Mismo comportamiento que el botón flotante

---

## 📁 2. Gestión de Base de Datos (Firebase)

### Estructura de Datos

```kotlin
data class Story(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val timestamp: Long = 0L,
    val isHighlighted: Boolean = false  // ⭐ Campo clave
)
```

### Colección en Firestore

**Colección:** `stories`

**Campos:**
- `userId`: ID del usuario propietario
- `username`: Nombre del usuario
- `imageUrl`: URL de la imagen (si aplica)
- `videoUrl`: URL del video (si aplica)
- `timestamp`: Momento de creación
- `isHighlighted`: **true** = destacada (permanente), **false** = temporal (24h)
- `expiresAt`: Timestamp de expiración (solo para no destacadas)

### Funciones de Firebase Implementadas

#### `createStory()`
Crea una nueva historia con opción de marcarla como destacada desde el inicio.

```kotlin
suspend fun createStory(
    userId: String,
    username: String,
    imageUrl: String = "",
    videoUrl: String = "",
    isHighlighted: Boolean = false
): String
```

#### `getUserStories()`
Obtiene historias activas (últimas 24 horas) de un usuario.

```kotlin
suspend fun getUserStories(userId: String): List<Story>
```

#### `getUserHighlightedStories()`
Obtiene **solo las historias destacadas** de un usuario (permanentes).

```kotlin
suspend fun getUserHighlightedStories(userId: String): List<Story>
```

#### `toggleStoryHighlight()`
Marca o desmarca una historia como destacada.

```kotlin
suspend fun toggleStoryHighlight(storyId: String, isHighlighted: Boolean)
```

#### `deleteStory()`
Elimina una historia permanentemente.

```kotlin
suspend fun deleteStory(storyId: String)
```

#### `artistHasActiveStory()`
Verifica si un artista tiene historias activas (para mostrar el anillo de color).

```kotlin
suspend fun artistHasActiveStory(userId: String): Boolean
```

#### `cleanupExpiredStories()`
Limpia automáticamente historias expiradas (solo las NO destacadas).

```kotlin
suspend fun cleanupExpiredStories()
```

**Importante:** Las historias destacadas (`isHighlighted = true`) **nunca se eliminan automáticamente**.

#### `uploadStoryMedia()`
Sube imágenes o videos para historias.

```kotlin
suspend fun uploadStoryMedia(
    uri: Uri, 
    userId: String, 
    isVideo: Boolean, 
    onProgress: (Int) -> Unit
): String
```

---

## 🖼️ 3. Visualización en el Perfil

### Sección "⭐ Highlights"

**Ubicación:** Después de "Mis Historias" y antes de la línea divisoria

**Características:**
- **Título:** "⭐ Highlights" (con emoji de estrella)
- **Diseño:** Carrusel horizontal (`LazyRow`) con círculos
- **Contenido:** Solo historias marcadas como destacadas
- **Indicador visual:** Icono de estrella amarilla en la esquina inferior derecha de cada círculo

### Componente Visual

```kotlin
LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    items(highlightedStories) { story ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(3.dp, PopArtColors.Yellow, CircleShape)
            ) {
                // Imagen o video
                AsyncImage(model = story.imageUrl, ...)
                
                // Icono de estrella en la esquina
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .background(PopArtColors.Yellow, CircleShape)
                ) {
                    Icon(ic_star, ...)
                }
            }
            
            // Tiempo transcurrido
            Text(formatTimeAgo(story.timestamp))
        }
    }
}
```

### Comportamiento

- **Carga automática:** Los highlights se cargan al abrir el perfil
- **Click:** Abre el visor de historias mostrando solo los highlights
- **Orden:** Más recientes primero
- **Persistencia:** Permanecen hasta que el usuario las elimine manualmente

---

## 🔄 4. Flujo Completo de Uso

### Crear y Destacar una Historia

1. Usuario toca el botón **+** en su foto de perfil
2. Toma una foto o selecciona de galería
3. La historia se publica (temporal por defecto)
4. Usuario toca su foto de perfil para ver sus historias
5. En el visor, toca el **botón de estrella** en la parte inferior
6. La historia se marca como destacada (fondo amarillo)
7. La historia aparece en la sección **"⭐ Highlights"** del perfil

### Desmarcar un Highlight

1. Usuario abre el visor de historias
2. Navega a la historia destacada
3. Toca el **botón de estrella** (ahora amarillo)
4. La historia se desmarca y volverá a expirar en 24h
5. Desaparece de la sección "Highlights"

### Eliminar una Historia

1. Usuario abre el visor de historias
2. Toca el **menú hamburguesa** (⋮)
3. Selecciona **"Eliminar historia"**
4. La historia se elimina permanentemente
5. Se avanza a la siguiente historia o se cierra el visor

---

## 🎨 5. Diseño Visual

### Botón de Destacar (FloatingActionButton)

```kotlin
FloatingActionButton(
    onClick = { onHighlightStory(currentStory) },
    containerColor = if (currentStory.isHighlighted) 
        PopArtColors.Yellow      // ⭐ Destacada
    else 
        Color.White.copy(alpha = 0.3f),  // 🌟 No destacada
    modifier = Modifier.size(56.dp)
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_star),
        tint = if (currentStory.isHighlighted) 
            PopArtColors.Black 
        else 
            Color.White
    )
}
```

### Círculo de Highlight

- **Tamaño:** 70dp
- **Borde:** 3dp amarillo (`PopArtColors.Yellow`)
- **Badge:** Estrella de 20dp en esquina inferior derecha
- **Texto:** Tiempo transcurrido (ej: "2h", "3d")

---

## 📊 6. Integración con Otras Pantallas

### MyMusicScreen (Tu Música)

- Muestra burbujas de historias de artistas favoritos
- Al hacer clic, abre el visor con historias activas
- Permite destacar/eliminar historias de otros artistas (si es el propietario)

### ProfileScreen (Perfil)

- Muestra "Mis Historias" (últimas 24h)
- Muestra "⭐ Highlights" (permanentes)
- Botón + para agregar nuevas historias
- Click en foto de perfil abre visor de historias propias

### StoryViewerScreen (Visor)

- Barra de progreso automático
- Botón de estrella flotante para destacar
- Menú hamburguesa con opciones completas
- Navegación por tap o swipe

---

## 🔧 7. Funciones Auxiliares

### `formatTimeAgo()`

Formatea timestamps en texto legible:

```kotlin
fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Ahora"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        else -> "${diff / 86400000}d"
    }
}
```

---

## 🚀 8. Próximos Pasos Opcionales

### Mejoras Sugeridas

1. **Grupos de Highlights:**
   - Permitir crear "álbumes" de highlights con nombres personalizados
   - Ej: "Conciertos", "Estudio", "Fans"

2. **Portadas Personalizadas:**
   - Permitir elegir una imagen de portada para cada grupo de highlights
   - Diferente de la primera historia del grupo

3. **Compartir Highlights:**
   - Generar enlaces compartibles para highlights individuales
   - Integración con Android Share Sheet

4. **Estadísticas:**
   - Mostrar vistas de cada highlight
   - Analíticas de engagement

5. **Orden Personalizado:**
   - Permitir reordenar highlights arrastrando
   - Fijar highlights importantes al inicio

---

## 📝 9. Notas Técnicas

### Persistencia

- **Historias normales:** Se eliminan automáticamente después de 24 horas
- **Historias destacadas:** Permanecen indefinidamente hasta eliminación manual
- **Campo clave:** `isHighlighted` en Firestore

### Rendimiento

- Carga lazy de historias (solo cuando se necesitan)
- Caché de imágenes con Coil
- Queries optimizadas con índices en Firestore

### Seguridad

- Solo el propietario puede destacar/eliminar sus historias
- Validación de userId en todas las operaciones
- URLs firmadas para medios en Firebase Storage

---

## ✅ Estado de Implementación

| Componente | Estado | Archivo |
|------------|--------|---------|
| Data class Story | ✅ Completo | ScreenStubs.kt |
| StoryViewerScreen | ✅ Completo | ScreenStubs.kt |
| Botón de destacar | ✅ Completo | ScreenStubs.kt |
| Funciones Firebase | ✅ Completo | FirebaseManager.kt |
| Sección Highlights | ✅ Completo | ProfileScreen.kt |
| Integración MyMusic | ✅ Completo | ScreenStubs.kt |
| Limpieza automática | ✅ Completo | FirebaseManager.kt |

---

## 🎯 Resumen

El sistema de historias destacadas está **completamente funcional** y listo para usar. Los usuarios pueden:

1. ✅ Crear historias (temporales por defecto)
2. ✅ Marcar historias como destacadas con el botón de estrella
3. ✅ Ver sus highlights en una sección dedicada del perfil
4. ✅ Desmarcar highlights para que vuelvan a ser temporales
5. ✅ Eliminar historias permanentemente
6. ✅ Las historias destacadas nunca expiran automáticamente

**Fecha de implementación:** 21 de noviembre de 2025
