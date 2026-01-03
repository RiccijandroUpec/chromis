#!/bin/bash
# Script de instalación del módulo de Facturación Electrónica para ChromisPOS

echo "================================"
echo "Instalación Facturación Electrónica"
echo "Ecuador - ChromisPOS"
echo "================================"
echo ""

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir con color
print_status() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}!${NC} $1"
}

echo "Paso 1: Verificando estructura de carpetas..."
if [ -d "src-pos/uk/chromis/pos/invoice" ]; then
    print_status "Módulo de facturación encontrado"
else
    print_error "No se encontró el módulo de facturación"
    exit 1
fi

echo ""
echo "Paso 2: Compilando clases Java..."
print_warning "Requiere compilar con: javac -d bin src-pos/uk/chromis/pos/invoice/**/*.java"

echo ""
echo "Paso 3: Creando tablas en base de datos..."
echo "Ejecute en MySQL:"
echo "  mysql -u usuario -p base_datos < src-pos/uk/chromis/pos/invoice/database/create_tables.sql"
read -p "¿Continuar? (s/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Ss]$ ]]; then
    print_status "Base de datos preparada"
fi

echo ""
echo "Paso 4: Configurando archivos..."

# Verificar chromisposconfig.properties
if grep -q "invoice.enabled" chromisposconfig.properties; then
    print_status "chromisposconfig.properties ya configurado"
else
    print_warning "Agregue configuración de facturación a chromisposconfig.properties"
fi

# Verificar invoice.properties
if [ -f "invoice.properties" ]; then
    print_status "invoice.properties encontrado"
else
    print_warning "Cree archivo invoice.properties en raíz del proyecto"
    cp src-pos/uk/chromis/pos/invoice/invoice.properties . 2>/dev/null
fi

echo ""
echo "Paso 5: Verificando librerías..."
print_warning "Librerías requeridas (ya incluidas en Java 8+):"
echo "  - java.net (HTTP)"
echo "  - java.security (Criptografía)"
echo "  - java.xml (XML)"
echo ""
print_warning "Librerías opcionales (agregar a /lib si es necesario):"
echo "  - bcprov-jdk15on-1.70.jar (firma digital avanzada)"
echo "  - pdfbox-2.0.28.jar (generación PDF)"
echo "  - httpclient-4.5.13.jar (HTTP avanzado)"

echo ""
echo "Paso 6: Configuración del Certificado Digital"
echo "1. Obtener certificado válido del SRI/autoridad competente"
echo "2. Guardar en ruta conocida (ej: C:/certificados/emisor.pfx)"
echo "3. Actualizar invoice.properties con ruta y contraseña"

echo ""
echo "================================"
print_status "Instalación completada!"
echo "================================"
echo ""
echo "Próximos pasos:"
echo "1. Revisar documentación:"
echo "   - src-pos/uk/chromis/pos/invoice/README.md"
echo "   - src-pos/uk/chromis/pos/invoice/INTEGRATION_GUIDE.md"
echo ""
echo "2. Ejecutar ejemplo:"
echo "   java uk.chromis.pos.invoice.example.InvoiceExample"
echo ""
echo "3. Integrar en ChromisPOS:"
echo "   - Agregar opción en menú principal"
echo "   - Llamar desde módulo de ventas"
echo ""
echo "4. Configurar ambiente SRI:"
echo "   - Test: celcert.sri.gob.ec"
echo "   - Producción: celcer.sri.gob.ec"
echo ""
echo "¡Listo para facturación electrónica!"
