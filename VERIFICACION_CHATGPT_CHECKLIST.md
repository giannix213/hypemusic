# ✅ VERIFICACIÓN COMPLETA - Checklist de ChatGPT

## 📋 Revisión de los 8 Puntos Críticos

### ✅ 1. startNewLiveSession() - CORRECTO

**Estado:** ✅ PERFECTO

**Código verificado:**
```kotlin
val sessionData = hashMapOf(
    "sessionId" to sessionId,           // ✅ String
    "userId" to userId,                 // ✅ String
    "username" to username,             // ✅ String
    "profileImageUrl" to profileImageUrl, // ✅ String
    "title" to title,                   // ✅ String
    "agoraChannelName" to channelName,  // ✅ String
    "agoraToken" to agoraToken,         // ✅ String
    "startTime" to System.currentTimeMillis(), // ✅ Long
    "isActive" to true,                 // ✅ Boolean (NO String)
    "viewerCount" to 0                  // ✅ Int
)
```

**Verificación:**
- ✅ `isActive` es `Boolean true` (no String "true")
- ✅ Todos los campos tienen el tipo correcto
- ✅ Se guarda ANTES de conectar a Agora
- ✅ Usa `System.currentTimeMillis()` para timestamp

---

### ✅ 2. getActiveLiveSessions() - CORRECTO

**Estado:** ✅ PERFECTO

**Código verificado:**
```kotlin
firestore.collection("live_sessions")
    .whereEqualTo("isActive", true)  // ✅ Filtro correcto
    .orderBy("startTime", Query.Direction.DESCENDING)
    .get()
    .await()
```

**Verificación:**
- ✅ Usa `.whereEqualTo("isActive", true)`
- ✅ El campo se llama exactamente "isActive" (no "is_active" ni "active")
- ✅ Compara con `true` (Boolean)
- ✅ NO filtra por userId (todos los Lives son visibles)

---

### ✅ 3. El espectador NO filtra por userId - CORRECTO

**Estado:** ✅ PERFECTO

**Verificación:**
- ✅ La consulta NO tiene `.whereEqualTo("userId", currentUser)`
- ✅ Todos los Lives activos son visibles para todos
- ✅ No hay filtros adicionales que limiten la visibilidad

---

### ✅ 4. Orden del flujo - CORRECTO

**Estado:** ✅ PERFECTO

**Flujo actual:**
```
1. startNewLiveSession() crea documento en Firebase
   ↓
2. Documento guardado con isActive = true
   ↓
3. onStartBroadcast() navega a LiveRecordingScreen
   ↓
4. LiveRecordingScreen conecta a Agora como broadcaster
```

**Verificación:**
- ✅ Firebase se guarda PRIMERO
- ✅ Agora se conecta DESPUÉS
- ✅ El orden es correcto

---

### ✅ 5. agoraChannelName es consistente - CORRECTO

**Estado:** ✅ PERFECTO

**Generación del canal:**
```kotlin
val channelName = "live_${userId}_${System.currentTimeMillis()}"
```

**Verificación:**
- ✅ Se genera UNA SOLA VEZ en `startNewLiveSession()`
- ✅ Se guarda en Firebase
- ✅ Se pasa al emisor vía `onStartBroadcast()`
- ✅ El espectador lo lee de Firebase
- ✅ NO hay espacios ni caracteres especiales
- ✅ Es único por timestamp

---

### ⚠️ 6. Rol de Agora - NECESITA VERIFICACIÓN

**Estado:** ⚠️ REVISAR

**Emisor (LiveRecordingScreen):**
```kotlin
val options = ChannelMediaOptions().apply {
    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER  // ✅ CORRECTO
    publishCameraTrack = true
    publishMicrophoneTrack = true
}
```

**Espectador (LiveStreamViewerScreen):**
```kotlin
// ⚠️ NECESITA VERIFICACIÓN
// Debe tener:
clientRoleType = Constants.CLIENT_ROLE_AUDIENCE  // ← VERIFICAR ESTO
```

**Acción requerida:**
- [ ] Verificar que LiveStreamViewerScreen use `CLIENT_ROLE_AUDIENCE`
- [ ] NO debe usar `CLIENT_ROLE_BROADCASTER`

---

### ⚠️ 7. Tokens de Agora - NECESITA VERIFICACIÓN

**Estado:** ⚠️ REVISAR

**Emisor:**
```kotlin
val data = hashMapOf(
    "channelName" to channelName,
    "role" to "publisher",  // ✅ CORRECTO para emisor
    "uid" to 0
)
```

**Espectador:**
```kotlin
// ⚠️ NECESITA VERIFICACIÓN
// Debe solicitar token con:
"role" to "subscriber"  // ← VERIFICAR ESTO
```

