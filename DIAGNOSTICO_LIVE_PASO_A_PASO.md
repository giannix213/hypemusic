# 🔍 Diagnóstico Live Streaming - Paso a Paso

## 🎯 Problema Reportado

- ✅ Dispositivo 1: Puede iniciar transmisión
- ❌ Dispositivo 2: No ve la transmisión (muestra "No hay Lives activos")
- ❌ Los dispositivos no se conectan entre sí

---

## 📋 Diagnóstico en 5 Pasos

### Paso 1: Verificar Logs del Dispositivo 1 (Streamer)

**Ejecutar:**
```bash
.\ver-logs-live-completo.bat
```

**Luego en el dispositivo 1:**
1. Abre la app
2. Ve a la sección Live
3. Presiona "Iniciar Live"
4. Observa los logs

**Logs esperados (BUENOS):**
```
✅ Sesión lista, llamando callback onStartBroadcast
✅ Transmisión iniciada
📝 Creando sesión de Live: [sessionId]
✅ Sesión de Live creada: [sessionId]
✅ Canal unido exitosamente
📹 Preview de cámara iniciado
```

**Logs de ERROR (MALOS):**
```
❌ Error creando sesión: [mensaje]
❌ Error al unirse al canal: [código]
❌ Error inicializando Agora: [mensaje]
```

---

### Paso 2: Verificar Firestore

**Ejecutar:**
```bash
.\verificar-firestore-lives.bat
```

**Verificar en Firebase Console:**

1. Ve a **Firestore Database**
2. Busca la colección `live_sessions`
3. Verifica:

**✅ CORRECTO:**
```
live_sessions/
  └─ [sessionId]/
     ├─ sessionId: "abc123"
     ├─ userId: "user_xyz"
     ├─ username: "TuNombre"
     ├─ isActive: true          ← DEBE SER TRUE
     ├─ agoraChannelName: "live_user_xyz_123456"
     ├─ startTime: 1234567890
     └─ viewerCount: 0
```

**❌ INCORRECTO:**
- No existe la colección `live_sessions`
- No hay documentos cuando transmites
- `isActive` está en `false`
- Campos vacíos o null

---

### Paso 3: Verificar Logs del Dispositivo 2 (Viewer)

**Con el script de logs aún corriendo:**

**En el dispositivo 2:**
1. Abre la app
2. Ve a la sección Live
3. Desliza para ver el catálogo
4. Observa los logs

**Logs esperados (BUENOS):**
```
📡 Obteniendo sesiones de Live activas...
📡 Live encontrado: TuNombre - Live de TuNombre
✅ 1 sesiones activas encontradas
```

**Logs de ERROR (MALOS):**
```
⚠️ No se encontraron videos en Firestore
❌ Error obteniendo sesiones activas: [mensaje]
✅ 0 sesiones activas encontradas
```

---

### Paso 4: Verificar Cloud Functions

**Verificar que las funciones estén desplegadas:**
```bash
firebase functions:list
```

**Debe mostrar:**
```
✔ generateAgoraToken (us-central1)
✔ generateStreamerToken (us-central1)
✔ generateViewerToken (us-central1)
```

**Ver logs de Cloud Functions:**
```bash
firebase functions:log
```

**Logs esperados cuando inicias Live:**
```
✅ Token generado para canal: live_user_xyz_123456
   Rol: publisher
   UID: 0
```

---

### Paso 5: Verificar Conexión de Agora

**En los logs, buscar:**

**Dispositivo 1 (Streamer):**
```
✅ Canal unido exitosamente
   Canal: live_user_xyz_123456
   UID: 12345
```

**Dispositivo 2 (Viewer):**
```
✅ Canal unido exitosamente
   Canal: live_user_xyz_123456
   UID: 67890
👤 Usuario unido: 12345  ← Debe ver el UID del streamer
```

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "No se crea documento en Firestore"

**Síntoma:**
- Dispositivo 1 transmite
- No aparece nada en Firestore

**Causa:**
- La función `createLiveSession` no se está llamando
- Error en Firebase

