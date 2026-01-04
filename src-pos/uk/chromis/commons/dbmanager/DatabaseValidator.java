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
package uk.chromis.commons.dbmanager;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import uk.chromis.commons.dialogs.SplashLogo;
import uk.chromis.pos.forms.LocalResource;
import uk.chromis.pos.util.AltEncrypter;

/**
 *
 * @author John
 */
public class DatabaseValidator {

    private final SplashLogo splash = new SplashLogo();
    public static CountDownLatch waitForUpdate;
    public static CountDownLatch waitForConnection;
    private Object[] result;
    private final String currentDir = System.getProperty("user.dir");
    private static PropertiesConfiguration config;
    private static String errorMsg = "";
    private static Boolean isConnected = false;
    private static Connection connection = null;
    private static Boolean isEmpty = true;
    private static String version = "";
    private static String id = "";
    private static String name = "";
    private static int versionInt = 0;
    private static int rowCount = 0;

    public static final int WARNING = 0;
    public static final int INFORMATION = 1;
    public static final int ERROR = 2;
    public static final int CONFIRMATION = 3;
    public static final int EXCEPTION = 4;
    public static final int SUCCESS = 5;

    public DatabaseValidator() {

    }

    public Boolean validate(String propertiesFile) {
        // Debug: log where we are looking for config
        String configPath = currentDir + "/" + propertiesFile;
        System.err.println("[DBVALIDATOR] user.dir=" + currentDir + " configPath=" + configPath);
        //Check if the properties file exists
        File configFile = new File(configPath);
        System.err.println("[DBVALIDATOR] exists=" + configFile.exists() + " isDir=" + configFile.isDirectory());
        if (!configFile.exists() || configFile.isDirectory()) {
            showAlertDialog(DatabaseValidator.WARNING,
                    "\nUnable to find '" + propertiesFile + "' !\n",
                    " Please run 'TerminalConfig' or 'CreateDatabase'\n to resolve the issue.",
                    true);
            splash.deleteSplashLogo();
            System.exit(0);
        }

        //Check if properties file has database properties
        try {
            config = new PropertiesConfiguration(configFile);
        } catch (ConfigurationException e) {
            showAlertDialog(DatabaseValidator.ERROR,
                    "\nRead Error !\n",
                    " Unable to read '" + propertiesFile + "' !\n",
                    true);
            splash.deleteSplashLogo();
            System.exit(0);
        }

        if (config.isEmpty()) {
            showAlertDialog(DatabaseValidator.ERROR,
                    "\nEmpty Configuration File !\n",
                    " Unable to read '" + propertiesFile + "' !. File is empty.\n",
                    true);
            splash.deleteSplashLogo();
            System.exit(0);
        }

        Boolean configGood = true;
        for (String s : new ArrayList<>(Arrays.asList("database.user", "database.password", "database.server", "database.port",
                "database.name", "database.library", "database.class"))) {
            if (!config.containsKey(s)) {
                configGood = false;
                continue;
            }
            String val = config.getString(s, "");
            if (val == null) {
                configGood = false;
                continue;
            }
            // Allow empty password; other fields must be non-blank
            if (!s.equals("database.password") && val.trim().isBlank()) {
                configGood = false;
            }
        }

        if (!configGood) {
            showAlertDialog(DatabaseValidator.ERROR,
                    "\nConfiguration File Issue !\n",
                    " '" + propertiesFile + "' is missing a database setting or the setting is empty.\n",
                    true);
            splash.deleteSplashLogo();
            System.exit(0);
        }

        //We are now in position to test the connectivity
        checkDatabase();
        return (Boolean) result[0];

    }

    public Object[] getValidationResult() {
        return result;
    }

    private String buildURL() {
        StringBuilder url;
        url = new StringBuilder();
        url.append("jdbc:mysql://");
        url.append(config.getString("database.server"));
        url.append(":");
        url.append(config.getString("database.port"));
        url.append("/");
        url.append(config.getString("database.name"));
        url.append("?zeroDateTimeBehavior=convert_To_NULL");
        return url.toString();
    }

    private void checkDatabase() {
        waitForUpdate = new CountDownLatch(1);
        waitForConnection = new CountDownLatch(1);
        connectionTest(true);

        try {
            waitForConnection.await();
        } catch (InterruptedException ex) {
        }

        // Debug: print connection results
        System.err.println("[DBVALIDATOR] connected=" + result[0]
            + " empty=" + result[3]
            + " version=" + result[4]
            + " versionInt=" + result[5]
            + " id=" + result[6]
            + " name=" + result[7]
            + " rowCount=" + result[8]
            + " errMsg='" + result[1] + "'");

//        System.out.println("\n*********************************************");
//        System.out.println("Connected   : " + (Boolean) result[0]);
//        System.out.println("Error msg   : " + (String) result[1]);
//        System.out.println("connection  : " + (result[2] != null));
//        System.out.println("Empty db    : " + (Boolean) result[3]);
//        System.out.println("Version     : " + (String) result[4]);
//        System.out.println("Version Int : " + (Integer) result[5]);
//        System.out.println("ID          : " + (String) result[6]);
//        System.out.println("Name        : " + (String) result[7]);
//        System.out.println("Rows        : " + (Integer) result[8]);
//        System.out.println("*********************************************");

        if (((!(Boolean) result[0]) || ((Boolean) result[3] && result[1].equals(""))
                || (result[4].equals("") || (int) result[8] != 1 || !result[6].equals("chromispos") || !result[7].equals("chromis pos")))) {
            showAlertDialog(DatabaseValidator.ERROR,
                    "\n " + (String) result[1] + " \n\n",
                    "\n Run 'TerminalConfig' or 'CreateDatabase' to resolve the issue. \n",
                    true);
            System.exit(0);
        }
        splash.deleteSplashLogo();
    }

