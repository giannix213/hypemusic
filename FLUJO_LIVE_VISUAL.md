# 🎬 Flujo Visual del Live Streaming

## 📊 Diagrama del Flujo Completo

```
┌─────────────────────────────────────────────────────────────────┐
│                         USUARIO                                  │
│                    (Toca "Iniciar Live")                        │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  LiveLauncherScreen.kt                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ LaunchedEffect(Unit) {                                    │  │
│  │   viewModel.startLiveSetup()                             │  │
│  │ }                                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    LiveViewModel.kt                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ fun startLiveSetup() {                                    │  │
│  │   _liveState.value = LiveState.Loading                   │  │
│  │   val session = firebaseManager.startNewLiveSession(...)  │  │
│  │   _liveState.value = LiveState.SessionReady(session)     │  │
│  │ }                                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  FirebaseManager.kt                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ suspend fun startNewLiveSession() {                       │  │
│  │   1. Generar nombre de canal                             │  │
│  │      channelName = "live_userId_timestamp"               │  │
│  │                                                           │  │
│  │   2. Llamar Cloud Function                               │  │
│  │      val token = generateAgoraToken(channelName)         │  │
│  │                                                           │  │
│  │   3. Crear documento en Firestore                        │  │
│  │      firestore.collection("live_sessions").add(...)      │  │
│  │                                                           │  │
│  │   4. Retornar LiveSession                                │  │
│  │      return LiveSession(...)                             │  │
│  │ }                                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              Firebase Cloud Functions                           │
│              (functions/index.js)                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ exports.generateAgoraToken = (data) => {                 │  │
│  │   const token = RtcTokenBuilder.buildTokenWithUid(       │  │
│  │     APP_ID,                                              │  │
│  │     APP_CERTIFICATE,                                     │  │
│  │     channelName,                                         │  │
│  │     uid,                                                 │  │
│  │     role,                                                │  │
│  │     expirationTime                                       │  │
│  │   );                                                     │  │
│  │   return { token, expiresAt, channelName, uid };        │  │
│  │ }                                                        │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Agora RTC Service                            │
│              (Servicio externo de Agora)                        │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ ✅ Token validado                                         │  │
│  │ ✅ Canal creado                                           │  │
│  │ ✅ Listo para streaming                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                  LiveRecordingScreen.kt                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ 📹 Cámara activada                                        │  │
│  │ 🔴 Transmitiendo en vivo                                  │  │
│  │ 👥 Espectadores: 0                                        │  │
│  │ 🛑 Botón para finalizar                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Estados del Live

```
┌──────────┐
│   Idle   │ ← Estado inicial
└────┬─────┘
     │ Usuario toca "Iniciar Live"
     ▼
┌──────────┐
│ Loading  │ ← Obteniendo token de Agora
└────┬─────┘
     │
     ├─── ✅ Éxito
     │    ▼
     │  ┌──────────────┐
     │  │SessionReady  │ ← Token recibido, sesión creada
     │  └──────┬───────┘
     │         │
     │         ▼
     │  ┌──────────────┐
     │  │ Transmitiendo│ ← Usuario transmitiendo en vivo
     │  └──────┬───────┘
     │         │ Usuario finaliza
     │         ▼
     │  ┌──────────────┐
     │  │   Idle       │ ← Vuelve al inicio
     │  └──────────────┘
     │
     └─── ❌ Error
          ▼
       ┌──────────┐
       │  Error   │ ← Mostrar mensaje de error
       └────┬─────┘
            │ Usuario reintenta
            ▼
       ┌──────────┐
       │ Loading  │ ← Vuelve a intentar
       └──────────┘
