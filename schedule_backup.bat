@echo off
title ChromisPOS - Programador de Respaldo Automatico

:: ============================================================
:: ChromisPOS Ecuador - Programador de Respaldos Automáticos
:: Usa el Programador de Tareas de Windows
:: ============================================================

cd /d "%~dp0"

echo ╔══════════════════════════════════════════════════╗
echo ║   ChromisPOS - Programador de Respaldos         ║
echo ╚══════════════════════════════════════════════════╝
echo.

:: Verificar si tenemos mysqldump
where mysqldump >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] mysqldump no encontrado.
    echo.
    echo Para usar respaldos automaticos, necesita MySQL/MariaDB en el PATH.
    echo Si usa XAMPP, agregue al PATH:
    echo   C:\xampp\mysql\bin
    echo.
    pause
    exit /b 1
)

:: Crear carpeta de backups si no existe
if not exist "backups" mkdir backups

:: Crear script de backup
set BACKUP_SCRIPT=%TEMP%\chromispos_backup.bat

(
echo @echo off
echo cd /d "C:\xampp\htdocs\chromispos\ChromisPOS"
echo.
echo :: Leer configuracion desde properties
echo for /f "tokens=2 delims==" %%%%a in ('findstr "database.server" chromisposconfig.properties'^) do set DB_HOST=%%%%a
echo for /f "tokens=2 delims==" %%%%a in ('findstr "database.port" chromisposconfig.properties'^) do set DB_PORT=%%%%a
echo for /f "tokens=2 delims==" %%%%a in ('findstr "database.name" chromisposconfig.properties'^) do set DB_NAME=%%%%a
echo for /f "tokens=2 delims==" %%%%a in ('findstr "database.user" chromisposconfig.properties'^) do set DB_USER=%%%%a
echo for /f "tokens=2 delims==" %%%%a in ('findstr "database.password" chromisposconfig.properties'^) do set DB_PASS=%%%%a
echo.
echo :: Limpiar espacios
echo set DB_HOST=%%DB_HOST: =%%
echo set DB_PORT=%%DB_PORT: =%%
echo set DB_NAME=%%DB_NAME: =%%
echo set DB_USER=%%DB_USER: =%%
echo set DB_PASS=%%DB_PASS: =%%
echo.
echo :: Generar nombre de archivo
echo set TIMESTAMP=%%date:/=-%%_%%time::=-%%
echo set TIMESTAMP=%%TIMESTAMP: =0%%
echo set BACKUP_FILE=backups\chromispos_backup_%%TIMESTAMP%%.sql
echo.
echo :: Ejecutar respaldo
echo mysqldump -h%%DB_HOST%% -P%%DB_PORT%% -u%%DB_USER%% -p%%DB_PASS%% --routines --triggers --databases %%DB_NAME%% ^> "%%BACKUP_FILE%%"
echo.
echo :: Comprimir (si 7zip esta disponible)
echo if exist "%%BACKUP_FILE%%" (
echo     if exist "C:\Program Files\7-Zip\7z.exe" (
echo         "C:\Program Files\7-Zip\7z.exe" a -tzip "%%BACKUP_FILE%%.zip" "%%BACKUP_FILE%%" -mx9
echo         del "%%BACKUP_FILE%%"
echo         echo [OK] Respaldo comprimido: %%BACKUP_FILE%%.zip
echo     ^) else (
echo         echo [OK] Respaldo creado: %%BACKUP_FILE%%
echo     ^)
echo ^)
) > "%BACKUP_SCRIPT%"

echo.
echo ============================================================
echo   CONFIGURACION DEL RESPALDO AUTOMATICO
echo ============================================================
echo.

set /p HOUR="Hora del respaldo (0-23, default 2): "
if "%HOUR%"=="" set HOUR=2

:: Crear tarea programada en Windows
schtasks /create /tn "ChromisPOS Backup Diario" /tr "%BACKUP_SCRIPT%" /sc daily /st %HOUR%:00 /f

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Tarea programada creada exitosamente!
    echo.
    echo   Nombre: ChromisPOS Backup Diario
    echo   Horario: Diario a las %HOUR%:00
    echo   Ubicacion: %BACKUP_SCRIPT%
    echo.
    echo Para ver o modificar la tarea:
    echo   - Abra "Programador de tareas" de Windows
    echo   - Busque "ChromisPOS Backup Diario"
    echo.
    echo Para eliminar la tarea:
    echo   schtasks /delete /tn "ChromisPOS Backup Diario" /f
    echo.
) else (
    echo.
    echo [ERROR] No se pudo crear la tarea programada.
    echo Ejecute este archivo como Administrador.
    echo.
)

pause
