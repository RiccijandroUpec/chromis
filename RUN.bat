@echo off
REM Compile and Run Chromis Admin Standalone

cd /d "%~dp0"
setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot

if not exist "!JAVA_HOME!\bin\javac.exe" (
    echo Error: Java no encontrado
    pause
    exit /b 1
)

set JAVAC=!JAVA_HOME!\bin\javac.exe
set JAR=!JAVA_HOME!\bin\jar.exe
set JAVA=!JAVA_HOME!\bin\java.exe

echo Compilando ChromisAdminApp.java...
if not exist "build\standalone" mkdir "build\standalone"

!JAVAC! -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if exist "build\standalone\uk\chromis\pos\panels\ChromisAdminApp.class" (
    echo Creando JAR...
    (
        echo Manifest-Version: 1.0
        echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
    ) > build\standalone\MANIFEST.MF
    
    !JAR! cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk
    
    echo Ejecutando aplicacion...
    !JAVA! -jar ChromisAdmin.jar
) else (
    echo Error de compilacion
    pause
)
