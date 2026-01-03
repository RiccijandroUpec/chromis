# 🚀 DEPLOYMENT - GUÍA RÁPIDA

## ⚡ EN 5 MINUTOS

### Paso 1: Ejecutar script de deployment

```powershell
cd C:\xampp\htdocs\chromispos\ChromisPOS
.\deploy.bat
```

✅ Verifica requisitos  
✅ Configura base de datos  
✅ Crea carpetas necesarias  

### Paso 2: Editar configuración

```powershell
# Abrir y editar:
chromispos-invoice.properties
```

Completa estos campos:
```properties
invoice.issuer.ruc=1712345678901          # Tu RUC (13 dígitos)
invoice.issuer.name=MI EMPRESA S.A.        # Nombre de empresa
invoice.certificate.path=C:\certs\empresa.pfx  # Certificado SRI
invoice.certificate.password=contraseña    # Password certificado
invoice.environment=test                   # test o production
```

### Paso 3: Integrar en ChromisPOS

En tu archivo `POS.java` o equivalente, agregar:

```java
// Al inicio
import uk.chromis.pos.invoice.integration.ChromisPOSInvoiceIntegration;

// En método de inicialización (constructor o init)
public void init() {
    // ... código existente ...
    
    // Integrar módulo de facturación
    ChromisPOSInvoiceIntegration.integrate(this);
}
```

### Paso 4: Compilar ChromisPOS

```powershell
# Compilar todo (incluye módulo de facturación)
javac -d build -cp chromispos.jar src/com/.../*.java
```

### Paso 5: Ejecutar

```powershell
java -jar chromispos.jar
```

✅ ¡LISTO! 

---

## 🎯 VER MENÚ DE FACTURACIÓN

En ChromisPOS:  
**Menú → Facturación Electrónica**

Opciones disponibles:
- Nueva Factura
- Mis Facturas  
- Configuración
- Estado del Módulo

---

## 🧪 PRUEBA EN AMBIENTE TEST

1. **Nueva Factura**: Crear una factura de prueba
2. **Generar XML**: El sistema genera automáticamente
3. **Enviar a SRI**: Prueba con certificado test
4. **Verificar**: Revisar logs en `logs/invoice/`

```powershell
# Ver logs
type logs\invoice\module.log
```

---

## 🔄 CAMBIAR A PRODUCCIÓN

**SOLO después de verificar que TODO funciona en test:**

1. Obtener certificado digital REAL del SRI
2. Editar `chromispos-invoice.properties`:
   ```properties
   invoice.environment=production
   invoice.certificate.path=C:\certs\certificado-real.pfx
   ```
3. Reiniciar ChromisPOS
4. ¡Listo!

---

## 📊 VERIFICAR DEPLOYMENT

```powershell
# Ver estado del módulo
# En ChromisPOS: Menú → Facturación → Estado del Módulo

# O revisar logs:
type logs\invoice\module.log | Select-Object -Last 20

# O contar facturas en BD:
mysql -u root chromisdb -e "SELECT COUNT(*) FROM electronic_invoices;"
```

---

## 🆘 PROBLEMAS COMUNES

| Problema | Solución |
|----------|----------|
| "Módulo no carga" | Revisar logs en `logs/invoice/` |
| "Conexión BD falla" | Verificar MySQL está corriendo |
| "Certificado inválido" | Verificar ruta en properties |
| "SRI rechaza factura" | Verificar ambiente es "test" |

---

## 📞 PRÓXIMOS PASOS

1. ✓ Ejecutar `.\deploy.bat`
2. ✓ Editar `chromispos-invoice.properties`
3. ✓ Integrar en ChromisPOS
4. ✓ Compilar y ejecutar
5. ✓ Probar en ambiente test
6. ✓ Cambiar a production

**¡HECHO! 🎉**

---

Para más detalles, ver: `DEPLOYMENT_GUIDE.md`
