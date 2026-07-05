package uk.chromis.pos.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Centralized Administration Panel
 *
 * Single entry point for back-office/admin screens: SRI config, invoicing,
 * users, roles and general system configuration. Operational screens used
 * during daily service (products, printer, payments, till closing, etc.)
 * keep their own direct menu entries and are not duplicated here.
 */
public class JPanelAdminCentral extends JPanel implements JPanelView {

    private static final Color ACCENT = new Color(41, 128, 185);
    private static final Color ACCENT_LIGHT = new Color(52, 152, 219);

    private final AppView appView;
    private final JTabbedPane tabs;

    public JPanelAdminCentral(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));

        add(createHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        stylePremiumTabs(tabs);
        initTabs();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(245, 245, 245));
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        wrapper.add(tabs, BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), 0, ACCENT_LIGHT));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 72));
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel(getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("SRI, usuarios, roles y configuración general del sistema");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(225, 238, 250));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new javax.swing.BoxLayout(textPanel, javax.swing.BoxLayout.Y_AXIS));
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private void stylePremiumTabs(JTabbedPane tabbedPane) {
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(new Color(60, 60, 60));
        tabbedPane.setUI(new BasicTabbedPaneUI() {
            {
                tabInsets = new Insets(10, 18, 10, 18);
                selectedTabPadInsets = new Insets(2, 2, 2, 2);
                tabAreaInsets = new Insets(6, 6, 0, 6);
                contentBorderInsets = new Insets(4, 4, 4, 4);
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                    int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
                // no dashed focus rectangle - flat, premium look
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y,
                    int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? Color.WHITE : new Color(236, 240, 244));
                g2.fillRoundRect(x, y, w, h + 8, 10, 10);
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y,
                    int w, int h, boolean isSelected) {
                if (isSelected) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(ACCENT);
                    g2.fillRect(x + 4, y + h - 3, w - 8, 3);
                    g2.dispose();
                }
            }

            @Override
            protected int getTabLabelShiftY(int tabPlacement, int tabIndex, boolean isSelected) {
                return 0;
            }
        });
    }

    private void initTabs() {
        // Each tab: {titleKey or label, iconName, panelClass}
        String[][] tabsDef = {
            {"Config SRI", "config.png", "uk.chromis.pos.invoice.forms.InvoiceConfigurationPanel"},
            {"Facturas SRI", "sales_print.png", "uk.chromis.pos.invoice.forms.InvoiceListPanel"},
            {"Usuarios", "user.png", "uk.chromis.pos.panels.JPanelUsers"},
            {"Roles", "user.png", "uk.chromis.pos.panels.JPanelRoles"},
            {"Configuracion", "config.png", "uk.chromis.pos.config.JPanelConfiguration"}
        };

        for (String[] tabDef : tabsDef) {
            addTab(tabDef[0], tabDef[1], tabDef[2]);
        }
    }

    private void addTab(String title, String iconName, String panelClass) {
        try {
            ImageIcon icon = null;
            try {
                icon = new ImageIcon(getClass().getResource("/uk/chromis/pos/images/" + iconName));
            } catch (Exception e1) {
                // icon stays null
            }
            JPanelView view = (JPanelView) Class.forName(panelClass)
                    .getConstructor(AppView.class)
                    .newInstance(appView);
            JComponent comp = view.getComponent();
            tabs.addTab(title, icon, comp);
        } catch (Exception e) {
            tabs.addTab(title, null, new JLabel("Error: " + e.getMessage()));
        }
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("menu.admin");
    }

    @Override
    public void activate() {
    }

    @Override
    public boolean deactivate() {
        return true;
    }
}
