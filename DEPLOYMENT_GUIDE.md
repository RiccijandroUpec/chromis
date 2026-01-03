# 🚀 GUÍA DE DEPLOYMENT - MÓDULO FACTURACIÓN ELECTRÓNICA

## ¿QUÉ ES DEPLOYMENT?

Es el proceso de llevar el módulo desde desarrollo a un servidor de producción donde los usuarios reales pueden usarlo.

---

## 🎯 TRES OPCIONES DE DEPLOYMENT

### **OPCIÓN 1: Deployment Local (Más Rápido - 10 minutos) ⭐ RECOMENDADO PARA EMPEZAR**

Usar la misma máquina donde está ChromisPOS sin cambios adicionales.

#### Paso 1: Verificar Requisitos
```powershell
# Abrir PowerShell como Administrador
# Verificar Java
java -version
# Debe mostrar: java version "1.8.0_402"

# Verificar MySQL
mysql -u root -e "SELECT VERSION();"
# Debe mostrar versión de MySQL
```

#### Paso 2: Configurar Datos de la Empresa
Editar: `chromispos-invoice.properties`

```properties
# DATOS DE TU EMPRESA (obtener del SRI)
invoice.issuer.ruc=1712345678901
invoice.issuer.name=MI EMPRESA S.A.

# AMBIENTE: test (primero) o production (después)
invoice.environment=test

# CERTIFICADO DIGITAL (obtener del SRI)
invoice.certificate.path=C:\certs\mi-empresa.pfx
invoice.certificate.password=mi_contraseña_segura

# BASE DE DATOS (ya está lista)
invoice.database.host=localhost
invoice.database.name=chromisdb
invoice.database.user=root

# CARPETA DE SALIDA
invoice.output.path=./facturas/
```

#### Paso 3: Obtener Certificado Digital del SRI

1. **Ir a:** https://www.sri.gob.ec/
2. **Descargar:** Certificado digital para facturación electrónica
3. **Convertir a .pfx** (si es necesario):
   ```powershell
   openssl pkcs12 -export -in cert.pem -inkey key.pem -out empresa.pfx
   ```
4. **Guardar en:** `C:\certs\empresa.pfx`

#### Paso 4: Integrar en ChromisPOS

En el archivo principal (`POS.java` o equivalente):

```java
import uk.chromis.pos.invoice.integration.InvoiceModuleInitializer;

public class POS extends JFrame {
    
    public void init() {
        // ... código existente ...
        
        // AGREGAR ESTAS LÍNEAS:
        if (InvoiceModuleInitializer.initializeModule()) {
            System.out.println("✓ Módulo de facturación cargado");
            addInvoiceMenu();
        }
    }
    
    private void addInvoiceMenu() {
        JMenu menuFacturacion = new JMenu("Facturación Electrónica");
        
        JMenuItem itemCrear = new JMenuItem("Nueva Factura");
        itemCrear.addActionListener(e -> {
            JPanel panel = InvoiceModuleInitializer.getCreateInvoicePanel();
            // Mostrar panel en ventana
        });
        
        JMenuItem itemListar = new JMenuItem("Mis Facturas");
        itemListar.addActionListener(e -> {
            JPanel panel = InvoiceModuleInitializer.getInvoiceListPanel();
            // Mostrar panel en ventana
        });
        
        menuFacturacion.add(itemCrear);
        menuFacturacion.add(itemListar);
        
        menuBar.add(menuFacturacion);
    }
}
```

#### Paso 5: Compilar y Ejecutar

```powershell
# Compilar módulo
cd C:\xampp\htdocs\chromispos\ChromisPOS
.\build_clean.bat

# Iniciar ChromisPOS
java -jar chromispos.jar
```

#### Paso 6: Probar en Modo Test

1. **Nueva factura:** Menú → Facturación Electrónica → Nueva Factura
2. **Completar datos** del cliente
3. **Generar XML**
4. **Enviar a SRI** (test)
5. **Verificar autorización**

✅ **¡LISTO PARA PRODUCCIÓN!**

---

### **OPCIÓN 2: Deployment en Servidor Separado (Mediano - 30 minutos)**

Usar un servidor Windows separado para mayor seguridad.

#### Paso 1: Preparar Servidor

```powershell
# En servidor destino:

# 1. Instalar Java 8 JDK
# Descargar de: https://adoptium.net/
# Instalar en: C:\Program Files\Java\jdk8u402-b06

# 2. Instalar MySQL Server
# https://dev.mysql.com/downloads/mysql/
# Versión 5.7 o superior

# 3. Crear carpeta para aplicación
mkdir C:\apps\chromispos
cd C:\apps\chromispos
```

#### Paso 2: Copiar Archivos

```powershell
# Desde máquina desarrollo:
# Copiar archivos a servidor

# Carpetas necesarias:
xcopy C:\xampp\htdocs\chromispos\ChromisPOS\uk C:\apps\chromispos\uk /S /I /Y
xcopy C:\xampp\htdocs\chromispos\ChromisPOS\build C:\apps\chromispos\build /S /I /Y
xcopy C:\xampp\htdocs\chromispos\ChromisPOS\chromispos.jar C:\apps\chromispos\ /Y

# Archivos de configuración:
copy C:\xampp\htdocs\chromispos\ChromisPOS\chromispos-invoice.properties C:\apps\chromispos\
copy C:\xampp\htdocs\chromispos\ChromisPOS\setup_database.sql C:\apps\chromispos\
```

