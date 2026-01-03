# 🔐 GUÍA DE CONFIGURACIÓN DE CERTIFICADO DIGITAL

## ✨ Nueva Vista de Selección de Certificado

El módulo de facturación electrónica ahora incluye una **interfaz gráfica completa** para seleccionar, validar y guardar certificados digitales directamente desde ChromisPOS.

---

## 📋 Cómo usar la nueva vista de certificado

### 1. **Abrir la pestaña de Certificado Digital**

En ChromisPOS:
```
Menú → Facturación Electrónica → Configuración
```

Haz clic en la pestaña: **"Certificado Digital"**

---

### 2. **Interfaz de Certificado**

La pestaña incluye:

```
┌────────────────────────────────────────────────────────────┐
│  Ruta del Certificado:  [____________________]  [Examinar]  │
│                                                             │
│  Contraseña:           [████████████]  [Validar]           │
│                                                             │
│  Estado:  ✓ Certificado válido y guardado                 │
│                                                             │
│  Certificado validado: empresa.pfx | Tamaño: 5.2 KB      │
│                                                             │
├────────────────────────────────────────────────────────────┤
│  Instrucciones para obtener certificado:                   │
│  1. Visita: https://www.sri.gob.ec                        │
│  2. Solicita un certificado digital (Personas Jurídicas)  │
│  3. Descarga el archivo .pfx o .p12                       │
│  4. Haz clic en 'Examinar' y selecciona el archivo        │
│  5. Ingresa la contraseña del certificado                 │
│  6. Haz clic en 'Validar' para verificar                  │
└────────────────────────────────────────────────────────────┘
```

---

### 3. **Paso a Paso para Configurar**

#### **Paso 1: Obtener Certificado del SRI**

**Sitio Web:** https://www.sri.gob.ec/

**Proceso:**
1. Ingresa a la página del SRI
2. Busca "Certificado Digital"
3. Selecciona "Personas Jurídicas" (empresas)
4. Completa el formulario con datos de tu empresa
5. Realiza el pago (si aplica)
6. Descarga el certificado en formato `.pfx` o `.p12`
7. Anota la **contraseña** del certificado

**Archivo descargado:** Será algo como `mi_empresa.pfx` o `empresa.p12`

---

#### **Paso 2: Seleccionar el Certificado**

**En la pestaña "Certificado Digital":**

1. Haz clic en botón **"Examinar"**
   - Se abre un explorador de archivos
   - Busca tu archivo `.pfx` o `.p12`
   - Haz clic en "Abrir"

2. El campo mostrará:
   - Ruta completa del archivo
   - Nombre del archivo
   - Tamaño del archivo

**Estado:** Aparecerá como "Seleccionado (no validado)" en color naranja

---

#### **Paso 3: Ingresar Contraseña**

1. En el campo **"Contraseña"** ingresa la contraseña del certificado
2. Es la contraseña que anotaste cuando descargaste el certificado

**Ejemplo:**
```
Contraseña: MiContraseña123!
```

---

#### **Paso 4: Validar el Certificado**

1. Haz clic en el botón **"Validar"**
2. El sistema:
   - Verifica que el archivo existe
   - Valida la extensión (.pfx o .p12)
   - Comprueba el tamaño
   - Intenta leer el certificado
   - **Guarda automáticamente** la ruta y contraseña

**Si todo es correcto:**
- Estado: ✓ Certificado válido y guardado
- Etiqueta verde
- Mensaje de éxito
- Se muestra información del certificado

**Si hay error:**
- Estado: Error con descripción
- Etiqueta roja
- Debes revisar y corregir

---

## 🔍 Validaciones Automáticas

El sistema valida automáticamente:

| Validación | Error si... |
|-----------|-------------|
| **Archivo existe** | No encuentras el archivo en esa ruta |
| **Extensión** | No es `.pfx` o `.p12` |
| **Tamaño** | Es mayor a 1 MB |
| **Legible** | El archivo está corrupto |
| **Contraseña** | No ingresaste contraseña |

---

## 💾 Guardado Automático

Cuando validas exitosamente el certificado:

1. **Archivo:** `chromispos-invoice.properties`
2. **Se guardan:**
   - `invoice.certificate.path` = Ruta completa
   - `invoice.certificate.password` = Contraseña (encriptada en producción)

3. **Ubicación:** Raíz del proyecto

**Ejemplo de archivo:**
```properties
invoice.issuer.ruc=1712345678901
invoice.issuer.name=MI EMPRESA S.A.
invoice.certificate.path=C:\Users\Usuario\Downloads\mi_empresa.pfx
invoice.certificate.password=MiContraseña123!
invoice.environment=test
```

---

## 🎯 Estados del Certificado

### 1. **No cargado** 🔴
- Rojo
- Significado: No hay certificado seleccionado
- Acción: Haz clic en "Examinar"

