/*
 * ChromisPOS - Premium Dashboard
 * 
 * This file is part of Chromis POS Version Chromis V1.5.4
 *
 * Copyright (c) 2015-2023 Chromis & previous Openbravo POS related works   
 *
 * https://www.chromis.co.uk
 *   
 * Chromis POS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Chromis POS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>
 */
package uk.chromis.pos.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Premium Centralized Dashboard
 * 
 * This dashboard provides quick access to all major system functions
 * with a modern, premium interface design.
 */
public class JPanelDashboardCentral extends JPanel implements JPanelView {

    private final AppView appView;
    private JPanel contentPanel;
    private JLabel dateTimeLabel;

    public JPanelDashboardCentral(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        initUI();
        startClockUpdate();
    }

    private void initUI() {
        // Header with date/time
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content with scroll
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 245));

        // Quick Access Cards
        contentPanel.add(createQuickAccessSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Common Operations Section
        contentPanel.add(createCommonOperationsSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Database Configuration Section
        contentPanel.add(createDatabaseConfigSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // System Settings Section
        contentPanel.add(createSystemSettingsSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Fiscal & Legal Section
        contentPanel.add(createFiscalLegalSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Integration Section
        contentPanel.add(createIntegrationSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Security Section
        contentPanel.add(createSecuritySection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Reports & Analytics Section
        contentPanel.add(createReportsSection());
        contentPanel.add(Box.createVerticalStrut(15));

        // Maintenance Section
        contentPanel.add(createMaintenanceSection());
        
        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new GradientPanel(new Color(52, 73, 94), new Color(44, 62, 80));
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 100));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📊 Dashboard Centralizado");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateTimeLabel.setForeground(new Color(200, 220, 240));
        updateDateTime();

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(dateTimeLabel);

        header.add(leftPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createQuickAccessSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("⚡ Acceso Rápido");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.X_AXIS));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createQuickCard("Productos", "📦", new Color(46, 204, 113)));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Usuarios", "👥", new Color(52, 152, 219)));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Reportes", "📈", new Color(155, 89, 182)));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Configuración", "⚙️", new Color(230, 126, 34)));

        section.add(cardsPanel);
        return section;
    }

    private JPanel createQuickCard(String title, String icon, Color bgColor) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(120, 100));
        card.setMaximumSize(new Dimension(120, 100));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            }
        });

        return card;
    }

    private JPanel createCommonOperationsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("🛠️ Operaciones Comunes");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] operations = {
            {"Nueva Venta", "Iniciar una nueva transacción de venta"},
            {"Devoluciones", "Procesar devoluciones de productos"},
            {"Cierre de Caja", "Cerrar caja y generar reportes"},
            {"Corte de Turno", "Finalizar turno del empleado"}
        };

        for (int i = 0; i < operations.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createOperationItem(operations[i][0], operations[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createSystemSettingsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("⚙️ Configuración del Sistema");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] settings = {
            {"Impuestos", "Configurar tasas de impuestos aplicables"},
            {"Impresoras", "Administrar conexiones de impresoras"},
            {"Recibos", "Personalizar formato de recibos"},
            {"Usuarios", "Gestionar usuarios del sistema"}
        };

        for (int i = 0; i < settings.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(settings[i][0], settings[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createReportsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("📊 Reportes & Análisis");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] reports = {
            {"Ventas Diarias", "Reporte de transacciones del día"},
            {"Inventario", "Estado actual del inventario"},
            {"Clientes", "Análisis de clientes y compras"},
            {"Financiero", "Resumen financiero y ganancias"}
        };

        for (int i = 0; i < reports.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createReportItem(reports[i][0], reports[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createDatabaseConfigSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("🗄️ CONFIGURACIÓN DE BASE DE DATOS");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] dbItems = {
            {"Conexión a Base de Datos", "Configurar servidor MySQL/PostgreSQL"},
            {"Credenciales", "Usuario, contraseña y base de datos"},
            {"Respaldo de BD", "Crear copias de seguridad automáticas"},
            {"Mantenimiento", "Optimización y limpiezade BD"},
            {"Recuperación", "Restaurar datos desde backup"}
        };

        for (int i = 0; i < dbItems.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(dbItems[i][0], dbItems[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createFiscalLegalSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("📋 FACTURACIÓN ELECTRÓNICA & LEGAL");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] fiscalItems = {
            {"Configuración Fiscal", "Datos del negocio y contribuyente"},
            {"Certificado Digital", "Certificados para SRI"},
            {"Facturación SRI", "Integración con sistema de rentas"},
            {"Plantillas de Facturas", "Diseño de recibos y facturas"},
            {"Comprobantes", "Configurar tipos de comprobantes"}
        };

        for (int i = 0; i < fiscalItems.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(fiscalItems[i][0], fiscalItems[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createIntegrationSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("🔄 INTEGRACIONES & SINCRONIZACIÓN");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] integrationItems = {
            {"Importar/Exportar", "Datos en formatos CSV, Excel, XML"},
            {"Sincronización", "Sincronizar con otros sistemas"},
            {"Cloud Storage", "Copias en la nube"},
            {"APIs", "Configuración de interfaces"},
            {"Webhooks", "Notificaciones automáticas"}
        };

        for (int i = 0; i < integrationItems.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(integrationItems[i][0], integrationItems[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createSecuritySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("🔐 SEGURIDAD & PERMISOS");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] securityItems = {
            {"Gestión de Usuarios", "Crear y administrar accesos"},
            {"Roles y Permisos", "Definir qué puede hacer cada usuario"},
            {"Auditoría", "Registro de todas las operaciones"},
            {"Contraseñas", "Política de seguridad de contraseñas"},
            {"Encriptación", "Proteger datos sensibles"}
        };

        for (int i = 0; i < securityItems.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(securityItems[i][0], securityItems[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createMaintenanceSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(new Color(245, 245, 245));
        section.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("🔧 MANTENIMIENTO & SOPORTE");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(52, 73, 94));
        section.add(sectionTitle);

        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(Color.WHITE);
        itemsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        String[][] maintenanceItems = {
            {"Limpieza de Datos", "Eliminar registros obsoletos"},
            {"Optimización", "Mejorar rendimiento del sistema"},
            {"Registros", "Ver logs de errores y eventos"},
            {"Actualización", "Descargar actualizaciones"},
            {"Información", "Versión y detalles del sistema"}
        };

        for (int i = 0; i < maintenanceItems.length; i++) {
            if (i > 0) itemsPanel.add(new JSeparator());
            itemsPanel.add(createSettingItem(maintenanceItems[i][0], maintenanceItems[i][1]));
        }

        section.add(itemsPanel);
        return section;
    }

    private JPanel createOperationItem(String title, String description) {
        return createMenuItem(title, description, "▶");
    }

    private JPanel createSettingItem(String title, String description) {
        return createMenuItem(title, description, "⚙️");
    }

    private JPanel createReportItem(String title, String description) {
        return createMenuItem(title, description, "📊");
    }

    private JPanel createMenuItem(String title, String description, String action) {
        JPanel item = new JPanel();
        item.setLayout(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 11));
        titleLabel.setForeground(new Color(52, 73, 94));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 9));
        descLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        item.add(textPanel, BorderLayout.CENTER);

        JButton btn = new JButton(action);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        item.add(btn, BorderLayout.EAST);

        item.setOpaque(true);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(240, 248, 255));
                btn.setBackground(new Color(41, 128, 185));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(Color.WHITE);
                btn.setBackground(new Color(52, 152, 219));
            }
        });

        return item;
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy - HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }

    private void startClockUpdate() {
        Thread clockThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    javax.swing.SwingUtilities.invokeLater(this::updateDateTime);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.start();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "📊 Dashboard";
    }

    @Override
    public void activate() {
        // Dashboard activation
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    private static class GradientPanel extends JPanel {
        private final Color color1;
        private final Color color2;

        GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
                    java.awt.RenderingHints.VALUE_RENDER_QUALITY);

            GradientPaint gradient = new GradientPaint(0, 0, color1,
                    0, getHeight(), color2);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static class JSeparator extends JPanel {
        JSeparator() {
            setBackground(new Color(230, 230, 230));
            setPreferredSize(new Dimension(0, 1));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        }
    }
}
