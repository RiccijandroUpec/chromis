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
package uk.chromis.beans;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.custom.CustomColour;
import uk.chromis.custom.CustomJLabel;
import uk.chromis.custom.CustomJPasswordField;
import uk.chromis.custom.ExtendedJButton;
import uk.chromis.globals.IconFactory;
import uk.chromis.osk.KeyBoard;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppUser;
import uk.chromis.pos.forms.ChromisFonts;

/**
 *
 *
 */
public class JPasswordDialog extends JDialog {

    private String m_sPassword = null;

    //Set the fonts to be used
    private final Font lblFont = ChromisFonts.DEFAULTFONTBOLD;
    private final Font txtFont = ChromisFonts.DEFAULTFONTBOLD;
    private final Font btnFont = ChromisFonts.DEFAULTFONTBOLD;

    private final CustomJPasswordField m_jpassword = new CustomJPasswordField(new Dimension(300, 25), txtFont);
    private final CustomJPasswordField oldPassword = new CustomJPasswordField(new Dimension(300, 25), txtFont);
    private final CustomJPasswordField validatePassword = new CustomJPasswordField(new Dimension(300, 25), txtFont);

    //Main panels to be used by miglayout
    private final JPanel mainPanel = new JPanel(new MigLayout("insets 10 10 10 10 ", "", ""));
    private final JPanel passwordPanel = new JPanel(new MigLayout("insets 0 0 0 0 ", "[120][220]", "[][][]"));

    private final JPanel btnPanel = new JPanel();
    private ExtendedJButton btnCancel;
    private ExtendedJButton btnOK;
    private AppUser user;
    private static int CHOICE = -1;

    private JPanel keyBoard;

