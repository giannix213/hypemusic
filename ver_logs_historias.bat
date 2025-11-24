@echo off
echo Filtrando logs de historias...
echo.
adb logcat -c
adb logcat | findstr "HISTORIAS_"