#### Paso 3: Configurar Base de Datos en Servidor

```powershell
# En servidor:
mysql -u root -p < C:\apps\chromispos\setup_database.sql

# Verificar:
mysql -u root -e "USE chromisdb; SHOW TABLES;"
```

#### Paso 4: Editar Configuración para Servidor

Editar: `C:\apps\chromispos\chromispos-invoice.properties`

```properties
# Apuntar a servidor MySQL
invoice.database.host=192.168.1.100      # IP del servidor MySQL
invoice.database.user=usuario_chromis
invoice.database.password=contraseña_segura

# Resto igual que Option 1
invoice.issuer.ruc=1712345678901
invoice.issuer.name=MI EMPRESA S.A.
invoice.certificate.path=C:\certs\empresa.pfx
invoice.environment=test
```

#### Paso 5: Crear Servicio Windows

```powershell
# Crear archivo: C:\apps\chromispos\start.bat
@echo off
cd C:\apps\chromispos
java -Xmx1024m -jar chromispos.jar
```

Luego crear servicio con NSSM:
```powershell
# Descargar NSSM de: https://nssm.cc/
nssm install ChromisPOS "C:\apps\chromispos\start.bat"
nssm start ChromisPOS
```

#### Paso 6: Crear Carpetas Necesarias

```powershell
mkdir C:\apps\chromispos\facturas
mkdir C:\apps\chromispos\logs\invoice
mkdir C:\certs
```

✅ **Deployment en servidor listo**

---

### **OPCIÓN 3: Deployment en Nube/Docker (Avanzado - 60 minutos)**

Para entornos corporativos con múltiples usuarios.

#### Paso 1: Crear Dockerfile

```dockerfile
FROM openjdk:8-jdk-alpine

# Instalar MySQL client
RUN apk add --no-cache mysql-client

# Crear usuario
RUN adduser -D chromis

# Directorio de trabajo
WORKDIR /app

# Copiar archivos
COPY build/classes ./build/classes
COPY chromispos.jar ./
COPY chromispos-invoice.properties ./
COPY facturas ./facturas
COPY logs ./logs

# Usuario
USER chromis

# Puerto
EXPOSE 8080

# Comando
CMD ["java", "-Xmx1024m", "-jar", "chromispos.jar"]
```

#### Paso 2: Construir imagen Docker

```bash
docker build -t chromispos:1.0 .
```

#### Paso 3: Docker Compose (Multi-container)

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:5.7
    environment:
      MYSQL_DATABASE: chromisdb
      MYSQL_ROOT_PASSWORD: root_password
    volumes:
      - mysql_data:/var/lib/mysql
      - ./setup_database.sql:/docker-entrypoint-initdb.d/setup.sql
    ports:
      - "3306:3306"

  chromispos:
    build: .
    depends_on:
      - mysql
    environment:
      DATABASE_HOST: mysql
      DATABASE_NAME: chromisdb
      DATABASE_USER: root
    ports:
      - "8080:8080"
    volumes:
      - ./facturas:/app/facturas
      - ./logs:/app/logs
      - ./certs:/app/certs

volumes:
  mysql_data:
```

#### Paso 4: Ejecutar

```bash
docker-compose up -d
```

✅ **Deployment en Docker listo**

---

## 📋 CHECKLIST DE DEPLOYMENT

### Antes de Desplegar

- [ ] ¿Tienes el RUC de tu empresa? (13 dígitos)
- [ ] ¿Tienes el certificado digital del SRI? (.pfx o .p12)
- [ ] ¿Está operativa la base de datos MySQL?
- [ ] ¿Está instalado Java 8 JDK?
- [ ] ¿Compilaste el módulo sin errores?
- [ ] ¿Probaste el ejemplo (InvoiceExample)?

### Durante el Deployment

- [ ] Configuración correcta en chromispos-invoice.properties
- [ ] Ruta del certificado verificada
- [ ] Base de datos chromisdb creada
- [ ] Carpetas facturas/ y logs/ creadas
- [ ] Permisos de archivo/carpeta correctos
- [ ] Puertos disponibles (no bloqueados por firewall)

### Después de Desplegar

- [ ] Módulo inicia sin errores
- [ ] Menú de facturación visible en ChromisPOS
- [ ] Puedes crear una factura de prueba
- [ ] Se genera XML correctamente
- [ ] Puedes enviar a SRI (test)
- [ ] Se recibe autorización del SRI
- [ ] Logs muestran las transacciones

---

## 🔒 SEGURIDAD EN PRODUCCIÓN

### Cambios Importantes Antes de Ir a Producción

#### 1. Cambiar Contraseñas

```properties
# chromispos-invoice.properties
invoice.database.password=contraseña_super_segura_123!
invoice.certificate.password=contraseña_certificado_456!
```

#### 2. Habilitar HTTPS

```properties
invoice.sri.url=https://cel.sri.gob.ec/...
```

#### 3. Cambiar Ambiente a Producción

```properties
# IMPORTANTE: Cambiar a 'production' solo después de verificar
invoice.environment=production
```

#### 4. Configurar Firewall

```powershell
# Permitir solo puertos necesarios
netsh advfirewall firewall add rule name="ChromisPOS" dir=in action=allow protocol=tcp localport=8080

