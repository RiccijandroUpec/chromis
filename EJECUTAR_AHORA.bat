@echo off
REM Compilador directo para Chromis Admin Desktop
setlocal enabledelayedexpansion
cd /d "C:\xampp\htdocs\chromispos\ChromisPOS"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
set JAVAC="!JAVA_HOME!\bin\javac.exe"
set JAR="!JAVA_HOME!\bin\jar.exe"

if not exist "!JAVA_HOME!" (
    echo Error: Java no encontrado
    exit /b 1
)

echo Compilando...
if not exist build\standalone mkdir build\standalone

!JAVAC! -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if exist build\standalone\uk\chromis\pos\panels\ChromisAdminApp.class (
    echo Creando JAR...
    (
        echo Manifest-Version: 1.0
        echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
    ) > build\standalone\MANIFEST.MF
    
    !JAR! cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk
    
    if exist ChromisAdmin.jar (
        echo EXITO - JAR creado
        echo Ejecutando...
        "!JAVA_HOME!\bin\java.exe" -jar ChromisAdmin.jar
        exit /b 0
    )
)

echo FALLO
exit /b 1
