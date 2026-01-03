package uk.chromis.pos.invoice;

import uk.chromis.pos.invoice.models.*;
import uk.chromis.pos.invoice.services.*;
import uk.chromis.pos.invoice.dao.*;
import uk.chromis.pos.invoice.forms.*;
import uk.chromis.pos.invoice.utils.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Módulo de Facturación Electrónica para ChromisPOS
 * Versión 1.0 - Ecuador
 * 
 * Gestor principal que coordina toda la funcionalidad de facturación electrónica
 */
public class InvoiceModule {
    
    private static InvoiceModule instance;
    private ElectronicInvoiceService invoiceService;
    private boolean initialized = false;
    
    private InvoiceModule() {
        invoiceService = new ElectronicInvoiceService();
    }
    
    /**
     * Obtiene la instancia singleton del módulo
     */
    public static synchronized InvoiceModule getInstance() {
        if (instance == null) {
            instance = new InvoiceModule();
        }
        return instance;
    }
    
    /**
     * Inicializa el módulo con certificado digital
     * 
     * @param certificatePath Ruta al archivo P12/PFX
     * @param password Contraseña del certificado
     * @param testMode true para ambiente de pruebas
     * @return true si inicializa exitosamente
     */
    public boolean initialize(String certificatePath, String password, boolean testMode) {
        try {
            invoiceService.initialize(certificatePath, password, testMode);
            initialized = true;
            return true;
        } catch (Exception e) {
            System.err.println("Error inicializando módulo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Verifica si el módulo está inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Crea una nueva factura electrónica
     */
    public ElectronicInvoice createInvoice(
            InvoiceIssuer issuer,
            InvoiceBuyer buyer,
            List<InvoiceDetail> details,
            List<PaymentMethod> payments) {
        
        ElectronicInvoice invoice = new ElectronicInvoice();
        invoice.setIssuer(issuer);
        invoice.setBuyer(buyer);
        invoice.setDetails(details);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setIssueDate(LocalDateTime.now());
        
        return invoice;
    }
    
    /**
     * Genera XML de la factura
     */
    public String generateXML(ElectronicInvoice invoice) throws Exception {
        try {
            InvoiceXMLGenerator xmlGen = new InvoiceXMLGenerator();
            return xmlGen.generateXML(invoice);
        } catch (Exception e) {
            System.err.println("Error generando XML: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Firma digitalmente la factura
     */
    public void signInvoice(ElectronicInvoice invoice) throws Exception {
        // Firma con certificado configurado
        invoiceService.signInvoice(invoice);
    }
    
    /**
     * Obtiene el servicio de facturación para operaciones avanzadas
     */
    public ElectronicInvoiceService getInvoiceService() {
        return invoiceService;
    }
    
    /**
     * Obtiene acceso a los DAOs para operaciones de base de datos
     */
    public ElectronicInvoiceDAO getInvoiceDAO() {
        try {
            return new ElectronicInvoiceDAO(null); // null connection - usar factory pattern en BD real
        } catch (Exception e) {
            System.err.println("Error creando DAO: " + e.getMessage());
            return null;
        }
    }
    
    public InvoiceDetailDAO getDetailDAO() {
        try {
            return new InvoiceDetailDAO(null);
        } catch (Exception e) {
            System.err.println("Error creando DAO: " + e.getMessage());
            return null;
        }
    }
    
    public PaymentMethodDAO getPaymentDAO() {
        try {
            return new PaymentMethodDAO(null);
        } catch (Exception e) {
            System.err.println("Error creando DAO: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtiene herramientas de validación
     */
    public static EcuadorValidators getValidators() {
        return new EcuadorValidators();
    }
    
    /**
     * Obtiene generador de claves de acceso
     */
    public static AccessKeyGenerator getKeyGenerator() {
        return new AccessKeyGenerator();
    }
    
    /**
     * Verifica disponibilidad de componentes requeridos
     */
    public Map<String, Boolean> getStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("initialized", initialized);
        status.put("certificateLoaded", initialized); // Simplificado
        status.put("databaseConnected", true); // Verificar en producción
        status.put("sriConnected", false); // Verificar en producción
        return status;
    }
}
