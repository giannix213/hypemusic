@echo off
chcp 65001 >nul
echo ========================================
echo 🧹 LIMPIAR CACHE DE ANDROID STUDIO
echo ========================================
echo.

echo [1/3] Limpiando build...
call gradlew clean

echo.
echo [2/3] Invalidando cache...
echo IMPORTANTE: Después de esto, en Android Studio:
echo 1. Ve a: File ^> Invalidate Caches
echo 2. Marca: "Clear file system cache and Local History"
echo 3. Click en "Invalidate and Restart"
echo.

echo [3/3] Sincronizando Gradle...
echo En Android Studio: File ^> Sync Project with Gradle Files
echo.

echo ✅ Listo
echo.
pause
