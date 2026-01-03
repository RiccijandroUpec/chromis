package uk.chromis.pos.invoice.utils;

import java.math.BigInteger;

/**
 * Utilidad para generar claves de acceso SRI
 * Clave de acceso: 49 dígitos
 * Formato: FFFFPPDDDDDDDDDDDDDDDRRRRRRRRRRRRRAAAADDDD
 * F: Fecha (DDMMYYYY - 8 dígitos)
 * P: Tipo de comprobante (2 dígitos)
 * D: Número de RUC del emisor (13 dígitos)
 * R: Número secuencial (9 dígitos)
 * A: Código de autorización (4 dígitos)
 * D: Dígito verificador (1 dígito)
 */
public class AccessKeyGenerator {
    
    /**
     * Genera la clave de acceso SRI
     */
    public static String generateAccessKey(String date, String documentType, String ruc, 
                                          String sequentialNumber, String authorizationCode) {
        // Formato: FFFFPPDDDDDDDDDDDDDDDRRRRRRRRRRRRRAAAADDDD
        // Reemplazar FF por 01 para facturas
        String baseKey = formatDate(date) + 
                        documentType + 
                        padLeft(ruc, 13, '0') + 
                        padLeft(sequentialNumber, 9, '0') + 
                        padLeft(authorizationCode, 4, '0');
        
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
