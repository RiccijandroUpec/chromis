# Facturación Electrónica Ecuador - ChromisPOS

## Descripción General
Módulo completo para generar, firmar y enviar facturas electrónicas según las especificaciones del SRI (Servicio de Rentas Internas) de Ecuador.

## Estructura del Módulo

```
src-pos/uk/chromis/pos/invoice/
├── models/
│   ├── ElectronicInvoice.java      - Modelo principal de factura
│   ├── InvoiceIssuer.java          - Información del emisor
│   ├── InvoiceBuyer.java           - Información del comprador
│   ├── InvoiceDetail.java          - Detalles de productos/servicios
│   ├── PaymentMethod.java          - Métodos de pago
│   └── InvoiceStatus.java          - Estados de factura
├── services/
│   ├── ElectronicInvoiceService.java    - Servicio principal
│   ├── InvoiceXMLGenerator.java         - Generación de XML
│   ├── DigitalSignatureService.java     - Firma digital
│   └── SRIIntegrationService.java       - Integración SRI
├── utils/
│   └── AccessKeyGenerator.java      - Generación de clave de acceso
├── integrations/
│   └── (Integraciones adicionales)
└── README.md                        - Este archivo
```

## Funcionalidades Implementadas

### 1. Generación de Clave de Acceso
- Generación automática de claves de acceso SRI (49 dígitos)
- Cálculo del dígito verificador mediante módulo 11
- Formato según especificaciones SRI

### 2. Generación de XML
- Estructura XML completa según formato SRI
- Información tributaria
- Datos del emisor y comprador
- Detalles de productos/servicios
- Cálculo automático de totales e impuestos
- Información de pagos

### 3. Firma Digital (Pendiente de implementación)
- Soporte para certificados digitales
- Firma PKCS#7
- Validación de firmas

### 4. Integración SRI (Pendiente de implementación)
- Envío a web service del SRI
- Consulta de estado de autorización
- Gestión de respuestas

## Requisitos

### Certificado Digital
- Certificado digital válido emitido por autoridad competente
- Formato: PFX o similar
- Contraseña configurada

### Dependencias Java
```xml
<dependency>
    <groupId>javax.xml.bind</groupId>
    <artifactId>jaxb-api</artifactId>
    <version>2.3.1</version>
</dependency>
```

### Librerías Recomendadas (a agregar al lib/)
- BouncyCastle: Firma digital PKCS#7
- Apache PDFBox: Generación de PDF
- Httpclient: Comunicación con SRI

## Uso Básico

```java
// 1. Crear servicio
ElectronicInvoiceService invoiceService = new ElectronicInvoiceService();
invoiceService.initialize("ruta/certificado.pfx", "contraseña", false);

// 2. Crear factura
ElectronicInvoice invoice = new ElectronicInvoice();
invoice.setIssuer(issuer);
invoice.setBuyer(buyer);
invoice.setDetails(details);

// 3. Procesar (genera XML, firma y envía)
invoiceService.processInvoice(invoice);

// 4. Obtener estado
String status = invoiceService.getInvoiceStatus(invoice);
```

## Configuración en chromisposconfig.properties

```properties
# Facturación Electrónica
invoice.enabled=true
invoice.environment=test
invoice.certificate.path=/ruta/al/certificado.pfx
invoice.certificate.password=contraseña

# Información del emisor
invoice.issuer.ruc=1234567890001
invoice.issuer.businessName=Mi Empresa S.A.
invoice.issuer.tradeName=Mi Negocio

# SRI Web Service
invoice.sri.url.test=https://celcert.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes
invoice.sri.url.production=https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes
```

## Estados de Factura
- **DRAFT**: Borrador (no firmada)
- **GENERATED**: Generada (XML creado)
- **SIGNED**: Firmada digitalmente
- **SENT_TO_SRI**: Enviada al SRI
- **AUTHORIZED**: Autorizada por el SRI
- **REJECTED**: Rechazada por el SRI
- **CANCELLED**: Cancelada

## Códigos de Documento
- **01**: Factura

## Códigos de Forma de Pago
- **01**: Efectivo
- **16**: Tarjeta débito
- **17**: Tarjeta crédito
- **19**: Cheque
- **20**: Transferencia bancaria
- **21**: Wallet (billetera digital)

## Códigos de Impuesto (IVA)
- **2**: IVA
- **3**: ICE
- **5**: IRBPNR

## Porcentajes de IVA
- **0**: 0%
- **2**: 5%
- **3**: 12%

## Próximos Pasos

1. **Implementar Firma Digital**
   - Agregar BouncyCastle al proyecto
   - Implementar generación de firma PKCS#7
   - Validación de firma

2. **Implementar Integración SRI**
   - Conexión a web service
   - Envío de facturas
   - Consulta de estado

3. **Persistencia en Base de Datos**
   - DAO para facturas electrónicas
   - Almacenamiento de XMLs
   - Historial de envíos

4. **Interfaz de Usuario**
   - Formulario de configuración
   - Vista de facturas electrónicas
   - Consulta de estado SRI

5. **Reportes**
   - Facturas enviadas
   - Facturas autorizadas
   - Facturas rechazadas

## Referencias
- [SRI - Facturación Electrónica](https://www.sri.gob.ec)
- [Especificaciones Técnicas SRI](https://www.sri.gob.ec/o/sri/documents)

## Licencia
Mismo que ChromisPOS
