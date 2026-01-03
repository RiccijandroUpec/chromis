# Troubleshooting - Guía de Resolución de Problemas

## 🔴 ERRORES COMUNES Y SOLUCIONES

### 1. ERROR: Certificate file not found

**Síntoma:**
```
java.io.FileNotFoundException: Certificate file not found: C:/certificados/emisor.pfx
```

**Causas posibles:**
- Ruta incorrecta en propiedades
- Archivo no existe en ubicación especificada
- Permisos de lectura insuficientes
- Ruta con espacios sin comillas

**Soluciones:**
```properties
# ✓ Correcto - Usar ruta absoluta
invoice.certificate.path=C:/certificados/emisor.pfx

# ✓ Correcto - Con espacios
invoice.certificate.path=C:/Mis Documentos/certificados/emisor.pfx

# ✗ Incorrecto - Backslashes sin escape
invoice.certificate.path=C:\certificados\emisor.pfx
```

**Verificar:**
1. Ruta existe: `ls -la C:/certificados/`
2. Archivo es accesible: `file C:/certificados/emisor.pfx`
3. Permisos correctos: `chmod 644 C:/certificados/emisor.pfx`

---

### 2. ERROR: Invalid RUC

**Síntoma:**
```
java.lang.IllegalArgumentException: RUC inválido
```

**Causas:**
- RUC no tiene 13 dígitos
- Contiene caracteres no numéricos
- Dígito verificador incorrecto

**Solución:**
```java
// RUC debe tener exactamente 13 dígitos
String ruc = "1234567890001"; // ✓ Válido
String ruc = "123456789000";  // ✗ Solo 12 dígitos
String ruc = "123456789000x"; // ✗ Contiene letra

// Validar antes de usar
if (EcuadorValidators.isValidRUC(ruc)) {
    // Usar RUC
}
```

**Verificación de dígito:**
```
Primeros 10 dígitos: 1234567890
Dígito verificador: 001 (últimos 3)
```

---

### 3. ERROR: Invalid Cedula

**Síntoma:**
```
java.lang.IllegalArgumentException: Cédula inválida
```

**Causas:**
- Cédula no tiene 10 dígitos
- Contiene caracteres
- Dígito verificador incorrecto

**Solución:**
```java
// Cédula debe tener exactamente 10 dígitos
String cedula = "1708123456"; // ✓ Válido
String cedula = "170812345";  // ✗ Solo 9 dígitos
String cedula = "170812345x"; // ✗ Contiene letra

// Usar validador
if (EcuadorValidators.isValidCedula(cedula)) {
    // Usar cédula
}

// Formatear para visualización
String formatted = EcuadorValidators.formatCedula("1708123456");
// Resultado: "170.812.345-6"
```

---

### 4. ERROR: Connection to SRI failed

**Síntoma:**
```
java.io.IOException: Connection refused
java.net.ConnectException: Connection timed out
```

**Causas:**
- Sin conexión a Internet
- Firewall bloqueando puerto 443
- URL de SRI incorrecta
- Ambiente configurado incorrectamente

**Soluciones:**

```bash
# Verificar conectividad
ping www.sri.gob.ec

# Verificar puerto HTTPS
telnet celcert.sri.gob.ec 443  # Test
telnet celcer.sri.gob.ec 443   # Producción

# Verificar proxy
curl -v https://celcert.sri.gob.ec/
```

**En código:**
```properties
# Verificar ambiente
invoice.environment=test  # ✓ celcert.sri.gob.ec
invoice.environment=production  # ✓ celcer.sri.gob.ec

# NO usar URL completa, es automática
# ✗ invoice.sri.url=https://...
```

---

### 5. ERROR: XML rejected by SRI

**Síntoma:**
```
estado>RECHAZADA
Mensaje: Estructura XML inválida
```

**Causas comunes:**
- Datos requeridos faltantes
- RUC emisor incorrecto
- Clave de acceso inválida
- Producto sin IVA configurado
- Formato de fecha incorrecto

**Verificación XML:**
```bash
# Validar XML generado
java -cp build uk.chromis.pos.invoice.example.InvoiceExample | tee invoice.xml

# Verificar estructura
xmllint --noout invoice.xml

# Verificar valores requeridos
grep -E "<razonSocial|<ruc|<claveAcceso|<codigo>" invoice.xml
```

