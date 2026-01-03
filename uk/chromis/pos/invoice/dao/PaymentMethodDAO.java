package uk.chromis.pos.invoice.dao;

import uk.chromis.pos.invoice.models.PaymentMethod;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para métodos de pago de facturas electrónicas
 */
public class PaymentMethodDAO {
    
    private Connection connection;
    
    public PaymentMethodDAO(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Inserta un método de pago
     */
    public void insertPaymentMethod(String invoiceId, PaymentMethod method) throws SQLException {
        String sql = "INSERT INTO payment_methods " +
            "(invoice_id, code, amount, description) " +
            "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            pstmt.setString(2, method.getCode());
            pstmt.setBigDecimal(3, method.getAmount());
            pstmt.setString(4, method.getDescription());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Obtiene todos los métodos de pago de una factura
     */
    public List<PaymentMethod> getPaymentMethodsByInvoiceId(String invoiceId) throws SQLException {
        List<PaymentMethod> methods = new ArrayList<>();
        String sql = "SELECT * FROM payment_methods WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    methods.add(mapResultSetToPaymentMethod(rs));
                }
            }
        }
        
        return methods;
    }
    
    /**
     * Elimina todos los métodos de pago de una factura
     */
    public void deletePaymentMethodsByInvoiceId(String invoiceId) throws SQLException {
        String sql = "DELETE FROM payment_methods WHERE invoice_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoiceId);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto PaymentMethod
     */
    private PaymentMethod mapResultSetToPaymentMethod(ResultSet rs) throws SQLException {
        PaymentMethod method = new PaymentMethod();
        
        method.setCode(rs.getString("code"));
        method.setAmount(rs.getBigDecimal("amount"));
        method.setDescription(rs.getString("description"));
        
        return method;
    }
}
