@echo off
echo ========================================
echo   Logs de Verificacion de Email
echo ========================================
echo.
echo Presiona Ctrl+C para detener
echo.
adb logcat -s AuthManager:D FirebaseAuth:D
