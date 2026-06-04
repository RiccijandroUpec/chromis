@echo off
REM ========================================================================
REM   CHROMISPOS - SISTEMA CENTRALIZADO PREMIUM - INICIO RÁPIDO
REM ========================================================================

:MENU
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║   ChromisPOS - Sistema Centralizado Premium                   ║
echo ║                                                                ║
echo ║   Bienvenido! Selecciona una opción:                          ║
echo ║                                                                ║
echo ║   1. Compilar y Ejecutar (RECOMENDADO)                        ║
echo ║   2. Ver Demo Interactiva en Navegador                        ║
echo ║   3. Compilar Solo (sin ejecutar)                             ║
echo ║   4. Ver Documentación                                        ║
echo ║   5. Salir                                                    ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set /p choice="Escribe tu opción (1-5): "

if "%choice%"=="1" goto COMPILE_AND_RUN
if "%choice%"=="2" goto SHOW_DEMO
if "%choice%"=="3" goto COMPILE_ONLY
if "%choice%"=="4" goto SHOW_DOCS
if "%choice%"=="5" goto EXIT
goto MENU

:COMPILE_AND_RUN
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  COMPILANDO Y EJECUTANDO...                                   ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"
cd /d "c:\xampp\htdocs\chromispos\ChromisPOS"

if not exist "build\classes" mkdir "build\classes"

echo [1/4] Compilando JPanelDashboardCentral...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelDashboardCentral.java" 2>nul
if %errorlevel% equ 0 (
    echo ✓ Compilado exitosamente
) else (
    echo ✗ Error en compilación
    pause
    goto MENU
)

echo [2/4] Compilando JPanelAdminPremium...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java" 2>nul
if %errorlevel% equ 0 (
    echo ✓ Compilado exitosamente
) else (
    echo ✗ Error en compilación
    pause
    goto MENU
)

echo [3/4] Actualizando chromispos.jar...
"%JAVA_HOME%\bin\jar.exe" uf "chromispos.jar" -C "build\classes" . 2>nul
if %errorlevel% equ 0 (
    echo ✓ JAR actualizado
) else (
    echo ✗ Error actualizando JAR
    pause
    goto MENU
)

echo [4/4] Iniciando ChromisPOS...
echo.
start "" "%JAVA_HOME%\bin\javaw.exe" ^
    -cp "build\classes;chromispos.jar;lib\*" ^
    -Duser.country=EC ^
    -Duser.language=es ^
    -Duser.timezone=America/Guayaquil ^
    -Dfile.encoding=UTF-8 ^
    uk.chromis.pos.forms.StartPOS

timeout /t 3 /nobreak

cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ✓ ¡ChromisPOS está iniciando!                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo 📌 BUSCA ESTOS NUEVOS PANELES EN EL MENÚ:
echo.
echo    📊 Dashboard Centralizado
echo       - Acceso rápido a todas las funciones
echo       - Reloj en tiempo real
echo       - Operaciones comunes
echo       - Configuración y reportes
echo.
echo    🔧 Panel de Administración Premium
echo       - 6 categorías principales
echo       - 18+ opciones de configuración
echo       - Diseño premium centralizado
echo.
echo ========================================================
echo.
echo Presiona ENTER para volver al menú...
pause >nul
goto MENU

:SHOW_DEMO
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ABRIENDO DEMO INTERACTIVA...                                 ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set "DEMO_FILE=C:\Users\richa\.copilot\session-state\c5c7c15c-9fae-49e5-88bb-31d0862e23af\preview_sistema.html"

if exist "%DEMO_FILE%" (
    echo ✓ Abriendo demostración interactiva...
    echo   - Verás el Dashboard Centralizado
    echo   - Verás el Panel de Administración
    echo   - Verás información del sistema
    echo.
    start "" "%DEMO_FILE%"
    echo ✓ Se abrió en tu navegador
) else (
    echo ✗ Archivo de demostración no encontrado
    echo   Ubicación esperada: %DEMO_FILE%
)

echo.
echo Presiona ENTER para volver al menú...
pause >nul
goto MENU

:COMPILE_ONLY
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  COMPILANDO SOLO...                                           ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"
cd /d "c:\xampp\htdocs\chromispos\ChromisPOS"

if not exist "build\classes" mkdir "build\classes"

echo Compilando JPanelDashboardCentral...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelDashboardCentral.java" 2>&1

echo Compilando JPanelAdminPremium...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java" 2>&1

echo Actualizando chromispos.jar...
"%JAVA_HOME%\bin\jar.exe" uf "chromispos.jar" -C "build\classes" . 2>&1

echo.
echo ✓ Compilación completada
echo.
echo Presiona ENTER para volver al menú...
pause >nul
goto MENU

:SHOW_DOCS
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  DOCUMENTACIÓN DISPONIBLE                                     ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Documentos disponibles:
echo.
echo 1. RESUMEN_FINAL.md
echo    - Resumen completo del sistema
echo    - Instrucciones de uso
echo    - Checklist final
echo.
echo 2. SISTEMA_CENTRALIZADO.md
echo    - Guía de implementación
echo    - Paso a paso
echo    - Personalización
echo.
echo 3. DEMOSTRACION_VISUAL.md
echo    - Mockups visuales
echo    - Características
echo    - Cómo probarlo
echo.
echo Ubicación: C:\Users\richa\.copilot\session-state\c5c7c15c-9fae-49e5-88bb-31d0862e23af\
echo.
echo Presiona ENTER para volver al menú...
pause >nul
goto MENU

:EXIT
cls
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ¡Gracias por usar ChromisPOS Premium!                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
pause
exit /b 0
