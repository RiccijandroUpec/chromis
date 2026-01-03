package uk.chromis.pos.invoice.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Servicio para calcular totales de facturas incluyendo impuestos
 */
public class InvoiceTotalsCalculator {
    
    // Tasas de impuesto en Ecuador
    public static final BigDecimal IVA_RATE_12 = new BigDecimal("0.12");
    public static final BigDecimal IVA_RATE_0 = new BigDecimal("0.00");
    
    public static class InvoiceTotal {
        public BigDecimal subtotal;
        public BigDecimal subtotalGravado;
        public BigDecimal subtotalExento;
        public BigDecimal iva12;
        public BigDecimal iva0;
        public BigDecimal descuentoTotal;
        public BigDecimal totalImpuestos;
        public BigDecimal total;
        
        @Override
        public String toString() {
            return String.format(
                "Subtotal: $%.2f | IVA 12%%: $%.2f | Total: $%.2f",
                subtotal, iva12, total
            );
        }
    }
    
    /**
     * Calcula los totales de una factura
     * @param subtotalGravado Subtotal de productos con IVA 12%
     * @param subtotalExento Subtotal de productos con IVA 0%
     * @param descuentoTotal Descuento total aplicado
     * @return InvoiceTotal con todos los cálculos
     */
    public static InvoiceTotal calculateTotals(
            BigDecimal subtotalGravado,
            BigDecimal subtotalExento,
            BigDecimal descuentoTotal) {
        
        if (subtotalGravado == null) subtotalGravado = BigDecimal.ZERO;
        if (subtotalExento == null) subtotalExento = BigDecimal.ZERO;
        if (descuentoTotal == null) descuentoTotal = BigDecimal.ZERO;
        
        InvoiceTotal result = new InvoiceTotal();
        
        // Subtotal antes de impuestos
        result.subtotalGravado = subtotalGravado;
        result.subtotalExento = subtotalExento;
        result.subtotal = subtotalGravado.add(subtotalExento);
        
        // Descuento
        result.descuentoTotal = descuentoTotal;
        BigDecimal baseConDescuento = result.subtotal.subtract(descuentoTotal);
        
        // IVA (solo aplica a productos gravados)
        result.iva12 = baseConDescuento
                .multiply(subtotalGravado.divide(result.subtotal, 10, RoundingMode.HALF_UP))
                .multiply(IVA_RATE_12)
                .setScale(2, RoundingMode.HALF_UP);
        
        result.iva0 = BigDecimal.ZERO; // En Ecuador, IVA 0% no genera impuesto
        
        // Total de impuestos
        result.totalImpuestos = result.iva12.add(result.iva0);
        
        // Total final
        result.total = baseConDescuento.add(result.totalImpuestos)
                .setScale(2, RoundingMode.HALF_UP);
        
        return result;
    }
    
    /**
     * Calcula los totales de una factura (versión simplificada)
     * @param subtotal Subtotal sin impuestos
     * @param descuento Descuento
     * @return InvoiceTotal con todos los cálculos
     */
    public static InvoiceTotal calculateTotals(BigDecimal subtotal, BigDecimal descuento) {
        if (subtotal == null) subtotal = BigDecimal.ZERO;
        if (descuento == null) descuento = BigDecimal.ZERO;
        
        // Asumir que todo es gravado (IVA 12%)
        return calculateTotals(subtotal, BigDecimal.ZERO, descuento);
    }
    
    /**
     * Calcula solo el subtotal de una lista de líneas
     * @param unitPrice Precio unitario
     * @param quantity Cantidad
     * @return Subtotal de la línea
     */
    public static BigDecimal calculateLineSubtotal(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) unitPrice = BigDecimal.ZERO;
        return unitPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calcula el IVA de un monto
     * @param amount Monto gravado
     * @param rate Tasa de IVA
     * @return IVA calculado
     */
    public static BigDecimal calculateIVA(BigDecimal amount, BigDecimal rate) {
        if (amount == null) amount = BigDecimal.ZERO;
        if (rate == null) rate = IVA_RATE_12;
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Prueba del calculador de totales
     */
    public static void main(String[] args) {
        // Ejemplo: Factura con subtotal $100
        BigDecimal subtotal = new BigDecimal("100.00");
        BigDecimal descuento = new BigDecimal("10.00");
        
        InvoiceTotal totals = calculateTotals(subtotal, descuento);
        
        System.out.println("═══════════════════════════════════");
        System.out.println("CÁLCULO DE TOTALES");
        System.out.println("═══════════════════════════════════");
        System.out.printf("Subtotal:           $%.2f%n", totals.subtotal);
        System.out.printf("Descuento:          -$%.2f%n", totals.descuentoTotal);
        System.out.printf("Base para impuesto: $%.2f%n", totals.subtotal.subtract(totals.descuentoTotal));
        System.out.printf("IVA 12%%:            $%.2f%n", totals.iva12);
        System.out.printf("─────────────────────────────────%n");
        System.out.printf("TOTAL:              $%.2f%n", totals.total);
        System.out.println("═══════════════════════════════════");
    }
}
