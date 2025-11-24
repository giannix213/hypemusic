@echo off
chcp 65001 >nul
echo ========================================
echo 🎬 PRUEBA RÁPIDA DE LIVE STREAMING
echo ========================================
echo.

echo [1/4] 🔨 Compilando APK...
call gradlew assembleDebug --quiet
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error en compilación
    pause
    exit /b 1
)
echo ✅ Compilación exitosa
echo.

echo [2/4] 📱 Instalando en dispositivo...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error en instalación
    echo.
    echo Verifica que el dispositivo esté conectado:
    adb devices
    pause
    exit /b 1
)
echo ✅ Instalación exitosa
echo.

echo [3/4] 🧹 Limpiando logs anteriores...
adb logcat -c
echo ✅ Logs limpiados
echo.

echo [4/4] 📊 Iniciando monitoreo de logs...
echo.
echo ========================================
echo INSTRUCCIONES:
echo 1. Abre la app en el dispositivo
echo 2. Ve a la pantalla de Lives
echo 3. Presiona "Iniciar Live"
echo 4. Observa los logs abajo
echo.
echo BUSCA ESTOS MENSAJES:
echo   ✅ "INICIANDO NUEVA SESIÓN DE LIVE"
echo   ✅ "Token de Agora recibido"
echo   ✅ "Sesión creada en Firestore"
echo.
echo Presiona Ctrl+C para detener
echo ========================================
echo.

adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D -v time
