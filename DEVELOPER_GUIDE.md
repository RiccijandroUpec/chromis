# Guía para Desarrolladores - Módulo Facturación Electrónica

## 🎓 Introducción para Nuevos Desarrolladores

Este documento proporciona orientación para desarrolladores que trabajarán en el módulo de Facturación Electrónica de ChromisPOS para Ecuador.

## 📚 Comenzar

### 1. Entender la Arquitectura

El módulo sigue una arquitectura en capas:

```
Presentación (Forms)
    ↓
Lógica de Negocio (Services)
    ↓
Acceso a Datos (DAO)
    ↓
Base de Datos (MySQL)
```

### 2. Estructura de Paquetes

```
uk.chromis.pos.invoice
├── models          → Objetos de dominio (POJO)
├── services        → Lógica de negocio
├── dao             → Acceso a datos
├── forms           → Interfaz gráfica (Swing)
├── utils           → Funciones auxiliares
├── integrations    → Integraciones externas
└── example         → Ejemplos de uso
```

### 3. Patrones Utilizados

- **DAO Pattern**: Abstracción de datos
- **Factory Pattern**: Creación de DAOs
- **Service Pattern**: Lógica de negocio
- **MVC Pattern**: Separación de responsabilidades

## 🔧 Configuración del Entorno

### Requisitos
- JDK 8+
- Maven o similar
- MySQL 5.7+
- IDE (Eclipse, IntelliJ, NetBeans)

### Pasos de Configuración

1. **Clonar/Descargar el proyecto**
   ```bash
   git clone <repository>
   cd ChromisPOS
   ```

2. **Compilar**
   ```bash
   javac -d bin src-pos/uk/chromis/pos/invoice/**/*.java
   ```

3. **Base de datos**
   ```bash
   mysql -u usuario -p base_datos < src-pos/uk/chromis/pos/invoice/database/create_tables.sql
   ```

4. **Configurar properties**
   ```bash
   cp src-pos/uk/chromis/pos/invoice/invoice.properties .
   # Editar con datos del emisor
   ```

## 📖 Cómo Usar el Módulo

### Crear una Factura Programáticamente

```java
// 1. Inicializar servicio
ElectronicInvoiceService service = new ElectronicInvoiceService();
service.initialize("certificado.pfx", "password", false); // false=test

// 2. Crear factura
ElectronicInvoice invoice = new ElectronicInvoice();
invoice.setId(UUID.randomUUID().toString());
invoice.setInvoiceNumber("000001");
invoice.setIssueDate(LocalDateTime.now());

// 3. Datos del emisor
InvoiceIssuer issuer = new InvoiceIssuer("1234567890001", "MI EMPRESA");
issuer.setAddress("Calle Principal 123");
issuer.setCity("Quito");
issuer.setProvince("Pichincha");
invoice.setIssuer(issuer);

// 4. Datos del comprador
InvoiceBuyer buyer = new InvoiceBuyer("1708123456", "C");
buyer.setBusinessName("Cliente");
buyer.setEmail("cliente@example.com");
invoice.setBuyer(buyer);

// 5. Agregar detalles
InvoiceDetail detail = new InvoiceDetail();
detail.setCode("001");
detail.setDescription("Producto A");
detail.setQuantity(new BigDecimal("1"));
detail.setUnitPrice(new BigDecimal("100.00"));
detail.setTaxCode("2");
detail.setTaxRate(new BigDecimal("12"));
detail.setLineTotal(new BigDecimal("100.00"));
invoice.getDetails().add(detail);

// 6. Calcular totales
invoice.setSubtotal(new BigDecimal("100.00"));
invoice.setIvaTotal(new BigDecimal("12.00"));
invoice.setTotal(new BigDecimal("112.00"));

// 7. Agregar método de pago
PaymentMethod payment = new PaymentMethod("01", new BigDecimal("112.00"));
invoice.getPaymentMethods().add(payment);

// 8. Procesar
service.processInvoice(invoice);

// 9. Verificar estado
if (invoice.getStatus() == InvoiceStatus.AUTHORIZED) {
    System.out.println("Autorizada: " + invoice.getAuthorizationNumber());
}
```

## 🧪 Pruebas

### Unit Tests Recomendados

```java
// Test: Validación de RUC
@Test
public void testValidRUC() {
    assertTrue(EcuadorValidators.isValidRUC("1234567890001"));
    assertFalse(EcuadorValidators.isValidRUC("123456789000x"));
}

// Test: Generación de clave acceso
@Test
public void testAccessKeyGeneration() {
    String key = AccessKeyGenerator.generateAccessKey(
        "03/01/2026", "01", "1234567890001", "000001", "0001"
    );
    assertEquals(49, key.length());
}

// Test: Validación de XML
@Test
public void testXMLGeneration() throws Exception {
    ElectronicInvoice invoice = createTestInvoice();
    InvoiceXMLGenerator generator = new InvoiceXMLGenerator();
    String xml = generator.generateXML(invoice);
    assertNotNull(xml);
    assertTrue(xml.contains("<factura>"));
}
```

### Pruebas en Ambiente Test SRI

