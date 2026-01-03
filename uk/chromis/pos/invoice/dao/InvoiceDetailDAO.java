package uk.chromis.pos.invoice.dao;

import uk.chromis.pos.invoice.models.InvoiceDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para detalles de facturas electrónicas
 */
public class InvoiceDetailDAO {
    
    private Connection connection;
    
    public InvoiceDetailDAO(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Inserta un detalle de factura
     */
    public void insertDetail(String invoiceId, InvoiceDetail detail) throws SQLException {
        String sql = "INSERT INTO invoice_details " +
            "(invoice_id, code, description, quantity, unit_price, discount, " +
            "tax_code, tax_rate, line_total) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            pstmt.setString(2, detail.getCode());
            pstmt.setString(3, detail.getDescription());
            pstmt.setBigDecimal(4, detail.getQuantity());
            pstmt.setBigDecimal(5, detail.getUnitPrice());
            pstmt.setBigDecimal(6, detail.getDiscount());
            pstmt.setString(7, detail.getTaxCode());
            pstmt.setBigDecimal(8, detail.getTaxRate());
            pstmt.setBigDecimal(9, detail.getLineTotal());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Obtiene todos los detalles de una factura
     */
    public List<InvoiceDetail> getDetailsByInvoiceId(String invoiceId) throws SQLException {
        List<InvoiceDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM invoice_details WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(mapResultSetToDetail(rs));
                }
            }
        }
        
        return details;
    }
    
    /**
     * Elimina todos los detalles de una factura
     */
    public void deleteDetailsByInvoiceId(String invoiceId) throws SQLException {
        String sql = "DELETE FROM invoice_details WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto InvoiceDetail
     */
    private InvoiceDetail mapResultSetToDetail(ResultSet rs) throws SQLException {
        InvoiceDetail detail = new InvoiceDetail();
        
        detail.setCode(rs.getString("code"));
        detail.setDescription(rs.getString("description"));
        detail.setQuantity(rs.getBigDecimal("quantity"));
        detail.setUnitPrice(rs.getBigDecimal("unit_price"));
        detail.setDiscount(rs.getBigDecimal("discount"));
        detail.setTaxCode(rs.getString("tax_code"));
        detail.setTaxRate(rs.getBigDecimal("tax_rate"));
        detail.setLineTotal(rs.getBigDecimal("line_total"));
        
        return detail;
    }
}
