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

import uk.chromis.pos.datalogic.DataLogicSystem;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import uk.chromis.globals.SystemProperty;
import uk.chromis.basic.BasicException;
import uk.chromis.beans.JFlowPanel;
import uk.chromis.beans.JPasswordPanel;
import uk.chromis.data.loader.Session;
import uk.chromis.format.Formats;
import uk.chromis.globals.IconFactory;
import uk.chromis.commons.dialogs.InformationPane;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.commons.utils.TerminalDataLogic;
import uk.chromis.commons.utils.TerminalInfo;
import uk.chromis.pos.printer.DeviceTicket;
import uk.chromis.pos.printer.TicketParser;
import uk.chromis.pos.printer.TicketPrinterException;
import uk.chromis.pos.scale.DeviceScale;
import uk.chromis.pos.scanpal2.DeviceScanner;
import uk.chromis.pos.scanpal2.DeviceScannerFactory;
import uk.chromis.data.loader.SessionFactory;
import uk.chromis.pos.util.Hashcypher;

public class JRootApp extends JPanel implements AppView {

    private AppProperties m_props;
    private final Session session = SessionFactory.getSession();
    private TerminalDataLogic terminal;
    private DataLogicSystem m_dlSystem;

    private String m_sActiveCashIndex;
    private int m_iActiveCashSequence;
    private Date m_dActiveCashDateStart;
    private Date m_dActiveCashDateEnd;
    private String m_sInventoryLocation;
    private StringBuilder inputtext;
    private DeviceScale m_Scale;
    private DeviceScanner m_Scanner;
    private DeviceTicket m_TP;
    private TicketParser m_TTP;
    private final Map<String, BeanFactory> m_aBeanFactories;
    public static JPrincipalApp m_principalapp = null;
    private static final HashMap<String, String> m_oldclasses; // This is for backwards compatibility purposes
    public static String os;
    public static JRootApp thisInstance;

    private String m_clock;
    private String m_date;

    static {
        m_oldclasses = new HashMap<>();
    }

