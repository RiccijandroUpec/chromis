#!/bin/bash
# Script para enviar factura de prueba al SRI

echo "═══════════════════════════════════════════════════════════════════"
echo "  Script de Prueba - Facturación Electrónica Ecuador"
echo "═══════════════════════════════════════════════════════════════════"
echo ""

# Variables configurables
RUC_EMISOR="${1:-1234567890001}"
AMBIENTE="${2:-test}"
CERTIFICADO="${3:-./certificado.pfx}"
CONTRASEÑA="${4:-password}"

echo "Configuración:"
echo "  RUC Emisor: $RUC_EMISOR"
echo "  Ambiente: $AMBIENTE"
echo "  Certificado: $CERTIFICADO"
echo ""

# Validaciones
if [ ! -f "$CERTIFICADO" ]; then
    echo "❌ Error: Certificado no encontrado: $CERTIFICADO"
    exit 1
fi

if [ "$AMBIENTE" != "test" ] && [ "$AMBIENTE" != "production" ]; then
    echo "❌ Error: Ambiente debe ser 'test' o 'production'"
    exit 1
fi

# Validar RUC
if ! [[ "$RUC_EMISOR" =~ ^[0-9]{13}$ ]]; then
    echo "❌ Error: RUC debe tener 13 dígitos"
    exit 1
fi

echo "✓ Validaciones OK"
echo ""

# Crear clase de prueba temporal
cat > TestSendInvoice.java <<'EOF'
import uk.chromis.pos.invoice.models.*;
import uk.chromis.pos.invoice.services.*;
import uk.chromis.pos.invoice.utils.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class TestSendInvoice {
    public static void main(String[] args) throws Exception {
        String ruc = args[0];
        String ambiente = args[1];
        String certificado = args[2];
        String contraseña = args[3];
        
        System.out.println("Iniciando prueba de envío...");
        System.out.println("");
        
        // Crear servicio
        System.out.println("[1/5] Inicializando servicio...");
        ElectronicInvoiceService service = new ElectronicInvoiceService();
        service.initialize(certificado, contraseña, ambiente.equals("production"));
        System.out.println("✓ Servicio inicializado");
        
        // Crear factura de prueba
        System.out.println("[2/5] Creando factura de prueba...");
        ElectronicInvoice invoice = createTestInvoice(ruc);
        System.out.println("✓ Factura creada: " + invoice.getInvoiceNumber());
        
        // Generar XML
        System.out.println("[3/5] Generando XML...");
        service.generateInvoiceXML(invoice);
        String accessKey = invoice.getAccessKey();
        System.out.println("✓ XML generado");
        System.out.println("  Clave de acceso: " + accessKey);
        
        // Firmar
        System.out.println("[4/5] Firmando XML...");
        service.signInvoice(invoice);
        System.out.println("✓ XML firmado");
        
        // Enviar a SRI
        System.out.println("[5/5] Enviando a SRI...");
        service.sendToSRI(invoice);
        System.out.println("✓ Enviado a SRI");
        System.out.println("");
        
        // Mostrar resultado
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("RESULTADO:");
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("Estado: " + invoice.getStatus().getDisplayName());
        System.out.println("Clave de acceso: " + invoice.getAccessKey());
        System.out.println("Número factura: " + invoice.getInvoiceNumber());
        System.out.println("Fecha: " + invoice.getIssueDate());
        System.out.println("Respuesta SRI: " + (invoice.getSriResponse() != null ? "Recibida" : "No"));
        
        if (invoice.getAuthorizationNumber() != null) {
            System.out.println("Número autorización: " + invoice.getAuthorizationNumber());
        }
        
        System.out.println("═══════════════════════════════════════════════════");
    }
    
    private static ElectronicInvoice createTestInvoice(String ruc) {
        ElectronicInvoice invoice = new ElectronicInvoice();
        
        // Emisor
        InvoiceIssuer issuer = new InvoiceIssuer();
        issuer.setRuc(ruc);
        issuer.setBusinessName("EMPRESA DE PRUEBA SA");
        issuer.setTradeName("NEGOCIO TEST");
        issuer.setAddress("Calle Prueba 123");
        issuer.setCity("Quito");
        issuer.setProvince("Pichincha");
        issuer.setCountry("Ecuador");
        issuer.setEmail("prueba@test.com");
        issuer.setPhone("0298887766");
        invoice.setIssuer(issuer);
        
        // Comprador
        InvoiceBuyer buyer = new InvoiceBuyer();
        buyer.setIdentificationType("05"); // RUC
        buyer.setIdentification("9999999999999");
        buyer.setName("CLIENTE TEST");
        buyer.setEmail("cliente@test.com");
        buyer.setAddress("Av. Test 456");
        buyer.setCity("Quito");
        invoice.setBuyer(buyer);
        
        // Detalles
        InvoiceDetail detail = new InvoiceDetail();
        detail.setCode("001");
        detail.setDescription("PRODUCTO DE PRUEBA");
        detail.setQuantity(new BigDecimal("1"));
        detail.setUnitPrice(new BigDecimal("100.00"));
        detail.setTaxCode("2"); // IVA
        detail.setTaxRate(new BigDecimal("12"));
        invoice.addDetail(detail);
        
        // Método de pago
        PaymentMethod payment = new PaymentMethod();
        payment.setCode("01"); // Efectivo
        payment.setAmount(new BigDecimal("112.00"));
        payment.setDescription("Pago efectivo");
        invoice.addPaymentMethod(payment);
        
        // Datos básicos
        invoice.setInvoiceNumber("001001000001");
        invoice.setIssueDate(LocalDate.now());
        
        return invoice;
    }
}
EOF

echo "Compilando prueba..."
javac -cp build/classes TestSendInvoice.java 2>&1 | head -10

if [ -f "TestSendInvoice.class" ]; then
    echo "✓ Compilado"
    echo ""
    echo "Ejecutando prueba..."
    echo ""
    java -cp .:build/classes TestSendInvoice "$RUC_EMISOR" "$AMBIENTE" "$CERTIFICADO" "$CONTRASEÑA"
    RESULTADO=$?
    
    # Limpiar
    rm -f TestSendInvoice.java TestSendInvoice.class
    
    if [ $RESULTADO -eq 0 ]; then
        echo ""
        echo "✓ Prueba completada exitosamente"
    else
        echo ""
        echo "❌ Error durante prueba"
        exit 1
    fi
else
    echo "❌ Error compilando prueba"
    exit 1
fi
