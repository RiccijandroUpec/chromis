@echo off
setlocal enabledelayedexpansion

set JAVAC="C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot\bin\javac.exe"
set BUILD_DIR=build\classes

if not exist "%BUILD_DIR%" mkdir "%BUILD_DIR%"

echo Compilando paneles nuevos...

%JAVAC% -d %BUILD_DIR% -sourcepath "src-pos;src-data;src-beans" -encoding UTF-8 -source 1.8 -target 1.8 -cp "chromispos.jar;lib/*" ^
  src-pos\uk\chromis\pos\inventory\ProductsPanel.java ^
  src-pos\uk\chromis\pos\inventory\CategoriesPanel.java ^
  src-pos\uk\chromis\pos\inventory\TaxesPanel.java ^
  src-pos\uk\chromis\pos\customers\CustomersPanel.java ^
  src-pos\uk\chromis\pos\panels\JPanelUsers.java ^
  src-pos\uk\chromis\pos\panels\JPanelRoles.java ^
  src-pos\uk\chromis\pos\panels\JPanelAdminCentral.java ^
  src-pos\uk\chromis\pos\config\JPanelConfiguration.java ^
  src-pos\uk\chromis\pos\datalogic\DataLogicCustomers.java 2>&1

if %ERRORLEVEL% EQU 0 (
    echo OK - Actualizando JAR...
    "C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot\bin\jar.exe" uf chromispos.jar -C %BUILD_DIR% .
    echo DONE - JAR actualizado
) else (
    echo ERROR de compilacion
)
