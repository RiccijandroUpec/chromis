/*
**    Chromis POS  - Open Source Point of Sale
**    SetupWizardDialog - Asistente de Configuración Inicial
**
**    Autor: Riccijandro | github.com/riccijandro
**    Contacto: richardrodriguez271@gmail.com
**
**    This program is free software: you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation, either version 3 of the License.
*/

package uk.chromis.pos.forms;

import uk.chromis.pos.config.ConfiguracionPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SetupWizardDialog extends JDialog {
    
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JButton btnNext, btnBack, btnFinish, btnCancel;
    private int currentStep = 0;
    private static final int TOTAL_STEPS = 4;
    
    // Campos de configuración
    private JTextField txtDBHost, txtDBPort, txtDBName, txtDBUser;
    private JPasswordField txtDBPass;
    private JTextField txtRUC, txtBusinessName, txtTradeName;
    private JTextField txtCertPath;
    private JPasswordField txtCertPass;
    private JComboBox<String> cmbEnvironment;
    
    public SetupWizardDialog(JFrame parent) {
        super(parent, "Asistente de Configuración Inicial - ChromisPOS Ecuador", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(650, 500));
        
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);
        
        // Panel 1: Bienvenida
        mainPanel.add(createWelcomePanel(), "step0");
        // Panel 2: Base de Datos
        mainPanel.add(createDatabasePanel(), "step1");
        // Panel 3: Datos de la Empresa
        mainPanel.add(createCompanyPanel(), "step2");
        // Panel 4: Facturación Electrónica
        mainPanel.add(createInvoicePanel(), "step3");
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        btnBack = new JButton("← Anterior");
        btnBack.setEnabled(false);
        btnBack.addActionListener(e -> previousStep());
        
        btnNext = new JButton("Siguiente →");
        btnNext.addActionListener(e -> nextStep());
        
        btnFinish = new JButton("✔ Finalizar");
        btnFinish.setVisible(false);
        btnFinish.addActionListener(e -> finishWizard());
        
        btnCancel = new JButton("Cancelar");
        btnCancel.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de cancelar? La configuración no se guardará.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                dispose();
            }
        });
        
        buttonPanel.add(btnBack);
        buttonPanel.add(btnNext);
        buttonPanel.add(btnFinish);
        buttonPanel.add(btnCancel);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(79, 70, 229)); // Indigo
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel lblTitle = new JLabel("⚙ Configuración Inicial - ChromisPOS Ecuador");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblStep = new JLabel("Paso 1 de " + TOTAL_STEPS);
        lblStep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStep.setForeground(new Color(199, 210, 254));
        lblStep.setName("lblStep");
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblStep, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        
        JLabel welcome = new JLabel("¡Bienvenido a ChromisPOS Ecuador!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        JTextArea desc = new JTextArea(
            "Este asistente le guiará en la configuración inicial del sistema.\n\n" +
            "Pasos a realizar:\n" +
            "  1. Configurar conexión a Base de Datos\n" +
            "  2. Datos de la empresa (RUC, Razón Social)\n" +
            "  3. Configuración de Facturación Electrónica SRI\n\n" +
            "Tiempo estimado: 5 minutos\n" +
            "¿Está listo para comenzar?"
        );
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        desc.setEditable(false);
        desc.setBackground(panel.getBackground());
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        
        gbc.gridy = 0;
        panel.add(welcome, gbc);
        gbc.gridy = 1;
        panel.add(desc, gbc);
        
        return panel;
    }
    
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Configuración de Base de Datos");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Host:"), gbc);
        gbc.gridx = 1;
        txtDBHost = new JTextField("localhost", 20);
        panel.add(txtDBHost, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Puerto:"), gbc);
        gbc.gridx = 1;
        txtDBPort = new JTextField("3306", 10);
        panel.add(txtDBPort, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Base de Datos:"), gbc);
        gbc.gridx = 1;
        txtDBName = new JTextField("chromispos_ec", 20);
        panel.add(txtDBName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtDBUser = new JTextField("root", 20);
        panel.add(txtDBUser, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtDBPass = new JPasswordField(20);
        panel.add(txtDBPass, gbc);
        
        // Botón probar conexión
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        JButton btnTest = new JButton("🔌 Probar Conexión");
        btnTest.setBackground(new Color(16, 185, 129));
        btnTest.setForeground(Color.WHITE);
        btnTest.addActionListener(e -> testConnection());
        panel.add(btnTest, gbc);
        
        return panel;
    }
    
    private JPanel createCompanyPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Datos de la Empresa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("RUC:"), gbc);
        gbc.gridx = 1;
        txtRUC = new JTextField(20);
        panel.add(txtRUC, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Razón Social:"), gbc);
        gbc.gridx = 1;
        txtBusinessName = new JTextField(30);
        panel.add(txtBusinessName, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Nombre Comercial:"), gbc);
        gbc.gridx = 1;
        txtTradeName = new JTextField(30);
        panel.add(txtTradeName, gbc);
        
        return panel;
    }
    
    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblTitle = new JLabel("Configuración de Facturación Electrónica");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        panel.add(new JLabel("Ambiente:"), gbc);
        gbc.gridx = 1;
        cmbEnvironment = new JComboBox<>(new String[]{"Pruebas", "Producción"});
        panel.add(cmbEnvironment, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Archivo .p12:"), gbc);
        gbc.gridx = 1;
        JPanel certPanel = new JPanel(new BorderLayout());
        txtCertPath = new JTextField(20);
        JButton btnBrowse = new JButton("📁");
        btnBrowse.addActionListener(e -> browseCertificate());
        certPanel.add(txtCertPath, BorderLayout.CENTER);
        certPanel.add(btnBrowse, BorderLayout.EAST);
        panel.add(certPanel, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Contraseña Firma:"), gbc);
        gbc.gridx = 1;
        txtCertPass = new JPasswordField(20);
        panel.add(txtCertPass, gbc);
        
        return panel;
    }
    
    private void browseCertificate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Certificados Digitales (.p12, .pfx)", "p12", "pfx"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            txtCertPath.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void testConnection() {
        String host = txtDBHost.getText();
        String port = txtDBPort.getText();
        String db = txtDBName.getText();
        String user = txtDBUser.getText();
        String pass = new String(txtDBPass.getPassword());
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db +
                        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            java.sql.Connection conn = java.sql.DriverManager.getConnection(url, user, pass);
            conn.close();
            JOptionPane.showMessageDialog(this, "✅ Conexión exitosa a la base de datos!",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ Error de conexión:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void nextStep() {
        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            cardLayout.show(mainPanel, "step" + currentStep);
            updateButtons();
        }
    }
    
    private void previousStep() {
        if (currentStep > 0) {
            currentStep--;
            cardLayout.show(mainPanel, "step" + currentStep);
            updateButtons();
        }
    }
    
    private void updateButtons() {
        btnBack.setEnabled(currentStep > 0);
        btnNext.setVisible(currentStep < TOTAL_STEPS - 1);
        btnFinish.setVisible(currentStep == TOTAL_STEPS - 1);
        
        // Actualizar label de paso
        for (Component c : ((Container) getContentPane().getComponent(0)).getComponents()) {
            if (c instanceof JLabel && "lblStep".equals(c.getName())) {
                ((JLabel) c).setText("Paso " + (currentStep + 1) + " de " + TOTAL_STEPS);
            }
        }
    }
    
    private void finishWizard() {
        // Guardar configuración en chromisposconfig.properties
        try {
            Properties props = new Properties();
            File configFile = new File("chromisposconfig.properties");
            
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
            }
            
            // Database
            props.setProperty("database.server", txtDBHost.getText());
            props.setProperty("database.port", txtDBPort.getText());
            props.setProperty("database.name", txtDBName.getText());
            props.setProperty("database.user", txtDBUser.getText());
            props.setProperty("database.password", new String(txtDBPass.getPassword()));
            
            // Company
            props.setProperty("invoice.issuer.ruc", txtRUC.getText());
            props.setProperty("invoice.issuer.businessName", txtBusinessName.getText());
            props.setProperty("invoice.issuer.tradeName", txtTradeName.getText());
            
            // Invoice
            props.setProperty("invoice.environment", 
                cmbEnvironment.getSelectedIndex() == 0 ? "test" : "production");
            props.setProperty("invoice.certificate.path", txtCertPath.getText());
            props.setProperty("invoice.certificate.password", new String(txtCertPass.getPassword()));
            props.setProperty("invoice.enabled", "true");
            
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "ChromisPOS Ecuador Configuration - Generated by Setup Wizard");
            }
            
            JOptionPane.showMessageDialog(this,
                "✅ Configuración guardada exitosamente!\n\n" +
                "El sistema se iniciará con la configuración proporcionada.",
                "Configuración Completa", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "❌ Error al guardar configuración:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static boolean isFirstRun() {
        File configFile = new File("chromisposconfig.properties");
        if (!configFile.exists()) return true;
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            Properties props = new Properties();
            props.load(fis);
            String dbName = props.getProperty("database.name", "");
            String ruc = props.getProperty("invoice.issuer.ruc", "");
            return dbName.isEmpty() || ruc.isEmpty();
        } catch (IOException e) {
            return true;
        }
    }
}
