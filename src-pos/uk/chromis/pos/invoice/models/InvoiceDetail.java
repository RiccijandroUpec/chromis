package uk.chromis.pos.invoice.models;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Detalle de línea en la factura
 */
public class InvoiceDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code; // Código del producto
    private String description; // Descripción del producto
    private BigDecimal quantity; // Cantidad
    private BigDecimal unitPrice; // Precio unitario
    private BigDecimal discount; // Descuento (porcentaje)
    private String taxCode; // Código de IVA (0%, 5%, 12%)
    private BigDecimal taxRate; // Tasa de IVA
    private BigDecimal lineTotal; // Total de la línea
    
    // Constructor
    public InvoiceDetail() {
        this.quantity = BigDecimal.ZERO;
        this.unitPrice = BigDecimal.ZERO;
        this.discount = BigDecimal.ZERO;
        this.taxRate = BigDecimal.ZERO;
        this.lineTotal = BigDecimal.ZERO;
    }
    
    // Constructor con parámetros
    public InvoiceDetail(String code, String description, BigDecimal quantity, BigDecimal unitPrice) {
        this();
        this.code = code;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Getters y Setters
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
    
    public BigDecimal getQuantity() {
        return quantity;
    }
    
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getDiscount() {
        return discount;
    }
    
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
    
    public String getTaxCode() {
        return taxCode;
    }
    
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }
    
    public BigDecimal getTaxRate() {
        return taxRate;
    }
    
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
    
    public BigDecimal getLineTotal() {
        return lineTotal;
    }
    
    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
