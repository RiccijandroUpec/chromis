package uk.chromis.pos.invoice.models;

/**
 * Estados posibles de una factura electrónica
 */
public enum InvoiceStatus {
    DRAFT("Borrador"),
    GENERATED("Generada"),
    SIGNED("Firmada"),
    SENT_TO_SRI("Enviada al SRI"),
    AUTHORIZED("Autorizada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada");
    
    private final String displayName;
    
    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
