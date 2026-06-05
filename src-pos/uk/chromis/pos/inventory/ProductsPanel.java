package uk.chromis.pos.inventory;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.table.*;
import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Panel de Gestión de Productos - CRUD completo con búsqueda en tiempo real.
 */
public class ProductsPanel extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(ProductsPanel.class.getName());
    private final AppView appView;

    // UI
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JButton btnNuevo, btnEditar, btnEliminar, btnRefrescar;

    // Columnas del modelo
    private static final String[] COLS = {"ID", "Código", "Nombre", "Precio", "Categoría", "Activo"};

    public ProductsPanel(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout(0, 0));
        buildUI();
    }

    private void buildUI() {
        // ── Header ───────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 80, 160));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("📦 Gestión de Productos");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Toolbar ───────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(245, 247, 250));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210, 215, 220)));

        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Buscar producto...");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadData(searchField.getText().trim()); }
        });

        btnNuevo    = makeBtn("➕ Nuevo",    new Color(46, 160, 67));
        btnEditar   = makeBtn("✏️ Editar",   new Color(41, 128, 185));
        btnEliminar = makeBtn("🗑️ Eliminar", new Color(203, 36, 49));
        btnRefrescar = makeBtn("🔄 Refrescar", new Color(100, 100, 110));

        toolbar.add(new JLabel("Buscar: "));
        toolbar.add(searchField);
        toolbar.add(btnNuevo);
        toolbar.add(btnEditar);
        toolbar.add(btnEliminar);
        toolbar.add(btnRefrescar);
        add(toolbar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(210, 230, 255));
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);   // ocultar ID

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));

        // Layout: header + toolbar + table
        JPanel center = new JPanel(new BorderLayout());
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        // ── Actions ───────────────────────────────────────────────
        btnNuevo.addActionListener(e -> showDialog(null));
        btnEditar.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
            showDialog(row);
        });
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefrescar.addActionListener(e -> loadData(""));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void activate() throws BasicException {
        loadData("");
    }

    private void loadData(String filter) {
        model.setRowCount(0);
        String sql = "SELECT p.id, p.reference, p.name, p.pricesell, c.name, p.iscom "
                   + "FROM products p LEFT JOIN categories c ON p.category = c.id "
                   + "WHERE p.name LIKE ? OR p.reference LIKE ? "
                   + "ORDER BY p.name LIMIT 500";
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + filter + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    String.format("$ %.2f", rs.getDouble(4)),
                    rs.getString(5),
                    rs.getBoolean(6) ? "✔" : ""
                });
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error cargando productos", ex);
            JOptionPane.showMessageDialog(this, "Error al cargar productos:\n" + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, code = "", name = "", price = "0.00", catId = null;
        if (editRow != null) {
            int modelRow = table.convertRowIndexToModel(editRow);
            id    = (String) model.getValueAt(modelRow, 0);
            code  = (String) model.getValueAt(modelRow, 1);
            name  = (String) model.getValueAt(modelRow, 2);
            price = ((String) model.getValueAt(modelRow, 3)).replace("$", "").trim();
        }

        JTextField fCode  = new JTextField(code, 20);
        JTextField fName  = new JTextField(name, 20);
        JTextField fPrice = new JTextField(price, 10);
        JComboBox<String[]> fCat = buildCategoryCombo(catId);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Código:")); form.add(fCode);
        form.add(new JLabel("Nombre:")); form.add(fName);
        form.add(new JLabel("Precio venta:")); form.add(fPrice);
        form.add(new JLabel("Categoría:")); form.add(fCat);

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nuevo Producto" : "Editar Producto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String catIdSel = fCat.getSelectedItem() != null ? ((String[]) fCat.getSelectedItem())[0] : null;
        try (Connection conn = appView.getSession().getConnection()) {
            if (editRow == null) {
                String newId = UUID.randomUUID().toString();
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO products (id, reference, name, pricesell, category, iscom) VALUES (?,?,?,?,?,0)");
                ps.setString(1, newId);
                ps.setString(2, fCode.getText().trim());
                ps.setString(3, fName.getText().trim());
                ps.setDouble(4, Double.parseDouble(fPrice.getText().trim()));
                ps.setString(5, catIdSel);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE products SET reference=?, name=?, pricesell=?, category=? WHERE id=?");
                ps.setString(1, fCode.getText().trim());
                ps.setString(2, fName.getText().trim());
                ps.setDouble(3, Double.parseDouble(fPrice.getText().trim()));
                ps.setString(4, catIdSel);
                ps.setString(5, id);
                ps.executeUpdate();
            }
            loadData(searchField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String[]> buildCategoryCombo(String selectedId) {
        JComboBox<String[]> combo = new JComboBox<>();
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof String[]) setText(((String[]) v)[1]);
                return this;
            }
        });
        try (Connection conn = appView.getSession().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) {
                String[] item = {rs.getString(1), rs.getString(2)};
                combo.addItem(item);
                if (rs.getString(1).equals(selectedId)) combo.setSelectedItem(item);
            }
        } catch (SQLException ex) { /* ignore */ }
        return combo;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un producto."); return; }
        int modelRow = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(modelRow, 0);
        String name = (String) model.getValueAt(modelRow, 2);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el producto \"" + name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            loadData(searchField.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al eliminar:\n" + ex.getMessage());
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "📦 Productos"; }
    @Override public boolean deactivate() { return true; }
}
