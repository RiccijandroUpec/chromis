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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import uk.chromis.basic.BasicException;
import uk.chromis.beans.JPasswordPanel;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.data.gui.JMessageDialog;
import uk.chromis.data.gui.MessageInf;
import uk.chromis.globals.IconFactory;
import uk.chromis.globals.SystemProperty;
import uk.chromis.pos.customers.CustomerInfo;
import uk.chromis.pos.customers.CustomersPayment;
import static uk.chromis.pos.forms.JRootApp.m_principalapp;
import uk.chromis.pos.panels.JPanelPayments;
import uk.chromis.pos.scripting.ScriptEngine;
import uk.chromis.pos.scripting.ScriptException;
import uk.chromis.pos.scripting.ScriptFactory;
import uk.chromis.pos.util.Hashcypher;
import uk.chromis.pos.util.StringUtils;

public class JPrincipalApp extends javax.swing.JPanel implements AppUserView {

    private static final Logger logger = Logger.getLogger("uk.chromis.pos.forms.JPrincipalApp");

    private final JRootApp m_appview;
    private final AppUser m_appuser;

    private DataLogicSystem m_dlSystem;

    private JLabel m_principalnotificator;

    private JPanelView m_jLastView;
    private Action m_actionfirst;

    private Map<String, JPanelView> m_aPreparedViews; // Prepared views   
    private Map<String, JPanelView> m_aCreatedViews;

    private Icon menu_open;
    private Icon menu_close;

    private List<MenuList> menuList = new ArrayList<>();
    private JPopupMenu actionMenu;

    private CustomerInfo customerInfo;

