package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceDetail;
import uk.chromis.pos.invoice.models.PaymentMethod;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Servicio para generar el RIDE (Representación Impresa) en formato PDF oficial.
 * Utiliza Apache PDFBox para dibujar un comprobante profesional y estructurado de Ecuador.
 */
public class RideGeneratorService {
    
    private String outputDirectory = "facturas";
    
    public RideGeneratorService() {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        uk.chromis.pos.invoice.utils.FileSecurity.restrictToOwner(dir);
    }
    
    /**
     * Genera la representación impresa (RIDE) en formato PDF
     * @param invoice Objeto de factura electrónica
     * @return Ruta completa al archivo RIDE PDF generado
     */
    public String generateRide(ElectronicInvoice invoice) throws Exception {
        String filename = "RIDE_" + invoice.getInvoiceNumber() + "_" + invoice.getAccessKey() + ".pdf";
        File file = new File(outputDirectory, filename);
        
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String formattedDate = invoice.getIssueDate().format(df);
        
        String tipoDoc = "04".equals(invoice.getDocumentType()) ? "NOTA DE CRÉDITO" : "FACTURA";
        
        // Cargar ambiente
        String environment = "PRUEBAS";
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File f = new java.io.File("chromisposconfig.properties");
            if (f.exists()) {
                props.load(new java.io.FileInputStream(f));
                String envVal = props.getProperty("invoice.environment", "1");
                if ("2".equals(envVal)) {
                    environment = "PRODUCCIÓN";
                }
            }
        } catch (Exception e) {
            System.err.println("Advertencia: no se pudo leer invoice.environment, asumiendo ambiente de pruebas: " + e.getMessage());
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                
                // Marca de agua si es ambiente de pruebas
                if ("PRUEBAS".equals(environment)) {
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA_BOLD, 42);
                    content.setNonStrokingColor(240, 200, 200); // Color rojo claro
                    // Rotar y colocar marca de agua
                    content.setTextMatrix(org.apache.pdfbox.util.Matrix.getRotateInstance(Math.toRadians(30), 100, 250));
                    content.showText("PRUEBAS - SIN VALIDEZ TRIBUTARIA");
                    content.endText();
                    content.setNonStrokingColor(0, 0, 0); // Restaurar a negro
                }
                
                // DIBUJAR BLOQUE IZQUIERDO: Datos del Emisor (x: 30 a 290, y: 750 a 600)
                content.addRect(30, 600, 260, 150);
                content.stroke();
                
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.newLineAtOffset(40, 730);
                content.showText(truncateString(invoice.getIssuer().getBusinessName(), 30));
                
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(0, -15);
                content.showText("Nombre Comercial: " + truncateString(invoice.getIssuer().getTradeName(), 32));
                
                content.newLineAtOffset(0, -15);
                content.showText("Dirección Matriz:");
                content.newLineAtOffset(0, -10);
                content.showText(truncateString(invoice.getIssuer().getAddress(), 45));
                
                content.newLineAtOffset(0, -15);
                content.showText("Dirección Sucursal:");
                content.newLineAtOffset(0, -10);
                content.showText(truncateString(invoice.getIssuer().getAddress(), 45));
                
                content.newLineAtOffset(0, -15);
                content.showText("Obligado a llevar contabilidad: SI");
                content.endText();
                
                // DIBUJAR BLOQUE DERECHO: SRI e Info del Documento (x: 310 a 580, y: 750 a 510)
                content.addRect(310, 520, 270, 230);
                content.stroke();
                
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.newLineAtOffset(320, 730);
                content.showText("R.U.C.: " + invoice.getIssuer().getRuc());
                
                content.setFont(PDType1Font.HELVETICA_BOLD, 10);
                content.newLineAtOffset(0, -20);
                content.showText(tipoDoc);
                
                content.setFont(PDType1Font.HELVETICA, 9);
                content.newLineAtOffset(0, -15);
                content.showText("No. " + invoice.getInvoiceNumber());
                