    public JPasswordDialog() {
        super(new JFrame());
        keyBoard = KeyBoard.getKeyboard(KeyBoard.Layout.QWERTY);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                m_sPassword = null;
                CHOICE = -1;
                dispose();
            }
        });
    }

    protected void buildPasswordRequest(String userName) {
        setRequestButtonPanel(new Dimension(105, 35));

        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.username"), lblFont));
        passwordPanel.add(new CustomJLabel(userName, lblFont), "wrap");
        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.password"), lblFont));
        passwordPanel.add(m_jpassword, "wrap");
        passwordPanel.add(btnPanel, "span 2,  align right, wrap");
        mainPanel.add(passwordPanel, "align center, wrap");

        setResizable(false);
        setModal(true);

        mainPanel.setBorder(BorderFactory.createLineBorder(CustomColour.getBorderColour(), 2));
        setTitle("Ingreso de Usuario");
        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(btnOK);

        setAlwaysOnTop(true);
        pack();

    }

    protected void buildPasswordChange(AppUser user) {

        this.user = user;
        setChangeButtonPanel(new Dimension(105, 35));

        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.username"), lblFont));
        passwordPanel.add(new CustomJLabel(user.getName(), lblFont), "wrap");
        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.passwordold"), lblFont));
        passwordPanel.add(oldPassword, "wrap");
        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.passwordnew"), lblFont));
        passwordPanel.add(m_jpassword, "wrap");
        passwordPanel.add(new CustomJLabel(AppLocal.getIntString("label.passwordrepeat"), lblFont));
        passwordPanel.add(validatePassword, "wrap");
        passwordPanel.add(btnPanel, "span 2,  align right, wrap");

        mainPanel.add(passwordPanel, "align center, wrap");

        setResizable(false);
        setModal(true);

        mainPanel.setBorder(BorderFactory.createLineBorder(CustomColour.getBorderColour(), 2));
        setTitle("Cambiar Contraseña");
        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(btnOK);

        setAlwaysOnTop(true);
        pack();
    }

    public String getPassword() {
        return m_sPassword;
    }

    private void setRequestButtonPanel(Dimension dimension) {
        btnOK = new ExtendedJButton(AppLocal.getIntString("button.ok"), JAlertPane.OK);
        btnOK.setPreferredSize(dimension);
        btnOK.setFont(btnFont);
        btnOK.setFocusable(false);
        btnOK.addActionListener((ActionEvent e) -> {
            m_sPassword = String.valueOf(m_jpassword.getPassword());
            CHOICE = 0;
            dispose();
        });
        btnPanel.add(btnOK);

        btnCancel = new ExtendedJButton(AppLocal.getIntString("button.cancel"), JAlertPane.CANCEL);
        btnCancel.setPreferredSize(dimension);
        btnCancel.setFont(btnFont);
        btnCancel.setFocusable(false);
        btnCancel.addActionListener((ActionEvent e) -> {
            m_sPassword = null;
            CHOICE = -1;
            dispose();
        });
        btnPanel.add(btnCancel);

        JButton kbButton = new JButton();
        kbButton.setBorderPainted(false);
        kbButton.setOpaque(false);
        kbButton.setPreferredSize(new Dimension(75, 35));
        kbButton.setIcon(IconFactory.getResizedIcon("keyboard.png", new Dimension(75, 35)));
        kbButton.addActionListener((ActionEvent e) -> {
            kbButton.setEnabled(false);
            mainPanel.add(keyBoard);
            int x = (this.getX() + (this.getWidth() / 2)) - 400;
            int y = this.getY() + this.getHeight() + 10;
            this.setLocation(x, this.getY());
            this.pack();
        });
        btnPanel.add(kbButton);
    }

    private void setChangeButtonPanel(Dimension dimension) {
        btnOK = new ExtendedJButton(AppLocal.getIntString("button.ok"), JAlertPane.OK);
        btnOK.setPreferredSize(dimension);
        btnOK.setFont(btnFont);
        btnOK.setFocusable(false);
        CHOICE = 0;
        btnOK.addActionListener((ActionEvent e) -> {
            m_sPassword = String.valueOf(m_jpassword.getPassword());
            if (user.authenticate(String.valueOf(oldPassword.getPassword()))) {
                if (m_sPassword.equals(String.valueOf(oldPassword.getPassword()))) {
                    m_sPassword = null;
                    JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.samepassword"), 16,
                            new Dimension(100, 50), JAlertPane.OK_OPTION);
                    CHOICE = -1;
                } else if (!m_sPassword.equals(String.valueOf(validatePassword.getPassword()))) {
                    m_sPassword = null;
                    JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.changepassworddistinct"), 16,
                            new Dimension(100, 50), JAlertPane.OK_OPTION);
                    CHOICE = -1;
                }
            } else {
                m_sPassword = null;
                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.badPassword"), 16,
                        new Dimension(100, 50), JAlertPane.OK_OPTION);
                CHOICE = -1;
            }
            //Password complexity validation to go here

            dispose();
        });
        btnPanel.add(btnOK);

        btnCancel = new ExtendedJButton(AppLocal.getIntString("button.cancel"), JAlertPane.CANCEL);
        btnCancel.setPreferredSize(dimension);
        btnCancel.setFont(btnFont);
        btnCancel.setFocusable(false);
        btnCancel.addActionListener((ActionEvent e) -> {
            m_sPassword = null;
            CHOICE = -1;
            dispose();
        });
        btnPanel.add(btnCancel);

        JButton kbButton = new JButton();
        kbButton.setBorderPainted(false);
        kbButton.setOpaque(false);
        kbButton.setPreferredSize(new Dimension(75, 35));
        kbButton.setIcon(IconFactory.getResizedIcon("keyboard.png", new Dimension(75, 35)));
        kbButton.addActionListener((ActionEvent e) -> {
            kbButton.setEnabled(false);
            mainPanel.add(keyBoard);
            int x = (this.getX() + (this.getWidth() / 2)) - 400;
            int y = this.getY() + this.getHeight() + 10;
            this.setLocation(x, this.getY());
            this.pack();
        });

        btnPanel.add(kbButton);
    }

    protected int getChoice() {
        return CHOICE;
    }

}
