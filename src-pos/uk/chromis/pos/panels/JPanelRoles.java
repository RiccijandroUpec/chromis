package uk.chromis.pos.panels;

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
 * Panel de Gestión de Roles - CRUD de roles del sistema.
 */
public class JPanelRoles extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(JPanelRoles.class.getName());
    private final AppView appView;

    private JTable table;
    private DefaultTableModel model;

    private static final String[] COLS = {"ID", "Nombre"};

    public JPanelRoles(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 90, 90));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("🔒 Gestión de Roles");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(243, 249, 249));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 220)));
        JButton btnNuevo    = makeBtn("➕ Nuevo",    new Color(46, 160, 67));
        JButton btnEditar   = makeBtn("✏️ Editar",   new Color(41, 128, 185));
        JButton btnEliminar = makeBtn("🗑️ Eliminar", new Color(203, 36, 49));
        JButton btnRefresh  = makeBtn("🔄 Refrescar", new Color(100, 100, 110));
        toolbar.add(btnNuevo); toolbar.add(btnEditar);
        toolbar.add(btnEliminar); toolbar.add(btnRefresh);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(20, 90, 90));
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
            if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona un rol."); return; }
            showDialog(r);
        });
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());
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
    public void activate() throws BasicException { loadData(); }

    private void loadData() {
        model.setRowCount(0);
        try (Connection conn = appView.getSession().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM roles ORDER BY name")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString(1), rs.getString(2)});
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error cargando roles", ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, name = "";
        if (editRow != null) {
            int mr = table.convertRowIndexToModel(editRow);
            id   = (String) model.getValueAt(mr, 0);
            name = (String) model.getValueAt(mr, 1);
        }
        JTextField fName = new JTextField(name, 24);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nombre del rol:")); form.add(fName);

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nuevo Rol" : "Editar Rol",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try (Connection conn = appView.getSession().getConnection()) {
            if (editRow == null) {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO roles (id, name) VALUES (?,?)");
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, fName.getText().trim());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE roles SET name=? WHERE id=?");
                ps.setString(1, fName.getText().trim());
                ps.setString(2, id);
                ps.executeUpdate();
            }
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un rol."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el rol \"" + name + "\"? Los usuarios con este rol perderán el acceso.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM roles WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar (en uso):\n" + ex.getMessage());
        }
    }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "🔒 Roles"; }
    @Override public boolean deactivate() { return true; }
}
