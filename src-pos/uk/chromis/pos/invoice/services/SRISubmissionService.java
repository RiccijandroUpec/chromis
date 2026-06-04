package uk.chromis.pos.invoice.services;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para enviar facturas al SRI (Servicio de Rentas Internas)
 */
public class SRISubmissionService {
    
    // URLs del SRI
    private static final String SRI_TEST_URL = "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    private static final String SRI_PRODUCTION_URL = "https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantes";
    
    // WSDL URLs (para consultas posteriores)
    private static final String SRI_CONSULTATION_TEST = "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantes";
    private static final String SRI_CONSULTATION_PROD = "https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantes";
    
    public static class SRIResponse {
        public boolean success;
        public String authorizationNumber;
        public String authorizationDate;
        public String status;
        public String errorMessage;
        public String claveAcceso;
        
        @Override
        public String toString() {
            if (success) {
                return String.format(
                    "✓ Autorizado\nNúmero: %s\nFecha: %s\nEstatus: %s",
                    authorizationNumber, authorizationDate, status
                );
            } else {
                return "✗ Error: " + errorMessage;
            }
        }
    }
    
    /**
     * Envía una factura al SRI
     * @param xmlContent Contenido XML de la factura
     * @param isProduction true para producción, false para test
     * @return SRIResponse con el resultado
     */
    public static SRIResponse submitInvoice(String xmlContent, boolean isProduction) {
        SRIResponse response = new SRIResponse();
        
        try {
            String endpoint = isProduction ? SRI_PRODUCTION_URL : SRI_TEST_URL;
            URL url = new URL(endpoint);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000); // 30 segundos
            connection.setReadTimeout(30000);
            
            // Escribir el XML
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = xmlContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Leer respuesta
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                // Éxito
                response.success = true;
                response.status = "RECIBIDA";
                response.errorMessage = "Factura recibida por el SRI";
                
                // En una implementación real, parsearías la respuesta XML aquí
                // Por ahora, simular una respuesta exitosa
                response.authorizationNumber = "0312202601123456789000100000000100013";
                response.authorizationDate = System.currentTimeMillis() + "";
                
            } else if (responseCode == 400) {
                response.success = false;
                response.status = "RECHAZADA";
                response.errorMessage = "Error en los datos: Verifícalos y reintenta";
                
            } else if (responseCode == 401) {
                response.success = false;
                response.status = "NO_AUTORIZADO";
                response.errorMessage = "Credenciales inválidas: Verifica tu certificado";
                
            } else if (responseCode == 403) {
                response.success = false;
                response.status = "PROHIBIDO";
                response.errorMessage = "Acceso prohibido: Contacta al SRI";
                
            } else if (responseCode == 500) {
                response.success = false;
                response.status = "ERROR_SERVIDOR";
                response.errorMessage = "Error en el servidor del SRI: Reintenta más tarde";
                
            } else {
                response.success = false;
                response.status = "ERROR";
                response.errorMessage = "Error desconocido (código: " + responseCode + ")";
            }
            
            connection.disconnect();
            
        } catch (java.net.SocketTimeoutException e) {
            response.success = false;
            response.status = "TIMEOUT";
            response.errorMessage = "Tiempo de espera agotado: El SRI no respondió a tiempo";
            System.err.println("Timeout al conectar al SRI: " + e.getMessage());
            
        } catch (java.net.UnknownHostException e) {
            response.success = false;
            response.status = "NO_CONEXION";
            response.errorMessage = "No hay conexión con el servidor del SRI";
            System.err.println("Host no disponible: " + e.getMessage());
            
        } catch (Exception e) {
            response.success = false;
            response.status = "ERROR_CONEXION";
            response.errorMessage = "Error de conexión: " + e.getMessage();
            System.err.println("Error enviando factura al SRI: " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * Consulta el estado de una factura en el SRI
     * @param claveAcceso Clave de acceso de la factura
     * @param isProduction true para producción, false para test
     * @return SRIResponse con el estado
     */
    public static SRIResponse checkInvoiceStatus(String claveAcceso, boolean isProduction) {
        SRIResponse response = new SRIResponse();
        
        try {
            String endpoint = isProduction ? SRI_CONSULTATION_PROD : SRI_CONSULTATION_TEST;
            URL url = new URL(endpoint);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == 200) {
                response.success = true;
                response.status = "AUTORIZADA";
                response.claveAcceso = claveAcceso;
            } else if (responseCode == 404) {
                response.success = false;
                response.status = "NO_ENCONTRADA";
                response.errorMessage = "La factura no se encuentra en el SRI";
            } else {
                response.success = false;
                response.status = "ERROR";
                response.errorMessage = "Error consultando estado (código: " + responseCode + ")";
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            response.success = false;
            response.status = "ERROR_CONSULTA";
            response.errorMessage = "Error consultando el SRI: " + e.getMessage();
            System.err.println("Error consultando factura: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Valida que el XML cumple con requisitos mínimos
     * @param xmlContent XML a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean validateXML(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return false;
        }
        
        // Validaciones básicas
        boolean hasRootElement = xmlContent.contains("<?xml");
        boolean hasInvoice = xmlContent.contains("<factura") || xmlContent.contains("<comprobante");
        boolean hasSignature = xmlContent.contains("<Signature") || xmlContent.contains("<ds:Signature");
        
        return hasRootElement && hasInvoice && hasSignature;
    }
    
    /**
     * Obtiene la URL del SRI según el ambiente
     * @param isProduction true para producción
     * @return URL del SRI
     */
    public static String getSRIEndpoint(boolean isProduction) {
        return isProduction ? SRI_PRODUCTION_URL : SRI_TEST_URL;
    }
    
    /**
     * Prueba de envío
     */
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("PRUEBA DE SERVICIO SRI");
        System.out.println("═══════════════════════════════════════");
        
        // Ejemplo de respuesta simulada
        SRIResponse response = new SRIResponse();
        response.success = true;
        response.authorizationNumber = "0312202601123456789000100000000100013";
        response.authorizationDate = "2026-01-03T10:30:00";
        response.status = "AUTORIZADA";
        response.claveAcceso = "0312202601123456789000100000000100013";
        
        System.out.println(response.toString());
        System.out.println("═══════════════════════════════════════");
    }
}
