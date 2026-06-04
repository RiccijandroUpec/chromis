@echo off
title ChromisPOS - Inicializar Base de Datos
echo ============================================================
echo   ChromisPOS Ecuador - Inicializar Base de Datos
echo   Autor: Riccijandro | github.com/riccijandro
echo ============================================================
echo.

:: Buscar MySQL
set MYSQL_PATH=
for /f "tokens=*" %%I in ('where mysql 2^>nul') do (
    set MYSQL_PATH=%%~dpI
    goto FOUND_MYSQL
)

:: Buscar en XAMPP
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set "MYSQL_PATH=C:\xampp\mysql\bin\"
    goto FOUND_MYSQL
)

:: Buscar en programas
for %%D in ("C:\Program Files\MySQL\MySQL Server 8.0\bin" "C:\Program Files\MySQL\MySQL Server 5.7\bin" "C:\Program Files (x86)\MySQL\MySQL Server 5.7\bin") do (
    if exist "%%~D\mysql.exe" (
        set "MYSQL_PATH=%%~D\"
        goto FOUND_MYSQL
    )
)

echo [ERROR] MySQL no encontrado.
echo.
echo Asegurese de tener MySQL/MariaDB instalado y en el PATH.
echo XAMPP: C:\xampp\mysql\bin\
echo.
pause
exit /b 1

:FOUND_MYSQL
echo [OK] MySQL encontrado en: %MYSQL_PATH%
echo.

:: Pedir datos de conexion
set /p DB_HOST="Host (default: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="Puerto (default: 3306): "
if "%DB_PORT%"=="" set DB_PORT=3306

set /p DB_USER="Usuario (default: root): "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS="Contrasena (dejar vacio para root sin pass): "

set /p DB_NAME="Nombre de Base de Datos (default: chromispos_ec): "
if "%DB_NAME%"=="" set DB_NAME=chromispos_ec

echo.
echo ============================================================
echo Creando base de datos '%DB_NAME%'...
echo ============================================================

:: Crear la base de datos
"%MYSQL_PATH%mysql.exe" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8 COLLATE utf8_general_ci;" 2>nul

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] No se pudo crear la base de datos.
    echo Posibles causas:
    echo   - MySQL no esta corriendo (inicia XAMPP primero)
    echo   - Credenciales incorrectas
    echo   - Puerto incorrecto
    echo.
    pause
    exit /b 1
)

echo [OK] Base de datos '%DB_NAME%' creada/listo.
echo.

echo ============================================================
echo Ejecutando script de inicializacion...
echo ============================================================

set "SQL_FILE=sql\init_chromispos_ecuador.sql"
set "TEMP_SQL=%TEMP%\chromispos_init_%RANDOM%.sql"

(
    type "%SQL_FILE%" | findstr /v "^USE "
    echo USE %DB_NAME%;
) > "%TEMP_SQL%"

"%MYSQL_PATH%mysql.exe" -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_PASS% %DB_NAME% < "%TEMP_SQL%"

if %ERRORLEVEL% EQU 0 (
    echo [OK] Base de datos inicializada correctamente!
) else (
    echo [ERROR] Fallo la inicializacion. Revise los mensajes arriba.
    del "%TEMP_SQL%" 2>nul
    pause
    exit /b 1
)

del "%TEMP_SQL%" 2>nul

echo.
echo ============================================================
echo Actualizando chromisposconfig.properties...
echo ============================================================

set "CONFIG_FILE=chromisposconfig.properties"

copy "%CONFIG_FILE%" "%CONFIG_FILE%.backup" >nul

powershell -Command ^
"(Get-Content '%CONFIG_FILE%') ^
    -replace 'database\.server\s*=.*', 'database.server = %DB_HOST%' ^
    -replace 'database\.port\s*=.*', 'database.port = %DB_PORT%' ^
    -replace 'database\.name\s*=.*', 'database.name = %DB_NAME%' ^
    -replace 'database\.user\s*=.*', 'database.user = %DB_USER%' ^
    -replace 'database\.password\s*=.*', 'database.password = %DB_PASS%' ^
| Set-Content '%CONFIG_FILE%'"

echo [OK] chromisposconfig.properties actualizado.
echo.

echo ============================================================
echo   BASE DE DATOS CONFIGURADA EXITOSAMENTE!
echo ============================================================
echo.
echo   Base de datos: %DB_NAME%
echo   Servidor:      %DB_HOST%:%DB_PORT%
echo   Usuario:       %DB_USER%
echo.
echo   YA PUEDE INICIAR ChromisPOS:
echo     - Ejecute: iniciar.bat
echo     - O: java -jar chromispos.jar
echo.
echo   Backup creado: chromisposconfig.properties.backup
echo.
pause
