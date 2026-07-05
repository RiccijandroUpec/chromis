-- ============================================================
-- ChromisPOS Ecuador - Actualizar Menu.Root en base de datos
-- Incluye: Ventas + Mantenimiento + Administración (SRI, usuarios,
-- roles y configuración general consolidados en un solo hub)
-- ============================================================
-- Ejecutar en la base de datos chromisnewtest (o la que uses)
-- ============================================================

-- Primero, eliminar el recurso Menu.Root existente (si existe)
DELETE FROM resources WHERE name = 'Menu.Root';

-- Insertar el nuevo Menu.Root con las secciones completas
INSERT INTO resources (id, name, restype, content) VALUES (
    'menu_root_001',
    'Menu.Root',
    0,
    '// MAIN - Ventas
group = menu.addGroup("menu.main");
        group.addPanel("sale.png", "menu.ticket", "uk.chromis.pos.sales.JPanelTicketSales");
        group.addPanel("saleedit.png", "menu.ticketEdit", "uk.chromis.pos.sales.JPanelTicketEdits");
        group.addPanel("customerpay.png", "menu.customerPayment", "uk.chromis.pos.customers.CustomersPayment");
        group.addPanel("payments.png", "menu.payments", "uk.chromis.pos.panels.JPanelPayments");
        group.addPanel("calculator.png", "menu.closeCash", "uk.chromis.pos.panels.JPanelCloseMoney");
        group.addPanel("printer.png", "menu.printer", "uk.chromis.pos.panels.JPanelPrinter");
        group.addPanel("timer.png", "menu.checkInCheckOut", "uk.chromis.pos.epm.JPanelEmployeePresence");

// MANTENIMIENTO E INVENTARIO
group = menu.addGroup("menu.maintenance");
        group.addPanel("products.png", "menu.products", "uk.chromis.pos.inventory.ProductsPanel");
        group.addPanel("products.png", "menu.categories", "uk.chromis.pos.inventory.CategoriesPanel");
        group.addPanel("products.png", "menu.taxcategories", "uk.chromis.pos.inventory.TaxCategoriesPanel");
        group.addPanel("products.png", "menu.taxes", "uk.chromis.pos.inventory.TaxesPanel");
        group.addPanel("user.png", "menu.customers", "uk.chromis.pos.customers.CustomersPanel");
        group.addPanel("resources.png", "menu.resources", "uk.chromis.pos.panels.JPanelResources");

// ADMINISTRACION
group = menu.addGroup("Administración");
        group.addPanel("config.png", "Administración", "uk.chromis.pos.panels.JPanelAdminCentral");'
);
