@echo off
echo ========================================
echo   LIMPIEZA DE VIDEOS DE CONCURSOS
echo ========================================
echo.
echo Este script eliminara TODOS los videos
echo de la coleccion contest_entries en Firestore.
echo.
echo ADVERTENCIA: Esta accion NO se puede deshacer.
echo.
pause

echo.
echo Verificando Node.js...
node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js no esta instalado
    echo Por favor instala Node.js desde https://nodejs.org
    pause
    exit /b 1
)

echo Node.js encontrado: 
node --version

echo.
echo Verificando firebase-admin...
npm list firebase-admin >nul 2>&1
if errorlevel 1 (
    echo.
    echo firebase-admin no esta instalado.
    echo Instalando firebase-admin...
    npm install firebase-admin
)

echo.
echo Ejecutando script de limpieza...
echo.
node limpiar-videos-concursos.js

echo.
echo ========================================
echo   PROCESO COMPLETADO
echo ========================================
pause
