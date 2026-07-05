package uk.chromis.pos.inventory;

import java.awt.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;
import uk.chromis.pos.inventory.dao.TaxDAO;
import uk.chromis.pos.inventory.ui.CrudUiUtils;

/**
 * Panel de Gestión de Impuestos - CRUD completo.
 */
public class TaxesPanel extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(TaxesPanel.class.getName());
    private final AppView appView;
    private final TaxDAO dao;

    private JTable table;
    private DefaultTableModel model;

    private static final String[] COLS = {"ID", "Nombre", "Tasa (%)", "Categoría"};

    public TaxesPanel(AppView appView) {
        this.appView = appView;
        this.dao = new TaxDAO(appView);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(140, 60, 20));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("💰 Gestión de Impuestos");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(250, 247, 242));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 210, 200)));
        JButton btnNuevo    = CrudUiUtils.makeBtn("➕ Nuevo",    new Color(46, 160, 67));
        JButton btnEditar   = CrudUiUtils.makeBtn("✏️ Editar",   new Color(41, 128, 185));
        JButton btnEliminar = CrudUiUtils.makeBtn("🗑️ Eliminar", new Color(203, 36, 49));
        JButton btnRefresh  = CrudUiUtils.makeBtn("🔄 Refrescar", new Color(100, 100, 110));
        toolbar.add(btnNuevo); toolbar.add(btnEditar);
        toolbar.add(btnEliminar); toolbar.add(btnRefresh);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(140, 60, 20));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        JPanel center = new JPanel(new BorderLayout());
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        btnNuevo.addActionListener(e -> showDialog(null));
        btnEditar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona un impuesto."); return; }
            showDialog(r);
        });
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());
    }

    @Override
    public void activate() throws BasicException { loadData(); }

    private void loadData() {
        model.setRowCount(0);
        try {
            for (Object[] row : dao.list()) {
                model.addRow(row);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error cargando impuestos", ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, name = "", rate = "0.00", catId = null;
        if (editRow != null) {
            int mr = table.convertRowIndexToModel(editRow);
            id   = (String) model.getValueAt(mr, 0);
            name = (String) model.getValueAt(mr, 1);
            rate = ((String) model.getValueAt(mr, 2)).replace("%", "").trim();
        }
        JTextField fName = new JTextField(name, 20);
        JTextField fRate = new JTextField(rate, 10);
        JComboBox<String[]> fCat = buildTaxCatCombo(catId);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nombre:")); form.add(fName);
        form.add(new JLabel("Tasa (ej: 12 para 12%):")); form.add(fRate);
        form.add(new JLabel("Categoría impuesto:")); form.add(fCat);

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nuevo Impuesto" : "Editar Impuesto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            double rateVal = Double.parseDouble(fRate.getText().trim()) / 100.0;
            if (editRow == null) {
                dao.insert(fName.getText().trim(), rateVal);
            } else {
                dao.update(id, fName.getText().trim(), rateVal);
            }
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private JComboBox<String[]> buildTaxCatCombo(String selectedId) {
        JComboBox<String[]> combo = CrudUiUtils.newIdLabelCombo();
        try {
            List<String[]> categories = dao.listTaxCategories();
            for (String[] item : categories) {
                combo.addItem(item);
                if (item[0].equals(selectedId)) combo.setSelectedItem(item);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error cargando categorías de impuesto para el combo", ex);
        }
        return combo;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un impuesto."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el impuesto \"" + name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(id);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar (en uso):\n" + ex.getMessage());
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "💰 Impuestos"; }
    @Override public boolean deactivate() { return true; }
}
