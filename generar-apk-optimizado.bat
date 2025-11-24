@echo off
echo ========================================
echo GENERANDO APK OPTIMIZADO PARA LIVE
echo ========================================
echo.

echo [1/3] Limpiando build anterior...
call .\gradlew clean
if %errorlevel% neq 0 (
    echo ERROR: No se pudo limpiar el build
    pause
    exit /b 1
)
echo OK - Build limpiado
echo.

echo [2/3] Generando APK Release optimizado...
echo Esto puede tomar 2-3 minutos...
call .\gradlew assembleRelease
if %errorlevel% neq 0 (
    echo ERROR: No se pudo generar el APK
    pause
    exit /b 1
)
echo OK - APK generado
echo.

echo [3/3] Ubicando APK...
echo.
echo ========================================
echo APK GENERADO EXITOSAMENTE
echo ========================================
echo.
echo Ubicacion del APK:
echo app\build\outputs\apk\release\
echo.
echo Archivos generados:
dir /b app\build\outputs\apk\release\*.apk
echo.
echo Tamano de los APKs:
for %%f in (app\build\outputs\apk\release\*.apk) do (
    echo %%~nxf - %%~zf bytes
)
echo.
echo NOTA: Se generaron APKs separados por arquitectura:
echo - app-armeabi-v7a-release.apk (para dispositivos de 32 bits)
echo - app-arm64-v8a-release.apk (para dispositivos de 64 bits)
echo.
echo Usa el APK arm64-v8a para dispositivos modernos (mas pequeno)
echo.
pause
