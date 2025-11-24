# 📨 Mensaje para ChatGPT

## Revisión de los 8 puntos críticos

He revisado todos los puntos que mencionaste. Aquí está el resultado:

### ✅ CORRECTOS (6/8)

1. **startNewLiveSession()** ✅
   - `isActive` es `Boolean true` (no String)
   - Todos los campos con tipos correctos
   - Se guarda en "live_sessions"

2. **getActiveLiveSessions()** ✅
   - Usa `.whereEqualTo("isActive", true)`
   - Campo exacto: "isActive"
   - Compara con Boolean

3. **No filtra por userId** ✅
   - Todos los Lives son visibles para todos

4. **Orden del flujo** ✅
   - Firebase PRIMERO → Agora DESPUÉS

5. **agoraChannelName** ✅
   - Se genera una sola vez: `"live_${userId}_${timestamp}"`
   - Sin espacios, único, consistente

6. **Rol del espectador** ✅
   - Espectador: `CLIENT_ROLE_AUDIENCE`
   - Emisor: `CLIENT_ROLE_BROADCASTER`

### ⚠️ NECESITAN VERIFICACIÓN (2/8)

7. **Tokens de Agora** ⚠️
   - Emisor solicita token con `role="publisher"` ✅
   - **Espectador usa el mismo token del emisor** ⚠️
   - ¿Debería generar su propio token con `role="subscriber"`?

8. **Firestore Rules** ⚠️
   - No puedo verificarlas desde el código
   - Necesito revisar Firebase Console
   - ¿Debe ser `allow read: if true;`?

---

## 📝 Código clave

### startNewLiveSession()
```kotlin
val sessionData = hashMapOf(
    "sessionId" to sessionId,
    "userId" to userId,
    "username" to username,
    "profileImageUrl" to profileImageUrl,
    "title" to title,
    "agoraChannelName" to channelName,
    "agoraToken" to agoraToken,
    "startTime" to System.currentTimeMillis(),
    "isActive" to true,  // ✅ Boolean
    "viewerCount" to 0
)
firestore.collection("live_sessions").document(sessionId).set(sessionData).await()
```

### getActiveLiveSessions()
```kotlin
firestore.collection("live_sessions")
    .whereEqualTo("isActive", true)  // ✅ Boolean
    .orderBy("startTime", Query.Direction.DESCENDING)
    .get()
    .await()
```

### Flujo completo
```kotlin
// 1. Usuario presiona "Iniciar Live"
fun startLiveSetup() {
    // 2. Crear sesión en Firebase
    val session = firebaseManager.startNewLiveSession(...)
    
    // 3. Navegar a LiveRecordingScreen
    onStartBroadcast(session.sessionId, session.agoraChannelName, session.agoraToken)
}

// 4. LiveRecordingScreen conecta a Agora
val options = ChannelMediaOptions().apply {
    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
}
engine.joinChannel(agoraToken, channelName, 0, options)
```

---

## ❓ Preguntas para ti

1. **Token del espectador:** ¿Puede usar el mismo token del emisor (role="publisher"), o debe generar uno propio con role="subscriber"?

2. **Firestore Rules:** ¿Deben ser así?
   ```javascript
   match /live_sessions/{sessionId} {
     allow read: if true;  // ← ¿Público?
     allow write: if request.auth != null;
   }
   ```

3. **Expiración del token:** ¿Cuánto tiempo debe durar? ¿3600 segundos está bien?

---

## 📊 Resumen

- **Estado:** 6/8 puntos verificados ✅
- **Confianza:** 85%
- **Bloqueadores:** Firestore Rules (crítico)
- **Dudas:** Token del espectador

¿Puedes confirmar los 2 puntos pendientes?
