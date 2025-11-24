@echo off
echo ========================================
echo VERIFICACION DE CLOUD FUNCTIONS
echo ========================================
echo.

echo Verificando funciones desplegadas en Firebase...
echo.
firebase functions:list
echo.

echo ========================================
echo Si ves las funciones listadas arriba, todo esta OK
echo Si no, ejecuta: verificar-y-desplegar-functions.bat
echo ========================================
echo.
pause