# Bloquear puerto MySQL (no debe ser público)
netsh advfirewall firewall add rule name="MySQL Block" dir=in action=block protocol=tcp localport=3306
```

#### 5. Backups Automáticos

```batch
REM backup_db.bat
@echo off
set timestamp=%date:~10,4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
C:\xampp\mysql\bin\mysqldump -u root chromisdb > C:\backups\chromisdb_%timestamp%.sql
```

Programar en Task Scheduler para ejecutar cada 24 horas.

---

## 🐛 TROUBLESHOOTING DE DEPLOYMENT

### "Módulo no carga"

```powershell
# Ver logs
type logs\invoice\module.log

# Verificar compilación
javac -version

# Recompilar
.\build_clean.bat
```

### "No puedo conectar a base de datos"

```powershell
# Verificar MySQL está corriendo
mysql -u root -e "SELECT 1;"

# Verificar chromisdb existe
mysql -u root -e "SHOW DATABASES LIKE 'chromisdb';"

# Verificar IP/host en config
type chromispos-invoice.properties | findstr database
```

### "Certificado no es válido"

```powershell
# Verificar certificado
openssl pkcs12 -info -in C:\certs\empresa.pfx

# Si es PEM, convertir a PFX
openssl pkcs12 -export -in cert.pem -inkey key.pem -out empresa.pfx
```

### "SRI rechaza la factura"

1. ✓ Verificar ambiente está en "test"
2. ✓ Verificar RUC en formato correcto (13 dígitos)
3. ✓ Ver logs de SRI: `logs/invoice/sri.log`
4. ✓ Hacer test con ejemplo: `InvoiceExample.java`

---

## 📊 MONITOREO POST-DEPLOYMENT

### Ver Estado del Módulo

```java
// Crear endpoint de salud
String status = InvoiceModuleInitializer.getModuleStatus();
System.out.println(status);
```

Debe mostrar:
```
=== Estado del Módulo de Facturación ===
initialized: ✓
certificateLoaded: ✓
databaseConnected: ✓
sriConnected: ✓
```

### Revisar Logs Regularmente

```powershell
# Ver últimas 100 líneas
Get-Content logs\invoice\module.log -Tail 100

# Ver errores
Select-String "ERROR" logs\invoice\module.log
```

### Monitorear Base de Datos

```powershell
# Contar facturas diarias
mysql -u root chromisdb -e "SELECT COUNT(*) FROM electronic_invoices WHERE DATE(issue_date) = CURDATE();"

# Facturas con estado DRAFT
mysql -u root chromisdb -e "SELECT COUNT(*) FROM electronic_invoices WHERE status = 'DRAFT';"

# Facturas enviadas al SRI
mysql -u root chromisdb -e "SELECT COUNT(*) FROM electronic_invoices WHERE sent_to_sri = TRUE;"
```

---

## 🚀 RESUMEN RÁPIDO (TL;DR)

```
OPCIÓN 1 - LOCAL (Más fácil, recomendado empezar):
1. Editar chromispos-invoice.properties
2. Agregar RUC, nombre empresa, ruta certificado
3. Integrar InvoiceModuleInitializer en ChromisPOS
4. Compilar y ejecutar
5. Probar en modo test

OPCIÓN 2 - SERVIDOR (Escalable):
1. Instalar Java + MySQL en servidor
2. Copiar archivos
3. Crear base de datos
4. Editar configuración con IPs correctas
5. Crear servicio Windows
6. Ejecutar

OPCIÓN 3 - DOCKER (Profesional):
1. Crear Dockerfile
2. Crear docker-compose.yml
3. docker-compose up -d
4. Acceder al contenedor
```

---

## 📞 PASOS INMEDIATOS (HOY)

```
1. Obtener certificado del SRI
   ↓
2. Guardar en C:\certs\
   ↓
3. Editar chromispos-invoice.properties
   ↓
4. Integrar en ChromisPOS
   ↓
5. Probar en ambiente test
   ↓
6. Cambiar a production cuando esté OK
```

---

## ✅ CHECKLIST FINAL

- [ ] Certificado digital descargado del SRI
- [ ] chromispos-invoice.properties completado
- [ ] Módulo compilado sin errores
- [ ] Base de datos operativa
- [ ] Integración en ChromisPOS completada
- [ ] Prueba en ambiente test exitosa
- [ ] Logs limpios (sin errores)
- [ ] Backups configurados
- [ ] Documentación actualizada

**¡LISTO PARA PRODUCCIÓN! 🎉**

---

*Guía de Deployment - Módulo Facturación Electrónica*  
*Versión 1.0 - 3 de Enero, 2026*