**Acción requerida:**
- [ ] Verificar que el espectador solicite token con role="subscriber"
- [ ] Verificar que el token no expire en 60 segundos
- [ ] Verificar Cloud Function `generateAgoraToken`

---

### ⚠️ 8. Firestore Security Rules - CRÍTICO

**Estado:** ⚠️ DEBE VERIFICARSE

**Reglas actuales:** DESCONOCIDAS

**Reglas necesarias:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /live_sessions/{sessionId} {
      // ✅ Permitir lectura pública (para espectadores sin login)
      allow read: if true;
      
      // ✅ Permitir escritura solo a usuarios autenticados
      allow write: if request.auth != null;
    }
  }
}
```

**Acción requerida:**
- [ ] Verificar reglas en Firebase Console
- [ ] Si dice `allow read: if request.auth != null;` → CAMBIAR a `if true;`
- [ ] Esto permite que espectadores sin login vean Lives

---

## 📊 RESUMEN DE VERIFICACIÓN

### ✅ Puntos Correctos (5/8)
1. ✅ startNewLiveSession() guarda correctamente
2. ✅ getActiveLiveSessions() consulta correctamente
3. ✅ No filtra por userId
4. ✅ Orden del flujo es correcto
5. ✅ agoraChannelName es consistente

### ⚠️ Puntos a Verificar (3/8)
6. ⚠️ Rol del espectador en Agora
7. ⚠️ Token del espectador
8. ⚠️ Firestore Security Rules

---

## 🔍 ARCHIVOS A REVISAR

### 1. LiveStreamViewerScreen.kt
**Buscar:**
```kotlin
clientRoleType = ???
```

**Debe ser:**
```kotlin
clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
```

### 2. Cloud Function (functions/index.js)
**Buscar:**
```javascript
exports.generateAgoraToken = functions.https.onCall((data, context) => {
    const role = data.role;  // "publisher" o "subscriber"
    // ...
});
```

**Verificar:**
- ✅ Acepta role="publisher" para emisor
- ✅ Acepta role="subscriber" para espectador
- ✅ Token no expira en 60 segundos (debe ser 3600 o más)

### 3. Firebase Console
**Ir a:**
1. Firebase Console
2. Firestore Database
3. Rules

**Verificar:**
```javascript
match /live_sessions/{sessionId} {
  allow read: if true;  // ← DEBE SER "if true"
  allow write: if request.auth != null;
}
```

---

## 🎯 PRÓXIMOS PASOS

### Paso 1: Verificar LiveStreamViewerScreen
```bash
# Buscar el archivo
# Revisar que use CLIENT_ROLE_AUDIENCE
```

### Paso 2: Verificar Cloud Function
```bash
cd functions
# Revisar index.js
# Verificar que maneje role="subscriber"
```

### Paso 3: Verificar Firestore Rules
```bash
# Ir a Firebase Console
# Firestore Database > Rules
# Cambiar si es necesario
```

### Paso 4: Probar
```bash
probar-live.bat
```

---

## 📝 COMANDOS PARA VERIFICAR

### Ver LiveStreamViewerScreen:
```bash
# Buscar CLIENT_ROLE en el archivo
findstr /C:"CLIENT_ROLE" app\src\main\java\com\metu\hypematch\LiveStreamViewerScreen.kt
```

### Ver Cloud Function:
```bash
type functions\index.js | findstr /C:"role"
```

### Ver Firestore Rules:
```bash
# Abrir Firebase Console
start https://console.firebase.google.com/project/_/firestore/rules
```

---

## ✅ CHECKLIST FINAL

Antes de probar en dispositivos reales:

- [x] startNewLiveSession() guarda isActive como Boolean
- [x] getActiveLiveSessions() usa whereEqualTo("isActive", true)
- [x] No filtra por userId
- [x] Orden del flujo es correcto
- [x] agoraChannelName es único y consistente
- [ ] LiveStreamViewerScreen usa CLIENT_ROLE_AUDIENCE
- [ ] Cloud Function acepta role="subscriber"
- [ ] Firestore Rules permiten lectura pública

---

## 🚨 SI ALGO FALLA

### Espectador no ve Lives:
1. Verificar Firestore Rules (punto 8)
2. Verificar logs: `adb logcat -s FirebaseManager:D`
3. Verificar Firebase Console: ¿Existe el documento?

### Espectador no puede unirse:
1. Verificar rol de Agora (punto 6)
2. Verificar token (punto 7)
3. Verificar logs de Agora

### Error al obtener token:
1. Verificar Cloud Function desplegada
2. Ver logs: `firebase functions:log`
3. Verificar credenciales de Agora

---

**Estado actual:** 5/8 puntos verificados ✅
**Acción requerida:** Verificar 3 puntos restantes ⚠️
