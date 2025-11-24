# 🚀 Guía Rápida: Desplegar Cloud Function de Agora

## ✅ Tu App ID ya está configurado

- App ID: `72117baf2c874766b556e6f83ac9c58d`
- App Certificate: `f907826ae8ff4c00b7057d15b6f2e628`

## 📋 Pasos para Desplegar

### 1. Instalar Firebase CLI (si no lo tienes)

```bash
npm install -g firebase-tools
```

### 2. Iniciar sesión en Firebase

```bash
firebase login
```

### 3. Inicializar Functions en tu proyecto

```bash
cd C:\Users\Freddy\HypeMatch
firebase init functions
```

**Responde:**
- ¿Qué lenguaje? → **JavaScript**
- ¿Instalar dependencias? → **Sí**

### 4. Instalar dependencia de Agora

```bash
cd functions
npm install agora-access-token
```

### 5. Copiar el código de la función

**Opción A: Copiar archivo completo**

Copia el contenido de `functions_index.js` (que ya tiene tu App ID configurado) a `functions/index.js`

**Opción B: Copiar manualmente**

Abre `functions/index.js` y reemplaza todo el contenido con el código de `functions_index.js`

### 6. Desplegar a Firebase

```bash
firebase deploy --only functions
```

Espera a que termine (puede tomar 1-2 minutos).

### 7. Verificar en Firebase Console

1. Ve a https://console.firebase.google.com/
2. Selecciona tu proyecto
3. Ve a **Functions**
4. Deberías ver 3 funciones:
   - `generateAgoraToken`
   - `generateStreamerToken`
   - `generateViewerToken`

## 🧪 Probar la función

En Firebase Console > Functions, haz clic en `generateStreamerToken` y prueba con:

```json
{
  "channelName": "test_channel_123"
}
```

Deberías recibir:
```json
{
  "token": "006...",
  "expiresAt": 1234567890,
  "channelName": "test_channel_123",
  "uid": 123456
}
```

## 🔧 Actualizar FirebaseManager.kt

Ahora que la Cloud Function está desplegada, actualiza `FirebaseManager.kt`:

### Busca la función `startNewLiveSession()`

Encuentra esta línea:
```kotlin
val agoraToken = "TEMP_TOKEN_${System.currentTimeMillis()}" // Token temporal
```

### Reemplázala con:

```kotlin
// Llamar a Cloud Function para obtener token de Agora
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

### También elimina esta línea de advertencia:

```kotlin
android.util.Log.w("FirebaseManager", "⚠️ USANDO TOKEN TEMPORAL - Implementa Cloud Function para producción")
```

## ✅ Checklist Final

- [ ] Firebase CLI instalado
- [ ] Sesión iniciada con `firebase login`
- [ ] Functions inicializadas
- [ ] Dependencia `agora-access-token` instalada
- [ ] Código copiado a `functions/index.js`
- [ ] Funciones desplegadas con `firebase deploy`
- [ ] Funciones visibles en Firebase Console
- [ ] Función probada y funcionando
- [ ] `FirebaseManager.kt` actualizado
- [ ] App sincronizada y compilada

## 🎉 ¡Listo!

Una vez completados estos pasos, tu app podrá:
1. Crear sesiones de Live
2. Obtener tokens reales de Agora
3. Transmitir en vivo

## 🐛 Troubleshooting

### Error: "firebase: command not found"
```bash
npm install -g firebase-tools
```

### Error: "Permission denied"
```bash
# En Windows, ejecuta PowerShell como Administrador
# En Mac/Linux:
sudo npm install -g firebase-tools
```

### Error al desplegar
```bash
# Verifica que estés en el directorio correcto
cd C:\Users\Freddy\HypeMatch

# Verifica que functions/ exista
dir functions

# Intenta de nuevo
firebase deploy --only functions
```

### La función no aparece en Console
- Espera 1-2 minutos después del deploy
- Refresca la página de Firebase Console
- Verifica que no haya errores en los logs

## 📞 Siguiente Paso

Una vez desplegada la Cloud Function, actualiza `FirebaseManager.kt` y ¡estarás listo para transmitir en vivo! 🎥
