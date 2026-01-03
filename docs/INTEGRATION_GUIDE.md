# Guía de Integración - Módulo Facturación Electrónica ChromisPOS

## 1. Instalación Automática

### Paso 1: Ejecutar instalador

```powershell
cd C:\xampp\htdocs\chromispos\ChromisPOS
.\install_invoice.bat
```

Este script:
- ✓ Compila el módulo de integración
- ✓ Copia clases compiladas
- ✓ Crea directorios necesarios
- ✓ Verifica base de datos
- ✓ Genera documentación

### Paso 2: Configurar certificado digital

Editar: `chromispos-invoice.properties`

```properties
# Ruta al certificado P12/PFX
invoice.certificate.path=C:\certs\mi-empresa.pfx

# Contraseña del certificado
invoice.certificate.password=mi_contraseña

# Modo: test o production
invoice.environment=test
```

### Paso 3: Integración en ChromisPOS

En el archivo principal de ChromisPOS (POS.java o equivalente), agregar:

```java
import uk.chromis.pos.invoice.integration.InvoiceModuleInitializer;

// En método de inicialización:
public void initializeModules() {
    // ... otros módulos ...
    
    // Inicializar módulo de facturación
    if (InvoiceModuleInitializer.initializeModule()) {
        System.out.println("✓ Módulo de facturación cargado");
        InvoiceModuleInitializer.showWelcomeDialog();
        
        // Agregar paneles al menú
        addInvoiceMenuItems();
    }
}

// Agregar opciones al menú
private void addInvoiceMenuItems() {
    JMenu menuFacturacion = new JMenu("Facturación Electrónica");
    
    JMenuItem itemCrear = new JMenuItem("Nueva Factura");
    itemCrear.addActionListener(e -> 
        showPanel(InvoiceModuleInitializer.getCreateInvoicePanel())
    );
    
    JMenuItem itemListar = new JMenuItem("Mis Facturas");
    itemListar.addActionListener(e -> 
        showPanel(InvoiceModuleInitializer.getInvoiceListPanel())
    );
    
    JMenuItem itemConfig = new JMenuItem("Configuración");
    itemConfig.addActionListener(e -> 
        showPanel(InvoiceModuleInitializer.getConfigurationPanel())
    );
    
    menuFacturacion.add(itemCrear);
    menuFacturacion.add(itemListar);
    menuFacturacion.addSeparator();
    menuFacturacion.add(itemConfig);
    
    menuBar.add(menuFacturacion);
}
```

## 2. Estructura de Directorios

```
ChromisPOS/
├── uk/chromis/pos/invoice/
│   ├── models/              (6 clases)
│   ├── services/            (4 clases)
│   ├── dao/                 (4 clases)
│   ├── forms/               (3 paneles Swing)
│   ├── utils/               (3 utilidades)
│   ├── example/             (ejemplo de uso)
│   ├── integration/         (integración ChromisPOS)
│   └── InvoiceModule.java   (clase principal)
├── build/classes/           (clases compiladas)
├── chromispos-invoice.properties  (configuración)
├── setup_database.sql       (esquema BD)
└── facturas/                (salida de XMLs)
```

## 3. Usar el módulo en código

### Obtener instancia

```java
import uk.chromis.pos.invoice.InvoiceModule;

InvoiceModule module = InvoiceModule.getInstance();
```

### Crear factura

```java
import uk.chromis.pos.invoice.models.*;
import java.math.BigDecimal;

// Crear emisor
InvoiceIssuer issuer = new InvoiceIssuer();
issuer.setRuc("1234567890001");
issuer.setBusinessName("MI EMPRESA");

// Crear comprador
InvoiceBuyer buyer = new InvoiceBuyer();
buyer.setIdentification("1234567890");
buyer.setBusinessName("CLIENTE");

// Crear detalle
InvoiceDetail detail = new InvoiceDetail();
detail.setProductCode("001");
detail.setDescription("Producto");
detail.setQuantity(new BigDecimal("2"));
detail.setUnitPrice(new BigDecimal("50.00"));

// Crear factura
List<InvoiceDetail> details = new ArrayList<>();
details.add(detail);

List<PaymentMethod> payments = new ArrayList<>();
PaymentMethod payment = new PaymentMethod();
payment.setPaymentMethod("01"); // Efectivo
payments.add(payment);

ElectronicInvoice invoice = module.createInvoice(issuer, buyer, details, payments);

// Generar XML
String xml = module.generateXML(invoice);

// Firmar
String signedXml = module.signInvoice(xml);

// Enviar a SRI (automático)
module.getInvoiceService().submitToSRI(signedXml);
```

### Consultar facturas

