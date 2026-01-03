package uk.chromis.pos.invoice.example;

import uk.chromis.pos.invoice.models.*;
import uk.chromis.pos.invoice.services.ElectronicInvoiceService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ejemplo de uso del módulo de facturación electrónica
 */
public class InvoiceExample {
    
    public static void main(String[] args) {
        try {
            // 1. Crear el servicio de facturación
            ElectronicInvoiceService invoiceService = new ElectronicInvoiceService();
            
            // 2. Inicializar con certificado (opcional)
            // invoiceService.initialize("ruta/certificado.pfx", "contraseña", false);
            
            // 3. Crear factura
            ElectronicInvoice invoice = new ElectronicInvoice();
            
            // 4. Configurar información del emisor
            InvoiceIssuer issuer = new InvoiceIssuer();
            issuer.setRuc("1234567890001");
            issuer.setBusinessName("MI EMPRESA S.A.");
            issuer.setTradeName("MI NEGOCIO");
            issuer.setAddress("Calle Principal 123");
            issuer.setCity("Quito");
            issuer.setProvince("Pichincha");
            issuer.setCountry("Ecuador");
            issuer.setEmail("info@miempresa.com");
            issuer.setPhone("0212345678");
            
            invoice.setIssuer(issuer);
            
            // 5. Configurar información del comprador
            InvoiceBuyer buyer = new InvoiceBuyer();
            buyer.setIdentification("1708123456");
            buyer.setIdentificationType("C"); // C = Cédula
            buyer.setBusinessName("Juan Pérez");
            buyer.setEmail("juan@cliente.com");
            buyer.setAddress("Calle Cliente 456");
            
            invoice.setBuyer(buyer);
            
            // 6. Establecer fecha y número
            invoice.setInvoiceNumber("000000001");
            invoice.setIssueDate(LocalDateTime.now());
            invoice.setIssueTime(LocalDateTime.now());
            
            // 7. Agregar detalles de productos
            InvoiceDetail detail1 = new InvoiceDetail();
            detail1.setCode("001");
            detail1.setDescription("Producto A");
            detail1.setQuantity(new BigDecimal("2"));
            detail1.setUnitPrice(new BigDecimal("50.00"));
            detail1.setTaxCode("2");
            detail1.setTaxRate(new BigDecimal("12"));
            detail1.setLineTotal(new BigDecimal("100.00"));
            
            invoice.getDetails().add(detail1);
            
            InvoiceDetail detail2 = new InvoiceDetail();
            detail2.setCode("002");
            detail2.setDescription("Producto B");
            detail2.setQuantity(new BigDecimal("1"));
            detail2.setUnitPrice(new BigDecimal("30.00"));
            detail2.setTaxCode("2");
            detail2.setTaxRate(new BigDecimal("12"));
            detail2.setLineTotal(new BigDecimal("30.00"));
            
            invoice.getDetails().add(detail2);
            
            // 8. Calcular totales
            BigDecimal subtotal = new BigDecimal("130.00");
            BigDecimal iva = subtotal.multiply(new BigDecimal("0.12"));
            BigDecimal total = subtotal.add(iva);
            
            invoice.setSubtotal(subtotal);
            invoice.setIvaTotal(iva);
            invoice.setTotal(total);
            
            // 9. Agregar método de pago
            PaymentMethod payment = new PaymentMethod();
            payment.setCode("01"); // Efectivo
            payment.setAmount(total);
            payment.setDescription("Efectivo");
            
            invoice.getPaymentMethods().add(payment);
            
            // 10. Generar XML
            System.out.println("Generando XML...");
            invoiceService.generateInvoiceXML(invoice);
            System.out.println("✓ XML generado exitosamente");
            System.out.println("Clave de acceso: " + invoice.getAccessKey());
            
            // 11. Mostrar XML
            System.out.println("\n--- XML GENERADO ---");
            System.out.println(invoice.getXmlContent());
            
            // 12. Firma digital (comentado porque requiere certificado)
            // System.out.println("\nFirmando documento...");
            // invoiceService.signInvoice(invoice);
            // System.out.println("✓ Documento firmado");
            
            // 13. Envío a SRI (comentado porque requiere ambiente SRI disponible)
            // System.out.println("\nEnviando al SRI...");
            // invoiceService.sendToSRI(invoice);
            // System.out.println("✓ Enviado al SRI");
            
            // 14. Mostrar estado
            System.out.println("\n--- ESTADO FACTURA ---");
            System.out.println("Estado: " + invoiceService.getInvoiceStatus(invoice));
            System.out.println("Subtotal: " + invoice.getSubtotal());
            System.out.println("IVA: " + invoice.getIvaTotal());
            System.out.println("Total: " + invoice.getTotal());
            
        } catch (Exception e) {
            System.err.println("Error al procesar factura:");
            e.printStackTrace();
        }
    }
}
