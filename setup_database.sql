-- ============================================
-- Setup Facturación Electrónica Ecuador
-- ============================================

-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS chromisdb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Seleccionar base de datos
USE chromisdb;

-- ============================================
-- Tabla principal de facturas electrónicas
-- ============================================
CREATE TABLE IF NOT EXISTS electronic_invoices (
    id VARCHAR(36) PRIMARY KEY NOT NULL COMMENT 'UUID único',
    invoice_number VARCHAR(20) NOT NULL COMMENT 'Número secuencial de factura',
    access_key VARCHAR(49) UNIQUE COMMENT 'Clave de acceso SRI (49 dígitos)',
    issue_date DATETIME NOT NULL COMMENT 'Fecha y hora de emisión',
    issuer_ruc VARCHAR(13) NOT NULL COMMENT 'RUC del emisor',
    buyer_identification VARCHAR(20) NOT NULL COMMENT 'Identificación del comprador',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT 'Subtotal sin impuestos',
    iva_total DECIMAL(10, 2) NOT NULL COMMENT 'Total IVA',
    total DECIMAL(10, 2) NOT NULL COMMENT 'Total a pagar',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT 'Estado de la factura',
    authorization_number VARCHAR(49) COMMENT 'Número de autorización SRI',
    sent_to_sri BOOLEAN DEFAULT FALSE COMMENT 'Indicador si fue enviada al SRI',
    sent_date DATETIME COMMENT 'Fecha de envío al SRI',
    sri_response LONGTEXT COMMENT 'Respuesta del SRI',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_issuer_ruc (issuer_ruc),
    INDEX idx_status (status),
    INDEX idx_issue_date (issue_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla de detalles de líneas de factura
-- ============================================
CREATE TABLE IF NOT EXISTS invoice_details (
    id VARCHAR(36) PRIMARY KEY NOT NULL COMMENT 'UUID único',
    invoice_id VARCHAR(36) NOT NULL COMMENT 'Referencia a factura',
    product_code VARCHAR(50) NOT NULL COMMENT 'Código del producto',
    product_description VARCHAR(300) NOT NULL COMMENT 'Descripción del producto',
    quantity DECIMAL(10, 4) NOT NULL COMMENT 'Cantidad de producto',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT 'Precio unitario',
    discount DECIMAL(10, 2) DEFAULT 0 COMMENT 'Descuento en línea',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT 'Subtotal de línea',
    tax_rate DECIMAL(5, 2) DEFAULT 12 COMMENT 'Porcentaje de IVA',
    tax_amount DECIMAL(10, 2) NOT NULL COMMENT 'Monto de impuesto',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice_id (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla de métodos de pago
-- ============================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id VARCHAR(36) PRIMARY KEY NOT NULL COMMENT 'UUID único',
    invoice_id VARCHAR(36) NOT NULL COMMENT 'Referencia a factura',
    payment_method VARCHAR(20) NOT NULL COMMENT 'Forma de pago (01=Efectivo, 02=Tarjeta, etc)',
    amount DECIMAL(10, 2) NOT NULL COMMENT 'Monto pagado con este método',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice_id (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla de configuración de facturación
-- ============================================
CREATE TABLE IF NOT EXISTS invoice_configuration (
    id INT PRIMARY KEY AUTO_INCREMENT,
    issuer_ruc VARCHAR(13) NOT NULL UNIQUE COMMENT 'RUC del emisor',
    issuer_name VARCHAR(300) NOT NULL COMMENT 'Razón social',
    trade_name VARCHAR(300) NOT NULL COMMENT 'Nombre comercial',
    certificate_path VARCHAR(500) NOT NULL COMMENT 'Ruta al certificado digital',
    certificate_password VARCHAR(255) COMMENT 'Contraseña del certificado',
    sri_url VARCHAR(500) COMMENT 'URL del servidor SRI',
    test_mode BOOLEAN DEFAULT TRUE COMMENT 'Modo prueba vs producción',
    email_notification VARCHAR(255) COMMENT 'Email para notificaciones',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Tabla de log de envíos a SRI
-- ============================================
CREATE TABLE IF NOT EXISTS sri_submission_log (
    id VARCHAR(36) PRIMARY KEY NOT NULL COMMENT 'UUID único',
    invoice_id VARCHAR(36) NOT NULL COMMENT 'Referencia a factura',
    access_key VARCHAR(49) NOT NULL COMMENT 'Clave de acceso',
    submission_date DATETIME NOT NULL COMMENT 'Fecha de envío',
    response_code VARCHAR(10) COMMENT 'Código de respuesta SRI',
    response_message LONGTEXT COMMENT 'Mensaje de respuesta',
    authorization_number VARCHAR(49) COMMENT 'Número de autorización SRI',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    INDEX idx_invoice_id (invoice_id),
    INDEX idx_access_key (access_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Vista de facturas con detalles resumidos
-- ============================================
CREATE OR REPLACE VIEW v_invoices_summary AS
SELECT 
    ei.id,
    ei.invoice_number,
    ei.access_key,
    ei.issue_date,
    ei.issuer_ruc,
    ei.buyer_identification,
    ei.subtotal,
    ei.iva_total,
    ei.total,
    ei.status,
    ei.authorization_number,
    ei.sent_to_sri,
    COUNT(id2.id) as detail_count
FROM electronic_invoices ei
LEFT JOIN invoice_details id2 ON ei.id = id2.invoice_id
GROUP BY ei.id;

-- ============================================
-- Vista de facturas pendientes de envío
-- ============================================
CREATE OR REPLACE VIEW v_invoices_pending_sri AS
SELECT 
    id,
    invoice_number,
    access_key,
    issue_date,
    issuer_ruc,
    buyer_identification,
    total,
    status
FROM electronic_invoices
WHERE sent_to_sri = FALSE AND status = 'DRAFT'
ORDER BY issue_date DESC;

-- ============================================
-- Vista de historial de envíos
-- ============================================
CREATE OR REPLACE VIEW v_sri_submission_history AS
SELECT 
    sri.id,
    sri.invoice_id,
    sri.access_key,
    sri.submission_date,
    sri.response_code,
    sri.authorization_number,
    ei.invoice_number,
    ei.issuer_ruc,
    ei.total
FROM sri_submission_log sri
JOIN electronic_invoices ei ON sri.invoice_id = ei.id
ORDER BY sri.submission_date DESC;

-- ============================================
-- Índices adicionales para performance
-- ============================================
CREATE INDEX idx_electronic_invoices_access_key ON electronic_invoices(access_key);
CREATE INDEX idx_electronic_invoices_issuer_date ON electronic_invoices(issuer_ruc, issue_date);
CREATE INDEX idx_invoice_details_invoice ON invoice_details(invoice_id);
CREATE INDEX idx_payment_methods_invoice ON payment_methods(invoice_id);
CREATE INDEX idx_sri_log_date ON sri_submission_log(submission_date);

-- ============================================
-- Fin del script de instalación
-- ============================================