### 2. **Seleccionado (no validado)** 🟠
- Naranja
- Significado: Archivo elegido pero sin validar
- Acción: Ingresa contraseña y haz clic en "Validar"

### 3. **Certificado válido y guardado** 🟢
- Verde
- Significado: Certificado listo para usar
- Acción: Puedes usarlo para generar facturas

### 4. **Error: [Descripción]** 🔴
- Rojo con mensaje
- Significado: Hay un problema con el certificado
- Acción: Lee el error y corrige

---

## 🧪 Pruebas

### **Test Local (sin enviar a SRI)**

1. Selecciona certificado TEST
2. En pestaña "Ambiente" → Selecciona **"Pruebas"**
3. Valida el certificado
4. Genera una factura de prueba
5. El sistema genera XML pero **NO envía a SRI**

### **Test con SRI (Servidor Test)**

1. Selecciona certificado TEST
2. Ambiente: **"Pruebas"** ✓
3. Haz clic en "Guardar Configuración"
4. Genera una factura
5. El sistema **SÍ envía a SRI test**
6. Recibes autorizaciones TEST (números no contabilizan)

### **Producción (Servidor Real)**

⚠️ **SOLO después de probar exitosamente con SRI test**

1. Obtén certificado **REAL** (distinto del test)
2. Selecciona el certificado real
3. Ambiente: **"Producción"** ⚠️
4. Valida el certificado
5. Genera facturas reales
6. **Los números son definitivos e irrevocables**

---

## ⚠️ Advertencias Importantes

### **Seguridad del Certificado**

- El archivo `.pfx` contiene tu identidad digital
- **NUNCA** compartas el archivo o la contraseña
- Guárdalo en lugar seguro
- Haz copias de respaldo
- No lo publiques en repositorios

### **Ambiente de Producción**

- Una vez generes facturas en PRODUCCIÓN, **NO PUEDES REVERTIR**
- Los números son secuenciales e irrevocables
- Verifica todo en TEST antes de pasar a PRODUCCIÓN
- Consulta con un contador o SRI si dudas

### **Renovación de Certificado**

- Los certificados expiran
- Antes de la expiración, obtén uno nuevo
- Actualiza en la interfaz gráfica
- El sistema registra el cambio automáticamente

---

## 🔧 Troubleshooting

### **Problema: "Archivo no encontrado"**

**Solución:**
- Verifica que el archivo existe
- Comprueba la ruta completa
- Asegúrate de tener permisos de lectura
- Intenta copiar el archivo a una carpeta más simple (sin caracteres especiales)

### **Problema: "Formato inválido"**

**Solución:**
- Debe ser `.pfx` o `.p12`
- Descarga nuevamente desde SRI
- Verifica que descargó el archivo correcto

### **Problema: "Contraseña incorrecta"**

**Solución:**
- Revisa que escribiste correctamente
- Recuerda que es sensible a mayúsculas
- Solicita nueva contraseña al SRI si olvidaste
- Intenta sin espacios al inicio/final

### **Problema: "Error al validar"**

**Solución:**
- Cierra cualquier programa que esté usando el certificado
- Intenta mover el archivo a otra ubicación
- Reinicia ChromisPOS
- Solicita nuevo certificado al SRI si persiste

---

## 📞 Soporte SRI

**Certificado SRI:**
- Sitio: https://www.sri.gob.ec/
- Teléfono: 1800 SRI (774)
- Email: [consultar en sitio]

**Certificado Test (desarrollo):**
- Disponible en sitio de SRI
- Menos requisitos
- Sirve para pruebas
- No requiere pago

---

## ✅ Checklist de Configuración

- [ ] Obtuve certificado del SRI (test o real)
- [ ] Descargué el archivo `.pfx` o `.p12`
- [ ] Anote la contraseña del certificado
- [ ] Abrí pestaña "Certificado Digital" en ChromisPOS
- [ ] Hice clic en "Examinar" y seleccioné el archivo
- [ ] Ingresé la contraseña
- [ ] Hice clic en "Validar"
- [ ] El estado dice "✓ Certificado válido y guardado"
- [ ] La contraseña se guardó automáticamente
- [ ] Ahora puedo generar facturas electrónicas

---

## 🎉 ¡Listo!

Tu certificado está configurado correctamente. Ahora puedes:

✅ Generar facturas electrónicas
✅ Firmarlas digitalmente
✅ Enviarlas al SRI
✅ Guardarlas en base de datos

**Próximo paso:** Ir a "Nueva Factura" y crear tu primera factura electrónica.

---

**Fecha:** 3 de Enero de 2026
**Versión:** 1.0 - Vista de Certificado Integrada
**Estado:** ✅ PRODUCCIÓN LISTA
