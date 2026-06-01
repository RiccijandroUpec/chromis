package uk.chromis.pos.setup;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.BeanFactoryApp;
import uk.chromis.pos.forms.BeanFactoryException;
import uk.chromis.pos.forms.JPanelView;

public class JPanelConfigEcuador extends JPanel implements JPanelView, BeanFactoryApp {

    private AppView m_App;
    private Properties props;
    private final String PROPS_FILE = "chromisposconfig.properties";

    // Campos DB
    private JTextField txtDbServer, txtDbPort, txtDbName, txtDbUser;
    private JPasswordField txtDbPass;

    // Campos SRI
    private JTextField txtRuc, txtRazonSocial, txtNombreComercial, txtCertPath;
    private JPasswordField txtCertPass;
    
    // Config Impuestos y Control
    private JTextField txtTaxMappings;
    private JCheckBox chkEnableInvoice;
    private JComboBox<String> cmbEnvironment;

    // Config Correo
    private JTextField txtMailServer, txtMailPort, txtMailUser;
    private JPasswordField txtMailPass;

    public JPanelConfigEcuador() {
        initComponents();
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
    }

    @Override
    public String getTitle() {
        return "Configuración SRI Ecuador";
    }

    @Override
    public void activate() throws BasicException {
        loadConfiguration();
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(43, 45, 48));
        JLabel title = new JLabel("Configuración Integrada - ChromisEC");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        header.add(title);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Facturación Electrónica (SRI)", createSRIPanel());
        tabbedPane.addTab("Base de Datos", createDatabasePanel());
        tabbedPane.addTab("Correo Electrónico (SMTP)", createEmailPanel());
        tabbedPane.addTab("Impuestos y Avanzado", createAdvancedPanel());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Guardar Configuraciones");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveConfiguration());
        footer.add(btnSave);