                content.newLineAtOffset(0, -20);
                content.showText("NÚMERO DE AUTORIZACIÓN:");
                content.setFont(PDType1Font.HELVETICA, 7);
                content.newLineAtOffset(0, -10);
                String authNum = invoice.getAuthorizationNumber() != null ? invoice.getAuthorizationNumber() : "PENDIENTE";
                content.showText(authNum);
                
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(0, -15);
                String authDateStr = invoice.getAuthorizationDate() != null ? invoice.getAuthorizationDate() : formattedDate;
                content.showText("FECHA Y HORA AUTORIZACIÓN: " + authDateStr);
                
                content.newLineAtOffset(0, -15);
                content.showText("AMBIENTE: " + environment);
                
                content.newLineAtOffset(0, -15);
                content.showText("EMISIÓN: NORMAL");
                
                content.newLineAtOffset(0, -15);
                content.showText("CLAVE DE ACCESO:");
                content.endText();
                
                // Clave de acceso en monospace centrado/pequeño
                content.beginText();
                content.setFont(PDType1Font.COURIER, 7);
                content.newLineAtOffset(320, 535);
                content.showText(invoice.getAccessKey());
                content.endText();
                
                // DIBUJAR BLOQUE COMPRADOR (x: 30 a 580, y: 440 a 500)
                content.addRect(30, 440, 550, 65);
                content.stroke();
                
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                content.newLineAtOffset(40, 490);
                content.showText("Razon Social / Nombres y Apellidos: ");
                content.setFont(PDType1Font.HELVETICA, 8);
                content.showText(invoice.getBuyer().getBusinessName());
                
                content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                content.newLineAtOffset(0, -15);
                content.showText("Identificación (RUC/Cédula): ");
                content.setFont(PDType1Font.HELVETICA, 8);
                content.showText(invoice.getBuyer().getIdentification());
                
                content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                content.newLineAtOffset(0, -15);
                content.showText("Fecha Emisión: ");
                content.setFont(PDType1Font.HELVETICA, 8);
                content.showText(formattedDate);
                
                if ("04".equals(invoice.getDocumentType())) {
                    content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                    content.newLineAtOffset(250, 30);
                    content.showText("Doc. Modificado: ");
                    content.setFont(PDType1Font.HELVETICA, 8);
                    content.showText("Factura No. " + invoice.getModifiedDocumentNumber());
                    
                    content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                    content.newLineAtOffset(0, -15);
                    content.showText("Motivo: ");
                    content.setFont(PDType1Font.HELVETICA, 8);
                    content.showText(invoice.getModificationReason());
                }
                content.endText();
                
                // DIBUJAR TABLA DE DETALLES
                float tableY = 410;
                content.setLineWidth(1f);
                
