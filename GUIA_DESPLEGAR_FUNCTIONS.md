# 🚀 Guía Paso a Paso: Desplegar Cloud Functions

## 📋 Requisitos Previos

Antes de desplegar, asegúrate de tener:

- ✅ Node.js instalado (versión 20 o superior)
- ✅ Firebase CLI instalado
- ✅ Sesión iniciada en Firebase CLI

---

## 🔧 Paso 1: Verificar Instalación

### Verificar Node.js
```bash
node --version
```
Debería mostrar: `v20.x.x` o superior

### Verificar Firebase CLI
```bash
firebase --version
```
Debería mostrar: `13.x.x` o superior

### Si no tienes Firebase CLI instalado:
```bash
npm install -g firebase-tools
```

---

## 🔐 Paso 2: Iniciar Sesión en Firebase

```bash
firebase login
```

Esto abrirá tu navegador para autenticarte con tu cuenta de Google.

**Verificar que estás autenticado:**
```bash
firebase projects:list
```

Deberías ver tu proyecto `hypematch-d8e0f` en la lista.

---

## 📦 Paso 3: Instalar Dependencias

```bash
cd functions
npm install
cd ..
```

Esto instalará:
- `firebase-functions`
- `firebase-admin`
- `agora-access-token`

---

## 🚀 Paso 4: Desplegar las Funciones

### Opción A: Usar el Script Automatizado (Recomendado)
```bash
.\desplegar-functions.bat
```

### Opción B: Comando Manual
```bash
firebase deploy --only functions
```

---

## ⏱️ Tiempo de Despliegue

El despliegue puede tomar **2-5 minutos**. Verás algo como:

```
✔  functions: Finished running predeploy script.
i  functions: ensuring required API cloudfunctions.googleapis.com is enabled...
i  functions: ensuring required API cloudbuild.googleapis.com is enabled...
✔  functions: required API cloudfunctions.googleapis.com is enabled
✔  functions: required API cloudbuild.googleapis.com is enabled
i  functions: preparing codebase default for deployment
i  functions: current functions in project:
   generateAgoraToken(us-central1)
   generateStreamerToken(us-central1)
   generateViewerToken(us-central1)
i  functions: uploading functions...
✔  functions: functions folder uploaded successfully
i  functions: updating Node.js 20 function generateAgoraToken(us-central1)...
i  functions: updating Node.js 20 function generateStreamerToken(us-central1)...
i  functions: updating Node.js 20 function generateViewerToken(us-central1)...
✔  functions[generateAgoraToken(us-central1)] Successful update operation.
✔  functions[generateStreamerToken(us-central1)] Successful update operation.
✔  functions[generateViewerToken(us-central1)] Successful update operation.

✔  Deploy complete!
```

---

## ✅ Paso 5: Verificar el Despliegue

### Ver Funciones Desplegadas
```bash
firebase functions:list
```

Deberías ver:
```
┌────────────────────────┬────────────┬─────────────┐
│ Function               │ Region     │ Status      │
├────────────────────────┼────────────┼─────────────┤
│ generateAgoraToken     │ us-central1│ ACTIVE      │
│ generateStreamerToken  │ us-central1│ ACTIVE      │
│ generateViewerToken    │ us-central1│ ACTIVE      │
└────────────────────────┴────────────┴─────────────┘
```

### Ver en Firebase Console
1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto `hypematch-d8e0f`
3. Ve a **Functions** en el menú lateral
4. Deberías ver las 3 funciones listadas

---

## 🧪 Paso 6: Probar las Funciones

### Desde Firebase Console

1. Ve a Firebase Console → Functions
2. Haz clic en `generateAgoraToken`
3. Ve a la pestaña **Logs**
4. Haz clic en **Test function**
5. Ingresa los datos de prueba:

```json
{
  "data": {
    "channelName": "test_channel_123",
    "role": "publisher",
    "uid": 0
  }
}
```

6. Haz clic en **Run**

**Resultado esperado:**
```json
{
  "result": {
    "token": "006abc123...",
    "expiresAt": 1234567890,
    "channelName": "test_channel_123",
    "uid": 0
  }
}
```

