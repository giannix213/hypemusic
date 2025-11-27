@echo off
echo ========================================
echo SINCRONIZANDO PROYECTO ANDROID
echo ========================================
echo.

echo Paso 1: Limpiando proyecto...
call gradlew.bat clean

echo.
echo Paso 2: Sincronizando con Gradle...
call gradlew.bat --refresh-dependencies

echo.
echo ========================================
echo SINCRONIZACION COMPLETADA
echo ========================================
echo.
echo Ahora abre Android Studio y:
echo 1. File ^> Sync Project with Gradle Files
echo 2. Build ^> Rebuild Project
echo.
pause
