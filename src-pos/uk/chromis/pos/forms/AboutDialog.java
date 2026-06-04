/*
**    Chromis POS - About Dialog
**    Información del sistema y créditos
**
**    Autor: Riccijandro | github.com/riccijandro
**    Contacto: richardrodriguez271@gmail.com
*/

package uk.chromis.pos.forms;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class AboutDialog extends JDialog {
    
    public AboutDialog(JFrame parent) {
        super(parent, "Acerca de ChromisPOS Ecuador", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Color.WHITE);
        
        // Logo y título
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel logoLabel = new JLabel("🛒", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 64));
        
        JLabel titleLabel = new JLabel("ChromisPOS Ecuador", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(79, 70, 229));
        
        // Versión
        String version = getVersion();
        JLabel versionLabel = new JLabel("Versión " + version, SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setForeground(Color.GRAY);
        
        JPanel titlePanel = new JPanel(new GridLayout(3, 1));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(logoLabel);
        titlePanel.add(titleLabel);
        titlePanel.add(versionLabel);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        // Información
        JTextArea info = new JTextArea(
            "\n" +
            "Punto de Venta Open Source adaptado para Ecuador\n" +
            "Completamente integrado con el Servicio de Rentas Internas (SRI)\n\n" +
            "✅ Facturación Electrónica\n" +
            "✅ Notas de Crédito\n" +
            "✅ Guías de Remisión\n" +
            "✅ Clave de Acceso 49 dígitos\n" +
            "✅ IVA 0%, 5%, 12%, 14%, 15%\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "👤 Autor: Riccijandro\n" +
            "📧 richardrodriguez271@gmail.com\n" +
            "📱 WhatsApp: +593 98 318 5069\n" +
            "🐙 github.com/riccijandro\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
            "Licencia: GNU General Public License v3.0\n" +
            "Basado en Chromis POS (chromis.co.uk)\n" +
            "y Openbravo POS"
        );
        info.setEditable(false);
        info.setBackground(Color.WHITE);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        
        // Botón cerrar
        JButton btnClose = new JButton("Cerrar");
        btnClose.setBackground(new Color(79, 70, 229));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnClose);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(info, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(panel);
    }
    
    private String getVersion() {
        try {
            Properties props = new Properties();
            File configFile = new File("chromisposconfig.properties");
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
                return props.getProperty("application.version", "1.5.5");
            }
        } catch (IOException e) {
            // Ignorar
        }
        return AppLocal.APP_VERSION;
    }
}
