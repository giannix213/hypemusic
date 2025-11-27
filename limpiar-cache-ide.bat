@echo off
echo ========================================
echo LIMPIANDO CACHE DEL IDE Y PROYECTO
echo ========================================
echo.

echo [1/4] Limpiando cache de Gradle...
call gradlew clean
echo.

echo [2/4] Eliminando carpetas de cache...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"
if exist ".idea\caches" rmdir /s /q ".idea\caches"
echo.

echo [3/4] Invalidando cache de Android Studio...
echo Por favor, en Android Studio:
echo 1. Ve a File ^> Invalidate Caches / Restart
echo 2. Selecciona "Invalidate and Restart"
echo.

echo [4/4] Reconstruyendo proyecto...
call gradlew build --refresh-dependencies
echo.

echo ========================================
echo LIMPIEZA COMPLETADA
echo ========================================
echo.
echo Ahora reinicia Android Studio para aplicar los cambios.
pause
