@echo off
echo ========================================
echo VERIFICACION Y DESPLIEGUE DE CLOUD FUNCTIONS
echo ========================================
echo.

echo [1/4] Verificando instalacion de Firebase CLI...
firebase --version
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Firebase CLI no esta instalado
    echo Instala Firebase CLI con: npm install -g firebase-tools
    pause
    exit /b 1
)
echo OK - Firebase CLI instalado
echo.

echo [2/4] Verificando funciones desplegadas...
firebase functions:list
echo.

echo [3/4] Instalando dependencias de Cloud Functions...
cd functions
call npm install
if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudieron instalar las dependencias
    cd ..
    pause
    exit /b 1
)
cd ..
echo OK - Dependencias instaladas
echo.

echo [4/4] Desplegando Cloud Functions...
firebase deploy --only functions
if %errorlevel% neq 0 (
    echo.
    echo ERROR: No se pudieron desplegar las funciones
    echo Verifica que estes logueado con: firebase login
    pause
    exit /b 1
)
echo.

echo ========================================
echo DESPLIEGUE COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Las siguientes funciones estan disponibles:
echo - generateAgoraToken
echo - generateStreamerToken
echo - generateViewerToken
echo.
echo Ahora puedes probar el Live en la app!
echo.
pause
