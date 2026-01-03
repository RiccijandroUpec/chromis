# CHECKLIST - Integración Completa de Facturación Electrónica

## ✅ ANTES DE COMENZAR

- [ ] Java 8 o superior instalado
- [ ] MySQL 5.7 o superior instalado
- [ ] ChromisPOS descargado y compilado
- [ ] Acceso a línea de comandos
- [ ] Certificado digital PFX del SRI (para producción)

## ✅ FASE 1: PREPARACIÓN DEL CÓDIGO

### 1.1 Copiar Módulo
```bash
# Copiar carpeta invoice al proyecto
cp -r src-pos/uk/chromis/pos/invoice /ruta/ChromisPOS/src-pos/uk/chromis/pos/
```
- [ ] Carpeta `invoice` copiada
- [ ] Estructura de directorios correcta
- [ ] Todos los archivos presentes

### 1.2 Verificar Archivos
```bash
# Verificar que existan todos los archivos
ls -la src-pos/uk/chromis/pos/invoice/
```
- [ ] 21 archivos Java
- [ ] 1 archivo SQL
- [ ] 3 archivos de documentación
- [ ] Carpetas: models, services, dao, forms, utils, example, database

### 1.3 Compilar Módulo
```bash
# Linux/Mac
./build_invoice.sh

# Windows
build_invoice.bat

# Manual
javac -d bin src-pos/uk/chromis/pos/invoice/**/*.java
```
- [ ] Compilación sin errores
- [ ] Clases compiladas correctamente
- [ ] No hay warnings críticos

## ✅ FASE 2: BASE DE DATOS

### 2.1 Crear Tablas
```bash
mysql -u usuario -p chromisdb < src-pos/uk/chromis/pos/invoice/database/create_tables.sql
```
- [ ] Conexión a MySQL exitosa
- [ ] 7 tablas creadas
- [ ] 3 vistas creadas
- [ ] Ningún error en script

### 2.2 Verificar Tablas
```bash
mysql -u usuario -p chromisdb
mysql> SHOW TABLES LIKE 'invoice%';
mysql> SELECT * FROM electronic_invoices LIMIT 1;
```
- [ ] Tabla `electronic_invoices` existe
- [ ] Tabla `invoice_details` existe
- [ ] Tabla `payment_methods` existe
- [ ] Tabla `sri_submission_log` existe
- [ ] Tabla `invoice_issuer_config` existe
- [ ] Tabla `invoice_series` existe
- [ ] 3 vistas creadas correctamente

### 2.3 Permisos de Base de Datos
```bash
mysql -u usuario -p chromisdb
mysql> GRANT ALL PRIVILEGES ON chromisdb.invoice* TO 'usuario'@'localhost';
mysql> FLUSH PRIVILEGES;
```
- [ ] Usuario tiene permisos de lectura
- [ ] Usuario tiene permisos de escritura
- [ ] Usuario puede crear triggers

## ✅ FASE 3: CONFIGURACIÓN

### 3.1 Archivo chromisposconfig.properties
```properties
# Agregar al final:
invoice.enabled=true
invoice.environment=test
invoice.certificate.path=C:/certificados/emisor.pfx
invoice.certificate.password=contraseña
invoice.issuer.ruc=1234567890001
invoice.issuer.businessName=MI EMPRESA S.A.
invoice.issuer.tradeName=MI NEGOCIO
```
- [ ] Archivo existe
- [ ] Propiedades agregadas
- [ ] Valores válidos
- [ ] Ruta de certificado correcta

### 3.2 Archivo invoice.properties
```bash
# Copiar y personalizar
cp src-pos/uk/chromis/pos/invoice/invoice.properties .
# Editar con datos del emisor
```
- [ ] Archivo creado en raíz
- [ ] RUC del emisor válido (13 dígitos)
- [ ] Razón social completa
- [ ] Dirección especificada
- [ ] Email válido
- [ ] Teléfono presente
- [ ] Ambiente configurado (test/producción)

### 3.3 Validar Configuración
```bash
# Verificar archivos properties
grep -n "invoice" chromisposconfig.properties
cat invoice.properties | head -20
```
- [ ] Propiedades cargan sin errores
- [ ] Valores accesibles por código
- [ ] Rutas de carpetas correctas

## ✅ FASE 4: CERTIFICADO DIGITAL

### 4.1 Obtener Certificado
```bash
# Certificado debe ser:
# - Archivo PFX o P12
# - Válido y no expirado
# - Emitido por autoridad ecuatoriana
# - Contraseña conocida
```
- [ ] Certificado PFX obtenido
- [ ] Ubicado en ruta segura
- [ ] Contraseña guardada
- [ ] Respaldo realizado

