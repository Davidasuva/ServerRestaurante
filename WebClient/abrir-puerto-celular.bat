@echo off
REM Permite que el celular entre a la pagina del restaurante (puerto 8080) a traves del Firewall de Windows.
REM EJECUTAR COMO ADMINISTRADOR: clic derecho sobre este archivo > "Ejecutar como administrador".
chcp 65001 >nul
net session >nul 2>&1
if %errorlevel% neq 0 (
  echo Este archivo necesita permisos de administrador.
  echo Clic derecho sobre el archivo, y elige "Ejecutar como administrador".
  pause
  exit /b 1
)

set PUERTO=8080

netsh advfirewall firewall delete rule name="Restaurante Web %PUERTO%" >nul 2>&1
netsh advfirewall firewall add rule name="Restaurante Web %PUERTO%" dir=in action=allow protocol=TCP localport=%PUERTO% profile=any

netsh advfirewall firewall delete rule name="Restaurante mDNS 5353" >nul 2>&1
netsh advfirewall firewall add rule name="Restaurante mDNS 5353" dir=in action=allow protocol=UDP localport=5353 profile=any

echo.
echo Listo. Direcciones IPv4 de este equipo:
ipconfig | findstr /R /C:"IPv4"
echo.
echo En el celular (conectado a la MISMA red Wi-Fi) abre  http://IP:%PUERTO%/  con la IP de la red Wi-Fi de arriba.
echo Al arrancar el backend tambien se imprime la direccion recomendada en la consola.
pause
