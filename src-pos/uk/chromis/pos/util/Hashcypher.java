/*
**    Chromis POS  - Open Source Point of Sale
**
**    This file is part of Chromis POS Version Chromis V1.5.4
**
**    Copyright (c) 2015-2023 Chromis & previous Openbravo POS related works   
**
**    https://www.chromis.co.uk
**   
**    Chromis POS is free software: you can redistribute it and/or modify
**    it under the terms of the GNU General Public License as published by
**    the Free Software Foundation, either version 3 of the License, or
**    (at your option) any later version.
**
**    Chromis POS is distributed in the hope that it will be useful,
**    but WITHOUT ANY WARRANTY; without even the implied warranty of
**    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
**    GNU General Public License for more details.
**
**    You should have received a copy of the GNU General Public License
**    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>
**
*/


package uk.chromis.pos.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 *
 *
 */
public class Hashcypher {

    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int PBKDF2_KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Creates a new instance of Hashcypher
     */
    public Hashcypher() {
    }

    /**
     *
     * @param sPassword
     * @param sHashPassword
     * @return
     */
    public static boolean authenticate(String sPassword, String sHashPassword) {
        if (sHashPassword == null || sHashPassword.equals("") || sHashPassword.startsWith("empty:")) {
            return sPassword == null || sPassword.equals("");
        } else if (sHashPassword.startsWith("pbkdf2:")) {
            return authenticatePbkdf2(sPassword, sHashPassword);
        } else if (sHashPassword.startsWith("sha1:")) {
            // Compatibilidad retroactiva con hashes existentes: solo verificación, nunca se generan nuevos.
            return sHashPassword.equals(legacySha1(sPassword));
        } else if (sHashPassword.startsWith("plain:")) {
            // Compatibilidad retroactiva con hashes existentes: solo verificación, nunca se generan nuevos.
            return sHashPassword.equals("plain:" + sPassword);
        } else {
            return sHashPassword.equals(sPassword);
        }
    }

    /**
     * Genera el hash de una contraseña usando PBKDF2WithHmacSHA256 con sal aleatoria por contraseña.
     * @param sPassword
     * @return
     */
    public static String hashString(String sPassword) {

        if (sPassword == null || sPassword.equals("")) {
            return "empty:";
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(sPassword, salt);

        return "pbkdf2:" + PBKDF2_ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    private static boolean authenticatePbkdf2(String sPassword, String sHashPassword) {
        try {
            String[] parts = sHashPassword.split(":");
            if (parts.length != 4) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);

            byte[] actualHash = pbkdf2(sPassword == null ? "" : sPassword, salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String sPassword, byte[] salt) {
        return pbkdf2(sPassword, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH_BITS);
    }

    private static byte[] pbkdf2(String sPassword, byte[] salt, int iterations, int keyLengthBits) {
        try {
            KeySpec spec = new PBEKeySpec(sPassword.toCharArray(), salt, iterations, keyLengthBits);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            // PBKDF2WithHmacSHA256 está garantizado en toda JVM estándar (Java 8+).
            throw new IllegalStateException("No se pudo calcular PBKDF2", e);
        }
    }

    /**
     * Mantiene la verificación de hashes SHA-1 heredados (formato "sha1:" previo a la migración a PBKDF2).
     */
    private static String legacySha1(String sPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(sPassword.getBytes("UTF-8"));
            return "sha1:" + StringUtils.byte2hex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular SHA-1 heredado", e);
        }
    }

}
