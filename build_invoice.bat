@echo off
REM Build Script - Compilación del módulo de Facturación Electrónica (Windows)

setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║  Compilación - Módulo Facturación Electrónica Ecuador        ║
echo ║  ChromisPOS - Windows                                         ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Variables
set PROJECT_DIR=.
set SOURCE_DIR=src-pos\uk\chromis\pos\invoice
set BUILD_DIR=build\classes
set LIB_DIR=lib

echo [1/6] Limpiando directorios anteriores...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
echo ✓ Directorios limpios
echo.

echo [2/6] Verificando estructura de carpetas...
if not exist "%SOURCE_DIR%" (
    echo ✗ Carpeta no encontrada: %SOURCE_DIR%
    exit /b 1
)
echo ✓ Estructura verificada
echo.

echo [3/6] Compilando código Java...
setlocal enabledelayedexpansion

for /r "%SOURCE_DIR%" %%f in (*.java) do (
    echo Compilando: %%f
)

javac -d "%BUILD_DIR%" -encoding UTF-8 -source 1.8 -target 1.8 ^
    "%SOURCE_DIR%\models\*.java" ^
    "%SOURCE_DIR%\services\*.java" ^
    "%SOURCE_DIR%\dao\*.java" ^
    "%SOURCE_DIR%\forms\*.java" ^
    "%SOURCE_DIR%\utils\*.java" ^
    "%SOURCE_DIR%\example\*.java"

if %ERRORLEVEL% EQU 0 (
    echo ✓ Compilación exitosa
) else (
    echo ✗ Error durante compilación
    exit /b 1
)
echo.

echo [4/6] Creando archivo JAR...
if not exist "dist" mkdir "dist"
cd "%BUILD_DIR%"
jar -cf "..\..\dist\invoice-module.jar" uk\chromis\pos\invoice\**\*.class 2>nul
cd ..\..

if exist "dist\invoice-module.jar" (
    echo ✓ JAR creado: dist\invoice-module.jar
) else (
    echo ! JAR no creado (opcional)
)
echo.

echo [5/6] Verificando clases compiladas...
for /r "%BUILD_DIR%" %%f in (*.class) do set /a TOTAL+=1
if not defined TOTAL set TOTAL=0
echo ✓ Total de clases compiladas: %TOTAL%
echo.

echo [6/6] Resumen de compilación:
echo    Directorio fuente: %SOURCE_DIR%
echo    Directorio salida: %BUILD_DIR%
echo    Clases compiladas: %TOTAL%
echo.

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ✓ Compilación completada exitosamente                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Próximos pasos:
echo 1. Crear tablas en BD: mysql -u usuario -p db ^< %SOURCE_DIR%\database\create_tables.sql
echo 2. Configurar archivo: invoice.properties
echo 3. Ejecutar ejemplo: java -cp %BUILD_DIR% uk.chromis.pos.invoice.example.InvoiceExample
echo.

pause
