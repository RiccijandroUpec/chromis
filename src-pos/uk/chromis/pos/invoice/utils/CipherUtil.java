package uk.chromis.pos.invoice.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad para encriptar y desencriptar contraseñas y datos sensibles
 * Usa AES-256 (Advanced Encryption Standard)
 */
public class CipherUtil {
    
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final String FIXED_KEY = "ChamisPOSInvoiceModuleSecureKey2025!"; // 32 bytes = 256 bits
    
    /**
     * Encripta una contraseña
     * @param plainPassword Contraseña en texto plano
     * @return Contraseña encriptada en Base64
     */
    public static String encrypt(String plainPassword) {
        try {
            if (plainPassword == null || plainPassword.isEmpty()) {
                return "";
            }
            
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
            
        } catch (Exception e) {
            System.err.println("Error encriptando contraseña: " + e.getMessage());
            // En caso de error, retornar la contraseña original (no es ideal pero mantiene compatibilidad)
            return plainPassword;
        }
    }
    
    /**
     * Desencripta una contraseña
     * @param encryptedPassword Contraseña encriptada en Base64
     * @return Contraseña en texto plano
     */
    public static String decrypt(String encryptedPassword) {
        try {
            if (encryptedPassword == null || encryptedPassword.isEmpty()) {
                return "";
            }
            
            // Si no es Base64 válido, asumir que es texto plano
            if (!isValidBase64(encryptedPassword)) {
                return encryptedPassword;
            }
            
            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            
            return new String(decryptedBytes);
            
        } catch (Exception e) {
            System.err.println("Error desencriptando contraseña: " + e.getMessage());
            // En caso de error, retornar la contraseña tal como está
            return encryptedPassword;
        }
    }
    
    /**
     * Obtiene la clave secreta para encriptación
     * @return SecretKey para AES
     */
    private static SecretKey getSecretKey() {
        try {
            // Usar una clave derivada del FIXED_KEY
            byte[] decodedKey = FIXED_KEY.getBytes();
            // Asegurar que es de 256 bits (32 bytes)
            byte[] keyBytes = new byte[32];
            System.arraycopy(decodedKey, 0, keyBytes, 0, Math.min(decodedKey.length, 32));
            
            return new SecretKeySpec(keyBytes, 0, keyBytes.length, ALGORITHM);
            
        } catch (Exception e) {
            System.err.println("Error generando clave secreta: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Verifica si una cadena es Base64 válida
     * @param str Cadena a validar
     * @return true si es Base64, false en caso contrario
     */
    private static boolean isValidBase64(String str) {
        try {
            if (str == null || str.isEmpty()) {
                return false;
            }
            Base64.getDecoder().decode(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Prueba la encriptación y desencriptación
     */
    public static void main(String[] args) {
        String testPassword = "MiContraseña123!";
        
        System.out.println("Contraseña original: " + testPassword);
        
        String encrypted = encrypt(testPassword);
        System.out.println("Encriptada: " + encrypted);
        
        String decrypted = decrypt(encrypted);
        System.out.println("Desencriptada: " + decrypted);
        
        System.out.println("¿Son iguales? " + testPassword.equals(decrypted));
    }
}
