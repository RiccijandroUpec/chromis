#!/bin/bash
# Script de Verificación Final - Facturación Electrónica Ecuador

echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║     VERIFICACIÓN FINAL - FACTURACIÓN ELECTRÓNICA ECUADOR     ║"
echo "║                      Módulo Completo                         ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Variables
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Contadores
TOTAL_FILES=0
JAVA_FILES=0
DOC_FILES=0
SCRIPT_FILES=0
SQL_FILES=0
ERRORS=0

echo -e "${BLUE}1. Verificando estructura de carpetas...${NC}"
echo ""

# Verificar carpetas principales
DIRS=("models" "services" "dao" "forms" "utils" "example" "database")

for dir in "${DIRS[@]}"; do
    if [ -d "src-pos/uk/chromis/pos/invoice/$dir" ]; then
        echo -e "${GREEN}✓${NC} Carpeta: $dir"
    else
        echo -e "${RED}✗${NC} Falta carpeta: $dir"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo -e "${BLUE}2. Contando archivos Java...${NC}"
echo ""

JAVA_FILES=$(find src-pos/uk/chromis/pos/invoice -name "*.java" -type f | wc -l)
echo -e "${GREEN}✓ Archivos Java: $JAVA_FILES${NC}"

if [ $JAVA_FILES -ne 21 ]; then
    echo -e "${YELLOW}! Esperaba 21 archivos, encontré $JAVA_FILES${NC}"
fi

echo ""
echo -e "${BLUE}3. Listando archivos Java...${NC}"
echo ""

find src-pos/uk/chromis/pos/invoice -name "*.java" -type f | sort | while read file; do
    LINES=$(wc -l < "$file")
    FILENAME=$(basename "$file")
    printf "  %-40s [%5d líneas]\n" "$FILENAME" "$LINES"
done

echo ""
echo -e "${BLUE}4. Verificando documentación...${NC}"
echo ""

DOCS=("README.md" "GETTING_STARTED.md" "INTEGRATION_GUIDE.md" "DEVELOPER_GUIDE.md" "QUICK_START.md" "TROUBLESHOOTING.md" "INTEGRATION_CHECKLIST.md" "VERSION.md" "MASTER_INVENTORY.md" "RESUMEN_COMPLETO.txt")

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        LINES=$(wc -l < "$doc")
        echo -e "${GREEN}✓${NC} $doc [$LINES líneas]"
        DOC_FILES=$((DOC_FILES + 1))
    else
        echo -e "${RED}✗${NC} Falta: $doc"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo -e "${BLUE}5. Verificando scripts...${NC}"
echo ""

SCRIPTS=("build_invoice.sh" "build_invoice.bat" "test_send_invoice.sh" "install_invoice.sh")

for script in "${SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        echo -e "${GREEN}✓${NC} $script"
        SCRIPT_FILES=$((SCRIPT_FILES + 1))
    else
        echo -e "${RED}✗${NC} Falta: $script"
        ERRORS=$((ERRORS + 1))
    fi
done

echo ""
echo -e "${BLUE}6. Verificando archivos SQL...${NC}"
echo ""

if [ -f "src-pos/uk/chromis/pos/invoice/database/create_tables.sql" ]; then
    LINES=$(wc -l < "src-pos/uk/chromis/pos/invoice/database/create_tables.sql")
    echo -e "${GREEN}✓${NC} create_tables.sql [$LINES líneas]"
    SQL_FILES=1
else
    echo -e "${RED}✗${NC} Falta: create_tables.sql"
    ERRORS=$((ERRORS + 1))
fi

echo ""
echo -e "${BLUE}7. Verificando configuración...${NC}"
echo ""

if [ -f "invoice.properties" ]; then
    echo -e "${GREEN}✓${NC} invoice.properties"
else
    echo -e "${YELLOW}! invoice.properties (puede existir o no)"
fi

echo ""
echo -e "${BLUE}8. Calculando estadísticas...${NC}"
echo ""

TOTAL_CODE_LINES=$(find src-pos/uk/chromis/pos/invoice -name "*.java" -type f -exec wc -l {} + | tail -1 | awk '{print $1}')
TOTAL_DOC_LINES=$(find . -maxdepth 1 -name "*.md" -type f -exec wc -l {} + | tail -1 | awk '{print $1}')

echo -e "  Archivos Java:        ${GREEN}$JAVA_FILES${NC}"
echo -e "  Líneas de código:     ${GREEN}$TOTAL_CODE_LINES${NC}"
echo -e "  Archivos doc:         ${GREEN}$DOC_FILES${NC}"
echo -e "  Líneas documentación: ${GREEN}$TOTAL_DOC_LINES${NC}"
echo -e "  Scripts ejecutables:  ${GREEN}$SCRIPT_FILES${NC}"
echo -e "  Archivos SQL:         ${GREEN}$SQL_FILES${NC}"

TOTAL_FILES=$((JAVA_FILES + DOC_FILES + SCRIPT_FILES + SQL_FILES))

echo ""
echo -e "${BLUE}9. Resumen de verificación...${NC}"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ TODAS LAS VERIFICACIONES EXITOSAS${NC}"
    echo ""
    echo -e "  ${GREEN}Total de archivos:${NC} $TOTAL_FILES"
    echo -e "  ${GREEN}Total de líneas:${NC} $((TOTAL_CODE_LINES + TOTAL_DOC_LINES))"
    echo -e "  ${GREEN}Estado:${NC} ${GREEN}100% COMPLETO${NC}"
else
    echo -e "${RED}✗ Se encontraron $ERRORS errores${NC}"
fi

echo ""
echo "╔═══════════════════════════════════════════════════════════════╗"
echo "║                    VERIFICACIÓN COMPLETADA                   ║"
echo "╚═══════════════════════════════════════════════════════════════╝"
echo ""

# Códigos de salida
if [ $ERRORS -eq 0 ]; then
    exit 0
else
    exit 1
fi
