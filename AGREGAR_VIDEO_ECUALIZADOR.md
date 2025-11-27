# 📹 Cómo Agregar Video de Referencia en el Ecualizador

## ✅ Cambios Realizados

He modificado `MyMusicScreen.kt` para que puedas usar un video personalizado en lugar del ecualizador animado.

## 📋 Pasos para Agregar tu Video

### 1. Preparar la Carpeta de Recursos

Crea la carpeta `raw` si no existe:
```
app/src/main/res/raw/
```

**Estructura completa:**
```
app/
└── src/
    └── main/
        └── res/
            └── raw/          ← Crear esta carpeta
                └── ecualizador_video.mp4  ← Tu video aquí
```

### 2. Copiar tu Video

1. **Copia tu video** desde tu PC a la carpeta `app/src/main/res/raw/`

2. **Renombra el archivo** siguiendo estas reglas:
   - Solo minúsculas
   - Sin espacios (usa guión bajo `_`)
   - Sin caracteres especiales
   - Extensión: `.mp4`, `.webm`, o `.mkv`

**Ejemplos válidos:**
- ✅ `ecualizador_video.mp4`
- ✅ `music_visualizer.mp4`
- ✅ `wave_animation.mp4`

**Ejemplos NO válidos:**
- ❌ `Ecualizador Video.mp4` (mayúsculas y espacios)
- ❌ `video-ecualizador.mp4` (guión medio)
- ❌ `ecualizador@2024.mp4` (caracteres especiales)

### 3. Actualizar el Código (si usas otro nombre)

Si tu video NO se llama `ecualizador_video.mp4`, actualiza esta línea en `MyMusicScreen.kt`:

```kotlin
@Composable
fun VideoEqualizerBackground(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    videoResId: Int = R.raw.ecualizador_video  // ← Cambia esto
) {
```

Por ejemplo, si tu video se llama `mi_video.mp4`:
```kotlin
videoResId: Int = R.raw.mi_video
```

### 4. Compilar y Probar

1. **Limpia el proyecto:**
   ```
   Build > Clean Project
   ```

2. **Reconstruye:**
   ```
   Build > Rebuild Project
   ```

3. **Ejecuta la app** y reproduce una canción en "Tu Música"

## 🎨 Características del Video

El video se reproduce con estas características:

- ✅ **Loop infinito**: Se repite automáticamente
- ✅ **Sin audio**: Solo visual (no interfiere con la música)
- ✅ **Sincronizado**: Se reproduce solo cuando la música está activa
- ✅ **Zoom automático**: Se ajusta para llenar el espacio
- ✅ **Sin controles**: Interfaz limpia

## 🎯 Cómo Funciona

1. Usuario reproduce una canción en "Tu Música"
2. Aparece la barra de reproducción expandida
3. El video se reproduce en el recuadro del ecualizador
4. Cuando pausas la música, el video también se pausa
5. Cuando reanudas, el video continúa

## 🔧 Personalización Adicional

### Cambiar el Tamaño del Video

En `EnhancedMusicPlayerBar`, modifica la altura:

```kotlin
Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)  // ← Cambia esto (ej: 80.dp, 100.dp)
        .clip(RoundedCornerShape(8.dp))
```

### Ajustar el Overlay (Oscurecer/Aclarar)

Modifica el alpha del overlay:

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(PopArtColors.Black.copy(alpha = 0.1f))  // ← 0.0 = transparente, 1.0 = negro
)
```

### Cambiar el Modo de Ajuste del Video

En `VideoEqualizerBackground`, cambia el `resizeMode`:

```kotlin
PlayerView(ctx).apply {
    player = videoPlayer
    useController = false
    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM  // Opciones:
    // RESIZE_MODE_FIT - Ajustar sin recortar
    // RESIZE_MODE_ZOOM - Llenar recortando
    // RESIZE_MODE_FILL - Estirar para llenar
}
```

## 📱 Recomendaciones para el Video

Para mejor rendimiento:

- **Resolución**: 720p o menos (1280x720)
- **Duración**: 5-15 segundos (se repetirá en loop)
- **Formato**: MP4 (H.264)
- **Tamaño**: Menos de 5MB
- **FPS**: 30fps

## 🐛 Solución de Problemas

### El video no aparece

1. Verifica que el archivo esté en `app/src/main/res/raw/`
2. Verifica que el nombre sea correcto (minúsculas, sin espacios)
3. Limpia y reconstruye el proyecto
4. Verifica que el nombre en el código coincida con el archivo

### El video se ve pixelado

- Usa un video de mayor resolución
- Cambia `resizeMode` a `RESIZE_MODE_FIT`

### El video consume mucha batería

- Reduce la resolución del video
- Reduce los FPS a 24fps
- Comprime el video con menor bitrate

## 🎬 Alternativa: Mantener el Ecualizador Animado

Si prefieres volver al ecualizador animado, reemplaza en `EnhancedMusicPlayerBar`:

```kotlin
// En lugar de VideoEqualizerBackground, usa:
SpotifyStyleEqualizer(
    isPlaying = isPlaying,
    barCount = 50,
    color = PopArtColors.Black,
    maxHeight = 44f
)
```

## 📝 Resumen

1. ✅ Código actualizado en `MyMusicScreen.kt`
2. ✅ Componente `VideoEqualizerBackground` creado
3. ✅ Integrado en `EnhancedMusicPlayerBar`
4. 📹 **Falta**: Copiar tu video a `app/src/main/res/raw/ecualizador_video.mp4`
5. 🔨 **Siguiente**: Compilar y probar

¡Listo para usar tu video personalizado! 🎉