**Solución:**
```kotlin
// Verificar en LiveScreenNew.kt línea ~580
onStreamStarted = {
    android.util.Log.d("LiveScreen", "✅ Transmisión iniciada")
    scope.launch {
        try {
            val userId = authManager.getUserId() ?: ""
            val username = authManager.getUserName()
            
            android.util.Log.d("LiveScreen", "📝 Llamando createLiveSession")
            android.util.Log.d("LiveScreen", "   SessionId: $liveSessionId")
            android.util.Log.d("LiveScreen", "   UserId: $userId")
            android.util.Log.d("LiveScreen", "   Username: $username")
            
            firebaseManager.createLiveSession(
                sessionId = liveSessionId,
                userId = userId,
                username = username,
                channelName = liveChannelName,
                title = "Live de $username"
            )
            
            android.util.Log.d("LiveScreen", "✅ createLiveSession completado")
        } catch (e: Exception) {
            android.util.Log.e("LiveScreen", "❌ Error creando sesión: ${e.message}", e)
        }
    }
}
```

---

### Problema 2: "Dispositivo 2 no ve Lives activos"

**Síntoma:**
- Firestore tiene documentos con `isActive = true`
- Dispositivo 2 muestra "No hay Lives activos"

**Causa:**
- `getActiveLiveSessions()` no está funcionando
- Problema con la query de Firestore

**Solución:**
Verificar en los logs:
```
📡 Obteniendo sesiones de Live activas...
```

Si no aparece, el problema está en `LiveScreenNew.kt` donde se llama `getActiveLiveSessions()`.

---

### Problema 3: "Token no se genera"

**Síntoma:**
```
❌ Error: No se recibió token de Agora
```

**Causa:**
- Cloud Functions no desplegadas
- Error en la función `generateAgoraToken`

**Solución:**
```bash
# Redesplegar funciones
firebase deploy --only functions

# Ver logs
firebase functions:log
```

---

### Problema 4: "No se conectan al mismo canal"

**Síntoma:**
- Ambos dispositivos se conectan
- No se ven entre sí

**Causa:**
- Están usando diferentes `channelName`
- Tokens con diferentes canales

**Verificar:**
```
Dispositivo 1: Canal: live_user_xyz_123456
Dispositivo 2: Canal: live_user_xyz_123456  ← DEBEN SER IGUALES
```

---

## 🔧 Script de Diagnóstico Automático

Voy a crear un script que verifique todo automáticamente:

```bash
.\diagnostico-automatico-live.bat
```

Este script:
1. ✅ Verifica dispositivos conectados
2. ✅ Captura logs en tiempo real
3. ✅ Muestra estado de Firestore
4. ✅ Verifica Cloud Functions
5. ✅ Genera reporte de diagnóstico

---

## 📊 Checklist de Verificación

Marca cada punto mientras pruebas:

### Dispositivo 1 (Streamer)
- [ ] App instalada y actualizada
- [ ] Usuario autenticado
- [ ] Permisos de cámara y micrófono concedidos
- [ ] Botón "Iniciar Live" funciona
- [ ] Aparece indicador "LIVE" rojo
- [ ] Se ve la cámara propia
- [ ] Logs muestran "Canal unido exitosamente"
- [ ] Documento creado en Firestore

### Dispositivo 2 (Viewer)
- [ ] App instalada y actualizada
- [ ] Usuario autenticado
- [ ] Va a sección Live
- [ ] Desliza para ver catálogo
- [ ] Ve el Live del dispositivo 1
- [ ] Puede hacer clic en el Live
- [ ] Se conecta y ve el video
- [ ] Logs muestran "Canal unido exitosamente"

### Backend
- [ ] Cloud Functions desplegadas
- [ ] Firestore tiene colección `live_sessions`
- [ ] Documentos se crean con `isActive = true`
- [ ] Tokens se generan correctamente

---

## 🚨 Acción Inmediata

**EJECUTA AHORA:**

1. **Abre una terminal y ejecuta:**
```bash
.\ver-logs-live-completo.bat
```

2. **En dispositivo 1:**
   - Inicia un Live
   - Observa los logs

3. **Copia y pega aquí los logs que aparecen**

4. **Abre otra terminal y ejecuta:**
```bash
.\verificar-firestore-lives.bat
```

5. **Toma captura de pantalla de Firestore**

Con esa información podré decirte exactamente dónde está el problema.

---

## 📞 Información Necesaria

Para ayudarte mejor, necesito:

1. **Logs del dispositivo 1** (cuando inicias Live)
2. **Logs del dispositivo 2** (cuando intentas ver)
3. **Captura de Firestore** (colección live_sessions)
4. **Resultado de:** `firebase functions:list`

---

¡Vamos a resolver esto! 🚀
