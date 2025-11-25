@echo off
chcp 65001 >nul
echo ========================================
echo 📊 INFORMACIÓN DE LA VERSIÓN ACTUAL
echo ========================================
echo.

echo Último commit:
"C:\Program Files\Git\bin\git.exe" log -1 --format="%%cd - %%s" --date=format:"%%d/%%m/%%Y %%H:%%M"
echo.

echo Rama actual:
"C:\Program Files\Git\bin\git.exe" branch --show-current
echo.

echo Estado:
"C:\Program Files\Git\bin\git.exe" status -s
echo.

echo Últimos 5 commits:
"C:\Program Files\Git\bin\git.exe" log -5 --oneline
echo.

echo ========================================
echo Comparte esta información con tu hermana
echo para verificar que tienen la misma versión
echo ========================================
echo.
pause
