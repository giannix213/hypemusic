# 🔧 Solución: Error al iniciar Live

## 🐛 Problema Identificado

El error "No se pudo iniciar la sesión de Live" ocurre porque falta la función `startNewLiveSession` en `FirebaseManager` que se encarga de:
1. Generar un token de Agora
2. Crear la sesión en Firestore
3. Retornar los datos necesarios para iniciar el Live

## ✅ Solución Implementada

He agregado las siguientes funciones a `FirebaseManager.kt`:

### 1. `startNewLiveSession()` - Inicia una nueva sesión de Live
- Genera un nombre de canal único
- Llama a la Cloud Function `generateAgoraToken` para obtener el token
- Crea un documento en Firestore con la sesión
- Retorna un objeto `LiveSession` con toda la información

### 2. `endLiveSession()` - Finaliza una sesión de Live
- Marca la sesión como inactiva en Firestore

### 3. `getActiveLiveSessions()` - Obtiene sesiones activas
- Para mostrar Lives en curso

### 4. `incrementViewerCount()` / `decrementViewerCount()`
- Para gestionar el contador de espectadores

## 🚀 Pasos para Verificar y Solucionar

### Paso 1: Verificar que las Cloud Functions estén desplegadas

Abre una terminal y ejecuta:

```bash
firebase functions:list
```

Deberías ver estas funciones:
- `generateAgoraToken`
- `generateStreamerToken`
- `generateViewerToken`

### Paso 2: Si las funciones NO están desplegadas, desplegarlas

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

### Paso 3: Verificar la configuración de Firebase en la app

Asegúrate de que tu app tenga el archivo `google-services.json` actualizado en:
```
app/google-services.json
```

### Paso 4: Rebuild de la app

```bash
# En Android Studio, ejecuta:
Build > Clean Project
Build > Rebuild Project
```

O desde la terminal:
```bash
gradlew clean
gradlew build
```

### Paso 5: Probar el Live

1. Abre la app
2. Ve a la pestaña "Live"
3. Intenta iniciar una transmisión
4. Revisa los logs en Logcat filtrando por:
   - `FirebaseManager`
   - `LiveViewModel`
   - `LiveLauncher`

## 📋 Logs Esperados

Si todo funciona correctamente, deberías ver en Logcat:

```
🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
👤 Usuario: [tu_nombre] ([tu_id])
📝 Título: Mi Live en Hype Match
📺 Canal generado: live_[userId]_[timestamp]
🔑 Solicitando token de Agora...
✅ Token de Agora recibido: [primeros_20_caracteres]...
💾 Creando documento en Firestore...
✅ Sesión creada en Firestore: [sessionId]
✅ ===== SESIÓN DE LIVE LISTA =====
```

## ❌ Posibles Errores y Soluciones

### Error: "Cloud Function not found"
**Causa:** Las Cloud Functions no están desplegadas
**Solución:** Ejecuta `firebase deploy --only functions`

### Error: "Permission denied"
**Causa:** El usuario no está autenticado o no tiene permisos
**Solución:** 
1. Verifica que el usuario esté logueado
2. Revisa las reglas de Firestore en Firebase Console

### Error: "Network error"
**Causa:** Problemas de conexión a internet
**Solución:** Verifica tu conexión y vuelve a intentar

### Error: "Invalid Agora credentials"
**Causa:** Las credenciales de Agora en `functions_index.js` son incorrectas
**Solución:** 
1. Ve a [Agora Console](https://console.agora.io/)
2. Verifica tu APP_ID y APP_CERTIFICATE
3. Actualiza los valores en `functions_index.js`
4. Redespliega las funciones

## 🔍 Verificar Credenciales de Agora

Las credenciales actuales en `functions_index.js` son:
```javascript
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';
```

Para verificar que son correctas:
1. Ve a https://console.agora.io/
2. Inicia sesión
3. Ve a "Project Management"
4. Verifica que el APP_ID coincida
5. Si es necesario, genera un nuevo APP_CERTIFICATE

## 📱 Probar desde la App

Una vez que todo esté configurado:

1. **Inicia la app**
2. **Ve a la pestaña Live** (ícono de play)
3. **Toca el botón para iniciar Live**
4. **Deberías ver:**
   - Pantalla de carga "Preparando Live..."
   - Luego la pantalla de grabación con la cámara activa
   - Botón rojo para finalizar

## 🎯 Resultado Esperado

Después de aplicar esta solución:
- ✅ El botón de Live funciona correctamente
- ✅ Se genera el token de Agora
- ✅ Se crea la sesión en Firestore
- ✅ La cámara se activa y puedes transmitir
- ✅ Los espectadores pueden unirse a tu Live

## 📞 Si el Problema Persiste

Si después de seguir todos los pasos el error continúa:

1. **Captura los logs completos** de Logcat
2. **Verifica el estado de Firebase Functions** en Firebase Console
3. **Revisa las reglas de seguridad** de Firestore
4. **Comparte los logs** para un diagnóstico más detallado

---

**Fecha de solución:** 22 de noviembre de 2025
**Archivos modificados:**
- `app/src/main/java/com/metu/hypematch/FirebaseManager.kt` (agregadas funciones de Live)
