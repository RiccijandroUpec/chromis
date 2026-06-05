@echo off
setlocal enabledelayedexpansion

REM Buscar instalacion de Java
set JAVA_HOME=
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
if "!JAVA_HOME!"=="" (
    for /d %%D in ("C:\Program Files\Java\*") do (
        if exist "%%D\bin\java.exe" set JAVA_HOME=%%D
    )
)

if "!JAVA_HOME!"=="" (
    echo Java no encontrado. Por favor instala JDK/JRE 8.
    pause
    exit /b 1
)

echo Iniciando ChromisPOS...
"!JAVA_HOME!\bin\java.exe" -cp "chromispos.jar;lib/*" uk.chromis.pos.forms.StartPOS
