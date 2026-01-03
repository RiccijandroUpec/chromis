package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceStatus;
import uk.chromis.pos.invoice.services.ElectronicInvoiceService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

/**
 * Panel para consultar y gestionar facturas electrónicas
 */
public class InvoiceListPanel extends JPanel {
    
    private ElectronicInvoiceService invoiceService;
    
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> statusFilter;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton downloadButton;
    private JButton cancelButton;
    
    public InvoiceListPanel(ElectronicInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
        initComponents();
        layoutComponents();
    }
    
    /**
     * Inicializa los componentes
     */
    private void initComponents() {
        // Filtros
        statusFilter = new JComboBox<>(new String[]{
            "Todos",
            "Borrador",
            "Generada",
            "Firmada",
            "Enviada al SRI",
            "Autorizada",
            "Rechazada",
            "Cancelada"
        });
        
        refreshButton = new JButton("Actualizar");
        viewDetailsButton = new JButton("Ver Detalles");
        downloadButton = new JButton("Descargar XML");
        cancelButton = new JButton("Cancelar Factura");
        
        // Tabla
        String[] columnNames = {"Número", "Clave Acceso", "Fecha", "Total", "Estado", "Enviada SRI", "Número Auth."};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoicesTable = new JTable(tableModel);
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Listeners
        refreshButton.addActionListener(e -> loadInvoices());
        viewDetailsButton.addActionListener(e -> viewDetails());
        downloadButton.addActionListener(e -> downloadXML());
        cancelButton.addActionListener(e -> cancelInvoice());
        
        // Cargar datos
        loadInvoices();
    }
    
    /**
     * Organiza los componentes en el panel
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Panel de filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Estado:"));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);
        
        add(filterPanel, BorderLayout.NORTH);
        
        // Panel de tabla
        add(new JScrollPane(invoicesTable), BorderLayout.CENTER);
        
        // Panel de botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Carga las facturas
     */
    private void loadInvoices() {
        tableModel.setRowCount(0);
        
        // Aquí iría la llamada al servicio para obtener las facturas
        // Por ahora es un ejemplo
        Vector<Object> row = new Vector<>();
        row.add("000001");
        row.add("1234567890001012345678901234567890123456789");
        row.add("2024-01-03");
        row.add("$150.00");
        row.add("Autorizada");
        row.add("Sí");
        row.add("1234567890001");
        
        tableModel.addRow(row);
    }
    
    /**
     * Muestra los detalles de una factura
     */
    private void viewDetails() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String invoiceNumber = (String) tableModel.getValueAt(selectedRow, 0);
        JOptionPane.showMessageDialog(this, "Detalles de factura: " + invoiceNumber, "Detalles", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Descarga el XML de una factura
     */
    private void downloadXML() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "XML descargado", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Cancela una factura
     */
    private void cancelInvoice() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String status = (String) tableModel.getValueAt(selectedRow, 4);
        if ("Autorizada".equals(status)) {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Desea cancelar esta factura?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Factura cancelada", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Solo se pueden cancelar facturas autorizadas", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
