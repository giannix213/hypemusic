@echo off
chcp 65001 >nul
echo ========================================
echo 💾 GUARDAR Y SUBIR CAMBIOS
echo ========================================
echo.

echo Cambios actuales:
echo.
git status --short
echo.

set /p mensaje="Describe qué cambiaste: "

if "%mensaje%"=="" (
    echo ❌ Necesitas escribir un mensaje
    pause
    exit /b 1
)

echo.
echo [1/3] Guardando cambios...
git add .

echo [2/3] Creando commit...
git commit -m "%mensaje%"

echo [3/3] Subiendo a GitHub...
git push origin master

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✅ CAMBIOS SUBIDOS CORRECTAMENTE
    echo ========================================
    echo.
    echo Tu hermana ya puede ver tus cambios
) else (
    echo.
    echo ❌ Error al subir cambios
    echo.
    echo Intenta primero: actualizar-proyecto.bat
)

echo.
pause
