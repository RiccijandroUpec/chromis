package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceDetail;
import uk.chromis.pos.invoice.models.InvoiceStatus;
import uk.chromis.pos.invoice.utils.AccessKeyGenerator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio para generar el XML de factura electrónica según especificaciones SRI
 */
public class InvoiceXMLGenerator {
    
    private static final String DOCUMENTO_TYPE_FACTURA = "01"; // 01 = Factura
    private static final String DOCUMENTO_TYPE_NOTA_CREDITO = "04"; // 04 = Nota de Crédito
    
    /**
     * Genera el XML de la factura electrónica
     */
    public String generateXML(ElectronicInvoice invoice) throws Exception {
        // Generar clave de acceso si no existe
        if (invoice.getAccessKey() == null || invoice.getAccessKey().isEmpty()) {
            generateAccessKey(invoice);
        }
        
        // Crear documento XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();
        
        // Elemento raíz
        String rootName = "04".equals(invoice.getDocumentType()) ? "notaCredito" : "factura";
        Element root = doc.createElement(rootName);
        root.setAttribute("version", "1.0.0");
        doc.appendChild(root);
        
        // Información general
        addInfoElement(doc, root, "infoTributaria", invoice);
        String infoName = "04".equals(invoice.getDocumentType()) ? "infoNotaCredito" : "infoFactura";
        addInfoElement(doc, root, infoName, invoice);
        addDetallesElement(doc, root, invoice);
        
        if ("01".equals(invoice.getDocumentType())) {
            // En la nota de crédito, los totales van dentro de infoNotaCredito, y no hay pagos
            addTotalesElement(doc, root, invoice);
            addPagosElement(doc, root, invoice);
        }
        
        // Convertir documento a String
        String xml = documentToString(doc);
        invoice.setXmlContent(xml);
        invoice.setStatus(InvoiceStatus.GENERATED);
        
        return xml;
    }
    
    /**
     * Genera la clave de acceso y la asigna a la factura
     */
    private void generateAccessKey(ElectronicInvoice invoice) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String date = invoice.getIssueDate().format(dateFormatter);
        
