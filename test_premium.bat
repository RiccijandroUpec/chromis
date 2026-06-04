@echo off
REM ChromisPOS Premium Panel Test & Launch
REM This script compiles the premium panels and runs ChromisPOS

setlocal enabledelayedexpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"
set "JAVAW=%JAVA_HOME%\bin\javaw.exe"

echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║     CHROMISPOS - SISTEMA CENTRALIZADO PREMIUM                 ║
echo ║                                                                ║
echo ║     Compilando paneles nuevos...                              ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Crear directorio de compilación
if not exist "build\classes" mkdir "build\classes"

REM Compilar los nuevos paneles
echo Compilando JPanelDashboardCentral...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelDashboardCentral.java" 2>nul

echo Compilando JPanelAdminPremium...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java" 2>nul

REM Actualizar el JAR con las nuevas clases compiladas
echo Actualizando chromispos.jar...
"%JAVA_HOME%\bin\jar.exe" uf "chromispos.jar" -C "build\classes" . 2>nul

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ✓ COMPILACIÓN LISTA - Iniciando ChromisPOS...               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Espera a que se abra la ventana de ChromisPOS...
echo.

REM Iniciar ChromisPOS
start "" "%JAVAW%" ^
    -cp "build\classes;chromispos.jar;lib\*" ^
    -Duser.country=EC ^
    -Duser.language=es ^
    -Duser.timezone=America/Guayaquil ^
    -Dfile.encoding=UTF-8 ^
    uk.chromis.pos.forms.StartPOS

echo ChromisPOS se está iniciando...
timeout /t 3 /nobreak

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  Busca los nuevos paneles en el menú:                         ║
echo ║                                                                ║
echo ║  📊 Dashboard Centralizado                                    ║
echo ║  🔧 Panel de Administración Premium                           ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
