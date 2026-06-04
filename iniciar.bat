@echo off
REM Compilación y ejecución de Chromis Admin Desktop
setlocal enabledelayedexpansion

REM Configurar variables
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
set JAVAC="!JAVA_HOME!\bin\javac.exe"
set JAR="!JAVA_HOME!\bin\jar.exe"
set JAVA="!JAVA_HOME!\bin\java.exe"
set SRC_DIR=C:\xampp\htdocs\chromispos\ChromisPOS\src-pos
set BUILD_DIR=C:\xampp\htdocs\chromispos\ChromisPOS\build\standalone
set APP_DIR=C:\xampp\htdocs\chromispos\ChromisPOS

REM Cambiar a directorio de aplicación
cd /d "%APP_DIR%"

REM Crear directorio de compilación
if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

REM Compilar
echo Compilando ChromisAdminApp.java...
%JAVAC% -d "%BUILD_DIR%" -encoding UTF-8 "%SRC_DIR%\uk\chromis\pos\panels\ChromisAdminApp.java"

if errorlevel 1 (
    echo Error de compilacion
    pause
    exit /b 1
)

echo Compilacion OK

REM Crear MANIFEST
(
    echo Manifest-Version: 1.0
    echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
) > "%BUILD_DIR%\MANIFEST.MF"

REM Crear JAR
echo Creando JAR...
%JAR% cfm ChromisAdmin.jar "%BUILD_DIR%\MANIFEST.MF" -C "%BUILD_DIR%" uk

if not exist ChromisAdmin.jar (
    echo Error creando JAR
    pause
    exit /b 1
)

echo JAR creado exitosamente
echo.
echo Iniciando aplicacion...
%JAVA% -jar ChromisAdmin.jar
