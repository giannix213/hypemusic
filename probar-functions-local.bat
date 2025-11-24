@echo off
echo ========================================
echo   PROBAR CLOUD FUNCTIONS LOCALMENTE
echo ========================================
echo.

echo [1/3] Verificando Firebase CLI...
call firebase --version
if errorlevel 1 (
    echo.
    echo ERROR: Firebase CLI no esta instalado
    echo Instala Firebase CLI con: npm install -g firebase-tools
    pause
    exit /b 1
)

echo.
echo [2/3] Instalando dependencias...
cd functions
call npm install
if errorlevel 1 (
    echo.
    echo ERROR: No se pudieron instalar las dependencias
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo [3/3] Iniciando emuladores de Firebase...
echo.
echo NOTA: Las funciones estaran disponibles en:
echo   http://localhost:5001/hypematch-d8e0f/us-central1/generateAgoraToken
echo.
echo Presiona Ctrl+C para detener los emuladores
echo.
call firebase emulators:start --only functions

pause
