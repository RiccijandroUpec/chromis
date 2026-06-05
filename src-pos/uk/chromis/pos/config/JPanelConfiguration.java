package uk.chromis.pos.config;

import java.awt.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.JPanelView;

/**
 * Panel de Configuración General del sistema Chromis.
 * Muestra y permite editar los parámetros de la tabla systemproperties.
 */
public class JPanelConfiguration extends JPanel implements JPanelView {

    private static final Logger logger = Logger.getLogger(JPanelConfiguration.class.getName());
    private final AppView appView;

    private JPanel propsPanel;
    private JScrollPane scroll;

    // Campos editables clave (property -> label amigable)
    private static final String[][] PROPS = {
        {"machine.name",       "Nombre de Máquina"},
        {"location",           "Ubicación / Sucursal"},
        {"receipts.printer",   "Impresora de Tickets"},
        {"taxincluded",        "IVA incluido en precios (true/false)"},
        {"ICONCOLOUR",         "Color de Iconos (ej: royalblue)"},
        {"customer.display",   "Pantalla de Cliente"},
        {"scale.com",          "Puerto Báscula"},
        {"barcode.scanner",    "Lector de Códigos"},
        {"receipt.footer",     "Pie de Ticket"},
        {"receipt.header",     "Cabecera de Ticket"},
    };

    public JPanelConfiguration(AppView appView) {
        this.appView = appView;
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(60, 60, 60));
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("⚙️ Configuración General del Sistema");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        propsPanel = new JPanel(new GridBagLayout());
        propsPanel.setBackground(Color.WHITE);
        propsPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        scroll = new JScrollPane(propsPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(210, 215, 220)));
        JButton btnSave    = new JButton("💾 Guardar cambios");
        JButton btnRefresh = new JButton("🔄 Recargar");
        btnSave.setBackground(new Color(46, 160, 67));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnRefresh.setBackground(new Color(100, 100, 110));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        footer.add(btnRefresh); footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveProps());
        btnRefresh.addActionListener(e -> loadProps());
    }

    @Override
    public void activate() throws BasicException { loadProps(); }

    private void loadProps() {
        propsPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel secLabel = new JLabel("Parámetros del sistema (systemproperties):");
        secLabel.setFont(new Font("Arial", Font.BOLD, 13));
        secLabel.setForeground(new Color(40, 40, 40));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
        propsPanel.add(secLabel, gbc);
        gbc.gridwidth = 1;

        try (Connection conn = appView.getSession().getConnection()) {
            int row = 1;
            for (String[] prop : PROPS) {
                String key = prop[0];
                String label = prop[1];
                String value = getProperty(conn, key);

                JLabel lbl = new JLabel(label + ":");
                lbl.setFont(new Font("Arial", Font.PLAIN, 12));
                lbl.setForeground(new Color(70, 70, 70));

                JTextField field = new JTextField(value, 30);
                field.setName(key); // usamos name para identificar la clave
                field.setFont(new Font("Arial", Font.PLAIN, 12));

                gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
                propsPanel.add(lbl, gbc);
                gbc.gridx = 1; gbc.weightx = 1.0;
                propsPanel.add(field, gbc);
                row++;
            }

            // Añadir separador y sección "todos los parámetros"
            JSeparator sep = new JSeparator();
            gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1.0;
            propsPanel.add(sep, gbc); row++;

            JLabel allLabel = new JLabel("Todos los parámetros de la BD:");
            allLabel.setFont(new Font("Arial", Font.BOLD, 12));
            allLabel.setForeground(new Color(60, 60, 60));
            gbc.gridy = row;
            propsPanel.add(allLabel, gbc); row++;
            gbc.gridwidth = 1;

            PreparedStatement ps = conn.prepareStatement(
                "SELECT property, value FROM systemproperties ORDER BY property");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String key   = rs.getString(1);
                String value = rs.getString(2);

                JLabel lbl = new JLabel(key + ":");
                lbl.setFont(new Font("Monospaced", Font.PLAIN, 11));
                lbl.setForeground(new Color(100, 100, 120));

                JTextField field = new JTextField(nvl(value), 28);
                field.setName(key);
                field.setFont(new Font("Monospaced", Font.PLAIN, 11));

                gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
                propsPanel.add(lbl, gbc);
                gbc.gridx = 1; gbc.weightx = 1.0;
                propsPanel.add(field, gbc);
                row++;
            }
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Error cargando configuración", ex);
            JOptionPane.showMessageDialog(this, "Error al cargar configuración:\n" + ex.getMessage());
        }
        propsPanel.revalidate();
        propsPanel.repaint();
    }

    private void saveProps() {
        int saved = 0;
        try (Connection conn = appView.getSession().getConnection()) {
            // Iterar sobre todos los campos del panel
            for (Component c : propsPanel.getComponents()) {
                if (c instanceof JTextField) {
                    JTextField field = (JTextField) c;
                    String key = field.getName();
                    String value = field.getText().trim();
                    if (key == null || key.isEmpty()) continue;

                    // UPSERT: si existe actualiza, si no inserta
                    PreparedStatement check = conn.prepareStatement(
                        "SELECT count(*) FROM systemproperties WHERE property=?");
                    check.setString(1, key);
                    ResultSet rs = check.executeQuery();
                    rs.next();
                    int count = rs.getInt(1);

                    if (count > 0) {
                        PreparedStatement upd = conn.prepareStatement(
                            "UPDATE systemproperties SET value=? WHERE property=?");
                        upd.setString(1, value);
                        upd.setString(2, key);
                        upd.executeUpdate();
                    } else {
                        PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO systemproperties (property, value) VALUES (?,?)");
                        ins.setString(1, key);
                        ins.setString(2, value);
                        ins.executeUpdate();
                    }
                    saved++;
                }
            }
            JOptionPane.showMessageDialog(this, "✅ " + saved + " parámetros guardados correctamente.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage());
        }
    }

    private String getProperty(Connection conn, String key) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT value FROM systemproperties WHERE property=?")) {
            ps.setString(1, key);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return nvl(rs.getString(1));
        } catch (SQLException ex) { /* ignore */ }
        return "";
    }

    private String nvl(String s) { return s != null ? s : ""; }

    @Override public JComponent getComponent() { return this; }
    @Override public String getTitle() { return "⚙️ Configuración"; }
    @Override public boolean deactivate() { return true; }
}