1. Usar URL: `celcert.sri.gob.ec`
2. Datos pueden ser ficticios
3. No requiere certificado válido (parcialmente)
4. Verificar respuestas de validación

## 🐛 Debugging

### Logs Útiles

```java
// Habilitar debug de XML
invoice.setXmlContent(xml);
System.out.println("XML Generado:");
System.out.println(invoice.getXmlContent());

// Verificar firma
boolean isValid = signatureService.validateSignature(invoice);
System.out.println("Firma válida: " + isValid);

// Ver respuesta SRI
System.out.println("Respuesta SRI:");
System.out.println(invoice.getSriResponse());
```

### Problemas Comunes

| Problema | Causa | Solución |
|----------|-------|----------|
| "Certificate not found" | Ruta incorrecta | Verificar ruta en properties |
| "Invalid RUC" | RUC incorrecto | Usar RUC válido |
| "Connection failed" | Sin Internet | Verificar conectividad |
| "XML rejected" | Estructura inválida | Revisar XML generado |

## 📋 Checkpoints de Desarrollo

### Antes de hacer commit:
- [ ] Código compila sin errores
- [ ] Pruebas unitarias pasan
- [ ] Documentación actualizada
- [ ] No hay funcionalidad rota
- [ ] Sigue convenciones de código

### Convenciones de Código

```java
// Nombres de clases: PascalCase
public class ElectronicInvoice { }

// Nombres de métodos: camelCase
public void generateInvoice() { }

// Constantes: UPPER_CASE
public static final String VAT_CODE = "2";

// Variables privadas: _miVariable o miVariable
private String _invoiceNumber;

// Documentación: JavaDoc para públicos
/**
 * Genera el XML de la factura
 * @param invoice Factura a procesar
 * @return XML generado
 * @throws Exception Si ocurre error
 */
public String generateXML(ElectronicInvoice invoice) throws Exception { }
```

## 🚀 Características Futuras para Implementar

### Corto Plazo (1-2 sprints)
```java
// Generación de PDF
public class InvoicePDFGenerator {
    public void generatePDF(ElectronicInvoice invoice, String path) { }
}

// Envío por email
public class InvoiceEmailService {
    public void sendByEmail(ElectronicInvoice invoice, String recipient) { }
}

// Reintentos de envío
public class SRIRetryService {
    public void retryFailedInvoices() { }
}
```

### Mediano Plazo (1-2 meses)
```java
// Notas de crédito
public class CreditNoteService { }

// Notas de débito
public class DebitNoteService { }

// Gestión de retenciones
public class WithholdingService { }

// Reportes avanzados
public class InvoiceReportService { }
```

### Largo Plazo (3+ meses)
```java
// API REST
@RestController
@RequestMapping("/api/invoice")
public class InvoiceController { }

// Dashboard
public class InvoiceDashboard { }

// Integración con ERP
public class ERPIntegrationService { }
```

## 📊 Base de Datos

### Agregar Nueva Tabla

```sql
-- En create_tables.sql
CREATE TABLE IF NOT EXISTS nueva_tabla (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    campo1 VARCHAR(255),
    campo2 DECIMAL(10, 2),
    
    CONSTRAINT fk_invoice FOREIGN KEY (invoice_id) 
        REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    
    KEY idx_invoice_id (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Crear DAO para Nueva Tabla

```java
public class NuevaTablaDAO {
    private Connection connection;
    
    public void insert(String invoiceId, Object data) throws SQLException {
        String sql = "INSERT INTO nueva_tabla (invoice_id, campo1, campo2) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            // ... más parámetros
            pstmt.executeUpdate();
        }
    }
    
    // Agregar al factory
}
```

## 🔐 Seguridad

### Validación de Entrada

```java
// Siempre validar entrada del usuario
if (ruc == null || ruc.isEmpty()) {
    throw new IllegalArgumentException("RUC no puede estar vacío");
}

if (!EcuadorValidators.isValidRUC(ruc)) {
    throw new IllegalArgumentException("RUC inválido");
}
```

### Manejo de Excepciones

```java
try {
    service.processInvoice(invoice);
} catch (SQLException e) {
    logger.error("Error de base de datos", e);
    throw new InvoiceException("Error al procesar factura", e);
} catch (Exception e) {
    logger.error("Error inesperado", e);
    invoice.setStatus(InvoiceStatus.REJECTED);
}
```

### Encriptación de Contraseñas

```java
// Para futuras mejoras
public class PasswordEncryptor {
    public static String encrypt(String password) {
        // Usar algoritmo seguro (BCrypt, scrypt, etc)
    }
}
```

## 📞 Recursos Útiles

- [Especificaciones SRI](https://www.sri.gob.ec/)
- [JavaDoc API](https://docs.oracle.com/javase/8/docs/api/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)

## 👥 Contacto

Para preguntas sobre el desarrollo:
- Revisar documentación en `invoice/`
- Consultar ejemplos en `InvoiceExample.java`
- Contactar equipo de desarrollo

---

**Última actualización**: 3 de Enero, 2026
**Versión**: 1.0.0
**Mantenedor**: Equipo de Desarrollo ChromisPOS
