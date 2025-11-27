# 🔍 Debug: Cámara No Se Abre en Live Recording

## ✅ Cambios Implementados

### 1. Solicitud de Permisos en Runtime
Agregué verificación y solicitud de permisos de cámara y micrófono antes de mostrar el Fragment de Zego.

```kotlin
// Verificar permisos al inicio
LaunchedEffect(Unit) {
    val cameraGranted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    
    val audioGranted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    
    hasCameraPermission = cameraGranted
    hasAudioPermission = audioGranted
}
```

### 2. Pantalla de Solicitud de Permisos
Si los permisos no están otorgados, se muestra una pantalla para solicitarlos:

```kotlin
if (!hasCameraPermission || !hasAudioPermission) {
    // Mostrar UI para solicitar permisos
    Button(onClick = {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
        )
    })
}
```

### 3. Logs Detallados para Debugging
Agregué logs extensivos para identificar problemas:

```kotlin
android.util.Log.d("LiveRecordingScreen", "🎬 INICIALIZANDO ZEGOCLOUD UIKIT")
android.util.Log.d("LiveRecordingScreen", "   APP_ID: ${ZegoConfig.APP_ID}")
android.util.Log.d("LiveRecordingScreen", "   Usuario: $username")
android.util.Log.d("LiveRecordingScreen", "   Canal: $channelName")
```

## 🔍 Cómo Debuggear

### Paso 1: Verificar Logs en Logcat
Abre Android Studio → Logcat y filtra por `LiveRecordingScreen`:

```
adb logcat | grep LiveRecordingScreen
```

Busca estos mensajes:

#### ✅ Inicialización Correcta:
```
🎬 INICIALIZANDO ZEGOCLOUD UIKIT
✅ Contexto es FragmentActivity
✅ Configuración HOST creada
✅ Fragment creado exitosamente
✅ Fragment agregado exitosamente
✅ INICIALIZACIÓN COMPLETA
```

#### ❌ Problemas Comunes:

**Problema 1: Permisos Denegados**
```
📹 Permisos verificados:
   Cámara: ❌ Denegado
   Audio: ❌ Denegado
```
**Solución**: La app mostrará una pantalla para solicitar permisos. Presiona "Otorgar Permisos".

**Problema 2: Contexto Incorrecto**
```
❌ ERROR: El contexto NO es FragmentActivity
```
**Solución**: Verificar que MainActivity extiende de FragmentActivity o ComponentActivity.

**Problema 3: Error Creando Fragment**
```
❌ ERROR creando Fragment: [mensaje de error]
```
**Solución**: Verificar credenciales de ZegoCloud en `ZegoConfig.kt`.

**Problema 4: Error Agregando Fragment**
```
❌ ERROR agregando Fragment: [mensaje de error]
```
**Solución**: Verificar que el FragmentManager está disponible.

### Paso 2: Verificar Permisos Manualmente
En el dispositivo/emulador:
1. Configuración → Apps → HypeMatch → Permisos
2. Verificar que Cámara y Micrófono estén activados

### Paso 3: Verificar Credenciales de ZegoCloud
Abre `app/src/main/java/com/metu/hypematch/ZegoConfig.kt`:

```kotlin
object ZegoConfig {
    const val APP_ID: Long = 124859353L
    const val APP_SIGN: String = "e5b1c6be49eed6bb441ae12dc4ba8bb2c488854870cb0f8e2d8ce28a5a06a8de"
}
```

Verifica que:
- ✅ APP_ID es correcto (número de 9 dígitos)
- ✅ APP_SIGN es correcto (string hexadecimal de 64 caracteres)

### Paso 4: Probar en Dispositivo Real
Si estás usando un emulador:
1. Verifica que el emulador tenga cámara virtual habilitada
2. Configuración del emulador → Camera → Webcam0 o Emulated

Si el problema persiste, prueba en un dispositivo físico.

## 🔧 Posibles Causas

### 1. Permisos No Otorgados
**Síntoma**: La app muestra pantalla de permisos
**Solución**: Otorgar permisos de cámara y micrófono

### 2. Credenciales de ZegoCloud Inválidas
**Síntoma**: Fragment se crea pero no muestra nada
**Solución**: Verificar APP_ID y APP_SIGN en ZegoConfig.kt

### 3. Emulador Sin Cámara
**Síntoma**: Permisos otorgados pero no se ve nada
**Solución**: Usar dispositivo físico o configurar cámara virtual en emulador

### 4. Conflicto con Otras Pantallas
**Síntoma**: La cámara se abre pero se cierra inmediatamente
**Solución**: Ya resuelto - todas las pantallas están dentro del `when` statement

### 5. Fragment No Se Adjunta Correctamente
**Síntoma**: Logs muestran "Fragment creado" pero no "Fragment agregado"
**Solución**: Verificar que MainActivity es FragmentActivity

## 📋 Checklist de Verificación

- [ ] Permisos de cámara otorgados
- [ ] Permisos de micrófono otorgados
- [ ] Credenciales de ZegoCloud correctas
- [ ] MainActivity extiende de ComponentActivity
- [ ] Logs muestran "INICIALIZACIÓN COMPLETA"
- [ ] No hay errores en Logcat
- [ ] Dispositivo/emulador tiene cámara disponible

## 🚀 Próximos Pasos

Si después de verificar todo lo anterior la cámara aún no se abre:

1. **Captura los logs completos**:
   ```bash
   adb logcat > logs.txt
   ```

2. **Busca errores de ZegoCloud**:
   ```bash
   adb logcat | grep -i "zego"
   ```

3. **Verifica la versión del SDK**:
   - Abre `app/build.gradle.kts`
   - Busca la dependencia de ZegoCloud
   - Verifica que sea la versión más reciente

4. **Prueba con configuración mínima**:
   - Crea un Fragment simple con solo la cámara
   - Si funciona, el problema es de configuración
   - Si no funciona, el problema es del SDK o permisos

## 📚 Referencias

- [ZegoCloud Documentation](https://docs.zegocloud.com/)
- [Android Camera Permissions](https://developer.android.com/training/permissions/requesting)
- [Fragment Transactions](https://developer.android.com/guide/fragments/transactions)