```

## 📦 Estructura de Datos

### LiveSession (Kotlin)
```kotlin
data class LiveSession(
    val sessionId: String,           // "abc123xyz"
    val userId: String,              // "user_001"
    val username: String,            // "Juan Pérez"
    val profileImageUrl: String,     // "https://..."
    val title: String,               // "Mi Live"
    val agoraChannelName: String,    // "live_user001_1234567890"
    val agoraToken: String,          // "006abc..."
    val startTime: Long,             // 1234567890
    val isActive: Boolean,           // true
    val viewerCount: Int             // 0
)
```

### Documento en Firestore (live_sessions)
```javascript
{
  "userId": "user_001",
  "username": "Juan Pérez",
  "profileImageUrl": "https://...",
  "title": "Mi Live",
  "agoraChannelName": "live_user001_1234567890",
  "agoraToken": "006abc...",
  "startTime": 1234567890,
  "isActive": true,
  "viewerCount": 0
}
```

## 🎯 Puntos Críticos

### ✅ Punto 1: Cloud Functions Desplegadas
```
firebase functions:list
```
**Debe mostrar:**
- generateAgoraToken ✅
- generateStreamerToken ✅
- generateViewerToken ✅

### ✅ Punto 2: Credenciales de Agora Correctas
```javascript
// functions/index.js
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';
```

### ✅ Punto 3: Usuario Autenticado
```kotlin
val currentUserId = authManager.getUserId() ?: ""
// No debe estar vacío
```

### ✅ Punto 4: Conexión a Internet
```
Verificar que el dispositivo tenga internet
```

## 🔍 Logs de Debugging

### Logs Exitosos
```
D/FirebaseManager: 🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
D/FirebaseManager: 👤 Usuario: Juan Pérez (user_001)
D/FirebaseManager: 📝 Título: Mi Live en Hype Match
D/FirebaseManager: 📺 Canal generado: live_user001_1234567890
D/FirebaseManager: 🔑 Solicitando token de Agora...
D/FirebaseManager: ✅ Token de Agora recibido: 006abc...
D/FirebaseManager: 💾 Creando documento en Firestore...
D/FirebaseManager: ✅ Sesión creada en Firestore: abc123xyz
D/FirebaseManager: ✅ ===== SESIÓN DE LIVE LISTA =====
D/LiveViewModel: ✅ Sesión creada: abc123xyz
D/LiveViewModel: 📺 Canal Agora: live_user001_1234567890
D/LiveLauncher: ✅ Sesión lista, iniciando LiveRecordingScreen
```

### Logs de Error
```
D/FirebaseManager: 🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
D/FirebaseManager: 👤 Usuario: Juan Pérez (user_001)
D/FirebaseManager: 📺 Canal generado: live_user001_1234567890
D/FirebaseManager: 🔑 Solicitando token de Agora...
E/FirebaseManager: ❌ ===== ERROR INICIANDO LIVE =====
E/FirebaseManager: ❌ Mensaje: Cloud Function 'generateAgoraToken' not found
E/FirebaseManager: ❌ Tipo: FirebaseFunctionsException
```

## 🛠️ Herramientas de Diagnóstico

### 1. Verificar Cloud Functions
```bash
firebase functions:list
```

### 2. Ver logs de Cloud Functions
```bash
firebase functions:log
```

### 3. Ver logs de la app
```
Logcat > Filtrar por: FirebaseManager
```

### 4. Verificar Firestore
```
Firebase Console > Firestore Database > live_sessions
```

## 📱 Experiencia del Usuario

### Flujo Visual en la App

```
┌─────────────────────┐
│   Pestaña "Live"    │
│                     │
│   [Iniciar Live]    │ ← Usuario toca aquí
└─────────────────────┘
          │
          ▼
┌─────────────────────┐
│  Preparando Live... │
│        ⏳           │ ← 2-3 segundos
└─────────────────────┘
          │
          ▼
┌─────────────────────┐
│   📹 Transmitiendo  │
│                     │
│   [Tu cámara aquí]  │
│                     │
│   👥 0 espectadores │
│                     │
│   [🛑 Finalizar]    │ ← Usuario puede finalizar
└─────────────────────┘
```

## 🎉 Resultado Final

Cuando todo funciona correctamente:

1. ✅ Usuario toca "Iniciar Live"
2. ✅ Ve "Preparando Live..." por 2-3 segundos
3. ✅ La cámara se activa automáticamente
4. ✅ Puede ver su propia imagen
5. ✅ Está transmitiendo en vivo
6. ✅ Otros usuarios pueden unirse y verlo
7. ✅ Puede finalizar cuando quiera

---

**Este flujo garantiza una experiencia fluida y profesional para el usuario.**
