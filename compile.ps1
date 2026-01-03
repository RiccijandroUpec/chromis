$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  COMPILACION - Facturacion Electronica" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

$JAVA_HOME = "C:\jdk8\jdk8u402-b06"
$JAVAC = "$JAVA_HOME\bin\javac.exe"
$SOURCE_DIR = "src-pos\uk\chromis\pos\invoice"
$BUILD_DIR = "build\classes"

if (-not (Test-Path $JAVAC)) {
    Write-Host "ERROR: javac no encontrado en $JAVAC" -ForegroundColor Red
    exit 1
}

Write-Host "✓ JDK encontrado" -ForegroundColor Green
Write-Host ""
Write-Host "[1/3] Limpiando directorios..." -ForegroundColor Yellow
if (Test-Path $BUILD_DIR) { Remove-Item $BUILD_DIR -Recurse -Force }
New-Item $BUILD_DIR -ItemType Directory -Force > $null
Write-Host "✓ Limpio" -ForegroundColor Green
Write-Host ""

Write-Host "[2/3] Compilando..." -ForegroundColor Yellow

$files = @(
    "models\ElectronicInvoice",
    "models\InvoiceIssuer",
    "models\InvoiceBuyer",
    "models\InvoiceDetail",
    "models\PaymentMethod",
    "models\InvoiceStatus",
    "services\ElectronicInvoiceService",
    "services\InvoiceXMLGenerator",
    "services\DigitalSignatureService",
    "services\SRIIntegrationService",
    "dao\ElectronicInvoiceDAO",
    "dao\InvoiceDetailDAO",
    "dao\PaymentMethodDAO",
    "dao\InvoiceDAOFactory",
    "forms\CreateInvoicePanel",
    "forms\InvoiceListPanel",
    "forms\InvoiceConfigurationPanel",
    "utils\AccessKeyGenerator",
    "utils\EcuadorValidators",
    "utils\InvoiceConstants",
    "example\InvoiceExample"
)

$javaFiles = @()
foreach ($f in $files) {
    $javaFiles += "$SOURCE_DIR\$f.java"
}

& $JAVAC -d $BUILD_DIR -encoding UTF-8 -source 1.8 -target 1.8 $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Compilacion exitosa" -ForegroundColor Green
} else {
    Write-Host "✗ Error en compilacion" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/3] Contando clases..." -ForegroundColor Yellow
$classCount = (Get-ChildItem $BUILD_DIR -Filter "*.class" -Recurse | Measure-Object).Count
Write-Host "✓ Total: $classCount clases" -ForegroundColor Green
Write-Host ""

if ($classCount -eq 21) {
    Write-Host "======================================================" -ForegroundColor Green
    Write-Host "  SUCCESS: 21 CLASES COMPILADAS" -ForegroundColor Green
    Write-Host "======================================================" -ForegroundColor Green
} else {
    Write-Host "WARNING: Esperaba 21, compiladas $classCount" -ForegroundColor Yellow
}
