# 🎯 Siguiente Paso: Activar Live Streaming

## ✅ Lo que YA está hecho

- ✅ App ID de Agora configurado: `72117baf2c874766b556e6f83ac9c58d`
- ✅ Agora SDK integrado en la app
- ✅ LiveRecordingScreen con funcionalidad completa
- ✅ Permisos configurados
- ✅ UI de controles implementada
- ✅ Código de Cloud Function listo en `functions_index.js`

## 🚀 Lo que DEBES hacer AHORA

### Opción 1: Desplegar Cloud Function (Recomendado - 10 minutos)

Sigue la guía: **`DESPLEGAR_CLOUD_FUNCTION.md`**

Pasos resumidos:
```bash
# 1. Instalar Firebase CLI
npm install -g firebase-tools

# 2. Login
firebase login

# 3. Inicializar
firebase init functions

# 4. Instalar Agora
cd functions
npm install agora-access-token

# 5. Copiar código
# Copia el contenido de functions_index.js a functions/index.js

# 6. Desplegar
firebase deploy --only functions
```

Luego actualiza `FirebaseManager.kt` (instrucciones en el archivo).

### Opción 2: Probar sin Cloud Function (Rápido - 2 minutos)

Si quieres probar AHORA sin desplegar la Cloud Function:

1. La app ya tiene un token temporal configurado
2. Solo funciona para pruebas locales
3. NO funcionará en producción

Para probar:
1. Abre la app
2. Ve a la sección Live
3. Haz clic en "Ir Live"
4. Concede permisos
5. Deberías ver tu cámara (aunque el token temporal puede fallar)

## 🎥 Cómo Probar Live Streaming

### 1. Conectar el botón de Live

En `LiveScreenNew.kt`, busca donde quieras agregar el botón y agrega:

```kotlin
var showLiveLauncher by remember { mutableStateOf(false) }

// Botón para iniciar Live
IconButton(
    onClick = { showLiveLauncher = true }
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_live),
        contentDescription = "Iniciar Live",
        tint = Color.White,
        modifier = Modifier.size(40.dp)
    )
}

// Al final del composable, antes del último }
if (showLiveLauncher) {
    LiveLauncherScreen(
        onClose = { showLiveLauncher = false }
    )
}
```

### 2. Ejecutar la app

```bash
# En Android Studio:
1. Sync Project with Gradle Files
2. Build > Rebuild Project
3. Run App
```

### 3. Probar la transmisión

1. Abre la app
2. Haz clic en el botón de Live
3. Concede permisos de cámara y audio
4. Espera a que cargue
5. Deberías ver:
   - Tu cámara en pantalla completa
   - Badge "LIVE" en rojo
   - Contador de espectadores
   - Botones de control (cambiar cámara, mutear)

## 📱 Ubicación del Botón de Live

Actualmente el botón de Live está en:
- `LiveScreenNew.kt` línea ~1200 (aproximadamente)
- Esquina superior izquierda del carrusel de videos

Ya está conectado, solo necesitas:
1. Desplegar la Cloud Function (o usar token temporal)
2. Ejecutar la app
3. Hacer clic en el ícono de Live

## 🐛 Si algo falla

### Error: "App ID is invalid"
- Ya está configurado correctamente ✅

### Error: "Failed to join channel"
- Necesitas desplegar la Cloud Function
- O el token temporal expiró

### Error: "Permission denied"
- Ve a Configuración > Apps > HypeMatch
- Concede permisos de cámara y audio

### No se ve la cámara
- Prueba en un dispositivo físico (no emulador)
- Verifica los logs en Logcat filtrando por "LiveRecording"

## 📊 Logs para Monitorear

Filtra en Logcat:
```
LiveRecording    # Logs de transmisión
LiveViewModel    # Logs de estados
LiveLauncher     # Logs de inicio
FirebaseManager  # Logs de Firebase
Agora           # Logs del SDK
```

## 🎯 Próximos Pasos Después de Probar

1. ✅ Probar transmisión básica
2. ⏳ Crear pantalla de viewers (LiveViewerScreen)
3. ⏳ Implementar chat en vivo
4. ⏳ Agregar efectos y filtros
5. ⏳ Sistema de regalos/donaciones

## 📞 Resumen

**Para probar AHORA (con token temporal):**
- Solo ejecuta la app y haz clic en el botón de Live

**Para producción (con tokens reales):**
- Sigue `DESPLEGAR_CLOUD_FUNCTION.md`
- Actualiza `FirebaseManager.kt`
- ¡Listo para transmitir! 🎉

---

## 🎬 ¡Todo está listo!

El código está completo y funcionando. Solo necesitas:
1. Desplegar la Cloud Function (10 min)
2. Actualizar FirebaseManager (2 min)
3. ¡Empezar a transmitir! 🚀