        String environment = "1";
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File f = new java.io.File("chromisposconfig.properties");
            if (f.exists()) {
                props.load(new java.io.FileInputStream(f));
                String envProp = props.getProperty("invoice.environment", "1");
                if ("production".equalsIgnoreCase(envProp) || "2".equals(envProp)) {
                    environment = "2";
                } else {
                    environment = "1";
                }
            }
        } catch (Exception e) {}

        String accessKey = AccessKeyGenerator.generateAccessKey(
            date,
            invoice.getDocumentType() != null ? invoice.getDocumentType() : DOCUMENTO_TYPE_FACTURA,
            invoice.getIssuer().getRuc(),
            environment, // 1 Pruebas, 2 Producción
            "001001", // Estab + PtoEmi
            invoice.getInvoiceNumber(),
            "12345678", // Código Numérico
            "1" // Tipo Emisión (1 Normal)
        );
        
        invoice.setAccessKey(accessKey);
    }
    
    /**
     * Agrega información tributaria
     */
    private void addInfoElement(Document doc, Element parent, String elementName, ElectronicInvoice invoice) {
        Element element = doc.createElement(elementName);
        
        if ("infoTributaria".equals(elementName)) {
            String ambiente = "1";
            try {
                Properties props = new Properties();
                File f = new File("chromisposconfig.properties");
                if (f.exists()) {
                    props.load(new FileInputStream(f));
                    String envProp = props.getProperty("invoice.environment", "1");
                    if ("production".equalsIgnoreCase(envProp) || "2".equals(envProp)) {
                        ambiente = "2";
                    } else {
                        ambiente = "1";
                    }
                }
            } catch (Exception e) {}
            
            addElement(doc, element, "ambiente", ambiente); // 1 = Pruebas, 2 = Producción
            addElement(doc, element, "tipoEmision", "1"); // 1 = Normal
            addElement(doc, element, "razonSocial", invoice.getIssuer().getBusinessName());
            addElement(doc, element, "nombreComercial", invoice.getIssuer().getTradeName());
            addElement(doc, element, "ruc", invoice.getIssuer().getRuc());
            addElement(doc, element, "claveAcceso", invoice.getAccessKey());
            addElement(doc, element, "codDoc", invoice.getDocumentType() != null ? invoice.getDocumentType() : DOCUMENTO_TYPE_FACTURA);
            addElement(doc, element, "estab", "001");
            addElement(doc, element, "ptoEmi", "001");
            addElement(doc, element, "secuencial", invoice.getInvoiceNumber());
            addElement(doc, element, "dirMatriz", invoice.getIssuer().getAddress());
        } else if ("infoFactura".equals(elementName)) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            addElement(doc, element, "fechaEmision", invoice.getIssueDate().format(dateFormatter));
            addElement(doc, element, "dirEstablecimiento", invoice.getIssuer().getAddress());
            addElement(doc, element, "obligadoContabilidad", "SI");
            addElement(doc, element, "tipoIdentificacionComprador", invoice.getBuyer().getIdentificationType());
            addElement(doc, element, "razonSocialComprador", invoice.getBuyer().getBusinessName());
            addElement(doc, element, "identificacionComprador", invoice.getBuyer().getIdentification());
            addElement(doc, element, "totalSinImpuestos", formatAmount(invoice.getSubtotal()));
            addElement(doc, element, "totalDescuento", "0.00");
            
            // totals element here for factura
            Element totales = doc.createElement("totalConImpuestos");
            Element totalImpuesto = doc.createElement("totalImpuesto");
            addElement(doc, totalImpuesto, "codigo", "2"); // 2 = IVA
            addElement(doc, totalImpuesto, "codigoPorcentaje", "3"); // 3 = 12%
            addElement(doc, totalImpuesto, "baseImponible", formatAmount(invoice.getSubtotal()));
            addElement(doc, totalImpuesto, "valor", formatAmount(invoice.getIvaTotal()));
            totales.appendChild(totalImpuesto);
            element.appendChild(totales);
            
            addElement(doc, element, "propina", "0.00");
            addElement(doc, element, "importeTotal", formatAmount(invoice.getTotal()));
            addElement(doc, element, "moneda", "USD");
        } else if ("infoNotaCredito".equals(elementName)) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            addElement(doc, element, "fechaEmision", invoice.getIssueDate().format(dateFormatter));
            addElement(doc, element, "dirEstablecimiento", invoice.getIssuer().getAddress());
            addElement(doc, element, "tipoIdentificacionComprador", invoice.getBuyer().getIdentificationType());
            addElement(doc, element, "razonSocialComprador", invoice.getBuyer().getBusinessName());
            addElement(doc, element, "identificacionComprador", invoice.getBuyer().getIdentification());
            addElement(doc, element, "obligadoContabilidad", "SI");
            addElement(doc, element, "codDocModificado", "01"); // Siempre anula facturas
            addElement(doc, element, "numDocModificado", "001-001-" + invoice.getModifiedDocumentNumber()); // Formato serie-estab-secuencial
            addElement(doc, element, "fechaEmisionDocSustento", invoice.getModifiedDocumentIssueDate() != null ? invoice.getModifiedDocumentIssueDate().format(dateFormatter) : invoice.getIssueDate().format(dateFormatter));
            addElement(doc, element, "totalSinImpuestos", formatAmount(invoice.getSubtotal()));
            addElement(doc, element, "valorModificacion", formatAmount(invoice.getTotal()));
            addElement(doc, element, "moneda", "USD");
            
            Element totales = doc.createElement("totalConImpuestos");
            Element totalImpuesto = doc.createElement("totalImpuesto");
            addElement(doc, totalImpuesto, "codigo", "2"); // 2 = IVA
            addElement(doc, totalImpuesto, "codigoPorcentaje", "3"); // 3 = 12%
            addElement(doc, totalImpuesto, "baseImponible", formatAmount(invoice.getSubtotal()));
            addElement(doc, totalImpuesto, "valor", formatAmount(invoice.getIvaTotal()));
            totales.appendChild(totalImpuesto);
            element.appendChild(totales);
            
            addElement(doc, element, "motivo", invoice.getModificationReason() != null ? invoice.getModificationReason() : "Anulación de Factura");
        }
        
        parent.appendChild(element);
    }
    
    /**
     * Agrega detalles de los productos
     */
    private void addDetallesElement(Document doc, Element parent, ElectronicInvoice invoice) {
        Element detalles = doc.createElement("detalles");
        
        for (InvoiceDetail detail : invoice.getDetails()) {
            Element detalle = doc.createElement("detalle");
            
            addElement(doc, detalle, "codigoInterno", detail.getCode());
            addElement(doc, detalle, "descripcion", detail.getDescription());
            addElement(doc, detalle, "cantidad", detail.getQuantity().toString());
            addElement(doc, detalle, "precioUnitario", formatAmount(detail.getUnitPrice()));
            addElement(doc, detalle, "descuento", formatAmount(detail.getDiscount()));
            addElement(doc, detalle, "precioTotalSinImpuesto", formatAmount(detail.getLineTotal()));
            
            // Impuestos
            Element impuestos = doc.createElement("impuestos");
            Element impuesto = doc.createElement("impuesto");
            addElement(doc, impuesto, "codigo", detail.getTaxCode());
            addElement(doc, impuesto, "codigoPorcentaje", getTaxRateCode(detail.getTaxRate()));
            addElement(doc, impuesto, "baseImponible", formatAmount(detail.getLineTotal()));
            addElement(doc, impuesto, "porcentaje", detail.getTaxRate().toString());
            addElement(doc, impuesto, "valor", calculateTaxAmount(detail.getLineTotal(), detail.getTaxRate()).toString());
            impuestos.appendChild(impuesto);
            detalle.appendChild(impuestos);
            
            detalles.appendChild(detalle);
        }
        
        parent.appendChild(detalles);
    }
    
    /**
     * Agrega totales (Sólo para Factura, ya no se usa, lo metimos dentro de infoFactura)
     */
    private void addTotalesElement(Document doc, Element parent, ElectronicInvoice invoice) {
        // Obsoleto: movido a infoFactura
    }
    
    /**
     * Agrega información de pagos
     */
    private void addPagosElement(Document doc, Element parent, ElectronicInvoice invoice) {
        Element pagos = doc.createElement("pagos");
        
        invoice.getPaymentMethods().forEach(payment -> {
            Element pago = doc.createElement("pago");
            addElement(doc, pago, "formaPago", payment.getCode());
            addElement(doc, pago, "total", formatAmount(payment.getAmount()));
            pagos.appendChild(pago);
        });
        
        parent.appendChild(pagos);
    }
    
    /**
     * Agrega un elemento simple con texto
     */
    private void addElement(Document doc, Element parent, String name, String value) {
        Element element = doc.createElement(name);
        element.setTextContent(value != null ? value : "");
        parent.appendChild(element);
    }
    
    /**
     * Convierte un documento XML a String
     */
    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        
        return writer.getBuffer().toString();
    }
    
    /**
     * Formatea un monto decimal
     */
    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return String.format("%.2f", amount);
    }
    
    /**
     * Calcula el monto de impuesto
     */
    private BigDecimal calculateTaxAmount(BigDecimal base, BigDecimal rate) {
        if (base == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return base.multiply(rate).divide(new BigDecimal("100"));
    }
    
    /**
     * Obtiene el código de porcentaje de IVA según la tasa
     */
    private String getTaxRateCode(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            return "0"; // 0%
        } else if (rate.compareTo(new BigDecimal("5")) == 0) {
            return "2"; // 5%
        } else if (rate.compareTo(new BigDecimal("12")) == 0) {
            return "3"; // 12%
        }
        return "3"; // Por defecto 12%
    }
}
