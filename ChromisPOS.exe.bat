@echo off
title ChromisPOS Ecuador v1.5.5
cd /d "%~dp0"

:: ============================================================
:: ChromisPOS Ecuador - Launcher Profesional
:: Usa classpath en lugar de -jar para cargar FlatLaf, PDFBox,
:: y todas las librerias externas correctamente.
:: ============================================================

setlocal enabledelayedexpansion

set "CYAN=[36m"
set "GREEN=[32m"
set "YELLOW=[33m"
set "RED=[31m"
set "RESET=[0m"

echo %CYAN%============================================%RESET%
echo %CYAN%   ChromisPOS Ecuador v1.5.5%RESET%
echo %CYAN%   Punto de Venta para Ecuador%RESET%
echo %CYAN%   Autor: Riccijandro - SRI Integrado%RESET%
echo %CYAN%============================================%RESET%
echo.

:: Verificar si existe chromispos.jar
if not exist "chromispos.jar" (
    echo %RED%[ERROR] No se encuentra chromispos.jar%RESET%
    echo.
    pause
    exit /b 1
)

:: Buscar Java
set JAVA_EXE=java

if exist "jre\bin\javaw.exe" (
    set "JAVA_EXE=jre\bin\javaw.exe"
) else if exist "jre\bin\java.exe" (
    set "JAVA_EXE=jre\bin\java.exe"
) else (
    where java >nul 2>nul
    if !ERRORLEVEL! NEQ 0 (
        echo %RED%[ERROR] Java no encontrado%RESET%
        echo Descargue Java 11+ desde: https://adoptium.net/
        pause
        exit /b 1
    )
)

:: Mostrar version de Java
for /f "tokens=3" %%g in ('"%JAVA_EXE%" -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
echo %GREEN%[OK] Java version: %JAVA_VERSION:"=%%RESET%

:: Construir classpath con todas las librerias
set CLASSPATH=build\classes;chromispos.jar
for %%f in (lib\*.jar) do set CLASSPATH=!CLASSPATH!;%%f

:: Opciones de JVM
set JAVA_OPTS=-Xms256m -Xmx1024m -Dflatlaf.animation=true -Duser.country=EC -Duser.language=es -Duser.timezone=America/Guayaquil

:: Modo debug
if "%1"=="-debug" (
    echo %YELLOW%Modo DEBUG activado%RESET%
    if not exist "logs" mkdir logs
    "%JAVA_EXE%" %JAVA_OPTS% -cp "!CLASSPATH!" uk.chromis.pos.forms.StartPOS > logs\POS.log 2>&1
    echo %GREEN%Logs en logs\POS.log%RESET%
) else (
    echo.
    echo %GREEN%Iniciando ChromisPOS Ecuador...%RESET%
    echo.
    start "" "%JAVA_EXE%" %JAVA_OPTS% -cp "!CLASSPATH!" uk.chromis.pos.forms.StartPOS
)

echo %GREEN%[OK] ChromisPOS iniciado correctamente%RESET%
exit /b 0