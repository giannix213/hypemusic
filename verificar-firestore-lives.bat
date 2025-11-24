@echo off
echo ========================================
echo   VERIFICAR LIVES EN FIRESTORE
echo ========================================
echo.
echo Abriendo Firebase Console...
echo.
echo 1. Ve a Firestore Database
echo 2. Busca la coleccion: live_sessions
echo 3. Verifica si hay documentos con isActive = true
echo.
echo Presiona cualquier tecla para abrir Firebase Console...
pause > nul

start https://console.firebase.google.com/project/hype-13966/firestore/databases/-default-/data/~2Flive_sessions

echo.
echo ========================================
echo   QUE VERIFICAR EN FIRESTORE:
echo ========================================
echo.
echo [✓] Existe la coleccion "live_sessions"?
echo [✓] Hay documentos cuando transmites?
echo [✓] El campo "isActive" esta en true?
echo [✓] El campo "agoraChannelName" tiene valor?
echo [✓] El campo "username" tiene tu nombre?
echo.
echo Si NO ves documentos cuando transmites:
echo   - El problema esta en createLiveSession
echo   - Revisa los logs con: ver-logs-live-completo.bat
echo.
echo Si SI ves documentos pero el otro dispositivo no los ve:
echo   - El problema esta en getActiveLiveSessions
echo   - Revisa los logs del dispositivo 2
echo.
pause