```java
ElectronicInvoiceDAO invoiceDAO = module.getInvoiceDAO();

// Obtener todas las facturas de un RUC
List<ElectronicInvoice> invoices = invoiceDAO.findByIssuerRuc("1234567890001");

// Obtener factura por clave de acceso
ElectronicInvoice invoice = invoiceDAO.findByAccessKey("0301202601123456789000100000000100013");

// Obtener facturas pendientes de envío
List<ElectronicInvoice> pending = invoiceDAO.findPendingSRI();
```

## 4. Validaciones

```java
import uk.chromis.pos.invoice.utils.EcuadorValidators;

EcuadorValidators validators = InvoiceModule.getValidators();

// Validar RUC
if (validators.validateRuc("1234567890001")) {
    System.out.println("RUC válido");
}

// Validar Cédula
if (validators.validateIdentification("1234567890")) {
    System.out.println("Cédula válida");
}
```

## 5. Generar clave de acceso

```java
import uk.chromis.pos.invoice.utils.AccessKeyGenerator;

AccessKeyGenerator keyGen = InvoiceModule.getKeyGenerator();

// Generar clave de acceso de 49 dígitos
String accessKey = keyGen.generateAccessKey(
    "01",           // Tipo de comprobante (01=Factura)
    "03",           // Mes (01-12)
    "01",           // Día (01-31)
    "2026",         // Año
    "1234567890001",// RUC emisor
    "001",          // Punto emisión
    "001",          // Punto de venta
    "000000001",    // Número secuencial
    "13"            // Código de la naturaleza
);

System.out.println("Clave de acceso: " + accessKey);
```

## 6. Configuración de base de datos

Crear base de datos automáticamente:

```powershell
cd C:\xampp\htdocs\chromispos\ChromisPOS
mysql -u root < setup_database.sql
```

Tablas creadas:
- **electronic_invoices** - Facturas electrónicas
- **invoice_details** - Líneas de facturas
- **payment_methods** - Métodos de pago
- **invoice_configuration** - Configuración
- **sri_submission_log** - Log de envíos a SRI

Vistas SQL:
- **v_invoices_summary** - Resumen de facturas
- **v_invoices_pending_sri** - Pendientes de SRI
- **v_sri_submission_history** - Historial de envíos

## 7. Certificado Digital

### Obtener certificado Ecuador

1. Ir a: https://www.sri.gob.ec/
2. Descargar certificado digital
3. Convertir a formato PKCS#12 (.pfx) si es necesario
4. Configurar en `chromispos-invoice.properties`

### Convertir PEM a PFX

```bash
openssl pkcs12 -export -in certificate.pem -inkey key.pem -out certificate.pfx
```

## 8. Troubleshooting

### Error: "Certificado no encontrado"
- Verificar ruta en chromispos-invoice.properties
- Verificar formato (.pfx o .p12)
- Verificar contraseña

### Error: "Base de datos no disponible"
- Verificar MySQL está corriendo en XAMPP
- Ejecutar setup_database.sql
- Verificar credenciales en properties

### Error: "Conexión a SRI fallida"
- Verificar ambiente (test vs production)
- Verificar certificado es válido
- Verificar conexión a Internet
- Ver logs en: logs/invoice/

## 9. Logs

Ver estado del módulo:

```java
System.out.println(InvoiceModuleInitializer.getModuleStatus());
```

Logs disponibles en:
- `logs/invoice/module.log` - Log general del módulo
- `logs/invoice/sri.log` - Comunicación con SRI
- `logs/invoice/signature.log` - Firmas digitales

## 10. Soporte y Documentación

- Documentación: Ver /docs/
- Ejemplos: uk/chromis/pos/invoice/example/InvoiceExample.java
- Panel de configuración: InvoiceConfigurationPanel
- Panel de creación: CreateInvoicePanel
- Listado: InvoiceListPanel

## 11. Checksum de Instalación

Verificar instalación correcta:

```powershell
# Verificar clases compiladas
Get-ChildItem -Path "uk\chromis\pos\invoice" -Recurse -Filter "*.class" | Measure-Object

# Debe mostrar: 22 clases

# Verificar base de datos
mysql -u root chromisdb -e "SHOW TABLES;"
```

## 12. Desinstalación

Para remover el módulo:

```powershell
# Remover directorios
Remove-Item -Path "uk\chromis\pos\invoice" -Recurse
Remove-Item -Path "build\classes\uk\chromis\pos\invoice" -Recurse
Remove-Item -Path "facturas" -Recurse
Remove-Item -Path "logs\invoice" -Recurse

# Remover configuración (opcional)
Remove-Item -Path "chromispos-invoice.properties"

# Remover base de datos (si lo desea)
mysql -u root -e "DROP DATABASE chromisdb;"
```

---

**Versión:** 1.0  
**Fecha:** 3 de Enero, 2026  
**Compatibilidad:** ChromisPOS 4.0+, Java 8+, MySQL 5.7+
