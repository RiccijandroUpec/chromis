package uk.chromis.pos.invoice.models;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Método de Pago
 */
public class PaymentMethod implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code; // 01=Efectivo, 16=Tarjeta débito, 17=Tarjeta crédito, 19=Cheque, 20=Transferencia, etc.
    private BigDecimal amount; // Monto pagado con este método
    private String description; // Descripción (ej: Visa, Mastercard)
    
    // Constructor
    public PaymentMethod() {
    }
    
    // Constructor con parámetros
    public PaymentMethod(String code, BigDecimal amount) {
        this.code = code;
        this.amount = amount;
    }
    
    // Getters y Setters
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
