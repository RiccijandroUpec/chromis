package uk.chromis.pos.invoice.models;

import java.io.Serializable;

/**
 * Información del Comprador (Cliente)
 */
public class InvoiceBuyer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String identification; // Cédula o RUC
    private String identificationType; // Tipo de ID: C (cédula), R (RUC), P (pasaporte)
    private String businessName; // Razón social (opcional)
    private String email; // Email
    private String address; // Dirección
    
    // Constructor
    public InvoiceBuyer() {
        this.identificationType = "C"; // Por defecto cédula
    }
    
    // Constructor con parámetros
    public InvoiceBuyer(String identification, String identificationType) {
        this.identification = identification;
        this.identificationType = identificationType;
    }
    
    // Getters y Setters
    public String getIdentification() {
        return identification;
    }
    
    public void setIdentification(String identification) {
        this.identification = identification;
    }
    
    public String getIdentificationType() {
        return identificationType;
    }
    
    public void setIdentificationType(String identificationType) {
        this.identificationType = identificationType;
    }
    
    public String getBusinessName() {
        return businessName;
    }
    
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
}
