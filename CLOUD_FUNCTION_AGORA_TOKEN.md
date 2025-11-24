# 🔐 Cloud Function para Generar Tokens de Agora

## Paso 1: Instalar Firebase CLI

```bash
npm install -g firebase-tools
firebase login
firebase init functions
```

## Paso 2: Instalar dependencias de Agora

En la carpeta `functions/`:

```bash
cd functions
npm install agora-access-token
```

## Paso 3: Crear la Cloud Function

Archivo: `functions/index.js`

```javascript
const functions = require('firebase-functions');
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

// Configuración de Agora
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';

/**
 * Genera un token de Agora para un canal específico
 * 
 * Parámetros esperados:
 * - channelName: Nombre del canal
 * - role: 'publisher' o 'subscriber'
 * - uid: ID del usuario (0 para cualquier usuario)
 * 
 * Retorna:
 * - token: Token de Agora válido por 1 hora
 * - expiresAt: Timestamp de expiración
 */
exports.generateAgoraToken = functions.https.onCall((data, context) => {
  try {
    // Validar parámetros
    const channelName = data.channelName;
    const roleStr = data.role || 'subscriber';
    const uid = data.uid || 0;
    
    if (!channelName) {
      throw new functions.https.HttpsError(
        'invalid-argument',
        'El nombre del canal es requerido'
      );
    }
    
    // Determinar el rol
    const role = roleStr === 'publisher' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
    
    // Configurar expiración (1 hora)
    const expirationTimeInSeconds = 3600;
    const currentTimestamp = Math.floor(Date.now() / 1000);
    const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;
    
    // Generar token
    const token = RtcTokenBuilder.buildTokenWithUid(
      APP_ID,
      APP_CERTIFICATE,
      channelName,
      uid,
      role,
      privilegeExpiredTs
    );
    
    console.log('✅ Token generado para canal:', channelName);
    console.log('   Rol:', roleStr);
    console.log('   UID:', uid);
    console.log('   Expira en:', new Date(privilegeExpiredTs * 1000).toISOString());
    
    return {
      token: token,
      expiresAt: privilegeExpiredTs,
      channelName: channelName,
      uid: uid
    };
    
  } catch (error) {
    console.error('❌ Error generando token:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Error al generar el token de Agora',
      error.message
    );
  }
});

/**
 * Genera un token de Agora para un streamer (publisher)
 * Simplificado para uso directo desde la app
 */
exports.generateStreamerToken = functions.https.onCall((data, context) => {
  // Verificar que el usuario esté autenticado
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'El usuario debe estar autenticado'
    );
  }
  
  const channelName = data.channelName;
  const uid = context.auth.uid.hashCode(); // Convertir UID de Firebase a número
  
  return exports.generateAgoraToken({
    channelName: channelName,
    role: 'publisher',
    uid: uid
  }, context);
});

/**
 * Genera un token de Agora para un espectador (subscriber)
 */
exports.generateViewerToken = functions.https.onCall((data, context) => {
  // Verificar que el usuario esté autenticado
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'El usuario debe estar autenticado'
    );
  }
  
  const channelName = data.channelName;
  const uid = context.auth.uid.hashCode(); // Convertir UID de Firebase a número
  
  return exports.generateAgoraToken({
    channelName: channelName,
    role: 'subscriber',
    uid: uid
  }, context);
});

// Helper para convertir string a número
String.prototype.hashCode = function() {
  let hash = 0;
  for (let i = 0; i < this.length; i++) {
    const char = this.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
};
```

## Paso 4: Desplegar la Cloud Function

```bash
firebase deploy --only functions
```

## Paso 5: Actualizar FirebaseManager.kt

Reemplaza la función `startNewLiveSession` para llamar a la Cloud Function:

```kotlin
suspend fun startNewLiveSession(
    userId: String,
    username: String,
    profileImageUrl: String,
    title: String
): LiveSession? {
    return try {
        android.util.Log.d("FirebaseManager", "🎬 Iniciando nueva sesión de Live...")
        
        val sessionId = UUID.randomUUID().toString()
        val channelName = "live_$sessionId"
        val startTime = System.currentTimeMillis()
        
        // Llamar a Cloud Function para obtener token de Agora
        val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
        val data = hashMapOf(
            "channelName" to channelName
        )
        
        val result = functions
            .getHttpsCallable("generateStreamerToken")
            .call(data)
            .await()
        
        val tokenData = result.data as? Map<*, *>
        val agoraToken = tokenData?.get("token") as? String
            ?: throw Exception("No se pudo obtener el token de Agora")
        
        android.util.Log.d("FirebaseManager", "✅ Token de Agora obtenido")
        
        // Crear documento en Firestore
        val sessionData = hashMapOf(
            "sessionId" to sessionId,
            "userId" to userId,
            "username" to username,
            "profileImageUrl" to profileImageUrl,
            "title" to title,
            "agoraChannelName" to channelName,
            "startTime" to startTime,
            "isActive" to true,
            "viewerCount" to 0,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        
        firestore.collection("live_sessions")
            .document(sessionId)
            .set(sessionData)
            .await()
        
        android.util.Log.d("FirebaseManager", "✅ Sesión de Live creada")
        
        LiveSession(
            sessionId = sessionId,
            userId = userId,
            username = username,
            profileImageUrl = profileImageUrl,
            title = title,
            agoraChannelName = channelName,
            agoraToken = agoraToken,
            startTime = startTime,
            isActive = true,
            viewerCount = 0
        )
    } catch (e: Exception) {
        android.util.Log.e("FirebaseManager", "❌ Error: ${e.message}", e)
        null
    }
}
```

## Paso 6: Configurar App ID en AgoraConfig.kt

Reemplaza `TU_APP_ID_AQUI` con tu App ID real de Agora en:
`app/src/main/java/com/metu/hypematch/AgoraConfig.kt`

## 🔒 Seguridad

- ✅ El App Certificate está solo en el backend (Cloud Function)
- ✅ Los tokens se generan en el servidor
- ✅ Los tokens expiran después de 1 hora
- ✅ Solo usuarios autenticados pueden obtener tokens

## 🧪 Probar la Cloud Function

Desde Firebase Console > Functions, puedes probar con:

```json
{
  "channelName": "test_channel_123"
}
```

## 📝 Notas

- Los tokens expiran en 1 hora por defecto
- Puedes ajustar el tiempo de expiración en la Cloud Function
- El UID se genera automáticamente desde el Firebase Auth UID
- Para viewers, usa `generateViewerToken` en lugar de `generateStreamerToken`
