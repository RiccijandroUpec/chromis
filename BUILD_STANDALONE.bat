@echo off
REM ====================================================================
REM  CHROMIS ADMIN - COMPILADOR STANDALONE
REM  Crea un JAR ejecutable independiente (sin necesidad de ChromisPOS)
REM ====================================================================

echo.
echo ╔═══════════════════════════════════════════════════════════════════════╗
echo ║                                                                       ║
echo ║            🔧 CHROMIS ADMIN STANDALONE - COMPILADOR                  ║
echo ║                                                                       ║
echo ║          Una sola aplicación grande, todo centralizado                ║
echo ║                                                                       ║
echo ╚═══════════════════════════════════════════════════════════════════════╝
echo.

REM Detectar Java
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo ❌ Error: Java no encontrado en %JAVA_HOME%
    echo.
    echo Por favor instala Eclipse Adoptium JDK desde:
    echo   https://adoptium.net/
    pause
    exit /b 1
)

set JAVAC="%JAVA_HOME%\bin\javac.exe"
set JAR="%JAVA_HOME%\bin\jar.exe"
set JAVA="%JAVA_HOME%\bin\java.exe"

cd /d "%~dp0"

REM Crear directorio de compilación
if not exist "build\standalone" mkdir "build\standalone"

echo 📦 Compilando ChromisAdminApp.java...
%JAVAC% -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if %errorlevel% neq 0 (
    echo ❌ Error en compilación
    pause
    exit /b 1
)

echo.
echo ✅ Compilación exitosa

REM Crear MANIFEST para JAR ejecutable
echo.
echo 📝 Creando MANIFEST...
(
    echo Manifest-Version: 1.0
    echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
    echo Class-Path: .
) > build\standalone\MANIFEST.MF

REM Crear JAR ejecutable
echo.
echo 🔨 Empaquetando en JAR...
%JAR% cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk

if %errorlevel% neq 0 (
    echo ❌ Error al crear JAR
    pause
    exit /b 1
)

echo.
echo ╔═══════════════════════════════════════════════════════════════════════╗
echo ║                                                                       ║
echo ║                   ✅ BUILD COMPLETADO EXITOSAMENTE                   ║
echo ║                                                                       ║
echo ╚═══════════════════════════════════════════════════════════════════════╝
echo.
echo 📦 Archivo creado: ChromisAdmin.jar
echo.
echo 🚀 PARA EJECUTAR:
echo    1. Haz doble clic en: ChromisAdmin.jar
echo       O en terminal: java -jar ChromisAdmin.jar
echo.
echo 📋 Esta es una aplicación STANDALONE:
echo    ✓ Una sola aplicación
echo    ✓ Todo centralizado
echo    ✓ Sin módulos dispersos
echo    ✓ Fácil de usar
echo.
pause
