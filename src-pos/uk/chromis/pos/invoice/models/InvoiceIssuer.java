package uk.chromis.pos.invoice.models;

import java.io.Serializable;

/**
 * Información del Emisor (Proveedor)
 */
public class InvoiceIssuer implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ruc; // RUC del emisor
    private String businessName; // Razón Social
    private String tradeName; // Nombre Comercial
    private String address; // Dirección
    private String city;
    private String province;
    private String country;
    private String email;
    private String phone;
    
    // Constructor
    public InvoiceIssuer() {
    }
    
    // Constructor con parámetros
    public InvoiceIssuer(String ruc, String businessName) {
        this.ruc = ruc;
        this.businessName = businessName;
    }
    
    // Getters y Setters
    public String getRuc() {
        return ruc;
    }
    
    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
    
    public String getBusinessName() {
        return businessName;
    }
    
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    
    public String getTradeName() {
        return tradeName;
    }
    
    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
