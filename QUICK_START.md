# Guía Rápida - Quick Start

## ⚡ Instalación Rápida (5 minutos)

### 1. Compilar
```bash
# Linux/Mac
./build_invoice.sh

# Windows
build_invoice.bat
```

### 2. Base de Datos
```bash
mysql -u root -p chromisdb < src-pos/uk/chromis/pos/invoice/database/create_tables.sql
```

### 3. Configurar
```bash
# Editar archivo
nano invoice.properties

# Cambiar valores:
invoice.issuer.ruc=1234567890001
invoice.issuer.businessName=Mi Empresa
invoice.certificate.path=/ruta/certificado.pfx
invoice.certificate.password=micontraseña
```

### 4. Prueba Rápida
```bash
java -cp build/classes uk.chromis.pos.invoice.example.InvoiceExample
```

## 📋 Estructura del Módulo

```
src-pos/uk/chromis/pos/invoice/
├── models/                     # Modelos de datos
│   ├── ElectronicInvoice.java
│   ├── InvoiceIssuer.java
│   ├── InvoiceBuyer.java
│   ├── InvoiceDetail.java
│   ├── PaymentMethod.java
│   └── InvoiceStatus.java
├── services/                   # Servicios (lógica)
│   ├── ElectronicInvoiceService.java
│   ├── InvoiceXMLGenerator.java
│   ├── DigitalSignatureService.java
│   └── SRIIntegrationService.java
├── dao/                        # Acceso a datos
│   ├── ElectronicInvoiceDAO.java
│   ├── InvoiceDetailDAO.java
│   ├── PaymentMethodDAO.java
│   └── InvoiceDAOFactory.java
├── forms/                      # Interfaz Swing
│   ├── CreateInvoicePanel.java
│   ├── InvoiceListPanel.java
│   └── InvoiceConfigurationPanel.java
├── utils/                      # Utilidades
│   ├── AccessKeyGenerator.java
│   ├── EcuadorValidators.java
│   └── InvoiceConstants.java
├── example/                    # Ejemplos
│   └── InvoiceExample.java
└── database/
    └── create_tables.sql       # Esquema MySQL
```

## 🚀 Flujo de Trabajo

```
1. Crear Factura
   └─> Llenar datos de emisor, comprador, productos
   
2. Generar XML
   └─> Validar datos
   └─> Crear estructura XML SRI
   └─> Generar clave de acceso (49 dígitos)
   
3. Firmar XML
   └─> Cargar certificado digital
   └─> Crear firma PKCS#7
   └─> Envolver XML firmado
   
4. Enviar a SRI
   └─> Conectar a servidor SRI
   └─> Enviar XML via SOAP/HTTPS
   └─> Recibir respuesta
   
5. Autorizar
   └─> SRI valida XML
   └─> Retorna número de autorización
   └─> Guardar en BD
```

## 📝 Crear una Factura (Ejemplo)

```java
// 1. Instanciar servicio
ElectronicInvoiceService service = new ElectronicInvoiceService();
service.initialize("cert.pfx", "password", false); // false=test

// 2. Crear factura
ElectronicInvoice invoice = new ElectronicInvoice();

// 3. Configurar emisor
InvoiceIssuer issuer = new InvoiceIssuer();
issuer.setRuc("1234567890001");
issuer.setBusinessName("Mi Empresa S.A.");
invoice.setIssuer(issuer);

// 4. Configurar comprador
InvoiceBuyer buyer = new InvoiceBuyer();
buyer.setIdentification("1708123456");
buyer.setName("Juan García");
invoice.setBuyer(buyer);

// 5. Agregar productos
InvoiceDetail detail = new InvoiceDetail();
detail.setCode("001");
detail.setDescription("Producto");
detail.setQuantity(new BigDecimal("1"));
detail.setUnitPrice(new BigDecimal("100.00"));
detail.setTaxRate(new BigDecimal("12")); // IVA 12%
invoice.addDetail(detail);

// 6. Procesar (generar, firmar, enviar)
service.processInvoice(invoice);

// 7. Verificar resultado
System.out.println("Estado: " + invoice.getStatus());
System.out.println("Clave: " + invoice.getAccessKey());
System.out.println("Autorización: " + invoice.getAuthorizationNumber());
```

