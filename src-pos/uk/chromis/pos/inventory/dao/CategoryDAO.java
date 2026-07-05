package uk.chromis.pos.inventory.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.chromis.pos.forms.AppView;

/**
 * Acceso a datos para el panel de administración de Categorías.
 */
public class CategoryDAO {

    private final AppView appView;

    public CategoryDAO(AppView appView) {
        this.appView = appView;
    }

    /** Cada fila: {id, name, parentName} */
    public List<Object[]> search(String filter) throws SQLException {
        String sql = "SELECT c.id, c.name, p.name FROM categories c "
                   + "LEFT JOIN categories p ON c.parentid = p.id "
                   + "WHERE c.name LIKE ? ORDER BY c.name LIMIT 300";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + filter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3)});
                }
            }
        }
        return rows;
    }

    /** Categorías candidatas a padre (excluye la propia categoría en edición): pares {id, name}. */
    public List<String[]> listParentCandidates(String excludeId) throws SQLException {
        List<String[]> categories = new ArrayList<>();
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, name FROM categories WHERE id != ? ORDER BY name")) {
            ps.setString(1, excludeId != null ? excludeId : "");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(new String[]{rs.getString(1), rs.getString(2)});
                }
            }
        }
        return categories;
    }

    public void insert(String name, String parentId) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO categories (id, name, parentid) VALUES (?,?,?)")) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, name);
            ps.setString(3, parentId);
            ps.executeUpdate();
        }
    }

    public void update(String id, String name, String parentId) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE categories SET name=?, parentid=? WHERE id=?")) {
            ps.setString(1, name);
            ps.setString(2, parentId);
            ps.setString(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        try (Connection conn = appView.getSession().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
