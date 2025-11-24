# ⚡ COMANDOS RÁPIDOS - Live Streaming

## 🔨 COMPILAR Y DESPLEGAR

### Compilar APK
```bash
gradlew assembleDebug
```

### Instalar en dispositivo conectado
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Instalar en ambos dispositivos (si hay 2 conectados)
```bash
# Listar dispositivos
adb devices

# Instalar en dispositivo específico
adb -s [DEVICE_ID] install -r app\build\outputs\apk\debug\app-debug.apk
```

## 📊 VER LOGS EN TIEMPO REAL

### Logs del Emisor (cuando inicia Live)
```bash
adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D -v time
```

### Logs del Espectador (cuando busca Lives)
```bash
adb logcat -s FirebaseManager:D LiveScreenNew:D -v time
```

### Logs completos de Live
```bash
adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D LiveScreenNew:D LiveStreamViewerScreen:D -v time
```

### Limpiar logs y empezar de nuevo
```bash
adb logcat -c
adb logcat -s FirebaseManager:D LiveLauncher:D -v time
```

## 🔍 VERIFICAR ESTADO

### Ver si hay Lives activos en Firebase
```bash
# Abrir Firebase Console
start https://console.firebase.google.com/project/_/firestore/data/live_sessions
```

### Ver logs de Cloud Functions
```bash
cd functions
firebase functions:log --only generateAgoraToken
```

### Listar Cloud Functions desplegadas
```bash
cd functions
firebase functions:list
```

## 🐛 DEBUGGING

### Ver logs filtrados por palabra clave

**Buscar "INICIANDO NUEVA SESIÓN":**
```bash
adb logcat | findstr "INICIANDO NUEVA SESIÓN"
```

**Buscar "Lives encontrados":**
```bash
adb logcat | findstr "Lives encontrados"
```

**Buscar errores:**
```bash
adb logcat *:E
```

### Ver logs de un dispositivo específico
```bash
# Listar dispositivos
adb devices

# Ver logs de dispositivo específico
adb -s [DEVICE_ID] logcat -s FirebaseManager:D
```

## 🔄 REINICIAR APP

### Forzar cierre y reinicio
```bash
# Cerrar app
adb shell am force-stop com.metu.hypematch

# Abrir app
adb shell am start -n com.metu.hypematch/.MainActivity
```

## 🧹 LIMPIAR Y RECOMPILAR

### Limpiar proyecto
```bash
gradlew clean
```

### Limpiar y compilar
```bash
gradlew clean assembleDebug
```

### Limpiar caché de Gradle
```bash
gradlew clean --no-daemon
```

## 📱 GESTIÓN DE DISPOSITIVOS

### Listar dispositivos conectados
```bash
adb devices -l
```

### Reiniciar ADB
```bash
adb kill-server
adb start-server
adb devices
```

### Ver información del dispositivo
```bash
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
```

## 🎬 FLUJO DE PRUEBA RÁPIDO

### Terminal 1 (Compilar e instalar)
```bash
gradlew assembleDebug && adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Terminal 2 (Logs del Emisor)
```bash
adb logcat -c
adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D -v time
```

### Terminal 3 (Logs del Espectador - si hay 2 dispositivos)
```bash
adb -s [DEVICE_2_ID] logcat -c
adb -s [DEVICE_2_ID] logcat -s FirebaseManager:D LiveScreenNew:D -v time
```

## 🔑 BUSCAR INFORMACIÓN ESPECÍFICA

### Buscar sessionId en logs
```bash
adb logcat | findstr "sessionId"
```

### Buscar token de Agora
```bash
adb logcat | findstr "Token de Agora"
```

### Buscar errores de Firebase
```bash
adb logcat | findstr "FirebaseManager.*Error"
```

### Buscar Lives encontrados
```bash
adb logcat | findstr "Lives encontrados"
```

## 📋 CHECKLIST DE VERIFICACIÓN

```bash
# 1. Compilar
gradlew assembleDebug

# 2. Instalar
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 3. Limpiar logs
adb logcat -c

# 4. Iniciar logs
adb logcat -s FirebaseManager:D LiveLauncher:D -v time

# 5. Abrir app y probar
# (Hacer manualmente en el dispositivo)

# 6. Verificar Firebase Console
start https://console.firebase.google.com/project/_/firestore/data/live_sessions
```

## 🚨 COMANDOS DE EMERGENCIA

### Si la app no responde
```bash
adb shell am force-stop com.metu.hypematch
adb shell am start -n com.metu.hypematch/.MainActivity
```

### Si ADB no detecta dispositivos
```bash
adb kill-server
adb start-server
adb devices
```

### Si hay problemas de permisos
```bash
adb shell pm grant com.metu.hypematch android.permission.CAMERA
adb shell pm grant com.metu.hypematch android.permission.RECORD_AUDIO
```

## 📞 SCRIPT TODO-EN-UNO

Crear archivo `probar-live.bat`:
```batch
@echo off
echo Compilando...
call gradlew assembleDebug
if %ERRORLEVEL% NEQ 0 exit /b 1

echo Instalando...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo Limpiando logs...
adb logcat -c

echo Iniciando logs...
adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D -v time
```

Ejecutar:
```bash
probar-live.bat
```
