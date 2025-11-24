@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 DESPLEGAR CONFIGURACIÓN COMPLETA DE LIVE
echo ========================================
echo.

echo Este script desplegará:
echo 1. Cloud Functions (generateAgoraToken)
echo 2. Firestore Rules (lectura pública para Lives)
echo.

set /p confirm="¿Continuar? (S/N): "
if /i not "%confirm%"=="S" (
    echo Operación cancelada
    pause
    exit /b 0
)

echo.
echo [1/2] 📤 Desplegando Cloud Functions...
echo.
cd functions
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error instalando dependencias
    cd ..
    pause
    exit /b 1
)

call firebase deploy --only functions
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error desplegando functions
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo ✅ Cloud Functions desplegadas
echo.

echo [2/2] 🔐 Desplegando Firestore Rules...
echo.
call firebase deploy --only firestore:rules
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error desplegando rules
    pause
    exit /b 1
)

echo.
echo ✅ Firestore Rules desplegadas
echo.

echo ========================================
echo ✅ DESPLIEGUE COMPLETADO
echo ========================================
echo.
echo Cambios aplicados:
echo 1. ✅ Cloud Function: generateAgoraToken
echo    - Emisor: token con role="publisher" (2 horas)
echo    - Espectador: token con role="subscriber" (1 hora)
echo.
echo 2. ✅ Firestore Rules: live_sessions
echo    - Lectura: Pública (allow read: if true)
echo    - Escritura: Solo autenticados
echo.
echo Próximo paso:
echo 1. Compilar app: gradlew assembleDebug
echo 2. Instalar: adb install -r app\build\outputs\apk\debug\app-debug.apk
echo 3. Probar Live en ambos dispositivos
echo.
pause
