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
import uk.chromis.pos.util.Hashcypher;

/**
 * Panel de Gestión de Usuarios - CRUD completo con cambio de contraseña.
 */
public class JPanelUsers extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(JPanelUsers.class.getName());
    private final AppView appView;

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;

    private static final String[] COLS = {"ID", "Nombre", "Rol", "Email", "Activo"};

    public JPanelUsers(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(80, 30, 140));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("👤 Gestión de Usuarios");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(new Color(248, 245, 252));
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(215, 205, 225)));
        searchField = new JTextField(18);
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadData(searchField.getText().trim()); }
        });
        JButton btnNuevo    = makeBtn("➕ Nuevo",      new Color(46, 160, 67));
        JButton btnEditar   = makeBtn("✏️ Editar",     new Color(41, 128, 185));
        JButton btnPass     = makeBtn("🔑 Contraseña", new Color(200, 130, 0));
        JButton btnEliminar = makeBtn("🗑️ Eliminar",  new Color(203, 36, 49));
        JButton btnRefresh  = makeBtn("🔄 Refrescar",  new Color(100, 100, 110));

        toolbar.add(new JLabel("Buscar: ")); toolbar.add(searchField);
        toolbar.add(btnNuevo); toolbar.add(btnEditar);
        toolbar.add(btnPass);  toolbar.add(btnEliminar); toolbar.add(btnRefresh);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(80, 30, 140));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(230, 215, 255));
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
            if (r < 0) { JOptionPane.showMessageDialog(this, "Selecciona un usuario."); return; }
            showDialog(r);
        });
        btnPass.addActionListener(e -> changePassword());
        btnEliminar.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData(""));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    @Override
    public void activate() throws BasicException { loadData(""); }

    private void loadData(String filter) {
        model.setRowCount(0);
        String sql = "SELECT p.id, p.name, r.name, p.appuser, p.visible "
                   + "FROM people p LEFT JOIN roles r ON p.role = r.id "
                   + "WHERE p.name LIKE ? ORDER BY p.name LIMIT 200";
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + filter + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString(1), rs.getString(2),
                    rs.getString(3), rs.getString(4),
                    rs.getBoolean(5) ? "✔" : ""
                });
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error cargando usuarios", ex);
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void showDialog(Integer editRow) {
        String id = null, name = "", email = "", roleId = null;
        boolean visible = true;
        if (editRow != null) {
            int mr = table.convertRowIndexToModel(editRow);
            id    = (String) model.getValueAt(mr, 0);
            name  = (String) model.getValueAt(mr, 1);
            email = nvl((String) model.getValueAt(mr, 3));
        }
        JTextField fName  = new JTextField(name,  22);
        JTextField fEmail = new JTextField(email, 22);
        JComboBox<String[]> fRole = buildRoleCombo(roleId);
        JCheckBox fVisible = new JCheckBox("Activo", visible);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nombre:")); form.add(fName);
        form.add(new JLabel("Email / Usuario:")); form.add(fEmail);
        form.add(new JLabel("Rol:")); form.add(fRole);
        form.add(new JLabel("Activo:")); form.add(fVisible);

        // Password (solo para nuevos)
        JPasswordField fPass = new JPasswordField(10);
        if (editRow == null) {
            form.add(new JLabel("Contraseña:")); form.add(fPass);
        }

        int res = JOptionPane.showConfirmDialog(this, form,
                editRow == null ? "Nuevo Usuario" : "Editar Usuario",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        String selRole = fRole.getSelectedItem() != null ? ((String[]) fRole.getSelectedItem())[0] : null;
        try (Connection conn = appView.getSession().getConnection()) {
            if (editRow == null) {
                String passHash = Hashcypher.hashString(new String(fPass.getPassword()));
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO people (id, name, appuser, apppassword, role, visible) VALUES (?,?,?,?,?,?)");
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, fName.getText().trim());
                ps.setString(3, fEmail.getText().trim());
                ps.setString(4, passHash);
                ps.setString(5, selRole);
                ps.setBoolean(6, fVisible.isSelected());
                ps.executeUpdate();
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE people SET name=?, appuser=?, role=?, visible=? WHERE id=?");
                ps.setString(1, fName.getText().trim());
                ps.setString(2, fEmail.getText().trim());
                ps.setString(3, selRole);
                ps.setBoolean(4, fVisible.isSelected());
                ps.setString(5, id);
                ps.executeUpdate();
            }
            loadData(searchField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private void changePassword() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un usuario."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);

        JPasswordField fNew  = new JPasswordField(15);
        JPasswordField fNew2 = new JPasswordField(15);
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Nueva contraseña:")); form.add(fNew);
        form.add(new JLabel("Confirmar:")); form.add(fNew2);

        int res = JOptionPane.showConfirmDialog(this, form,
                "Cambiar contraseña - " + name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        if (!new String(fNew.getPassword()).equals(new String(fNew2.getPassword()))) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden."); return;
        }
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE people SET apppassword=? WHERE id=?")) {
            ps.setString(1, Hashcypher.hashString(new String(fNew.getPassword())));
            ps.setString(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Contraseña actualizada correctamente.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error:\n" + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String[]> buildRoleCombo(String selectedId) {
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
             ResultSet rs = st.executeQuery("SELECT id, name FROM roles ORDER BY name")) {
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
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un usuario."); return; }
        int mr = table.convertRowIndexToModel(row);
        String id   = (String) model.getValueAt(mr, 0);
        String name = (String) model.getValueAt(mr, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el usuario \"" + name + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM people WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
            loadData(searchField.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se puede eliminar (en uso):\n" + ex.getMessage());
        }
    }

    private String nvl(String s) { return s != null ? s : ""; }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "👤 Usuarios"; }
    @Override public boolean deactivate() { return true; }
}
