package uk.chromis.pos.inventory.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.chromis.pos.forms.AppView;

/**
 * Acceso a datos para el panel de administración de Impuestos.
 */
public class TaxDAO {

    private final AppView appView;

    public TaxDAO(AppView appView) {
        this.appView = appView;
    }

    /** Cada fila: {id, name, formattedRate, categoryPlaceholder} */
    public List<Object[]> list() throws SQLException {
        String sql = "SELECT id, name, rate FROM taxes ORDER BY name";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString(1), rs.getString(2),
                    String.format("%.2f%%", rs.getDouble(3) * 100),
                    ""
                });
            }
        }
        return rows;
    }

    /** Categorías de impuesto para el combo del formulario: pares {id, name}. */
    public List<String[]> listTaxCategories() throws SQLException {
        List<String[]> categories = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM taxcategories ORDER BY name")) {
            while (rs.next()) {
                categories.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        }
        return categories;
    }

    public void insert(String name, double rate) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO taxes (id, name, rate) VALUES (?,?,?)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, name);
            ps.setDouble(3, rate);
            ps.executeUpdate();
        }
    }

    public void update(String id, String name, double rate) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE taxes SET name=?, rate=? WHERE id=?")) {
            ps.setString(1, name);
            ps.setDouble(2, rate);
            ps.setString(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM taxes WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