### 4.2 Validar Certificado (Opcional)
```bash
# Verificar certificado
openssl pkcs12 -info -in certificado.pfx -passin pass:contraseña
```
- [ ] Certificado válido
- [ ] No expirado
- [ ] Contiene clave privada

### 4.3 Actualizar Configuración
- [ ] Ruta de certificado en properties
- [ ] Contraseña encriptada (futuro)
- [ ] Respaldo del certificado

## ✅ FASE 5: INTEGRACIÓN EN CHROMISPOS

### 5.1 Agregar al Menú Principal
```java
// En JFramePrincipal o menú principal
JMenu menuVentas = new JMenu("Ventas");
JMenuItem itemFacturacion = new JMenuItem("Facturación Electrónica");
itemFacturacion.addActionListener(e -> abrirFacturacionElectronica());
menuVentas.add(itemFacturacion);
```
- [ ] Opción de menú creada
- [ ] Click funciona
- [ ] Panel se abre correctamente

### 5.2 Crear Instancia de Servicio
```java
// En clase principal de ChromisPOS
private static ElectronicInvoiceService invoiceService;

public static void initializeInvoiceService() {
    invoiceService = new ElectronicInvoiceService();
    try {
        invoiceService.initialize("certificado.pfx", "password", false);
    } catch (Exception e) {
        logger.error("Error inicializando facturación", e);
    }
}
```
- [ ] Servicio inicializado
- [ ] Certificado cargado
- [ ] Sin errores de compilación

### 5.3 Integrar Paneles
```java
// Agregar paneles a la interfaz
CreateInvoicePanel createPanel = new CreateInvoicePanel(invoiceService);
InvoiceListPanel listPanel = new InvoiceListPanel(invoiceService);
InvoiceConfigurationPanel configPanel = new InvoiceConfigurationPanel(invoiceService);

// Agregar a tabbedPane o ventana
tabbedPane.addTab("Crear Factura", createPanel);
tabbedPane.addTab("Mis Facturas", listPanel);
tabbedPane.addTab("Configuración", configPanel);
```
- [ ] Paneles agregados
- [ ] Pestañas visibles
- [ ] Funcionalidad básica trabaja

### 5.4 Compilar ChromisPOS
```bash
javac -d bin -cp lib/* src-pos/**/*.java
```
- [ ] Sin errores de compilación
- [ ] Sin warnings críticos
- [ ] Todas las clases compilan

## ✅ FASE 6: PRUEBAS BÁSICAS

### 6.1 Ejecutar Ejemplo
```bash
java -cp build/classes uk.chromis.pos.invoice.example.InvoiceExample
```
- [ ] Ejemplo ejecuta sin errores
- [ ] XML se genera correctamente
- [ ] Datos se muestran en consola
- [ ] Clave de acceso generada

### 6.2 Prueba de Validadores
```java
// Prueba validadores
assertTrue(EcuadorValidators.isValidRUC("1234567890001"));
assertTrue(EcuadorValidators.isValidCedula("1708123456"));
assertTrue(EcuadorValidators.isValidEmail("usuario@example.com"));
```
- [ ] RUC válido acepta
- [ ] RUC inválido rechaza
- [ ] Cédula válida acepta
- [ ] Cédula inválida rechaza
- [ ] Email válido acepta

### 6.3 Prueba de Generación XML
```java
// Crear factura de prueba
ElectronicInvoice invoice = new ElectronicInvoice();
// ... llenar datos ...
service.generateInvoiceXML(invoice);
// Verificar XML generado
assertNotNull(invoice.getXmlContent());
assertTrue(invoice.getXmlContent().contains("<factura>"));
```
- [ ] XML genera sin errores
- [ ] XML contiene estructura correcta
- [ ] Clave de acceso está presente
- [ ] Datos del emisor incluidos

### 6.4 Prueba de Base de Datos
```java
// Insertar y recuperar factura
Connection conn = getConnection();
ElectronicInvoiceDAO dao = new ElectronicInvoiceDAO(conn);
dao.insertInvoice(invoice);
ElectronicInvoice retrieved = dao.getInvoiceById(invoice.getId());
assertNotNull(retrieved);
assertEquals(invoice.getInvoiceNumber(), retrieved.getInvoiceNumber());
```
- [ ] Factura se inserta en BD
- [ ] Se puede recuperar
- [ ] Datos coinciden
- [ ] Totales correctos

## ✅ FASE 7: PRUEBAS CON SRI

### 7.1 Ambiente Test
```java
// Configurar para ambiente test
invoiceService.initialize("cert.pfx", "password", false); // false = test
```
- [ ] Ambiente configurado en test
- [ ] URL correcta: celcert.sri.gob.ec
- [ ] No requiere certificado válido

