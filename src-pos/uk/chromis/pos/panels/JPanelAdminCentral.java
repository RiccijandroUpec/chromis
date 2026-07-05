package uk.chromis.pos.panels;

import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Centralized Administration Panel
 *
 * Tabbed view grouping real functional screens: SRI Config, Invoice Mgmt,
 * Admin Premium, Dashboard, Printer, Employee Presence, Payments.
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
        // Each tab: {titleKey or label, iconName, panelClass}
        String[][] tabsDef = {
            {"Config SRI", "config.png", "uk.chromis.pos.invoice.forms.InvoiceConfigurationPanel"},
            {"Facturas SRI", "sales_print.png", "uk.chromis.pos.invoice.forms.InvoiceListPanel"},
            {"Admin Premium", "dashboard.png", "uk.chromis.pos.panels.JPanelAdminPremium"},
            {"Dashboard", "dashboard.png", "uk.chromis.pos.panels.JPanelDashboardCentral"},
            {"Productos", "products.png", "uk.chromis.pos.inventory.ProductsPanel"},
            {"Categorias", "products.png", "uk.chromis.pos.inventory.CategoriesPanel"},
            {"Impuestos", "products.png", "uk.chromis.pos.inventory.TaxesPanel"},
            {"Usuarios", "user.png", "uk.chromis.pos.panels.JPanelUsers"},
            {"Configuracion", "config.png", "uk.chromis.pos.config.JPanelConfiguration"},
            {"Impresoras", "printer.png", "uk.chromis.pos.panels.JPanelPrinter"},
            {"Empleados", "timer.png", "uk.chromis.pos.epm.JPanelEmployeePresence"},
            {"Pagos", "payments.png", "uk.chromis.pos.panels.JPanelPayments"},
            {"Cierre Caja", "calculator.png", "uk.chromis.pos.panels.JPanelCloseMoney"}
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
