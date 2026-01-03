# Módulo de Facturación Electrónica para ChromisPOS - Ecuador

## 📋 Descripción General

Módulo completo y profesional para generar, firmar y enviar facturas electrónicas según los requisitos del **SRI (Servicio de Rentas Internas)** de Ecuador.

Incluye:
- ✅ Generación de XML conforme a normas SRI
- ✅ Firma digital PKCS#7
- ✅ Integración con web service del SRI
- ✅ Almacenamiento en base de datos
- ✅ Interfaz gráfica (Swing)
- ✅ Validadores ecuatorianos
- ✅ Documentación completa

## 🎯 Características

### Funcionalidades Principales
- Crear facturas electrónicas completas
- Generar clave de acceso automática SRI (49 dígitos)
- Generar XML según especificaciones SRI
- Firmar digitalmente con certificado PKCS#7
- Enviar a web service del SRI
- Consultar estado de autorización
- Almacenar y gestionar facturas
- Generar reportes

### Validaciones Integradas
- RUC ecuatoriano (13 dígitos con verificación)
- Cédula ecuatoriana (10 dígitos con verificación)
- Email válido
- Datos requeridos por SRI
- Cálculo automático de impuestos

### Seguridad
- Firma digital con certificados
- Validación de datos de entrada
- Auditoría de cambios
- HTTPS para comunicación con SRI
- Encriptación de contraseñas (preparado)

## 📦 Contenido del Módulo

```
invoice/
├── models/                  # Modelos de datos (6)
├── services/               # Servicios principales (4)
├── dao/                    # Acceso a datos (4)
├── forms/                  # Interfaz gráfica (3)
├── utils/                  # Utilidades (3)
├── integrations/           # Integraciones
├── database/               # Scripts SQL
├── example/                # Ejemplos
├── README.md               # Esta documentación
├── INTEGRATION_GUIDE.md    # Guía de integración
└── invoice.properties      # Configuración
```

## 🚀 Inicio Rápido

### 1. Instalación
```bash
cd /ruta/chromispos
./install_invoice.sh
```

O manualmente:
```bash
# Crear tablas
mysql -u usuario -p base_datos < src-pos/uk/chromis/pos/invoice/database/create_tables.sql

# Compilar
javac -d bin src-pos/uk/chromis/pos/invoice/**/*.java
```

### 2. Configuración
```properties
# En chromisposconfig.properties
invoice.enabled=true
invoice.environment=test
invoice.certificate.path=/ruta/certificado.pfx
invoice.certificate.password=contraseña
```

### 3. Uso Básico
```java
// Crear servicio
ElectronicInvoiceService service = new ElectronicInvoiceService();
service.initialize("cert.pfx", "password", false); // false = test

// Crear factura
ElectronicInvoice invoice = new ElectronicInvoice();
// ... completar datos ...

// Procesar (genera XML, firma y envía)
service.processInvoice(invoice);

// Verificar estado
String status = invoice.getStatus().getDisplayName();
```

## 📚 Documentación

| Documento | Descripción |
|-----------|-------------|
| [README.md](README.md) | Descripción técnica completa |
| [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) | Cómo integrar en ChromisPOS |
| [InvoiceExample.java](example/InvoiceExample.java) | Ejemplo de código funcional |
| [create_tables.sql](database/create_tables.sql) | Script de base de datos |

## 🔧 Requisitos

### Hardware
- Procesador: 1 GHz mínimo
- Memoria RAM: 512 MB mínimo (1 GB recomendado)
- Disco: 100 MB disponibles

### Software
- Java 8 o superior
- MySQL 5.7 o superior
- ChromisPOS 1.5+
- Certificado digital válido del SRI

### Certificado Digital
- Obtener en: https://www.sri.gob.ec/
- Formato: PFX o P12
- Emisor: Autoridad competente ecuatoriana

## 📋 Estados de Factura

```
DRAFT         → Borrador (no firmada)
GENERATED     → Generada (XML creado)
SIGNED        → Firmada digitalmente
SENT_TO_SRI   → Enviada al SRI
AUTHORIZED    → Autorizada por SRI ✅
REJECTED      → Rechazada por SRI ❌
CANCELLED     → Cancelada
```

## 🌍 Ambientes SRI

