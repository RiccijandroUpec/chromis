@echo off
REM Usar javac desde la instalación existente de Java

setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  BÚSQUEDA AUTOMÁTICA DE COMPILADOR JAVA                       ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Buscar carpeta de Java
for /d %%D in ("C:\Program Files\Java\*") do (
    if exist "%%D\bin\javac.exe" (
        echo ✓ Encontrado javac en: %%D
        set JAVA_HOME=%%D
        goto FOUND
    )
)

for /d %%D in ("C:\Program Files (x86)\Java\*") do (
    if exist "%%D\bin\javac.exe" (
        echo ✓ Encontrado javac en: %%D
        set JAVA_HOME=%%D
        goto FOUND
    )
)

echo ✗ javac NO encontrado
echo.
echo DEBES INSTALAR JDK (Java Development Kit) - NO solo JRE
echo.
echo 1. Desinstala lo que tengas: Panel Control ^> Programas ^> Desinstalar
echo    - Busca "Java" y desinstala TODO
echo.
echo 2. Descarga e instala OpenJDK 8:
echo    https://adoptium.net/
echo    - Selecciona "OpenJDK 8 (LTS)" 
echo    - Windows x64
echo    - JDK (NOT JRE)
echo    - Al instalar, MARCA "Add to PATH"
echo.
echo 3. Cierra PowerShell y abre UNO NUEVO
echo 4. Ejecuta: .\compile.bat
echo.
pause
exit /b 1

:FOUND
echo.
echo [1/3] Limpiando directorios...
set SOURCE_DIR=src-pos\uk\chromis\pos\invoice
set BUILD_DIR=build\classes

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
echo ✓ Directorios limpios
echo.

echo [2/3] Compilando código Java...
"%JAVA_HOME%\bin\javac.exe" -d "%BUILD_DIR%" -encoding UTF-8 -source 1.8 -target 1.8 ^
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
    pause
    exit /b 1
)
echo.

echo [3/3] Verificando clases...
for /r "%BUILD_DIR%" %%f in (*.class) do set /a TOTAL+=1
if not defined TOTAL set TOTAL=0
echo ✓ Total de clases: %TOTAL%
echo.

if %TOTAL% EQU 21 (
    echo ╔════════════════════════════════════════════════════════════════╗
    echo ║  ✓ COMPILACIÓN EXITOSA - 21 CLASES COMPILADAS                 ║
    echo ╚════════════════════════════════════════════════════════════════╝
) else (
    echo ! Se compilaron %TOTAL% clases (esperaba 21)
)

echo.
pause
