package uk.chromis.pos.panels;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * CHROMIS ADMIN - APLICACIÓN STANDALONE
 * Una sola aplicación grande con todo centralizado
 * No necesita instalar múltiples módulos
 */
public class ChromisAdminApp extends JFrame {
    
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JLabel timeLabel;
    private Timer timer;
    
    public ChromisAdminApp() {
        setTitle("🔧 CHROMIS ADMIN - Sistema Centralizado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(true);
        setIconImage(createIcon());
        
        initUI();
        startClock();
        setVisible(true);
    }
    
    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        // HEADER
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // CONTENIDO PRINCIPAL CON TABS
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPane.addTab("📊 Dashboard", createDashboardPanel());
        tabbedPane.addTab("⚙️ Admin", createAdminPanel());
        tabbedPane.addTab("🗄️ Base de Datos", createDatabasePanel());
        tabbedPane.addTab("📋 Facturación", createBillingPanel());
        tabbedPane.addTab("🔐 Seguridad", createSecurityPanel());
        tabbedPane.addTab("📊 Reportes", createReportsPanel());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // FOOTER
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(new Color(52, 74, 96));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel("🔧 CHROMIS ADMIN - CENTRALIZADO");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(timeLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(panel, 
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Tarjetas de acceso rápido
        JPanel cardsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        cardsPanel.setOpaque(false);
        cardsPanel.add(createCard("📦 Productos", "Gestionar catálogo"));
        cardsPanel.add(createCard("👥 Usuarios", "Gestionar accesos"));
        cardsPanel.add(createCard("💰 Ventas", "Crear ventas"));
        cardsPanel.add(createCard("📊 Reportes", "Análisis"));
        cardsPanel.add(createCard("⚙️ Config", "Configuración"));
        cardsPanel.add(createCard("📋 Facturación", "Facturación SRI"));
        
        JLabel quickAccessLabel = new JLabel("⚡ ACCESO RÁPIDO");
        quickAccessLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        quickAccessLabel.setForeground(new Color(52, 74, 96));
        
        panel.add(quickAccessLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cardsPanel);
        panel.add(Box.createVerticalStrut(20));
        
        // Información del sistema
        JLabel systemLabel = new JLabel("📊 INFORMACIÓN DEL SISTEMA");
        systemLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        systemLabel.setForeground(new Color(52, 74, 96));
        panel.add(systemLabel);
        panel.add(Box.createVerticalStrut(10));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(240, 245, 250));
        infoPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(52, 74, 96), 1),
            new EmptyBorder(15, 15, 15, 15)));
        
        infoPanel.add(createInfoItem("Versión", "1.1.0"));
        infoPanel.add(createInfoItem("Base de Datos", "ChromisNewTest (Conectada)"));
        infoPanel.add(createInfoItem("Servidor", "Localhost:3306"));
        infoPanel.add(createInfoItem("Usuario", "root"));
        infoPanel.add(createInfoItem("Estado", "✅ En Línea"));
        
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JPanel createAdminPanel() {
        JPanel mainAdminPanel = new JPanel();
        mainAdminPanel.setLayout(new BoxLayout(mainAdminPanel, BoxLayout.Y_AXIS));
        mainAdminPanel.setBackground(Color.WHITE);
        mainAdminPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(mainAdminPanel,
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        String[] sections = {
            "📦 INVENTARIO",
            "👥 PERSONAL",
            "💰 IMPUESTOS",
            "⚙️ SISTEMA",
            "📊 REPORTES",
            "💾 INTEGRACIÓN",
            "📋 FACTURACIÓN",
            "🔧 MANTENIMIENTO",
            "🔐 SEGURIDAD"
        };
        
        String[][] items = {
            {"Productos", "Categorías", "Stock", "Proveedores", "Composición"},
            {"Usuarios", "Permisos", "Roles", "Presencia", "Salarios"},
            {"Impuestos", "Descuentos", "Precios", "Pagos", "Monedas"},
            {"General", "Base de Datos", "Servidor", "Recibos", "Impresoras", "Display", "Localización"},
            {"Ventas", "Inventario", "Clientes", "Ganancias", "Gráficos", "Auditoría"},
            {"Backup", "Restaurar", "Exportar", "Importar", "Sincronización", "Cloud"},
            {"Configurar", "SRI", "Certificados", "Plantillas", "Fiscal"},
            {"Maintenance", "Logs", "Performance", "Caché", "Updates"},
            {"Configuración", "Backups", "Encriptación", "Passwords", "Sessions"}
        };
        
        for (int i = 0; i < sections.length; i++) {
            JPanel sectionPanel = createAdminSection(sections[i], items[i]);
            mainAdminPanel.add(sectionPanel);
            mainAdminPanel.add(Box.createVerticalStrut(15));
        }
        
        mainAdminPanel.add(Box.createVerticalGlue());
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private JPanel createAdminSection(String title, String[] items) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(240, 245, 250));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(52, 74, 96), 1),
            new EmptyBorder(10, 10, 10, 10)));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(52, 74, 96));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        for (String item : items) {
            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setOpaque(false);
            
            JLabel itemLabel = new JLabel("  ▸ " + item);
            itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            JButton btn = new JButton("Configurar");
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            btn.setBackground(new Color(52, 150, 219));
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(5, 15, 5, 15));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            itemPanel.add(itemLabel, BorderLayout.WEST);
            itemPanel.add(btn, BorderLayout.EAST);
            
            panel.add(itemPanel);
            panel.add(Box.createVerticalStrut(3));
        }
        
        return panel;
    }
    
    private JPanel createDatabasePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🗄️ CONFIGURACIÓN DE BASE DE DATOS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 74, 96));
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        String[] fields = {
            "Servidor: ", "Localhost",
            "Puerto: ", "3306",
            "Base de Datos: ", "chromisnewtest",
            "Usuario: ", "root",
            "Contraseña: ", "••••••••"
        };
        
        for (int i = 0; i < fields.length; i += 2) {
            JPanel rowPanel = createFieldRow(fields[i], fields[i+1]);
            panel.add(rowPanel);
            panel.add(Box.createVerticalStrut(10));
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton testBtn = new JButton("🔗 Probar Conexión");
        testBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        testBtn.setBackground(new Color(46, 204, 113));
        testBtn.setForeground(Color.WHITE);
        
        JButton saveBtn = new JButton("💾 Guardar");
        saveBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveBtn.setBackground(new Color(52, 150, 219));
        saveBtn.setForeground(Color.WHITE);
        
        buttonPanel.add(testBtn);
        buttonPanel.add(saveBtn);
        
        panel.add(Box.createVerticalStrut(15));
        panel.add(buttonPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📋 FACTURACIÓN ELECTRÓNICA");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 74, 96));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        String[] configs = {
            "RUC de la Empresa",
            "Razón Social",
            "Número de Autorización SRI",
            "Certificado Digital",
            "Plantilla de Factura"
        };
        
        for (String config : configs) {
            JPanel configPanel = createConfigItem(config);
            panel.add(configPanel);
            panel.add(Box.createVerticalStrut(10));
        }
        
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("🔐 SEGURIDAD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 74, 96));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        String[] securityOptions = {
            "Gestión de Usuarios y Roles",
            "Política de Contraseñas",
            "Autenticación de Dos Factores",
            "Auditoría y Registros",
            "Encriptación de Datos",
            "Backups de Seguridad",
            "Control de Sesiones",
            "Permisos de Acceso"
        };
        
        for (String option : securityOptions) {
            JPanel optionPanel = createSecurityOption(option);
            panel.add(optionPanel);
            panel.add(Box.createVerticalStrut(8));
        }
        
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("📊 REPORTES Y ANÁLISIS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 74, 96));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        
        String[] reports = {
            "📈 Reporte de Ventas Diarias",
            "📦 Reporte de Inventario",
            "👥 Reporte de Clientes",
            "💹 Reporte de Ganancias",
            "🔍 Auditoría del Sistema",
            "📋 Reporte Fiscal"
        };
        
        for (String report : reports) {
            JPanel reportPanel = createReportItem(report);
            panel.add(reportPanel);
            panel.add(Box.createVerticalStrut(10));
        }
        
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    
    private JPanel createCard(String title, String desc) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(52, 150, 219));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(41, 128, 185), 1),
            new EmptyBorder(15, 15, 15, 15)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel descLabel = new JLabel(desc);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(new Color(220, 235, 250));
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(descLabel);
        
        return card;
    }
    
    private JPanel createInfoItem(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        JLabel labelL = new JLabel(label);
        labelL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelL.setForeground(new Color(52, 74, 96));
        
        JLabel valueL = new JLabel(value);
        valueL.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(labelL, BorderLayout.WEST);
        panel.add(valueL, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFieldRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        JLabel labelL = new JLabel(label);
        labelL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelL.setPreferredSize(new Dimension(150, 30));
        
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(labelL, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createConfigItem(String config) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(240, 245, 250));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(10, 10, 10, 10)));
        
        JLabel label = new JLabel("▸ " + config);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btn = new JButton("Configurar");
        btn.setBackground(new Color(52, 150, 219));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(btn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSecurityOption(String option) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(240, 250, 245));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(46, 204, 113), 1),
            new EmptyBorder(10, 10, 10, 10)));
        
        JLabel label = new JLabel("🔒 " + option);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btn = new JButton("Gestionar");
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(btn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createReportItem(String report) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(new Color(250, 245, 240));
        panel.setBorder(new CompoundBorder(
            new LineBorder(new Color(230, 126, 34), 1),
            new EmptyBorder(10, 10, 10, 10)));
        
        JLabel label = new JLabel(report);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JButton btn = new JButton("Ver Reporte");
        btn.setBackground(new Color(230, 126, 34));
        btn.setForeground(Color.WHITE);
        btn.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        panel.add(label, BorderLayout.WEST);
        panel.add(btn, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            new EmptyBorder(10, 20, 10, 20)));
        
        JLabel statusLabel = new JLabel("✅ Sistema Online - Conectado a BD");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(46, 204, 113));
        
        JLabel versionLabel = new JLabel("Versión 1.1.0 | © 2024 Chromis Admin");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        versionLabel.setForeground(new Color(150, 150, 150));
        
        panel.add(statusLabel, BorderLayout.WEST);
        panel.add(versionLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void startClock() {
        timer = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            String formatted = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            timeLabel.setText("⏰ " + formatted);
        });
        timer.start();
    }
    
    private Image createIcon() {
        int size = 32;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(new Color(52, 150, 219));
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g2d.drawString("⚙", 6, 24);
        g2d.dispose();
        return image;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChromisAdminApp());
    }
}
