package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;

/**
 * Servicio principal de facturación electrónica
 * Orquesta el flujo completo de generación, firma y envío de facturas
 */
public class ElectronicInvoiceService {
    
    private InvoiceXMLGenerator xmlGenerator;
    private DigitalSignatureService signatureService;
    private SRIIntegrationService sriService;
    
    public ElectronicInvoiceService() {
        this.xmlGenerator = new InvoiceXMLGenerator();
        this.signatureService = null; // Se inicializa con certificado
        this.sriService = new SRIIntegrationService(false); // false = pruebas, true = producción
    }
    
    /**
     * Inicializa el servicio con los parámetros necesarios
     */
    public void initialize(String certificatePath, String certificatePassword, boolean production) {
        this.signatureService = new DigitalSignatureService(certificatePath, certificatePassword);
        this.sriService = new SRIIntegrationService(production);
    }
    
    /**
     * Procesa una factura completamente:
     * 1. Genera XML
     * 2. Firma digitalmente
     * 3. Envía al SRI
     */
    public void processInvoice(ElectronicInvoice invoice) throws Exception {
        // Paso 1: Generar XML
        generateInvoiceXML(invoice);
        
        // Paso 2: Firmar documento
        if (signatureService != null) {
            signInvoice(invoice);
        }
        
        // Paso 3: Enviar al SRI
        sendToSRI(invoice);
    }
    
    /**
     * Genera el XML de la factura
     */
    public void generateInvoiceXML(ElectronicInvoice invoice) throws Exception {
        xmlGenerator.generateXML(invoice);
    }
    
    /**
     * Firma digitalmente la factura
     */
    public void signInvoice(ElectronicInvoice invoice) throws Exception {
        if (signatureService == null) {
            throw new IllegalStateException("Signature service not initialized. Call initialize() first.");
        }
        signatureService.signInvoice(invoice);
    }
    
    /**
     * Envía la factura al SRI
     */
    public void sendToSRI(ElectronicInvoice invoice) throws Exception {
        sriService.sendInvoiceToSRI(invoice);
    }
    
    /**
     * Obtiene el estado de una factura
     */
    public String getInvoiceStatus(ElectronicInvoice invoice) {
        return invoice.getStatus().getDisplayName();
    }
    
    /**
     * Obtiene el servicio de generación de XML
     */
    public InvoiceXMLGenerator getXMLGenerator() {
        return xmlGenerator;
    }
    
    /**
     * Obtiene el servicio de firma digital
     */
    public DigitalSignatureService getSignatureService() {
        return signatureService;
    }
    
    /**
     * Obtiene el servicio de integración SRI
     */
    public SRIIntegrationService getSRIService() {
        return sriService;
    }
}
