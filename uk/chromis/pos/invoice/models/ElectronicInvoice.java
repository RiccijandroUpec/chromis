package uk.chromis.pos.invoice.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para Factura Electrónica - Ecuador (SRI)
 */
public class ElectronicInvoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String invoiceNumber;
    private String accessKey; // Clave de acceso SRI
    private LocalDateTime issueDate;
    private LocalDateTime issueTime;
    
    // Información del emisor
    private InvoiceIssuer issuer;
    
    // Información del comprador
    private InvoiceBuyer buyer;
    
    // Detalles de la factura
    private List<InvoiceDetail> details;
    private BigDecimal subtotal;
    private BigDecimal ivaTotal;
    private BigDecimal total;
    
    // Información de pago
    private List<PaymentMethod> paymentMethods;
    
    // Estado
    private InvoiceStatus status;
    private String authorizationNumber;
    private String authorizationDate;
    private String xmlContent;
    private String signedXmlContent;
    
    // Información de envío a SRI
    private boolean sentToSRI;
    private LocalDateTime sentDate;
    private String sriResponse;
    
    // Constructor
    public ElectronicInvoice() {
        this.details = new ArrayList<>();
        this.paymentMethods = new ArrayList<>();
        this.status = InvoiceStatus.DRAFT;
        this.sentToSRI = false;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    
    public String getAccessKey() {
        return accessKey;
    }
    
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    public LocalDateTime getIssueDate() {
        return issueDate;
    }
    
    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }
    
    public LocalDateTime getIssueTime() {
        return issueTime;
    }
    
    public void setIssueTime(LocalDateTime issueTime) {
        this.issueTime = issueTime;
    }
    
    public InvoiceIssuer getIssuer() {
        return issuer;
    }
    
    public void setIssuer(InvoiceIssuer issuer) {
        this.issuer = issuer;
    }
    
    public InvoiceBuyer getBuyer() {
        return buyer;
    }
    
    public void setBuyer(InvoiceBuyer buyer) {
        this.buyer = buyer;
    }
    
    public List<InvoiceDetail> getDetails() {
        return details;
    }
    
    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    public BigDecimal getIvaTotal() {
        return ivaTotal;
    }
    
    public void setIvaTotal(BigDecimal ivaTotal) {
        this.ivaTotal = ivaTotal;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }
    
    public void setPaymentMethods(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
    
    public InvoiceStatus getStatus() {
        return status;
    }
    
    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
    
    public String getAuthorizationNumber() {
        return authorizationNumber;
    }
    
    public void setAuthorizationNumber(String authorizationNumber) {
        this.authorizationNumber = authorizationNumber;
    }
    
    public String getAuthorizationDate() {
        return authorizationDate;
    }
    
    public void setAuthorizationDate(String authorizationDate) {
        this.authorizationDate = authorizationDate;
    }
    
    public String getXmlContent() {
        return xmlContent;
    }
    
    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }
    
    public String getSignedXmlContent() {
        return signedXmlContent;
    }
    
    public void setSignedXmlContent(String signedXmlContent) {
        this.signedXmlContent = signedXmlContent;
    }
    
    public boolean isSentToSRI() {
        return sentToSRI;
    }
    
    public void setSentToSRI(boolean sentToSRI) {
        this.sentToSRI = sentToSRI;
    }
    
    public LocalDateTime getSentDate() {
        return sentDate;
    }
    
    public void setSentDate(LocalDateTime sentDate) {
        this.sentDate = sentDate;
    }
    
    public String getSriResponse() {
        return sriResponse;
    }
    
    public void setSriResponse(String sriResponse) {
        this.sriResponse = sriResponse;
    }
}
