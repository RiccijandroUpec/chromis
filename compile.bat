@echo off
setlocal enabledelayedexpansion

set JAVAC=C:\jdk8\jdk8u402-b06\bin\javac.exe
set SOURCE_DIR=src-pos\uk\chromis\pos\invoice
set BUILD_DIR=build\classes

echo ======================================================
echo   COMPILACION - Facturacion Electronica
echo ======================================================
echo.

if not exist "%JAVAC%" (
    echo ERROR: javac no encontrado en %JAVAC%
    pause
    exit /b 1
)

echo [1/3] Limpiando...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%" >nul
mkdir "%BUILD_DIR%" >nul
echo OK
echo.

echo [2/3] Compilando 21 clases...

"%JAVAC%" -d "%BUILD_DIR%" -encoding UTF-8 -source 1.8 -target 1.8 ^
    "%SOURCE_DIR%\models\ElectronicInvoice.java" ^
    "%SOURCE_DIR%\models\InvoiceIssuer.java" ^
    "%SOURCE_DIR%\models\InvoiceBuyer.java" ^
    "%SOURCE_DIR%\models\InvoiceDetail.java" ^
    "%SOURCE_DIR%\models\PaymentMethod.java" ^
    "%SOURCE_DIR%\models\InvoiceStatus.java" ^
    "%SOURCE_DIR%\services\ElectronicInvoiceService.java" ^
    "%SOURCE_DIR%\services\InvoiceXMLGenerator.java" ^
    "%SOURCE_DIR%\services\DigitalSignatureService.java" ^
    "%SOURCE_DIR%\services\SRIIntegrationService.java" ^
    "%SOURCE_DIR%\dao\ElectronicInvoiceDAO.java" ^
    "%SOURCE_DIR%\dao\InvoiceDetailDAO.java" ^
    "%SOURCE_DIR%\dao\PaymentMethodDAO.java" ^
    "%SOURCE_DIR%\dao\InvoiceDAOFactory.java" ^
    "%SOURCE_DIR%\forms\CreateInvoicePanel.java" ^
    "%SOURCE_DIR%\forms\InvoiceListPanel.java" ^
    "%SOURCE_DIR%\forms\InvoiceConfigurationPanel.java" ^
    "%SOURCE_DIR%\utils\AccessKeyGenerator.java" ^
    "%SOURCE_DIR%\utils\EcuadorValidators.java" ^
    "%SOURCE_DIR%\utils\InvoiceConstants.java" ^
    "%SOURCE_DIR%\example\InvoiceExample.java"

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilacion fallida
    pause
    exit /b 1
)
echo OK
echo.

echo [3/3] Contando clases...
setlocal enabledelayedexpansion
set COUNT=0
for /r "%BUILD_DIR%" %%f in (*.class) do set /a COUNT+=1
echo Total: !COUNT! clases
echo.

if !COUNT! EQU 21 (
    echo ======================================================
    echo   SUCCESS: 21 CLASES COMPILADAS
    echo ======================================================
) else (
    echo WARNING: Se compilaron !COUNT! clases (esperaba 21)
)

pause

REM Variables
set SOURCE_DIR=src-pos\uk\chromis\pos\invoice
set BUILD_DIR=build\classes

echo ✓ javac encontrado - compilando...
echo.

echo [1/3] Limpiando directorios...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
echo ✓ Directorios limpios
echo.

echo [2/3] Compilando código Java...
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
    pause
    exit /b 1
)
echo.

echo [3/3] Verificando clases compiladas...
for /r "%BUILD_DIR%" %%f in (*.class) do set /a TOTAL+=1
if not defined TOTAL set TOTAL=0
echo ✓ Total de clases compiladas: %TOTAL%
echo.

if %TOTAL% EQU 21 (
    echo ╔════════════════════════════════════════════════════════════════╗
    echo ║  ✓ COMPILACIÓN COMPLETADA EXITOSAMENTE                        ║
    echo ║  ✓ %TOTAL% clases compiladas correctamente                       ║
    echo ╚════════════════════════════════════════════════════════════════╝
    echo.
    echo Próximos pasos:
    echo   1. Crear BD: mysql -u usuario -p chromisdb ^< create_tables.sql
    echo   2. Configurar: invoice.properties
    echo   3. Probar: java -cp build/classes InvoiceExample
    echo.
) else (
    echo ! Advertencia: Se compilaron %TOTAL% clases (esperaba 21)
)

pause
