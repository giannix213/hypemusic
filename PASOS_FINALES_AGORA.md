# 🚀 Pasos Finales para Activar Live Streaming

## ✅ Lo que ya está hecho

- ✅ Agora SDK integrado
- ✅ LiveRecordingScreen con funcionalidad completa
- ✅ Permisos configurados
- ✅ UI de controles implementada
- ✅ Manejo de estados y errores

## 🔧 Pasos que DEBES hacer ahora

### 1. Obtener credenciales de Agora (5 minutos)

1. Ve a https://console.agora.io/
2. Crea una cuenta o inicia sesión
3. Crea un nuevo proyecto
4. Activa "App Certificate" en la configuración del proyecto
5. Copia:
   - **App ID** (ejemplo: `a1b2c3d4e5f6g7h8i9j0`)
   - **App Certificate** (ejemplo: `f907826ae8ff4c00b7057d15b6f2e628`)

### 2. Configurar App ID en la app (1 minuto)

Abre `app/src/main/java/com/metu/hypematch/AgoraConfig.kt`:

```kotlin
object AgoraConfig {
    const val APP_ID = "PEGA_TU_APP_ID_AQUI"  // ← Reemplaza esto
    // ...
}
```

### 3. Configurar Cloud Function (10 minutos)

#### 3.1 Instalar Firebase CLI

```bash
npm install -g firebase-tools
firebase login
```

#### 3.2 Inicializar Functions en tu proyecto

```bash
cd TU_PROYECTO
firebase init functions
```

Selecciona:
- JavaScript o TypeScript (recomiendo JavaScript)
- Instalar dependencias: Sí

#### 3.3 Instalar Agora Token Generator

```bash
cd functions
npm install agora-access-token
```

#### 3.4 Copiar el código de la función

Abre `functions/index.js` y copia el código de `CLOUD_FUNCTION_AGORA_TOKEN.md`.

Reemplaza:
```javascript
const APP_ID = 'PEGA_TU_APP_ID_AQUI';
const APP_CERTIFICATE = 'PEGA_TU_APP_CERTIFICATE_AQUI';
```

#### 3.5 Desplegar

```bash
firebase deploy --only functions
```

### 4. Actualizar FirebaseManager (5 minutos)

Abre `app/src/main/java/com/metu/hypematch/FirebaseManager.kt`.

Busca la función `startNewLiveSession()` y reemplaza la sección del token:

```kotlin
// ANTES (token temporal):
val agoraToken = "TEMP_TOKEN_${System.currentTimeMillis()}"

// DESPUÉS (token real de Cloud Function):
val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
val data = hashMapOf("channelName" to channelName)

val result = functions
    .getHttpsCallable("generateStreamerToken")
    .call(data)
    .await()

val tokenData = result.data as? Map<*, *>
val agoraToken = tokenData?.get("token") as? String
    ?: throw Exception("No se pudo obtener el token de Agora")
```

### 5. Agregar dependencia de Firebase Functions (1 minuto)

En `app/build.gradle.kts`, agrega:

```kotlin
// Firebase Functions
implementation("com.google.firebase:firebase-functions")
```

### 6. Conectar el botón de Live (2 minutos)

En `LiveScreenNew.kt`, busca donde quieras agregar el botón de Live y agrega:

```kotlin
var showLiveLauncher by remember { mutableStateOf(false) }

// Donde tengas el botón de "Ir Live"
Button(onClick = { showLiveLauncher = true }) {
    Text("Ir Live")
}

// Al final del composable
if (showLiveLauncher) {
    LiveLauncherScreen(
        onClose = { showLiveLauncher = false }
    )
}
```

### 7. Sync y Build (2 minutos)

```bash
# En Android Studio:
File > Sync Project with Gradle Files
Build > Rebuild Project
```

## 🧪 Probar la implementación

### Prueba 1: Verificar permisos

1. Ejecuta la app
2. Haz clic en "Ir Live"
3. Deberías ver solicitud de permisos de cámara y audio
4. Concede los permisos

### Prueba 2: Verificar Cloud Function

En Firebase Console:
1. Ve a Functions
2. Busca `generateStreamerToken`
3. Prueba con:
   ```json
   {
     "channelName": "test_123"
   }
   ```
4. Deberías recibir un token

### Prueba 3: Transmisión real

1. Abre la app
2. Haz clic en "Ir Live"
3. Espera a que cargue (verás "Preparando Live...")
4. Deberías ver tu cámara
5. El badge "LIVE" debería aparecer en rojo
6. Prueba los controles:
   - Cambiar cámara
   - Mutear/Desmutear
   - Cerrar transmisión

## 📱 Probar con dos dispositivos

Para probar que funciona:

1. **Dispositivo 1 (Streamer):**
   - Inicia Live
   - Transmite

2. **Dispositivo 2 (Viewer):**
   - TODO: Necesitas crear `LiveViewerScreen` (próximo paso)
   - Por ahora, puedes usar la web demo de Agora

## 🐛 Si algo falla

### Error: "App ID is invalid"
```
Solución: Verifica que copiaste bien el App ID en AgoraConfig.kt
```

### Error: "Failed to join channel"
```
Solución: 
1. Verifica que la Cloud Function esté desplegada
2. Revisa los logs de Firebase Functions
3. Verifica que el App Certificate sea correcto
```

### Error: "Permission denied"
```
Solución: 
1. Ve a Configuración de la app
2. Concede permisos de cámara y audio manualmente
```

### No se ve la cámara
```
Solución:
1. Revisa Logcat filtrando por "LiveRecording"
2. Verifica que los permisos estén concedidos
3. Prueba en un dispositivo físico (no emulador)
```

## 📊 Monitoreo

### Logs importantes

Filtra en Logcat:
```
LiveRecording    # Logs de transmisión
LiveViewModel    # Logs de estados
FirebaseManager  # Logs de Firebase
Agora           # Logs del SDK
```

### Firebase Console

Monitorea:
- Functions > Logs (para ver llamadas a generateStreamerToken)
- Firestore > live_sessions (para ver sesiones activas)

## 🎯 Siguiente paso: Crear pantalla de Viewers

Una vez que funcione la transmisión, el siguiente paso es crear `LiveViewerScreen.kt` para que otros usuarios puedan ver el Live.

La diferencia principal:
```kotlin
// Streamer (ya implementado)
clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

// Viewer (por implementar)
clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
```

## ✨ Checklist Final

- [ ] App ID configurado en AgoraConfig.kt
- [ ] Cloud Function desplegada
- [ ] Firebase Functions agregado a build.gradle
- [ ] FirebaseManager actualizado para usar Cloud Function
- [ ] Botón de Live conectado en la UI
- [ ] App compilada sin errores
- [ ] Permisos concedidos en el dispositivo
- [ ] Transmisión probada y funcionando

---

¡Una vez completados estos pasos, tendrás Live Streaming funcionando! 🎉
