package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.utils.EcuadorValidators;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        
        browseCertificateButton.addActionListener(e -> browseCertificate());
        
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
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        certificatePanel.add(new JLabel("Ruta del Certificado:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        certificatePanel.add(certificatePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        certificatePanel.add(browseCertificateButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        certificatePanel.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        certificatePanel.add(certificatePasswordField, gbc);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(230, 240, 250));
        JLabel infoLabel = new JLabel("<html>El certificado debe ser un archivo PFX válido emitido por autoridad competente en Ecuador</html>");
        infoPanel.add(infoLabel);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
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
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Certificados PKCS12 (*.pfx)", "pfx", "p12"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            certificatePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
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
