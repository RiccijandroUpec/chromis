@echo off
setlocal enabledelayedexpansion
cd /d "C:\xampp\htdocs\chromispos\ChromisPOS"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot

if not exist "build\standalone" mkdir "build\standalone"

"!JAVA_HOME!\bin\javac.exe" -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if exist "build\standalone\uk\chromis\pos\panels\ChromisAdminApp.class" (
    (
        echo Manifest-Version: 1.0
        echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
    ) > build\standalone\MANIFEST.MF
    
    "!JAVA_HOME!\bin\jar.exe" cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk
    
    if exist "ChromisAdmin.jar" (
        echo OK - ChromisAdmin.jar creado
        exit /b 0
    )
)

echo FAIL - Error en compilacion
exit /b 1
