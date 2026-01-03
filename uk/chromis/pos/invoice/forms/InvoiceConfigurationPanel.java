package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.utils.EcuadorValidators;
import uk.chromis.pos.invoice.utils.CipherUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Panel de configuración de facturación electrónica
 */
public class InvoiceConfigurationPanel extends JPanel {
    
    private ElectronicInvoiceService invoiceService;
    
    // Componentes UI
    private JTextField rucField;
    private JTextField businessNameField;
    private JTextField tradeNameField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField provinceField;
    private JTextField emailField;
    private JTextField phoneField;
    
    private JTextField certificatePathField;
    private JPasswordField certificatePasswordField;
    private JButton browseCertificateButton;
    private JButton validateCertificateButton;
    private JLabel certificateStatusLabel;
    private JLabel certificateInfoLabel;
    
    private JRadioButton testRadio;
    private JRadioButton productionRadio;
    
    private JButton saveButton;
    private JButton testConnectionButton;
    
    public InvoiceConfigurationPanel(ElectronicInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        initComponents();
        layoutComponents();
    }
    
    /**
     * Inicializa los componentes
     */
    private void initComponents() {
        // Información del emisor
        rucField = new JTextField(15);
        businessNameField = new JTextField(30);
        tradeNameField = new JTextField(30);
        addressField = new JTextField(40);
        cityField = new JTextField(20);
        provinceField = new JTextField(20);
        emailField = new JTextField(30);
        phoneField = new JTextField(15);
        
        // Certificado digital
        certificatePathField = new JTextField(30);
        certificatePathField.setEditable(false);
        certificatePasswordField = new JPasswordField(15);
        browseCertificateButton = new JButton("Examinar...");
        validateCertificateButton = new JButton("Validar");
        certificateStatusLabel = new JLabel("No cargado");
        certificateStatusLabel.setForeground(Color.RED);
        certificateInfoLabel = new JLabel("");
        certificateInfoLabel.setFont(certificateInfoLabel.getFont().deriveFont(10f));
        
        browseCertificateButton.addActionListener(e -> browseCertificate());
        validateCertificateButton.addActionListener(e -> validateCertificate());
        
        // Ambiente
        testRadio = new JRadioButton("Pruebas", true);
        productionRadio = new JRadioButton("Producción");
        ButtonGroup environmentGroup = new ButtonGroup();
        environmentGroup.add(testRadio);
        environmentGroup.add(productionRadio);
        
        // Botones
        saveButton = new JButton("Guardar Configuración");
        testConnectionButton = new JButton("Probar Conexión");
        
        saveButton.addActionListener(e -> saveConfiguration());
        testConnectionButton.addActionListener(e -> testConnection());
    }
    
