@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
set JAVAC=!JAVA_HOME!\bin\javac.exe
set JAR=!JAVA_HOME!\bin\jar.exe

if not exist "build\standalone" mkdir "build\standalone"

echo Compilando...
!JAVAC! -d build\standalone src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if exist "build\standalone\uk\chromis\pos\panels\ChromisAdminApp.class" (
    echo Manifest-Version: 1.0 > build\standalone\MANIFEST.MF
    echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp >> build\standalone\MANIFEST.MF
    
    !JAR! cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk
    echo OK
) else (
    echo FAIL
)