    public JPrincipalApp(JRootApp appview, AppUser appuser) {

        initComponents();

        m_jPanelLeft.setVisible(false);

        menuList = new ArrayList<>();
        actionMenu = new JPopupMenu("Menu");
        actionMenu.removeAll();

        m_appview = appview;
        m_appuser = appuser;

        m_dlSystem = (DataLogicSystem) m_appview.getBean("uk.chromis.pos.datalogic.DataLogicSystem");

        m_appuser.fillPermissions(m_dlSystem);

        m_actionfirst = null;
        m_jLastView = null;

        m_aPreparedViews = new HashMap<>();
        m_aCreatedViews = new HashMap<>();

        applyComponentOrientation(appview.getComponentOrientation());

        m_principalnotificator = new JLabel();
        m_principalnotificator.applyComponentOrientation(getComponentOrientation());
        m_principalnotificator.setText(m_appuser.getName());
        m_principalnotificator.setIcon(m_appuser.getIcon());

        m_jPanelContainer.add(new JPanel(), "<NULL>");
        showView("<NULL>");

                try {
            // CHROMIS ECUADOR: Intentar cargar Menu.Root.txt local primero (menu completo)
            String menuText = StringUtils.readResource("/uk/chromis/pos/templates/Menu.Root.txt");
            if (menuText == null || menuText.trim().isEmpty()) {
                throw new Exception("Menu.Root.txt is empty");
            }
            menuList.clear();
            m_jPanelLeft.setViewportView(getScriptMenu(menuText));
        } catch (Exception e) {
            // Fallback: cargar desde base de datos
            try {
                String menuText = m_dlSystem.getResourceAsText("Menu.Root");
                if (menuText == null || menuText.trim().isEmpty()) {
                    throw new ScriptException("Menu.Root is empty in database");
                }
                m_jPanelLeft.setViewportView(getScriptMenu(menuText));
            } catch (ScriptException ex) {
                try {
                    menuList.clear();
                    m_jPanelLeft.setViewportView(getScriptMenu(StringUtils.readResource("/uk/chromis/pos/templates/Menu.Root.txt")));
                } catch (IOException | ScriptException ex2) {
                    logger.log(Level.SEVERE, "Cannot read default menu", ex2);
                }
            }
        }

        for (int j = 0; j < menuList.size(); j++) {
            if (menuList.get(j).getMenuClass().equals("uk.chromis.pos.customers.CustomersPayment")) {
                JMenuItem cPayment;
                try {
                    cPayment = new JMenuItem(AppLocal.getIntString("menu.customerPayment"), IconFactory.getResizedIcon("customerpay.png", new Dimension(30, 30)));
                } catch (Exception ex) {
                    cPayment = new JMenuItem(AppLocal.getIntString("menu.customerPayment"));
                }
                actionMenu.add(cPayment);
                cPayment.addActionListener((ActionEvent ev)
                        -> new CustomersPayment(m_appview)
                );
            } else if (menuList.get(j).getMenuClass().equals("uk.chromis.pos.panels.JPanelPayments")) {
                JMenuItem payment;
                try {
                    payment = new JMenuItem(AppLocal.getIntString("menu.payments"), IconFactory.getResizedIcon("payments.png", new Dimension(30, 30)));
                } catch (Exception ex) {
                    payment = new JMenuItem(AppLocal.getIntString("menu.payments"));
                }
                actionMenu.add(payment);
                payment.addActionListener((ActionEvent ev)
                        -> new JPanelPayments(m_appview)
                );
            } else {
                addItem(menuList.get(j).getMenuTitle(), menuList.get(j).getMenuClass(), menuList.get(j).getImageIcon());
            }
        }

        JMenuItem password;
        try {
            password = new JMenuItem("Change Password", IconFactory.getResizedIcon("password.png", new Dimension(30, 30)));
        } catch (Exception ex) {
            password = new JMenuItem("Change Password");
        }
        actionMenu.add(password);
        password.setPreferredSize(new Dimension(200, 40));
        password.addActionListener((ActionEvent ev) -> {
            m_principalapp.changePassword();
        });
        actionMenu.addSeparator();

// --- ChromisPOS Ecuador: About Dialog ---
        JMenuItem aboutItem;
        try {
            aboutItem = new JMenuItem("Acerca de ChromisPOS", IconFactory.getResizedIcon("chromislogo.png", new Dimension(30, 30)));
        } catch (Exception ex) {
            aboutItem = new JMenuItem("Acerca de ChromisPOS");
        }
        actionMenu.add(aboutItem);
        aboutItem.setPreferredSize(new Dimension(250, 40));
        aboutItem.addActionListener((ActionEvent ev) -> {
            AboutDialog about = new AboutDialog((JFrame) SwingUtilities.getWindowAncestor(m_principalapp));
            about.setVisible(true);
        });

// --- ChromisPOS Ecuador: Backup & Restore ---
        JMenuItem backupItem;
        try {
            backupItem = new JMenuItem("Respaldo y Restauracion", IconFactory.getResizedIcon("database.png", new Dimension(30, 30)));
        } catch (Exception ex) {
            backupItem = new JMenuItem("Respaldo y Restauracion");
        }
        actionMenu.add(backupItem);
        backupItem.setPreferredSize(new Dimension(250, 40));
        backupItem.addActionListener((ActionEvent ev) -> {
            BackupRestoreDialog backup = new BackupRestoreDialog((JFrame) SwingUtilities.getWindowAncestor(m_principalapp));
            backup.setVisible(true);
        });

//        JMenuItem paymentTest;
//        try {
//            paymentTest = new JMenuItem("Payment Test", IconFactory.getResizedIcon("password.png", new Dimension(30, 30)));
//        } catch (Exception ex) {
//            paymentTest = new JMenuItem("Payment Test");
//        }
//        actionMenu.add(paymentTest);
//        paymentTest.setPreferredSize(new Dimension(200, 40));
//        paymentTest.addActionListener((ActionEvent ev)
//                -> new JPaymentsPanel(m_appview)
//        );
    }