## 🔑 Claves de Acceso

Formato 49 dígitos:
```
DDMMYY + TipoDoc + RUC(13) + Secuencial(9) + CódigoAuth(4) + DigVerif(1)

Ejemplo:
030126 + 01 + 1234567890001 + 000000001 + 0001 + 8

Generador: AccessKeyGenerator.generateAccessKey(...)
```

## 🎯 Validadores

```java
// Validar RUC (13 dígitos)
EcuadorValidators.isValidRUC("1234567890001");

// Validar Cédula (10 dígitos)
EcuadorValidators.isValidCedula("1708123456");

// Validar Email
EcuadorValidators.isValidEmail("usuario@example.com");

// Formatear
EcuadorValidators.formatRUC("1234567890001");    // 123.456.789-000-1
EcuadorValidators.formatCedula("1708123456");     // 170.812.345-6
```

## 💾 BD - Tablas Principales

| Tabla | Propósito |
|-------|-----------|
| `electronic_invoices` | Facturas principales |
| `invoice_details` | Productos/servicios |
| `payment_methods` | Formas de pago |
| `sri_submission_log` | Registro de envíos |
| `invoice_issuer_config` | Config del emisor |
| `invoice_series` | Control de numeración |

## ⚙️ Propiedades Principales

```properties
# Ambiente: test o production
invoice.environment=test

# RUC emisor
invoice.issuer.ruc=1234567890001

# Certificado digital
invoice.certificate.path=/ruta/certificado.pfx
invoice.certificate.password=contraseña

# Datos BD
database.url=jdbc:mysql://localhost:3306/chromisdb
database.user=usuario
database.password=contraseña
```

## 🔗 URLs SRI

| Ambiente | URL |
|----------|-----|
| Test | https://celcert.sri.gob.ec/comprobantes-electronicos-ws/WebServices/ValidarComprobante |
| Producción | https://celcer.sri.gob.ec/comprobantes-electronicos-ws/WebServices/ValidarComprobante |

## 📊 Codigos SRI

### Tipos de Documento
- `01`: Factura
- `03`: Liquidación
- `04`: Nota de Crédito
- `05`: Nota de Débito

### Códigos de Impuesto
- `2`: IVA (12% habitual)
- `3`: ICE
- `5`: IRBPNR

### Códigos de Pago
- `01`: Efectivo
- `02`: Cheque
- `03`: Transferencia bancaria
- `04`: Tarjeta de crédito
- `05`: Tarjeta de débito
- `06`: Dinero electrónico
- `20`: Otra forma

## ❓ Problemas Frecuentes

| Problema | Solución |
|----------|----------|
| Certificado no encontrado | Verificar ruta en properties |
| RUC inválido | Debe tener 13 dígitos |
| Error de BD | Verificar credenciales MySQL |
| Conexión SRI fallida | Verificar internet y firewall |
| XML rechazado | Ver detalles en `sri_submission_log` |

## 📞 Recursos

- Documentación completa: `README.md`
- Guía de integración: `INTEGRATION_GUIDE.md`
- Guía para developers: `DEVELOPER_GUIDE.md`
- Troubleshooting: `TROUBLESHOOTING.md`
- Checklist: `INTEGRATION_CHECKLIST.md`

## ✅ Verificación Final

```bash
# Verificar compilación
ls -la build/classes/uk/chromis/pos/invoice/**/*.class | wc -l

# Verificar BD
mysql -e "SELECT COUNT(*) as tablas FROM information_schema.TABLES WHERE TABLE_SCHEMA='chromisdb' AND TABLE_NAME LIKE 'invoice%';"

# Verificar configuración
grep -c "invoice" chromisposconfig.properties

# Ejecutar ejemplo
java -cp build/classes uk.chromis.pos.invoice.example.InvoiceExample | grep "Estado:"
```

---

**Guía Rápida - Versión 1.0.0**  
**Para más detalles, ver documentación completa**
