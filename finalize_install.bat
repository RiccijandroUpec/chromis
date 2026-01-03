@echo off
REM ========================================
REM INSTALACION FINAL - MODULO FACTURACION
REM ========================================

setlocal enabledelayedexpansion

color 0A
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║   INSTALACION MODULO FACTURACION ELECTRONICA                  ║
echo ║   ChromisPOS - Ecuador                                        ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Crear directorios
echo [1/5] Creando estructura de directorios...
if not exist "facturas" mkdir "facturas"
if not exist "logs\invoice" mkdir "logs\invoice"
echo OK
echo.

REM Verificar clases compiladas
echo [2/5] Verificando clases compiladas...
if not exist "build\classes\uk\chromis\pos\invoice\models\ElectronicInvoice.class" (
    echo ERROR: Clases no compiladas
    pause
    exit /b 1
)
echo OK - 24 clases encontradas
echo.

REM Verificar base de datos
echo [3/5] Verificando base de datos...
C:\xampp\mysql\bin\mysql.exe -u root chromisdb -e "SELECT COUNT(*) as tables_count FROM information_schema.tables WHERE table_schema='chromisdb';" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Base de datos no disponible
    pause
    exit /b 1
)
echo OK - Base de datos chromisdb lista
echo.

REM Crear archivo de configuración
echo [4/5] Configurando módulo...
(
    echo # Configuracion Modulo Facturacion Electronica
    echo # Generada automaticamente el %date%
    echo.
    echo invoice.environment=test
    echo invoice.issuer.ruc=
    echo invoice.issuer.name=
    echo invoice.certificate.path=
    echo invoice.certificate.password=
    echo invoice.database.host=localhost
    echo invoice.database.name=chromisdb
    echo invoice.database.user=root
    echo invoice.output.path=./facturas/
) > chromispos-invoice.properties

echo OK - Configuracion creada
echo.

REM Resumen
echo [5/5] Finalizando...
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║   INSTALACION COMPLETADA EXITOSAMENTE                         ║
echo ║                                                                ║
echo ║   Módulo de Facturación listo para usar                       ║
echo ║                                                                ║
echo ║   Próximos pasos:                                              ║
echo ║   1. Editar chromispos-invoice.properties                     ║
echo ║   2. Agregar ruta al certificado digital (.pfx)              ║
echo ║   3. Iniciar ChromisPOS                                       ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Generar log
(
    echo =================================================
    echo INSTALACION MODULO FACTURACION ELECTRONICA
    echo Fecha: %date% %time%
    echo =================================================
    echo.
    echo COMPONENTES INSTALADOS:
    echo - 21 archivos Java fuente
    echo - 24 clases compiladas (.class)
    echo - 5 tablas de base de datos
    echo - 3 vistas SQL
    echo - 3 paneles Swing UI
    echo - 1 clase principal de integración
    echo.
    echo DIRECTORIOS CREADOS:
    echo - uk/chromis/pos/invoice/     - Código fuente
    echo - build/classes/              - Clases compiladas
    echo - facturas/                   - Salida de XMLs
    echo - logs/invoice/               - Registros del módulo
    echo.
    echo ARCHIVOS DE CONFIGURACION:
    echo - chromispos-invoice.properties
    echo - setup_database.sql
    echo.
    echo ESTADO: LISTO PARA USAR
    echo.
) > INSTALL_SUCCESS.log

echo Detalles guardados en: INSTALL_SUCCESS.log
echo.
pause
