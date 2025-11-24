@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 SUBIR PROYECTO A GITHUB
echo ========================================
echo.

echo Este script te ayudará a subir tu proyecto a GitHub
echo.

REM Verificar si Git está instalado
git --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Git no está instalado
    echo.
    echo Descarga Git desde: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo ✅ Git está instalado
echo.

REM Verificar si ya es un repositorio Git
if exist .git (
    echo ℹ️  Este proyecto ya es un repositorio Git
    echo.
    goto :update_repo
) else (
    echo ℹ️  Este proyecto NO es un repositorio Git todavía
    echo.
    goto :init_repo
)

:init_repo
echo ========================================
echo CONFIGURACIÓN INICIAL
echo ========================================
echo.

REM Verificar configuración de Git
git config user.name >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Necesitas configurar Git primero
    echo.
    set /p username="Ingresa tu nombre: "
    set /p email="Ingresa tu email: "
    
    git config --global user.name "%username%"
    git config --global user.email "%email%"
    
    echo ✅ Git configurado
    echo.
)

echo [1/5] Inicializando repositorio Git...
git init
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al inicializar Git
    pause
    exit /b 1
)
echo ✅ Repositorio inicializado
echo.

echo [2/5] Creando .gitignore...
if not exist .gitignore (
    (
        echo # Android
        echo *.iml
        echo .gradle
        echo /local.properties
        echo /.idea/
        echo .DS_Store
        echo /build
        echo /captures
        echo .externalNativeBuild
        echo .cxx
        echo *.apk
        echo *.ap_
        echo *.dex
        echo.
        echo # Firebase
        echo google-services.json
        echo firebase-adminsdk-*.json
        echo.
        echo # Agora
        echo **/AgoraConfig.kt
        echo.
        echo # Claves
        echo *.keystore
        echo *.jks
        echo key.properties
        echo.
        echo # Logs
        echo *.log
        echo.
        echo # Gradle
        echo .gradle/
        echo build/
    ) > .gitignore
    echo ✅ .gitignore creado
) else (
    echo ℹ️  .gitignore ya existe
)
echo.

echo [3/5] Agregando archivos...
git add .
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al agregar archivos
    pause
    exit /b 1
)
echo ✅ Archivos agregados
echo.

echo [4/5] Haciendo commit inicial...
git commit -m "Initial commit: App con Live Streaming funcional"
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al hacer commit
    pause
    exit /b 1
)
echo ✅ Commit realizado
echo.

echo [5/5] Conectando con GitHub...
echo.
echo ========================================
echo IMPORTANTE: Crea el repositorio en GitHub primero
echo ========================================
echo.
echo 1. Ve a https://github.com/new
echo 2. Nombre: hypematch-app (o el que prefieras)
echo 3. NO marques "Initialize with README"
echo 4. Click "Create repository"
echo 5. Copia la URL del repositorio
echo.
set /p repo_url="Pega la URL del repositorio (https://github.com/...): "

git remote add origin %repo_url%
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al conectar con GitHub
    pause
    exit /b 1
)

git branch -M main
git push -u origin main
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al subir a GitHub
    echo.
    echo Posibles causas:
    echo - No tienes permisos en el repositorio
    echo - La URL es incorrecta
    echo - Necesitas autenticarte
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ PROYECTO SUBIDO EXITOSAMENTE
echo ========================================
echo.
echo Tu proyecto está ahora en GitHub!
echo URL: %repo_url%
echo.
echo Próximos pasos:
echo 1. Agrega colaboradores en Settings ^> Collaborators
echo 2. Comparte las credenciales de Firebase/Agora por mensaje privado
echo 3. Crea un README.md con instrucciones de setup
echo.
pause
exit /b 0

:update_repo
echo ========================================
echo ACTUALIZAR REPOSITORIO EXISTENTE
echo ========================================
echo.

echo [1/4] Verificando cambios...
git status
echo.

set /p continue="¿Quieres subir estos cambios? (S/N): "
if /i not "%continue%"=="S" (
    echo Operación cancelada
    pause
    exit /b 0
)

echo [2/4] Agregando cambios...
git add .
echo ✅ Cambios agregados
echo.

echo [3/4] Haciendo commit...
set /p commit_msg="Mensaje del commit: "
git commit -m "%commit_msg%"
if %ERRORLEVEL% NEQ 0 (
    echo ℹ️  No hay cambios para hacer commit
    pause
    exit /b 0
)
echo ✅ Commit realizado
echo.

echo [4/4] Subiendo a GitHub...
git push
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error al subir cambios
    echo.
    echo Intenta primero: git pull
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ CAMBIOS SUBIDOS EXITOSAMENTE
echo ========================================
echo.
pause
exit /b 0
