@echo off
REM Compilar y ejecutar Chromis Admin - VERSIÓN CORREGIDA
setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
set JAVAC="!JAVA_HOME!\bin\javac.exe"
set JAR="!JAVA_HOME!\bin\jar.exe"
set JAVA="!JAVA_HOME!\bin\java.exe"

cd /d C:\xampp\htdocs\chromispos\ChromisPOS

echo Compilando ChromisAdminApp.java (version corregida)...
if not exist build\standalone mkdir build\standalone

!JAVAC! -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if errorlevel 1 (
    echo Error de compilacion
    pause
    exit /b 1
)

echo OK - Compilacion exitosa
echo.
echo Creando JAR...
(
    echo Manifest-Version: 1.0
    echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
) > build\standalone\MANIFEST.MF

!JAR! cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk

if exist ChromisAdmin.jar (
    echo OK - JAR creado
    echo.
    echo Abriendo aplicacion...
    !JAVA! -jar ChromisAdmin.jar
) else (
    echo Error creando JAR
    pause
)
