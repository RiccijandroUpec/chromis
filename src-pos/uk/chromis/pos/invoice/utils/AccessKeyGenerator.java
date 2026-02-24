package uk.chromis.pos.invoice.utils;

import java.math.BigInteger;

/**
 * Utilidad para generar claves de acceso SRI
 * Clave de acceso: 49 dígitos
 * Formato: DDMMYYYY(8) + tipoComprobante(2) + RUC(13) + tipoAmbiente(1) + serie(6) + secuencial(9) + codigoNumerico(8) + tipoEmision(1) + digitoVerificador(1)
 */
public class AccessKeyGenerator {
    
    /**
     * Genera la clave de acceso SRI de 49 dígitos
     * @param date fecha de emisión en formato dd/MM/yyyy o yyyy-MM-dd
     * @param documentType tipo de comprobante (ej. "01" = factura)
     * @param ruc RUC del emisor (13 dígitos)
     * @param environment tipo de ambiente: "1" = pruebas, "2" = producción
     * @param serie serie del comprobante (establecimiento 3 + puntoEmision 3, ej. "001001")
     * @param sequentialNumber número secuencial (9 dígitos)
     * @param numericCode código numérico aleatorio (8 dígitos)
     * @param emissionType tipo de emisión: "1" = normal
     */
    public static String generateAccessKey(String date, String documentType, String ruc,
                                           String environment, String serie,
                                           String sequentialNumber, String numericCode,
                                           String emissionType) {
        if (!"1".equals(environment) && !"2".equals(environment)) {
            throw new IllegalArgumentException("environment debe ser '1' (pruebas) o '2' (producción)");
        }
        if (!"1".equals(emissionType)) {
            throw new IllegalArgumentException("emissionType debe ser '1' (normal)");
        }
        // Formato: DDMMYYYY(8) + tipoComp(2) + RUC(13) + ambiente(1) + serie(6) + secuencial(9) + codNumerico(8) + tipoEmision(1)
        String baseKey = formatDate(date) +
                        padLeft(documentType, 2, '0') +
                        padLeft(ruc, 13, '0') +
                        environment +
                        padLeft(serie, 6, '0') +
                        padLeft(sequentialNumber, 9, '0') +
                        padLeft(numericCode, 8, '0') +
                        emissionType;
        
        // Calcular dígito verificador
        String verifierDigit = calculateVerifierDigit(baseKey);
        
        return baseKey + verifierDigit;
    }
    
    /**
     * Formatea la fecha en DDMMYYYY
     */
    private static String formatDate(String date) {
        // Suponiendo que date viene en formato YYYY-MM-DD o DD/MM/YYYY
        if (date.contains("-")) {
            String[] parts = date.split("-");
            return parts[2] + parts[1] + parts[0]; // DD + MM + YYYY
        } else if (date.contains("/")) {
            return date.replace("/", "");
        }
        return date;
    }
    
    /**
     * Rellena una cadena con ceros a la izquierda
     */
    private static String padLeft(String str, int length, char padChar) {
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }
    
    /**
     * Calcula el dígito verificador usando módulo 11
     */
    private static String calculateVerifierDigit(String baseKey) {
        int[] weights = {7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        
        int sum = 0;
        for (int i = 0; i < baseKey.length(); i++) {
            int digit = Character.getNumericValue(baseKey.charAt(i));
            sum += digit * weights[i];
        }
        
        int remainder = sum % 11;
        int verifier = 11 - remainder;
        
        if (verifier == 11) {
            verifier = 0;
        } else if (verifier == 10) {
            verifier = 1;
        }
        
        return String.valueOf(verifier);
    }
}
