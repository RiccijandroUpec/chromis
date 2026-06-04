#!/usr/bin/env powershell
# ChromisPOS Premium Compilation Script

$JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-11.0.29.7-hotspot"
$projectDir = Get-Location
$buildDir = "$projectDir\build\classes"

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  COMPILANDO PANELES PREMIUM PARA ChromisPOS                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Verificar Java
if (-not (Test-Path "$JAVA_HOME\bin\javac.exe")) {
    Write-Host "✗ javac NO encontrado en $JAVA_HOME" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Encontrado Java en: $JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Crear directorio de compilación
Write-Host "[1/3] Preparando directorios..." -ForegroundColor Yellow
if (-not (Test-Path $buildDir)) {
    New-Item -ItemType Directory -Path $buildDir -Force | Out-Null
}
Write-Host "✓ Directorio listo: $buildDir" -ForegroundColor Green
Write-Host ""

# Compilar JPanelDashboardCentral
Write-Host "[2/3] Compilando JPanelDashboardCentral.java..." -ForegroundColor Yellow
& "$JAVA_HOME\bin\javac.exe" `
    -encoding UTF-8 `
    -d $buildDir `
    -sourcepath "src-pos;src-data;src-beans" `
    -cp "chromispos.jar;lib\*" `
    "src-pos\uk\chromis\pos\panels\JPanelDashboardCentral.java" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Error compilando JPanelDashboardCentral" -ForegroundColor Red
    exit 1
}
Write-Host "✓ JPanelDashboardCentral compilado exitosamente" -ForegroundColor Green
Write-Host ""

# Compilar JPanelAdminPremium
Write-Host "[2.5/3] Compilando JPanelAdminPremium.java..." -ForegroundColor Yellow
& "$JAVA_HOME\bin\javac.exe" `
    -encoding UTF-8 `
    -d $buildDir `
    -sourcepath "src-pos;src-data;src-beans" `
    -cp "chromispos.jar;lib\*" `
    "src-pos\uk\chromis\pos\panels\JPanelAdminPremium.java" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Error compilando JPanelAdminPremium" -ForegroundColor Red
    exit 1
}
Write-Host "✓ JPanelAdminPremium compilado exitosamente" -ForegroundColor Green
Write-Host ""

# Actualizar JAR
Write-Host "[3/3] Actualizando chromispos.jar..." -ForegroundColor Yellow
& "$JAVA_HOME\bin\jar.exe" uf chromispos.jar -C $buildDir . 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Error actualizando JAR" -ForegroundColor Red
    exit 1
}
Write-Host "✓ chromispos.jar actualizado correctamente" -ForegroundColor Green
Write-Host ""

Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✓ COMPILACIÓN COMPLETADA CON ÉXITO                           ║" -ForegroundColor Green
Write-Host "║  Los paneles premium están listos para usar                   ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "Próximo paso: Ejecuta run_chromispos.bat para ver los cambios" -ForegroundColor Cyan