    private void connectionTest(Boolean useLatch) {
        try {
            connection = (Connection) DriverManager.getConnection(buildURL(), config.getString("database.user"), getDatabasePassword());
            DriverManager.setLoginTimeout(5);
            isConnected = (connection == null) ? false : connection.isValid(5);
            if (isConnected) {
                isDBEmpty();
                if (!isEmpty) {
                    getVersionDetails();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                }
            }
        } catch (SQLException ex) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            switch (ex.getSQLState()) {
                //unable to connect to server                
                case "08S01":
                case "08001":
                    isConnected = false;
                    errorMsg = LocalResource.getString("alert.unableToConnect");
                    break;
                //database not available
                case "42000":
                case "3D000":
                    isConnected = false;
                    errorMsg = LocalResource.getString("alert.databaseNotFound");
                    break;
                //Authentication error
                case "28000":
                case "28P01":
                case "08004":
                    isConnected = false;
                    errorMsg = LocalResource.getString("alert.authenticationError");
                    break;
                default:
                    isConnected = false;
                    errorMsg = LocalResource.getString("alert.unknownError") + " : " + ex.getSQLState();
            }
        } catch (SecurityException | IllegalArgumentException ex) {
            System.out.println(ex);
        }
        result = new Object[]{isConnected, errorMsg, connection, isEmpty, version, versionInt, id.toLowerCase(), name.toLowerCase(), rowCount};
        if (useLatch) {
            waitForConnection.countDown();
        }
    }

    private static String getDatabasePassword() {
        String sDBPassword = config.getString("database.password", "");
        if (sDBPassword == null || sDBPassword.isBlank()) {
            return "";
        }
        if (sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + config.getString("database.user"));
            return cypher.decrypt(sDBPassword.substring(6));
        }
        return sDBPassword;
    }

    private static void isDBEmpty() {
        try {
            Statement stmtTables = connection.createStatement();
            ResultSet rsTables = stmtTables.executeQuery("select count(*) from information_schema.tables where table_type = 'BASE TABLE' and TABLE_SCHEMA = database()");
            if (rsTables.next()) {
                isEmpty = (rsTables.getInt(1) == 0);
            }
        } catch (SQLException ex) {
        }
    }

    private static void getVersionDetails() {
        try {
            String sql = "select count(*) from applications";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rowCount = rs.getInt(1);
            }
            sql = "Select * from applications ";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                id = rs.getString("id");
                name = rs.getString("name");
                version = rs.getString("version");
                versionInt = rs.getInt("versionint");
            }
        } catch (SQLException ex) {
        }
    }

    private void showAlertDialog(int type, String strHeaderText, String strContext, Boolean undecorated) {
        AlertDialog jAlert = new AlertDialog(type,
                strHeaderText,
                strContext,
                undecorated);
        jAlert.setLocationRelativeTo(null);
        jAlert.setVisible(true);
    }

    public class AlertDialog extends JDialog {

        protected AlertDialog(int type, String strHeaderText, String strContextText, Boolean unDecorated) {
            super(new JFrame());
            alertDialog(type, strHeaderText, strContextText, unDecorated);
            pack();
        }

        private void alertDialog(int type, String headerText, String contextText, Boolean unDecorated) {
            JPanel btnPanel = new JPanel();
            JButton btn = new JButton("OK");
            btn.addActionListener((ActionEvent e) -> {
                dispose();
            });
            btnPanel.add(btn);

            ImageIcon img;

            switch (type) {
                case 0:
                    img = new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/fixedimages/warning.png"));
                    break;
                case 1:
                    img = new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/fixedimages/information.png"));
                    break;
                case 3:
                    img = new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/fixedimages/confirmation.png"));
                    break;
                case 5:
                    img = new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/fixedimages/success.png"));
                    break;
                default:
                    img = new javax.swing.ImageIcon(getClass().getResource("/uk/chromis/fixedimages/error.png"));
                    break;
            }

            //Set the dialog with no minimize or expand icons on title bar
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent evt) {
                    dispose();
                }
            });

            //Create the main panel layout
            JPanel panel = new JPanel(new MigLayout("insets 10 4 5 2", "[][]", "[]0[][]"));
            JTextArea headerTextArea = new JTextArea(1, 1);
            setTextAreaParameters(headerTextArea);
            panel.add(headerTextArea, "left,  pushx, growx, split 2");
            JLabel iconLabel = new JLabel();
            iconLabel.setIcon(img);
            panel.add(iconLabel, "wrap, width :45:, align right");
            JSeparator separator = new JSeparator();
            separator.setOrientation(JSeparator.HORIZONTAL);
            panel.add(separator, "span, center, gapy 10, growx, wrap");
            JTextArea contextArea = new JTextArea(1, 1);
            setTextAreaParameters(contextArea);
            panel.add(contextArea, "left, span, width 350:350:, pushx, growx, split 2, wrap");
            headerTextArea.setText(headerText);
            contextArea.setText(contextText);
            panel.add(btnPanel, "span, gapy 5, height 32:32:32, right");

            setAlwaysOnTop(true);
            setResizable(false);
            setModal(true);

            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
            setUndecorated(unDecorated);
            getContentPane().add(panel);
        }

        private void setTextAreaParameters(JTextArea textArea) {
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setDisabledTextColor(new java.awt.Color(0, 0, 0));
            textArea.setEnabled(false);
            textArea.setFocusable(false);
            textArea.setOpaque(false);
            textArea.setRequestFocusEnabled(false);
        }

    }

}
