@echo off
REM ============================================================
REM  CHROMIS ADMIN DESKTOP - COMPILADOR
REM  Compila la aplicación de escritorio y genera JAR ejecutable
REM ============================================================

setlocal enabledelayedexpansion
cd /d "%~dp0"

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                                                           ║
echo ║     🔧 COMPILANDO CHROMIS ADMIN - APLICACIÓN DE ESCRITORIO
echo ║                                                           ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.

if not exist "!JAVA_HOME!\bin\javac.exe" (
    echo ❌ Error: Java no encontrado en:
    echo    !JAVA_HOME!
    pause
    exit /b 1
)

echo ✓ Java encontrado
echo.

echo 📁 Creando directorio de compilación...
if not exist "build\standalone" mkdir "build\standalone"

echo.
echo 🔨 Compilando ChromisAdminApp.java...
"!JAVA_HOME!\bin\javac.exe" -d build\standalone -encoding UTF-8 src-pos\uk\chromis\pos\panels\ChromisAdminApp.java

if errorlevel 1 (
    echo.
    echo ❌ Error durante la compilación
    pause
    exit /b 1
)

echo ✓ Compilación exitosa

echo.
echo 📦 Creando archivo MANIFEST...
(
    echo Manifest-Version: 1.0
    echo Main-Class: uk.chromis.pos.panels.ChromisAdminApp
) > build\standalone\MANIFEST.MF

echo ✓ MANIFEST creado

echo.
echo 🎁 Empaquetando en JAR...
"!JAVA_HOME!\bin\jar.exe" cfm ChromisAdmin.jar build\standalone\MANIFEST.MF -C build\standalone uk

if errorlevel 1 (
    echo.
    echo ❌ Error al crear JAR
    pause
    exit /b 1
)

echo ✓ JAR creado exitosamente

echo.
echo ╔═══════════════════════════════════════════════════════════╗
echo ║                                                           ║
echo ║           ✅ COMPILACIÓN COMPLETADA EXITOSAMENTE          ║
echo ║                                                           ║
echo ╚═══════════════════════════════════════════════════════════╝
echo.
echo 📦 Archivo generado: ChromisAdmin.jar
echo.
echo 🚀 PARA EJECUTAR LA APLICACIÓN DE ESCRITORIO:
echo.
echo    Opción 1: Doble clic en ChromisAdmin.jar
echo    Opción 2: java -jar ChromisAdmin.jar
echo.
echo ✨ Una sola aplicación de escritorio con TODO centralizado
echo    • 6 tabs principales
echo    • 49 opciones de configuración  
echo    • Interfaz premium
echo    • Sin módulos dispersos
echo.
pause
