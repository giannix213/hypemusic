# Resumen de ProfileScreen.kt

## Descripción General
ProfileScreen es la pantalla de perfil de usuario en la app HypeMatch. Muestra información del usuario, sus historias, galería de fotos/videos, estadísticas y permite editar el perfil.

## Componentes Principales

### 1. Estados y Variables
- `userProfile`: Datos del perfil del usuario (UserProfile)
- `userStories`: Lista de historias del usuario (List<ArtistStory>)
- `songMediaUrls`: URLs de medios de canciones del usuario
- `isLoading`: Estado de carga inicial
- `isUploadingMedia`: Estado de subida de archivos
- `uploadProgress`: Progreso de subida (0-100)
- `uploadType`: Tipo de subida ("story", "profile", "cover", "gallery")
- `showStoryViewer`: Mostrar visor de historias
- `showStoryCamera`: Mostrar cámara para historias
- `showStoryOptions`: Mostrar opciones de historia (BottomSheet)
- `showEditDialog`: Mostrar diálogo de edición de perfil
- `expandedImageUrl`: URL de imagen expandida en galería
- `isAnonymous`: Si el usuario es invitado/anónimo

### 2. Funciones de Carga de Datos

#### `refreshProfile()`
- Recarga todos los datos del perfil
- Obtiene: perfil completo, medios de canciones, historias
- Se ejecuta cuando se necesita actualizar la UI

#### `LaunchedEffect(userId)`
- Carga inicial de datos al entrar a la pantalla
- Carga perfil, medios y historias
- Maneja usuarios anónimos

#### `reloadStories()`
- Recarga manualmente las historias
- Muestra Toast con cantidad de historias

### 3. Launchers (Selectores de Archivos)

#### `profileImageLauncher`
- Selecciona imagen de perfil
- Sube a Firebase Storage (carpeta "profile")
- Actualiza Firestore con la nueva URL
- Muestra progreso de subida

#### `coverImageLauncher`
- Selecciona imagen de portada
- Sube a Firebase Storage (carpeta "cover")
- Actualiza Firestore con la nueva URL

#### `galleryLauncher`
- Selecciona fotos/videos para galería
- Sube a Firebase Storage
- Agrega URL al array `galleryPhotos` en Firestore

#### `storyImageLauncher`
- Selecciona imagen para historia desde galería
- **Proceso de 3 pasos:**
  1. Sube archivo a Firebase Storage (carpeta "stories/")
  2. Crea objeto ArtistStory con metadata
  3. Guarda metadata en Firestore
- Espera 3 segundos y hace hasta 5 reintentos para verificar que la historia se guardó
- Actualiza la UI con las nuevas historias

### 4. Componentes de UI

#### Header
- Imagen de portada (220dp altura)
- Botón de editar portada (esquina superior derecha)
- Gradiente oscuro en la parte inferior para contraste

#### Foto de Perfil
- Círculo de 140dp
- Borde con gradiente multicolor si tiene historias
- Borde amarillo si no tiene historias
- Clickable para abrir visor de historias (solo si tiene historias)
- Botón "+" para agregar historia (esquina inferior derecha)
- Botón de compartir perfil (esquina superior derecha del círculo)

#### Nombre de Usuario
- Tamaño 32sp, negrita
- Botón de editar nombre al lado
- Badge "🎤 Artista" si `isArtist == true`

#### Estadísticas
- 4 columnas: Seguidores, Siguiendo, Canciones, Plays
- Fondo con borde amarillo
- Formato especial para plays (K, M)

#### Sección "Mis Historias"
- LazyRow horizontal con historias
- Primer item: botón "Agregar" o "Nueva"
- Cada historia:
  - Círculo de 80dp con borde cyan
  - Indicador de video si es video
  - Botón de eliminar (esquina superior derecha)
  - Caption o "Historia" como texto
- Máximo 10 historias visibles
- Mensaje si no hay historias: "📸 No tienes historias aún"

#### Highlights (Historias Destacadas)
- Sección "⭐ Highlights"
- LazyRow horizontal
- Círculos de 70dp con borde amarillo
- Icono de estrella en esquina inferior derecha
- Muestra timestamp formateado

#### Botón "Editar Perfil"
- Botón amarillo con icono de editar
- Abre diálogo `EditProfileDialog`
- Permite editar: bio, país, géneros, redes sociales

#### Biografía
- Fondo semi-transparente con borde cyan
- Padding de 16dp
- Solo se muestra si hay bio

#### País de Origen
- Fondo amarillo semi-transparente
- Icono de mundo
- Solo se muestra si está configurado

