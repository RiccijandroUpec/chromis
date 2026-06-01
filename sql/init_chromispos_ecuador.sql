-- ============================================================
-- ChromisPOS Ecuador - Script de Inicialización de Base de Datos
-- Autor: Riccijandro | github.com/riccijandro
-- ============================================================

USE chromisnewtest;

-- Tabla de testing de pool de conexiones (c3p0)
CREATE TABLE IF NOT EXISTS c3p0testing (
  a CHAR(1)
);

-- Propiedades del sistema
CREATE TABLE IF NOT EXISTS systemproperties (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  constant VARCHAR(255) NOT NULL,
  classname VARCHAR(255),
  uservalue VARCHAR(2000),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Blobs del sistema (logos, imágenes)
CREATE TABLE IF NOT EXISTS systemblobs (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  constant VARCHAR(255) NOT NULL,
  classname VARCHAR(255),
  defaultimage MEDIUMBLOB,
  userimage MEDIUMBLOB,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Roles / Personas
CREATE TABLE IF NOT EXISTS people (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  apppassword VARCHAR(255),
  card VARCHAR(255),
  role VARCHAR(255),
  visible BOOLEAN DEFAULT TRUE,
  image MEDIUMBLOB,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Recursos
CREATE TABLE IF NOT EXISTS resources (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  restype VARCHAR(255),
  content MEDIUMBLOB,
  contenttype VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Impuestos
CREATE TABLE IF NOT EXISTS taxes (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  custcategory VARCHAR(36),
  parentid VARCHAR(36),
  rate DOUBLE,
  ratecascade BOOLEAN DEFAULT FALSE,
  rateorder INTEGER,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Categorías de productos
CREATE TABLE IF NOT EXISTS categories (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  parentid VARCHAR(36),
  image MEDIUMBLOB,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Productos
CREATE TABLE IF NOT EXISTS products (
  id VARCHAR(36) NOT NULL,
  reference VARCHAR(255),
  code VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  pricebuy DOUBLE,
  pricesell DOUBLE,
  category VARCHAR(36),
  taxcat VARCHAR(36),
  stockcost DOUBLE DEFAULT 0,
  stockvolume DOUBLE DEFAULT 0,
  image MEDIUMBLOB,
  ispack BOOLEAN DEFAULT FALSE,
  attributes VARCHAR(2000),
  discountenabled BOOLEAN DEFAULT FALSE,
  discountrate DOUBLE DEFAULT 0,
  visible BOOLEAN DEFAULT TRUE,
  soldout BOOLEAN DEFAULT FALSE,
  alertlevel DOUBLE DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Stock
CREATE TABLE IF NOT EXISTS stockcurrent (
  location VARCHAR(36) NOT NULL,
  product VARCHAR(36) NOT NULL,
  attributesetinstance_id VARCHAR(36),
  units DOUBLE DEFAULT 0,
  PRIMARY KEY (location, product)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Locaciones
CREATE TABLE IF NOT EXISTS locations (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  address VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Facturas / Tickets
CREATE TABLE IF NOT EXISTS receipts (
  id VARCHAR(36) NOT NULL,
  money VARCHAR(36),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Líneas de ticket
CREATE TABLE IF NOT EXISTS ticketlines (
  ticket VARCHAR(36) NOT NULL,
  line INTEGER NOT NULL,
  product VARCHAR(36),
  attributesetinstance_id VARCHAR(36),
  units DOUBLE,
  price DOUBLE,
  taxid VARCHAR(36),
  attributes VARCHAR(2000),
  PRIMARY KEY (ticket, line)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Tickets
CREATE TABLE IF NOT EXISTS tickets (
  id VARCHAR(36) NOT NULL,
  tickettype INTEGER,
  ticketid INTEGER,
  person VARCHAR(36),
  customer VARCHAR(36),
  status INTEGER DEFAULT 0,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Clientes
CREATE TABLE IF NOT EXISTS customers (
  id VARCHAR(36) NOT NULL,
  searchkey VARCHAR(255) NOT NULL,
  taxid VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  address VARCHAR(255),
  address2 VARCHAR(255),
  postal VARCHAR(255),
  city VARCHAR(255),
  region VARCHAR(255),
  country VARCHAR(255),
  email VARCHAR(255),
  phone VARCHAR(255),
  phone2 VARCHAR(255),
  visible BOOLEAN DEFAULT TRUE,
  notes VARCHAR(1000),
  image MEDIUMBLOB,
  curdate DATETIME,
  curdebt DOUBLE DEFAULT 0,
  maxdebt DOUBLE DEFAULT 0,
  taxincluded BOOLEAN DEFAULT TRUE,
  taxcategory VARCHAR(36),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Pagos de tickets
CREATE TABLE IF NOT EXISTS payments (
  id VARCHAR(36) NOT NULL,
  receipt VARCHAR(36),
  payment VARCHAR(255),
  total DOUBLE,
  notes VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Cierres de caja
CREATE TABLE IF NOT EXISTS closedcash (
  money VARCHAR(36) NOT NULL,
  host VARCHAR(255),
  hostsequence INTEGER,
  datestart DATETIME,
  dateend DATETIME,
  nosales INTEGER,
  PRIMARY KEY (money)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Botones del menú de ventas
CREATE TABLE IF NOT EXISTS ticketbuttons (
  id VARCHAR(36) NOT NULL,
  name VARCHAR(255) NOT NULL,
  content MEDIUMBLOB,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Tabla de facturas electrónicas Ecuador (SRI)
CREATE TABLE IF NOT EXISTS electronic_invoices (
  id VARCHAR(36) NOT NULL,
  invoice_number VARCHAR(20),
  access_key VARCHAR(49),
  issue_date DATETIME,
  issuer_ruc VARCHAR(13),
  buyer_identification VARCHAR(20),
  subtotal DECIMAL(10,2),
  iva_total DECIMAL(10,2),
  total DECIMAL(10,2),
  status VARCHAR(30),
  xml_content MEDIUMTEXT,
  sri_response TEXT,
  created_date DATETIME,
  updated_date DATETIME,
  document_type VARCHAR(2) DEFAULT '01',
  modified_document_number VARCHAR(20),
  modification_reason VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ====================================================================
-- Datos iniciales mínimos para que el sistema arranque
-- ====================================================================

-- Insertar registro en systemproperties para país Ecuador
INSERT IGNORE INTO systemproperties (id, name, constant, classname, uservalue) VALUES
  (UUID(), 'User Country', 'USERCOUNTRY', 'text', 'EC'),
  (UUID(), 'User Language', 'USERLANGUAGE', 'text', 'es'),
  (UUID(), 'Currency', 'CURRENCY', 'text', 'USD'),
  (UUID(), 'Screen Mode', 'SCREENMODE', 'text', 'window'),
  (UUID(), 'Look and Feel', 'LAF', 'text', 'javax.swing.plaf.metal.MetalLookAndFeel'),
  (UUID(), 'Icon Colour', 'ICONCOLOUR', 'text', ''),
  (UUID(), 'Sales Layout', 'SALESLAYOUT', 'text', '');

-- Insertar locación por defecto
INSERT IGNORE INTO locations (id, name, address) VALUES 
  ('00000000-0000-0000-0000-000000000000', 'Principal', 'Quito, Ecuador');

-- Insertar usuario administrador por defecto (sin contraseña)
INSERT IGNORE INTO people (id, name, apppassword, role, visible) VALUES 
  ('00000000-0000-0000-0000-000000000001', 'Administrador', NULL, 'administrator', TRUE);

-- Insertar impuesto IVA 12% Ecuador
INSERT IGNORE INTO taxes (id, name, rate, ratecascade, rateorder) VALUES
  ('00000000-0000-0000-0000-000000000010', 'IVA 12%', 0.12, FALSE, 1),
  ('00000000-0000-0000-0000-000000000011', 'IVA 0%', 0.00, FALSE, 2),
  ('00000000-0000-0000-0000-000000000012', 'IVA 15%', 0.15, FALSE, 3);

-- Cierre de caja inicial
INSERT IGNORE INTO closedcash (money, host, hostsequence, datestart) VALUES
  ('00000000-0000-0000-0000-000000000099', 'localhost', 1, NOW());

SELECT 'Base de datos ChromisPOS Ecuador inicializada correctamente!' as resultado;
