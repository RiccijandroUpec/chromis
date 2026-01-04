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

import com.bulenkov.darcula.DarculaLaf;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import org.tinylog.Logger;
import uk.chromis.commons.dbmanager.DatabaseValidator;
import uk.chromis.format.Formats;
import uk.chromis.globals.IconFactory;
import uk.chromis.globals.SystemProperty;
import uk.chromis.commons.dbmanager.DbUtils;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.commons.dialogs.WarningLogo;
import uk.chromis.commons.utils.TerminalInfo;
import uk.chromis.pos.repair.DatabaseRepair;

public class StartPOS {

    private static ServerSocket serverSocket;
    private static WarningLogo warning;
    public static Locale defaultLocale = Locale.getDefault();

    private static boolean registerApp() {
        // prevent multiple instances running on same machine, Socket is never used in app
        try {
            serverSocket = new ServerSocket(65326);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static void main(final String args[]) {

        ChromisFonts.setFont("Liberation Serif");
        setUIFont(new FontUIResource(ChromisFonts.DEFAULTFONT));

        if (Arrays.stream(args).anyMatch("-debug"::equals)) {
            try {
                Files.createDirectories(Paths.get(".\\logs"));
                System.setOut(new PrintStream("./logs/POSstd.log"));
                System.setErr(new PrintStream("./logs/POSerror.log"));
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(StartPOS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String javaV = System.getProperty("java.version");
        if (Integer.parseInt(javaV.substring(0, javaV.indexOf("."))) < 11) {
            JAlertPane.showAlertDialog(JAlertPane.WARNING,
                    null,
                    "Incorrect Java Version Found !!",
                    "Chromis and its associated application require Java 11 as a minimum",
                    JAlertPane.OK_OPTION, true);
            System.exit(0);
        }

        String tuid = TerminalInfo.getTerminalID();
        String name = TerminalInfo.getTerminalName();
        if (name == null || name.equalsIgnoreCase("Unknown")) {
            try {
                name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                name = "POS-" + tuid.substring(0, Math.min(tuid.length(), 8));
            }
            TerminalInfo.setTerminalName(name);
        }

        if (!registerApp()) {
            warning = new WarningLogo();
        }
        DatabaseValidator dbValidate = new DatabaseValidator();
        dbValidate.validate("chromisposconfig.properties");

        DatabaseRepair.repairPayments();

        if (DbUtils.getTriggerCount() < 35) {
            JAlertPane.messageBox(new Dimension(500, 200), JAlertPane.NONE, "\nThe database is missing all or some of the required \ntiggers\\views.\n\n"
                    + "Please run Administration to repair.", 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
            System.exit(0);
        }

        if (DbUtils.getViewCount() != 1) {
            JAlertPane.messageBox(new Dimension(500, 200), JAlertPane.NONE, "\nThe database is missing all or some of the required \ntiggers\\views.\n\n"
                    + "Please run Administration to repair.", 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
            System.exit(0);
        }

        // Set the format patterns
        Formats.setIntegerPattern(SystemProperty.INTEGER);
        Formats.setDoublePattern(SystemProperty.DOUBLE);
        Formats.setCurrencyPattern(SystemProperty.CURRENCY);
        Formats.setPercentPattern(SystemProperty.PERCENT);
        Formats.setDatePattern(SystemProperty.DATE);
        Formats.setTimePattern(SystemProperty.TIME);
        Formats.setDateTimePattern(SystemProperty.DATETIME);

        if (!(new File("iconsets/" + SystemProperty.ICONCOLOUR + ".zip")).exists()) {
            JAlertPane.showAlertDialog(JAlertPane.WARNING,
                    null,
                    "\n Chromis cannot run !!",
                    "  Unable to find the icons file.",
                    JAlertPane.OK_OPTION, true);
            System.exit(0);
        }

        IconFactory.cacheIconsFromZip("/iconsets/" + SystemProperty.ICONCOLOUR + ".zip");
        IconFactory.cacheIconsFromFolder("/images");

        // tests if the database is lower version than the application
        int dbVersionInt = AppConfig.getVersionInt();
        int appVersionInt = AppLocal.APP_VERSIONINT;

        if (dbVersionInt < appVersionInt) {
            AppConfig.putInt("application.versionint", dbVersionInt);
            AppConfig.put("application.version", AppConfig.getVersion());
            // UpdatePanel update = new UpdatePanel();
            JAlertPane.showAlertDialog(JAlertPane.WARNING,
                null,
                "\n  Chromis database needs to be updated.",
                "  Please run 'Chromis Administration' to update system.",
                JAlertPane.OK_OPTION, true);
            System.exit(0);
        }

        if (dbVersionInt != appVersionInt) {
            System.err.println("DB versionInt=" + dbVersionInt + " app versionInt=" + appVersionInt);
            System.err.println("DB version=" + AppConfig.getVersion() + " app version=" + AppLocal.APP_VERSION);
            JAlertPane.showAlertDialog(JAlertPane.WARNING,
                null,
                " Versions do not match !!",
                "\n The Chromis database and this application versions do not match. ",
                JAlertPane.OK_OPTION, true);
            System.exit(0);
        }

        Properties p = new Properties();
        p.put("windowDecoration", "off");
        p.put("logoString", "");

        switch (SystemProperty.LAF) {
            case "com.jtattoo.plaf.acryl.AcrylLookAndFeel":
                com.jtattoo.plaf.acryl.AcrylLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.aero.AeroLookAndFeel":
                com.jtattoo.plaf.aero.AeroLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel":
                com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.fast.FastLookAndFeel":
                com.jtattoo.plaf.fast.FastLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.graphite.GraphiteLookAndFeel":
                com.jtattoo.plaf.graphite.GraphiteLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.hifi.HiFiLookAndFeel":
                com.jtattoo.plaf.hifi.HiFiLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.mcwin.McWinLookAndFeel":
                com.jtattoo.plaf.mcwin.McWinLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.mint.MintLookAndFeel":
                com.jtattoo.plaf.mint.MintLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.noire.NoireLookAndFeel":
                com.jtattoo.plaf.noire.NoireLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.smart.SmartLookAndFeel":
                com.jtattoo.plaf.smart.SmartLookAndFeel.setCurrentTheme(p);
                break;
            case "com.jtattoo.plaf.texture.TextureLookAndFeel":
                com.jtattoo.plaf.texture.TextureLookAndFeel.setCurrentTheme(p);
                break;
        }

        try {
            Object laf = Class.forName(SystemProperty.LAF).getDeclaredConstructor().newInstance();
            if (laf instanceof LookAndFeel) {
                UIManager.setLookAndFeel((LookAndFeel) laf);
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {

        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.error(ex);
        }

        setUIFont(new FontUIResource(ChromisFonts.DEFAULTFONT));

        //get the terminal Id for this machine
        DbUtils.getTerminalName();

        startApp();
    }

    private static void startApp() {

        java.awt.EventQueue.invokeLater(() -> {
            AppConfig config = AppConfig.getInstance();
            JRootFrame rootframe = new JRootFrame();

            if (SystemProperty.SCREENDRAG) {
                Point origin = new Point();
                rootframe.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        origin.x = e.getX();
                        origin.y = e.getY();
                    }
                });
                rootframe.addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        Point p = rootframe.getLocation();
                        rootframe.setLocation(p.x + e.getX() - origin.x, p.y + e.getY() - origin.y);
                    }
                });
            }

            switch (SystemProperty.SCREENMODE) {
                case "fullscreen":
                    rootframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    rootframe.initFrame(config);
                    break;
                case "kiosk": {
                    rootframe.setUndecorated(true);
                    rootframe.getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
                    rootframe.initFrame(config);
                    break;
                }
                case "kiosk - full screen": {
                    rootframe.setUndecorated(true);
                    rootframe.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    rootframe.initFrame(config);
                    break;
                }
                default: {
                    // rootframe.setUndecorated(true);
                    rootframe.initFrame(config);
                    break;
                }
            }
        });
    }

    private static void setUIFont(FontUIResource f) {
        Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

}
