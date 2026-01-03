package uk.chromis.pos.invoice.utils;

/**
 * Validadores para Ecuador - Facturación Electrónica
 */
public class EcuadorValidators {
    
    /**
     * Valida un RUC (Registro Único de Contribuyentes)
     * RUC: 13 dígitos
     * Primeros 10: número de identificación
     * Dígitos 11-13: consecutivo de establecimientos
     */
    public static boolean isValidRUC(String ruc) {
        if (ruc == null || ruc.length() != 13) {
            return false;
        }
        
        // Verificar que sean solo dígitos
        if (!ruc.matches("\\d+")) {
            return false;
        }
        
        // Validar dígito verificador
        return validateEcuadorIdentificationCode(ruc.substring(0, 10));
    }
    
    /**
     * Valida una cédula ecuatoriana (10 dígitos)
     */
    public static boolean isValidCedula(String cedula) {
        if (cedula == null || cedula.length() != 10) {
            return false;
        }
        
        // Verificar que sean solo dígitos
        if (!cedula.matches("\\d+")) {
            return false;
        }
        
        return validateEcuadorIdentificationCode(cedula);
    }
    
    /**
     * Valida identificación (cédula o RUC)
     */
    private static boolean validateEcuadorIdentificationCode(String code) {
        if (code.length() < 10) {
            return false;
        }
        
        // Tomar los primeros 9 dígitos
        String baseCode = code.substring(0, 9);
        int checkDigit = Integer.parseInt(code.substring(9, 10));
        
        // Pesos para el algoritmo de validación
        int[] weights = {2, 3, 4, 5, 6, 7, 8, 9, 1};
        int sum = 0;
        
        for (int i = 0; i < baseCode.length(); i++) {
            int digit = Integer.parseInt(String.valueOf(baseCode.charAt(i)));
            int product = digit * weights[i];
            
            if (product >= 10) {
                product = product - 9;
            }
            
            sum += product;
        }
        
        int remainder = sum % 10;
        int calculatedDigit = remainder == 0 ? 0 : 10 - remainder;
        
        return calculatedDigit == checkDigit;
    }
    
    /**
     * Valida un email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Email es opcional
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Formatea un RUC para visualización
     * Ejemplo: 1234567890001 -> 123.456.789-0001
     */
    public static String formatRUC(String ruc) {
        if (ruc == null || ruc.length() != 13) {
            return ruc;
        }
        return ruc.substring(0, 3) + "." + 
               ruc.substring(3, 6) + "." + 
               ruc.substring(6, 9) + "-" + 
               ruc.substring(9);
    }
    
    /**
     * Formatea una cédula para visualización
     * Ejemplo: 1708123456 -> 170.812.345-6
     */
    public static String formatCedula(String cedula) {
        if (cedula == null || cedula.length() != 10) {
            return cedula;
        }
        return cedula.substring(0, 3) + "." + 
               cedula.substring(3, 6) + "." + 
               cedula.substring(6, 9) + "-" + 
               cedula.substring(9);
    }
    
    /**
     * Extrae provincia de código de provincia ecuatoriano
     */
    public static String getProvinceFromCode(String code) {
        switch (code) {
            case "01": return "Azuay";
            case "02": return "Bolívar";
            case "03": return "Cañar";
            case "04": return "Carchi";
            case "05": return "Cotopaxi";
            case "06": return "Chimborazo";
            case "07": return "El Oro";
            case "08": return "Esmeraldas";
            case "09": return "Guayas";
            case "10": return "Pichincha";
            case "11": return "Imbabura";
            case "12": return "Loja";
            case "13": return "Los Ríos";
            case "14": return "Manabí";
            case "15": return "Morona Santiago";
            case "16": return "Napo";
            case "17": return "Pastaza";
            case "18": return "Tungurahua";
            case "19": return "Zamora Chinchipe";
            case "20": return "Galápagos";
            case "21": return "Sucumbíos";
            case "22": return "Orellana";
            case "23": return "Santo Domingo de los Tsáchilas";
            case "24": return "Santa Elena";
            default: return "Desconocida";
        }
    }
}
