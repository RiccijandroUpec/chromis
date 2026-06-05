@echo off
REM Usar javac desde la instalación existente de Java

setlocal enabledelayedexpansion

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  BÚSQUEDA AUTOMÁTICA DE COMPILADOR JAVA                       ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Buscar carpeta de Java
where javac >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=*" %%I in ('where javac') do (
        set JAVAC_PATH=%%~dpI
        set JAVA_HOME=!JAVAC_PATH:\bin\=!
        echo ✓ Encontrado javac en PATH: !JAVA_HOME!
        goto FOUND
    )
)

for /d %%D in ("C:\Program Files\Eclipse Adoptium\*") do (
    if exist "%%D\bin\javac.exe" (
        echo ✓ Encontrado javac en: %%D
        set JAVA_HOME=%%D
        goto FOUND
    )
)

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
dir /s /b "%SOURCE_DIR%\*.java" > sources.txt
echo src-pos\uk\chromis\pos\sales\JPanelTicket.java >> sources.txt
echo src-pos\uk\chromis\pos\forms\StartPOS.java >> sources.txt
echo src-pos\uk\chromis\commons\dbmanager\DatabaseValidator.java >> sources.txt
echo src-pos\uk\chromis\pos\repair\DatabaseRepair.java >> sources.txt
echo src-pos\uk\chromis\pos\forms\AppUser.java >> sources.txt
echo src-pos\uk\chromis\pos\forms\AppLocal.java >> sources.txt
echo src-pos\uk\chromis\pos\forms\JRootApp.java >> sources.txt
echo src-pos\uk\chromis\pos\sales\JTicketLines.java >> sources.txt
echo src-pos\uk\chromis\pos\forms\JPrincipalApp.java >> sources.txt
echo src-beans\uk\chromis\beans\JPasswordDialog.java >> sources.txt
echo src-pos\uk\chromis\pos\inventory\ProductsPanel.java >> sources.txt
echo src-pos\uk\chromis\pos\inventory\CategoriesPanel.java >> sources.txt
echo src-pos\uk\chromis\pos\inventory\TaxesPanel.java >> sources.txt
echo src-pos\uk\chromis\pos\customers\CustomersPanel.java >> sources.txt
echo src-pos\uk\chromis\pos\panels\JPanelUsers.java >> sources.txt
echo src-pos\uk\chromis\pos\panels\JPanelRoles.java >> sources.txt
echo src-pos\uk\chromis\pos\panels\JPanelAdminCentral.java >> sources.txt
echo src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java >> sources.txt
echo src-pos\uk\chromis\pos\config\JPanelConfiguration.java >> sources.txt
"%JAVA_HOME%\bin\javac.exe" -d "%BUILD_DIR%" -sourcepath "src-pos;src-data;src-beans" -encoding UTF-8 -source 1.8 -target 1.8 -cp "chromispos.jar;lib/*" @sources.txt
del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo ✓ Compilación exitosa
    echo [2.5/3] Actualizando chromispos.jar con clases nuevas...
    "%JAVA_HOME%\bin\jar.exe" uf chromispos.jar -C "%BUILD_DIR%" .
    if !ERRORLEVEL! EQU 0 (
        echo ✓ chromispos.jar actualizado correctamente con las clases compiladas
    ) else (
        echo ✗ Error al actualizar chromispos.jar
        exit /b 1
    )
) else (
    echo ✗ Error durante compilación
    if exist sources.txt del sources.txt
    exit /b 1
)
echo.

echo [3/3] Verificando clases...
for /r "%BUILD_DIR%" %%f in (*.class) do set /a TOTAL+=1
if not defined TOTAL set TOTAL=0
echo ✓ Total de clases: %TOTAL%
echo.

echo ╔════════════════════════════════════════════════════════════════╗
echo ║  ✓ COMPILACIÓN COMPLETADA - %TOTAL% CLASES ENCONTRADAS         ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

