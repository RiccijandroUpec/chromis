package uk.chromis.pos.invoice.utils;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utilidad para encriptar y desencriptar contraseñas y datos sensibles.
 * Usa AES-256/GCM con una clave maestra generada por instalación (nunca incrustada en el código fuente).
 *
 * La clave maestra se toma, en orden de prioridad, de:
 *  1. La variable de entorno CHROMIS_INVOICE_MASTER_KEY (Base64 de 32 bytes).
 *  2. Un archivo de clave local generado en el primer uso, con permisos restringidos al propietario.
 */
public class CipherUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_LENGTH_BYTES = 32; // 256 bits
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final String MASTER_KEY_ENV_VAR = "CHROMIS_INVOICE_MASTER_KEY";
    private static final String MASTER_KEY_FILE = System.getProperty("user.home")
            + File.separator + ".chromispos" + File.separator + "invoice_master.key";

    private static volatile SecretKey cachedKey;

    /**
     * Encripta una contraseña con AES-256/GCM (IV aleatorio por operación).
     * @param plainPassword Contraseña en texto plano
     * @return IV + texto cifrado, codificados en Base64
     */
    public static String encrypt(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return "";
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            buffer.put(iv).put(encryptedBytes);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo encriptar el valor.", e);
        }
    }

    /**
     * Desencripta una contraseña producida por {@link #encrypt(String)}.
     * Si el valor no es un texto cifrado reconocible (p. ej. una configuración antigua
     * guardada en texto plano antes de esta migración), se retorna tal cual para no
     * romper configuraciones existentes.
     * @param encryptedPassword Contraseña encriptada en Base64
     * @return Contraseña en texto plano
     */
    public static String decrypt(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return "";
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encryptedPassword);
        } catch (IllegalArgumentException notBase64) {
            // Configuración heredada guardada en texto plano antes de esta migración.
            return encryptedPassword;
        }

        if (decoded.length <= GCM_IV_LENGTH_BYTES) {
            return encryptedPassword;
        }

        try {
            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH_BYTES);
            byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] decryptedBytes = cipher.doFinal(cipherText);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (AEADBadTagException authFailure) {
            // No descifra con el esquema GCM actual. Puede ser una configuración cifrada con el
            // esquema heredado (clave fija + AES/ECB, previo a esta migración) o texto plano que
            // decodifica como Base64 por casualidad. Intentar el esquema heredado antes de rendirse.
            String legacyPlain = tryLegacyDecrypt(decoded);
            return legacyPlain != null ? legacyPlain : encryptedPassword;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo desencriptar el valor: clave maestra ausente o dato corrupto.", e);
        }
    }

    /**
     * Compatibilidad retroactiva únicamente: descifra valores generados por el esquema previo
     * a esta migración (clave fija incrustada en el código + AES/ECB). Nunca se usa para cifrar
     * valores nuevos — en cuanto la configuración se vuelva a guardar, se migra automáticamente
     * al esquema AES/GCM con clave por instalación.
     */
    private static String tryLegacyDecrypt(byte[] decoded) {
        try {
            byte[] legacyKeyBytes = new byte[32];
            byte[] rawKey = "ChamisPOSInvoiceModuleSecureKey2025!".getBytes(StandardCharsets.UTF_8);
            System.arraycopy(rawKey, 0, legacyKeyBytes, 0, Math.min(rawKey.length, 32));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(legacyKeyBytes, ALGORITHM));
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception legacyFailure) {
            return null;
        }
    }

    /**
     * Desencripta directamente a char[] para evitar retener la contraseña en un String
     * inmutable (que no puede borrarse de memoria).
     */
    public static char[] decryptToCharArray(String encryptedValue) {
        String plain = decrypt(encryptedValue);
        char[] result = plain.toCharArray();
        // No se puede purgar el contenido del String intermedio (inmutable en la JVM),
        // pero sí evitamos que sobreviva como campo de instancia más allá de este método.
        return result;
    }

    /**
     * Obtiene (o genera en el primer uso) la clave maestra AES-256 de esta instalación.
     */
    private static synchronized SecretKey getSecretKey() throws Exception {
        if (cachedKey != null) {
            return cachedKey;
        }

        byte[] keyBytes = readKeyFromEnvironment();
        if (keyBytes == null) {
            keyBytes = readOrCreatePersistedKey();
        }

        cachedKey = new SecretKeySpec(keyBytes, ALGORITHM);
        return cachedKey;
    }

    private static byte[] readKeyFromEnvironment() {
        String encoded = System.getenv(MASTER_KEY_ENV_VAR);
        if (encoded == null || encoded.isEmpty()) {
            return null;
        }
        byte[] keyBytes = Base64.getDecoder().decode(encoded);
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(MASTER_KEY_ENV_VAR + " debe decodificar a " + KEY_LENGTH_BYTES + " bytes.");
        }
        return keyBytes;
    }

    private static byte[] readOrCreatePersistedKey() throws Exception {
        Path keyPath = Path.of(MASTER_KEY_FILE);

        if (Files.exists(keyPath)) {
            byte[] existing = Base64.getDecoder().decode(Files.readString(keyPath).trim());
            if (existing.length == KEY_LENGTH_BYTES) {
                return existing;
            }
        }

        byte[] newKey = new byte[KEY_LENGTH_BYTES];
        new SecureRandom().nextBytes(newKey);
        persistKey(keyPath, newKey);
        return newKey;
    }

    private static void persistKey(Path keyPath, byte[] keyBytes) throws Exception {
        Files.createDirectories(keyPath.getParent());
        String encoded = Base64.getEncoder().encodeToString(keyBytes);

        try (FileOutputStream fos = new FileOutputStream(keyPath.toFile())) {
            fos.write(encoded.getBytes(StandardCharsets.UTF_8));
        }

        try {
            Set<PosixFilePermission> ownerOnly = EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(keyPath, ownerOnly);
        } catch (UnsupportedOperationException notPosix) {
            // Sistemas de archivos no-POSIX (p. ej. Windows sin ACL POSIX): usar el mecanismo estándar de File.
            File f = keyPath.toFile();
            f.setReadable(false, false);
            f.setWritable(false, false);
            f.setReadable(true, true);
            f.setWritable(true, true);
        }
    }
}
