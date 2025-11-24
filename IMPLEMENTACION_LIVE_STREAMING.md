# 🎥 Implementación de Live Streaming con Agora

## ✅ Archivos Creados

1. **LiveViewModel.kt** - Maneja el estado y la lógica de negocio del Live
2. **LiveLauncherScreen.kt** - Pantalla de inicio del Live con estados de carga/error
3. **LiveSession.kt** - Modelo de datos para sesiones de Live
4. **LiveRecordingScreen.kt** - Pantalla de transmisión (stub, requiere integración con Agora)
5. **FirebaseManager.kt** - Funciones agregadas para gestionar sesiones de Live

## 📋 Funciones Agregadas a FirebaseManager

### Funciones de Live Streaming

```kotlin
// Iniciar nueva sesión de Live
suspend fun startNewLiveSession(
    userId: String,
    username: String,
    profileImageUrl: String,
    title: String
): LiveSession?

// Finalizar sesión de Live
suspend fun endLiveSession(sessionId: String)

// Obtener sesiones activas
suspend fun getActiveLiveSessions(): List<LiveSession>

// Incrementar/decrementar viewers
suspend fun incrementLiveViewers(sessionId: String)
suspend fun decrementLiveViewers(sessionId: String)
```

## 🔄 Flujo de Inicio de Live

### Para el Streamer (Emisor)

1. Usuario hace clic en "Ir Live"
2. Se muestra `LiveLauncherScreen`
3. `LiveViewModel.startLiveSetup()` se ejecuta automáticamente
4. `FirebaseManager.startNewLiveSession()` crea la sesión en Firestore
5. **TODO**: Cloud Function genera token de Agora (actualmente usa token temporal)
6. Estado cambia a `LiveState.SessionReady`
7. Se muestra `LiveRecordingScreen` con los datos de la sesión
8. **TODO**: Integrar Agora SDK para transmitir

### Estados del ViewModel

- **Idle**: Estado inicial
- **Loading**: Obteniendo token de Agora
- **Error**: Falló la creación de la sesión
- **SessionReady**: Sesión creada, listo para transmitir

## 🚀 Cómo Usar

### Iniciar Live desde tu UI

```kotlin
// En tu composable principal (ej: LiveScreenNew.kt)
var showLiveLauncher by remember { mutableStateOf(false) }

if (showLiveLauncher) {
    LiveLauncherScreen(
        onClose = { showLiveLauncher = false }
    )
}

// Botón para iniciar Live
Button(onClick = { showLiveLauncher = true }) {
    Text("Ir Live")
}
```

## ✅ Implementación Completa de Agora

### 1. Dependencias Agregadas

- ✅ Agora SDK 4.2.6 agregado a `build.gradle.kts`
- ✅ Permisos de cámara, audio y red agregados al `AndroidManifest.xml`

### 2. Archivos Creados

- ✅ `AgoraConfig.kt` - Configuración de Agora
- ✅ `LiveRecordingScreen.kt` - Integración completa con Agora SDK
- ✅ `CLOUD_FUNCTION_AGORA_TOKEN.md` - Instrucciones para Cloud Function

### 3. Cloud Function para Token de Agora

Ver archivo `CLOUD_FUNCTION_AGORA_TOKEN.md` para instrucciones completas.

Resumen rápido:

```javascript
// Cloud Function (Firebase Functions)
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

exports.generateAgoraToken = functions.https.onCall((data, context) => {
  const appId = 'TU_APP_ID';
  const appCertificate = 'f907826ae8ff4c00b7057d15b6f2e628';
  const channelName = data.channelName;
  const uid = 0; // 0 para cualquier usuario
  const role = RtcRole.PUBLISHER; // PUBLISHER para streamer
  const expirationTimeInSeconds = 3600; // 1 hora
  
  const currentTimestamp = Math.floor(Date.now() / 1000);
  const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;
  
  const token = RtcTokenBuilder.buildTokenWithUid(
    appId,
    appCertificate,
    channelName,
    uid,
    role,
    privilegeExpiredTs
  );
  
  return { token };
});
```

### 2. Integración de Agora SDK en LiveRecordingScreen

```kotlin
// En LiveRecordingScreen.kt
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.ChannelMediaOptions

// Inicializar Agora
val agoraEngine = RtcEngine.create(
    context,
    "TU_APP_ID",
    object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            // Canal unido exitosamente
        }
    }
)

// Configurar para transmitir
agoraEngine.enableVideo()
agoraEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
agoraEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

// Unirse al canal
val options = ChannelMediaOptions()
options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

agoraEngine.joinChannel(agoraToken, channelName, 0, options)
```

### 3. Dependencias de Agora

Agregar al `build.gradle`:

```gradle
dependencies {
    // Agora SDK
    implementation 'io.agora.rtc:full-sdk:4.2.0'
}
```

### 4. Permisos en AndroidManifest.xml

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

## 📊 Estructura de Datos en Firestore

### Colección: `live_sessions`

```json
{
  "sessionId": "uuid-123",
  "userId": "user_123",
  "username": "StreamerDemo",
  "profileImageUrl": "https://...",
  "title": "Mi Live",
  "agoraChannelName": "live_uuid-123",
  "startTime": 1234567890,
  "endTime": 1234567890,
  "isActive": true,
  "viewerCount": 42,
  "createdAt": Timestamp,
  "updatedAt": Timestamp
}
```

## 🎯 Próximos Pasos

1. ✅ Estructura básica implementada
2. ⏳ Implementar Cloud Function para tokens de Agora
3. ⏳ Integrar Agora SDK en LiveRecordingScreen
4. ⏳ Crear pantalla de visualización para viewers
5. ⏳ Implementar chat en vivo
6. ⏳ Agregar efectos y filtros
7. ⏳ Implementar sistema de regalos/donaciones

## 📝 Notas

- El token temporal actual NO funcionará con Agora real
- Necesitas configurar Firebase Cloud Functions
- El App Certificate debe mantenerse secreto (solo en backend)
- Los viewers necesitarán su propio token con rol AUDIENCE
