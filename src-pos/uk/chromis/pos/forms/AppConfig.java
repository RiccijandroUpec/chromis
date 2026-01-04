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
package uk.chromis.pos.forms;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.configuration.PropertiesConfiguration;
import uk.chromis.basic.BasicException;
import uk.chromis.commons.dbmanager.DbUtils;
import uk.chromis.data.loader.PreparedSentence;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.data.loader.SerializerReadInteger;
import uk.chromis.data.loader.SerializerReadString;
import uk.chromis.data.loader.SessionFactory;
import uk.chromis.pos.util.StringUtils;

/**
 *
 * @author John
 */
public class AppConfig implements AppProperties {

    private static final String DATABASE_SERVER = "database.server";
    private static final String DATABASE_PORT = "database.port";
    private static final String DATABASE_NAME = "database.name";
    private static final String DATABASE_USER = "database.user";
    private static final String DATABASE_PASSWORD = "database.password";
    private static final String DATABASE_PROVIDER_CLASS = "database.class";
    private static final String DATABASE_LIBRARY = "database.library";
    public static final String DATABASE_TIMEZONE = "?zeroDateTimeBehavior=convert_To_NULL"; //&serverTimezone=UTC";

    private static PropertiesConfiguration config;
    private static final File CONFIGFILE = new File(System.getProperty("user.dir") + "/" + "chromisposconfig.properties");
    private static final AppConfig INSTANCE = new AppConfig(CONFIGFILE);

    private static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

//    static {
//        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//    }

    public static Dimension getScreenSize() {
        return screenSize;
    }