    private class PrintTimeAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            m_clock = getLineTimer();
            m_date = getLineDate();
            jClock.setText("     " + m_date + "  " + m_clock);
        }
    }

    private String getLineTimer() {
        return Formats.TIME.formatValue(new Date());
    }

    private String getLineDate() {
        return Formats.DATE.formatValue(new Date());
    }

    /**
     * Creates new form JRootApp
     */
    public JRootApp() {

        m_aBeanFactories = new HashMap<>();
        initComponents();

        if (SystemProperty.NEWLOGIN) {
            m_jPanelContainer.remove(m_jPanelLogin);
            m_jPanelContainer.revalidate();
            m_jPanelContainer.repaint();
            buildLogin();
        } else {
            jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(30, 30));
        }
        thisInstance = this;
    }
   
    public static JRootApp getRootInstance(){
        return thisInstance;
    }
    
    private ImageIcon scaleImage(ImageIcon icon, int w, int h) {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        if (icon.getIconWidth() > w) {
            width = w;
            height = (width * icon.getIconHeight()) / icon.getIconWidth();
        }

        if (height > h) {
            height = h;
            width = (icon.getIconWidth() * height) / icon.getIconHeight();
        }
        return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_DEFAULT));
    }

    private void buildLogin() {
        JPanel loginPanel = new JPanel(new MigLayout("insets 0 0 0 0, align center", "[center]", "0[]5[250:250:250]30[]0"));
        JLabel chromisText = new JLabel();
        JButton loginBtn = new JButton();
        JButton exitBtn = new JButton();

     //   logoLabel.setIcon(scaleImage(SystemProperty.STARTLOGO, 1024, 350));

        chromisText.setText("<html><center><b>Chromis POS - Free Open Source POS Solution</b><br>"
                + "Copyright \u00A9 2015 - 2023 Chromis <br>"
                + "http://www.chromis.co.uk<br>"
                + "<br>"
                + "Chromis POS is Open Source Software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.<br>"
                + "<br>"
                + "Chromis POS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.<br>"
                + "<br>"
                + "You should have received a copy of the GNU General Public License along with Chromis POS.  If not, see http://www.gnu.org/licenses/<br>"
                + "</center>");
        chromisText.setPreferredSize(new java.awt.Dimension(800, 300));
        chromisText.setFont(ChromisFonts.DEFAULTFONT.deriveFont(14f));

        exitBtn.setFont(ChromisFonts.DEFAULTBUTTONFONT.deriveFont(20f));
        exitBtn.setIcon(IconFactory.getIcon("exit.png"));
        exitBtn.setFocusPainted(false);
        exitBtn.setFocusable(false);
        exitBtn.setPreferredSize(new Dimension(250, 50));
        exitBtn.setRequestFocusEnabled(false);
        exitBtn.addActionListener((ActionEvent evt) -> {
            m_jCloseActionPerformed(evt);
        });

        loginBtn.setFont(ChromisFonts.DEFAULTBUTTONFONT.deriveFont(20f));
        loginBtn.setIcon(IconFactory.getIcon("enter.png"));
        loginBtn.setFocusPainted(false);
        loginBtn.setFocusable(false);
        loginBtn.setPreferredSize(new Dimension(250, 50));
        loginBtn.setRequestFocusEnabled(false);
        loginBtn.addActionListener((ActionEvent evt) -> {
            Object[] result = JAlertPane.loginDialog();
            if ((int) result[0] == 0) {
                //check if card number supplied
                AppUser m_actionuser = null;
                try {
                    m_actionuser = m_dlSystem.findPeopleByCard((String) result[1]);
                    if (m_actionuser != null) {
                        openAppView(m_actionuser);
                        return;
                    }
                } catch (BasicException e) {
                }

                try {
                    m_actionuser = m_dlSystem.findPeopleByName((String) result[1]);

                    if (m_actionuser != null && !((String) result[2]).isBlank()) {
                        if (Hashcypher.authenticate((String) result[2], m_actionuser.getPassword())) {
                            terminal.loginUser(m_actionuser);
                            openAppView(m_actionuser);
                            return;
                        }
                    }
                } catch (BasicException ex) {
                    Logger.getLogger(JRootApp.class.getName()).log(Level.SEVERE, null, ex);
                }
                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.unabletologin"), 14,
                        new Dimension(100, 50), JAlertPane.OK_OPTION);
            }
        });

        loginPanel.add("wrap", new JLabel(scaleImage(SystemProperty.STARTLOGO, 1024, 350)));
        loginPanel.add("wrap", chromisText);
        loginPanel.add("split 2, gapright 45", loginBtn);
        loginPanel.add(exitBtn);

        m_jPanelContainer.add(loginPanel);
    }

    public static final int INIT_SUCCESS = 0;
    public static final int INIT_FAIL_CONFIG = 1;
    public static final int INIT_FAIL_EXIT = 2;
    public static final int INIT_FAIL_RETRY = 3;

    /**
     *
     * @param props
     * @return
     */
    public int initApp(AppProperties props) {

        m_props = props;
        m_dlSystem = (DataLogicSystem) getBean("uk.chromis.pos.datalogic.DataLogicSystem");
        terminal = new TerminalDataLogic();
        terminal.setTerminalVersion();

        // support for different component orientation languages.
        applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));

        try {
            String sActiveCashIndex = terminal.getActiveCash();
            Object[] valcash = sActiveCashIndex == null
                    ? null
                    : m_dlSystem.findActiveCash(sActiveCashIndex);
            if (valcash == null || !TerminalInfo.getTerminalName().equals(valcash[0])) {
                setActiveCash(UUID.randomUUID().toString(), m_dlSystem.getSequenceCash(TerminalInfo.getTerminalName()), new Date(), null);
                m_dlSystem.execInsertCash(
                        new Object[]{getActiveCashIndex(), TerminalInfo.getTerminalName(), getActiveCashSequence(), getActiveCashDateStart(), getActiveCashDateEnd(), 0});
            } else {
                setActiveCash(sActiveCashIndex, (Integer) valcash[1], (Date) valcash[2], (Date) valcash[3]);
            }
        } catch (BasicException e) {

        }

        m_sInventoryLocation = terminal.getTerminalLocation();
        try {
            if (m_dlSystem.findLocationName(m_sInventoryLocation) == null) {
                m_sInventoryLocation = null;
            }
        } catch (BasicException ex) {
            Logger.getLogger(JRootApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (m_sInventoryLocation == null) {
            m_sInventoryLocation = "MainStore";
            terminal.setTerminalLocation(m_sInventoryLocation);
        }

        // setup the display
        m_TP = new DeviceTicket(this, m_props);
        m_TTP = new TicketParser(getDeviceTicket(), m_dlSystem);

        printerStart();

        m_Scale = new DeviceScale(this, m_props);

        m_Scanner = DeviceScannerFactory.createInstance(m_props);
        
        new javax.swing.Timer(250, new PrintTimeAction()).start();

        String sWareHouse;

        try {
            sWareHouse = m_dlSystem.findLocationName(m_sInventoryLocation);
        } catch (BasicException e) {
            sWareHouse = null;
        }

        jLabel1.setIcon(SystemProperty.STARTLOGO);

        showLogin();

        //check for autologon file
        if ((new File("autologon.user")).exists()) {
            Properties prop = new Properties();

            try {                
                ByteArrayInputStream bis = new ByteArrayInputStream(decrypt(new File("autologon.user")));
                prop.load(bis);

                if (prop.getProperty("terminal.id").equals(TerminalInfo.getTerminalID())) {
                    AppUser m_actionuser = null;
                    m_actionuser = m_dlSystem.findPeopleByName(prop.getProperty("system.user"));
                    if (Hashcypher.authenticate(prop.getProperty("user.password"), m_actionuser.getPassword())) {
                        terminal.loginUser(m_actionuser);
                        m_jPanelTitle.setVisible(false);
                        openAppView(m_actionuser);
                    }
                }
            } catch (IOException ex) {
            } catch (BasicException ex) {
                Logger.getLogger(JRootApp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(JRootApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        return INIT_SUCCESS;
    }
	
    public static byte[] decrypt(File inputFile) throws Exception {
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            byte[] keyBytes = new byte[16];
            inputStream.read(keyBytes);
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] inputBytes = new byte[(int) inputFile.length() - 16];
            inputStream.read(inputBytes);
            byte[] outputBytes = cipher.doFinal(inputBytes);
            ByteArrayInputStream inputStreamBytes = new ByteArrayInputStream(outputBytes);
            return outputBytes;
        }

    }

    public static JPrincipalApp getPricipalApp() {
        return m_principalapp;
    }

    private String readDataBaseVersion() {
        try {
            return m_dlSystem.findVersion();
        } catch (BasicException ed) {
            return null;
        }
    }

    private String getServerIP() {
        StringBuilder server = new StringBuilder(AppConfig.getString("database.server"));
        server.append(":");
        server.append(AppConfig.getString("database.port"));
        return server.toString();
    }

    public void tryToClose() {
        if (closeAppView()) {
            m_TP.getDeviceDisplay().clearVisor();
            SwingUtilities.getWindowAncestor(this).dispose();
        }
    }

    @Override
    public DeviceTicket getDeviceTicket() {
        return m_TP;
    }

    @Override
    public DeviceScale getDeviceScale() {
        return m_Scale;
    }

    @Override
    public DeviceScanner getDeviceScanner() {
        return m_Scanner;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public String getInventoryLocation() {
        return m_sInventoryLocation;
    }

    @Override
    public String getActiveCashIndex() {
        return m_sActiveCashIndex;
    }

    @Override
    public int getActiveCashSequence() {
        return m_iActiveCashSequence;
    }

    @Override
    public Date getActiveCashDateStart() {
        return m_dActiveCashDateStart;
    }

    /**
     *
     * @return
     */
    @Override
    public Date getActiveCashDateEnd() {
        return m_dActiveCashDateEnd;
    }

    /**
     *
     * @param sIndex
     * @param iSeq
     * @param dStart
     * @param dEnd
     */
    @Override
    public void setActiveCash(String sIndex, int iSeq, Date dStart, Date dEnd) {
        m_sActiveCashIndex = sIndex;
        m_iActiveCashSequence = iSeq;
        m_dActiveCashDateStart = dStart;
        m_dActiveCashDateEnd = dEnd;
        terminal.setActiveCash(m_sActiveCashIndex);
    }

    /**
     *
     * @return
     */
    @Override
    public AppProperties getProperties() {
        return m_props;
    }

    /**
     *
     * @param beanfactory
     * @return
     * @throws BeanFactoryException
     */
    @Override
    public Object getBean(String beanfactory) throws BeanFactoryException {
        beanfactory = mapNewClass(beanfactory);
        BeanFactory bf = (BeanFactory) this.m_aBeanFactories.get(beanfactory);
        if (bf == null) {
            try {
                Class bfclass = Class.forName(beanfactory);
                if (BeanFactory.class.isAssignableFrom(bfclass)) {
                    bf = (BeanFactory) bfclass.getDeclaredConstructor().newInstance();
                } else {
                    Constructor constMyView = bfclass.getConstructor(new Class[]{AppView.class});
                    Object bean = constMyView.newInstance(new Object[]{this});
                    bf = new BeanFactoryObj(bean);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
                throw new BeanFactoryException(e);
            }
            this.m_aBeanFactories.put(beanfactory, bf);
            if ((bf instanceof BeanFactoryApp)) {
                ((BeanFactoryApp) bf).init(this);
            }
        }
        return bf.getBean();
    }

    private static String mapNewClass(String classname) {
        String newclass = m_oldclasses.get(classname);
        return newclass == null
                ? classname
                : newclass;
    }

    /**
     *
     */
    @Override
    public void waitCursorBegin() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     *
     */
    @Override
    public void waitCursorEnd() {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     *
     * @return
     */
    @Override
    public AppUserView getAppUserView() {
        return m_principalapp;
    }

    private void printerStart() {
        String sresource = m_dlSystem.getResourceAsXML("Display.Start");
        if (sresource == null) {
            m_TP.getDeviceDisplay().writeVisor(AppLocal.APP_NAME, AppLocal.APP_VERSION);
        } else {
            try {
                m_TTP.printTicket(sresource);
            } catch (TicketPrinterException eTP) {
                m_TP.getDeviceDisplay().writeVisor(AppLocal.APP_NAME, AppLocal.APP_VERSION);
            }
        }
    }

    private void listPeople() {
        try {
            jScrollPane1.getViewport().setView(null);
            JFlowPanel jPeople = new JFlowPanel();
            jPeople.applyComponentOrientation(getComponentOrientation());
            List people = m_dlSystem.listPeopleVisible();

            for (int i = 0; i < people.size(); i++) {

                AppUser user = (AppUser) people.get(i);
                JButton btn = new JButton(new AppUserAction(user));
                btn.applyComponentOrientation(getComponentOrientation());
                btn.setFocusPainted(false);
                btn.setFocusable(false);
                btn.setRequestFocusEnabled(false);
                btn.setMaximumSize(new Dimension(130, 60));
                btn.setPreferredSize(new Dimension(130, 60));
                btn.setMinimumSize(new Dimension(130, 60));
                btn.setHorizontalAlignment(SwingConstants.CENTER);
                btn.setHorizontalTextPosition(AbstractButton.CENTER);
                btn.setVerticalTextPosition(AbstractButton.BOTTOM);
                jPeople.add(btn);
                btn.setFont(btn.getFont().deriveFont(16f));
            }
            jScrollPane1.getViewport().setView(jPeople);

        } catch (BasicException ee) {
        }
    }

    private class AppUserAction extends AbstractAction {

        private final AppUser m_actionuser;

        public AppUserAction(AppUser user) {
            m_actionuser = user;
            putValue(Action.SMALL_ICON, m_actionuser.getIcon());
            putValue(Action.NAME, m_actionuser.getName());
        }

        public AppUser getUser() {
            return m_actionuser;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (m_actionuser.authenticate()) {
                terminal.loginUser(m_actionuser);
                openAppView(m_actionuser);
            } else {
                Object[] result = JPasswordPanel.requestPassword(m_actionuser.getName());
                String sPassword = (String) result[1];
                if (sPassword != null) {
                    if (m_actionuser.authenticate(sPassword)) {
                        terminal.loginUser(m_actionuser);
                        openAppView(m_actionuser);
                    } else {
                        JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.badPassword"), 16,
                                new Dimension(100, 50), JAlertPane.OK_OPTION);
                    }
                }
            }
        }

    }

    private void showView(String view) {
        CardLayout cl = (CardLayout) (m_jPanelContainer.getLayout());
        cl.show(m_jPanelContainer, view);
    }

    private void openAppView(AppUser user) {
        if (closeAppView()) {
            m_principalapp = new JPrincipalApp(this, user);
            m_jPanelContainer.add(m_principalapp, "_" + m_principalapp.getUser().getId());
            showView("_" + m_principalapp.getUser().getId());

            m_principalapp.activate();
        }
    }

    public void exitToLogin() {
        closeAppView();
        showLogin();
    }

    /**
     *
     * @return
     */
    public boolean closeAppView() {
        if (m_principalapp == null) {
            return true;
        } else if (!m_principalapp.deactivate()) {
            return false;
        } else {
            m_jPanelContainer.remove(m_principalapp);
            m_principalapp = null;
            terminal.logoutUser();
            showLogin();
            return true;
        }
    }

    private void showLogin() {
        // Show Login
        listPeople();
        showView("login");

        printerStart();

        // keyboard listener activation
        inputtext = new StringBuilder();
        m_txtKeys.setText(null);
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_txtKeys.requestFocus();
            }
        });
    }

    private void processKey(char c) {
        if ((c == '\n') || (c == '?')) {
            AppUser user = null;
            try {
                user = m_dlSystem.findPeopleByCard(inputtext.toString());
            } catch (BasicException e) {
            }

            if (user == null) {
                // user not found
                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.nocard"), 16,
                        new Dimension(100, 50), JAlertPane.OK_OPTION);
            } else {
                openAppView(user);
            }
            inputtext = new StringBuilder();
        } else {
            inputtext.append(c);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jPanelTitle = new javax.swing.JPanel();
        m_jLblTitle = new javax.swing.JLabel();
        poweredby = new javax.swing.JLabel();
        jClock = new javax.swing.JLabel();
        m_jPanelContainer = new javax.swing.JPanel();
        m_jPanelLogin = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 0));
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        m_jLogonName = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        m_txtKeys = new javax.swing.JTextField();
        m_jClose = new javax.swing.JButton();

        setEnabled(false);
        setPreferredSize(new java.awt.Dimension(1024, 738));
        setLayout(new java.awt.BorderLayout());

        m_jPanelTitle.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, javax.swing.UIManager.getDefaults().getColor("button.darkShadow")));
        m_jPanelTitle.setLayout(new java.awt.BorderLayout());

        m_jLblTitle.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jLblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jPanelTitle.add(m_jLblTitle, java.awt.BorderLayout.CENTER);

        poweredby.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        poweredby.setIcon(IconFactory.getIcon("poweredby.png"));
        poweredby.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        poweredby.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        poweredby.setMaximumSize(new java.awt.Dimension(222, 34));
        poweredby.setPreferredSize(new java.awt.Dimension(180, 34));
        poweredby.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                poweredbyMouseClicked(evt);
            }
        });
        m_jPanelTitle.add(poweredby, java.awt.BorderLayout.LINE_END);

        jClock.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(18f));
        jClock.setForeground(new java.awt.Color(102, 102, 102));
        jClock.setPreferredSize(new java.awt.Dimension(280, 34));
        jClock.setRequestFocusEnabled(false);
        m_jPanelTitle.add(jClock, java.awt.BorderLayout.LINE_START);

        add(m_jPanelTitle, java.awt.BorderLayout.NORTH);

        m_jPanelContainer.setLayout(new java.awt.CardLayout());

        m_jPanelLogin.setLayout(new java.awt.BorderLayout());

        jPanel4.setMinimumSize(new java.awt.Dimension(518, 177));
        jPanel4.setPreferredSize(new java.awt.Dimension(518, 177));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));
        jPanel4.add(filler2);

        jLabel1.setFont(ChromisFonts.DEFAULTFONT.deriveFont(12f));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(IconFactory.getIcon("chromislogo.png"));
        jLabel1.setText("<html><center><b>ChromisEC - Sistema de Punto de Venta para Ecuador</b><br>" +
            "Derechos Reservados &#169; 2015 - 2023 Chromis &amp; Contribuidores<br>" +
            "Bifurcaci\u00f3n desarrollada para Ecuador por riccijandro<br>" +
            "https://github.com/riccijandro/chromisposec<br>" +
            "<br>" +
            "ChromisEC es Software de C\u00f3digo Abierto: puede redistribuirlo y/o modificarlo bajo los t\u00e9rminos de la Licencia P\u00fablica General GNU, publicada por la Free Software Foundation, en su versi\u00f3n 3 o posterior.<br>" +
            "<br>" +
            "ChromisEC se distribuye con la esperanza de que sea \u00fatil, pero SIN NINGUNA GARANT\u00cdA; ni siquiera la garant\u00eda impl\u00edcita de COMERCIABILIDAD o IDONEIDAD PARA UN PROP\u00d3SITO PARTICULAR. Consulte la Licencia P\u00fablica General GNU para m\u00e1s detalles.<br>" +
            "<br>" +
            "Deber\u00eda haber recibido una copia de la Licencia P\u00fablica General GNU junto con ChromisEC. Si no, consulte http://www.gnu.org/licenses/<br>" +
            "</center>");
        jLabel1.setAlignmentX(0.5F);
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel1.setMaximumSize(new java.awt.Dimension(800, 1024));
        jLabel1.setMinimumSize(new java.awt.Dimension(518, 177));
        jLabel1.setPreferredSize(new java.awt.Dimension(518, 177));
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jPanel4.add(jLabel1);

        m_jPanelLogin.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(300, 400));

        m_jLogonName.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jLogonName.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridLayout(0, 1, 5, 5));
        jPanel2.add(jPanel8, java.awt.BorderLayout.NORTH);

        m_jLogonName.add(jPanel2, java.awt.BorderLayout.LINE_END);

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setFont(ChromisFonts.DEFAULTFONT.deriveFont(12f));

        m_txtKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_txtKeys.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_txtKeysKeyTyped(evt);
            }
        });

        m_jClose.setFont(ChromisFonts.DEFAULTBUTTONFONT.deriveFont(20f));
        m_jClose.setIcon(IconFactory.getIcon("exit.png"));
        m_jClose.setFocusPainted(false);
        m_jClose.setFocusable(false);
        m_jClose.setPreferredSize(new java.awt.Dimension(100, 50));
        m_jClose.setRequestFocusEnabled(false);
        m_jClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jCloseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(m_jClose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 259, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(m_txtKeys, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(313, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(m_txtKeys, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(m_jClose, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 281, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(104, 104, 104)
                .add(m_jLogonName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .add(15, 15, 15)
                .add(m_jLogonName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(434, 608, Short.MAX_VALUE))
            .add(jPanel5Layout.createSequentialGroup()
                .add(jScrollPane1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        m_jPanelLogin.add(jPanel5, java.awt.BorderLayout.EAST);

        m_jPanelContainer.add(m_jPanelLogin, "login");

        add(m_jPanelContainer, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    private void m_jCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jCloseActionPerformed
        tryToClose();
    }//GEN-LAST:event_m_jCloseActionPerformed

    private void m_txtKeysKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtKeysKeyTyped
        m_txtKeys.setText("0");
        processKey(evt.getKeyChar());
    }//GEN-LAST:event_m_txtKeysKeyTyped

    private void poweredbyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_poweredbyMouseClicked

        int mb = 1024 * 1024;
        Runtime runtime = Runtime.getRuntime();

        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        DefaultTableModel valueModel = new DefaultTableModel();
        JTable table = new JTable(valueModel);
        table.setShowGrid(false);

        valueModel.addColumn("Details");
        valueModel.addColumn("Value");
        table.getColumn("Details").setMinWidth(150);
        table.getColumn("Details").setMaxWidth(150);
        table.getColumn("Value").setMaxWidth(380);
        table.getColumn("Value").setMinWidth(380);
        valueModel.addRow(new Object[]{"Locale Setting", Locale.getDefault().toString()});
        valueModel.addRow(new Object[]{"Terminal ID ", AppConfig.getString("terminalID")});
        valueModel.addRow(new Object[]{"Database Server", getServerIP()});
        valueModel.addRow(new Object[]{"Database Name", AppConfig.getDatabaseName()});
        valueModel.addRow(new Object[]{"Database Version", readDataBaseVersion()});

//        String test = "Terminal 654";
//
//        System.out.println(UUID.nameUUIDFromBytes(test.getBytes()));
//
//        System.out.println(test.hashCode());
//        Properties f = System.getProperties();
        valueModel.addRow(new Object[]{"Java Home", System.getProperty("java.home")});
        valueModel.addRow(new Object[]{"Java Version", System.getProperty("java.version")});
        valueModel.addRow(new Object[]{"Java RunTime Version", System.getProperty("java.runtime.version")});
        valueModel.addRow(new Object[]{"Java Vendor", System.getProperty("java.vendor")});
        valueModel.addRow(new Object[]{"Java Vendor URL ", System.getProperty("java.vendor.url")});
        valueModel.addRow(new Object[]{"Operating System", System.getProperty("os.name")});
        // model.addRow(new Object[]{"Sync library", Sync.getVersion()});
        valueModel.addRow(new Object[]{"Memory Used", ((runtime.totalMemory() - runtime.freeMemory()) / mb) + " MB"});
        valueModel.addRow(new Object[]{"Total Memory Allocated", (runtime.totalMemory() / mb) + " MB"});
        valueModel.addRow(new Object[]{"Max. Memory Available", (runtime.maxMemory() / mb) + " MB"});
        contentPanel.add(table);
        InformationPane.showInformationDialog(true, contentPanel, true, true, this);


    }//GEN-LAST:event_poweredbyMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler2;
    private javax.swing.JLabel jClock;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton m_jClose;
    private javax.swing.JLabel m_jLblTitle;
    private javax.swing.JPanel m_jLogonName;
    private javax.swing.JPanel m_jPanelContainer;
    private javax.swing.JPanel m_jPanelLogin;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JTextField m_txtKeys;
    private javax.swing.JLabel poweredby;
    // End of variables declaration//GEN-END:variables
}
