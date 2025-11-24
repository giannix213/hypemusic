@echo off
echo ========================================
echo   DESPLEGAR CLOUD FUNCTIONS A FIREBASE
echo ========================================
echo.

echo [1/4] Verificando Firebase CLI...
call firebase --version
if errorlevel 1 (
    echo.
    echo ERROR: Firebase CLI no esta instalado
    echo Instala Firebase CLI con: npm install -g firebase-tools
    pause
    exit /b 1
)

echo.
echo [2/4] Instalando dependencias...
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
echo [3/4] Desplegando funciones a Firebase...
call firebase deploy --only functions
if errorlevel 1 (
    echo.
    echo ERROR: No se pudieron desplegar las funciones
    echo Verifica que estes autenticado: firebase login
    pause
    exit /b 1
)

echo.
echo [4/4] Listando funciones desplegadas...
call firebase functions:list

echo.
echo ========================================
echo   DESPLIEGUE COMPLETADO EXITOSAMENTE
echo ========================================
echo.
echo Funciones disponibles:
echo   - generateAgoraToken
echo   - generateStreamerToken
echo   - generateViewerToken
echo.
echo Para ver los logs en tiempo real:
echo   firebase functions:log
echo.
pause
