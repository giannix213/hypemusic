# ✅ Paso 2: Cloud Functions - COMPLETADO

## 🎯 Resumen

Las Cloud Functions para generar tokens de Agora **ya están implementadas** en tu proyecto. Solo necesitas desplegarlas a Firebase.

---

## 📦 Archivos Creados/Verificados

### 1. Cloud Functions
- ✅ `functions/index.js` - Funciones implementadas
- ✅ `functions/package.json` - Dependencias configuradas
- ✅ `functions/.gitignore` - Archivos ignorados

### 2. Scripts de Despliegue
- ✅ `desplegar-functions.bat` - Script automatizado para desplegar
- ✅ `probar-functions-local.bat` - Script para probar localmente

### 3. Documentación
- ✅ `PASO_2_CLOUD_FUNCTIONS.md` - Documentación técnica
- ✅ `GUIA_DESPLEGAR_FUNCTIONS.md` - Guía paso a paso

---

## 🔧 Funciones Implementadas

### 1. generateAgoraToken
**Propósito:** Generar tokens de Agora para cualquier canal y rol

**Parámetros:**
```javascript
{
  channelName: string,  // Nombre del canal
  role: string,         // 'publisher' o 'subscriber'
  uid: number          // ID del usuario (0 para auto)
}
```

**Uso desde Kotlin:**
```kotlin
val functions = Firebase.functions
val data = hashMapOf(
    "channelName" to channelName,
    "role" to "publisher",
    "uid" to 0
)

val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()

val token = (result.data as? Map<*, *>)?.get("token") as? String
```

---

### 2. generateStreamerToken
**Propósito:** Generar token para streamers (simplificado)

**Características:**
- ✅ Requiere autenticación
- ✅ Genera UID automáticamente
- ✅ Rol: PUBLISHER

**Uso desde Kotlin:**
```kotlin
val functions = Firebase.functions
val data = hashMapOf("channelName" to channelName)

val result = functions
    .getHttpsCallable("generateStreamerToken")
    .call(data)
    .await()
```

---

### 3. generateViewerToken
**Propósito:** Generar token para espectadores (simplificado)

**Características:**
- ✅ Requiere autenticación
- ✅ Genera UID automáticamente
- ✅ Rol: SUBSCRIBER

**Uso desde Kotlin:**
```kotlin
val functions = Firebase.functions
val data = hashMapOf("channelName" to channelName)

val result = functions
    .getHttpsCallable("generateViewerToken")
    .call(data)
    .await()
```

---

## 🚀 Cómo Desplegar

### Opción 1: Script Automatizado (Recomendado)
```bash
.\desplegar-functions.bat
```

### Opción 2: Comandos Manuales
```bash
# 1. Instalar dependencias
cd functions
npm install
cd ..

# 2. Desplegar
firebase deploy --only functions

# 3. Verificar
firebase functions:list
```

---

## ✅ Verificación

### Antes de Desplegar
- [x] Funciones implementadas en `functions/index.js`
- [x] Dependencias en `functions/package.json`
- [x] Credenciales de Agora configuradas
- [x] Scripts de despliegue creados

### Después de Desplegar
- [ ] Ejecutar `firebase deploy --only functions`
- [ ] Verificar con `firebase functions:list`
- [ ] Probar desde Firebase Console
- [ ] Probar desde la app

---

## 🔐 Seguridad

### ✅ Implementado Correctamente
- ✅ App ID en el cliente (público)
- ✅ App Certificate SOLO en Cloud Functions (privado)
- ✅ Tokens generados en el backend
- ✅ Tokens con expiración (1 hora)

### ❌ Nunca Hacer
- ❌ Poner App Certificate en el código del cliente
- ❌ Generar tokens en el cliente
- ❌ Compartir tokens entre usuarios

---

## 📊 Integración con la App

Las funciones ya están siendo llamadas desde:

### 1. LiveViewModel.kt
```kotlin
// Al iniciar Live
val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()
```

