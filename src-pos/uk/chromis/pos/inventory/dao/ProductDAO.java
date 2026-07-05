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
 * Acceso a datos para el panel de administración de Productos.
 */
public class ProductDAO {

    private final AppView appView;

    public ProductDAO(AppView appView) {
        this.appView = appView;
    }

    /**
     * Busca productos por nombre o código. Cada fila: {id, reference, name, formattedPrice, categoryName, activoMark}
     */
    public List<Object[]> search(String filter) throws SQLException {
        String sql = "SELECT p.id, p.reference, p.name, p.pricesell, c.name, p.ispack "
                   + "FROM products p LEFT JOIN categories c ON p.category = c.id "
                   + "WHERE p.name LIKE ? OR p.reference LIKE ? "
                   + "ORDER BY p.name LIMIT 500";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + filter + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        String.format("$ %.2f", rs.getDouble(4)),
                        rs.getString(5),
                        rs.getBoolean(6) ? "✔" : ""
                    });
                }
            }
        }
        return rows;
    }

    /** Categorías disponibles para el combo del formulario: pares {id, name}. */
    public List<String[]> listCategories() throws SQLException {
        List<String[]> categories = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) {
                categories.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        }
        return categories;
    }

    public void insert(String code, String name, double price, String categoryId) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO products (id, reference, name, pricesell, category, ispack) VALUES (?,?,?,?,?,0)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, code);
            ps.setString(3, name);
            ps.setDouble(4, price);
            ps.setString(5, categoryId);
            ps.executeUpdate();
        }
    }

    public void update(String id, String code, String name, double price, String categoryId) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE products SET reference=?, name=?, pricesell=?, category=? WHERE id=?")) {
            ps.setString(1, code);
            ps.setString(2, name);
            ps.setDouble(3, price);
            ps.setString(4, categoryId);
            ps.setString(5, id);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