    private void addItem(String itemMessage, String action, ImageIcon imageIcon) {
        JMenuItem tmpItem = new JMenuItem(itemMessage);
        if (imageIcon != null) {
            try {
                Image image = imageIcon.getImage();
                Image newimg = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);
                tmpItem = new JMenuItem(itemMessage, new ImageIcon(newimg));
            } catch (Exception ex) {
                tmpItem = new JMenuItem(itemMessage);
            }
        }
        actionMenu.add(tmpItem);
        tmpItem.setPreferredSize(new Dimension(300, 40));
        tmpItem.addActionListener((ActionEvent ev) -> {
            m_principalapp.showTask(action);
        });
    }

    public class MenuList {

        private String menu = null;
        private String menuTitle = null;
        private String menuClass = null;
        private ImageIcon imageIcon = null;

        private MenuList() {

        }

        private MenuList(String menu, String menuTitle, String menuClass, ImageIcon imageIcon) {
            this.menu = menu;
            this.menuTitle = menuTitle;
            this.menuClass = menuClass;
            this.imageIcon = imageIcon;
        }

        public String getMenu() {
            return menu;
        }

        public String getMenuTitle() {
            return menuTitle;
        }

        public String getMenuClass() {
            return menuClass;
        }

        public ImageIcon getImageIcon() {
            return imageIcon;
        }

    }

    public void setMenuVisible(JButton jButton) {
        actionMenu.show(jButton, jButton.getWidth() / 2, jButton.getHeight() / 2);
    }

    public void showSalesButton(Boolean value) {
        jBtnSales.setVisible(value);
    }

    private Component getScriptMenu(String menutext) throws ScriptException {

        ScriptMenu menu = new ScriptMenu();

        ScriptEngine eng = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
        eng.put("menu", menu);
        eng.eval(menutext);
        return menu.getTaskPane();
    }

    public class ScriptMenu {

        private final JXTaskPaneContainer taskPane;

        private ScriptMenu() {
            taskPane = new JXTaskPaneContainer();
            taskPane.applyComponentOrientation(getComponentOrientation());
        }

        public ScriptGroup addGroup(String key) {

            ScriptGroup group = new ScriptGroup(key);
            taskPane.add(group.getTaskGroup());
            return group;
        }

        public JXTaskPaneContainer getTaskPane() {
            return taskPane;
        }
    }

    public class ScriptGroup {

        private final JXTaskPane taskGroup;

        private ScriptGroup(String key) {
            taskGroup = new JXTaskPane();
            taskGroup.applyComponentOrientation(getComponentOrientation());
            taskGroup.setFocusable(false);
            taskGroup.setRequestFocusEnabled(false);
            taskGroup.setTitle(AppLocal.getIntString(key));
            taskGroup.setVisible(false);
        }

        public void addPanel(String icon, String key, String classname) {
            //Feature added for Kidsgrove Tropicals - allows the use of fobs for user has the rights
            if (!(SystemProperty.USEFOBS && classname.equals("uk.chromis.pos.panels.JPanelCloseMoney"))) {
                addAction(new MenuPanelAction(m_appview, icon, key, classname));
            } else {

            }
        }

        public void addExecution(String icon, String key, String classname) {
            addAction(new MenuExecAction(m_appview, icon, key, classname));
        }

        public ScriptSubmenu addSubmenu(String icon, String key, String classname) {
            ScriptSubmenu submenu = new ScriptSubmenu(key);
            m_aPreparedViews.put(classname, new JPanelMenu(submenu.getMenuDefinition()));
            addAction(new MenuPanelAction(m_appview, icon, key, classname));
            return submenu;
        }

        private void addAction(Action act) {
            if (AppUser.hasPermission((String) act.getValue(AppUserView.ACTION_TASKNAME))) {
                menuList.add(new MenuList(taskGroup.getTitle(), (String) act.getValue(Action.NAME), (String) act.getValue(AppUserView.ACTION_TASKNAME), (ImageIcon) act.getValue(Action.SMALL_ICON)));
                if (m_actionfirst == null) {
                    m_actionfirst = act;
                }
            }
        }

        public JXTaskPane getTaskGroup() {
            return taskGroup;
        }
    }

    public class ScriptSubmenu {

        private final MenuDefinition menudef;

        private ScriptSubmenu(String key) {
            menudef = new MenuDefinition(key);
        }

        public void addTitle(String key) {
            menudef.addMenuTitle(key);
        }

        public void addPanel(String icon, String key, String classname) {
            menudef.addMenuItem(new MenuPanelAction(m_appview, icon, key, classname));
        }

        public void addExecution(String icon, String key, String classname) {
            menudef.addMenuItem(new MenuExecAction(m_appview, icon, key, classname));
        }

        public ScriptSubmenu addSubmenu(String icon, String key, String classname) {
            ScriptSubmenu submenu = new ScriptSubmenu(key);
            m_aPreparedViews.put(classname, new JPanelMenu(submenu.getMenuDefinition()));
            menudef.addMenuItem(new MenuPanelAction(m_appview, icon, key, classname));
            return submenu;
        }

        public MenuDefinition getMenuDefinition() {
            return menudef;
        }
    }

    public JComponent getNotificator() {
        return m_principalnotificator;
    }

    public void activate() {
        if (m_actionfirst != null) {
            m_actionfirst.actionPerformed(null);
            m_actionfirst = null;
        }
    }

    public boolean deactivate() {
        if (m_jLastView == null) {
            return true;
        } else if (m_jLastView.deactivate()) {
            m_jLastView = null;
            showView("<NULL>");
            return true;
        } else {
            return false;
        }

    }

    private class ExitAction extends AbstractAction {

        public ExitAction(String icon, String keytext) {
            putValue(Action.SMALL_ICON, new ImageIcon(JPrincipalApp.class.getResource(icon)));
            putValue(Action.NAME, AppLocal.getIntString(keytext));
            putValue(AppUserView.ACTION_TASKNAME, keytext);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            m_appview.closeAppView();
        }
    }

    public void exitToLogin() {
        m_appview.closeAppView();
    }

    public void changePassword() {
        Object[] result = JPasswordPanel.changePassword(m_appuser);
        if ((int) result[0] == 0) {
            String sNewPassword = Hashcypher.hashString((String) result[1]);
            if (sNewPassword != null) {
                try {
                    m_dlSystem.execChangePassword(new Object[]{sNewPassword, m_appuser.getId()});
                    m_appuser.setPassword(sNewPassword);
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.passwordchanged"), 16,
                            new Dimension(100, 50), JAlertPane.OK_OPTION);
                } catch (BasicException e) {
                    JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.cannotchangepassword"), 16,
                            new Dimension(100, 50), JAlertPane.OK_OPTION);
                }
            }
        }
    }

    private void showView(String sView) {
        CardLayout cl = (CardLayout) (m_jPanelContainer.getLayout());
        cl.show(m_jPanelContainer, sView);
    }

    @Override
    public AppUser getUser() {
        return m_appuser;
    }

    @Override
    public void showTask(String sTaskClass) {

        customerInfo = new CustomerInfo("");
        customerInfo.setName("");

        m_appview.waitCursorBegin();
        if (AppUser.hasPermission(sTaskClass)) {
            JPanelView m_jMyView = (JPanelView) m_aCreatedViews.get(sTaskClass);

            if (m_jLastView == null || (m_jMyView != m_jLastView && m_jLastView.deactivate())) {

                // Construct the new view
                if (m_jMyView == null) {

                    // Is the view prepared
                    m_jMyView = m_aPreparedViews.get(sTaskClass);
                    if (m_jMyView == null) {
                        // The view is not prepared. Try to get as a Bean...
                        try {
                            m_jMyView = (JPanelView) m_appview.getBean(sTaskClass);
                        } catch (BeanFactoryException e) {
                            m_jMyView = new JPanelNull(m_appview, e);
                        }
                    }

                    m_jMyView.getComponent().applyComponentOrientation(getComponentOrientation());
                    m_jPanelContainer.add(m_jMyView.getComponent(), sTaskClass);
                    m_aCreatedViews.put(sTaskClass, m_jMyView);
                }

                try {
                    m_jMyView.activate();
                } catch (BasicException e) {
                    JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.notactive"), e));
                }

                m_jLastView = m_jMyView;
                showView(sTaskClass);
                String sTitle = m_jMyView.getTitle();
                m_jPanelTitle.setVisible(sTitle != null);
                m_jTitle.setText(sTitle);
            }
        } else {
            JAlertPane.messageBox(new Dimension(350, 100), JAlertPane.WARNING, AppLocal.getIntString("message.notpermissions"), 16,
                    new Dimension(100, 50), JAlertPane.OK_OPTION);
        }
        m_appview.waitCursorEnd();
    }

    @Override
    public void executeTask(String sTaskClass) {
        m_appview.waitCursorBegin();

        if (AppUser.hasPermission(sTaskClass)) {
            try {
                ProcessAction myProcess = (ProcessAction) m_appview.getBean(sTaskClass);

                try {
                    MessageInf m = myProcess.execute();
                    if (m != null) {
                        JMessageDialog.showMessage(JPrincipalApp.this, m);
                    }
                } catch (BasicException eb) {
                    JMessageDialog.showMessage(JPrincipalApp.this, new MessageInf(eb));
                }
            } catch (BeanFactoryException e) {
                JAlertPane.messageBox(new Dimension(350, 100), JAlertPane.WARNING, AppLocal.getIntString("message.notpermissions"), 16,
                        new Dimension(100, 50), JAlertPane.OK_OPTION);
            }
        } else {
            JAlertPane.messageBox(new Dimension(350, 100), JAlertPane.WARNING, AppLocal.getIntString("message.notpermissions") + " " + sTaskClass, 16,
                    new Dimension(100, 50), JAlertPane.OK_OPTION);

        }
        m_appview.waitCursorEnd();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jPanelLeft = new javax.swing.JScrollPane();
        m_jPanelRight = new javax.swing.JPanel();
        m_jPanelTitle = new javax.swing.JPanel();
        btnActionMenu = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jBtnSales = new javax.swing.JButton();
        m_jTitle = new javax.swing.JLabel();
        m_jPanelContainer = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setFont(ChromisFonts.DEFAULTFONT.deriveFont(12f)
        );
        jPanel1.setLayout(new java.awt.BorderLayout());

        m_jPanelLeft.setMinimumSize(new java.awt.Dimension(23, 0));
        jPanel1.add(m_jPanelLeft, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.LINE_START);

        m_jPanelRight.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        m_jPanelRight.setFont(ChromisFonts.DEFAULTFONT.deriveFont(12f));
        m_jPanelRight.setLayout(new java.awt.BorderLayout());

        m_jPanelTitle.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));

        btnActionMenu.setIcon(IconFactory.getIcon("menu.png"));
        btnActionMenu.setMaximumSize(new java.awt.Dimension(18, 18));
        btnActionMenu.setMinimumSize(new java.awt.Dimension(32, 32));
        btnActionMenu.setPreferredSize(new java.awt.Dimension(52, 40));
        btnActionMenu.setRequestFocusEnabled(false);
        btnActionMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActionMenuActionPerformed(evt);
            }
        });

        jButton1.setIcon(IconFactory.getIcon("logout.png"));
        jButton1.setPreferredSize(new java.awt.Dimension(52, 40));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jBtnSales.setIcon(IconFactory.getIcon("sale_new.png"));
        jBtnSales.setMaximumSize(new java.awt.Dimension(52, 40));
        jBtnSales.setMinimumSize(new java.awt.Dimension(52, 40));
        jBtnSales.setPreferredSize(new java.awt.Dimension(52, 40));
        jBtnSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnSalesActionPerformed(evt);
            }
        });

        m_jTitle.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(24f));
        m_jTitle.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout m_jPanelTitleLayout = new javax.swing.GroupLayout(m_jPanelTitle);
        m_jPanelTitle.setLayout(m_jPanelTitleLayout);
        m_jPanelTitleLayout.setHorizontalGroup(
            m_jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jPanelTitleLayout.createSequentialGroup()
                .addComponent(btnActionMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBtnSales, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(m_jTitle, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)
                .addGap(5, 5, 5))
        );
        m_jPanelTitleLayout.setVerticalGroup(
            m_jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jPanelTitleLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(m_jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBtnSales, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(m_jPanelTitleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(btnActionMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(m_jTitle, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );

        m_jPanelRight.add(m_jPanelTitle, java.awt.BorderLayout.NORTH);

        m_jPanelContainer.setFont(ChromisFonts.DEFAULTFONT.deriveFont(14f));
        m_jPanelContainer.setLayout(new java.awt.CardLayout());
        m_jPanelRight.add(m_jPanelContainer, java.awt.BorderLayout.CENTER);

        add(m_jPanelRight, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnActionMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionMenuActionPerformed
        actionMenu.show(btnActionMenu, btnActionMenu.getWidth() / 2, btnActionMenu.getHeight() / 2);
    }//GEN-LAST:event_btnActionMenuActionPerformed

    private void jBtnSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnSalesActionPerformed
        showTask("uk.chromis.pos.sales.JPanelTicketSales");

    }//GEN-LAST:event_jBtnSalesActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.out.println("Logout");
        m_appview.exitToLogin();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActionMenu;
    private javax.swing.JButton jBtnSales;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel m_jPanelContainer;
    public static javax.swing.JScrollPane m_jPanelLeft;
    private javax.swing.JPanel m_jPanelRight;
    private javax.swing.JPanel m_jPanelTitle;
    private javax.swing.JLabel m_jTitle;
    // End of variables declaration//GEN-END:variables

}
