package uk.chromis.pos.invoice.integration;

import uk.chromis.pos.invoice.InvoiceModule;
import uk.chromis.pos.invoice.forms.*;
import uk.chromis.pos.invoice.integration.InvoiceModuleInitializer;
import uk.chromis.pos.ticket.TicketInfo;
import uk.chromis.pos.ticket.TicketLineInfo;
import uk.chromis.pos.payment.PaymentInfo;
import uk.chromis.pos.customers.CustomerInfoExt;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Clase para integrar el módulo de facturación en ChromisPOS
 * 
 * USO:
 * En tu clase principal POS.java, agregar en el constructor o método init():
 * 
 *   ChromisPOSInvoiceIntegration.integrate(this);
 * 
 * Donde 'this' es la instancia de POS (o JFrame principal)
 */
public class ChromisPOSInvoiceIntegration {
    
    /**
     * Integra el módulo de facturación en ChromisPOS
     * Llama a este método una sola vez en la inicialización de la aplicación
     */
    public static void integrate(JFrame mainWindow) {
        try {
            // 1. Inicializar módulo
            if (!InvoiceModuleInitializer.initializeModule()) {
                System.err.println("ERROR: No se pudo inicializar módulo de facturación");
                return;
            }
            
            System.out.println("✓ Módulo de facturación inicializado correctamente");
            
            // 2. Agregar menú a la barra de menús
            addInvoiceMenuToMenuBar(mainWindow);
            
            // 3. Mostrar ventana de bienvenida
            InvoiceModuleInitializer.showWelcomeDialog();
            
            System.out.println("✓ Integración completada exitosamente");
            
        } catch (Exception e) {
            System.err.println("ERROR integrando módulo: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Agrega el menú de facturación a la barra de menús principal
     */
    private static void addInvoiceMenuToMenuBar(JFrame mainWindow) {
        try {
            // Obtener la barra de menús
            JMenuBar menuBar = mainWindow.getJMenuBar();
            if (menuBar == null) {
                menuBar = new JMenuBar();
                mainWindow.setJMenuBar(menuBar);
            }
            
            // Crear menú principal
            JMenu menuFacturacion = new JMenu("Facturación Electrónica");
            menuFacturacion.setMnemonic('F');
            
            // OPCIÓN 1: Nueva Factura
            JMenuItem itemNuevaFactura = new JMenuItem("Nueva Factura");
            itemNuevaFactura.setMnemonic('N');
            itemNuevaFactura.addActionListener(e -> {
                showInvoicePanel(
                    InvoiceModuleInitializer.getCreateInvoicePanel(),
                    "Nueva Factura Electrónica"
                );
            });
            
            // OPCIÓN 2: Mis Facturas
            JMenuItem itemListaFacturas = new JMenuItem("Mis Facturas");
            itemListaFacturas.setMnemonic('M');
            itemListaFacturas.addActionListener(e -> {
                showInvoicePanel(
                    InvoiceModuleInitializer.getInvoiceListPanel(),
                    "Listado de Facturas"
                );
            });
            
            // OPCIÓN 3: Configuración
            JMenuItem itemConfiguracion = new JMenuItem("Configuración");
            itemConfiguracion.setMnemonic('C');
            itemConfiguracion.addActionListener(e -> {
                showInvoicePanel(
                    InvoiceModuleInitializer.getConfigurationPanel(),
                    "Configuración de Facturación"
                );
            });
            
            // SEPARADOR
            menuFacturacion.addSeparator();
            
            // OPCIÓN 4: Ver Estado
            JMenuItem itemEstado = new JMenuItem("Estado del Módulo");
            itemEstado.addActionListener(e -> {
                String estado = InvoiceModuleInitializer.getModuleStatus();
                JOptionPane.showMessageDialog(
                    mainWindow,
                    estado,
                    "Estado del Módulo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
            
            // Agregar items al menú
            menuFacturacion.add(itemNuevaFactura);
            menuFacturacion.add(itemListaFacturas);
            menuFacturacion.addSeparator();
            menuFacturacion.add(itemConfiguracion);
            menuFacturacion.add(itemEstado);
            
            // Agregar menú a la barra
            menuBar.add(menuFacturacion);
            
            System.out.println("✓ Menú de facturación agregado");
            
        } catch (Exception e) {
            System.err.println("ERROR agregando menú: " + e.getMessage());
        }
    }
    
    /**
     * Muestra un panel de facturación en una ventana modal
     */
    private static void showInvoicePanel(JPanel panel, String titulo) {
        if (panel == null) {
            JOptionPane.showMessageDialog(
                null,
                "Error al cargar el panel",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        JFrame ventana = new JFrame(titulo);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setSize(1024, 600);
        ventana.setLocationRelativeTo(null);
        ventana.setContentPane(panel);
        ventana.setVisible(true);
    }
    public static boolean isElectronicInvoiceActive = false;
    
    static {
        try {
            Properties props = new Properties();
            File f = new File("chromisposconfig.properties");
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    props.load(fis);
                    isElectronicInvoiceActive = Boolean.parseBoolean(props.getProperty("invoice.enabled", "false"));
                }
            }
        } catch (Exception e) {
            isElectronicInvoiceActive = false;
        }
    }
    
    public static void setElectronicInvoiceActive(boolean active) {
        isElectronicInvoiceActive = active;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    File f = new File("chromisposconfig.properties");
                    if (f.exists()) {
                        try (FileInputStream fis = new FileInputStream(f)) {
                            props.load(fis);
                        }
                    }
                    props.setProperty("invoice.enabled", String.valueOf(active));
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
                        props.store(fos, "Actualizado desde panel de ventas");
                    }
                } catch (Exception e) {
                    System.err.println("Error guardando estado de facturación: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Obtiene la instancia del módulo para uso avanzado
     */
    public static InvoiceModule getInvoiceModule() {
        return InvoiceModuleInitializer.getModule();
    }
    
    /**
     * Procesa un ticket de venta de ChromisPOS de forma asíncrona para facturación electrónica.
     */
    public static void processTicket(TicketInfo ticket) {
        if (!isElectronicInvoiceActive) {
            System.out.println("Facturación electrónica inactiva. Omitiendo procesamiento de factura.");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Cargar propiedades de configuración
                    Properties props = new Properties();
                    File f = new File("chromisposconfig.properties");
                    if (f.exists()) {
                        try (FileInputStream fis = new FileInputStream(f)) {
                            props.load(fis);
                        }
                    } else {
                        System.out.println("Configuración 'chromisposconfig.properties' no encontrada. Omitiendo facturación electrónica.");
                        return;
                    }

                    if (!Boolean.parseBoolean(props.getProperty("invoice.enabled", "false"))) {
                        System.out.println("Facturación electrónica deshabilitada (invoice.enabled=false).");
                        return;
                    }

                    System.out.println("Iniciando procesamiento de factura electrónica para ticket No. " + ticket.getTicketId());
                    
                    // 1. Instanciar y rellenar factura electrónica
                    uk.chromis.pos.invoice.models.ElectronicInvoice invoice = new uk.chromis.pos.invoice.models.ElectronicInvoice();
                    invoice.setInvoiceNumber(String.format("%09d", ticket.getTicketId()));
                    
                    // Determinar tipo de documento (01=Factura, 04=Nota de Crédito)
                    if (ticket.isRefund()) {
                        invoice.setDocumentType("04");
                        invoice.setModifiedDocumentType("01");
                        invoice.setModifiedDocumentNumber("001-001-" + String.format("%09d", ticket.getTicketId()));
                        invoice.setModificationReason("Devolución / Anulación de ticket");
                    } else {
                        invoice.setDocumentType("01");
                    }
                    
                    // Datos del emisor
                    uk.chromis.pos.invoice.models.InvoiceIssuer issuer = new uk.chromis.pos.invoice.models.InvoiceIssuer();
                    issuer.setRuc(props.getProperty("invoice.issuer.ruc", "9999999999999"));
                    issuer.setBusinessName(props.getProperty("invoice.issuer.businessName", "EMPRESA ECUADOR S.A."));
                    issuer.setTradeName(props.getProperty("invoice.issuer.tradeName", "NEGOCIO ECUADOR"));
                    issuer.setAddress(props.getProperty("invoice.issuer.address", "Dirección Emisor"));
                    invoice.setIssuer(issuer);
                    
                    // Datos del comprador
                    uk.chromis.pos.invoice.models.InvoiceBuyer buyer = new uk.chromis.pos.invoice.models.InvoiceBuyer();
                    CustomerInfoExt customer = ticket.getCustomer();
                    if (customer != null) {
                        buyer.setBusinessName(customer.getName());
                        String taxId = customer.getTaxid() != null ? customer.getTaxid().trim() : "";
                        buyer.setIdentification(taxId.isEmpty() ? "9999999999999" : taxId);
                        
                        if (taxId.length() == 13) {
                            buyer.setIdentificationType("04"); // RUC
                        } else if (taxId.length() == 10) {
                            buyer.setIdentificationType("05"); // Cédula
                        } else if (taxId.isEmpty() || taxId.equals("9999999999999")) {
                            buyer.setIdentificationType("07"); // Consumidor Final
                        } else {
                            buyer.setIdentificationType("06"); // Pasaporte/Otros
                        }
                        
                        buyer.setEmail(customer.getEmail() != null ? customer.getEmail() : "");
                    } else {
                        buyer.setBusinessName("CONSUMIDOR FINAL");
                        buyer.setIdentification("9999999999999");
                        buyer.setIdentificationType("07");
                        buyer.setEmail("");
                    }
                    invoice.setBuyer(buyer);
                    
                    // Rellenar líneas
                    java.util.List<uk.chromis.pos.invoice.models.InvoiceDetail> details = new java.util.ArrayList<>();
                    for (int i = 0; i < ticket.getLinesCount(); i++) {
                        TicketLineInfo line = ticket.getLine(i);
                        uk.chromis.pos.invoice.models.InvoiceDetail detail = new uk.chromis.pos.invoice.models.InvoiceDetail();
                        
                        String productCode = line.getProductID() != null ? line.getProductID() : "001";
                        detail.setCode(productCode.length() > 25 ? productCode.substring(0, 25) : productCode);
                        detail.setDescription(line.getProductName());
                        detail.setQuantity(new java.math.BigDecimal(line.getMultiply()));
                        detail.setUnitPrice(new java.math.BigDecimal(line.getPrice()));
                        detail.setDiscount(java.math.BigDecimal.ZERO);
                        detail.setLineTotal(new java.math.BigDecimal(line.getSubValue()));
                        
                        // Impuestos
                        detail.setTaxCode("2"); // 2 = IVA
                        double rate = line.getTaxInfo() != null ? line.getTaxInfo().getRate() * 100 : 12.0;
                        detail.setTaxRate(new java.math.BigDecimal(rate));
                        
                        details.add(detail);
                    }
                    invoice.setDetails(details);
                    
                    // Totales
                    invoice.setSubtotal(new java.math.BigDecimal(ticket.getSubTotal()));
                    invoice.setIvaTotal(new java.math.BigDecimal(ticket.getTax()));
                    invoice.setTotal(new java.math.BigDecimal(ticket.getTicketTotal()));
                    
                    // Métodos de pago
                    java.util.List<uk.chromis.pos.invoice.models.PaymentMethod> paymentMethods = new java.util.ArrayList<>();
                    if (ticket.getPayments() != null) {
                        for (PaymentInfo paymentInfo : ticket.getPayments()) {
                            uk.chromis.pos.invoice.models.PaymentMethod payment = new uk.chromis.pos.invoice.models.PaymentMethod();
                            payment.setAmount(new java.math.BigDecimal(paymentInfo.getPaid()));
                            
                            String name = paymentInfo.getName() != null ? paymentInfo.getName() : "cash";
                            if ("cash".equals(name)) {
                                payment.setCode("01"); // Efectivo
                            } else if ("magcard".equals(name)) {
                                payment.setCode("17"); // Tarjeta de Crédito
                            } else if ("paperin".equals(name)) {
                                payment.setCode("19"); // Cheque
                            } else {
                                payment.setCode("20"); // Transferencia/Otros
                            }
                            payment.setDescription(name);
                            paymentMethods.add(payment);
                        }
                    }
                    if (paymentMethods.isEmpty()) {
                        uk.chromis.pos.invoice.models.PaymentMethod defaultPayment = new uk.chromis.pos.invoice.models.PaymentMethod();
                        defaultPayment.setCode("01");
                        defaultPayment.setAmount(invoice.getTotal());
                        defaultPayment.setDescription("Efectivo");
                        paymentMethods.add(defaultPayment);
                    }
                    invoice.setPaymentMethods(paymentMethods);
                    
                    // 2. Inicializar y procesar a través del servicio
                    uk.chromis.pos.invoice.services.ElectronicInvoiceService service = new uk.chromis.pos.invoice.services.ElectronicInvoiceService();
                    String certPath = props.getProperty("invoice.certificate.path", "");
                    char[] certPass = uk.chromis.pos.invoice.utils.CipherUtil.decryptToCharArray(
                            props.getProperty("invoice.certificate.password", ""));
                    boolean isProd = "2".equals(props.getProperty("invoice.environment", "1"));

                    service.initialize(certPath, certPass, isProd);
                    
                    // Cargar configuración de correo específica del usuario que realiza la venta
                    String userId = "default";
                    if (ticket.getUser() != null) {
                        userId = ticket.getUser().getId();
                    }
                    service.getEmailSender().loadConfigurationForUser(userId);
                    
                    service.processInvoice(invoice);
                    
                    System.out.println("✓ Ticket " + ticket.getTicketId() + " procesado exitosamente por Facturación Electrónica.");
                    
                } catch (Exception e) {
                    System.err.println("Error procesando facturación electrónica automática: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
