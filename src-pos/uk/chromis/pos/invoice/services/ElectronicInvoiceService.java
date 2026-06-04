package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import java.io.File;
import java.io.FileWriter;

/**
 * Servicio principal de facturación electrónica
 * Orquesta el flujo completo de generación, firma, envío de facturas,
 * generación del RIDE (HTML) y envío automático por correo electrónico.
 */
public class ElectronicInvoiceService {
    
    private InvoiceXMLGenerator xmlGenerator;
    private DigitalSignatureService signatureService;
    private SRIIntegrationService sriService;
    private RideGeneratorService rideGenerator;
    private EmailSenderService emailSender;
    
    public ElectronicInvoiceService() {
        this.xmlGenerator = new InvoiceXMLGenerator();
        this.signatureService = null; // Se inicializa con certificado en initialize()
        this.sriService = new SRIIntegrationService(false); // false = pruebas, true = producción
        this.rideGenerator = new RideGeneratorService();
        this.emailSender = new EmailSenderService();
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
     * 2. Firma digitalmente (XAdES-BES)
     * 3. Envía al SRI
     * 4. Genera representación impresa RIDE
     * 5. Envía la factura por correo de forma asíncrona
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
        
        // Paso 4: Generar RIDE y enviar correo electrónico si se transmitió
        if (invoice.isSentToSRI()) {
            try {
                String ridePath = rideGenerator.generateRide(invoice);
                System.out.println("✓ RIDE generado exitosamente en: " + ridePath);
                
                // Generar archivo XML local para adjuntar al correo
                File xmlFile = new File(rideGenerator.getOutputDirectory(), "XML_" + invoice.getInvoiceNumber() + ".xml");
                try (FileWriter fw = new FileWriter(xmlFile)) {
                    fw.write(invoice.getSignedXmlContent() != null ? invoice.getSignedXmlContent() : invoice.getXmlContent());
                }
                
                File rideFile = new File(ridePath);
                
                // Paso 5: Enviar correo electrónico al comprador
                if (invoice.getBuyer() != null && invoice.getBuyer().getEmail() != null && !invoice.getBuyer().getEmail().isEmpty()) {
                    String subject = "Comprobante Electrónico " + invoice.getInvoiceNumber() + " - " + invoice.getIssuer().getBusinessName();
                    
                    String body = "<h2>Estimado(a) " + invoice.getBuyer().getBusinessName() + ",</h2>" +
                                  "<p>Le informamos que se ha generado un comprobante electrónico para usted.</p>" +
                                  "<p><b>Detalles del documento:</b></p>" +
                                  "<ul>" +
                                  "  <li><b>Tipo de Documento:</b> " + ("04".equals(invoice.getDocumentType()) ? "Nota de Crédito" : "Factura") + "</li>" +
                                  "  <li><b>Número:</b> " + invoice.getInvoiceNumber() + "</li>" +
                                  "  <li><b>Clave de Acceso:</b> " + invoice.getAccessKey() + "</li>" +
                                  "  <li><b>Valor Total:</b> $ " + String.format("%.2f", invoice.getTotal()) + "</li>" +
                                  "</ul>" +
                                  "<p>Adjunto a este correo encontrará el archivo XML autorizado y la representación impresa del RIDE (en formato HTML).</p>" +
                                  "<p>Atentamente,<br/><b>" + invoice.getIssuer().getBusinessName() + "</b></p>";
                    
                    emailSender.sendEmailAsync(invoice.getBuyer().getEmail(), subject, body, xmlFile, rideFile);
                }
            } catch (Exception ex) {
                System.err.println("Advertencia: No se pudo generar RIDE o enviar correo: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
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
            throw new IllegalStateException("Servicio de firma digital no inicializado. Llama a initialize() primero.");
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
    
    public InvoiceXMLGenerator getXMLGenerator() {
        return xmlGenerator;
    }
    
    public DigitalSignatureService getSignatureService() {
        return signatureService;
    }
    
    public SRIIntegrationService getSRIService() {
        return sriService;
    }
    
    public RideGeneratorService getRideGenerator() {
        return rideGenerator;
    }
    
    public EmailSenderService getEmailSender() {
        return emailSender;
    }
}
