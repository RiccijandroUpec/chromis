package uk.chromis.pos.invoice.dao;

import java.sql.Connection;

/**
 * Factory para DAOs de facturación electrónica
 * Patrón Factory para centralizar creación de DAOs
 */
public class InvoiceDAOFactory {
    
    private Connection connection;
    
    public InvoiceDAOFactory(Connection connection) {
        this.connection = connection;
    }
    
    /**
     * Obtiene DAO para facturas electrónicas
     */
    public ElectronicInvoiceDAO getElectronicInvoiceDAO() {
        return new ElectronicInvoiceDAO(connection);
    }
    
    /**
     * Obtiene DAO para detalles de facturas
     */
    public InvoiceDetailDAO getInvoiceDetailDAO() {
        return new InvoiceDetailDAO(connection);
    }
    
    /**
     * Obtiene DAO para métodos de pago
     */
    public PaymentMethodDAO getPaymentMethodDAO() {
        return new PaymentMethodDAO(connection);
    }
}
