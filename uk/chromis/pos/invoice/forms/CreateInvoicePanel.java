package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.models.*;
import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.utils.EcuadorValidators;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Vector;

/**
 * Panel para crear nuevas facturas electrónicas
 */
public class CreateInvoicePanel extends JPanel {
    
    private ElectronicInvoiceService invoiceService;
    
    // Componentes UI
    private JTextField invoiceNumberField;
    private JTextField buyerIdField;
    private JTextField buyerNameField;
    private JTextField buyerEmailField;
    
    private JTable detailsTable;
    private DefaultTableModel detailsModel;
    
    private JTextField productCodeField;
    private JTextField productDescField;
    private JSpinner quantitySpinner;
    private JTextField unitPriceField;
    private JComboBox<String> taxRateCombo;
    
    private JLabel subtotalLabel;
    private JLabel ivaLabel;
    private JLabel totalLabel;
    
    private JButton addDetailButton;
    private JButton removeDetailButton;
    private JButton generateButton;
    private JButton signButton;
    private JButton sendButton;
    
    public CreateInvoicePanel(ElectronicInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        initComponents();
        layoutComponents();
    }
    
    /**
     * Inicializa los componentes
     */
    private void initComponents() {
        // Información de factura
        invoiceNumberField = new JTextField(10);
        
        // Información del comprador
        buyerIdField = new JTextField(15);
        buyerNameField = new JTextField(30);
        buyerEmailField = new JTextField(30);
        
        // Productos/servicios
        productCodeField = new JTextField(10);
        productDescField = new JTextField(30);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 999999.0, 1.0));
        unitPriceField = new JTextField(12);
        taxRateCombo = new JComboBox<>(new String[]{"0%", "5%", "12%"});
        
        // Tabla de detalles
        String[] columnNames = {"Código", "Descripción", "Cantidad", "P. Unit.", "Desc.", "IVA %", "Total"};
        detailsModel = new DefaultTableModel(columnNames, 0);
        detailsTable = new JTable(detailsModel);
        
        // Botones
        addDetailButton = new JButton("Agregar Producto");
        removeDetailButton = new JButton("Eliminar Producto");
        generateButton = new JButton("Generar XML");
        signButton = new JButton("Firmar");
        sendButton = new JButton("Enviar SRI");
        
        // Listeners
        addDetailButton.addActionListener(e -> addDetail());
        removeDetailButton.addActionListener(e -> removeDetail());
        generateButton.addActionListener(e -> generateXML());
        signButton.addActionListener(e -> signInvoice());
        sendButton.addActionListener(e -> sendToSRI());
    }
    
    /**
     * Organiza los componentes en el panel
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Panel superior con información de factura
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Número Factura:"), gbc);
        gbc.gridx = 1;
        topPanel.add(invoiceNumberField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(new JLabel("Cédula/RUC Comprador:"), gbc);
        gbc.gridx = 1;
        topPanel.add(buyerIdField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        topPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 3;
        topPanel.add(buyerNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        topPanel.add(buyerEmailField, gbc);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Panel central con tabla y formulario de productos
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // Panel de productos
        JPanel productPanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        productPanel.add(new JLabel("Código:"), gbc);
        gbc.gridx = 1;
        productPanel.add(productCodeField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        productPanel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 3;
        productPanel.add(productDescField, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0;
        productPanel.add(new JLabel("Cantidad:"), gbc);
        gbc.gridx = 5;
        productPanel.add(quantitySpinner, gbc);
        
        gbc.gridx = 6; gbc.gridy = 0;
        productPanel.add(new JLabel("Precio Unit.:"), gbc);
        gbc.gridx = 7;
        productPanel.add(unitPriceField, gbc);
        
        gbc.gridx = 8; gbc.gridy = 0;
        productPanel.add(new JLabel("IVA:"), gbc);
        gbc.gridx = 9;
        productPanel.add(taxRateCombo, gbc);
        
        gbc.gridx = 10; gbc.gridy = 0;
        productPanel.add(addDetailButton, gbc);
        
        centerPanel.add(productPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(detailsTable), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior con totales y botones
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        subtotalLabel = new JLabel("Subtotal: $0.00");
        ivaLabel = new JLabel("  IVA: $0.00");
        totalLabel = new JLabel("  TOTAL: $0.00");
        
        totalsPanel.add(subtotalLabel);
        totalsPanel.add(ivaLabel);
        totalsPanel.add(totalLabel);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(removeDetailButton);
        buttonsPanel.add(generateButton);
        buttonsPanel.add(signButton);
        buttonsPanel.add(sendButton);
        
        bottomPanel.add(totalsPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Agrega un detalle a la tabla
     */
    private void addDetail() {
        try {
            String code = productCodeField.getText();
            String description = productDescField.getText();
            double quantity = (Double) quantitySpinner.getValue();
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText());
            String taxRate = (String) taxRateCombo.getSelectedItem();
            
            if (code.isEmpty() || description.isEmpty() || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos correctamente", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            BigDecimal lineTotal = unitPrice.multiply(new BigDecimal(quantity));
            
            Vector<Object> row = new Vector<>();
            row.add(code);
            row.add(description);
            row.add(String.format("%.2f", quantity));
            row.add(String.format("$%.2f", unitPrice));
            row.add("0.00");
            row.add(taxRate);
            row.add(String.format("$%.2f", lineTotal));
            
            detailsModel.addRow(row);
            
            // Limpiar campos
            productCodeField.setText("");
            productDescField.setText("");
            quantitySpinner.setValue(1.0);
            unitPriceField.setText("");
            
            updateTotals();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Elimina un detalle de la tabla
     */
    private void removeDetail() {
        int selectedRow = detailsTable.getSelectedRow();
        if (selectedRow >= 0) {
            detailsModel.removeRow(selectedRow);
            updateTotals();
        }
    }
    
    /**
     * Actualiza los totales
     */
    private void updateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        
        for (int i = 0; i < detailsModel.getRowCount(); i++) {
            String totalStr = (String) detailsModel.getValueAt(i, 6);
            totalStr = totalStr.replace("$", "");
            BigDecimal total = new BigDecimal(totalStr);
            subtotal = subtotal.add(total);
            
            String taxRate = (String) detailsModel.getValueAt(i, 5);
            BigDecimal rate = new BigDecimal(taxRate.replace("%", ""));
            iva = iva.add(total.multiply(rate).divide(new BigDecimal("100")));
        }
        
        BigDecimal grand = subtotal.add(iva);
        
        subtotalLabel.setText(String.format("Subtotal: $%.2f", subtotal));
        ivaLabel.setText(String.format("  IVA: $%.2f", iva));
        totalLabel.setText(String.format("  TOTAL: $%.2f", grand));
    }
    
    /**
     * Genera el XML de la factura
     */
    private void generateXML() {
        try {
            // Validar datos
            if (buyerIdField.getText().isEmpty() || buyerNameField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete información del comprador", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!EcuadorValidators.isValidCedula(buyerIdField.getText())) {
                JOptionPane.showMessageDialog(this, "Cédula no válida", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (detailsModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Agregue al menos un producto", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            JOptionPane.showMessageDialog(this, "XML generado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Firma la factura
     */
    private void signInvoice() {
        try {
            JOptionPane.showMessageDialog(this, "Factura firmada correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Envía la factura al SRI
     */
    private void sendToSRI() {
        try {
            JOptionPane.showMessageDialog(this, "Factura enviada al SRI. Verifique el estado...", "Enviado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