| Ambiente | URL | Certificado | Datos |
|----------|-----|-------------|-------|
| **Test** | celcert.sri.gob.ec | Parcial | Prueba |
| **Producción** | celcer.sri.gob.ec | Válido | Real |

## 📊 Estructura de Base de Datos

### Tabla Principal: electronic_invoices
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | VARCHAR(36) | ID único (UUID) |
| invoice_number | VARCHAR(20) | Número secuencial |
| access_key | VARCHAR(49) | Clave de acceso SRI |
| issue_date | DATETIME | Fecha y hora emisión |
| status | VARCHAR(30) | Estado actual |
| total | DECIMAL(10,2) | Total a pagar |
| sent_to_sri | BOOLEAN | Enviada al SRI |
| authorization_number | VARCHAR(49) | Número autorización |

### Tablas Relacionadas
- invoice_details: Productos/servicios
- payment_methods: Formas de pago
- sri_submission_log: Auditoría
- invoice_issuer_config: Configuración
- invoice_series: Series de numeración

## 💡 Ejemplos

### Crear una Factura Simple
```java
// Ver archivo: example/InvoiceExample.java
ElectronicInvoice invoice = new ElectronicInvoice();

// Emisor
InvoiceIssuer issuer = new InvoiceIssuer("1234567890001", "MI EMPRESA");
invoice.setIssuer(issuer);

// Comprador
InvoiceBuyer buyer = new InvoiceBuyer("1708123456", "C");
invoice.setBuyer(buyer);

// Producto
InvoiceDetail detail = new InvoiceDetail("001", "Producto A", 
    new BigDecimal("1"), new BigDecimal("100.00"));
invoice.getDetails().add(detail);

// Calcular totales
invoice.setSubtotal(new BigDecimal("100.00"));
invoice.setIvaTotal(new BigDecimal("12.00"));
invoice.setTotal(new BigDecimal("112.00"));
```

### Integración con Módulo de Ventas
```java
// En cuando se completa una venta
public void onSaleComplete(Ticket ticket) {
    if (invoiceConfig.isElectronic()) {
        ElectronicInvoice inv = convertTicketToInvoice(ticket);
        invoiceService.processInvoice(inv);
    }
}
```

## 🔐 Validadores Disponibles

```java
// Validar RUC
EcuadorValidators.isValidRUC("1234567890001"); // true

// Validar Cédula
EcuadorValidators.isValidCedula("1708123456"); // true

// Formatear
EcuadorValidators.formatRUC("1234567890001"); // 123.456.789-0001

// Validar Email
EcuadorValidators.isValidEmail("email@example.com"); // true

// Obtener Provincia
EcuadorValidators.getProvinceFromCode("10"); // "Pichincha"
```

## 🎨 Interfaz de Usuario

### Paneles Disponibles
1. **CreateInvoicePanel**
   - Crear nuevas facturas
   - Agregar productos
   - Calcular totales
   - Botones de acción

2. **InvoiceListPanel**
   - Ver facturas generadas
   - Filtrar por estado
   - Descargar XML
   - Cancelar facturas

3. **InvoiceConfigurationPanel**
   - Configurar emisor
   - Seleccionar certificado
   - Ambiente (test/producción)
   - Pruebas de conexión

## 🐛 Troubleshooting

| Problema | Solución |
|----------|----------|
| "Certificate file not found" | Verificar ruta en properties |
| "Invalid RUC format" | RUC debe tener 13 dígitos |
| "Connection to SRI failed" | Verificar Internet, URL SRI, ambiente |
| "XML rejected by SRI" | Revisar estructura XML, datos requeridos |

## 📞 Soporte

- **Documentación**: Ver carpeta `invoice/`
- **Ejemplos**: Ver `invoice/example/InvoiceExample.java`
- **SRI**: https://www.sri.gob.ec/
- **Forum**: https://community.chromispos.com/

## 📝 Licencia

Mismo que ChromisPOS

## 🔄 Versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0.0 | 03/01/2026 | Versión inicial - Todas las funciones básicas |

## ✨ Características Futuras

- [ ] Generación de PDF
- [ ] Envío por email
- [ ] Notas de crédito
- [ ] Notas de débito
- [ ] API REST
- [ ] Dashboard de reportes
- [ ] Integración con ERP

---

**Estado**: ✅ **COMPLETAMENTE IMPLEMENTADO**

Para empezar: [Guía de Integración](INTEGRATION_GUIDE.md)
