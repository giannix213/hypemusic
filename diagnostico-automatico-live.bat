@echo off
color 0A
echo ========================================
echo   DIAGNOSTICO AUTOMATICO - LIVE STREAMING
echo ========================================
echo.

echo [1/5] Verificando dispositivos conectados...
echo.
adb devices
echo.

set /p continuar="Hay dispositivos conectados? (S/N): "
if /i "%continuar%" NEQ "S" (
    echo.
    echo ERROR: Conecta al menos un dispositivo
    pause
    exit /b 1
)

echo.
echo [2/5] Verificando Cloud Functions...
echo.
firebase functions:list
echo.

echo [3/5] Verificando Firestore...
echo.
echo Abriendo Firebase Console en el navegador...
start https://console.firebase.google.com/project/hype-13966/firestore/databases/-default-/data/~2Flive_sessions
echo.
echo Verifica en el navegador:
echo   - Existe la coleccion "live_sessions"?
echo   - Hay documentos con isActive = true?
echo.
set /p firestore_ok="Firestore se ve correcto? (S/N): "

echo.
echo [4/5] Capturando logs en tiempo real...
echo.
echo INSTRUCCIONES:
echo 1. Deja esta ventana abierta
echo 2. En dispositivo 1: Inicia un Live
echo 3. Observa los logs aqui
echo 4. Presiona Ctrl+C cuando termines
echo.
echo Presiona cualquier tecla para iniciar captura de logs...
pause > nul
echo.
echo ========================================
echo   LOGS EN TIEMPO REAL
echo ========================================
echo.

adb logcat -c
adb logcat | findstr /I "LiveScreen LiveRecording LiveViewer LiveLauncher Agora FirebaseManager live_sessions Token createLiveSession getActiveLiveSessions"

echo.
echo ========================================
echo   DIAGNOSTICO COMPLETADO
echo ========================================
echo.
echo Revisa los logs arriba para identificar el problema.
echo.
echo PROBLEMAS COMUNES:
echo.
echo 1. Si NO ves "Creando sesion de Live":
echo    - createLiveSession no se esta llamando
echo    - Problema en LiveScreenNew.kt
echo.
echo 2. Si NO ves "Sesion de Live creada":
echo    - Error guardando en Firestore
echo    - Verifica permisos de Firebase
echo.
echo 3. Si NO ves "Canal unido exitosamente":
echo    - Token no generado correctamente
echo    - Problema con Cloud Functions
echo.
echo 4. Si dispositivo 2 NO ve Lives:
echo    - getActiveLiveSessions no funciona
echo    - Verifica query de Firestore
echo.
pause