---

## 📱 Paso 7: Probar desde la App

### Código de Prueba (Kotlin)

Agrega esto en cualquier parte de tu app para probar:

```kotlin
fun testAgoraToken() {
    val functions = Firebase.functions
    val data = hashMapOf(
        "channelName" to "test_channel",
        "role" to "publisher",
        "uid" to 0
    )
    
    functions
        .getHttpsCallable("generateAgoraToken")
        .call(data)
        .addOnSuccessListener { result ->
            val resultData = result.data as? Map<*, *>
            val token = resultData?.get("token") as? String
            val expiresAt = resultData?.get("expiresAt") as? Long
            
            android.util.Log.d("AgoraTest", "✅ Token generado exitosamente!")
            android.util.Log.d("AgoraTest", "   Token: ${token?.take(20)}...")
            android.util.Log.d("AgoraTest", "   Expira: ${Date(expiresAt!! * 1000)}")
        }
        .addOnFailureListener { e ->
            android.util.Log.e("AgoraTest", "❌ Error: ${e.message}")
        }
}
```

Llama a esta función desde un botón o al iniciar la app.

---

## 📊 Paso 8: Monitorear Logs

### Ver Logs en Tiempo Real
```bash
firebase functions:log
```

### Ver Logs de una Función Específica
```bash
firebase functions:log --only generateAgoraToken
```

### Ver Logs en Firebase Console
1. Ve a Firebase Console → Functions
2. Haz clic en la función que quieres ver
3. Ve a la pestaña **Logs**

---

## 🐛 Solución de Problemas

### Error: "Firebase CLI not found"
**Solución:**
```bash
npm install -g firebase-tools
```

### Error: "Not logged in"
**Solución:**
```bash
firebase login
```

### Error: "Permission denied"
**Solución:**
Verifica que tu cuenta tenga permisos de Editor o Propietario en el proyecto Firebase.

### Error: "Function deployment failed"
**Solución:**
1. Verifica que `functions/package.json` tenga las dependencias correctas
2. Ejecuta `npm install` en la carpeta `functions`
3. Intenta desplegar de nuevo

### Error: "agora-access-token not found"
**Solución:**
```bash
cd functions
npm install agora-access-token
cd ..
firebase deploy --only functions
```

---

## 🔄 Actualizar Funciones

Si haces cambios en `functions/index.js`, simplemente vuelve a desplegar:

```bash
firebase deploy --only functions
```

O usa el script:
```bash
.\desplegar-functions.bat
```

---

## 💰 Costos

Las Cloud Functions tienen un **plan gratuito generoso**:

- ✅ 2 millones de invocaciones/mes gratis
- ✅ 400,000 GB-segundos/mes gratis
- ✅ 200,000 CPU-segundos/mes gratis

Para una app pequeña/mediana, probablemente no pagarás nada.

---

## 📈 Métricas

Puedes ver el uso de tus funciones en:

1. Firebase Console → Functions
2. Haz clic en una función
3. Ve a la pestaña **Usage**

Verás:
- Número de invocaciones
- Tiempo de ejecución promedio
- Errores
- Uso de memoria

---

## ✅ Checklist Final

- [ ] Node.js instalado
- [ ] Firebase CLI instalado
- [ ] Sesión iniciada en Firebase
- [ ] Dependencias instaladas (`npm install` en `functions/`)
- [ ] Funciones desplegadas (`firebase deploy --only functions`)
- [ ] Funciones verificadas (`firebase functions:list`)
- [ ] Funciones probadas desde Firebase Console
- [ ] Funciones probadas desde la app
- [ ] Logs monitoreados

---

## 🎉 ¡Listo!

Tus Cloud Functions están desplegadas y funcionando. Ahora puedes:

1. ✅ Generar tokens de Agora desde tu app
2. ✅ Iniciar transmisiones en vivo
3. ✅ Permitir que espectadores vean las transmisiones

**Siguiente paso:** Probar el flujo completo de Live Streaming en dispositivos reales.
