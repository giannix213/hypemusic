@echo off
echo ========================================
echo   LOGS DE LIVE STREAMING - DIAGNOSTICO
echo ========================================
echo.
echo Conectando dispositivos...
echo.

adb devices

echo.
echo ========================================
echo   CAPTURANDO LOGS EN TIEMPO REAL
echo ========================================
echo.
echo Presiona Ctrl+C para detener
echo.
echo INSTRUCCIONES:
echo 1. Deja esta ventana abierta
echo 2. En el dispositivo 1: Inicia un Live
echo 3. En el dispositivo 2: Intenta ver el Live
echo 4. Observa los logs aqui
echo.
echo ========================================
echo.

adb logcat -c
adb logcat | findstr /I "LiveScreen LiveRecording LiveViewer LiveLauncher Agora FirebaseManager live_sessions Token"

pause
