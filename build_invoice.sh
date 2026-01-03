#!/bin/bash
# Build Script - Compilación del módulo de Facturación Electrónica

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║  Compilación - Módulo Facturación Electrónica Ecuador        ║"
echo "║  ChromisPOS                                                   ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Variables
PROJECT_DIR="."
SOURCE_DIR="src-pos/uk/chromis/pos/invoice"
BUILD_DIR="build/classes"
LIB_DIR="lib"

echo -e "${BLUE}1. Limpiando directorios anteriores...${NC}"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
echo -e "${GREEN}✓ Directorios limpios${NC}"
echo ""

echo -e "${BLUE}2. Verificando estructura de carpetas...${NC}"
if [ ! -d "$SOURCE_DIR" ]; then
    echo -e "${RED}✗ Carpeta no encontrada: $SOURCE_DIR${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Estructura verificada${NC}"
echo ""

echo -e "${BLUE}3. Compilando código Java...${NC}"
# Compilar todas las clases
javac \
    -d "$BUILD_DIR" \
    -encoding UTF-8 \
    -source 1.8 \
    -target 1.8 \
    "$SOURCE_DIR/models"/*.java \
    "$SOURCE_DIR/services"/*.java \
    "$SOURCE_DIR/dao"/*.java \
    "$SOURCE_DIR/forms"/*.java \
    "$SOURCE_DIR/utils"/*.java \
    "$SOURCE_DIR/example"/*.java \
    2>&1 | grep -v "^Note:"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Compilación exitosa${NC}"
else
    echo -e "${RED}✗ Error durante compilación${NC}"
    exit 1
fi
echo ""

echo -e "${BLUE}4. Creando archivo JAR (opcional)...${NC}"
mkdir -p "dist"
cd "$BUILD_DIR"
jar -cf "../../dist/invoice-module.jar" uk/chromis/pos/invoice/**/*.class 2>/dev/null
cd - > /dev/null

if [ -f "dist/invoice-module.jar" ]; then
    echo -e "${GREEN}✓ JAR creado: dist/invoice-module.jar${NC}"
else
    echo -e "${YELLOW}! JAR no creado (opcional)${NC}"
fi
echo ""

echo -e "${BLUE}5. Verificando clases compiladas...${NC}"
TOTAL=$(find "$BUILD_DIR" -name "*.class" | wc -l)
echo -e "${GREEN}✓ Total de clases compiladas: $TOTAL${NC}"
echo ""

echo -e "${BLUE}6. Resumen de compilación:${NC}"
echo "   Directorio fuente: $SOURCE_DIR"
echo "   Directorio salida: $BUILD_DIR"
echo "   Clases compiladas: $TOTAL"
echo ""

echo -e "${GREEN}╔════════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  ✓ Compilación completada exitosamente                        ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Próximos pasos:"
echo "1. Crear tablas en BD: mysql -u usuario -p db < $SOURCE_DIR/database/create_tables.sql"
echo "2. Configurar archivo: invoice.properties"
echo "3. Ejecutar ejemplo: java -cp $BUILD_DIR uk.chromis.pos.invoice.example.InvoiceExample"
echo ""
