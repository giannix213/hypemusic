# 🎨 Sistema de Portada Personalizable Implementado

## ✅ Nueva Funcionalidad

Se ha agregado la capacidad de personalizar la portada de las canciones con las siguientes opciones:

### 1. Icono de Edición
- **Ubicación**: Al costado derecho de la portada generada
- **Diseño**: Botón circular amarillo con icono `ic_edit` negro
- **Tamaño**: 56dp
- **Función**: Abre un diálogo con opciones de personalización

### 2. Opciones de Personalización

#### Opción 1: Cambiar Foto
- Permite seleccionar una foto personalizada desde la galería
- La foto se integra con la plantilla `user_plantilla`
- Mantiene el formato circular y el nombre del usuario arriba
- Borde morado para indicar que es personalizada

#### Opción 2: Agregar Video
- Permite seleccionar un video como portada animada
- El video se sube a Firebase Storage
- Muestra un preview con icono de play
- Borde morado para indicar que es un video
- Texto "VIDEO" en la parte inferior

#### Opción 3: Restaurar Predeterminada
- Solo aparece si hay una personalización activa
- Restaura la portada generada automáticamente con la foto de perfil
- Vuelve al borde amarillo

### 3. Indicadores Visuales

**Estados de la portada:**
- **Predeterminada**: Borde amarillo + texto "Se genera automáticamente con tu foto de perfil"
- **Foto personalizada**: Borde morado + texto "Foto personalizada aplicada"
- **Video**: Borde morado + icono de play + texto "Video personalizado seleccionado"

### 4. Flujo de Trabajo

1. Usuario abre pantalla de subir música
2. Ve la portada generada automáticamente
3. Puede hacer clic en el icono de edición (amarillo)
4. Se abre diálogo con 3 opciones:
   - Cambiar Foto (botón morado)
   - Agregar Video (botón cyan)
   - Restaurar Predeterminada (botón blanco, solo si hay personalización)
5. Al seleccionar foto: se regenera la portada con la nueva foto
6. Al seleccionar video: se muestra preview del video
7. Al publicar: se sube la portada personalizada o el video

## 🎯 Implementación Técnica

### Estados Agregados
```kotlin
var customCoverUri by remember { mutableStateOf<Uri?>(null) }
var customVideoUri by remember { mutableStateOf<Uri?>(null) }
var showCoverOptionsDialog by remember { mutableStateOf(false) }
```

### Launchers Agregados
```kotlin
// Selector de imagen personalizada
val customImagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    customCoverUri = uri
    // Regenera portada con foto personalizada
}

// Selector de video
val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    customVideoUri = uri
}
```

### Nueva Función
```kotlin
suspend fun generateCoverImageWithCustomPhoto(
    context: android.content.Context,
    customPhoto: Bitmap,
    username: String
): Bitmap
```
- Genera portada usando la plantilla + foto personalizada + nombre

### Función en FirebaseManager
```kotlin
suspend fun uploadVideoFile(uri: Uri, onProgress: (Int) -> Unit): String
```
- Sube videos a Firebase Storage en la carpeta `videos/`
- Monitorea el progreso de subida
- Retorna la URL de descarga

### Modelo de Datos Actualizado
```kotlin
data class UploadSongData(
    // ... campos existentes
    val videoUrl: String = "", // Nuevo campo para video de portada
)
```

## 🎨 Diseño del Diálogo

### Título
- "Personalizar Portada" en negro y negrita

### Botones
1. **Cambiar Foto**
   - Color: Morado
   - Icono: `ic_image`
   - Texto: Blanco

2. **Agregar Video**
   - Color: Cyan
   - Icono: `ic_play`
   - Texto: Negro

3. **Restaurar Predeterminada** (condicional)
   - Color: Blanco
   - Texto: Negro
   - Solo visible si hay personalización activa

### Botón Cerrar
- TextButton en la parte inferior
- Texto: "Cerrar" en negro

## 📱 Experiencia de Usuario

### Ventajas
1. **Flexibilidad**: El usuario puede elegir entre 3 opciones
2. **Visual**: Indicadores claros del estado de la portada
3. **Reversible**: Puede restaurar la portada predeterminada en cualquier momento
4. **Innovador**: Soporte para videos como portada (único en la industria)
5. **Intuitivo**: Icono de edición claramente visible

### Casos de Uso
- **Artista con branding**: Puede usar su logo o imagen promocional
- **Lanzamiento especial**: Puede usar un video teaser como portada
- **Simplicidad**: Puede mantener la portada automática si prefiere

## 🔄 Lógica de Subida

### Con Video
1. Sube el video a Firebase Storage (33% del progreso)
2. Sube el audio (67% restante)
3. Guarda metadata con `videoUrl` lleno e `imageUrl` vacío

### Con Foto Personalizada
1. Genera bitmap con la foto personalizada
2. Convierte a archivo temporal
3. Sube a Firebase Storage (50% del progreso)
4. Sube el audio (50% restante)
5. Guarda metadata con `imageUrl` lleno y `videoUrl` vacío

### Predeterminada
1. Genera bitmap con foto de perfil
2. Sube normalmente como antes

## ✨ Resultado Final

Los usuarios ahora tienen control total sobre la portada de sus canciones:
- Pueden usar la portada automática (rápido y fácil)
- Pueden personalizar con su propia foto (branding)
- Pueden agregar un video (innovador y llamativo)
- Pueden cambiar de opinión en cualquier momento

El icono de edición amarillo es claramente visible y accesible, haciendo que la funcionalidad sea descubrible sin ser intrusiva.
