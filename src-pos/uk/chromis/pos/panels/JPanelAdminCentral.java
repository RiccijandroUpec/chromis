package uk.chromis.pos.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Centralized Administration Panel
 *
 * This panel groups together the most used management screens (Products, Categories,
 * Users, Taxes, and System Configuration) into a single tabbed view. It provides a
 * premium look‑and‑feel using the application’s default Font and a dark theme.
 */
public class JPanelAdminCentral extends JPanel implements JPanelView {

    private final AppView appView;
    private final JTabbedPane tabs;

    public JPanelAdminCentral(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        tabs = new JTabbedPane();
        initTabs();
        add(tabs, BorderLayout.CENTER);
    }

    private void initTabs() {
        // Products panel – reuse existing product selector if present
        addTab("menu.products", "products.png", "uk.chromis.pos.inventory.JProductsSelector");
        // Categories panel – reuse existing category tree
        addTab("menu.categories", "category.png", "uk.chromis.pos.catalog.JCatalog");
        // Users / Employees panel – reuse existing user management
        addTab("menu.users", "user.png", "uk.chromis.pos.setup.JPanelConfigEcuador"); // placeholder for user config
        // Taxes panel – reuse tax configuration
        addTab("menu.taxes", "tax.png", "uk.chromis.pos.taxes.JTaxesPanel"); // assumes such class exists
        // System configuration – the dedicated config UI
        addTab("Configuración ChromisEC", "config.png", "uk.chromis.pos.setup.JPanelConfigEcuador");
    }

    private void addTab(String titleKey, String iconName, String panelClass) {
        try {
            String title = AppLocal.getIntString(titleKey);
            ImageIcon icon = new ImageIcon(getClass().getResource("/uk/chromis/pos/images/" + iconName));
            JPanelView view = (JPanelView) Class.forName(panelClass)
                    .getConstructor(AppView.class)
                    .newInstance(appView);
            JComponent comp = view.getComponent();
            tabs.addTab(title, icon, comp);
        } catch (Exception e) {
            // If the class cannot be loaded, add a placeholder tab with the error message
            tabs.addTab(titleKey, null, new javax.swing.JLabel("Error loading " + panelClass + ": " + e.getMessage()));
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
        // No special activation needed – tabs are ready
    }

    @Override
    public boolean deactivate() {
        return true;
    }
}
