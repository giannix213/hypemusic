@echo off
chcp 65001 >nul
echo ========================================
echo 📥 ACTUALIZANDO PROYECTO
echo ========================================
echo.

"C:\Program Files\Git\bin\git.exe" fetch origin
"C:\Program Files\Git\bin\git.exe" pull origin master

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ Proyecto actualizado correctamente
    echo.
    echo Último commit:
    "C:\Program Files\Git\bin\git.exe" log -1 --format="%%cd - %%s" --date=format:"%%d/%%m/%%Y %%H:%%M"
) else (
    echo.
    echo ❌ Error al actualizar
)

echo.
pause
