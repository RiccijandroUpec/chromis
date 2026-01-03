# Guía de Integración del Módulo de Facturación Electrónica

## 1. Estructura del Proyecto Implementado

```
src-pos/uk/chromis/pos/invoice/
├── models/                          # Modelos de datos
│   ├── ElectronicInvoice.java
│   ├── InvoiceIssuer.java
│   ├── InvoiceBuyer.java
│   ├── InvoiceDetail.java
│   ├── PaymentMethod.java
│   └── InvoiceStatus.java
├── services/                        # Servicios principales
│   ├── ElectronicInvoiceService.java  # Orquestador
│   ├── InvoiceXMLGenerator.java       # Generación XML
│   ├── DigitalSignatureService.java   # Firma digital
│   └── SRIIntegrationService.java     # Integración SRI
├── dao/                             # Data Access Objects
│   ├── ElectronicInvoiceDAO.java
│   ├── InvoiceDetailDAO.java
│   ├── PaymentMethodDAO.java
│   └── InvoiceDAOFactory.java
├── forms/                           # Interfaz Gráfica
│   ├── CreateInvoicePanel.java       # Crear facturas
│   ├── InvoiceListPanel.java         # Listar facturas
│   └── InvoiceConfigurationPanel.java # Configuración
├── utils/                           # Utilidades
│   ├── AccessKeyGenerator.java       # Generador clave acceso
│   ├── EcuadorValidators.java        # Validadores
│   └── InvoiceConstants.java         # Constantes
├── integrations/                    # Integraciones futuras
├── database/                        # Scripts SQL
│   └── create_tables.sql
├── example/                         # Ejemplos de uso
│   └── InvoiceExample.java
└── README.md                        # Este archivo
```

## 2. Instalación de Librerías

### 2.1 Librerías Requeridas
Ya están incluidas en Java estándar:
- `java.net.*` - Comunicación HTTP
- `java.security.*` - Criptografía y firma digital
- `java.xml.*` - Manejo de XML
- `java.sql.*` - Base de datos

### 2.2 Librerías Opcionales (Recomendadas)
Agregar al `/lib` si necesita funcionalidades avanzadas:

**Para firma digital PKCS#7 (Opcional):**
```
bcprov-jdk15on-1.70.jar          (BouncyCastle Provider)
bcpkix-jdk15on-1.70.jar          (BouncyCastle PKIX)
```

**Para generar PDF (Opcional):**
```
pdfbox-2.0.28.jar               (Apache PDFBox)
```

**Para HTTP mejorado (Opcional):**
```
httpclient-4.5.13.jar           (Apache HttpClient)
httpcore-4.4.15.jar             (Apache HttpCore)
commons-codec-1.15.jar          (Commons Codec)
commons-logging-1.2.jar         (Commons Logging)
```

## 3. Configuración de Base de Datos

### 3.1 Crear Tablas
```bash
mysql -u usuario -p base_datos < database/create_tables.sql
```

### 3.2 Estructura de Tablas
- `electronic_invoices` - Facturas principales
- `invoice_details` - Detalles/líneas
- `payment_methods` - Formas de pago
- `sri_submission_log` - Auditoría de envíos
- `invoice_issuer_config` - Configuración del emisor
- `invoice_series` - Series de numeración

## 4. Configuración Inicial

### 4.1 Archivo `chromisposconfig.properties`
Agregar:
```properties
# Facturación Electrónica
invoice.enabled=true
invoice.environment=test
invoice.certificate.path=C:/certificados/emisor.pfx
invoice.certificate.password=contraseña_encriptada
```

### 4.2 Archivo `invoice.properties`
Crear archivo con configuración completa (ver ejemplo en proyecto)

## 5. Integración en ChromisPOS

### 5.1 Integración en el Módulo de Ventas
En `sales/JTicketsBagWindow.java` o similar:

```java
// Importar
import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.models.ElectronicInvoice;

// Crear servicio (singleton recomendado)
private static ElectronicInvoiceService invoiceService;

// Al finalizar una venta
public void onTicketComplete(Ticket ticket) {
    if (isElectronicInvoiceEnabled()) {
        ElectronicInvoice invoice = convertTicketToInvoice(ticket);
        invoiceService.processInvoice(invoice);
    }
}

private ElectronicInvoice convertTicketToInvoice(Ticket ticket) {
    // Convertir datos del ticket a formato de factura
    // ...
}

private boolean isElectronicInvoiceEnabled() {
    // Verificar configuración
    return true;
}
```

