# 🔧 Corrección: Lives Activos Ahora Funcionan

## 🐛 Problema Identificado

Los Lives no aparecían en el catálogo porque el campo `sessionId` no se estaba guardando en el documento de Firestore.

### Causa Raíz:
```kotlin
// ANTES (Incorrecto):
val docRef = firestore.collection("live_sessions")
    .add(sessionData)  // ← Genera ID automático pero NO lo guarda en el documento
    .await()

// El documento se guardaba SIN el campo sessionId
// Cuando se intentaba leer, sessionId era null
```

## ✅ Solución Aplicada

Ahora el `sessionId` se guarda explícitamente en el documento:

```kotlin
// AHORA (Correcto):
val docRef = firestore.collection("live_sessions").document()
val sessionId = docRef.id  // ← Obtener el ID primero

val sessionData = hashMapOf(
    "sessionId" to sessionId,  // ← Guardar el ID en el documento
    "userId" to userId,
    "username" to username,
    // ... resto de campos
)

docRef.set(sessionData).await()  // ← Guardar con el ID incluido
```

## 🔄 Cambios Realizados

### 1. En `startNewLiveSession()`:
- Generar el ID del documento primero
- Incluir `sessionId` en los datos
- Usar `.set()` en lugar de `.add()`

### 2. En `getActiveLiveSessions()`:
- Usar `doc.id` como fallback si `sessionId` no existe
- Agregar más logs para debugging
- Manejar casos donde falten datos

## 📊 Estructura del Documento en Firestore

### Antes (Incorrecto):
```javascript
live_sessions/abc123xyz {
  userId: "user001",
  username: "Juan",
  // ... otros campos
  // ❌ sessionId: NO EXISTE
}
```

### Ahora (Correcto):
```javascript
live_sessions/abc123xyz {
  sessionId: "abc123xyz",  // ✅ Ahora existe
  userId: "user001",
  username: "Juan",
  title: "Mi Live",
  agoraChannelName: "live_user001_1234567890",
  agoraToken: "006...",
  startTime: 1234567890,
  isActive: true,
  viewerCount: 0
}
```

## 🚀 Cómo Probar Ahora

### Paso 1: Rebuild de la App
```
Build > Clean Project
Build > Rebuild Project
```

### Paso 2: Dispositivo A (Streamer)
1. Abre la app
2. Ve a la pestaña "Live"
3. Presiona "Iniciar Live"
4. Comienza a transmitir

### Paso 3: Dispositivo B (Espectador)
1. Abre la app
2. Ve a la pestaña "Live"
3. **Desliza hacia la izquierda** (swipe left)
4. Deberías ver el Live del Dispositivo A
5. Toca para unirte

## 📱 Logs Esperados

### En el Dispositivo que Transmite:
```
D/FirebaseManager: 🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
D/FirebaseManager: 👤 Usuario: Juan (user001)
D/FirebaseManager: 📺 Canal generado: live_user001_1234567890
D/FirebaseManager: 🔑 Solicitando token de Agora...
D/FirebaseManager: ✅ Token de Agora recibido: 006...
D/FirebaseManager: 💾 Creando documento en Firestore...
D/FirebaseManager: ✅ Sesión creada en Firestore: abc123xyz
```

### En el Dispositivo que Ve:
```
D/LiveScreen: 📡 ===== CARGANDO LIVES ACTIVOS =====
D/FirebaseManager: 📡 Obteniendo sesiones de Live activas...
D/FirebaseManager:   📡 Live encontrado: Juan - Mi Live
D/FirebaseManager: ✅ 1 sesiones activas encontradas
D/LiveScreen: ✅ Lives activos cargados: 1
D/LiveScreen:   🔴 Juan - 0 espectadores
```

## 🎯 Verificación en Firebase Console

1. Ve a: https://console.firebase.google.com/project/hype-13966/firestore
2. Busca la colección `live_sessions`
3. Deberías ver documentos con:
   - ✅ `sessionId` (campo presente)
   - ✅ `isActive: true`
   - ✅ `username` con el nombre del usuario
   - ✅ `agoraChannelName` con el canal

## ⚠️ Nota Importante

Si ya habías iniciado Lives antes de esta corrección, esos documentos NO tendrán el campo `sessionId`. La función `getActiveLiveSessions()` ahora usa `doc.id` como fallback, así que deberían funcionar de todas formas.

Para limpiar Lives antiguos:
1. Ve a Firebase Console
2. Firestore Database
3. Colección `live_sessions`
4. Elimina documentos antiguos o actualiza `isActive` a `false`

## ✅ Resultado Esperado

Ahora cuando inicies un Live:
1. ✅ Se guarda correctamente en Firestore con `sessionId`
2. ✅ Aparece en el catálogo de Lives activos
3. ✅ Otros usuarios pueden verlo
4. ✅ Se actualiza cada 10 segundos
5. ✅ Muestra el número de espectadores

---

**Estado:** ✅ Corregido
**Archivos modificados:** FirebaseManager.kt
**Listo para probar:** Sí
**Rebuild necesario:** Sí
