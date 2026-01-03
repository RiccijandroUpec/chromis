package uk.chromis.pos.invoice.integration;

import uk.chromis.pos.invoice.InvoiceModule;
import uk.chromis.pos.invoice.forms.*;
import uk.chromis.pos.invoice.integration.InvoiceModuleInitializer;

import javax.swing.*;
import java.awt.*;

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
    
    /**
     * Obtiene la instancia del módulo para uso avanzado
     */
    public static InvoiceModule getInvoiceModule() {
        return InvoiceModuleInitializer.getModule();
    }
}
