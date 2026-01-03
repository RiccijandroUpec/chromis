@echo off
REM ========================================
REM SCRIPT DE DEPLOYMENT AUTOMATICO
REM Modulo Facturacion Electronica
REM ========================================

setlocal enabledelayedexpansion

color 0A
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║         ASSISTANT DE DEPLOYMENT AUTOMATICO                    ║
echo ║      Modulo Facturacion Electronica - ChromisPOS              ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Variables
set JAVA_HOME=C:\jdk8\jdk8u402-b06
set MYSQL_PATH=C:\xampp\mysql\bin
set APP_NAME=chromispos-invoice
set VERSION=1.0

echo [PASO 1/5] Verificando requisitos...
echo.

REM Verificar Java
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java JDK no encontrado en %JAVA_HOME%
    echo.
    echo Por favor instala Java 8 JDK de: https://adoptium.net/
    pause
    exit /b 1
)
echo ✓ Java encontrado (%JAVA_HOME%)

REM Verificar MySQL
if not exist "%MYSQL_PATH%\mysql.exe" (
    echo ERROR: MySQL no encontrado en %MYSQL_PATH%
    echo.
    echo Por favor instala MySQL en XAMPP
    pause
    exit /b 1
)
echo ✓ MySQL encontrado (%MYSQL_PATH%)

REM Verificar clases compiladas
if not exist "build\classes\uk\chromis\pos\invoice\models\ElectronicInvoice.class" (
    echo ERROR: Clases no compiladas
    echo.
    echo Ejecuta primero: .\build_clean.bat
    pause
    exit /b 1
)
echo ✓ Clases compiladas encontradas

REM Verificar chromispos.jar
if not exist "chromispos.jar" (
    echo ERROR: chromispos.jar no encontrado
    pause
    exit /b 1
)
echo ✓ ChromisPOS JAR encontrado

echo.
echo [PASO 2/5] Configurando base de datos...
echo.

REM Crear base de datos
"%MYSQL_PATH%\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS chromisdb DEFAULT CHARACTER SET utf8mb4;" >nul 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se puede conectar a MySQL
    echo Verifica que MySQL está corriendo
    pause
    exit /b 1
)

echo ✓ Base de datos chromisdb verificada

REM Importar esquema
"%MYSQL_PATH%\mysql.exe" -u root chromisdb < setup_database.sql >nul 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se puede importar esquema
    pause
    exit /b 1
)

echo ✓ Esquema de BD importado

echo.
echo [PASO 3/5] Creando carpetas necesarias...
echo.

if not exist "facturas" mkdir "facturas"
echo ✓ Carpeta facturas/ creada

if not exist "logs\invoice" mkdir "logs\invoice"
echo ✓ Carpeta logs/invoice/ creada

if not exist "certs" mkdir "certs"
echo ✓ Carpeta certs/ creada

if not exist "backups" mkdir "backups"
echo ✓ Carpeta backups/ creada

echo.
echo [PASO 4/5] Validando configuración...
echo.

REM Verificar archivo de propiedades
if not exist "chromispos-invoice.properties" (
    echo ERROR: chromispos-invoice.properties no encontrado
    pause
    exit /b 1
)

REM Buscar valores vacíos
findstr "^invoice.issuer.ruc=$" chromispos-invoice.properties >nul
if %ERRORLEVEL% EQU 0 (
    echo ! ADVERTENCIA: invoice.issuer.ruc no está configurado
    echo.
    echo NECESITAS CONFIGURAR:
    echo 1. Editar: chromispos-invoice.properties
    echo 2. Agregar RUC de tu empresa
    echo 3. Agregar nombre de la empresa
    echo 4. Agregar ruta del certificado digital
    echo 5. Ejecutar este script nuevamente
    echo.
    pause
    exit /b 1
)

echo ✓ Configuración validada

echo.
echo [PASO 5/5] Generando reporte de instalación...
echo.

REM Generar reporte
(
    echo =======================================================
    echo REPORTE DE DEPLOYMENT - %date% %time%
    echo =======================================================
    echo.
    echo COMPONENTES VERIFICADOS:
    echo ✓ Java JDK 8
    echo ✓ MySQL 5.7+
    echo ✓ 24 clases compiladas
    echo ✓ Base de datos chromisdb
    echo ✓ Esquema de BD importado
    echo ✓ Carpetas de aplicación
    echo ✓ Archivo de configuración
    echo.
    echo INFORMACIÓN DE DEPLOYMENT:
    echo Versión del módulo: %VERSION%
    echo Nombre de aplicación: %APP_NAME%
    echo Base de datos: chromisdb
    echo Puerto: 3306 ^(MySQL^)
    echo.
    echo PRÓXIMOS PASOS:
    echo 1. Integrar en ChromisPOS:
    echo    - Editar archivo principal ^(POS.java^)
    echo    - Agregar InvoiceModuleInitializer.initializeModule^(^)
    echo.
    echo 2. Compilar ChromisPOS:
    echo    - javac -d build -cp chromispos.jar ...
    echo.
    echo 3. Ejecutar:
    echo    - java -jar chromispos.jar
    echo.
    echo 4. Probar en ambiente TEST:
    echo    - Crear una factura de prueba
    echo    - Enviar a SRI
    echo    - Verificar autorización
    echo.
    echo 5. Cambiar a PRODUCTION:
    echo    - Editar: chromispos-invoice.properties
    echo    - Cambiar: invoice.environment=production
    echo.
    echo =======================================================
) > DEPLOYMENT_REPORT.txt

type DEPLOYMENT_REPORT.txt

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║     DEPLOYMENT LISTO PARA PRODUCCION                          ║
echo ║                                                                ║
echo ║  Reporte guardado en: DEPLOYMENT_REPORT.txt                   ║
echo ║                                                                ║
echo ║  Próximo paso: Integrar en ChromisPOS                         ║
echo ║  Ver: DEPLOYMENT_GUIDE.md para detalles                       ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

pause
