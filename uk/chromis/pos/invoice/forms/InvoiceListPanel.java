package uk.chromis.pos.invoice.forms;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceStatus;
import uk.chromis.pos.invoice.services.ElectronicInvoiceService;
import uk.chromis.pos.invoice.dao.ElectronicInvoiceDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import uk.chromis.basic.BasicException;
import uk.chromis.pos.forms.AppView;
import uk.chromis.pos.forms.BeanFactoryApp;
import uk.chromis.pos.forms.BeanFactoryException;
import uk.chromis.pos.forms.JPanelView;

public class InvoiceListPanel extends JPanel implements JPanelView, BeanFactoryApp {
    
    private AppView m_App;
    private ElectronicInvoiceService invoiceService;
    private ElectronicInvoiceDAO dao;
    private Connection connection;
    
    private JTable invoicesTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> statusFilter;
    private JButton refreshButton;
    private JButton viewDetailsButton;
    private JButton downloadButton;
    private JButton cancelButton;
    
    private List<ElectronicInvoice> currentInvoices;
    
    public InvoiceListPanel() {
        initComponents();
    }
    
    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        try {
            // Conectar a DB
            Properties props = new Properties();
            File f = new File("chromisposconfig.properties");
            if (f.exists()) {
                props.load(new FileInputStream(f));
            }
            
            String url = "jdbc:mysql://" + props.getProperty("database.server", "localhost") + ":" + 
                         props.getProperty("database.port", "3306") + "/" + 
                         props.getProperty("database.name", "chromispos") + "?useSSL=false&allowPublicKeyRetrieval=true";
            String user = props.getProperty("database.user", "root");
            String pass = props.getProperty("database.password", "");
            
            connection = DriverManager.getConnection(url, user, pass);
            dao = new ElectronicInvoiceDAO(connection);
            
            // Inicializar servicio
            invoiceService = new ElectronicInvoiceService();
            String certPath = props.getProperty("invoice.certificate.path", "");
            String certPass = props.getProperty("invoice.certificate.password", "");
            boolean production = "2".equals(props.getProperty("invoice.environment", "1"));
            invoiceService.initialize(certPath, certPass, production);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error conectando a BD: " + e.getMessage());
        }
        layoutComponents();
    }

    @Override
    public String getTitle() {
        return "Reportes SRI Ecuador";
    }

    @Override
    public void activate() throws BasicException {
        loadInvoices();
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
    
    private void initComponents() {
        statusFilter = new JComboBox<>(new String[]{
            "Todos", "01 - Facturas", "04 - Notas de Crédito"
        });
        
        refreshButton = new JButton("Actualizar");
        viewDetailsButton = new JButton("Ver Detalles");
        downloadButton = new JButton("Descargar XML (SRI)");
        cancelButton = new JButton("Anular (Generar N/C)");
        
        String[] columnNames = {"Tipo", "Número", "Clave Acceso", "Fecha", "Total", "Estado SRI", "Doc Modificado"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoicesTable = new JTable(tableModel);
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        refreshButton.addActionListener(e -> loadInvoices());
        viewDetailsButton.addActionListener(e -> viewDetails());
        downloadButton.addActionListener(e -> downloadXML());
        cancelButton.addActionListener(e -> cancelInvoice());
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Mostrar:"));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);
        
        add(new JScrollPane(invoicesTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadInvoices() {
        if (dao == null) return;
        tableModel.setRowCount(0);
        
        try {
            currentInvoices = dao.getAllInvoices();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (ElectronicInvoice inv : currentInvoices) {
                String filter = (String) statusFilter.getSelectedItem();
                if (filter.contains("01") && !"01".equals(inv.getDocumentType())) continue;
                if (filter.contains("04") && !"04".equals(inv.getDocumentType())) continue;
                
                Vector<Object> row = new Vector<>();
                row.add("04".equals(inv.getDocumentType()) ? "Nota de Crédito" : "Factura");
                row.add(inv.getInvoiceNumber());
                row.add(inv.getAccessKey());
                row.add(inv.getIssueDate().format(formatter));
                row.add("$" + inv.getTotal());
                row.add(inv.getStatus().getDisplayName());
                row.add(inv.getModifiedDocumentNumber() != null ? inv.getModifiedDocumentNumber() : "-");
                
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error cargando facturas: " + e.getMessage());
        }
    }
    
    private void viewDetails() {
        int row = invoicesTable.getSelectedRow();
        if (row < 0) return;
        ElectronicInvoice inv = currentInvoices.get(row);
        JOptionPane.showMessageDialog(this, 
            "Tipo: " + ("04".equals(inv.getDocumentType()) ? "Nota de Crédito" : "Factura") + "\n" +
            "Número: " + inv.getInvoiceNumber() + "\n" +
            "Clave Acceso: " + inv.getAccessKey() + "\n" +
            "Total: $" + inv.getTotal() + "\n" +
            "Estado: " + inv.getStatus().getDisplayName() + "\n\n" +
            "Respuesta SRI:\n" + inv.getSriResponse(), 
            "Detalles", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void downloadXML() {
        int row = invoicesTable.getSelectedRow();
        if (row < 0) return;
        ElectronicInvoice inv = currentInvoices.get(row);
        
        if (inv.getStatus() != InvoiceStatus.AUTHORIZED) {
            JOptionPane.showMessageDialog(this, "Solo se pueden descargar facturas AUTORIZADAS por el SRI.");
            return;
        }
        
        try {
            String xml = invoiceService.getSRIService().downloadAuthorizedXml(inv.getAccessKey());
            if (xml != null && !xml.isEmpty()) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(inv.getAccessKey() + ".xml"));
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    java.nio.file.Files.write(fileChooser.getSelectedFile().toPath(), xml.getBytes("UTF-8"));
                    JOptionPane.showMessageDialog(this, "XML Descargado exitosamente.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo descargar el XML desde el SRI.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error descargando XML: " + e.getMessage());
        }
    }
    
    private void cancelInvoice() {
        int row = invoicesTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura a anular.");
            return;
        }
        ElectronicInvoice original = currentInvoices.get(row);
        
        if ("04".equals(original.getDocumentType())) {
            JOptionPane.showMessageDialog(this, "No puedes anular una Nota de Crédito.");
            return;
        }
        if (original.getStatus() != InvoiceStatus.AUTHORIZED) {
            JOptionPane.showMessageDialog(this, "Sólo puedes anular (generar NC) de facturas AUTORIZADAS. Si está rechazada o en borrador, ignórala.");
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, "Ingrese el motivo de anulación (Ej: Devolución):");
        if (reason == null || reason.trim().isEmpty()) return;
        
        try {
            // Crear Nota de Crédito clonando datos
            ElectronicInvoice nc = new ElectronicInvoice();
            nc.setId(java.util.UUID.randomUUID().toString());
            nc.setDocumentType("04");
            nc.setModifiedDocumentNumber(original.getInvoiceNumber());
            nc.setModifiedDocumentIssueDate(original.getIssueDate());
            nc.setModificationReason(reason);
            
            // Generar nuevo secuencial para NC (Para prueba asumiremos +1)
            // Normalmente debe haber una secuencia propia en DB para NC
            nc.setInvoiceNumber("0000000" + (int)(Math.random() * 90 + 10)); 
            
            nc.setIssueDate(LocalDateTime.now());
            nc.setIssuer(original.getIssuer());
            nc.setBuyer(original.getBuyer());
            nc.setSubtotal(original.getSubtotal());
            nc.setIvaTotal(original.getIvaTotal());
            nc.setTotal(original.getTotal());
            nc.setDetails(original.getDetails());
            // NC no tiene pagos
            
            // Procesar SRI
            invoiceService.processInvoice(nc);
            dao.insertInvoice(nc);
            
            JOptionPane.showMessageDialog(this, "Nota de Crédito generada y enviada al SRI con éxito.\nEstado: " + nc.getStatus().getDisplayName());
            loadInvoices();
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generando Nota de Crédito: " + e.getMessage());
        }
    }
}