    protected AppConfig(File configFile) {
        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                config = new PropertiesConfiguration(configFile);
                config.setAutoSave(true);
                setDefaults();
            } else {
                config = new PropertiesConfiguration(configFile);
                config.setAutoSave(true);
            }
        } catch (Exception e) {

        }

    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public static void setDefaults() {
        config.setProperty(DATABASE_USER, "");
        setDatabasePassword("");
        config.setProperty(DATABASE_SERVER, "localhost");
        config.setProperty(DATABASE_PORT, "3306");
        config.setProperty(DATABASE_NAME, "");
        config.setProperty(DATABASE_LIBRARY, "/lib/mysql-connector-java-8.0.23.jar");
        config.setProperty(DATABASE_PROVIDER_CLASS, "com.mysql.cj.jdbc.Driver");

    }

    public static PropertiesConfiguration getConfig() {
        return config;
    }

    public static String getDatabaseServer() {
        return config.getString(DATABASE_SERVER, null);
    }

    public static void setDatabaseServer(String server) {
        config.setProperty(DATABASE_SERVER, server);
    }

    public static String getDatabasePort() {
        return config.getString(DATABASE_PORT, null);
    }

    public static void setDatabasePort(String port) {
        config.setProperty(DATABASE_PORT, port);
    }

    public static String getDatabaseName() {
        return config.getString(DATABASE_NAME, null);
    }

    public static void setDatabaseName(String name) {
        config.setProperty(DATABASE_NAME, name);
    }

    public static String getDatabaseUser() {
        return config.getString(DATABASE_USER, null);
    }

    public static void setDatabaseUser(String user) {
        config.setProperty(DATABASE_USER, user);
    }

    public static String getDatabasePassword() {
        return config.getString(DATABASE_PASSWORD, null);
    }

    public static String getClearDatabasePassword() {
        String sDBPassword = getDatabasePassword();
        if (sDBPassword == null) {
            return null;
        }
        if (sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + getDatabaseUser());
            return cypher.decrypt(sDBPassword.substring(6));
        }
        // Allow plain-text password fallback
        return sDBPassword;
    }

    public static void setDatabasePassword(String password) {
        AltEncrypter cypher = new AltEncrypter("cypherkey" + getDatabaseUser());
        if (password.equals("")) {
            config.setProperty(DATABASE_PASSWORD, "");
        } else {
            AppConfig.put("database.password", "crypt:" + cypher.encrypt(password));
            config.setProperty(DATABASE_PASSWORD, "crypt:" + cypher.encrypt(password));
        }
    }

    public static String getDatabaseProviderClass() {
        return config.getString(DATABASE_PROVIDER_CLASS, null);
    }

    public static void setDatabaseProviderClass(String databaseProviderClass) {
        config.setProperty(DATABASE_PROVIDER_CLASS, databaseProviderClass);
    }

    public static String getDatabaseLibrary() {
        return config.getString(DATABASE_LIBRARY, null);
    }

    public static String getDatabaseLibraryPath() {
        String dirname = System.getProperty("user.dir") == null ? "./" : System.getProperty("user.dir");
        return dirname + config.getString(DATABASE_LIBRARY, null);
    }

    public static void setDatabaseLibrary(String databaseLibrary) {
        config.setProperty(DATABASE_LIBRARY, databaseLibrary);
    }

    public static String getDatabaseURL() {
        StringBuilder url;
        url = new StringBuilder();
        url.append("jdbc:mysql://");
        url.append(getDatabaseServer());
        url.append(":");
        url.append(getDatabasePort());
        url.append("/");
        url.append(getDatabaseName());
        url.append("?zeroDateTimeBehavior=convert_To_NULL");
        return url.toString();
    }

    public static int getInt(String key) {
        return config.getInt(key, 0);
    }

    public static int getInt(String key, int value) {
        return config.getInt(key, value);
    }

    public static void putInt(String key, int value) {
        config.setProperty(key, value);
    }

    public static String getString(String key) {
        return config.getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static void put(String key, String value) {
        config.setProperty(key, value);
    }

    public static boolean getBoolean(String key) {
        return config.getBoolean(key, false);
    }

    public static void put(String key, boolean value) {
        config.setProperty(key, value);
    }

    public static String getOS() {
        if (System.getProperty("os.name").toLowerCase().startsWith("linux")) {
            return "Linux";
        } else if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            return "Windows";
        } else {
            return "OSX";
        }
    }

    public static String[] getParameters(String key) {
        return config.getStringArray(key);
    }

    //get the terminal ID
    public String getTerminalID() {
        return getString("terminalID", null);
    }

    @Override
    public File getConfigFile() {
        return CONFIGFILE;
    }

    @Override
    public String getHost() {
        return DbUtils.getTerminalName();
    }

    @Override
    public String getProperty(String sKey) {
        return null;
    }

    public static String getVersion() {
        SentenceList m_siteGuid = new PreparedSentence(SessionFactory.getSession(), "select version from applications ", null, SerializerReadString.INSTANCE);
        try {
            return (String) m_siteGuid.list().get(0);
        } catch (BasicException ex) {
            return "";
        }
    }

    public static int getVersionInt() {
        SentenceList m_version = new PreparedSentence(SessionFactory.getSession(), "select versionint from applications ", null, SerializerReadInteger.INSTANCE);
        try {
            return (int) m_version.list().get(0);
        } catch (BasicException ex) {
            return 0;
        }
    }

    private static class AltEncrypter {

        private Cipher cipherDecrypt;
        private Cipher cipherEncrypt;

        public AltEncrypter(String passPhrase) {

            try {
                SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
                sr.setSeed(passPhrase.getBytes("UTF8"));
                KeyGenerator kGen = KeyGenerator.getInstance("DESEDE");
                kGen.init(168, sr);
                Key key = kGen.generateKey();

                cipherEncrypt = Cipher.getInstance("DESEDE/ECB/PKCS5Padding");
                cipherEncrypt.init(Cipher.ENCRYPT_MODE, key);

                cipherDecrypt = Cipher.getInstance("DESEDE/ECB/PKCS5Padding");
                cipherDecrypt.init(Cipher.DECRYPT_MODE, key);
            } catch (UnsupportedEncodingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            }
        }

        public String encrypt(String str) {
            try {
                return StringUtils.byte2hex(cipherEncrypt.doFinal(str.getBytes("UTF8")));
            } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
            }
            return null;
        }

        public String decrypt(String str) {
            try {
                return new String(cipherDecrypt.doFinal(StringUtils.hex2byte(str)), "UTF8");
            } catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e) {
            }
            return null;
        }
    }

}
