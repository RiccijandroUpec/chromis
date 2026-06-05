/*
 * ChromisPOS - Premium Administration Panel
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
 * Premium Centralized Administration Panel
 * 
 * This panel provides a comprehensive, premium-looking interface for
 * centralized system administration and configuration.
 * All management functions are accessible from this single screen.
 */
public class JPanelAdminPremium extends JPanel implements JPanelView {

    private final AppView appView;
        private JPanel contentPanel;

        public JPanelAdminPremium(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        initUI();
    }

    private void showScreen(String taskClass) {
        if (appView != null && appView.getAppUserView() != null) {
            appView.getAppUserView().showTask(taskClass);
        }
    }

        private void initUI() {
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(245, 245, 245));

        // SECCION 1: VENTAS
        contentPanel.add(createSection("\uD83D\uDCB0 VENTAS", new String[][]{
            {"Nueva Venta", "sale.png", "Iniciar venta en POS", "uk.chromis.pos.sales.JPanelTicketSales"},
            {"Devoluciones", "saleedit.png", "Procesar devoluciones", "uk.chromis.pos.sales.JPanelTicketEdits"},
            {"Pagos", "payments.png", "M\u00E9todos de pago", "uk.chromis.pos.panels.JPanelPayments"},
            {"Cierre de Caja", "calculator.png", "Cerrar caja", "uk.chromis.pos.panels.JPanelCloseMoney"}
        }));

        // SECCION 2: FACTURACION SRI
        contentPanel.add(createSection("\uD83D\uDCCB FACTURACI\u00D3N SRI", new String[][]{
            {"Configuraci\u00F3n SRI", "config.png", "Configurar datos SRI", "uk.chromis.pos.setup.JPanelConfigEcuador"},
            {"Gesti\u00F3n Facturas", "sales_print.png", "Facturas electr\u00F3nicas", "uk.chromis.pos.invoice.forms.InvoiceListPanel"}
        }));

        // SECCION 3: INVENTARIO
        contentPanel.add(createSection("\uD83D\uDCE6 INVENTARIO", new String[][]{
            {"Productos", "products.png", "Gestión de productos", "uk.chromis.pos.inventory.ProductsPanel"},
            {"Categorías", "products.png", "Categorías de productos", "uk.chromis.pos.inventory.CategoriesPanel"},
            {"Clientes", "user.png", "Gestión de clientes", "uk.chromis.pos.customers.CustomersPanel"},
            {"Impuestos", "products.png", "Gestión de impuestos", "uk.chromis.pos.inventory.TaxesPanel"}
        }));

        // SECCION 4: ADMINISTRACION
        contentPanel.add(createSection("\uD83D\uDD27 ADMINISTRACI\u00D3N", new String[][]{
            {"Usuarios", "user.png", "Gestión de usuarios", "uk.chromis.pos.panels.JPanelUsers"},
            {"Roles", "user.png", "Permisos y roles", "uk.chromis.pos.panels.JPanelRoles"},
            {"Dashboard", "dashboard.png", "Panel central", "uk.chromis.pos.panels.JPanelDashboardCentral"},
            {"Admin Central", "config.png", "Admin con tabs", "uk.chromis.pos.panels.JPanelAdminCentral"},
            {"Empleados", "timer.png", "Control de presencia", "uk.chromis.pos.epm.JPanelEmployeePresence"},
            {"Impresoras", "printer.png", "Configurar impresoras", "uk.chromis.pos.panels.JPanelPrinter"}
        }));

        // SECCION 5: SISTEMA
        contentPanel.add(createSection("\u2699\uFE0F SISTEMA", new String[][]{
            {"Configuración General", "config.png", "Ajustes de Chromis", "uk.chromis.pos.config.JPanelConfiguration"},
            {"Respaldo y Restauración", "backup.png", "Copias de seguridad", "uk.chromis.pos.forms.BackupRestoreDialog"}
        }));

        contentPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new GradientPanel(new Color(41, 128, 185), new Color(52, 152, 219));
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🔧 Panel de Administración Centralizado");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Acceso centralizado a todas las configuraciones del sistema");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 240));

        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.setOpaque(false);
        labelPanel.add(titleLabel);
        labelPanel.add(subtitleLabel);

        header.add(labelPanel, BorderLayout.WEST);
        return header;
    }

        /**
     * Creates a section with items. Each item is String[]{label, iconName, description, taskClass}
     */
    private JPanel createSection(String title, String[][] items) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setBackground(new Color(245, 245, 245));
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sectionTitle = new JLabel("\u25A0 " + title);
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(41, 128, 185));
        sectionPanel.add(sectionTitle);

        JPanel itemsContainer = new JPanel();
        itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
        itemsContainer.setBackground(Color.WHITE);
        itemsContainer.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                JSeparator sep = new JSeparator();
                sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                itemsContainer.add(sep);
            }
            // items[i][3] = taskClass (or null)
            String taskClass = items[i].length > 3 ? items[i][3] : null;
            itemsContainer.add(createItemButton(items[i][0], items[i][1], items[i][2], taskClass));
        }

        sectionPanel.add(itemsContainer);
        sectionPanel.add(Box.createVerticalStrut(15));

        return sectionPanel;
    }

    private JPanel createItemButton(String label, String iconName, String description, String taskClass) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Icon (if available)
        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/uk/chromis/pos/images/" + iconName));
            iconLabel.setIcon(icon);
        } catch (Exception e) {
            iconLabel.setText("📋");
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        }
        itemPanel.add(iconLabel, BorderLayout.WEST);

        // Text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

                JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(41, 41, 41));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        descLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);
        textPanel.add(descLabel);
        itemPanel.add(textPanel, BorderLayout.CENTER);

        JButton actionBtn = new JButton("\u25B6");
        actionBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        actionBtn.setBackground(new Color(52, 152, 219));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        actionBtn.setFocusPainted(false);
        actionBtn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Wire button + panel click to open screen
        if (taskClass != null) {
            actionBtn.addActionListener((ActionEvent e) -> showScreen(taskClass));
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showScreen(taskClass);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    itemPanel.setBackground(new Color(240, 248, 255));
                    actionBtn.setBackground(new Color(41, 128, 185));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    itemPanel.setBackground(Color.WHITE);
                    actionBtn.setBackground(new Color(52, 152, 219));
                }
            });
        } else {
            itemPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    itemPanel.setBackground(new Color(240, 248, 255));
                    actionBtn.setBackground(new Color(41, 128, 185));
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    itemPanel.setBackground(Color.WHITE);
                    actionBtn.setBackground(new Color(52, 152, 219));
                }
            });
        }

        itemPanel.add(actionBtn, BorderLayout.EAST);
        itemPanel.setOpaque(true);

        return itemPanel;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return "🔧 Admin Premium";
    }

    @Override
    public void activate() {
        // Panel activation
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    /**
     * Custom gradient panel for premium look
     */
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

    /**
     * Separator line component
     */
    private static class JSeparator extends JPanel {
        JSeparator() {
            setBackground(new Color(230, 230, 230));
            setPreferredSize(new Dimension(0, 1));
        }
    }
}
