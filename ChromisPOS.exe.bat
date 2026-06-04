@echo off
title ChromisPOS Ecuador
cd /d "%~dp0"

:: Configurar título de la ventana
title ChromisPOS Ecuador v1.5.5 - Punto de Venta

:: Colores
set "CYAN=[36m"
set "GREEN=[32m"
set "YELLOW=[33m"
set "RED=[31m"
set "RESET=[0m"

echo %CYAN%╔══════════════════════════════════════════════════╗%RESET%
echo %CYAN%║         ChromisPOS Ecuador v1.5.5               ║%RESET%
echo %CYAN%║     Punto de Venta Open Source para Ecuador     ║%RESET%
echo %CYAN%║     Autor: Riccijandro - SRI Integrado          ║%RESET%
echo %CYAN%╚══════════════════════════════════════════════════╝%RESET%
echo.

:: Verificar si existe chromispos.jar
if not exist "chromispos.jar" (
    echo %RED%[ERROR] No se encuentra chromispos.jar%RESET%
    echo.
    echo %YELLOW%Asegurese de ejecutar este archivo desde la carpeta%RESET%
    echo %YELLOW%donde esta instalado ChromisPOS Ecuador.%RESET%
    echo.
    pause
    exit /b 1
)

:: Verificar Java
set JAVA_EXE=java
set JAVA_OPTS=

:: Buscar JRE empaquetado primero
if exist "jre\bin\javaw.exe" (
    set "JAVA_EXE=jre\bin\javaw.exe"
) else if exist "jre\bin\java.exe" (
    set "JAVA_EXE=jre\bin\java.exe"
) else (
    where java >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo %RED%[ERROR] Java no encontrado%RESET%
        echo.
        echo %YELLOW%ChromisPOS Ecuador requiere Java 11 o superior.%RESET%
        echo %YELLOW%Descargue Java desde: https://adoptium.net/%RESET%
        echo.
        pause
        exit /b 1
    )
)

:: Verificar versión de Java
for /f "tokens=3" %%g in ('"%JAVA_EXE%" -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
echo %GREEN%[OK] Java version: %JAVA_VERSION%%RESET%

:: Configurar opciones de memoria
set JAVA_OPTS=-Xms256m -Xmx1024m

:: Configurar opciones de look and feel
set JAVA_OPTS=%JAVA_OPTS% -Dflatlaf.animation=true

:: Modo debug (opcional)
if "%1"=="-debug" (
    echo %YELLOW>Modo DEBUG activado%RESET%
    set JAVA_OPTS=%JAVA_OPTS% -Ddebug=true
    if not exist "logs" mkdir logs
    echo.
    echo %YELLOW%Iniciando ChromisPOS en modo debug...%RESET%
    echo %YELLOW%Los logs se guardaran en: logs\POSS.log%RESET%
    echo.
    "%JAVA_EXE%" %JAVA_OPTS% -jar chromispos.jar -debug
) else (
    echo.
    echo %GREEN%Iniciando ChromisPOS Ecuador...%RESET%
    echo.
    start "" "%JAVA_EXE%" %JAVA_OPTS% -jar chromispos.jar
)

exit /b 0
