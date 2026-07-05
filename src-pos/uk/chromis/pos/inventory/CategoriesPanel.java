package uk.chromis.pos.inventory;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;
import uk.chromis.pos.inventory.dao.CategoryDAO;
import uk.chromis.pos.inventory.ui.CrudUiUtils;

/**
 * Panel de Gestión de Categorías - CRUD completo.
 */
public class CategoriesPanel extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(CategoriesPanel.class.getName());
    private final AppView appView;
    private final CategoryDAO dao;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private static final String[] COLS = {"ID", "Nombre", "Categoría Padre"};

    public CategoriesPanel(AppView appView) {
        this.appView = appView;
        this.dao = new CategoryDAO(appView);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 130, 100));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("🗂️ Gestión de Categorías");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(245, 247, 250));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)));
        searchField = new JTextField(18);
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadData(searchField.getText().trim()); }
        });
        JButton btnNuevo    = CrudUiUtils.makeBtn("➕ Nueva",    new Color(46, 160, 67));
        JButton btnEditar   = CrudUiUtils.makeBtn("✏️ Editar",   new Color(41, 128, 185));
        JButton btnEliminar = CrudUiUtils.makeBtn("🗑️ Eliminar", new Color(203, 36, 49));
        JButton btnRefresh  = CrudUiUtils.makeBtn("🔄 Refrescar", new Color(100, 100, 110));
        toolbar.add(new JLabel("Buscar: "));
        toolbar.add(searchField);
        toolbar.add(btnNuevo); toolbar.add(btnEditar);
        toolbar.add(btnEliminar); toolbar.add(btnRefresh);

        // Table
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(30, 130, 100));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 240, 220));
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
            if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona una categoría."); return; }
            showDialog(r);
        });
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData(""));
    }

    @Override
    public void activate() throws BasicException { loadData(""); }

    private void loadData(String filter) {
        model.setRowCount(0);
        try {
            for (Object[] row : dao.search(filter)) {
                model.addRow(row);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error cargando categorías", ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, name = "", parentId = null;
        if (editRow != null) {
            int mr = table.convertRowIndexToModel(editRow);
            id   = (String) model.getValueAt(mr, 0);
            name = (String) model.getValueAt(mr, 1);
        }
        JTextField fName = new JTextField(name, 22);
        JComboBox<String[]> fParent = buildParentCombo(parentId, id);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nombre:")); form.add(fName);
        form.add(new JLabel("Categoría padre:")); form.add(fParent);

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nueva Categoría" : "Editar Categoría",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String selParent = fParent.getSelectedItem() != null ? ((String[]) fParent.getSelectedItem())[0] : null;
        if (selParent != null && selParent.isEmpty()) selParent = null;
        try {
            if (editRow == null) {
                dao.insert(fName.getText().trim(), selParent);
            } else {
                dao.update(id, fName.getText().trim(), selParent);
            }
            loadData(searchField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private JComboBox<String[]> buildParentCombo(String selectedId, String excludeId) {
        JComboBox<String[]> combo = CrudUiUtils.newIdLabelCombo();
        combo.addItem(new String[]{"", "(Sin categoría padre)"});
        try {
            List<String[]> categories = dao.listParentCandidates(excludeId);
            for (String[] item : categories) {
                combo.addItem(item);
                if (item[0].equals(selectedId)) combo.setSelectedItem(item);
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Error cargando categorías padre para el combo", ex);
        }
        return combo;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona una categoría."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la categoría \"" + name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(id);
            loadData(searchField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar:\n" + ex.getMessage());
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "🗂️ Categorías"; }
    @Override public boolean deactivate() { return true; }
}