#### Géneros Musicales
- Fondo cyan semi-transparente
- Icono de géneros
- Solo se muestra si están configurados

#### Redes Sociales
- Título "Redes Sociales"
- Cada red con:
  - Fondo de color según plataforma (Instagram: rosa, TikTok: cyan, YouTube: rojo)
  - Icono de la plataforma
  - Handle con "@"
  - Clickable para abrir enlace

#### Galería
- Título "Galería"
- LazyRow horizontal
- Imágenes de 140dp con borde amarillo
- Clickable para expandir imagen

### 5. Diálogos y Pantallas Modales

#### `showStoryOptions` (BottomSheet)
Muestra 3 opciones:
1. **Tomar Foto**: Abre `StoryCamera`
2. **Seleccionar de Galería**: Abre `storyImageLauncher`
3. **Cambiar Foto de Perfil**: Abre `profileImageLauncher`

#### `showStoryCamera` (StoryCamera)
- Pantalla completa de cámara
- Callback `onPhotoTaken`:
  - Cierra la cámara
  - Sube la foto como historia (mismo proceso de 3 pasos)
  - Hace reintentos para verificar que se guardó
  - Actualiza UI

#### `showEditDialog` (EditProfileDialog)
- Edita bio, país, géneros y redes sociales
- Guarda en Firestore
- Actualiza estado local

#### `showShareDialog`
- Diálogo para compartir perfil

#### `showStoryViewer`
- Visor de historias en pantalla completa
- Navega por las historias del usuario

### 6. Indicadores de Progreso

#### Durante Subida de Archivos
- `isUploadingMedia = true`
- `uploadType` indica qué se está subiendo
- `uploadProgress` muestra porcentaje (0-100)
- Se muestra CircularProgressIndicator

### 7. Manejo de Usuarios Anónimos
- Si `isAnonymous == true`:
  - Muestra perfil básico con username "Invitado"
  - Oculta botones de edición
  - No permite subir contenido
  - No muestra opciones de historia

### 8. Logs de Debug
El código tiene logs extensivos para debugging:
- `📝` Cargando datos
- `🔄` Recargando
- `✅` Operación exitosa
- `❌` Error
- `📸` Historias
- `🚀` Subida de archivos
- `💾` Guardando en Firestore
- `🆔` IDs generados
- `📊` Estadísticas

### 9. Integración con Firebase

#### FirebaseManager
- `getFullUserProfile(userId)`: Obtiene perfil completo
- `getUserSongMedia(userId)`: Obtiene medios de canciones
- `getUserStories(userId)`: Obtiene historias del usuario
- `uploadProfileImage()`: Sube imagen de perfil/portada
- `uploadGalleryMedia()`: Sube foto/video a galería
- `uploadStoryMedia()`: Sube archivo de historia
- `uploadStoryMetadata()`: Guarda metadata de historia en Firestore
- `deleteStory()`: Elimina historia
- `updateProfileImage()`: Actualiza URL de foto de perfil
- `updateCoverImage()`: Actualiza URL de portada
- `updateUsername()`: Actualiza nombre de usuario
- `updateUserProfile()`: Actualiza bio y redes sociales
- `getUserHighlightedStories()`: Obtiene historias destacadas

### 10. Características Especiales

#### Sistema de Historias
- Expiración automática (24 horas)
- Soporte para imágenes y videos
- Indicador visual de video
- Eliminación individual
- Visor de historias tipo Instagram/TikTok
- Reintentos automáticos para verificar guardado

#### Animaciones y Efectos
- Gradientes multicolor en bordes de historias
- Efectos de presión en historias
- Elevación en botones
- Transiciones suaves

#### Responsive Design
- LazyColumn para scroll vertical
- LazyRow para scroll horizontal
- Padding adaptativo
- Tamaños fijos para consistencia

## Flujo de Usuario Típico

1. Usuario entra a ProfileScreen
2. Se cargan datos (perfil, historias, medios)
3. Usuario ve su perfil con todas las secciones
4. Puede:
   - Ver sus historias (click en foto de perfil)
   - Agregar nueva historia (botón +)
   - Editar perfil (botón Editar Perfil)
   - Cambiar foto de perfil/portada
   - Agregar fotos a galería
   - Eliminar historias
   - Compartir perfil
   - Abrir redes sociales

## Notas Importantes

- El archivo tiene 3139 líneas (muy extenso)
- Usa Jetpack Compose para UI
- Estilo Pop Art con colores vibrantes
- Manejo robusto de errores con try-catch
- Logs detallados para debugging
- Soporte completo para usuarios anónimos
- Sistema de reintentos para operaciones críticas
