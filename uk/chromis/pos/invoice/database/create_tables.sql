-- ============================================
-- Base de Datos para Facturación Electrónica Ecuador
-- ============================================

-- Tabla principal de facturas electrónicas
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
    xml_content LONGTEXT COMMENT 'Contenido XML generado',
    signed_xml_content LONGTEXT COMMENT 'XML firmado digitalmente',
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    KEY idx_access_key (access_key),
    KEY idx_status (status),
    KEY idx_issuer_ruc (issuer_ruc),
    KEY idx_buyer_identification (buyer_identification),
    KEY idx_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Facturas electrónicas generadas';

-- Tabla de detalles de facturas (items/productos)
CREATE TABLE IF NOT EXISTS invoice_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    code VARCHAR(50) NOT NULL COMMENT 'Código del producto',
    description VARCHAR(255) NOT NULL COMMENT 'Descripción del producto',
    quantity DECIMAL(10, 4) NOT NULL COMMENT 'Cantidad',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT 'Precio unitario',
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT 'Descuento',
    tax_code VARCHAR(5) NOT NULL COMMENT 'Código de impuesto',
    tax_rate DECIMAL(5, 2) NOT NULL COMMENT 'Tasa de impuesto',
    line_total DECIMAL(10, 2) NOT NULL COMMENT 'Total de la línea',
    
    CONSTRAINT fk_invoice_detail FOREIGN KEY (invoice_id) 
        REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    
    KEY idx_invoice_id (invoice_id),
    KEY idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Detalles/items de las facturas electrónicas';

-- Tabla de métodos de pago
CREATE TABLE IF NOT EXISTS payment_methods (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    code VARCHAR(10) NOT NULL COMMENT 'Código de forma de pago',
    amount DECIMAL(10, 2) NOT NULL COMMENT 'Monto pagado',
    description VARCHAR(255) COMMENT 'Descripción del pago',
    
    CONSTRAINT fk_payment_method FOREIGN KEY (invoice_id) 
        REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    
    KEY idx_invoice_id (invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Métodos de pago de las facturas electrónicas';

-- Tabla de auditoria de envíos a SRI
CREATE TABLE IF NOT EXISTS sri_submission_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id VARCHAR(36) NOT NULL,
    access_key VARCHAR(49) NOT NULL,
    sent_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    request_xml LONGTEXT COMMENT 'XML enviado',
    response_status VARCHAR(50) COMMENT 'Estado de respuesta',
    response_message LONGTEXT COMMENT 'Mensaje de respuesta',
    http_status_code INT COMMENT 'Código HTTP de respuesta',
    
    CONSTRAINT fk_sri_log FOREIGN KEY (invoice_id) 
        REFERENCES electronic_invoices(id) ON DELETE CASCADE,
    
    KEY idx_invoice_id (invoice_id),
    KEY idx_access_key (access_key),
    KEY idx_sent_date (sent_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de auditoría de envíos a SRI';

-- Tabla de configuración de emisor
CREATE TABLE IF NOT EXISTS invoice_issuer_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(13) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL COMMENT 'Razón Social',
    trade_name VARCHAR(255) COMMENT 'Nombre Comercial',
    address VARCHAR(255),
    city VARCHAR(100),
    province VARCHAR(100),
    country VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    certificate_path VARCHAR(500) COMMENT 'Ruta al certificado digital',
    certificate_password VARCHAR(255) COMMENT 'Contraseña del certificado (encriptada)',
    environment VARCHAR(20) DEFAULT 'test' COMMENT 'Ambiente: test o production',
    active BOOLEAN DEFAULT TRUE,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    KEY idx_ruc (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Configuración del emisor de facturas';

-- Tabla de series de facturación
CREATE TABLE IF NOT EXISTS invoice_series (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ruc VARCHAR(13) NOT NULL,
    series_number VARCHAR(3) NOT NULL COMMENT 'Número de serie (001, 002, etc)',
    point_of_sale VARCHAR(3) NOT NULL COMMENT 'Punto de venta (001, 002, etc)',
    next_sequential_number INT NOT NULL DEFAULT 1 COMMENT 'Próximo número secuencial',
    last_authorized_number INT COMMENT 'Último número autorizado por SRI',
    active BOOLEAN DEFAULT TRUE,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_series (ruc, series_number, point_of_sale),
    KEY idx_ruc (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Series de numeración para facturas por emisor';

-- Índices adicionales para optimización
ALTER TABLE electronic_invoices ADD INDEX idx_issuer_status (issuer_ruc, status);
ALTER TABLE electronic_invoices ADD INDEX idx_sent_to_sri (sent_to_sri, status);
ALTER TABLE invoice_details ADD INDEX idx_tax_code (tax_code);

-- Vistas útiles
CREATE OR REPLACE VIEW vw_invoices_by_status AS
SELECT 
    status,
    COUNT(*) as total,
    SUM(total) as amount
FROM electronic_invoices
GROUP BY status;

CREATE OR REPLACE VIEW vw_authorized_invoices AS
SELECT 
    id,
    invoice_number,
    access_key,
    issue_date,
    issuer_ruc,
    buyer_identification,
    total,
    authorization_number
FROM electronic_invoices
WHERE status = 'AUTHORIZED'
ORDER BY issue_date DESC;

CREATE OR REPLACE VIEW vw_pending_invoices AS
SELECT 
    id,
    invoice_number,
    issue_date,
    issuer_ruc,
    total,
    status
FROM electronic_invoices
WHERE sent_to_sri = FALSE
ORDER BY issue_date ASC;
