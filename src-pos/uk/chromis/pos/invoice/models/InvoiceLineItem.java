package uk.chromis.pos.invoice.models;

import uk.chromis.pos.invoice.services.InvoiceTotalsCalculator;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Modelo para líneas de detalle de factura
 */
public class InvoiceLineItem {
    
    private int lineNumber;
    private String code;
    private String description;
    private String unit; // Unidad de medida (unidad, docena, metro, etc)
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private String taxRate; // "12" para IVA 12%, "0" para IVA 0%
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal total;
    
    // Constructor vacío
    public InvoiceLineItem() {
        this.lineNumber = 1;
        this.unit = "unidad";
        this.quantity = 1;
        this.unitPrice = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.taxRate = "12";
        this.subtotal = BigDecimal.ZERO;
        this.tax = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
    }
    
    // Constructor completo
    public InvoiceLineItem(int lineNumber, String code, String description, 
                          String unit, int quantity, BigDecimal unitPrice,
                          String taxRate) {
        this.lineNumber = lineNumber;
        this.code = code;
        this.description = description;
        this.unit = unit;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = BigDecimal.ZERO;
        this.taxRate = taxRate;
        calculateTotals();
    }
    
    /**
     * Calcula subtotal, impuesto y total de la línea
     */
    public void calculateTotals() {
        // Subtotal = cantidad × precio unitario
        this.subtotal = InvoiceTotalsCalculator.calculateLineSubtotal(unitPrice, quantity);
        
        // Descuento
        if (discount == null) {
            discount = BigDecimal.ZERO;
        }
        BigDecimal subtotalAfterDiscount = subtotal.subtract(discount);
        
        // Impuesto
        BigDecimal taxRate = new BigDecimal(this.taxRate);
        this.tax = subtotalAfterDiscount
                .multiply(taxRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Total = subtotal - descuento + impuesto
        this.total = subtotalAfterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Valida que la línea tenga datos mínimos requeridos
     * @return true si es válida
     */
    public boolean isValid() {
        return code != null && !code.isEmpty()
            && description != null && !description.isEmpty()
            && quantity > 0
            && unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0;
    }
    
    // Getters y Setters
    public int getLineNumber() {
        return lineNumber;
    }
    
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateTotals();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateTotals();
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
        calculateTotals();
    }
    
    public String getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
        calculateTotals();
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public BigDecimal getTax() {
        return tax;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Línea %d: %s - Cantidad: %d x $%.2f = $%.2f (Impuesto: $%.2f)",
            lineNumber, description, quantity, unitPrice, subtotal, tax
        );
    }
}
