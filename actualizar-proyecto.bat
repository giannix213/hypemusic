@echo off
chcp 65001 >nul
echo ========================================
echo 📥 DESCARGAR CAMBIOS DE GITHUB
echo ========================================
echo.
echo Descargando últimos cambios de tu hermana...
echo.

git pull origin master

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Proyecto actualizado correctamente
    echo.
    echo Ya puedes trabajar en Android Studio
) else (
    echo.
    echo ❌ Hubo un problema al actualizar
    echo.
    echo Posibles soluciones:
    echo 1. Verifica tu conexión a internet
    echo 2. Si tienes cambios sin guardar, primero súbelos
    echo 3. Contacta a tu hermana si el problema persiste
)

echo.
pause
