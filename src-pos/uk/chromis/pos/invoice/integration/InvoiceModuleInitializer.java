package uk.chromis.pos.invoice.integration;

import uk.chromis.pos.invoice.InvoiceModule;
import uk.chromis.pos.invoice.forms.InvoiceConfigurationPanel;
import uk.chromis.pos.invoice.forms.CreateInvoicePanel;
import uk.chromis.pos.invoice.forms.InvoiceListPanel;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

/**
 * Inicializador del módulo de facturación en ChromisPOS
 */
public class InvoiceModuleInitializer {
    
    private static final String CONFIG_FILE = "chromispos-invoice.properties";
    private static final String LOG_FILE = "logs/invoice/module.log";
    
    private static InvoiceModule module;
    private static Properties config;
    
    /**
     * Inicializa el módulo de facturación
     */
    public static boolean initializeModule() {
        try {
            log("Iniciando módulo de facturación electrónica...");
            
            // Cargar configuración
            config = loadConfiguration();
            
            // Obtener instancia del módulo
            module = InvoiceModule.getInstance();
            
            // Inicializar con certificado si está disponible
            String certPath = config.getProperty("invoice.certificate.path", "");
            String certPass = config.getProperty("invoice.certificate.password", "");
            boolean testMode = config.getProperty("invoice.environment", "test").equals("test");
            
            if (!certPath.isEmpty()) {
                if (module.initialize(certPath, certPass, testMode)) {
                    log("Módulo inicializado correctamente con certificado");
                    return true;
                }
            } else {
                log("AVISO: Certificado no configurado. Módulo en modo demostración");
                return true;
            }
            
        } catch (Exception e) {
            log("ERROR al inicializar módulo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }
    
    /**
     * Carga archivo de configuración
     */
    private static Properties loadConfiguration() throws IOException {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                log("Configuración cargada desde: " + CONFIG_FILE);
            }
        } else {
            log("AVISO: Archivo de configuración no encontrado: " + CONFIG_FILE);
        }
        
        return props;
    }
    
    /**
     * Obtiene el módulo inicializado
     */
    public static InvoiceModule getModule() {
        if (module == null) {
            initializeModule();
        }
        return module;
    }
    
    /**
     * Obtiene la configuración
     */
    public static Properties getConfiguration() {
        if (config == null) {
            try {
                config = loadConfiguration();
            } catch (IOException e) {
                log("Error cargando configuración: " + e.getMessage());
            }
        }
        return config;
    }
    
    /**
     * Crea panel de configuración
     */
    public static JPanel getConfigurationPanel() {
        return new InvoiceConfigurationPanel(module.getInvoiceService());
    }
    
    /**
     * Crea panel de creación de facturas
     */
    public static JPanel getCreateInvoicePanel() {
        return new CreateInvoicePanel(module.getInvoiceService());
    }
    
    /**
     * Crea panel de listado de facturas
     */
    public static JPanel getInvoiceListPanel() {
        return new InvoiceListPanel(module.getInvoiceService());
    }
    
    /**
     * Registra mensajes de log
     */
    private static void log(String message) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new java.util.Date());
        String logMessage = "[" + timestamp + "] " + message;
        System.out.println(logMessage);
        
        // Escribir a archivo de log
        try {
            File logDir = new File("logs/invoice");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            try (FileWriter fw = new FileWriter(LOG_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(logMessage);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo log: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene status del módulo
     */
    public static String getModuleStatus() {
        if (module == null) {
            return "NO_INICIALIZADO";
        }
        
        java.util.Map<String, Boolean> status = module.getStatus();
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== Estado del Módulo de Facturación ===\n");
        status.forEach((key, value) -> 
            sb.append(key).append(": ").append(value ? "✓" : "✗").append("\n")
        );
        
        return sb.toString();
    }
    
    /**
     * Muestra ventana de bienvenida
     */
    public static void showWelcomeDialog() {
        String message = "Módulo de Facturación Electrónica inicializado correctamente\n" +
                        "\n" +
                        "Características:\n" +
                        "• Generación de facturas electrónicas\n" +
                        "• Firma digital con certificados X.509\n" +
                        "• Integración con SRI (Ecuador)\n" +
                        "• Gestión de pagos y descuentos\n" +
                        "• Validación de datos fiscales\n" +
                        "\n" +
                        "Para comenzar: Menú Administración > Facturación Electrónica";
        
        JOptionPane.showMessageDialog(
            null,
            message,
            "Facturación Electrónica - ChromisPOS",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