**En código:**
```java
// Validar antes de enviar
if (invoice.getIssuer() == null) {
    throw new IllegalArgumentException("Falta emisor");
}
if (invoice.getBuyer() == null) {
    throw new IllegalArgumentException("Falta comprador");
}
if (invoice.getDetails().isEmpty()) {
    throw new IllegalArgumentException("Falta productos");
}
if (invoice.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
    throw new IllegalArgumentException("Total debe ser > 0");
}
```

---

### 6. ERROR: Database connection failed

**Síntoma:**
```
com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
```

**Causas:**
- MySQL no corriendo
- Credenciales incorrectas
- BD no existe
- Host/puerto incorrectos

**Soluciones:**

```bash
# Verificar MySQL corriendo
mysql --version
mysqld --version

# Verificar conexión
mysql -h localhost -u usuario -p -e "SELECT 1"

# Verificar BD existe
mysql -u usuario -p -e "SHOW DATABASES LIKE 'chromisdb'"

# Verificar tablas
mysql -u usuario -p chromisdb -e "SHOW TABLES LIKE 'electronic%'"
```

**Configurar correctamente:**
```properties
# En chromisposconfig.properties o código
database.server=localhost
database.port=3306
database.name=chromisdb
database.user=usuario
database.password=contraseña
database.class=com.mysql.jdbc.Driver
```

---

### 7. ERROR: Out of Memory

**Síntoma:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Causa:**
- Muchas facturas en memoria
- XML muy grande
- No hay límite de resultados

**Solución:**

```bash
# Aumentar memoria JVM
java -Xmx1024m -cp build MyApplication

# En código - paginar resultados
List<ElectronicInvoice> invoices = dao.getInvoicesByStatus(
    InvoiceStatus.SENT_TO_SRI, 
    0,     // offset
    100    // limit
);

// Procesar en lotes
for (int i = 0; i < total; i += 100) {
    List<ElectronicInvoice> batch = dao.getInvoices(i, 100);
    processBatch(batch);
    System.gc(); // Hint de garbage collection
}
```

---

### 8. ERROR: Certificate password incorrect

**Síntoma:**
```
javax.crypto.BadPaddingException: Given final block not properly padded
java.io.IOException: keystore password was incorrect
```

**Causas:**
- Contraseña del certificado incorrecta
- Certificado corrupto
- Tipo de certificado no soportado

**Solución:**

```java
// Verificar contraseña antes de usar
try {
    DigitalSignatureService service = new DigitalSignatureService(
        "certificado.pfx", 
        "contraseña"  // Verificar que sea correcta
    );
    service.loadCertificate();
    System.out.println("Certificado cargado exitosamente");
} catch (Exception e) {
    System.err.println("Error con certificado: " + e.getMessage());
    e.printStackTrace();
}

// Verificar tipo de certificado
// openssl pkcs12 -info -in certificado.pfx -passin pass:contraseña
```

---

### 9. ERROR: Access key generation failed

**Síntoma:**
```
java.lang.NumberFormatException: For input string
```

**Causa:**
- RUC contiene caracteres no numéricos
- Número secuencial inválido
- Formato de fecha incorrecto

**Solución:**

```java
// Validar inputs antes de generar clave
String ruc = "1234567890001";        // 13 dígitos
String fecha = "03/01/2026";         // DD/MM/YYYY
String secuencial = "000001";        // 9 dígitos
String codigo = "0001";              // 4 dígitos

// Validaciones
if (!ruc.matches("\\d{13}")) {
    throw new IllegalArgumentException("RUC debe tener 13 dígitos");
}
if (!fecha.matches("\\d{2}/\\d{2}/\\d{4}")) {
    throw new IllegalArgumentException("Fecha debe ser DD/MM/YYYY");
}

// Generar
String accessKey = AccessKeyGenerator.generateAccessKey(
    fecha, "01", ruc, secuencial, codigo
);

// Validar resultado
if (accessKey.length() != 49) {
    throw new IllegalStateException("Clave acceso inválida");
}
```

---

### 10. ERROR: Duplicate access key

**Síntoma:**
```
java.sql.SQLIntegrityConstraintViolationException: 
Duplicate entry for key 'access_key'
```

**Causa:**
- Factura duplicada
- Número secuencial reutilizado
- Algoritmo de generación con problema

