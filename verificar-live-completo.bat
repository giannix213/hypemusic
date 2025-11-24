@echo off
echo ========================================
echo VERIFICACION COMPLETA DE LIVE STREAMING
echo ========================================
echo.

echo [1/5] Verificando compilacion...
echo.
call gradlew assembleDebug --quiet
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error en compilacion
    pause
    exit /b 1
)
echo ✅ Compilacion exitosa
echo.

echo [2/5] Verificando Cloud Functions...
echo.
cd functions
call firebase functions:list
cd ..
echo.

echo [3/5] Verificando Firestore (live_sessions)...
echo.
echo Abriendo Firebase Console...
start https://console.firebase.google.com/project/_/firestore/data/live_sessions
echo.

echo [4/5] Iniciando logs en tiempo real...
echo.
echo Presiona Ctrl+C para detener los logs
echo.
echo === LOGS DEL EMISOR (Busca: "INICIANDO NUEVA SESIÓN DE LIVE") ===
adb logcat -s FirebaseManager:D LiveLauncher:D LiveRecording:D
echo.

pause