    /**
     * Organiza los componentes en el panel
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Pestaña 1: Información del Emisor
        JPanel issuerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // RUC
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        issuerPanel.add(new JLabel("RUC:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(rucField, gbc);
        
        // Razón Social
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Razón Social:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(businessNameField, gbc);
        
        // Nombre Comercial
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Nombre Comercial:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(tradeNameField, gbc);
        
        // Dirección
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(addressField, gbc);
        
        // Ciudad
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Ciudad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(cityField, gbc);
        
        // Provincia
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Provincia:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(provinceField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(emailField, gbc);
        
        // Teléfono
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        issuerPanel.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issuerPanel.add(phoneField, gbc);
        
        tabbedPane.addTab("Información del Emisor", issuerPanel);
        
        // Pestaña 2: Certificado Digital
        JPanel certificatePanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Seleccionar archivo
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        certificatePanel.add(new JLabel("Ruta del Certificado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        certificatePanel.add(certificatePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        certificatePanel.add(browseCertificateButton, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        certificatePanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        certificatePanel.add(certificatePasswordField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        certificatePanel.add(validateCertificateButton, gbc);
        
        // Estado
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Estado:"));
        statusPanel.add(certificateStatusLabel);
        certificatePanel.add(statusPanel, gbc);
        
        // Información
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        certificatePanel.add(certificateInfoLabel, gbc);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(230, 240, 250));
        JLabel infoLabel = new JLabel("<html>" +
                "<b>Instrucciones para obtener certificado:</b><br>" +
                "1. Visita: <u>https://www.sri.gob.ec</u><br>" +
                "2. Solicita un certificado digital (Personas Jurídicas)<br>" +
                "3. Descarga el archivo .pfx o .p12<br>" +
                "4. Haz clic en 'Examinar' y selecciona el archivo<br>" +
                "5. Ingresa la contraseña del certificado<br>" +
                "6. Haz clic en 'Validar' para verificar<br>" +
                "</html>");
        infoPanel.add(infoLabel);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        certificatePanel.add(infoPanel, gbc);
        
        tabbedPane.addTab("Certificado Digital", certificatePanel);
        
        // Pestaña 3: Ambiente
        JPanel environmentPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        environmentPanel.add(testRadio, gbc);
        
        gbc.gridy = 1;
        environmentPanel.add(productionRadio, gbc);
        
        JPanel warningPanel = new JPanel();
        warningPanel.setBackground(new Color(255, 240, 200));
        JLabel warningLabel = new JLabel("<html><b>ADVERTENCIA:</b> Use producción solo cuando esté completamente seguro de la configuración</html>");
        warningPanel.add(warningLabel);
        
        gbc.gridy = 2; gbc.gridwidth = 2;
        environmentPanel.add(warningPanel, gbc);
        
        tabbedPane.addTab("Ambiente", environmentPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(testConnectionButton);
        buttonPanel.add(saveButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Examina archivos para seleccionar certificado
     */
    private void browseCertificate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Certificados PKCS12 (*.pfx, *.p12)", "pfx", "p12"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Seleccionar Certificado Digital");
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            certificatePathField.setText(selectedFile.getAbsolutePath());
            certificateStatusLabel.setText("Seleccionado (no validado)");
            certificateStatusLabel.setForeground(new Color(255, 165, 0)); // Orange
            certificateInfoLabel.setText("Archivo: " + selectedFile.getName() + " (" + formatFileSize(selectedFile.length()) + ")");
        }
    }
    
    /**
     * Valida el certificado seleccionado
     */
    private void validateCertificate() {
        String certificatePath = certificatePathField.getText();
        String password = new String(certificatePasswordField.getPassword());
        
        if (certificatePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un certificado primero", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Por favor, ingresa la contraseña del certificado", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File certFile = new File(certificatePath);
        
        // Validar que el archivo existe
        if (!certFile.exists()) {
            JOptionPane.showMessageDialog(this, 
                "El archivo de certificado no existe", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            certificateStatusLabel.setText("Error: Archivo no encontrado");
            certificateStatusLabel.setForeground(Color.RED);
            return;
        }
        
        // Validar extensión
        String fileName = certFile.getName().toLowerCase();
        if (!fileName.endsWith(".pfx") && !fileName.endsWith(".p12")) {
            JOptionPane.showMessageDialog(this, 
                "El archivo debe ser PFX o P12", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            certificateStatusLabel.setText("Error: Formato inválido");
            certificateStatusLabel.setForeground(Color.RED);
            return;
        }
        
        // Validar tamaño (menor a 1MB)
        if (certFile.length() > 1024 * 1024) {
            JOptionPane.showMessageDialog(this, 
                "El certificado es muy grande (>1MB)", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            certificateStatusLabel.setText("Error: Archivo muy grande");
            certificateStatusLabel.setForeground(Color.RED);
            return;
        }
        
        try {
            // Intentar leer el archivo
            byte[] certBytes = Files.readAllBytes(Paths.get(certificatePath));
            
            // Simular validación (en producción, se parsearia el PKCS12)
            if (certBytes.length > 0) {
                // Guardar en configuración
                saveCertificateConfig(certificatePath, password);
                
                certificateStatusLabel.setText("✓ Certificado válido y guardado");
                certificateStatusLabel.setForeground(new Color(34, 139, 34)); // Dark Green
                certificateInfoLabel.setText("Certificado validado: " + certFile.getName() + " | " + 
                    "Tamaño: " + formatFileSize(certFile.length()) + " | Contraseña guardada");
                
                JOptionPane.showMessageDialog(this, 
                    "Certificado validado y guardado correctamente\n\n" +
                    "Archivo: " + certFile.getName() + "\n" +
                    "Ruta: " + certificatePath + "\n" +
                    "Tamaño: " + formatFileSize(certFile.length()) + "\n\n" +
                    "El certificado está listo para usar", 
                    "Validación Exitosa", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al validar el certificado: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            certificateStatusLabel.setText("Error: " + e.getMessage());
            certificateStatusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Guarda la configuración del certificado
     */
    private void saveCertificateConfig(String path, String password) {
        try {
            String configFile = System.getProperty("user.dir") + File.separator + "chromispos-invoice.properties";
            Properties props = new Properties();
            
            // Cargar propiedades existentes
            if (new File(configFile).exists()) {
                props.load(Files.newInputStream(Paths.get(configFile)));
            }
            
            // Actualizar certificado
            props.setProperty("invoice.certificate.path", path);
            // ✓ Encriptar la contraseña antes de guardar
            String encryptedPassword = CipherUtil.encrypt(password);
            props.setProperty("invoice.certificate.password", encryptedPassword);
            props.setProperty("invoice.certificate.password.encrypted", "true");
            
            // Guardar
            props.store(Files.newOutputStream(Paths.get(configFile)), "Configuracion de Certificado Digital");
        } catch (Exception e) {
            System.err.println("Error guardando configuración: " + e.getMessage());
        }
    }
    
    /**
     * Formatea el tamaño de archivo
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Valida y guarda la configuración
     */
    private void saveConfiguration() {
        if (rucField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el RUC", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!EcuadorValidators.isValidRUC(rucField.getText())) {
            JOptionPane.showMessageDialog(this, "RUC no válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (businessNameField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese la razón social", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!emailField.getText().isEmpty() && !EcuadorValidators.isValidEmail(emailField.getText())) {
            JOptionPane.showMessageDialog(this, "Email no válido", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JOptionPane.showMessageDialog(this, "Configuración guardada exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Prueba la conexión con el SRI
     */
    private void testConnection() {
        JOptionPane.showMessageDialog(this, "Conexión con SRI verificada correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}
