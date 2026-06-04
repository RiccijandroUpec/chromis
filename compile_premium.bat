@echo off
setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  COMPILANDO PANELES PREMIUM                                   ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Usar Java de Eclipse Adoptium
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"

REM Verificar que existe
if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo ✗ javac NO encontrado en %JAVA_HOME%
    exit /b 1
)

echo ✓ Encontrado javac en: %JAVA_HOME%

echo ✓ Encontrado javac en: %JAVA_HOME%
echo.
echo [1/3] Preparando directorios de compilación...
if not exist "build\classes" mkdir "build\classes"
echo ✓ Directorio listo
echo.

echo [2/3] Compilando JPanelDashboardCentral.java...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelDashboardCentral.java" 2>&1

if !ERRORLEVEL! NEQ 0 (
    echo ✗ Error compilando JPanelDashboardCentral
    exit /b 1
)
echo ✓ JPanelDashboardCentral compilado exitosamente
echo.

echo [2.5/3] Compilando JPanelAdminPremium.java...
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "build\classes" ^
    -sourcepath "src-pos;src-data;src-beans" ^
    -cp "chromispos.jar;lib\*" ^
    "src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java" 2>&1

if !ERRORLEVEL! NEQ 0 (
    echo ✗ Error compilando JPanelAdminPremium
    exit /b 1
)
echo ✓ JPanelAdminPremium compilado exitosamente
echo.

echo [3/3] Actualizando chromispos.jar...
"%JAVA_HOME%\bin\jar.exe" uf "chromispos.jar" -C "build\classes" . 2>&1

if !ERRORLEVEL! NEQ 0 (
    echo ✗ Error actualizando JAR
    exit /b 1
)
echo ✓ chromispos.jar actualizado
echo.

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ✓ COMPILACIÓN COMPLETADA CON ÉXITO                           ║
echo ║  Paneles Premium están listos para usar                       ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

pause
