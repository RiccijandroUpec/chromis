package uk.chromis.pos.invoice.dao;

import uk.chromis.pos.invoice.models.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para facturas electrónicas
 * Maneja la persistencia de facturas en base de datos
 */
public class ElectronicInvoiceDAO {
    
    private Connection connection;
    
    public ElectronicInvoiceDAO(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Crea una nueva factura en la base de datos
     */
    public void insertInvoice(ElectronicInvoice invoice) throws SQLException {
        String sql = "INSERT INTO electronic_invoices " +
            "(id, invoice_number, access_key, issue_date, issuer_ruc, buyer_identification, " +
            "subtotal, iva_total, total, status, created_date, updated_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getId());
            pstmt.setString(2, invoice.getInvoiceNumber());
            pstmt.setString(3, invoice.getAccessKey());
            pstmt.setTimestamp(4, Timestamp.valueOf(invoice.getIssueDate()));
            pstmt.setString(5, invoice.getIssuer().getRuc());
            pstmt.setString(6, invoice.getBuyer().getIdentification());
            pstmt.setBigDecimal(7, invoice.getSubtotal());
            pstmt.setBigDecimal(8, invoice.getIvaTotal());
            pstmt.setBigDecimal(9, invoice.getTotal());
            pstmt.setString(10, invoice.getStatus().name());
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Actualiza una factura existente
     */
    public void updateInvoice(ElectronicInvoice invoice) throws SQLException {
        String sql = "UPDATE electronic_invoices SET " +
            "access_key = ?, status = ?, authorization_number = ?, " +
            "sent_to_sri = ?, sent_date = ?, sri_response = ?, updated_date = ? " +
            "WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getAccessKey());
            pstmt.setString(2, invoice.getStatus().name());
            pstmt.setString(3, invoice.getAuthorizationNumber());
            pstmt.setBoolean(4, invoice.isSentToSRI());
            if (invoice.getSentDate() != null) {
                pstmt.setTimestamp(5, Timestamp.valueOf(invoice.getSentDate()));
            } else {
                pstmt.setNull(5, java.sql.Types.TIMESTAMP);
            }
            pstmt.setString(6, invoice.getSriResponse());
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(8, invoice.getId());
            
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Obtiene una factura por ID
     */
    public ElectronicInvoice getInvoiceById(String id) throws SQLException {
        String sql = "SELECT * FROM electronic_invoices WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene una factura por número de acceso
     */
    public ElectronicInvoice getInvoiceByAccessKey(String accessKey) throws SQLException {
        String sql = "SELECT * FROM electronic_invoices WHERE access_key = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accessKey);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Obtiene todas las facturas
     */
    public List<ElectronicInvoice> getAllInvoices() throws SQLException {
        List<ElectronicInvoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM electronic_invoices ORDER BY created_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }
        
        return invoices;
    }
    
    /**
     * Obtiene facturas por estado
     */
    public List<ElectronicInvoice> getInvoicesByStatus(InvoiceStatus status) throws SQLException {
        List<ElectronicInvoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM electronic_invoices WHERE status = ? ORDER BY created_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    invoices.add(mapResultSetToInvoice(rs));
                }
            }
        }
        
        return invoices;
    }
    
    /**
     * Obtiene facturas autorizadas
     */
    public List<ElectronicInvoice> getAuthorizedInvoices() throws SQLException {
        return getInvoicesByStatus(InvoiceStatus.AUTHORIZED);
    }
    
    /**
     * Obtiene facturas pendientes de envío
     */
    public List<ElectronicInvoice> getPendingInvoices() throws SQLException {
        List<ElectronicInvoice> invoices = new ArrayList<>();
        String sql = "SELECT * FROM electronic_invoices WHERE status IN ('SIGNED', 'REJECTED') " +
                    "AND sent_to_sri = false ORDER BY created_date ASC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }
        
        return invoices;
    }
    
    /**
     * Elimina una factura
     */
    public void deleteInvoice(String id) throws SQLException {
        String sql = "DELETE FROM electronic_invoices WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto ElectronicInvoice
     */
    private ElectronicInvoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        ElectronicInvoice invoice = new ElectronicInvoice();
        
        invoice.setId(rs.getString("id"));
        invoice.setInvoiceNumber(rs.getString("invoice_number"));
        invoice.setAccessKey(rs.getString("access_key"));
        invoice.setIssueDate(rs.getTimestamp("issue_date").toLocalDateTime());
        invoice.setStatus(InvoiceStatus.valueOf(rs.getString("status")));
        invoice.setAuthorizationNumber(rs.getString("authorization_number"));
        invoice.setSentToSRI(rs.getBoolean("sent_to_sri"));
        
        Timestamp sentDate = rs.getTimestamp("sent_date");
        if (sentDate != null) {
            invoice.setSentDate(sentDate.toLocalDateTime());
        }
        
        invoice.setSriResponse(rs.getString("sri_response"));
        invoice.setSubtotal(rs.getBigDecimal("subtotal"));
        invoice.setIvaTotal(rs.getBigDecimal("iva_total"));
        invoice.setTotal(rs.getBigDecimal("total"));
        
        return invoice;
    }
}