        add(header, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel createSRIPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Habilitar Facturación SRI:"), gbc);
        chkEnableInvoice = new JCheckBox("Activar");
        gbc.gridx = 1; p.add(chkEnableInvoice, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Ambiente SRI:"), gbc);
        cmbEnvironment = new JComboBox<>(new String[]{"1 - Pruebas", "2 - Producción"});
        gbc.gridx = 1; p.add(cmbEnvironment, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("RUC (13 dígitos):"), gbc);
        txtRuc = new JTextField(20);
        gbc.gridx = 1; p.add(txtRuc, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Razón Social:"), gbc);
        txtRazonSocial = new JTextField(20);
        gbc.gridx = 1; p.add(txtRazonSocial, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Nombre Comercial:"), gbc);
        txtNombreComercial = new JTextField(20);
        gbc.gridx = 1; p.add(txtNombreComercial, gbc);
        
        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Ruta Certificado (.p12/.pfx):"), gbc);
        txtCertPath = new JTextField(20);
        gbc.gridx = 1; p.add(txtCertPath, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Clave Certificado:"), gbc);
        txtCertPass = new JPasswordField(20);
        gbc.gridx = 1; p.add(txtCertPass, gbc);

        // Dummy filler
        gbc.gridx = 0; gbc.gridy++; gbc.weighty = 1.0; p.add(new JLabel(""), gbc);
        return p;
    }

    private JPanel createDatabasePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Servidor MySQL:"), gbc);
        txtDbServer = new JTextField(20);
        gbc.gridx = 1; p.add(txtDbServer, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Puerto:"), gbc);
        txtDbPort = new JTextField(20);
        gbc.gridx = 1; p.add(txtDbPort, gbc);
        
        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Base de Datos:"), gbc);
        txtDbName = new JTextField(20);
        gbc.gridx = 1; p.add(txtDbName, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Usuario:"), gbc);
        txtDbUser = new JTextField(20);
        gbc.gridx = 1; p.add(txtDbUser, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Contraseña:"), gbc);
        txtDbPass = new JPasswordField(20);
        gbc.gridx = 1; p.add(txtDbPass, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weighty = 1.0; p.add(new JLabel(""), gbc);
        return p;
    }

    private JPanel createEmailPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Servidor SMTP (Ej. smtp.gmail.com):"), gbc);
        txtMailServer = new JTextField(20);
        gbc.gridx = 1; p.add(txtMailServer, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Puerto (Ej. 587):"), gbc);
        txtMailPort = new JTextField(20);
        gbc.gridx = 1; p.add(txtMailPort, gbc);
        
        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Correo Remitente:"), gbc);
        txtMailUser = new JTextField(20);
        gbc.gridx = 1; p.add(txtMailUser, gbc);

        gbc.gridx = 0; gbc.gridy++; p.add(new JLabel("Contraseña / App Password:"), gbc);
        txtMailPass = new JPasswordField(20);
        gbc.gridx = 1; p.add(txtMailPass, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weighty = 1.0; p.add(new JLabel(""), gbc);
        return p;
    }
    
    private JPanel createAdvancedPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; p.add(new JLabel("Mapeo Dinámico SRI (Tasa:Código):"), gbc);
        txtTaxMappings = new JTextField(30);
        gbc.gridx = 1; p.add(txtTaxMappings, gbc);
        
        JLabel hint = new JLabel("Ej: 0:0, 5:5, 12:2, 14:3, 15:4");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        gbc.gridy++; gbc.gridx = 1; p.add(hint, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.weighty = 1.0; p.add(new JLabel(""), gbc);
        return p;
    }

    private void loadConfiguration() {
        props = new Properties();
        try {
            File f = new File(PROPS_FILE);
            if (f.exists()) {
                props.load(new FileInputStream(f));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        txtDbServer.setText(props.getProperty("database.server", "localhost"));
        txtDbPort.setText(props.getProperty("database.port", "3306"));
        txtDbName.setText(props.getProperty("database.name", "chromispos"));
        txtDbUser.setText(props.getProperty("database.user", "root"));
        txtDbPass.setText(props.getProperty("database.password", ""));
        
        txtRuc.setText(props.getProperty("invoice.issuer.ruc", ""));
        txtRazonSocial.setText(props.getProperty("invoice.issuer.businessName", ""));
        txtNombreComercial.setText(props.getProperty("invoice.issuer.tradeName", ""));
        txtCertPath.setText(props.getProperty("invoice.certificate.path", ""));
        txtCertPass.setText(props.getProperty("invoice.certificate.password", ""));
        
        chkEnableInvoice.setSelected(Boolean.parseBoolean(props.getProperty("invoice.enabled", "false")));
        String env = props.getProperty("invoice.environment", "1");
        if (env.equals("2")) { cmbEnvironment.setSelectedIndex(1); } else { cmbEnvironment.setSelectedIndex(0); }

        txtMailServer.setText(props.getProperty("invoice.mail.smtp.host", ""));
        txtMailPort.setText(props.getProperty("invoice.mail.smtp.port", "587"));
        txtMailUser.setText(props.getProperty("invoice.mail.user", ""));
        txtMailPass.setText(props.getProperty("invoice.mail.password", ""));

        txtTaxMappings.setText(props.getProperty("invoice.tax.mappings", "0:0,5:5,12:2,13:10,14:3,15:4"));
    }

    private void saveConfiguration() {
        props.setProperty("database.server", txtDbServer.getText());
        props.setProperty("database.port", txtDbPort.getText());
        props.setProperty("database.name", txtDbName.getText());
        props.setProperty("database.user", txtDbUser.getText());
        props.setProperty("database.password", new String(txtDbPass.getPassword()));
        
        props.setProperty("invoice.issuer.ruc", txtRuc.getText());
        props.setProperty("invoice.issuer.businessName", txtRazonSocial.getText());
        props.setProperty("invoice.issuer.tradeName", txtNombreComercial.getText());
        props.setProperty("invoice.certificate.path", txtCertPath.getText());
        props.setProperty("invoice.certificate.password", new String(txtCertPass.getPassword()));
        
        props.setProperty("invoice.enabled", String.valueOf(chkEnableInvoice.isSelected()));
        props.setProperty("invoice.environment", cmbEnvironment.getSelectedIndex() == 0 ? "1" : "2");

        props.setProperty("invoice.mail.smtp.host", txtMailServer.getText());
        props.setProperty("invoice.mail.smtp.port", txtMailPort.getText());
        props.setProperty("invoice.mail.user", txtMailUser.getText());
        props.setProperty("invoice.mail.password", new String(txtMailPass.getPassword()));
        
        props.setProperty("invoice.tax.mappings", txtTaxMappings.getText());

        props.setProperty("USERCOUNTRY", "EC");
        props.setProperty("USERLANGUAGE", "es");
        props.setProperty("CURRENCY", "USD");

        try {
            props.store(new FileOutputStream(PROPS_FILE), "ChromisPOS Ecuador Configuration");
            JOptionPane.showMessageDialog(this, "¡Configuración de ChromisEC guardada con éxito!\nPor favor, reinicie la aplicación si hizo cambios en la base de datos.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
