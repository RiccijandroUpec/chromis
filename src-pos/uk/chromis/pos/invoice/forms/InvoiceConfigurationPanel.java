package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.utils.EcuadorValidators;
import uk.chromis.pos.invoice.utils.CipherUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Panel de configuración de facturación electrónica
 */
public class InvoiceConfigurationPanel extends JPanel {
    
    private ElectronicInvoiceService invoiceService;
    private Properties properties;
    private static final String CONFIG_FILE = "chromisposconfig.properties";
    
    // Componentes UI - Emisor
    private JTextField rucField;
    private JTextField businessNameField;
    private JTextField tradeNameField;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField provinceField;
    private JTextField emailField;
    private JTextField phoneField;
    
    // Componentes UI - Certificado
    private JTextField certificatePathField;
    private JPasswordField certificatePasswordField;
    private JButton browseCertificateButton;
    private JButton validateCertificateButton;
    private JLabel certificateStatusLabel;
    private JLabel certificateInfoLabel;
    
    // Componentes UI - SMTP Correo
    private JTextField smtpHostField;
    private JTextField smtpPortField;
    private JTextField smtpUserField;
    private JPasswordField smtpPasswordField;
    private JCheckBox smtpSSLCheck;
    private JComboBox<UserComboItem> userSelector;
    
    // Componentes UI - Ambiente
    private JRadioButton testRadio;
    private JRadioButton productionRadio;
    
    private JButton saveButton;
    private JButton testConnectionButton;
    