### 5.2 Integración en Menú Principal
Agregar opción en menú:
```
Venta > Facturación Electrónica
       ├── Crear Factura
       ├── Mis Facturas
       └── Configuración
```

## 6. Flujo de Facturación Completo

```
1. CREAR FACTURA
   ├─ Datos del comprador
   ├─ Productos/servicios
   └─ Métodos de pago

2. GENERAR XML
   ├─ Validar datos
   ├─ Generar clave de acceso
   ├─ Crear estructura XML
   └─ Guardar en DB

3. FIRMAR DIGITALMENTE
   ├─ Cargar certificado
   ├─ Firmar XML con PKCS#7
   ├─ Generar XML firmado
   └─ Actualizar estado

4. ENVIAR A SRI
   ├─ Validar XML firmado
   ├─ Conectar web service SRI
   ├─ Enviar documento
   ├─ Recibir respuesta
   └─ Guardar autorización

5. FACTURA AUTORIZADA
   ├─ Guardar número de autorización
   ├─ Generar PDF/Impresión
   ├─ Archivar documentos
   └─ Enviar por email (opcional)
```

## 7. Validaciones Implementadas

### 7.1 Validaciones de Ecuador
- RUC: 13 dígitos con dígito verificador
- Cédula: 10 dígitos con dígito verificador
- Email: Formato válido
- Claves de acceso: 49 dígitos con validación módulo 11

### 7.2 Validaciones de Negocio
- Factura debe tener al menos 1 producto
- Total debe ser > 0
- Método de pago debe ser válido
- Comprador debe estar identificado

## 8. Manejo de Errores

### 8.1 Errores Comunes
```
1. Certificado inválido/expirado
   → Solución: Renew certificado con autoridad competente

2. Error de conexión a SRI
   → Solución: Verificar conectividad, URL correcta, ambiente

3. XML rechazado por SRI
   → Solución: Validar estructura XML, datos requeridos

4. Clave de acceso duplicada
   → Solución: Usar algoritmo de generación único
```

### 8.2 Logs y Auditoría
```
- Tabla `sri_submission_log` registra todos los envíos
- Guardar XML generado para auditoría
- Respuestas del SRI almacenadas en BD
```

## 9. Pruebas

### 9.1 Ambiente de Pruebas (Test)
- URL: `https://celcert.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes`
- No requiere certificado válido (parcialmente)
- Datos de prueba aceptados

### 9.2 Ambiente de Producción
- URL: `https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes`
- Requiere certificado válido
- Datos reales deben estar correctos

### 9.3 Casos de Prueba
```java
// Ver InvoiceExample.java para ejemplo completo
ElectronicInvoiceService service = new ElectronicInvoiceService();
service.initialize("cert.pfx", "password", false);

ElectronicInvoice invoice = new ElectronicInvoice();
// ... llenar datos ...
service.processInvoice(invoice);
```

## 10. Próximas Mejoras

### 10.1 Corto Plazo
- [ ] Generar PDF automático
- [ ] Envío de email con factura
- [ ] Consulta de estado SRI en background
- [ ] Reintentos automáticos de envío

### 10.2 Mediano Plazo
- [ ] Soporte para notas de crédito
- [ ] Soporte para notas de débito
- [ ] Gestión de retenciones
- [ ] Reportes avanzados

### 10.3 Largo Plazo
- [ ] Integración con sistemas de ERP
- [ ] API REST para terceros
- [ ] Portal de consulta de facturas
- [ ] Integración con servicios de pago

## 11. Referencias

- [Portal SRI](https://www.sri.gob.ec/)
- [Especificaciones Técnicas SRI](https://www.sri.gob.ec/o/sri/documents)
- [Documentación Facturación Electrónica](https://www.sri.gob.ec/comprobantes-electronicos)

## 12. Soporte

Para preguntas o problemas, contacte:
- Email: soporte@chromispos.com
- Wiki: https://wiki.chromispos.com/invoice
