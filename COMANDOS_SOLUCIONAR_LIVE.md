# 🚀 Comandos para Solucionar el Error de Live

## Ejecuta estos comandos en orden:

### 1. Verificar si Firebase CLI está instalado
```bash
firebase --version
```

**Si no está instalado:**
```bash
npm install -g firebase-tools
```

### 2. Verificar que estés logueado
```bash
firebase login
```

### 3. Verificar funciones actuales
```bash
firebase functions:list
```

### 4. Instalar dependencias de Cloud Functions
```bash
cd functions
npm install
cd ..
```

### 5. Desplegar Cloud Functions
```bash
firebase deploy --only functions
```

**Espera a que termine (2-3 minutos)**

### 6. Verificar que se desplegaron correctamente
```bash
firebase functions:list
```

Deberías ver:
- ✅ generateAgoraToken
- ✅ generateStreamerToken  
- ✅ generateViewerToken

### 7. Rebuild de la app

**Opción A - Desde Android Studio:**
1. Build > Clean Project
2. Build > Rebuild Project

**Opción B - Desde terminal:**
```bash
gradlew clean
gradlew build
```

### 8. Ejecutar la app y probar

1. Abre la app en el emulador/dispositivo
2. Ve a la pestaña "Live"
3. Intenta iniciar una transmisión
4. Revisa los logs en Logcat (filtra por `FirebaseManager`)

## 🎯 Script Automático

También puedes usar el script que creé:

```bash
verificar-y-desplegar-functions.bat
```

Este script hace todo automáticamente.

## 📋 Logs Esperados

Si todo funciona, en Logcat verás:

```
D/FirebaseManager: 🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
D/FirebaseManager: 👤 Usuario: [tu_nombre] ([tu_id])
D/FirebaseManager: 📺 Canal generado: live_[userId]_[timestamp]
D/FirebaseManager: 🔑 Solicitando token de Agora...
D/FirebaseManager: ✅ Token de Agora recibido: [token]...
D/FirebaseManager: 💾 Creando documento en Firestore...
D/FirebaseManager: ✅ Sesión creada en Firestore: [sessionId]
D/FirebaseManager: ✅ ===== SESIÓN DE LIVE LISTA =====
```

## ❌ Si Ves Errores

### Error: "command not found: firebase"
**Solución:** Instala Firebase CLI
```bash
npm install -g firebase-tools
```

### Error: "Not logged in"
**Solución:** Loguéate
```bash
firebase login
```

### Error: "Permission denied"
**Solución:** Verifica tu proyecto de Firebase
```bash
firebase use --add
```
Selecciona tu proyecto de la lista

### Error: "Module not found: agora-access-token"
**Solución:** Instala las dependencias
```bash
cd functions
npm install agora-access-token
cd ..
firebase deploy --only functions
```

## ✅ Verificación Final

Después de ejecutar todos los comandos:

1. ✅ `firebase functions:list` muestra las 3 funciones
2. ✅ La app se rebuildeó sin errores
3. ✅ Al intentar iniciar Live, ves los logs de éxito
4. ✅ La cámara se activa y puedes transmitir

---

**Si después de esto sigue sin funcionar, comparte:**
1. El output de `firebase functions:list`
2. Los logs de Logcat (filtra por `FirebaseManager`)
3. Cualquier mensaje de error que veas
