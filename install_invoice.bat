@echo off
REM ========================================
REM INSTALACION COMPLETA - MODULO FACTURACION
REM ========================================

setlocal enabledelayedexpansion

color 0A
cls

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║   INSTALACION - MODULO FACTURACION ELECTRONICA                ║
echo ║   Para ChromisPOS - Ecuador                                   ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Configuraciones
set JAVA_HOME=C:\jdk8\jdk8u402-b06
set JAVAC=%JAVA_HOME%\bin\javac.exe
set MYSQL=C:\xampp\mysql\bin\mysql.exe
set SOURCE_DIR=src-pos\uk\chromis\pos\invoice
set BUILD_DIR=build\classes
set INSTALL_DIR=uk\chromis\pos\invoice

REM Verificar Java
echo [1/7] Verificando Java...
if not exist "%JAVAC%" (
    echo ERROR: JDK no encontrado
    pause
    exit /b 1
)
echo OK - JDK encontrado
echo.

REM Compilar módulo principal
echo [2/7] Compilando módulo de integración...
"%JAVAC%" -d "%BUILD_DIR%" -encoding UTF-8 -source 1.8 -target 1.8 ^
    InvoiceModule.java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se compiló InvoiceModule.java
    pause
    exit /b 1
)
echo OK - Módulo de integración compilado
echo.

REM Copiar clases compiladas
echo [3/7] Copiando clases compiladas...
if exist "%INSTALL_DIR%" rmdir /s /q "%INSTALL_DIR%"
mkdir "%INSTALL_DIR%"

xcopy "%BUILD_DIR%\uk\chromis\pos\invoice" "%INSTALL_DIR%" /S /I /Y >nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se pudieron copiar las clases
    pause
    exit /b 1
)
echo OK - Clases copiadas
echo.

REM Crear directorios de salida
echo [4/7] Creando directorios de aplicación...
if not exist "facturas" mkdir "facturas"
if not exist "logs\invoice" mkdir "logs\invoice"
echo OK - Directorios creados
echo.

REM Verificar base de datos
echo [5/7] Verificando base de datos...
"%MYSQL%" -u root -e "USE chromisdb; SELECT COUNT(*) as tables_count FROM information_schema.tables WHERE table_schema='chromisdb';" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo AVISO: Base de datos no está disponible
    echo Se creará automáticamente en primera ejecución
) else (
    echo OK - Base de datos verificada
)
echo.

REM Crear archivo README
echo [6/7] Creando documentación de instalación...
(
echo # Instalacion Modulo Facturacion Electronica
echo.
echo ## Completado exitosamente en: %date% %time%
echo.
echo ### Proximos pasos:
echo.
echo 1. **Configurar certificado digital**
echo    - Editar chromispos-invoice.properties
echo    - Agregar ruta al archivo P12/PFX
echo    - Agregar contraseña
echo.
echo 2. **Iniciar ChromisPOS**
echo    - El módulo se cargará automáticamente
echo    - Disponible en menú Administración ^> Facturación
echo.
echo 3. **Crear primera factura**
echo    - Ir a Ventas ^> Nueva Factura Electrónica
echo    - Completar datos de cliente
echo    - Generar y enviar a SRI
echo.
echo ### Estructura instalada:
echo.
echo - uk/chromis/pos/invoice/           - Código compilado
echo - build/classes/                    - Clases (.class)
echo - chromispos-invoice.properties     - Configuración
echo - facturas/                         - Salida de XML
echo - logs/invoice/                     - Registros
echo.
echo ### Soporte:
echo.
echo Contactar a: soporte@chromispos.com
echo Documentación: /docs/INVOICE_MODULE.md
) > INVOICE_INSTALL_LOG.txt

echo OK - Documentación creada
echo.

REM Resumen final
echo [7/7] Finalizando...
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                                                                ║
echo ║   INSTALACION COMPLETADA EXITOSAMENTE                         ║
echo ║                                                                ║
echo ║   El módulo de facturación está listo para usar               ║
echo ║                                                                ║
echo ║   Próximo paso: Configurar certificado digital                ║
echo ║                                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Detalles en: INVOICE_INSTALL_LOG.txt
echo.
pause