                // Encabezados
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 8);
                content.newLineAtOffset(40, tableY - 10);
                content.showText("Cod.");
                content.newLineAtOffset(60, 0);
                content.showText("Descripción");
                content.newLineAtOffset(200, 0);
                content.showText("Cant.");
                content.newLineAtOffset(50, 0);
                content.showText("P. Unit");
                content.newLineAtOffset(60, 0);
                content.showText("Desc.");
                content.newLineAtOffset(60, 0);
                content.showText("Precio Total");
                content.endText();
                
                // Línea de cabecera de la tabla
                content.moveTo(30, tableY);
                content.lineTo(580, tableY);
                content.moveTo(30, tableY - 15);
                content.lineTo(580, tableY - 15);
                content.stroke();
                
                float currentY = tableY - 15;
                for (InvoiceDetail detail : invoice.getDetails()) {
                    currentY -= 15;
                    
                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 8);
                    content.newLineAtOffset(40, currentY + 3);
                    content.showText(detail.getCode());
                    content.newLineAtOffset(60, 0);
                    content.showText(truncateString(detail.getDescription(), 38));
                    content.newLineAtOffset(200, 0);
                    content.showText(detail.getQuantity().toString());
                    
                    // Alinear números a la derecha simulado
                    content.newLineAtOffset(50, 0);
                    content.showText("$" + String.format("%.2f", detail.getUnitPrice()));
                    content.newLineAtOffset(60, 0);
                    content.showText("$" + String.format("%.2f", detail.getDiscount()));
                    content.newLineAtOffset(60, 0);
                    content.showText("$" + String.format("%.2f", detail.getLineTotal()));
                    content.endText();
                    
                    content.moveTo(30, currentY);
                    content.lineTo(580, currentY);
                    content.stroke();
                }
                
                // Línea vertical izquierda y derecha de la tabla
                content.moveTo(30, tableY);
                content.lineTo(30, currentY);
                content.moveTo(580, tableY);
                content.lineTo(580, currentY);
                content.stroke();
                
                // SECCIÓN INFERIOR: Info Adicional y Totales (y: currentY - 20)
                float bottomY = currentY - 15;
                
                // Cuadro Info Adicional (x: 30 a 290)
                content.addRect(30, bottomY - 130, 260, 120);
                content.stroke();
                
                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 9);
                content.newLineAtOffset(40, bottomY - 15);
                content.showText("Información Adicional");
                
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(0, -20);
                content.showText("Email: " + truncateString(invoice.getBuyer().getEmail(), 35));
                content.newLineAtOffset(0, -15);
                content.showText("Impreso por: ChromisPOS Ecuador");
                
                // Mostrar formas de pago si existen
                if ("01".equals(invoice.getDocumentType()) && invoice.getPaymentMethods() != null && !invoice.getPaymentMethods().isEmpty()) {
                    content.newLineAtOffset(0, -20);
                    content.setFont(PDType1Font.HELVETICA_BOLD, 7);
                    content.showText("FORMA DE PAGO");
                    content.setFont(PDType1Font.HELVETICA, 7);
                    for (PaymentMethod payment : invoice.getPaymentMethods()) {
                        content.newLineAtOffset(0, -12);
                        content.showText(getFriendlyPaymentMethodName(payment.getCode()) + ": $" + String.format("%.2f", payment.getAmount()));
                    }
                }
                content.endText();
                
                // Tabla de Totales (x: 310 a 580)
                content.addRect(310, bottomY - 110, 270, 100);
                content.stroke();
                
                BigDecimal subtotal = invoice.getSubtotal() != null ? invoice.getSubtotal() : BigDecimal.ZERO;
                BigDecimal iva = invoice.getIvaTotal() != null ? invoice.getIvaTotal() : BigDecimal.ZERO;
                BigDecimal total = invoice.getTotal() != null ? invoice.getTotal() : BigDecimal.ZERO;
                
                float totalY = bottomY - 5;
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(320, totalY - 15);
                content.showText("SUBTOTAL Gravado");
                content.newLineAtOffset(160, 0);
                content.showText("$ " + String.format("%.2f", subtotal));
                
                content.setFont(PDType1Font.HELVETICA, 8);
                content.newLineAtOffset(-160, -15);
                content.showText("SUBTOTAL 0%");
                content.newLineAtOffset(160, 0);
                content.showText("$ 0.00");
                
                content.newLineAtOffset(-160, -15);
                content.showText("SUBTOTAL Sin Impuestos");
                content.newLineAtOffset(160, 0);
                content.showText("$ " + String.format("%.2f", subtotal));
                
                content.newLineAtOffset(-160, -15);
                content.showText("VALOR IVA");
                content.newLineAtOffset(160, 0);
                content.showText("$ " + String.format("%.2f", iva));
                
                content.setFont(PDType1Font.HELVETICA_BOLD, 10);
                content.newLineAtOffset(-160, -20);
                content.showText("VALOR TOTAL");
                content.newLineAtOffset(160, 0);
                content.showText("$ " + String.format("%.2f", total));
                content.endText();
            }
            
            document.save(file);
        }
        uk.chromis.pos.invoice.utils.FileSecurity.restrictToOwner(file);

        return file.getAbsolutePath();
    }
    
    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
    
    private String getFriendlyPaymentMethodName(String code) {
        switch (code) {
            case "01": return "EFECTIVO";
            case "16": return "TARJETA DÉBITO";
            case "17": return "TARJETA CRÉDITO";
            case "19": return "CHEQUE";
            case "20": return "TRANS. BANCARIA";
            case "21": return "BILLETERA DIGITAL";
            default: return "OTROS SIST. FINANC.";
        }
    }
    
    public String getOutputDirectory() {
        return outputDirectory;
    }
    
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
