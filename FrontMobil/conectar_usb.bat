@echo off
echo Conectando el puerto 3000 de tu PC a tu celular por cable USB...
"C:\Users\chris\AppData\Local\Android\Sdk\platform-tools\adb.exe" reverse tcp:3000 tcp:3000
if %errorlevel% equ 0 (
    echo [EXITO] Puente USB activo en http://127.0.0.1:3000/
) else (
    echo [ERROR] Verifica que tu celular este conectado por USB con Depuracion por USB activa.
)
pause
