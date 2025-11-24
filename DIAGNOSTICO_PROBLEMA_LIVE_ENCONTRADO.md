# 🔍 DIAGNÓSTICO: Problema Encontrado en Live Streaming

## ❌ PROBLEMA IDENTIFICADO

El dispositivo espectador muestra **"Lives encontrados: 0"** porque **la sesión de Live nunca se guarda en Firebase**.

## 🕵️ ANÁLISIS DEL CÓDIGO

### 1. ✅ LiveSession.kt - CORRECTO
```kotlin
data class LiveSession(
    val sessionId: String = "",
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val title: String = "",
    val agoraChannelName: String = "",
    val agoraToken: String = "",
    val startTime: Long = 0L,
    val isActive: Boolean = false,  // ✅ Campo correcto
    val viewerCount: Int = 0
)
```

### 2. ✅ FirebaseManager.kt - FUNCIONES EXISTEN
```kotlin
// ✅ Función para iniciar Live (línea ~2400)
suspend fun startNewLiveSession(
    userId: String,
    username: String,
    profileImageUrl: String,
    title: String
): LiveSession?

// ✅ Función para obtener Lives activos (línea ~2300)
suspend fun getActiveLiveSessions(): List<LiveSession> {
    val snapshot = firestore.collection("live_sessions")
        .whereEqualTo("isActive", true)  // ✅ Filtro correcto
        .orderBy("startTime", ...)
        .get()
        .await()
}
```

### 3. ❌ LiveLauncherScreen.kt - PROBLEMA CRÍTICO
```kotlin
// ❌ Intenta usar LiveViewModel que NO EXISTE
val viewModel = remember {
    LiveViewModel(  // ← ESTA CLASE NO EXISTE
        firebaseManager = firebaseManager,
        currentUserId = currentUserId,
        currentUsername = currentUsername,
        currentUserProfilePic = currentUserProfilePic
    )
}

val liveState by viewModel.liveState.collectAsState()  // ← FALLA AQUÍ
```

### 4. ❌ LiveRecordingScreen.kt - NO GUARDA EN FIREBASE
```kotlin
// ✅ Agora se conecta correctamente
engine.joinChannel(agoraToken, channelName, 0, options)

// ❌ PERO NUNCA LLAMA A:
// firebaseManager.startNewLiveSession(...)
```

## 🎯 CAUSA RAÍZ

1. **LiveViewModel no existe** → El flujo de inicio de Live está roto
2. **LiveRecordingScreen no guarda la sesión** → Aunque Agora funciona, Firebase no se entera
3. **El espectador busca en Firebase** → No encuentra nada porque nunca se guardó

## 📊 FLUJO ACTUAL (ROTO)

```
Usuario presiona "Iniciar Live"
    ↓
LiveLauncherScreen intenta crear LiveViewModel ❌ FALLA
    ↓
(Si funcionara) LiveRecordingScreen inicia Agora ✅ FUNCIONA
    ↓
❌ NUNCA se llama a firebaseManager.startNewLiveSession()
    ↓
Firebase: live_sessions está vacío
    ↓
Espectador: "Lives encontrados: 0"
```

## 📊 FLUJO CORRECTO (NECESARIO)

```
Usuario presiona "Iniciar Live"
    ↓
LiveLauncherScreen llama a firebaseManager.startNewLiveSession()
    ↓
Firebase crea documento en live_sessions con isActive=true ✅
    ↓
LiveRecordingScreen inicia Agora con el token recibido ✅
    ↓
Espectador consulta Firebase y encuentra el Live ✅
```

## 🔧 SOLUCIÓN REQUERIDA

### Opción 1: Crear LiveViewModel (Complejo)
- Crear clase LiveViewModel
- Implementar estados (Idle, Loading, Error, SessionReady)
- Manejar coroutines y StateFlow

### Opción 2: Simplificar LiveLauncherScreen (RECOMENDADO)
- Eliminar dependencia de LiveViewModel
- Llamar directamente a `firebaseManager.startNewLiveSession()`
- Usar estados simples con `remember` y `mutableStateOf`

## 📝 ARCHIVOS A MODIFICAR

1. **LiveLauncherScreen.kt** - Reescribir sin LiveViewModel
2. **LiveRecordingScreen.kt** - Verificar que recibe sessionId correcto
3. **LiveScreenNew.kt** - Verificar navegación

## ⚠️ NOTA IMPORTANTE

El contador de Lives SÍ cambia cuando inicias un Live porque probablemente hay código en otro lugar que incrementa un contador, pero **el documento de la sesión nunca se crea en Firestore**.

## 🎬 PRÓXIMO PASO

Implementar la solución simplificada que:
1. Elimina LiveViewModel
2. Llama directamente a Firebase
3. Guarda la sesión correctamente
4. Permite que los espectadores la vean
