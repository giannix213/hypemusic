# 🎨 Guía para Agregar Íconos Personalizados en HypeMatch

## 📁 Ubicación de los Íconos

Los íconos personalizados se agregan en la carpeta:
```
app/src/main/res/drawable/
```

**Ruta completa:** `HypeMatch/app/src/main/res/drawable/`

---

## 📋 Tipos de Íconos que Puedes Agregar

### 1. **Íconos PNG** (Imágenes)
- Formatos: `.png`, `.jpg`, `.webp`
- Recomendado: PNG con transparencia
- Tamaños recomendados:
  - **24dp**: 72px × 72px (3x)
  - **48dp**: 144px × 144px (3x)
  - **96dp**: 288px × 288px (3x)

### 2. **Íconos Vectoriales** (XML)
- Formato: `.xml` (Vector Drawable)
- Ventajas: Escalables, menor tamaño
- Ideal para íconos simples

---

## 🎯 Cómo Agregar Íconos

### Opción 1: Agregar PNG Directamente

1. **Coloca tu archivo PNG** en:
   ```
   app/src/main/res/drawable/
   ```

2. **Nombre del archivo:**
   - Usa solo minúsculas
   - Usa guiones bajos `_` (no espacios ni guiones `-`)
   - Ejemplos válidos:
     - ✅ `ic_pause.png`
     - ✅ `icon_play_circle.png`
     - ✅ `btn_stop.png`
     - ❌ `Icon-Pause.png` (mayúsculas)
     - ❌ `play icon.png` (espacios)

### Opción 2: Crear Vector Drawable (XML)

Crea un archivo XML en `app/src/main/res/drawable/`:

**Ejemplo: `ic_pause_custom.xml`**
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M6,4h4v16H6V4zm8,0h4v16h-4V4z"/>
</vector>
```

---

## 💻 Cómo Usar los Íconos en Kotlin/Compose

### 1. Usar PNG con `painterResource`

```kotlin
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image

// En tu Composable:
Image(
    painter = painterResource(id = R.drawable.ic_pause),
    contentDescription = "Pausar",
    modifier = Modifier.size(32.dp)
)

// O con Icon:
Icon(
    painter = painterResource(id = R.drawable.ic_pause),
    contentDescription = "Pausar",
    tint = colors.primary,
    modifier = Modifier.size(32.dp)
)
```

### 2. Usar Vector Drawable (XML)

```kotlin
Icon(
    painter = painterResource(id = R.drawable.ic_pause_custom),
    contentDescription = "Pausar",
    tint = colors.primary,
    modifier = Modifier.size(32.dp)
)
```

---

## 🎨 Íconos Recomendados para HypeMatch

### Íconos de Reproducción
- `ic_play.png` - Botón play
- `ic_pause.png` - Botón pausa ⏸️
- `ic_stop.png` - Botón stop ⏹️
- `ic_next.png` - Siguiente canción ⏭️
- `ic_previous.png` - Canción anterior ⏮️

### Íconos de Música
- `ic_music_note.png` - Nota musical 🎵
- `ic_headphones.png` - Audífonos 🎧
- `ic_microphone.png` - Micrófono 🎤
- `ic_guitar.png` - Guitarra 🎸

### Íconos de Redes Sociales
- `ic_instagram.png`
- `ic_youtube.png`
- `ic_tiktok.png`
- `ic_spotify.png`

---

## 📦 Ejemplo Completo: Agregar Ícono de Pausa

### Paso 1: Crear el archivo XML

**Archivo:** `app/src/main/res/drawable/ic_pause_circle.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#FF000000"
        android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10s10,-4.48 10,-10S17.52,2 12,2zM11,16H9V8h2V16zM15,16h-2V8h2V16z"/>
</vector>
```

### Paso 2: Usar en tu código

```kotlin
// En MyMusicScreen o cualquier Composable:
IconButton(
    onClick = { /* acción */ }
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_pause_circle),
        contentDescription = "Pausar",
        tint = colors.primary,
        modifier = Modifier.size(32.dp)
    )
}
```

---

## 🔧 Solución para el Problema Actual

Para tu caso específico en `MyMusicScreen`, puedes:

### Opción A: Usar Material Icons Extendidos

Agrega esta dependencia en `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
}
```

Luego usa:
```kotlin
import androidx.compose.material.icons.filled.PauseCircle

Icon(
    imageVector = Icons.Filled.PauseCircle,
    contentDescription = "Pausar"
)
```

### Opción B: Crear tu propio ícono de pausa

1. Crea `app/src/main/res/drawable/ic_pause_filled.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M6,19h4V5H6V19zM14,5v14h4V5H14z"/>
</vector>
```

2. Úsalo en tu código:

```kotlin
Icon(
    painter = painterResource(id = R.drawable.ic_pause_filled),
    contentDescription = "Pausar",
    tint = colors.primary,
    modifier = Modifier.size(32.dp)
)
```

---

## 📝 Reglas Importantes

1. ✅ **Nombres válidos:**
   - Solo minúsculas
   - Solo letras, números y guiones bajos
   - Debe empezar con letra
   - Ejemplos: `ic_play`, `icon_music`, `btn_pause`

2. ❌ **Nombres inválidos:**
   - `Icon-Play.png` (mayúsculas y guión)
   - `play icon.png` (espacios)
   - `1_icon.png` (empieza con número)

3. 🎨 **Tamaños recomendados:**
   - Íconos pequeños: 24dp (72px)
   - Íconos medianos: 48dp (144px)
   - Íconos grandes: 96dp (288px)

4. 🔄 **Después de agregar íconos:**
   - Haz "Sync Project with Gradle Files"
   - O simplemente reconstruye el proyecto

---

## 🚀 Recursos para Descargar Íconos

- **Material Icons:** https://fonts.google.com/icons
- **Flaticon:** https://www.flaticon.com/
- **Icons8:** https://icons8.com/
- **Iconify:** https://icon-sets.iconify.design/

---

## ✨ Ejemplo de Uso en HypeMatch

```kotlin
// En ScreenStubs.kt - MyMusicScreen
IconButton(
    onClick = { 
        if (isCurrentlyPlaying) {
            if (isPlaying) {
                player.pause()
                isPlaying = false
            } else {
                player.play()
                isPlaying = true
            }
        } else {
            // Reproducir canción
        }
    }
) {
    Icon(
        painter = painterResource(
            id = if (isCurrentlyPlaying && isPlaying) 
                R.drawable.ic_pause_filled 
            else 
                R.drawable.ic_play_filled
        ),
        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
        tint = colors.primary,
        modifier = Modifier.size(32.dp)
    )
}
```

---

¡Listo! Ahora puedes agregar y usar íconos personalizados en tu app HypeMatch. 🎵
