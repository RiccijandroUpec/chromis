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
    exit /b 1
)

echo [1/3] Limpiando...
if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
echo OK
echo.

echo [2/3] Compilando 21 clases...
echo.

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

if !ERRORLEVEL! NEQ 0 (
    echo.
    echo ERROR: Compilacion fallida
    echo ERRORLEVEL: !ERRORLEVEL!
    exit /b 1
)

echo.
echo OK - Compilacion completada
echo.

echo [3/3] Contando clases...
setlocal enabledelayedexpansion
set COUNT=0
for /r "%BUILD_DIR%" %%f in (*.class) do set /a COUNT+=1

echo Total: !COUNT! clases compiladas
echo.

if !COUNT! EQU 21 (
    echo ======================================================
    echo   SUCCESS: 21 CLASES COMPILADAS EXITOSAMENTE
    echo ======================================================
) else (
    echo.
    echo Se compilaron !COUNT! clases (se esperaban 21)
)

echo.
echo COMPILACION FINALIZADA
