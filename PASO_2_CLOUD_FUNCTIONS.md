# ✅ Paso 2: Cloud Functions para Tokens de Agora

## 🎯 Estado Actual

Las Cloud Functions **YA ESTÁN IMPLEMENTADAS** en tu proyecto! 🎉

---

## 📋 Funciones Disponibles

### 1. `generateAgoraToken` (Función Principal)
Genera tokens de Agora para cualquier canal y rol.

**Parámetros:**
```javascript
{
  channelName: string,  // Nombre del canal
  role: string,         // 'publisher' o 'subscriber'
  uid: number          // ID del usuario (0 para auto-asignar)
}
```

**Retorna:**
```javascript
{
  token: string,        // Token de Agora
  expiresAt: number,    // Timestamp de expiración
  channelName: string,  // Nombre del canal
  uid: number          // UID asignado
}
```

**Duración del token:** 1 hora (3600 segundos)

---

### 2. `generateStreamerToken` (Para Broadcasters)
Función simplificada para streamers. Requiere autenticación.

**Parámetros:**
```javascript
{
  channelName: string  // Nombre del canal
}
```

**Características:**
- ✅ Verifica autenticación del usuario
- ✅ Genera UID automáticamente desde Firebase Auth
- ✅ Rol: PUBLISHER (broadcaster)

---

### 3. `generateViewerToken` (Para Espectadores)
Función simplificada para espectadores. Requiere autenticación.

**Parámetros:**
```javascript
{
  channelName: string  // Nombre del canal
}
```

**Características:**
- ✅ Verifica autenticación del usuario
- ✅ Genera UID automáticamente desde Firebase Auth
- ✅ Rol: SUBSCRIBER (viewer)

---

## 🔐 Configuración de Seguridad

### Credenciales de Agora (Ya configuradas)
```javascript
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';
```

⚠️ **IMPORTANTE**: Estas credenciales están **SOLO en el backend** (Cloud Functions), nunca en el cliente. ✅

---

## 🚀 Desplegar las Funciones

### Opción 1: Desplegar Todas las Funciones
```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

### Opción 2: Desplegar Solo una Función Específica
```bash
firebase deploy --only functions:generateAgoraToken
```

### Opción 3: Usar el Script Automatizado
```bash
.\verificar-y-desplegar-functions.bat
```

---

## 🧪 Probar las Funciones

### Desde Firebase Console
1. Ve a Firebase Console → Functions
2. Selecciona la función `generateAgoraToken`
3. Haz clic en "Probar función"
4. Ingresa los datos de prueba:
```json
{
  "channelName": "test_channel",
  "role": "publisher",
  "uid": 0
}
```

### Desde la App (Kotlin)
```kotlin
val functions = Firebase.functions
val data = hashMapOf(
    "channelName" to "live_channel_123",
    "role" to "publisher",
    "uid" to 0
)

functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .addOnSuccessListener { result ->
        val token = (result.data as? Map<*, *>)?.get("token") as? String
        android.util.Log.d("Agora", "✅ Token: $token")
    }
    .addOnFailureListener { e ->
        android.util.Log.e("Agora", "❌ Error: ${e.message}")
    }
```

---

## 📊 Verificar Estado de las Funciones

### Ver Funciones Desplegadas
```bash
firebase functions:list
```

### Ver Logs en Tiempo Real
```bash
firebase functions:log
```

### Ver Logs de una Función Específica
```bash
firebase functions:log --only generateAgoraToken
```

---

## 🔄 Integración con la App

Las funciones ya están siendo llamadas desde tu app en:

### 1. `LiveViewModel.kt`
```kotlin
// Genera token al iniciar Live
val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()
```

### 2. `FirebaseManager.kt`
```kotlin
// En startNewLiveSession()
val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()
```

---

## ✅ Checklist de Verificación

- [x] Cloud Functions configuradas en `functions/index.js`
- [x] Dependencias instaladas (`agora-access-token`)
- [x] Credenciales de Agora configuradas
- [x] Función `generateAgoraToken` implementada
- [x] Función `generateStreamerToken` implementada
- [x] Función `generateViewerToken` implementada
- [ ] Funciones desplegadas en Firebase (ejecutar deploy)
- [ ] Funciones probadas desde Firebase Console
- [ ] Funciones probadas desde la app

---

## 🎯 Próximos Pasos

### 1. Desplegar las Funciones
```bash
firebase deploy --only functions
```

### 2. Verificar el Despliegue
```bash
firebase functions:list
```

Deberías ver:
```
✔ generateAgoraToken(us-central1)
✔ generateStreamerToken(us-central1)
✔ generateViewerToken(us-central1)
```

### 3. Probar desde la App
- Iniciar un Live desde la app
- Verificar en los logs que se obtiene el token
- Verificar que la transmisión se conecta correctamente

---

## 🐛 Troubleshooting

### Error: "Function not found"
**Solución:** Desplegar las funciones
```bash
firebase deploy --only functions
```

### Error: "Invalid APP_CERTIFICATE"
**Solución:** Verificar que el App Certificate en `functions/index.js` sea correcto

### Error: "Token expired"
**Solución:** Los tokens duran 1 hora. Generar un nuevo token.

### Error: "Channel name required"
**Solución:** Asegurarse de pasar `channelName` en los parámetros

---

## 📝 Ejemplo Completo de Uso

### Streamer (Broadcaster)
```kotlin
// 1. Generar token
val channelName = "live_${userId}_${System.currentTimeMillis()}"
val data = hashMapOf(
    "channelName" to channelName,
    "role" to "publisher",
    "uid" to 0
)

val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()

val token = (result.data as? Map<*, *>)?.get("token") as? String

// 2. Iniciar transmisión
LiveRecordingScreen(
    sessionId = sessionId,
    channelName = channelName,
    agoraToken = token!!,
    onStreamStarted = { /* ... */ },
    onStreamEnded = { /* ... */ }
)
```

### Viewer (Espectador)
```kotlin
// 1. Obtener datos del Live desde Firestore
val liveSession = firebaseManager.getLiveSession(sessionId)

// 2. Generar token de viewer
val data = hashMapOf(
    "channelName" to liveSession.channelName,
    "role" to "subscriber",
    "uid" to 0
)

val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()

val token = (result.data as? Map<*, *>)?.get("token") as? String

// 3. Ver transmisión
LiveStreamViewerScreen(
    sessionId = sessionId,
    channelName = liveSession.channelName,
    agoraToken = token!!,
    streamerName = liveSession.username,
    onExit = { /* ... */ }
)
```

---

## 🎉 Resumen

Las Cloud Functions para generar tokens de Agora **ya están implementadas** en tu proyecto. Solo necesitas:

1. ✅ Desplegar las funciones a Firebase
2. ✅ Probar desde la app
3. ✅ Verificar que los tokens se generan correctamente

**Siguiente paso:** Desplegar las funciones y probar el flujo completo de Live Streaming.
