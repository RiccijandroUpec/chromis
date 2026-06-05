package uk.chromis.pos.customers;

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
 * Panel de Gestión de Clientes - CRUD completo con búsqueda por nombre/RUC.
 */
public class CustomersPanel extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(CustomersPanel.class.getName());
    private final AppView appView;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private static final String[] COLS = {"ID", "Nombre", "RUC/CI", "Teléfono", "Email", "Ciudad"};

    public CustomersPanel(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 110, 160));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("🤝 Gestión de Clientes");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(243, 248, 252));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 215, 225)));
        searchField = new JTextField(20);
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadData(searchField.getText().trim()); }
        });
        JButton btnNuevo    = makeBtn("➕ Nuevo",    new Color(46, 160, 67));
        JButton btnEditar   = makeBtn("✏️ Editar",   new Color(41, 128, 185));
        JButton btnEliminar = makeBtn("🗑️ Eliminar", new Color(203, 36, 49));
        JButton btnRefresh  = makeBtn("🔄 Refrescar", new Color(100, 100, 110));
        toolbar.add(new JLabel("Buscar: ")); toolbar.add(searchField);
        toolbar.add(btnNuevo); toolbar.add(btnEditar);
        toolbar.add(btnEliminar); toolbar.add(btnRefresh);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(20, 110, 160));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(200, 230, 250));
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
            if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona un cliente."); return; }
            showDialog(r);
        });
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData(""));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void activate() throws BasicException { loadData(""); }

    private void loadData(String filter) {
        model.setRowCount(0);
        String sql = "SELECT id, name, taxid, phone, email, city "
                   + "FROM customers WHERE name LIKE ? OR taxid LIKE ? OR email LIKE ? "
                   + "ORDER BY name LIMIT 300";
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + filter + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2), rs.getString(3),
                    nvl(rs.getString(4)), nvl(rs.getString(5)), nvl(rs.getString(6))
                });
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error cargando clientes", ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, name = "", taxid = "", phone = "", email = "", city = "";
        if (editRow != null) {
            int mr = table.convertRowIndexToModel(editRow);
            id    = (String) model.getValueAt(mr, 0);
            name  = nvl((String) model.getValueAt(mr, 1));
            taxid = nvl((String) model.getValueAt(mr, 2));
            phone = nvl((String) model.getValueAt(mr, 3));
            email = nvl((String) model.getValueAt(mr, 4));
            city  = nvl((String) model.getValueAt(mr, 5));
        }
        JTextField fName  = new JTextField(name,  22);
        JTextField fTaxid = new JTextField(taxid, 15);
        JTextField fPhone = new JTextField(phone, 15);
        JTextField fEmail = new JTextField(email, 22);
        JTextField fCity  = new JTextField(city,  18);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nombre / Razón Social:")); form.add(fName);
        form.add(new JLabel("RUC / Cédula:"));          form.add(fTaxid);
        form.add(new JLabel("Teléfono:"));               form.add(fPhone);
        form.add(new JLabel("Email:"));                  form.add(fEmail);
        form.add(new JLabel("Ciudad:"));                 form.add(fCity);

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nuevo Cliente" : "Editar Cliente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try (Connection conn = appView.getSession().getConnection()) {
            if (editRow == null) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO customers (id, name, taxid, phone, email, city) VALUES (?,?,?,?,?,?)");
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, fName.getText().trim());
                ps.setString(3, fTaxid.getText().trim());
                ps.setString(4, fPhone.getText().trim());
                ps.setString(5, fEmail.getText().trim());
                ps.setString(6, fCity.getText().trim());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE customers SET name=?, taxid=?, phone=?, email=?, city=? WHERE id=?");
                ps.setString(1, fName.getText().trim());
                ps.setString(2, fTaxid.getText().trim());
                ps.setString(3, fPhone.getText().trim());
                ps.setString(4, fEmail.getText().trim());
                ps.setString(5, fCity.getText().trim());
                ps.setString(6, id);
                ps.executeUpdate();
            }
            loadData(searchField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un cliente."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el cliente \"" + name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM customers WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            loadData(searchField.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar:\n" + ex.getMessage());
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "🤝 Clientes"; }
    @Override public boolean deactivate() { return true; }
}