### 2. FirebaseManager.kt
```kotlin
// En startNewLiveSession()
val result = functions
    .getHttpsCallable("generateAgoraToken")
    .call(data)
    .await()
```

---

## 🧪 Cómo Probar

### 1. Probar Localmente (Opcional)
```bash
.\probar-functions-local.bat
```

### 2. Probar en Firebase Console
1. Ve a Firebase Console → Functions
2. Selecciona `generateAgoraToken`
3. Haz clic en "Test function"
4. Ingresa datos de prueba
5. Verifica que retorna un token

### 3. Probar desde la App
```kotlin
// Agregar en cualquier parte para probar
fun testToken() {
    val functions = Firebase.functions
    val data = hashMapOf(
        "channelName" to "test",
        "role" to "publisher",
        "uid" to 0
    )
    
    functions
        .getHttpsCallable("generateAgoraToken")
        .call(data)
        .addOnSuccessListener { result ->
            val token = (result.data as? Map<*, *>)?.get("token")
            android.util.Log.d("Test", "✅ Token: $token")
        }
        .addOnFailureListener { e ->
            android.util.Log.e("Test", "❌ Error: ${e.message}")
        }
}
```

---

## 📈 Monitoreo

### Ver Logs en Tiempo Real
```bash
firebase functions:log
```

### Ver Logs en Firebase Console
1. Firebase Console → Functions
2. Selecciona una función
3. Ve a la pestaña "Logs"

### Métricas
1. Firebase Console → Functions
2. Selecciona una función
3. Ve a la pestaña "Usage"

---

## 🐛 Solución de Problemas

### Error: "Function not found"
**Causa:** Las funciones no están desplegadas
**Solución:** `firebase deploy --only functions`

### Error: "Invalid token"
**Causa:** Token expirado (duran 1 hora)
**Solución:** Generar un nuevo token

### Error: "Permission denied"
**Causa:** Usuario no autenticado
**Solución:** Verificar que el usuario esté logueado en Firebase Auth

### Error: "Channel name required"
**Causa:** Falta el parámetro `channelName`
**Solución:** Asegurarse de pasar todos los parámetros requeridos

---

## 💰 Costos

### Plan Gratuito (Spark)
- ✅ 2M invocaciones/mes
- ✅ 400K GB-segundos/mes
- ✅ 200K CPU-segundos/mes

### Estimación para tu App
- Generar token: ~100ms
- Costo por token: ~$0.000001
- 1000 Lives/mes: ~$0.001 (prácticamente gratis)

---

## 🎯 Próximos Pasos

### Paso 3: Integrar Catálogo de Lives
1. Crear pantalla de Lives activos
2. Mostrar lista de transmisiones
3. Al hacer clic, obtener token de viewer
4. Navegar a `LiveStreamViewerScreen`

### Paso 4: Pruebas en Dispositivos Reales
1. Probar transmisión en dispositivo 1
2. Probar visualización en dispositivo 2
3. Verificar contador de espectadores
4. Verificar calidad de video/audio

---

## 📋 Checklist Final

- [x] Funciones implementadas
- [x] Dependencias configuradas
- [x] Scripts de despliegue creados
- [x] Documentación completa
- [ ] Funciones desplegadas a Firebase
- [ ] Funciones probadas desde Console
- [ ] Funciones probadas desde la app
- [ ] Logs monitoreados

---

## 🎉 Estado Actual

**Paso 2: COMPLETADO** ✅

Las Cloud Functions están listas para ser desplegadas. Solo necesitas ejecutar:

```bash
.\desplegar-functions.bat
```

O manualmente:

```bash
firebase deploy --only functions
```

Una vez desplegadas, tu sistema de Live Streaming estará completamente funcional.

---

## 📚 Recursos Adicionales

- [Documentación de Agora](https://docs.agora.io/)
- [Firebase Functions](https://firebase.google.com/docs/functions)
- [Agora Token Generator](https://github.com/AgoraIO/Tools/tree/master/DynamicKey/AgoraDynamicKey)

---

**Siguiente:** Desplegar las funciones y probar el flujo completo de Live Streaming 🚀