**Solución:**

```java
// Garantizar número secuencial único
String invoiceNumber = generateUniqueInvoiceNumber();

// Usar tabla de series
InvoiceSeries series = getOrCreateSeries(ruc, "001", "001");
long nextNumber = series.getNextSequentialNumber();
series.setNextSequentialNumber(nextNumber + 1);
updateSeries(series);

// Generar con número único
String accessKey = AccessKeyGenerator.generateAccessKey(
    date, 
    docType, 
    ruc, 
    String.format("%09d", nextNumber),  // Número único
    "0001"
);

// Verificar no existe
if (dao.getInvoiceByAccessKey(accessKey) != null) {
    throw new DuplicateAccessKeyException("Clave ya existe");
}
```

---

## ⚠️ WARNINGS Y ADVERTENCIAS

### Warning: Deprecated API

```
Note: Some input files use or override a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
```

**Solución:**
- No es error, es aviso
- No afecta funcionamiento
- Actualizar cuando sea posible

### Warning: Unchecked cast

```
warning: [unchecked] unchecked cast from Object to List
```

**Solución:**
```java
// ✗ Genera warning
List list = new ArrayList();
List<String> strings = (List<String>) list;

// ✓ Correcto
List<String> strings = new ArrayList<>();
```

---

## 🔍 DEBUGGING

### Ver logs detallados

```java
// Habilitar debug
System.setProperty("java.util.logging.config.file", "logging.properties");

// Añadir en código
Logger logger = Logger.getLogger(ElectronicInvoiceService.class.getName());
logger.setLevel(Level.FINE);

// Usar logs
logger.fine("Iniciando generación XML");
logger.info("Factura generada exitosamente");
logger.warning("Certificado expira pronto");
logger.severe("Error crítico: " + e.getMessage());
```

### Ver XML generado

```java
// Guardar XML para inspección
ElectronicInvoice invoice = ...;
service.generateInvoiceXML(invoice);

// Guardar a archivo
Files.write(
    Paths.get("debug_invoice.xml"), 
    invoice.getXmlContent().getBytes(StandardCharsets.UTF_8)
);

// Mostrar en consola
System.out.println(invoice.getXmlContent());

// Validar XML
ValidatorFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
// ... validar contra esquema SRI
```

### Inspeccionar respuesta SRI

```java
// Después de enviar
invoiceService.sendToSRI(invoice);

// Ver respuesta
String response = invoice.getSriResponse();
System.out.println("Respuesta SRI:");
System.out.println(response);

// Guardar respuesta
Files.write(
    Paths.get("sri_response.xml"), 
    response.getBytes(StandardCharsets.UTF_8)
);

// Parsear respuesta
DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));
String estado = doc.getElementsByTagName("estado").item(0).getTextContent();
System.out.println("Estado: " + estado);
```

---

## 📋 CHECKLIST DE DIAGNOSTICO

Si algo no funciona:

- [ ] ¿Java instalado? `java -version`
- [ ] ¿MySQL corriendo? `mysql --version`
- [ ] ¿Código compilado? `ls build/classes`
- [ ] ¿BD con tablas? `mysql -e "SHOW TABLES"`
- [ ] ¿Properties configurado? `cat invoice.properties | head -5`
- [ ] ¿Certificado existe? `ls -la certificado.pfx`
- [ ] ¿Internet funciona? `ping www.sri.gob.ec`
- [ ] ¿Logs disponibles? `tail -f application.log`
- [ ] ¿Ejemplo funciona? `java ... InvoiceExample`
- [ ] ¿Validadores funcionan? Test unitarios pasan

---

## 📞 OBTENER AYUDA

1. **Revisar documentación:**
   - README.md
   - INTEGRATION_GUIDE.md
   - DEVELOPER_GUIDE.md

2. **Buscar en logs:**
   - application.log
   - mysql.log
   - Console output

3. **Contactar soporte:**
   - SRI: https://www.sri.gob.ec/
   - ChromisPOS: https://community.chromispos.com/

4. **Compartir información:**
   - Mensaje de error completo
   - Logs relevantes
   - Versión Java
   - Versión MySQL
   - OS (Windows/Linux/Mac)

---

**Guía de Troubleshooting - Versión 1.0.0**  
**Última actualización: 3 de Enero, 2026**
