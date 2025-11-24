# 🎯 EMPEZAR AQUÍ - Activar Live Streaming

## ✅ Lo que YA está hecho

- ✅ App ID de Agora configurado
- ✅ Código de Live Streaming completo
- ✅ Permisos configurados
- ✅ UI implementada
- ✅ Cloud Function lista para desplegar

## 🚀 Lo que DEBES hacer (15 minutos)

### Opción 1: Desplegar Cloud Function (Recomendado)

**Para tener Live Streaming funcionando en producción:**

1. **Instalar Node.js** (5 min)
   - Ve a: https://nodejs.org/
   - Descarga e instala la versión LTS
   - Reinicia PowerShell

2. **Ejecutar script automático** (10 min)
   ```powershell
   cd C:\Users\Freddy\HypeMatch
   .\setup-firebase-functions.ps1
   ```
   
   El script hará todo automáticamente.

3. **Actualizar FirebaseManager.kt** (2 min)
   - Ver sección abajo

**Guías detalladas:**
- `GUIA_VISUAL_DESPLIEGUE.md` - Paso a paso con capturas
- `INSTALAR_NODEJS_Y_FIREBASE.md` - Instalación detallada

### Opción 2: Probar sin Cloud Function (Rápido)

**Para probar AHORA (solo pruebas locales):**

1. Ejecuta la app
2. Haz clic en el botón de Live
3. Concede permisos
4. ¡Deberías ver tu cámara!

⚠️ **Nota:** El token temporal puede fallar. Para producción, necesitas la Cloud Function.

---

## 📝 Actualizar FirebaseManager.kt

Una vez desplegada la Cloud Function, actualiza el código:

### 1. Abrir archivo

`app/src/main/java/com/metu/hypematch/FirebaseManager.kt`

### 2. Buscar la función `startNewLiveSession()`

Busca estas líneas (alrededor de la línea 2120):

```kotlin
// TODO: Aquí deberías llamar a tu Cloud Function para obtener el token de Agora
// Por ahora, usaremos un token de prueba (esto NO funcionará en producción)

val agoraToken = "TEMP_TOKEN_${System.currentTimeMillis()}" // Token temporal

android.util.Log.w("FirebaseManager", "⚠️ USANDO TOKEN TEMPORAL - Implementa Cloud Function para producción")
```

### 3. Reemplazar con:

```kotlin
// Llamar a Cloud Function para obtener token de Agora
android.util.Log.d("FirebaseManager", "📞 Llamando a Cloud Function para obtener token...")

val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
val data = hashMapOf("channelName" to channelName)

val result = functions
    .getHttpsCallable("generateStreamerToken")
    .call(data)
    .await()

val tokenData = result.data as? Map<*, *>
val agoraToken = tokenData?.get("token") as? String
    ?: throw Exception("No se pudo obtener el token de Agora")

android.util.Log.d("FirebaseManager", "✅ Token de Agora obtenido desde Cloud Function")
```

### 4. Guardar y sincronizar

En Android Studio:
- File > Sync Project with Gradle Files
- Build > Rebuild Project

---

## 🧪 Probar Live Streaming

### 1. Ejecutar la app

```
Run > Run 'app'
```

### 2. Navegar a Live

- Abre la app
- Ve a la sección de videos
- Busca el ícono de Live (esquina superior izquierda)

### 3. Iniciar transmisión

1. Haz clic en el ícono de Live
2. Concede permisos de cámara y audio
3. Espera a que cargue (verás "Preparando Live...")
4. Deberías ver:
   - Tu cámara en pantalla completa
   - Badge "LIVE" en rojo
   - Contador de espectadores
   - Botones de control

### 4. Probar controles

- 🔄 Cambiar cámara (frontal/trasera)
- 🎤 Mutear/Desmutear micrófono
- ❌ Finalizar transmisión

---

## 📊 Monitorear

### Logs en Android Studio

Filtra en Logcat:
```
LiveRecording    # Logs de transmisión
LiveViewModel    # Logs de estados
FirebaseManager  # Logs de Firebase
Agora           # Logs del SDK
```

### Firebase Console

1. Ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto
3. Ve a **Functions** > **Logs**
4. Deberías ver llamadas a `generateStreamerToken`

---

## 🐛 Troubleshooting

### Error: "App ID is invalid"
✅ Ya está configurado correctamente

### Error: "Failed to join channel"
❌ Necesitas desplegar la Cloud Function

### Error: "Permission denied"
📱 Ve a Configuración > Apps > HypeMatch > Permisos
   Concede cámara y audio

### No se ve la cámara
📱 Prueba en un dispositivo físico (no emulador)
📋 Revisa logs en Logcat

---

## 📚 Archivos de Ayuda

| Archivo | Descripción |
|---------|-------------|
| `GUIA_VISUAL_DESPLIEGUE.md` | Guía paso a paso con capturas |
| `INSTALAR_NODEJS_Y_FIREBASE.md` | Instalación detallada |
| `setup-firebase-functions.ps1` | Script automático |
| `DESPLEGAR_CLOUD_FUNCTION.md` | Guía completa de despliegue |
| `functions_index.js` | Código de la Cloud Function |

---

## 🎯 Checklist Completo

### Despliegue
- [ ] Node.js instalado
- [ ] Firebase CLI instalado
- [ ] Sesión iniciada en Firebase
- [ ] Functions inicializadas
- [ ] Dependencia de Agora instalada
- [ ] Código copiado
- [ ] Funciones desplegadas
- [ ] Funciones visibles en Firebase Console

### Código
- [ ] FirebaseManager.kt actualizado
- [ ] Proyecto sincronizado
- [ ] App compilada sin errores

### Pruebas
- [ ] App ejecutada
- [ ] Permisos concedidos
- [ ] Transmisión iniciada
- [ ] Cámara visible
- [ ] Controles funcionando

---

## 🎉 ¡Listo para transmitir!

Una vez completados estos pasos, tendrás Live Streaming funcionando completamente.

**Próximos pasos:**
- Crear pantalla de viewers (LiveViewerScreen)
- Implementar chat en vivo
- Agregar efectos y filtros

---

## 📞 Resumen de 3 Pasos

1. **Instalar Node.js** → https://nodejs.org/
2. **Ejecutar script** → `.\setup-firebase-functions.ps1`
3. **Actualizar código** → FirebaseManager.kt

¡Eso es todo! 🚀
