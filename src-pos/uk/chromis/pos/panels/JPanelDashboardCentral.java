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
import java.awt.event.ActionEvent;
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

                cardsPanel.add(createQuickCard("Nueva Venta", "\uD83D\uDCB0", new Color(46, 204, 113), "uk.chromis.pos.sales.JPanelTicketSales"));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Cierre Caja", "\uD83D\uDCCB", new Color(52, 152, 219), "uk.chromis.pos.panels.JPanelCloseMoney"));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Admin Premium", "\uD83D\uDD27", new Color(155, 89, 182), "uk.chromis.pos.panels.JPanelAdminPremium"));
        cardsPanel.add(Box.createHorizontalStrut(10));
        cardsPanel.add(createQuickCard("Config SRI", "\u2699\uFE0F", new Color(230, 126, 34), "uk.chromis.pos.setup.JPanelConfigEcuador"));

        section.add(cardsPanel);
        return section;
    }

    // Helper: open a real functional screen via AppView
    private void showScreen(String taskClass) {
        if (appView != null && appView.getAppUserView() != null) {
            appView.getAppUserView().showTask(taskClass);
        }
    }

    private JPanel createQuickCard(String title, String icon, Color bgColor, String taskClass) {
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
            public void mouseClicked(MouseEvent e) {
                showScreen(taskClass);
            }
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

    // Overloaded for backward compatibility (no action)
    private JPanel createQuickCard(String title, String icon, Color bgColor) {
        return createQuickCard(title, icon, bgColor, null);
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

                addOperationItem(itemsPanel, "Nueva Venta", "Iniciar una nueva transacci\u00F3n de venta", "uk.chromis.pos.sales.JPanelTicketSales");
        addOperationItem(itemsPanel, "Devoluciones", "Procesar devoluciones de productos", "uk.chromis.pos.sales.JPanelTicketEdits");
        addOperationItem(itemsPanel, "Cierre de Caja", "Cerrar caja y generar reportes", "uk.chromis.pos.panels.JPanelCloseMoney");
        addOperationItem(itemsPanel, "Corte de Turno", "Finalizar turno del empleado", "uk.chromis.pos.epm.JPanelEmployeePresence");

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

                addSettingItem(itemsPanel, "Configuraci\u00F3n SRI", "Configuraci\u00F3n fiscal Ecuador", "uk.chromis.pos.setup.JPanelConfigEcuador");
        addSettingItem(itemsPanel, "Facturas SRI", "Gesti\u00F3n de facturas electr\u00F3nicas", "uk.chromis.pos.invoice.forms.InvoiceListPanel");
        addSettingItem(itemsPanel, "Impresoras", "Administrar conexiones de impresoras", "uk.chromis.pos.panels.JPanelPrinter");
        addSettingItem(itemsPanel, "Admin Premium", "Panel de administraci\u00F3n avanzado", "uk.chromis.pos.panels.JPanelAdminPremium");

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

                itemsPanel.add(createReportItem("Cierre de Caja", "Ventas y cierre del d\u00EDa", "uk.chromis.pos.panels.JPanelCloseMoney"));

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

                itemsPanel.add(createSettingItem("Respaldo de BD", "Crear copias de seguridad", "uk.chromis.pos.forms.BackupRestoreDialog"));
        itemsPanel.add(new JSeparator());
        itemsPanel.add(createSettingItem("Restaurar BD", "Restaurar datos desde backup", "uk.chromis.pos.forms.BackupRestoreDialog"));

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

                itemsPanel.add(createSettingItem("Configuraci\u00F3n SRI", "Datos del negocio y SRI", "uk.chromis.pos.setup.JPanelConfigEcuador"));
        itemsPanel.add(new JSeparator());
        itemsPanel.add(createSettingItem("Gesti\u00F3n Facturas SRI", "Facturas electr\u00F3nicas", "uk.chromis.pos.invoice.forms.InvoiceListPanel"));
        itemsPanel.add(new JSeparator());
        itemsPanel.add(createSettingItem("Pagos", "M\u00E9todos de pago", "uk.chromis.pos.panels.JPanelPayments"));

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

                itemsPanel.add(createSettingItem("Cambiar Contrase\u00F1a", "Cambiar contrase\u00F1a del usuario actual", "uk.chromis.pos.forms.JPrincipalApp"));

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

                itemsPanel.add(createSettingItem("Respaldo y Restauraci\u00F3n", "Copias de seguridad", "uk.chromis.pos.forms.BackupRestoreDialog"));

        section.add(itemsPanel);
        return section;
    }

        private void addOperationItem(JPanel itemsPanel, String title, String description, String taskClass) {
        if (itemsPanel.getComponentCount() > 0) {
            itemsPanel.add(new JSeparator());
        }
        itemsPanel.add(createMenuItem(title, description, "\u25B6", taskClass));
    }

    private void addSettingItem(JPanel itemsPanel, String title, String description, String taskClass) {
        if (itemsPanel.getComponentCount() > 0) {
            itemsPanel.add(new JSeparator());
        }
        itemsPanel.add(createMenuItem(title, description, "\u2699\uFE0F", taskClass));
    }

    private JPanel createOperationItem(String title, String description) {
        return createMenuItem(title, description, "\u25B6", null);
    }

    private JPanel createSettingItem(String title, String description) {
        return createMenuItem(title, description, "\u2699\uFE0F", null);
    }

    private JPanel createSettingItem(String title, String description, String taskClass) {
        return createMenuItem(title, description, "\u2699\uFE0F", taskClass);
    }

    private JPanel createReportItem(String title, String description, String taskClass) {
        return createMenuItem(title, description, "\uD83D\uDCCA", taskClass);
    }

    private JPanel createReportItem(String title, String description) {
        return createMenuItem(title, description, "\uD83D\uDCCA", null);
    }

    private JPanel createMenuItem(String title, String description, String actionSymbol, String taskClass) {
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

        JButton btn = new JButton(actionSymbol);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Wire button click to open functional screen
        if (taskClass != null) {
            btn.addActionListener((ActionEvent e) -> showScreen(taskClass));
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showScreen(taskClass);
                }
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
        } else {
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
        }

        item.add(btn, BorderLayout.EAST);
        item.setOpaque(true);

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