    public InvoiceConfigurationPanel(ElectronicInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        this.properties = new Properties();
        initComponents();
        layoutComponents();
        loadConfiguration();
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
        
        // SMTP Correo
        userSelector = new JComboBox<UserComboItem>();
        userSelector.addItem(new UserComboItem("default", "Configuración Global (Por Defecto)"));
        try {
            uk.chromis.pos.datalogic.DataLogicSystem dlSystem = (uk.chromis.pos.datalogic.DataLogicSystem) uk.chromis.pos.forms.JRootApp.getRootInstance().getBean("uk.chromis.pos.datalogic.DataLogicSystem");
            if (dlSystem != null) {
                java.util.List<?> people = dlSystem.listPeopleVisible();
                if (people != null) {
                    for (Object p : people) {
                        if (p instanceof uk.chromis.pos.forms.AppUser) {
                            uk.chromis.pos.forms.AppUser user = (uk.chromis.pos.forms.AppUser) p;
                            userSelector.addItem(new UserComboItem(user.getId(), user.getName()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la lista de usuarios para la configuración de correo: " + e.getMessage());
        }
        userSelector.addActionListener(e -> loadConfigurationForSelectedUser());

        smtpHostField = new JTextField(25);
        smtpPortField = new JTextField(8);
        smtpUserField = new JTextField(25);
        smtpPasswordField = new JPasswordField(20);
        smtpSSLCheck = new JCheckBox("Usar SSL/TLS", true);
        
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
        issuerPanel.add(new JLabel("Email Envío Comprobantes:"), gbc);
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
                "1. Solicita un certificado de firma electrónica en formato .p12 o .pfx<br>" +
                "2. Haz clic en 'Examinar' y selecciona el archivo<br>" +
                "3. Ingresa la contraseña del certificado<br>" +
                "4. Haz clic en 'Validar' para verificar y registrar<br>" +
                "</html>");
        infoPanel.add(infoLabel);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        certificatePanel.add(infoPanel, gbc);
        
        tabbedPane.addTab("Certificado Digital", certificatePanel);
        
        // Pestaña 3: Configuración SMTP (Correo Electrónico)
        JPanel smtpPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Selector de Usuario
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        smtpPanel.add(new JLabel("Usuario a Configurar:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        smtpPanel.add(userSelector, gbc);
        
        // Host
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        smtpPanel.add(new JLabel("Servidor SMTP (Host):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        smtpPanel.add(smtpHostField, gbc);
        
        // Puerto
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        smtpPanel.add(new JLabel("Puerto SMTP:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        smtpPanel.add(smtpPortField, gbc);
        
        // Usuario
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        smtpPanel.add(new JLabel("Correo Emisor (Usuario):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        smtpPanel.add(smtpUserField, gbc);
        
        // Contraseña
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        smtpPanel.add(new JLabel("Contraseña Correo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        smtpPanel.add(smtpPasswordField, gbc);
        
        // SSL
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        smtpPanel.add(smtpSSLCheck, gbc);
        
        JPanel mailInfoPanel = new JPanel();
        mailInfoPanel.setBackground(new Color(240, 240, 240));
        JLabel mailInfoLabel = new JLabel("<html>" +
                "<b>Configuración Común:</b><br>" +
                "• <b>Configuración Global:</b> Define los valores SMTP base del sistema.<br>" +
                "• <b>Configuración de Cajero:</b> Sobrescribe el Correo/Contraseña de ese cajero. Deja en blanco Host/Puerto para heredar los valores globales.<br>" +
                "• <b>Gmail:</b> Host: <i>smtp.gmail.com</i> | Puerto: <i>465</i> (SSL) o <i>587</i> (TLS/STARTTLS)<br>" +
                "• Nota: Para Gmail requiere activar 'Contraseñas de aplicación'.<br>" +
                "• <b>Outlook/Office365:</b> Host: <i>smtp.office365.com</i> | Puerto: <i>587</i>" +
                "</html>");
        mailInfoPanel.add(mailInfoLabel);
        gbc.gridy = 6;
        smtpPanel.add(mailInfoPanel, gbc);
        
        tabbedPane.addTab("Servicio de Correo", smtpPanel);
        
        // Pestaña 4: Ambiente
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
        JLabel warningLabel = new JLabel("<html><b>ADVERTENCIA:</b> Use producción únicamente con firmas válidas registradas en el SRI.</html>");
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
     * Carga propiedades desde chromisposconfig.properties
     */
    private void loadConfiguration() {
        try {
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    properties.load(fis);
                }
                
                // Emisor
                rucField.setText(properties.getProperty("invoice.issuer.ruc", ""));
                businessNameField.setText(properties.getProperty("invoice.issuer.businessName", ""));
                tradeNameField.setText(properties.getProperty("invoice.issuer.tradeName", ""));
                addressField.setText(properties.getProperty("invoice.issuer.address", ""));
                cityField.setText(properties.getProperty("invoice.issuer.city", ""));
                provinceField.setText(properties.getProperty("invoice.issuer.province", ""));
                emailField.setText(properties.getProperty("invoice.issuer.email", ""));
                phoneField.setText(properties.getProperty("invoice.issuer.phone", ""));
                
                // Certificado
                certificatePathField.setText(properties.getProperty("invoice.certificate.path", ""));
                String encryptedPass = properties.getProperty("invoice.certificate.password", "");
                if (!encryptedPass.isEmpty()) {
                    try {
                        certificatePasswordField.setText(CipherUtil.decrypt(encryptedPass));
                        certificateStatusLabel.setText("✓ Cargado de configuración");
                        certificateStatusLabel.setForeground(new Color(34, 139, 34));
                    } catch (Exception e) {
                        certificatePasswordField.setText("");
                    }
                }
                
                // Correo SMTP (se carga el usuario seleccionado, que inicialmente es "default")
                loadConfigurationForSelectedUser();
                
                // Ambiente
                String env = properties.getProperty("invoice.environment", "1");
                if ("2".equals(env) || "production".equalsIgnoreCase(env)) {
                    productionRadio.setSelected(true);
                } else {
                    testRadio.setSelected(true);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar configuración: " + e.getMessage());
            e.printStackTrace();
        }
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
            JOptionPane.showMessageDialog(this, "Por favor, selecciona un certificado primero", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingresa la contraseña del certificado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        File certFile = new File(certificatePath);
        if (!certFile.exists()) {
            JOptionPane.showMessageDialog(this, "El archivo de certificado no existe", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Intentar cargarlo en el servicio para validar contraseña
            java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
            try (FileInputStream fis = new FileInputStream(certFile)) {
                ks.load(fis, password.toCharArray());
            }
            
            certificateStatusLabel.setText("✓ Certificado verificado correctamente");
            certificateStatusLabel.setForeground(new Color(34, 139, 34)); // Dark Green
            
            JOptionPane.showMessageDialog(this, 
                "Contraseña y archivo de certificado validados exitosamente.", 
                "Validación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al validar el certificado (Contraseña incorrecta o archivo dañado): " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            certificateStatusLabel.setText("Error al cargar certificado");
            certificateStatusLabel.setForeground(Color.RED);
        }
    }
    
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Guarda la configuración en chromisposconfig.properties
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
        
        try {
            // Cargar archivo original
            File f = new File(CONFIG_FILE);
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    properties.load(fis);
                }
            }
            
            // Habilitar el módulo
            properties.setProperty("invoice.enabled", "true");
            
            // Emisor
            properties.setProperty("invoice.issuer.ruc", rucField.getText());
            properties.setProperty("invoice.issuer.businessName", businessNameField.getText());
            properties.setProperty("invoice.issuer.tradeName", tradeNameField.getText());
            properties.setProperty("invoice.issuer.address", addressField.getText());
            properties.setProperty("invoice.issuer.city", cityField.getText());
            properties.setProperty("invoice.issuer.province", provinceField.getText());
            properties.setProperty("invoice.issuer.email", emailField.getText());
            properties.setProperty("invoice.issuer.phone", phoneField.getText());
            
            // Certificado
            if (!certificatePathField.getText().isEmpty()) {
                properties.setProperty("invoice.certificate.path", certificatePathField.getText());
                String password = new String(certificatePasswordField.getPassword());
                if (!password.isEmpty()) {
                    properties.setProperty("invoice.certificate.password", CipherUtil.encrypt(password));
                }
            }
            
            // Correo SMTP (para el usuario seleccionado)
            UserComboItem selected = (UserComboItem) userSelector.getSelectedItem();
            if (selected != null) {
                String hostKey, portKey, userKey, passKey, sslKey;
                if (selected.getId().equals("default")) {
                    hostKey = "invoice.mail.host";
                    portKey = "invoice.mail.port";
                    userKey = "invoice.mail.user";
                    passKey = "invoice.mail.password";
                    sslKey = "invoice.mail.ssl";
                } else {
                    hostKey = "invoice.mail.host." + selected.getId();
                    portKey = "invoice.mail.port." + selected.getId();
                    userKey = "invoice.mail.user." + selected.getId();
                    passKey = "invoice.mail.password." + selected.getId();
                    sslKey = "invoice.mail.ssl." + selected.getId();
                }
                
                properties.setProperty(hostKey, smtpHostField.getText());
                properties.setProperty(portKey, smtpPortField.getText());
                properties.setProperty(userKey, smtpUserField.getText());
                
                String mailPass = new String(smtpPasswordField.getPassword());
                if (!mailPass.isEmpty()) {
                    properties.setProperty(passKey, CipherUtil.encrypt(mailPass));
                } else {
                    properties.remove(passKey);
                }
                properties.setProperty(sslKey, String.valueOf(smtpSSLCheck.isSelected()));
            }
            
            // Ambiente
            properties.setProperty("invoice.environment", testRadio.isSelected() ? "1" : "2");
            
            // Guardar en archivo
            try (FileOutputStream fos = new FileOutputStream(f)) {
                properties.store(fos, "Configuracion Facturacion Electronica Ecuador - ChromisPOS");
            }
            
            // Reinicializar el servicio de facturación del POS
            String certPath = properties.getProperty("invoice.certificate.path", "");
            String encryptedCertPass = properties.getProperty("invoice.certificate.password", "");
            char[] certPass = encryptedCertPass.isEmpty()
                    ? new char[0]
                    : CipherUtil.decryptToCharArray(encryptedCertPass);
            boolean isProd = "2".equals(properties.getProperty("invoice.environment", "1"));
            try {
                invoiceService.initialize(certPath, certPass, isProd);
            } finally {
                java.util.Arrays.fill(certPass, '\0');
            }
            
            JOptionPane.showMessageDialog(this, "Configuración guardada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar configuración: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * Prueba la conexión de red (simulado/ping)
     */
    private void testConnection() {
        try {
            boolean isProd = productionRadio.isSelected();
            String host = isProd ? "cel.sri.gob.ec" : "celcer.sri.gob.ec";
            boolean reachable = java.net.InetAddress.getByName(host).isReachable(5000);
            if (reachable) {
                JOptionPane.showMessageDialog(this, "✓ Conexión exitosa con el servidor del SRI (" + host + ").", "Prueba Exitosa", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "✗ No se pudo establecer conexión con el SRI (" + host + ").", "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al probar conexión: " + e.getMessage(), "Error de Conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Carga la configuración del usuario seleccionado en los campos SMTP
     */
    private void loadConfigurationForSelectedUser() {
        UserComboItem selected = (UserComboItem) userSelector.getSelectedItem();
        if (selected == null) return;
        
        String hostKey, portKey, userKey, passKey, sslKey;
        if (selected.getId().equals("default")) {
            hostKey = "invoice.mail.host";
            portKey = "invoice.mail.port";
            userKey = "invoice.mail.user";
            passKey = "invoice.mail.password";
            sslKey = "invoice.mail.ssl";
        } else {
            hostKey = "invoice.mail.host." + selected.getId();
            portKey = "invoice.mail.port." + selected.getId();
            userKey = "invoice.mail.user." + selected.getId();
            passKey = "invoice.mail.password." + selected.getId();
            sslKey = "invoice.mail.ssl." + selected.getId();
        }
        
        smtpHostField.setText(properties.getProperty(hostKey, selected.getId().equals("default") ? "smtp.gmail.com" : ""));
        smtpPortField.setText(properties.getProperty(portKey, selected.getId().equals("default") ? "465" : ""));
        smtpUserField.setText(properties.getProperty(userKey, ""));
        
        String encryptedMailPass = properties.getProperty(passKey, "");
        if (!encryptedMailPass.isEmpty()) {
            try {
                smtpPasswordField.setText(CipherUtil.decrypt(encryptedMailPass));
            } catch (Exception e) {
                smtpPasswordField.setText("");
            }
        } else {
            smtpPasswordField.setText("");
        }
        
        String sslVal = properties.getProperty(sslKey, "");
        if (!sslVal.isEmpty()) {
            smtpSSLCheck.setSelected(Boolean.parseBoolean(sslVal));
        } else {
            smtpSSLCheck.setSelected(selected.getId().equals("default"));
        }
    }

    /**
     * Elemento para representar a un usuario en el JComboBox
     */
    private static class UserComboItem {
        private final String id;
        private final String name;
        
        public UserComboItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}