### 7.2 Envío de Prueba
```java
// Enviar factura de prueba al SRI
ElectronicInvoice testInvoice = createTestInvoice();
service.processInvoice(testInvoice);
// Verificar respuesta
String status = testInvoice.getStatus().getDisplayName();
System.out.println("Estado: " + status);
```
- [ ] Se puede conectar a SRI
- [ ] XML se envía correctamente
- [ ] Se recibe respuesta
- [ ] Respuesta se procesa
- [ ] Estado se actualiza

### 7.3 Consultar Estado
```java
// Consultar estado en SRI
String accessKey = invoice.getAccessKey();
String status = sriService.queryAuthorizationStatus(accessKey);
System.out.println(status);
```
- [ ] Consulta ejecuta sin errores
- [ ] Se recibe respuesta SRI
- [ ] Estado es válido

## ✅ FASE 8: PRUEBAS DE INTERFAZ

### 8.1 Panel de Creación
- [ ] Campos aceptan entrada
- [ ] Tabla de productos funciona
- [ ] Botón "Agregar Producto" funciona
- [ ] Totales se calculan correctamente
- [ ] Botón "Generar XML" funciona

### 8.2 Panel de Listado
- [ ] Lista muestra facturas
- [ ] Filtros funcionan
- [ ] Click en fila selecciona
- [ ] Botones de acción responden
- [ ] Descarga de XML funciona

### 8.3 Panel de Configuración
- [ ] Campos editables
- [ ] Validación de RUC funciona
- [ ] Selección de certificado funciona
- [ ] Ambiente se puede cambiar
- [ ] Configuración se guarda

## ✅ FASE 9: DOCUMENTACIÓN

### 9.1 Revisar Documentación
- [ ] README.md leído
- [ ] GETTING_STARTED.md revisado
- [ ] INTEGRATION_GUIDE.md consultado
- [ ] DEVELOPER_GUIDE.md entendido
- [ ] Ejemplos funcionan

### 9.2 Crear Documentación Local
- [ ] Guía de instalación en equipo
- [ ] Pasos de configuración documentados
- [ ] Certificado guardado de forma segura
- [ ] Credenciales anotadas

### 9.3 Capacitación de Usuarios
- [ ] Usuarios saben crear facturas
- [ ] Usuarios entienden proceso
- [ ] Usuarios pueden resolver problemas básicos
- [ ] Manual de usuario disponible

## ✅ FASE 10: MIGRACIÓN A PRODUCCIÓN

### 10.1 Preparación
- [ ] Ambiente test validado completamente
- [ ] Certificado digital válido obtenido
- [ ] BD en producción lista
- [ ] Backup de datos realizado
- [ ] Plan de rollback disponible

### 10.2 Cambio de Ambiente
```properties
# Cambiar en properties:
invoice.environment=production
# URL cambia automáticamente a: celcer.sri.gob.ec
```
- [ ] Ambiente cambiado a producción
- [ ] URL verificada
- [ ] Certificado válido configurado

### 10.3 Prueba en Producción
- [ ] Facturas se pueden crear
- [ ] Se envían al SRI correctamente
- [ ] Se reciben autorizaciones
- [ ] Datos se persisten

### 10.4 Monitoreo
- [ ] Sistema funcionando sin errores
- [ ] BD respaldada regularmente
- [ ] Logs revisados periódicamente
- [ ] Soporte disponible

## 📋 SUMA TOTAL DE CHECKLIST

- **Fase 1 (Código):** 3 checklist
- **Fase 2 (BD):** 3 checklist
- **Fase 3 (Configuración):** 3 checklist
- **Fase 4 (Certificado):** 3 checklist
- **Fase 5 (Integración):** 4 checklist
- **Fase 6 (Pruebas básicas):** 4 checklist
- **Fase 7 (Pruebas SRI):** 3 checklist
- **Fase 8 (Interfaz):** 3 checklist
- **Fase 9 (Documentación):** 3 checklist
- **Fase 10 (Producción):** 4 checklist

**Total: 33 checklist principales**

## 🎯 TIEMPO ESTIMADO

- Fase 1: 15 minutos
- Fase 2: 20 minutos
- Fase 3: 30 minutos
- Fase 4: 20 minutos
- Fase 5: 45 minutos
- Fase 6: 30 minutos
- Fase 7: 30 minutos
- Fase 8: 30 minutos
- Fase 9: 20 minutos
- Fase 10: 1 hora

**Total estimado: 4-5 horas de integración completa**

## 📞 SOPORTE

Si encuentra problemas:
1. Revisar DEVELOPER_GUIDE.md
2. Verificar logs de aplicación
3. Consultar especificaciones SRI
4. Revisar archivos de ejemplo

---

**Checklist creado:** 3 de Enero, 2026  
**Versión:** 1.0.0  
**Estado:** Completo
