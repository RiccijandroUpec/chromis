package uk.chromis.pos.invoice.services;

import uk.chromis.pos.invoice.models.ElectronicInvoice;
import uk.chromis.pos.invoice.models.InvoiceStatus;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Servicio para enviar facturas al SRI
 * Maneja la comunicación con el servicio web del SRI
 */
public class SRIIntegrationService {
    
    private String sriWebServiceUrl;
    private boolean productionEnvironment;
    private int connectionTimeout = 30000; // 30 segundos
    private int readTimeout = 30000; // 30 segundos
    
    public SRIIntegrationService(boolean productionEnvironment) {
        this.productionEnvironment = productionEnvironment;
        this.sriWebServiceUrl = productionEnvironment ? 
            "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes" :
            "https://celcert.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    }
    
    /**
     * Envía la factura al SRI
     */
    public boolean sendInvoiceToSRI(ElectronicInvoice invoice) throws Exception {
        if (invoice.getSignedXmlContent() == null || invoice.getSignedXmlContent().isEmpty()) {
            throw new IllegalArgumentException("Invoice must be signed before sending to SRI");
        }
        
        try {
            // Codificar XML en Base64 para envío
            String xmlBase64 = Base64.getEncoder().encodeToString(
                invoice.getSignedXmlContent().getBytes("UTF-8")
            );
            
            // Construir SOAP request
            String soapRequest = buildSoapRequest(xmlBase64, invoice.getAccessKey());
            
            // Enviar al SRI
            String response = sendSoapRequest(soapRequest);
            
            // Procesar respuesta
            boolean success = processSRIResponse(response, invoice);
            
            invoice.setStatus(success ? InvoiceStatus.SENT_TO_SRI : InvoiceStatus.REJECTED);
            invoice.setSentToSRI(success);
            invoice.setSentDate(LocalDateTime.now());
            invoice.setSriResponse(response);
            
            return success;
        } catch (Exception e) {
            invoice.setStatus(InvoiceStatus.REJECTED);
            invoice.setSriResponse("Error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Construye el SOAP request para enviar al SRI
     */
    private String buildSoapRequest(String xmlBase64, String accessKey) {
        StringBuilder soap = new StringBuilder();
        soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        soap.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        soap.append("xmlns:ws=\"http://ec.gob.sri.ws.impuestos\">\n");
        soap.append("  <soap:Body>\n");
        soap.append("    <ws:validarComprobante>\n");
        soap.append("      <xml>").append(xmlBase64).append("</xml>\n");
        soap.append("    </ws:validarComprobante>\n");
        soap.append("  </soap:Body>\n");
        soap.append("</soap:Envelope>\n");
        
        return soap.toString();
    }
    
    /**
     * Envía el SOAP request al SRI
     */
    private String sendSoapRequest(String soapRequest) throws Exception {
        URL url = new URL(sriWebServiceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            // Configurar conexión
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            connection.setRequestProperty("SOAPAction", "");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            
            // Enviar request
            byte[] requestBytes = soapRequest.getBytes("UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(requestBytes.length));
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBytes);
                os.flush();
            }
            
            // Leer response
            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode >= 400) ? 
                connection.getErrorStream() : connection.getInputStream();
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\n");
                }
            }
            
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Procesa la respuesta del SRI
     */
    private boolean processSRIResponse(String response, ElectronicInvoice invoice) {
        // Buscar estado de validación en la respuesta
        if (response.contains("estado>RECIBIDA") || response.contains("estado>AUTORIZADA")) {
            invoice.setStatus(InvoiceStatus.AUTHORIZED);
            
            // Extraer número de autorización si está disponible
            int authStart = response.indexOf("numeroAutorizacion>");
            if (authStart > -1) {
                int authEnd = response.indexOf("<", authStart + 18);
                String authNumber = response.substring(authStart + 18, authEnd);
                invoice.setAuthorizationNumber(authNumber);
            }
            
            return true;
        } else if (response.contains("estado>RECHAZADA") || response.contains("estado>ERROR")) {
            invoice.setStatus(InvoiceStatus.REJECTED);
            return false;
        }
        
        // Si no hay estado claro, considerarlo como pendiente
        invoice.setStatus(InvoiceStatus.SENT_TO_SRI);
        return true;
    }
    
    /**
     * Consulta el estado de autorización en el SRI
     */
    public String queryAuthorizationStatus(String accessKey) throws Exception {
        // Construir SOAP request para consulta
        StringBuilder soap = new StringBuilder();
        soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        soap.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        soap.append("xmlns:ws=\"http://ec.gob.sri.ws.impuestos\">\n");
        soap.append("  <soap:Body>\n");
        soap.append("    <ws:autorizacionComprobante>\n");
        soap.append("      <claveAcceso>").append(accessKey).append("</claveAcceso>\n");
        soap.append("    </ws:autorizacionComprobante>\n");
        soap.append("  </soap:Body>\n");
        soap.append("</soap:Envelope>\n");
        
        return sendSoapRequest(soap.toString());
    }
    
    /**
     * Descarga el XML autorizado desde el SRI
     */
    public String downloadAuthorizedXml(String accessKey) throws Exception {
        String response = queryAuthorizationStatus(accessKey);
        
        // Extraer comprobante del response (Base64)
        int comprobantStart = response.indexOf("comprobante>");
        if (comprobantStart > -1) {
            int comprobanteEnd = response.indexOf("<", comprobantStart + 12);
            String comprobanteBase64 = response.substring(comprobantStart + 12, comprobanteEnd);
            
            // Decodificar Base64
            byte[] decodedBytes = Base64.getDecoder().decode(comprobanteBase64);
            return new String(decodedBytes, "UTF-8");
        }
        
        return null;
    }
    
    /**
     * Obtiene la respuesta del SRI para una factura
     */
    public String getSRIResponse(ElectronicInvoice invoice) {
        return invoice.getSriResponse();
    }
    
    /**
     * Establece la respuesta del SRI
     */
    public void setSRIResponse(ElectronicInvoice invoice, String response) {
        invoice.setSriResponse(response);
    }
    
    /**
     * Valida si la factura fue autorizada por el SRI
     */
    public boolean isAuthorized(ElectronicInvoice invoice) {
        return InvoiceStatus.AUTHORIZED.equals(invoice.getStatus());
    }
    
    /**
     * Obtiene la URL del web service del SRI
     */
    public String getSRIWebServiceUrl() {
        return sriWebServiceUrl;
    }
    
    /**
     * Establece la URL del web service del SRI
     */
    public void setSRIWebServiceUrl(String sriWebServiceUrl) {
        this.sriWebServiceUrl = sriWebServiceUrl;
    }
    
    /**
     * Verifica si está en ambiente de producción
     */
    public boolean isProductionEnvironment() {
        return productionEnvironment;
    }
    
    /**
     * Establece el ambiente
     */
    public void setProductionEnvironment(boolean productionEnvironment) {
        this.productionEnvironment = productionEnvironment;
        this.sriWebServiceUrl = productionEnvironment ? 
            "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes" :
            "https://celcert.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    }
    
    /**
     * Obtiene timeout de conexión
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    /**
     * Establece timeout de conexión
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
}
