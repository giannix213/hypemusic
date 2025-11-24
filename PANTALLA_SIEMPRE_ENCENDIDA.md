# 🔆 Pantalla Siempre Encendida Durante Grabación

## ✨ Mejora Implementada

La pantalla ahora **permanece encendida** mientras:
- 📹 Estás grabando un video
- 👀 Estás revisando el video en preview

## 🔧 Implementación Técnica

### 1. Permiso en AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />
```
✅ Ya estaba agregado

### 2. CameraRecordingScreen
```kotlin
// Mantener la pantalla encendida durante la grabación
DisposableEffect(isRecording) {
    val window = (context as? android.app.Activity)?.window
    if (isRecording) {
        window?.addFlags(FLAG_KEEP_SCREEN_ON)
        Log.d("CameraScreen", "🔆 Pantalla mantenida encendida")
    } else {
        window?.clearFlags(FLAG_KEEP_SCREEN_ON)
        Log.d("CameraScreen", "🌙 Pantalla puede apagarse")
    }
    
    onDispose {
        window?.clearFlags(FLAG_KEEP_SCREEN_ON)
    }
}
```

**Comportamiento:**
- ✅ Se activa automáticamente al iniciar grabación
- ✅ Se desactiva automáticamente al detener grabación
- ✅ Se limpia al salir de la pantalla

### 3. VideoPreviewScreen
```kotlin
// Mantener la pantalla encendida mientras se revisa el video
DisposableEffect(Unit) {
    val window = (context as? android.app.Activity)?.window
    window?.addFlags(FLAG_KEEP_SCREEN_ON)
    
    onDispose {
        window?.clearFlags(FLAG_KEEP_SCREEN_ON)
    }
}
```

**Comportamiento:**
- ✅ Se activa al entrar a la pantalla de preview
- ✅ Se desactiva al salir (subir, volver a grabar, o cancelar)

## 🎯 Ventajas

### Para el Usuario:
- 📹 **No se interrumpe la grabación** por pantalla apagada
- 👀 **Puede revisar el video completo** sin tocar la pantalla
- 🎬 **Mejor experiencia** al grabar videos largos (hasta 60 segundos)

### Para el Sistema:
- 🔋 **Eficiente:** Solo se activa cuando es necesario
- 🧹 **Limpio:** Se desactiva automáticamente al salir
- 🛡️ **Seguro:** Usa el flag oficial de Android

## 📱 Flujo Completo

```
Usuario abre cámara
    ↓
Pantalla normal (puede apagarse)
    ↓
Usuario presiona GRABAR
    ↓
🔆 Pantalla se mantiene encendida
    ↓
Grabando... (0-60 segundos)
    ↓
Usuario detiene grabación
    ↓
🌙 Pantalla puede apagarse
    ↓
Abre preview del video
    ↓
🔆 Pantalla se mantiene encendida
    ↓
Usuario revisa el video
    ↓
Usuario presiona SUBIR o GRABAR DE NUEVO
    ↓
🌙 Pantalla vuelve a comportamiento normal
```

## 🐛 Debugging

Los logs te ayudan a verificar el comportamiento:

```
CameraScreen: 🔆 Pantalla mantenida encendida
CameraScreen: 🌙 Pantalla puede apagarse
VideoPreview: 🔆 Pantalla mantenida encendida durante preview
VideoPreview: 🌙 Pantalla puede apagarse
```

## 🔋 Impacto en Batería

**Mínimo:**
- Solo se activa durante grabación/preview (máximo 60 segundos)
- Se desactiva automáticamente al terminar
- No afecta otras partes de la app

## ✅ Archivos Modificados

1. **CameraScreen.kt** - Mantiene pantalla encendida durante grabación
2. **VideoPreviewScreen.kt** - Mantiene pantalla encendida durante preview
3. **AndroidManifest.xml** - Ya tenía el permiso WAKE_LOCK

## 🎉 Resultado

**¡Ahora puedes grabar videos sin preocuparte de que la pantalla se apague!** 📹✨

La pantalla permanecerá encendida automáticamente mientras grabas o revisas tus videos.
