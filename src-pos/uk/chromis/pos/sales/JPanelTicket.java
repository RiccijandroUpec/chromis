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
package uk.chromis.pos.sales;

import uk.chromis.pos.datalogic.DataLogicReceipts;
import uk.chromis.pos.datalogic.DataLogicLoyalty;
import uk.chromis.pos.datalogic.DataLogicCustomers;
import uk.chromis.pos.datalogic.DataLogicSales;
import uk.chromis.pos.datalogic.DataLogicSystem;
import bsh.EvalError;
import bsh.Interpreter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import javax.swing.*;
import uk.chromis.basic.BasicException;
import uk.chromis.data.gui.*;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.pos.customers.*;
import uk.chromis.pos.forms.*;
import uk.chromis.pos.inventory.TaxCategoryInfo;
import uk.chromis.pos.panels.JProductFinder;
import uk.chromis.pos.payment.*;
import uk.chromis.pos.printer.*;
import uk.chromis.pos.sales.restaurant.RestaurantDBUtils;
import uk.chromis.pos.scale.ScaleException;
import uk.chromis.pos.scripting.*;
import uk.chromis.pos.ticket.*;
import javax.swing.event.ListSelectionEvent;
import uk.chromis.commons.dbmanager.DbUtils;
import uk.chromis.globals.SystemProperty;
import uk.chromis.format.Formats;
import uk.chromis.globals.CompanyInfo;
import uk.chromis.pos.barcodes.Barcode;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.commons.dialogs.Receipt;
import uk.chromis.commons.utils.KeyedData;
import uk.chromis.commons.utils.TerminalInfo;
import uk.chromis.pos.forms.JPrincipalApp;

import uk.chromis.pos.giftcards.GiftCardLogic;
import uk.chromis.pos.inventory.AlternativeBarcode;
import uk.chromis.pos.loyalty.*;
import uk.chromis.pos.payment.PaymentInfo;
import uk.chromis.pos.printer.DeviceDisplayAdvance;
import uk.chromis.pos.printer.IncludeFile;
import uk.chromis.pos.util.AutoLogoff;
import uk.chromis.globals.IconFactory;
import uk.chromis.pos.auditing.Audit;

public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {

    private final static int NUMBERZERO = 0;
    private final static int NUMBERVALID = 1;
    private final static int NUMBERINVALID = 2;
    private final static int NUMBER_INPUTZERO = 0;
    private final static int NUMBER_INPUTZERODEC = 1;
    private final static int NUMBER_INPUTINT = 2;
    private final static int NUMBER_INPUTDEC = 3;
    private final static int NUMBER_PORZERO = 4;
    private final static int NUMBER_PORZERODEC = 5;
    private final static int NUMBER_PORINT = 6;
    private final static int NUMBER_PORDEC = 7;

    //Start of code refactoring
    private final Boolean isRestaurant = TerminalInfo.getPosType().equalsIgnoreCase("Restaurant");
    protected JTicketLines ticketLines, ticketLines2;

    //refactoring to do
    private StringBuilder barCodeStart;
    private String barCode;
    protected String siteGuid;
    private String ticketPrintType;

    //DataLogic classes
    protected DataLogicReceipts dlReceipts;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;

    protected TicketInfo m_oTicket;
    protected JPanelButtons m_jbtnconfig;
    protected AppView m_App;

    protected Object m_oTicketExt;
    protected TicketsEditor m_panelticket;
    private int m_iNumberStatus;
    private int m_iNumberStatusInput;
    private int m_iNumberStatusQty;
    private TicketParser m_TTP;
    private StringBuffer m_sBarcode;
    private JTicketsBag m_ticketsbag;
    private SentenceList senttax;
    private ListKeyed taxcollection;
    private SentenceList senttaxcategories;
    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    private TaxesLogic taxeslogic;
    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;
    private Action logout;
    private Integer delay = 0;
    private JPaymentSelect paymentdialog;

    private RestaurantDBUtils restDB;
    private KitchenDisplay kitchenDisplay;
    private Boolean receiptRequired = false;
    private TicketInfo m_ticket;
    private TicketInfo m_ticketCopy;
    private AppConfig m_config;
    private Boolean fromNumberPad = true;

    protected Component southcomponent;
    private LoyaltyCard loyaltyCard;

    private JPrincipalApp principalApp;
    private AlternativeBarcode alternativeBarcode;
    private Barcode bar;
    private ProductInfoExt defaultProduct = new ProductInfoExt();
    private CustomerDeliveryInfo deliveryInfo;
    private final HashMap<String, Object> parameters = new HashMap();

// Set up hash for age restriction items    
    private final HashMap<String, Integer> ageRestrictedProducts = new HashMap<>();
// Set up hashmaps used for keyboard shortcut mapping    
    private final HashMap<String, String> mappedKeys = new HashMap();
    private final HashMap<String, Object> instanceMap = new HashMap();

    private final GiftCardLogic giftCard = new GiftCardLogic();
    private final DateFormat format = new SimpleDateFormat(SystemProperty.DATETIME);

    public JPanelTicket() {
        initComponents();
        m_jButtons.setLocation(50, 0);
    }

    @Override
    public void init(AppView app) throws BeanFactoryException {
        m_App = app;

// Create DataLogic instances currently required
        dlSystem = (DataLogicSystem) m_App.getBean("uk.chromis.pos.datalogic.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("uk.chromis.pos.datalogic.DataLogicSales");
        dlCustomers = (DataLogicCustomers) m_App.getBean("uk.chromis.pos.datalogic.DataLogicCustomers");
        dlReceipts = (DataLogicReceipts) app.getBean("uk.chromis.pos.datalogic.DataLogicReceipts");

// Get list of any age restricted products into hashmap reducing database reads later
        try {
            List<KeyedData> restricted = dlSales.getRestrictedProducts();
            for (KeyedData k : restricted) {
                ageRestrictedProducts.put(k.getId(), k.getInt());
            }
        } catch (BasicException ex) {

        }

// Set up configuration
//      set '0' as required
        m_jNumberKey.dotIs00(SystemProperty.PRICEWITH00);
// set the siteguid for this site
        siteGuid = dlSystem.getSiteGUID();

// set up alternativeBarcode              
        alternativeBarcode = new AlternativeBarcode(dlSales);

        restDB = new RestaurantDBUtils();
        m_jbtnScale.setVisible(m_App.getDeviceScale().existsScale());
        m_ticketsbag = getJTicketsBag();

        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");
        if (SystemProperty.TAXINCLUDED) {
            ticketLines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.LineIncTaxes"));
        } else {
            ticketLines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.LineExclTaxes"));
        }
        ticketPanel.add(ticketLines, java.awt.BorderLayout.CENTER);

        ticketLinelListener();

        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);

        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);

        southcomponent = getSouthComponent();
        catcontainer.add(southcomponent, BorderLayout.CENTER);
        southcomponent.setPreferredSize(new Dimension(0, 320));

        senttax = dlSales.getTaxList(siteGuid);

        senttaxcategories = dlSales.getTaxCategoriesList(siteGuid);

        taxcategoriesmodel = new ComboBoxValModel();

        jEditAttributes.setEnabled(false);

        stateToZero();

        m_oTicket = null;
        m_oTicketExt = null;

        /*
        Code to drive full screen display
         */
        ticketLines2 = new JTicketLines(dlSystem.getResourceAsXML("Display.TicketLines"));
        customDisplay(TerminalInfo.hasCustomerDisplay());

// Change the screen layout if required        
        SaleScreens.createLayout(this);

        btnLogout.addActionListener((ActionEvent e) -> {
            jbtnLogout();
        });

        btnCustomer.addActionListener((ActionEvent e) -> {
            btnCustomerAction();
        });

        btnReprint.addActionListener((ActionEvent e) -> {
            btnReprintAction();
        });

        initElectronicInvoiceToggle();
    }

    private void initElectronicInvoiceToggle() {
        final javax.swing.JToggleButton btnToggleInvoice = new javax.swing.JToggleButton();
        btnToggleInvoice.setFocusPainted(false);
        btnToggleInvoice.setFocusable(false);
        btnToggleInvoice.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
        btnToggleInvoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnToggleInvoice.setOpaque(true);
        btnToggleInvoice.setContentAreaFilled(true);
        btnToggleInvoice.setBorderPainted(false);
        
        btnToggleInvoice.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean selected = btnToggleInvoice.isSelected();
                uk.chromis.pos.invoice.integration.ChromisPOSInvoiceIntegration.setElectronicInvoiceActive(selected);
                updateToggleStyle(btnToggleInvoice, selected);
            }
        });
        
        boolean active = uk.chromis.pos.invoice.integration.ChromisPOSInvoiceIntegration.isElectronicInvoiceActive;
        btnToggleInvoice.setSelected(active);
        updateToggleStyle(btnToggleInvoice, active);
        
        m_jButtonsExt.add(javax.swing.Box.createHorizontalStrut(10));
        m_jButtonsExt.add(btnToggleInvoice);
    }
    
    private void updateToggleStyle(javax.swing.JToggleButton btn, boolean active) {
        if (active) {
            btn.setText("Facturación: ACTIVA");
            btn.setBackground(new java.awt.Color(46, 204, 113));
            btn.setForeground(java.awt.Color.WHITE);
        } else {
            btn.setText("Facturación: INACTIVA");
            btn.setBackground(new java.awt.Color(231, 76, 60));
            btn.setForeground(java.awt.Color.WHITE);
        }
    }

    private void addGiftCardSale() {
        System.out.println("GiftCard");
        if (SystemProperty.GIFTCARDSENABLED) {
            Object[] result = JAlertPane.inputBox(new Dimension(450, 200), AppLocal.getIntString("message.giftcardNumber"), 16,
                    new Dimension(100, 35), JAlertPane.OK_CANCEL_OPTION, SystemProperty.GIFTCARDSTART);
            if ((Integer) result[0] == 4) {
                return;
            }
            if (!((String) result[1]).startsWith(SystemProperty.GIFTCARDSTART)) {
                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.invalidCardNumber"), 16,
                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                return;
            }
            TicketLineInfo newline = null;
            try {
                newline = JGiftCardEdit.showMessage(this, m_App, (String) result[1], dlSales, giftCard.isCardActivated((String) result[1]));
            } catch (BasicException ex) {
                Logger.getLogger(JPanelTicket.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            if (newline != null) {
                addTicketLine(newline);
                newline.setProperty("cardnotes", "Giftcard Number  : " + (String) result[1]);
                newline.setProperty("cardbalance", Formats.CURRENCY.formatValue(giftCard.getCardBalance((String) result[1]) + newline.getPrice()));
                newline.setProperty("cardnumber", (String) result[1]);
                m_oTicket.getLine(ticketLines.getSelectedIndex()).setProductAttSetInstDesc("Giftcard Number - " + (String) result[1]);
                refreshTicket();
            }
            stateToZero();
        }
    }

    public void addLoyaltyLine(ProductInfoExt product, Boolean redeem) {
        addTicketLine(product, 1.0, 0.00);
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setProductAttSetInstDesc("Loyalty purchase - " + product.getBurnValue() + " points.");
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setProperty("redeemed_points", Integer.toString(product.getBurnValue()));
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setProperty("cardnotes", "Loyalty purchase - " + product.getBurnValue() + " points.");
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setPrice(0.00);
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setSellingPrice(0.00);
        refreshTicket();
    }

    //Add Tickeline from price & plus key
    private void addTicketDefaultLine(ProductInfoExt oProduct, double dMul) {
        if (SystemProperty.DEFAULTPRODUCT) {
            if (SystemProperty.TAXINCLUDED) {
                addTicketLine(oProduct, 1.0, oProduct.getPriceSellinc());
            } else {
                addTicketLine(oProduct, 1.0, oProduct.getPriceSell());
            }
        } else {
            JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.defaultnotallowed"), 16,
                    new Dimension(125, 50), JAlertPane.OK_OPTION);
            stateToZero();
        }
    }

    /*
     * Addticket lines from various actions
     */
    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {
        TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID());
        switch (oProduct.getID().toLowerCase()) {
            case "servicecharge":
                if (m_oTicket.getNoSC().equals("0")) {
                    m_oTicket.setNoSC("1");
                    addTicketLine(new TicketLineInfo(oProduct, dMul, oProduct.getUnitPrice(dMul), tax, (java.util.Properties) (oProduct.getProperties().clone())));
                } else {
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.multipleservicecharge"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                    stateToZero();
                }
                return;
            case "deliverycharge":
                if (m_oTicket.getNoDelivery().equals("0")) {
                    // Object[] result = JAlertPane.inputDeliveryBox(new Dimension(450, 200), 14, new Dimension(100, 35), JAlertPane.OK_CANCEL_OPTION, "");
                    Object[] result = JAlertPane.inputDeliveryBox();

                    if ((Integer) result[0] == 0) {
                        deliveryInfo = (CustomerDeliveryInfo) result[1];
                        m_oTicket.setDeliveryInfo(deliveryInfo);
                        m_oTicket.setNoDelivery("1");
                    } else {
                        return;
                    }
                } else {
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.multipledeliverycharge"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                    stateToZero();
                    return;
                }
            case "xxx999_999xxx_x9x9x9":
                if (SystemProperty.LOTTOPAYOUT) {
                    oProduct.setPriceSell(-dPrice);
                    addTicketLine(new TicketLineInfo(oProduct, 1, -dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
                    stateToZero();
                    return;
                }
                break;
        }

        if (oProduct.isVprice()) {
            oProduct.setPriceSell(excludeTaxes(defaultProduct.getTaxCategoryID(), getInputValue()));
            addTicketLine(new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone())));
        } else {
            addTicketLine(new TicketLineInfo(oProduct, dMul, oProduct.getUnitPrice(dMul), tax, (java.util.Properties) (oProduct.getProperties().clone())));
        }
    }

    //All other addticketlines end up here
    protected void addTicketLine(TicketLineInfo oLine) {
        if (!m_oTicket.isRefund()) {
            if (SystemProperty.SCONOFF && m_oTicket.getNoSC().equals("0")) {
                m_oTicket.setNoSC("1");
                if (!"restaurant".equals(TerminalInfo.getPosType()) & !SystemProperty.SCRESTAURANT) {
                    addServiceChargeLine();
                } else if ("restaurant".equals(TerminalInfo.getPosType())) {
                    addServiceChargeLine();
                }
            }
        }

        Integer requiredAge = ageRestrictedProducts.get(oLine.getProductID());
        if (requiredAge != null) {
            if (requiredAge > m_oTicket.getAgeChecked()) {
                if (checkAgeAlert(requiredAge)) {
                    m_oTicket.setAgeChecked((requiredAge >= m_oTicket.getAgeChecked()) ? requiredAge : m_oTicket.getAgeChecked());
                } else {
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.age.declined"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                    stateToZero();
                    return;
                }
            }
        }

        if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
            if (oLine.isProductCom()) {
                int i = ticketLines.getSelectedIndex();

                if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }
                while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }
                if (i >= 0) {
                    m_oTicket.insertLine(i, oLine);
                    ticketLines.insertTicketLine(i, oLine);
                } else {
                    errorBeep();
                }
            } else {
                m_oTicket.addLine(oLine);
                ticketLines.addTicketLine(oLine);

                try {
                    int i = ticketLines.getSelectedIndex();
                    TicketLineInfo line = m_oTicket.getLine(i);
                    if (line.compulsoryAttributes()) {
                        if (SystemProperty.SHOWGUI) {
                            JProductAttEditNew attedit = JProductAttEditNew.getAttributesEditor(this, m_App.getSession());
                            attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                            attedit.setVisible(true);
                            if (attedit.isOK()) {
                                // The user pressed OK
                                line.setProductAttSetInstId(attedit.getAttributeSetInst());
                                line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                                paintTicketLine(i, line);
                            }
                        } else {
                            JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                            attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                            attedit.setVisible(true);
                            if (attedit.isOK()) {
                                line.setProductAttSetInstId(attedit.getAttributeSetInst());
                                line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                                paintTicketLine(i, line);
                            }
                        }
                    }
                } catch (BasicException ex) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                    msg.show(this);
                }
            }

            if (m_oTicket.isTaxExempt()) {
                applyTaxExemption(oLine);
            }

            if (m_oTicket.hasCustomer() && m_oTicket.getCustomer().getCustomerDiscount() > 0.00) {
                applyDiscount(oLine);
            }

// check if the ticket has a discount applied
            checkSellingPrices(m_oTicket);
            checkServiceCharge(m_oTicket);

            if (SystemProperty.CONSOLIDATED) {
                HashMap<String, Double> products = new HashMap();
                for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                    TicketLineInfo current_line = m_oTicket.getLine(i);
                    if (!current_line.isProductVprice() & !current_line.isSystemObject()) {
                        products.putIfAbsent(current_line.getProductID(), 0.00);
                        products.put(current_line.getProductID(), products.get(current_line.getProductID()) + current_line.getMultiply());
                    }
                }

                TicketLineInfo current_line = m_oTicket.getLine(ticketLines.getSelectedIndex());
                int selectedIndex = ticketLines.getSelectedIndex();

                for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                    TicketLineInfo current_ticketline = m_oTicket.getLine(i);
                    if (current_ticketline.getProductID().equals(current_line.getProductID()) & i != selectedIndex) {
                        if (products.get(current_ticketline.getProductID()) != null) {
                            current_ticketline.setMultiply(products.get(current_ticketline.getProductID()));
                            removeTicketLine(selectedIndex, false);
                        }
                    }
                }
                refreshTicket();
            }

            executeEventAndRefresh("ticket.pretotals");

            CustomerDisplay.updateDisplay(oLine);
            printPartialTotals();
            stateToZero();

            // read resource ticket.change and execute
            // not used unless activated in ticket.buttons
            executeEvent(m_oTicket, m_oTicketExt, "ticket.change");
        }
        refreshTicket();
    }

    private void ticketLinelListener() {
        ticketLines.addListSelectionListener((ListSelectionEvent e) -> {
            if (ticketLines.getSelectedIndex() >= 0) {
                if (m_oTicket.getLine(ticketLines.getSelectedIndex()).getProductID() != null) {
                    TicketLineInfo current_line = m_oTicket.getLine(ticketLines.getSelectedIndex());
                    jEditAttributes.setEnabled(current_line.hasAttributes());
                    if (current_line.getProductInfoExt() != null) {
                        m_jEditLine.setEnabled(!current_line.getProductInfoExt().hasTierPringing());
                    } else {
                        m_jEditLine.setEnabled(true);
                    }
                    if (current_line.isServiceCharge()) {
                        m_jEditLine.setEnabled(false);
                    }
                }
            }
        });
    }

    private void customDisplay(Boolean showDisplay) {
        if (showDisplay) {
            if ((m_App.getDeviceTicket().getDeviceDisplay() != null)
                    && (m_App.getDeviceTicket().getDeviceDisplay() instanceof DeviceDisplayAdvance)) {
                DeviceDisplayAdvance advDisplay = (DeviceDisplayAdvance) m_App.getDeviceTicket().getDeviceDisplay();
                if (advDisplay.hasFeature(DeviceDisplayAdvance.TICKETLINES)) {
                    ticketLines2 = new JTicketLines(dlSystem.getResourceAsXML("Display.TicketLines"));
                    advDisplay.setTicketLines(ticketLines2);
                }
                ticketLines.addListSelectionListener((ListSelectionEvent e) -> {
                    if (advDisplay.hasFeature(DeviceDisplayAdvance.TICKETLINES)) {
                        int i = ticketLines.getSelectedIndex();
                        if (m_oTicket == null || m_oTicket.getLinesCount() == 0) {
                            CustomerDisplay.updateDisplay("display.Message");
                        }
                    }
                });
            }
        }
    }

    private void saveCurrentTicket() {
        if ((String) m_oTicketExt != null) {
            try {
                dlReceipts.updateSharedTicket((String) m_oTicketExt, m_oTicket, m_oTicket.getPickupId(), "P" + m_oTicket.getId().substring(24), deliveryInfo);
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
    }

    protected abstract JTicketsBag getJTicketsBag();

    protected abstract Component getSouthComponent();

    protected abstract void resetSouthComponent();

    protected abstract void reLoadCatalog();

    @Override
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt) {
        btnCustomer.setEnabled(true);
        loyaltyCard = null;
        m_jLblLoyaltyCard.setText(null);
        // check if a inactivity timer has been created, and if it is not running start up again
        // this is required for autologoff mode in restaurant and it is set to return to the table view.        
        switch (TerminalInfo.getPosType()) {
            case "restaurant":
                if (SystemProperty.ENABLEAUTOLOGOFF && SystemProperty.AUTOLOGOFFINACTIVITYTIMER) {
                    if (!AutoLogoff.getInstance().isTimerRunning()) {
                        AutoLogoff.getInstance().activateTimer();
                    }
                }
        }

        m_jNumberKey.setEnabled(true);
        jEditAttributes.setVisible(true);
        m_jList.setVisible(true);

        m_oTicket = oTicket;
        m_oTicketExt = oTicketExt;

        if (m_oTicket != null) {
            // Asign preliminary properties to the receipt
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date()); // Set the edition date.

            if (m_oTicket.getLoyaltyCard() != null) {
                loyaltyCard = m_oTicket.getLoyaltyCard();
                m_jLblLoyaltyCard.setText("Loyalty card : " + loyaltyCard.getCardNumber());
            }
            // Set some of the table details here if in restaurant mode
            if (TerminalInfo.getPosType().equals("restaurant") && !oTicket.getOldTicket()) {
                // Check if there is a customer name in the database for this table
                if (restDB.getCustomerNameInTable(oTicketExt.toString()).equals("")) {
                    if (m_oTicket.getCustomer() != null) {
                        restDB.setCustomerNameInTable(m_oTicket.getCustomer().toString(), oTicketExt.toString());
                    }
                }
                //Check if the waiters name is in the table, this will be the person who opened the ticket                        
                if (restDB.getWaiterNameInTable(oTicketExt.toString()).equals("")) {
                    restDB.setWaiterNameInTable(m_App.getAppUserView().getUser().getName(), oTicketExt.toString());
                }
                restDB.setTicketIdInTable(m_oTicket.getId(), oTicketExt.toString());
                restDB.setTableLock(m_oTicket.getId(), m_App.getAppUserView().getUser().getName());
            }
        } else {
            //reset the loyalty to null 
            loyaltyCard = null;
        }

        // lets check if this is a moved ticket        
        if ((m_oTicket != null) && (SystemProperty.SHOWCUSTOMERDETAILS || SystemProperty.SHOWWAITERDETAILS)) {
            // check if the old table and the new table are the same                      
            if (restDB.getTableMovedFlag(m_oTicket.getId())) {
                restDB.moveCustomer(oTicketExt.toString(), m_oTicket.getId());
            }
        }

        // if there is a customer assign update the debt details
        if (m_oTicket != null && m_oTicket.getCustomer() != null) {
            try {
                m_oTicket.getCustomer().setCurrentDebt(dlSales.getCustomerDebt(m_oTicket.getCustomer().getId()));
            } catch (BasicException ex) {
                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // read resources ticket.show and execute
        executeEvent(m_oTicket, m_oTicketExt, "ticket.show");
        j_btnKitchenPrt.setVisible(AppUser.hasPermission("sales.PrintKitchen"));
        refreshTicket();
    }

    @Override
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }

    private void refreshTicket() {
        if (m_oTicket != null) {
            m_jDelete.setVisible(!m_oTicket.isRefund());
        }
        CardLayout cl = (CardLayout) (getLayout());

        if (m_oTicket == null) {
            btnSplit.setEnabled(false);
            m_jTicketId.setText(null);
            m_jLblLoyaltyCard.setText(null);
            ticketLines.clearTicketLines();

            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);

            stateToZero();
            repaint();

            cl.show(this, "null");

            if ((m_oTicket != null) && (m_oTicket.getLinesCount() == 0)) {
                resetSouthComponent();
            }

        } else {
            btnSplit.setEnabled((AppUser.hasPermission("sales.Total") && (m_oTicket.getArticlesCount()) > 1));
            if (m_oTicket.isRefund()) {
                //Make disable Search and Edit Buttons
                m_jNumberKey.justEquals();
                jEditAttributes.setVisible(false);
                m_jEditLine.setVisible(false);
                m_jList.setVisible(false);
            }

            setTicketName(isRestaurant ? m_oTicket.getTableName(m_oTicketExt) : m_oTicket.getName(m_oTicketExt));

            int line = ticketLines.getSelectedIndex();
            ticketLines.clearTicketLines();
            ticketLines2.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                ticketLines.addTicketLine(m_oTicket.getLine(i));
                ticketLines2.addTicketLine(m_oTicket.getLine(i));
            }

            printPartialTotals();
            stateToZero();

            cl.show(this, "ticket");
            if (m_oTicket.getLinesCount() == 0) {
                resetSouthComponent();
            }

            m_jKeyFactory.setText(null);
            java.awt.EventQueue.invokeLater(() -> {
                m_jKeyFactory.requestFocus();
            });
        }
    }

    @Override
    public void setTicketName(String tName) {
        m_jTicketId.setText(tName);
    }

    @Override
    public void activate() throws BasicException {
// if the autologoff and inactivity is configured the setup the timer with action
        principalApp = JRootApp.getPricipalApp();

        // Authorization for buttons        
        m_jEditLine.setVisible(AppUser.hasPermission("button.lineeditor"));
        m_jDelete.setEnabled(AppUser.hasPermission("sales.EditLines"));
        m_jNumberKey.setMinusEnabled(AppUser.hasPermission("sales.EditLines"));

        btnSplit.setEnabled(AppUser.hasPermission("sales.Total"));
        m_jNumberKey.setEqualsEnabled(AppUser.hasPermission("sales.Total"));

        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());

        //     btnSplit.setVisible(false);
        m_ticketsbag.activate();

        //   m_jButtons.setPreferredSize(new java.awt.Dimension(180, 56));
        try {
            defaultProduct = dlSales.getDefaultProductInfo("DefaultProduct", siteGuid);
        } catch (BasicException ex) {
            defaultProduct = null;
        }

        logout = new logout();
        if (SystemProperty.ENABLEAUTOLOGOFF && SystemProperty.AUTOLOGOFFINACTIVITYTIMER) {
            try {
                delay = SystemProperty.AUTOLOGOFFPERIOD;
                if (delay != 0) {
                    AutoLogoff.getInstance().setTimer(delay * 1000, logout);
                }
            } catch (NumberFormatException e) {
                delay = 0;
            }
        }

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this);
        paymentdialogrefund.init(m_App);

        java.util.List<TaxInfo> taxlist = senttax.list();
        taxcollection = new ListKeyed<>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
        taxcategoriescollection = new ListKeyed<>(taxcategorieslist);

        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        taxeslogic = new TaxesLogic(taxlist);

        instanceMap.put("m_ticketsbag", m_ticketsbag);
        instanceMap.put("principalApp", principalApp);

        btnReprint.setEnabled(AppUser.hasPermission("button.reprintlastticket"));

    }

    @Override
    public boolean deactivate() {
        AutoLogoff.getInstance().deactivateTimer();
        if (m_oTicket != null) {
            if (isRestaurant) {
                restDB.clearTableLockByTicket(m_oTicket.getId());
            }
        }
        return m_ticketsbag.deactivate();
    }

    @Override
    public Object getBean() {
        return this;
    }

    @Override
    public JComponent getComponent() {
        return this;

    }

    private void printPartialTotals() {
        if (m_oTicket.getLinesCount() == 0) {
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);
        } else {
            m_jTotalEuros.setText(m_oTicket.printTotal());
            m_jTaxesEuros.setText(m_oTicket.printTaxAmount());
            m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
        }
        repaint();
    }

    private void paintTicketLine(int index, TicketLineInfo oLine) {
        if (executeEventAndRefresh("ticket.setline", new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {
            m_oTicket.setLine(index, oLine);
            ticketLines.setTicketLine(index, oLine);
            ticketLines.setSelectedIndex(index);

            CustomerDisplay.updateDisplay(oLine);
            printPartialTotals();
            stateToZero();

            executeEventAndRefresh("ticket.pretotals");
            executeEventAndRefresh("ticket.change");
        }
    }

    private ProductInfoExt getInputProduct() {
        if (SystemProperty.TAXINCLUDED) {
            defaultProduct.setPriceSellInc(getInputValue());
        } else {
            defaultProduct.setPriceSell(getInputValue());
        }
        return defaultProduct;
    }

    private void removeTicketLine(int i, Boolean audit) {
        if ((("OK".equals(m_oTicket.getLine(i).getProperty("sendstatus")) && SystemProperty.ALLOWSENTITEMREFUND))
                || (!"OK".equals(m_oTicket.getLine(i).getProperty("sendstatus")) && AppUser.hasPermission("sales.EditLines"))) {

            if ("OK".equals(m_oTicket.getLine(i).getProperty("sendstatus"))) {
                m_oTicket.getLine(i).setProperty("sendstatus", "Cancel");
                printTicket("Printer.TicketKitchen", m_oTicket, m_oTicketExt, true);
                JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.orderCancellation"), 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
            }

            if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
                if (audit) {
                    Audit.itemRemoved(m_oTicket, i, "Single Item Removed");
                }
                if (m_oTicket.getLine(i).isProductCom()) {
                    m_oTicket.removeLine(i);
                    ticketLines.removeTicketLine(i);
                } else {
                    m_oTicket.removeLine(i);
                    ticketLines.removeTicketLine(i);
                    while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                        m_oTicket.removeLine(i);
                        ticketLines.removeTicketLine(i);
                    }
                }

                CustomerDisplay.clearDisplay();
                printPartialTotals();
                stateToZero();
                executeEventAndRefresh("ticket.pretotals");
                executeEventAndRefresh("ticket.change");

            }
        } else {
            JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.cannotdeletesentline"), 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
        }
    }

    private double includeTaxes(String tcid, double dValue) {
//        if (m_jaddtax.isSelected()) {
//            TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
//            double dTaxRate = tax == null ? 0.0 : tax.getRate();
//            return dValue / (1.0 + dTaxRate);
//        } else {
//            return dValue;
//        }
        return 0.00;
    }

    private double excludeTaxes(String tcid, double dValue) {
        TaxInfo tax = taxeslogic.getTaxInfo(tcid, m_oTicket.getCustomer());
        double dTaxRate = tax == null ? 0.0 : tax.getRate();
        return dValue / (1.0 + dTaxRate);
    }

    private double getInputValue() {
        try {
            return Double.parseDouble(m_jPrice.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double getPorValue() {
        try {
            return Double.parseDouble(m_jQty.getText().substring(1));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return 1.0;
        }
    }

    //clear the keyboard input buffers
    private void stateToZero() {
        m_jQty.setText("");
        m_jPrice.setText("");
        m_sBarcode = new StringBuffer();
        m_iNumberStatus = NUMBER_INPUTZERO;
        m_iNumberStatusInput = NUMBERZERO;
        m_iNumberStatusQty = NUMBERZERO;
        repaint();
    }

    private void incProductByCode(String barCode) {
// Modify to allow number x with scanned products.    
        alternativeBarcode = new AlternativeBarcode(barCode, siteGuid, dlSales);
//        int count = 1;
//        if (barCode.contains("*")) {
//            count = (barCode.indexOf("*") == 0) ? 1 : parseInt(barCode.substring(0, barCode.indexOf("*")));
//            barCode = barCode.substring(barCode.indexOf("*") + 1, barCode.length());
//        }

        try {

            //Get the product details and set the description - this is for a future release
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(alternativeBarcode.getM_sbaseBarcode());

            if (oProduct == null) {
                errorBeep();
                JAlertPane.messageBox(JAlertPane.INFORMATION, "\n" + barCode + " - " + AppLocal.getIntString("message.noproduct"), 16,
                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                stateToZero();
            } else {
                oProduct.setName(alternativeBarcode.getDescription());
                if (bar.getBarcodeType() == Barcode.ISSN) {
                    //get the price code 
                    if (oProduct.getProperty("price" + barCode.charAt(10)) != null) {
                        if (SystemProperty.TAXINCLUDED) {
                            oProduct.setPriceSellInc(Double.valueOf(oProduct.getProperty("price" + barCode.charAt(10))));
                        } else {
                            oProduct.setSellingPrice(Double.valueOf(oProduct.getProperty("price" + barCode.charAt(10))));
                        }
                    }
                    if (oProduct.getProperty("day" + barCode.charAt(11)) != null) {
                        oProduct.setName(oProduct.getName() + oProduct.getProperty("day" + barCode.charAt(11)));
                    }
                }
                incProduct(alternativeBarcode.getSalesCount(), oProduct);
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
        }
    }

    private void incProductByCodePrice(String barCode, double dPriceSell) {
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(barCode);
            if (oProduct == null) {
                errorBeep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);
                stateToZero();
            } else //if (m_jaddtax.isSelected()) {
            //    TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
            //    addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
            // } else {
            {
                addTicketLine(oProduct, 1.0, dPriceSell);
            }
        } catch (BasicException eData) {
            stateToZero();
            new MessageInf(eData).show(this);
        }
    }

    //All button presses end up here
    private void incProduct(ProductInfoExt prod) {
        if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    incProduct(value, prod);
                    m_oTicket.getLine(ticketLines.getSelectedIndex()).setProperty("sendstatus", "scale");
                    refreshTicket();
                }
            } catch (ScaleException e) {
                errorBeep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                stateToZero();
            }
        } else if (prod.getID().equalsIgnoreCase("giftcard-sale") || prod.getID().equalsIgnoreCase("giftcard-topup")) {
            addGiftCardSale();
        } else if (prod.getID().equalsIgnoreCase("deliverycharge") && m_oTicket.getNoDelivery() == "1") {
            incProduct(1.0, prod);
        } else if (!prod.isVprice()) {
            incProduct(1.0, prod);
        } else {
            errorBeep();
            JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.novprice"), 16,
                    new Dimension(125, 50), JAlertPane.OK_OPTION);
        }
    }

    private void incProduct(double dPor, ProductInfoExt prod) {
        if (!prod.isScale() && prod.isVprice()) {
            addTicketLine(prod, getPorValue(), getInputValue());
        } else {
            addTicketLine(prod, dPor, prod.getPriceSell());
        }
    }

    protected void buttonTransition(ProductInfoExt prod) {
        if (m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusQty == NUMBERZERO) {
            // add single product to ticket lines
            incProduct(prod);
        } else if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERZERO) {
            incProduct(getInputValue(), prod);
        } else if (prod.isVprice()) {
            addTicketLine(prod, getPorValue(), getInputValue());
        } else {
            errorBeep();
        }
    }

    private Boolean checkAgeAlert(int age) {
        return JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.age.required", age), 16,
                new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 5;
    }

    private void addGiftCardLine(ProductInfoExt product, Double amount, String cardNumber) {
        addTicketLine(product, 1.0, amount);
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setProductAttSetInstDesc("Giftcard Number - " + cardNumber);
        m_oTicket.getLine(ticketLines.getSelectedIndex()).setProperty("cardnotes", "Giftcard Number - " + cardNumber);
        refreshTicket();
    }

    @Override
    public void addServiceChargeLine() {
        try {
            ProductInfoExt oProduct = dlSales.getProductInfo("ServiceCharge");
            addTicketLine(oProduct, 1.0, 0.0);
            refreshTicket();
        } catch (BasicException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processTicketLineReceipt() {
        if (m_oTicket.getLinesCount() > 0) {
            if (closeTicket(m_oTicket, m_oTicketExt)) {
                if (m_oTicket.isRefund()) {
                    try {
                        JRefundLines.updateRefunds();
                    } catch (BasicException ex) {
                    }
                }
                m_ticketsbag.deleteTicket();
                if ((!("restaurant".equals(TerminalInfo.getPosType()))
                        && SystemProperty.ENABLEAUTOLOGOFF
                        && SystemProperty.AUTOLOGOFFAFTERSALE)) {
                    ((JRootApp) m_App).closeAppView();
                } else if (("restaurant".equals(TerminalInfo.getPosType()))
                        && SystemProperty.ENABLEAUTOLOGOFF
                        && SystemProperty.AUTOLOGOFFAFTERSALE
                        && !SystemProperty.AUTOLOGOFFTOTABLES) {
                    ((JRootApp) m_App).closeAppView();
                }
            } else {
                refreshTicket();
            }
        } else {
            errorBeep();
        }
    }

    private void getScaleWeightFromUserDialogInput() {
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else if (m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    newline.setMultiply(value);
                    newline.setPrice(Math.abs(newline.getPrice()));
                    paintTicketLine(i, newline);
                    //  m_oTicket.getLine(ticketLines.getSelectedIndex()).setProperty("sendstatus", "scale");
                }
            } catch (ScaleException e) {
                errorBeep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                stateToZero();
            }
        } else {
            errorBeep();
        }
    }

    private void decreaseSelectedTicketLineByMultiplier() {
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else {
            double dPor = getPorValue();
            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
            if (m_oTicket.getLine(i).isServiceCharge()) {
                if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.deleteservicecharge"), 16,
                        new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {
                    return;
                }
                m_oTicket.setNoSC("0");
            }

            if (m_oTicket.getLine(i).getMultiply() > dPor) {
                if (m_oTicket.isNormal()) {
                    newline.setMultiply(m_oTicket.getLine(i).getMultiply() - dPor);
                    newline.setPrice(-Math.abs(newline.getPrice()));
                    paintTicketLine(i, newline);
                    checkSellingPrices(m_oTicket);
                    checkServiceCharge(m_oTicket);
                    refreshTicket();
                }
            }
        }
    }

    private void increaseSelectedTicketLineByMultiplier() {
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else {
            double dPor = getPorValue();
            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
            if (m_oTicket.getLine(i).isServiceCharge() && SystemProperty.SCONOFF) {
                return;
            }
            if (m_oTicket.isRefund()) {
                newline.setMultiply(-dPor);
                newline.setPrice(Math.abs(newline.getPrice()));
                paintTicketLine(i, newline);
            } else {
                newline.setMultiply(m_oTicket.getLine(i).getMultiply() + dPor);
                newline.setPrice(Math.abs(newline.getPrice()));
                paintTicketLine(i, newline);
            }
            checkSellingPrices(m_oTicket);
            checkServiceCharge(m_oTicket);
            refreshTicket();
        }
    }

    protected void removeSCLine() {
        int l = 0;
        for (TicketLineInfo tl : m_oTicket.getLines()) {
            if (tl.isServiceCharge()) {
                if (l < 0) {
                    errorBeep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(l));
                    if (m_oTicket.isRefund()) {
                        newline.setMultiply(newline.getMultiply() + 1.0);
                        if (newline.getMultiply() >= 0) {
                            removeTicketLine(l, true);
                        } else {
                            paintTicketLine(l, newline);
                        }
                    } else {
                        // substract one unit to the selected line
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        if (newline.getMultiply() <= 0.0) {
                            removeTicketLine(l, true);
                        } else {
                            paintTicketLine(l, newline);
                        }
                    }
                    checkSellingPrices(m_oTicket);
                    checkServiceCharge(m_oTicket);
                    refreshTicket();
                }
            }
        }
    }

    private void decreaseSelectedTicketLine() {
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else {
            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
            if (m_oTicket.getLine(i).isServiceCharge()) {
                if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.deleteservicecharge"), 16,
                        new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {
                    return;
                }
                removeTicketLine(i, true);
                m_oTicket.setNoSC("0");
                return;
            }
            if (m_oTicket.getLine(i).isDeliveryCharge()) {
                if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.deletedeliverycharge"), 16,
                        new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {

                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.multipleservicecharge"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                    return;
                }
                removeTicketLine(i, true);
                m_oTicket.setNoDelivery("0");
                deliveryInfo = null;
                return;
            }

            if (m_oTicket.isRefund()) {
                newline.setMultiply(newline.getMultiply() + 1.0);
                if (newline.getMultiply() >= 0) {
                    removeTicketLine(i, true);
                } else {
                    paintTicketLine(i, newline);
                }
            } else {
                // substract one unit to the selected line
                if (("OK".equals(m_oTicket.getLine(i).getProperty("sendstatus")) && !SystemProperty.ALLOWSENTITEMREFUND)) {
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.cannotdeletesentline"), 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
                    return;
                }

                if (newline.getMultiply() == 1) {
                    if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.finallineitem"), 16,
                            new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 5) {
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        if (newline.getMultiply() <= 0.0) {
                            removeTicketLine(i, true);
                        } else {
                            paintTicketLine(i, newline);
                        }
                    }
                } else {
                    newline.setMultiply(newline.getMultiply() - 1.0);
                    Audit.itemRemoved(m_oTicket, i, 1.0, "Single Item Removed");
                    if (newline.getMultiply() <= 0.0) {
                        removeTicketLine(i, true);
                    } else {
                        paintTicketLine(i, newline);
                    }
                }
            }
            checkSellingPrices(m_oTicket, true, 1.0, newline.getProductName(), newline.getProductID());
            checkServiceCharge(m_oTicket);
            refreshTicket();
        }
    }

    private void checkServiceCharge(TicketInfo m_oTicket) {
        Double serviceCalculation = 0.00;
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            if (!m_oTicket.getLine(i).isServiceCharge()) {
                serviceCalculation = serviceCalculation + (m_oTicket.getLine(i).getSellingPrice() * m_oTicket.getLine(i).getMultiply());
            }
        }
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            if (m_oTicket.getLine(i).isServiceCharge()) {
                if (m_oTicket.isRefund()) {
                    m_oTicket.getLine(i).setPrice(-serviceCalculation * (SystemProperty.SCRATE / 100));
                } else {
                    m_oTicket.getLine(i).setPrice((serviceCalculation * (SystemProperty.SCRATE / 100)) * (1 + m_oTicket.getLine(i).getTaxInfo().getRate()));
                }
            }
        }
    }

    private void checkSellingPrices(TicketInfo m_oTicket) {
        checkSellingPrices(m_oTicket, false, 0.0, null, null);
    }

    private void checkSellingPrices(TicketInfo m_oTicket, String name, String id) {
        checkSellingPrices(m_oTicket, false, 0.0, name, id);
    }

    private void checkSellingPrices(TicketInfo m_oTicket, Boolean itemRemoved, Double qty, String name, String id) {
        HashMap<String, Double> products = new HashMap();
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            TicketLineInfo current_line = m_oTicket.getLine(i);
            if (!current_line.isProductVprice() & !current_line.isSystemObject()) {
                products.putIfAbsent(current_line.getProductID(), 0.00);
                if (!current_line.isDiscounted()) {
                    products.put(current_line.getProductID(), products.get(current_line.getProductID()) + current_line.getMultiply());
                }
            }
        }
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            TicketLineInfo current_line = m_oTicket.getLine(i);
            if (!current_line.isProductVprice() & !current_line.isSystemObject() & current_line.getProperty("edited") == null
                    & !m_oTicket.getTicketType().toString().equalsIgnoreCase("REFUND")) {
                if (m_oTicket.isTaxExempt()) {
                    m_oTicket.getLine(i).setPrice(current_line.getProductInfoExt().getUnitPriceExempt(products.get(current_line.getProductID())));
                } else {
                    m_oTicket.getLine(i).setPrice(current_line.getProductInfoExt().getUnitPrice(products.get(current_line.getProductID())));
                }
                current_line.setProperty("tierpriced", String.valueOf(current_line.getProductInfoExt().isTierPriced(products.get(current_line.getProductID()))));
            } else if (m_oTicket.getTicketType().toString().equalsIgnoreCase("REFUND") && current_line.getPrice() > 0) {
                current_line.setPrice(current_line.getPrice() * -1);
            }
        }
        if (itemRemoved) {
            parameters.clear();
            parameters.put("qty", qty);
            parameters.put("name", (name != null) ? name : m_oTicket.getLine(ticketLines.getSelectedIndex()).getProductName());
            parameters.put("id", id);
            CustomerDisplay.updateDisplay("Display.ItemRemoved", parameters);
        } else if (ticketLines.getSelectedIndex() > -1) {
            CustomerDisplay.updateDisplay(m_oTicket.getLine(ticketLines.getSelectedIndex()));
        }
    }

    private void increaseSelectedTicketLine() {
        int i = ticketLines.getSelectedIndex();
        if ("OK".equals(m_oTicket.getLine(i).getProperty("sendstatus"))) {
            JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.cannotincreasesentline"), 16, new Dimension(125, 50), JAlertPane.OK_OPTION);
            return;
        }

        if (i != -1 && (m_oTicket.getLine(i).isDeliveryCharge() || m_oTicket.getLine(i).isServiceCharge())) {
            JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString(
                    (m_oTicket.getLine(i).isDeliveryCharge()) ? "message.multipledeliverycharge" : "message.multipleservicecharge"), 16,
                    new Dimension(125, 50), JAlertPane.OK_OPTION);
            return;
        }

        if (i < 0) {
            errorBeep();
        } else {
            if (m_oTicket.getLine(i).getProperty("cardnotes") != null) {
                if (m_oTicket.getLine(i).getProperty("cardnotes").startsWith("Loyalty purchase")) {
                    JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.rescanloyalty"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                    stateToZero();
                    return;
                }
            }
            if (!m_oTicket.getLine(i).isGiftCardSale() & !m_oTicket.getLine(i).isGiftCardTopUp()) {
                TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                if (m_oTicket.isRefund()) {
                    newline.setMultiply(newline.getMultiply() - 1.0);
                    paintTicketLine(i, newline);
                } else {
                    newline.setMultiply(newline.getMultiply() + 1.0);
                    paintTicketLine(i, newline);
                }
                checkSellingPrices(m_oTicket);
                checkServiceCharge(m_oTicket);
                refreshTicket();
            } else {
                stateToZero();
            }
        }
    }

    private void readWeightFromScales() {
        if (m_App.getDeviceScale().existsScale() && AppUser.hasPermission("sales.EditLines")) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, value, product.getPriceSell());
                }
            } catch (ScaleException e) {
                errorBeep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                stateToZero();
            }
        } else {
            errorBeep();
        }
    }

    private void errorBeep() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void pricePeriod(String strValue) {
        m_jPrice.setText(SystemProperty.PRICEWITH00 ? setTempjPrice(strValue) : strValue);
    }

    private void pricePeriodWithNumber(String strValue) {
        m_jPrice.setText(SystemProperty.PRICEWITH00 ? setTempjPrice(m_jPrice.getText() + strValue) : m_jPrice.getText() + strValue);
    }

    private boolean closeTicket(TicketInfo ticket, Object ticketext) {
        AutoLogoff.getInstance().deactivateTimer();
        boolean resultok = false;

        if (AppUser.hasPermission("sales.Total")) {
// Check if we have a warranty to print               
            warrantyCheck(ticket);
            try {
                // reset the payment info
                taxeslogic.calculateTaxes(ticket);

                //Only reset if is sale
                if (ticket.getTicketTotal() >= 0.0) {
                    ticket.resetPayments();
                }
                //read resource ticket.total and execute
                if (executeEvent(ticket, ticketext, "ticket.total") == null) {
                    //show total due on customer display
                    parameters.clear();
                    parameters.put("ticket", ticket);
                    CustomerDisplay.updateDisplay("Display.TicketTotal", parameters);

                    // Select the Payments information
                    paymentdialog = ticket.isRefund()
                            ? paymentdialogrefund
                            : paymentdialogreceipt;

                    paymentdialog.setTransactionID(ticket.getTransactionID());

                    if (ticket.isRefund()) {
                        printTicket("Display.RefundTotal", ticket, ticketext, false);
                    }

                    if (paymentdialog.showDialog(ticket.getTicketTotal(), ticket.getCustomer(), ticket.getLoyaltyCard())) {
                        // assign the payments selected and calculate taxes.         
                        ticket.setPayments(paymentdialog.getSelectedPayments());

                        for (PaymentInfo p : paymentdialog.getSelectedPayments()) {
                            if (p.getName().equals("giftcard")) {
                                giftCard.updateRedeemedValue(p.getCardName(), ticket, p.getPaid());
                            }
                        }

                        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo());
                        ticket.setActiveCash(m_App.getActiveCashIndex());
                        ticket.setDate(new Date());

                        ticket.setCardFees(paymentdialog.getSurcharge());

                        //read resource ticket.save and execute
                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {
                            try {
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation(), loyaltyCard, ticketext, deliveryInfo);
                                
                                // Facturación Electrónica Ecuador - Procesar asíncronamente
                                uk.chromis.pos.invoice.integration.ChromisPOSInvoiceIntegration.processTicket(ticket);

                                // Process Gift Card and gift Vouchers here
                                for (TicketLineInfo line : m_oTicket.getLines()) {
                                    if ((line.getProperty("vCode") != "") && (line.getProperty("vCode") != null)) {
                                        try {
                                            dlSales.sellVoucher(new Object[]{line.getProperty("vCode"), Integer.toString(ticket.getTicketId()), line.getPrice()});
                                        } catch (BasicException ex) {
                                        }
                                    }

                                    switch (line.getProductID()) {
                                        case "giftcard-sale":
                                            giftCard.activateCard(line.getProperty("cardnumber"), line.getPrice(), ticket);
                                            break;
                                        case "giftcard-topup":
                                            giftCard.updateCard(line.getProperty("cardnumber"), line.getPrice(), ticket);
                                            break;
                                    }
                                }

                                // code added to allow last ticket reprint       
                                AppConfig.put("lastticket.number", Integer.toString(ticket.getTicketId()));
                                AppConfig.put("lastticket.type", Integer.toString(ticket.getTicketType().getId()));

                            } catch (BasicException eData) {
                                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                                msg.show(this);
                            }

                            //read resource ticket.close and execute
                            //New ticket receipt routine
                            //************************************************************************
                            boolean isCash = false;
                            String change = "";
                            Double tendered = 0.00;

                            for (PaymentInfo p : ticket.getPayments()) {
                                tendered = tendered + p.getTendered();
                                if ("cash".equals(p.getName())) {
                                    isCash = true;
                                    if (p.getTotal() < 0.00) {
                                        change = Formats.CURRENCY.formatValue(p.getTotal());
                                    } else {
                                        change = Formats.CURRENCY.formatValue(p.getChange());
                                    }
                                }

                            }

//                            if (!ticket.isRefund()) {
//                                printTicket("Display.Refundtest", ticket, ticketext, false);
//                            }
                            ticket.setProperty("change", change);

                            openDrawer(ticket);

                            parameters.clear();
                            parameters.put("ticket", ticket);
                            CustomerDisplay.updateDisplay("Display.TicketChange", parameters);

                            paymentdialog.setReceiptRequired(Receipt.required(Formats.CURRENCY.formatValue(ticket.getTicketTotal()),
                                    Formats.CURRENCY.formatValue(tendered), change, SystemProperty.RECEIPTPRINTOFF));
                            //End of new receipt routine
                            //****************************************************************************

                            //   printTicket("Printer.Change", ticket, ticketext);
                            //this is the old vesrion of ticket.close
                            //*****************************************************************************************************
                            //executeEvent(ticket, ticketext, "ticket.close", new ScriptArg("print", paymentdialog.printReceipt()));
                            //*****************************************************************************************************
                            if (loyaltyCard != null) {
                                loyaltyCard.processTicketPoints(loyaltyCard.getCardNumber(), ticket);
                            }

                            if (paymentdialog.isGiftReceiptRequired()) {
                                printTicket("Printer.GiftReceipt");
                            }

                            if (paymentdialog.printReceipt() || receiptRequired) {
                                printTicket("Printer.Ticket", ticket, ticketext, false);
                            }

                            //show default display message
                            CustomerDisplay.updateDisplay("display.Message");
                            resultok = true;

                            // if restaurant clear any customer name in table for this table once receipt is printed
                            if ("restaurant".equals(TerminalInfo.getPosType()) && !ticket.getOldTicket()) {
                                restDB.clearCustomerNameInTable(ticketext.toString());
                                restDB.clearWaiterNameInTable(ticketext.toString());
                                restDB.clearTicketIdInTable(ticketext.toString());
                                restDB.clearTableLockByName(ticketext.toString());
                            }
                        }
                    }
                }
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
                resultok = false;
            }

            // reset the payment info
            m_oTicket.resetTaxes();
            m_oTicket.resetPayments();
        }

        // cancelled the ticket.total script
        // or canceled the payment dialog
        // or canceled the ticket.close script
        AutoLogoff.getInstance().activateTimer();
        return resultok;
    }

    private boolean checkVoucherCurrentTicket(String voucher) {
        for (TicketLineInfo line : m_oTicket.getLines()) {
            if (line.getProperty("vCode") != null && line.getProperty("vCode").equals(voucher)) {
                return (true);
            }
        }
        return (false);
    }

    private boolean warrantyCheck(TicketInfo ticket) {
        receiptRequired = false;
        int lines = 0;
        while (lines < ticket.getLinesCount()) {
            if (!receiptRequired) {
                receiptRequired = ticket.getLine(lines).warrantyRequired();
                return (true);
            }
            lines++;
        }
        return false;
    }

    public String getPickupString(TicketInfo pTicket) {
        if (pTicket == null) {
            return ("0");
        }
        String tmpPickupId = Integer.toString(pTicket.getPickupId());
        if (SystemProperty.PICKUPSIZE >= tmpPickupId.length()) {
            while (tmpPickupId.length() < SystemProperty.PICKUPSIZE) {
                tmpPickupId = "0" + tmpPickupId;
            }
        }
        return (tmpPickupId);
    }

    private void printVoucher(String sresourcename) {
        String source = dlSystem.getResourceAsXML(sresourcename);
        String sresource;
        IncludeFile incFile = new IncludeFile(source, dlSystem);

        if (source == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
        } else {
            sresource = incFile.processInclude();
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                m_TTP.printTicket(script.eval(sresource).toString());
            } catch (ScriptException | TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
            }
        }

    }

    private void openDrawer(TicketInfo ticket) {
        String sresource = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><output><opendrawerNoLog/></output>";
        try {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
            m_TTP.printTicket(script.eval(sresource).toString(), ticket);
        } catch (ScriptException | TicketPrinterException ex) {
            Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printTicket(String sresourcename, TicketInfo ticket, Object ticketext, Boolean cancel) {
        String source = dlSystem.getResourceAsXML(sresourcename);
        String sresource;
        IncludeFile incFile = new IncludeFile(source, dlSystem);
        if (source.isEmpty()) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(JPanelTicket.this);
        } else {
            sresource = incFile.processInclude();
            if (SystemProperty.PICKUPENABLED) {
                if (ticket.getPickupId() == 0) {
                    try {
                        ticket.setPickupId(dlSales.getNextPickupIndex());
                    } catch (BasicException e) {
                        ticket.setPickupId(0);
                    }
                }
            } else {
                ticket.setPickupId(0);
            }
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                if (ticket.getLinesCount() > 0) {
                    script.put("salesticket", ticket.getLine(0).getProperty("salesticket"));
                }
                script.put("ticket", ticket);
                script.put("place", ticketext);
                script.put("warranty", receiptRequired);
                //  script.put("pickupid", getPickupString(ticket));
                script.put("ticketpanel", this);
                script.put("loyalty", loyaltyCard);
                script.put("company", new CompanyInfo());
                //Removed until logic has been redone
                script.put("giftcard", new GiftCardLogic());
                script.put("printers", setRemoteUnits(ticket, cancel));
                script.put("pickupcode", "P" + ticket.getId().substring(24));
                script.put("taxincluded", ticket.isTaxInclusive());
                script.put("pickupenabled", SystemProperty.PICKUPENABLED);
                script.put("cancellation", cancel);
                script.put("currentDate", format.format(new Date()));
                ReceiptTaxesInfo ltr = new ReceiptTaxesInfo();
                List<ReceiptTaxesInfo> lines = ltr.getReceiptTaxLines(dlSales.getLineTaxRates(ticket.getId()));
                script.put("nett", ((ticket.isTaxInclusive()) ? ltr.printReceiptSubTotal(lines, ticket.getTicketTotal()) : Formats.CURRENCY.formatValue(ticket.getSubTotalExcluding())));
                script.put("tickettaxdetails", lines);
                script.put("deliveryinfo", deliveryInfo);
                script.put("cardType", new String[]{"", LocalResource.getString("paymentdescription.cmagcard1"), LocalResource.getString("paymentdescription.cmagcard2"),
                    LocalResource.getString("paymentdescription.cmagcard3"), LocalResource.getString("paymentdescription.cmagcard4"), LocalResource.getString("transpayment.cmagcard5")});

                script.put("cardRefundType", new String[]{"", LocalResource.getString("paymentdescription.cmagcard1refund"), LocalResource.getString("paymentdescription.cmagcard2refund"),
                    LocalResource.getString("paymentdescription.cmagcard3refund"), LocalResource.getString("paymentdescription.cmagcard4refund"), LocalResource.getString("paymentdescription.cmagcard5refund")});

                refreshTicket();

                m_TTP.printTicket(script.eval(sresource).toString(), ticket);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            } catch (BasicException | TicketPrinterException ex) {
                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Set<String> setRemoteUnits(TicketInfo ticket, Boolean cancel) {
        Set<String> ptrs = new HashSet<>();
        if (cancel) {
            ticket.getLines().stream().filter(t -> (t.isProductKitchen() && t.getProperty("sendstatus").equalsIgnoreCase("cancel"))).forEachOrdered(t -> {
                ptrs.add(t.getRemotePrinter());
            });
        } else {
            ticket.getLines().stream().filter(t -> (t.isProductKitchen())).forEachOrdered(t -> {
                ptrs.add(t.getRemotePrinter());
            });
        }
        return ptrs;
    }

    public void kitchenOrderScreen() {
        kitchenOrderScreen(kitchenOrderId(), 1, false);
    }

    public void kitchenOrderScreen(String id) {
        kitchenOrderScreen(id, 1, true);
    }

    public void kitchenOrderScreen(Integer display, String ticketid) {
        kitchenOrderScreen(kitchenOrderId(), display, false);
    }

    public void kitchenOrderScreen(Integer display) {
        kitchenOrderScreen(kitchenOrderId(), display, false);
    }

    public void kitchenOrderScreen(Integer display, Boolean primary) {
        kitchenOrderScreen(kitchenOrderId(), display, primary);
    }

    public String kitchenOrderId() {
        if ((m_oTicket.getCustomer() != null)) {
            return m_oTicket.getCustomer().getName();
        } else if (m_oTicketExt != null) {
            return m_oTicketExt.toString();
        } else {
            if (SystemProperty.PICKUPENABLED) {
                if (m_oTicket.getPickupId() == 0) {
                    try {
                        m_oTicket.setPickupId(dlSales.getNextPickupIndex());
                    } catch (BasicException e) {
                        m_oTicket.setPickupId(0);
                    }
                }
            } else {
                m_oTicket.setPickupId(0);
            }
            return getPickupString(m_oTicket);
        }
    }

    private void kitchenOrderScreen(String id, Integer display, boolean primary) {
        Integer lastDisplay = null;   // Keeps track of the display the last product was sent to, to ensure pairing of products and components 
        // Create a UUID for this order for the kitchenorder table 
        String orderUUID = UUID.randomUUID().toString();
        for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
            if ("No".equals(m_oTicket.getLine(i).getProperty("sendstatus"))) {
                if (m_oTicket.getLine(i).isProductDisplay()) {
                    if (!primary) {
                        display = (m_oTicket.getLine(i).getDisplayId() == 0) ? 1 : m_oTicket.getLine(i).getDisplayId();
                    }
                    try {
                        // If this is a component item, use the display number for the parent item 
                        if (m_oTicket.getLine(i).isProductCom()) {
                            if (lastDisplay != null && !primary) {
                                display = lastDisplay;
                            }
                            if (m_oTicket.getLine(i).isProductDisplay()) {
                                dlSystem.addOrder(UUID.randomUUID().toString(), orderUUID, (int) m_oTicket.getLine(i).getMultiply(), "+ " + m_oTicket.getLine(i).getKitchenName(),
                                        m_oTicket.getLine(i).getProductAttSetInstDesc(), m_oTicket.getLine(i).getProperty("notes"), id, display, 1, i);
                            }
                        } else if (m_oTicket.getLine(i).isProductDisplay()) {
                            dlSystem.addOrder(UUID.randomUUID().toString(), orderUUID, (int) m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getKitchenName(),
                                    m_oTicket.getLine(i).getProductAttSetInstDesc(), m_oTicket.getLine(i).getProperty("notes"), id, display, 0, i);
                        }
                        lastDisplay = display;

                    } catch (BasicException ex) {
                        Logger.getLogger(JPanelTicket.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                    lastDisplay = display;
                }
            }
        }
    }

    public void doRefresh() {
        refreshTicket();
    }

    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {
        // resource here is guaranteed to be not null
        try {
            scr.setSelectedIndex(ticketLines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);
        } catch (ScriptException e) {
            //  MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, resource + ": " + AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
            return msg;
        }
    }

    public void evalScriptAndRefresh(String resource, ScriptArg... args) {
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);

            scr.setSelectedIndex(ticketLines.getSelectedIndex());
            evalScript(scr, resource, args);
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
        }
    }

    public void printTicket(String resource) {
        printTicket(resource, m_oTicket, m_oTicketExt, false);
    }

    private Object executeEventAndRefresh(String eventkey, ScriptArg... args) {
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(ticketLines.getSelectedIndex());
            Object result = evalScript(scr, resource, args);
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
            return result;
        }
    }

    private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg... args) {
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketext);
            return evalScript(scr, resource, args);
        }
    }

    protected String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    protected BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }

    private void setSelectedIndex(int i) {
        ticketLines.setSelectedIndex((i >= 0 && i < m_oTicket.getLinesCount())
                ? i
                : m_oTicket.getLinesCount() - 1);
    }

    private String setTempjPrice(String jPrice) {
        jPrice = jPrice.replace(".", "");
// remove all leading zeros from the string        
        long tempL = Long.parseLong(jPrice);
        jPrice = Long.toString(tempL);

        while (jPrice.length() < 3) {
            jPrice = "0" + jPrice;
        }
        return (jPrice.length() <= 2) ? jPrice : (new StringBuffer(jPrice).insert(jPrice.length() - 2, ".").toString());
    }

    private void validateISSNBarcode(String barcode) {

    }

    private void stateTransition(char cTrans) {
        if (cTrans == '\n') {
            if (m_sBarcode.length() == 0) {
                processTicketLineReceipt();
            } else {

                bar = new Barcode(m_sBarcode.toString());
                barCode = bar.getBarCode();

                //code for KT
                if (dlSystem.isFobPresent(barCode.toString())) {
                    stateToZero();
                    principalApp.showTask("uk.chromis.pos.panels.JPanelCloseMoney");
                    return;
                }

                switch (bar.getBarcodeType()) {
                    case Barcode.INVALID:
                        errorBeep();
                        break;
                    case Barcode.NORMAL:
                        System.out.println("Normal");
                        break;
                    case Barcode.CUSTOMERWITHLOYALTY:
                        System.out.println("CustomerWithLoyalty");
                        if (loyaltyCard == null) {
                            loyaltyCard = (SystemProperty.LOYALTYTYPE.equals("earnx"))
                                    ? new CollectTransactionLoyaltyPoints(barCode)
                                    : new CollectItemLoyaltyPoints(barCode);
                            m_oTicket.setLoyaltyCard(loyaltyCard);

                            if (loyaltyCard != null) {
                                m_jLblLoyaltyCard.setText("Loyalty card : " + barCode);
                                loyaltyCard.showCardStatus(this);
                            }
                        } else if (!loyaltyCard.getCardNumber().equals(barCode)) {
                            JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.cardinuse", loyaltyCard.getCardNumber()), 16,
                                    new Dimension(125, 50), JAlertPane.OK_OPTION);
                        } else {
                            if (SystemProperty.LOYALTYTYPE.equals("collectx")) {
                                loyaltyCard.showCardStatus(this);
                            }
                        }
                        stateToZero();
                        return;
                    case Barcode.CUSTOMER:
                        System.out.println("Customer");
                        if (DataLogicLoyalty.isCardRetired(barCode)) {
                            errorBeep();
                            JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.cardretired", barCode), 16,
                                    new Dimension(125, 50), JAlertPane.OK_OPTION);
                            stateToZero();
                            return;
                        }

                        CustomerInfoExt newCustomer = null;
                        try {
                            newCustomer = dlSales.findCustomerExt(barCode);
                            if (newCustomer == null) {
                                errorBeep();
                                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.nocustomer", barCode), 16,
                                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                            }
                        } catch (BasicException e) {

                        }
                        if (!m_oTicket.hasCustomer() && newCustomer != null) {
                            processCustomer(newCustomer);
                        } else if (newCustomer != null) {
//offer to remove the customer and add new customer                             
                            if (removeCustomer()) {
                                processCustomer(newCustomer);
                            }
                        }

                        stateToZero();
                        return;
                    case Barcode.LOYALTYCARD:
                        System.out.println("Loyalty");
                        if (SystemProperty.LOYALTYENABLED) {
                            if (DataLogicLoyalty.isCardRetired(barCode) || DataLogicLoyalty.cardReplaced(barCode)) {
                                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.loyaltyreplaced", barCode), 16,
                                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                                stateToZero();
                                return;
                            }

                            if (DataLogicLoyalty.isCardLocked(barCode)) {
                                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.loyaltydisabled", barCode), 16,
                                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                                stateToZero();
                                return;
                            }
                            if (loyaltyCard == null) {
                                loyaltyCard = (SystemProperty.LOYALTYTYPE.equals("earnx"))
                                        ? new CollectTransactionLoyaltyPoints(barCode)
                                        : new CollectItemLoyaltyPoints(barCode);

                                loyaltyCard = (loyaltyCard.isCardActive()) ? loyaltyCard : null;
                                m_oTicket.setLoyaltyCard(loyaltyCard);

                                if (loyaltyCard != null) {
                                    if (loyaltyCard.isCardActive()) {
                                        m_jLblLoyaltyCard.setText("Loyalty card : " + barCode);
                                        loyaltyCard.showCardStatus(this);
                                    }
                                }
                            } else if (!loyaltyCard.getCardNumber().equals(barCode)) {
                                JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.cardinuse", loyaltyCard.getCardNumber()), 16,
                                        new Dimension(125, 50), JAlertPane.OK_OPTION);
                            } else {
                                if (SystemProperty.LOYALTYTYPE.equals("collectx")) {
                                    loyaltyCard.showCardStatus(this);
                                }
                            }
                            stateToZero();
                            return;
                        }
                    case Barcode.GIFTCARD:
                        System.out.println("GiftCard");
                        if (SystemProperty.GIFTCARDSENABLED) {
                            TicketLineInfo newline = null;
                            try {
                                newline = JGiftCardEdit.showMessage(this, m_App, barCode, dlSales, giftCard.isCardActivated(barCode));
                            } catch (BasicException ex) {
                                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if (newline != null) {
                                addTicketLine(newline);
                                newline.setProperty("cardnotes", "Giftcard Number  : " + barCode);
                                newline.setProperty("cardbalance", Formats.CURRENCY.formatValue(giftCard.getCardBalance(barCode) + newline.getPrice()));
                                newline.setProperty("cardnumber", barCode);
                                m_oTicket.getLine(ticketLines.getSelectedIndex()).setProductAttSetInstDesc("Giftcard Number - " + barCode);
                                refreshTicket();
                            }
                            stateToZero();
                            return;
                        }
                    case Barcode.GIFTVOUCHER:
                        System.out.println("GiftVoucher");
                        break;
                    case Barcode.PICKUPBARCODE:
                        m_ticketsbag.getTicketByCode(barCode);
                        return;
                    case Barcode.VOUCHER:
                        System.out.println("voucher");
                        break;
                    case Barcode.ISSN:
                        System.out.println("Newspaper");
                        break;
                }

                if (bar.getBarcodeType() != Barcode.INVALID) {
                    // Check for gift voucher code and add if found code for kidsgrove Tropicals
                    if (barCode.length() > 4) {
                        barCodeStart = new StringBuilder(m_sBarcode.substring(0, 3));
                        switch (barCodeStart.toString()) {
                            case "05V":
                            case "10V":
                            case "20V":
                                try {
                                    if (dlSales.getVoucher(barCode)) {
                                        stateToZero();
                                        JOptionPane.showMessageDialog(null, "Voucher Code \"" + barCode + "\" already Sold. Please use another voucher",
                                                "Invalid Voucher",
                                                JOptionPane.WARNING_MESSAGE);
                                    } else if (checkVoucherCurrentTicket(barCodeStart.toString())) {
                                        stateToZero();
                                        JOptionPane.showMessageDialog(null, "Voucher Code \"" + barCode + "\" already on Ticket. Please use another voucher",
                                                "Invalid Voucher",
                                                JOptionPane.WARNING_MESSAGE);
                                    } else {
                                        ProductInfoExt oProduct = new ProductInfoExt();
                                        oProduct = dlSales.getProductInfoByCode(barCodeStart.toString());
                                        if (oProduct != null) {
                                            oProduct.setCode(barCodeStart.toString());
                                            oProduct.setName(oProduct.getName());
                                            oProduct.setProperty("vCode", barCode);
                                            oProduct.setProperty("linenotes", barCode);
                                            // oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                                            oProduct.setTaxCategoryID("GiftCards");
                                            addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                                        } else {
                                            errorBeep();
                                            JOptionPane.showMessageDialog(null,
                                                    "Vocher code " + barCodeStart + " - " + AppLocal.getIntString("message.noproduct"),
                                                    "Check", JOptionPane.WARNING_MESSAGE);
                                            stateToZero();
                                        }
                                    }
                                    return;
                                } catch (BasicException ex) {
                                    Logger.getLogger(JPanelTicket.class
                                            .getName()).log(Level.SEVERE, null, ex);
                                }
                                break;
                        }
                    }
                    // Test for a customer card
                    if (barCode.startsWith("2") && ((barCode.length() == 13) || (barCode.length() == 12))) {
// we now have a variable barcode being passed   
// get the variable type   
                        ProductInfoExt oProduct = null;
                        try {
                            oProduct = dlSales.getProductInfoByCode(barCode.toString());
                        } catch (BasicException ex) {
                            Logger.getLogger(JPanelTicket.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }
                        // no exact match for the product
                        if (oProduct != null) {
                            incProductByCode(barCode.toString());
                        } else {
                            String sVariableTypePrefix;
                            String prodCode;
                            String sVariableNum;
                            double dPriceSell = 0.0;
                            double weight = 1.0;
                            /*
                        if (sCode.length() == 13) {
                            sVariableTypePrefix = sCode.substring(0, 2);
                            //sVariableNum = sCode.substring(8, 12);
                            sVariableNum = sCode.substring(7, 12);
                            prodCode = sCode.replace(sCode.substring(7, sCode.length() - 1), "00000");
                            prodCode = prodCode.substring(0, sCode.length() - 1);
                        } else {
                            sVariableTypePrefix = sCode.substring(0, 2);;
                            //sVariableNum = sCode.substring(7, 11);
                            sVariableNum = sCode.substring(6, 11);
                            //prodCode = sCode.replace(sCode.substring(6, sCode.length() - 1), "00000");
                            prodCode = sCode.replace(sCode.substring(5, sCode.length() - 1), "000000");
                            prodCode = prodCode.substring(0, sCode.length() - 1);
                        }
                        if (sCode.length() == 13) {
                             */
                            int iBarCodeLen = barCode.length();
                            int iValueIndex;
                            sVariableTypePrefix = barCode.substring(0, 2);
                            iValueIndex = sVariableTypePrefix.equals("28") && (iBarCodeLen == 13) ? iBarCodeLen - 6 : iBarCodeLen - 5;
                            sVariableNum = barCode.substring(iValueIndex, iBarCodeLen - 1);
                            prodCode = barCode.substring(0, iValueIndex).concat("000000");
                            prodCode = prodCode.substring(0, iBarCodeLen - 1);
                            if (iBarCodeLen == 13) {

                                switch (sVariableTypePrefix) {
                                    case "20":
                                        dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                        break;
                                    case "21":
                                        dPriceSell = Double.parseDouble(sVariableNum) / 10;
                                        break;
                                    case "22":
                                        dPriceSell = Double.parseDouble(sVariableNum);
                                        break;
                                    case "23":
                                        weight = Double.parseDouble(sVariableNum) / 1000;
                                        break;
                                    case "24":
                                        weight = Double.parseDouble(sVariableNum) / 100;
                                        break;
                                    case "25":
                                        weight = Double.parseDouble(sVariableNum) / 10;
                                        break;
                                    case "28":
                                        /*
                                    sVariableNum = sCode.substring(7, 12);
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                    break;
                            }
                        } else if (sCode.length() == 12) {
                            switch (sCode.substring(0, 1)) {
                                case "2":
                                    sVariableNum = sCode.substring(7, 11);
                                         */
                                        dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                        break;
                                }
                            } else if ((iBarCodeLen == 12) && barCode.substring(0, 1).equals("2")) {
                                dPriceSell = Double.parseDouble(sVariableNum) / 100;
                            }
// we now know the product code and the price or weight of it.
// lets check for the product in the database. 
                            try {
                                oProduct = dlSales.getProductInfoByCode(prodCode);
                                if (oProduct == null) {
                                    errorBeep();
                                    JOptionPane.showMessageDialog(null,
                                            prodCode + " - " + AppLocal.getIntString("message.noproduct"),
                                            "Check", JOptionPane.WARNING_MESSAGE);
                                    stateToZero();
                                    //} else if (barCode.length() == 13) {
                                } else if (iBarCodeLen == 13) {
                                    switch (sVariableTypePrefix) {
                                        case "23":
                                        case "24":
                                        case "25":
                                            oProduct.setProperty("product.weight", Double.toString(weight));
                                            dPriceSell = oProduct.getPriceSell();
                                            break;
                                    }
                                } else // Handle UPC code, get the product base price if zero then it is a price passed otherwise it is a weight                                
                                if (oProduct.getPriceSell() != 0.0) {
                                    weight = Double.parseDouble(sVariableNum) / 100;
                                    oProduct.setProperty("product.weight", Double.toString(weight));
                                    dPriceSell = oProduct.getPriceSell();
                                } else {
                                    dPriceSell = Double.parseDouble(sVariableNum) / 100;
                                }
                                //  if (m_jaddtax.isSelected()) {
                                //      addTicketLine(oProduct, weight, dPriceSell);
                                // } else {
                                TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(), m_oTicket.getCustomer());
                                addTicketLine(oProduct, weight, dPriceSell / (1.0 + tax.getRate()));
                                // }
                            } catch (BasicException eData) {
                                stateToZero();
                                new MessageInf(eData).show(this);
                            }
                        }
                    } else if (m_jbtnconfig.getEvent("script.CustomCodeProcessor") != null) {
                        Object oTicketLine = executeEvent(m_oTicket, m_oTicketExt, "script.CustomCodeProcessor", new ScriptArg("sCode", barCode));
                        if (oTicketLine instanceof TicketLineInfo) {
                            addTicketLine((TicketLineInfo) oTicketLine);
                        } else {
                            incProductByCode(barCode.toString());
                        }
                    } else {
                        incProductByCode(barCode.toString());
                    }
                } else {
                    errorBeep();
                }
            }
            /**
             * ******************************************************************
             * end of barcode handling routine
             * ******************************************************************
             */
        } else {
            m_sBarcode.append(cTrans);
            //CE pressed so clear fields 
            if (cTrans == '\u007f') {
                stateToZero();
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                m_jPrice.setText("0");
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_INPUTZERO)) {
                pricePeriod(Character.toString(cTrans));
                m_iNumberStatus = NUMBER_INPUTINT;
                if (m_iNumberStatusInput != NUMBERINVALID) {
                    m_iNumberStatusInput = NUMBERVALID;
                }
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_INPUTINT)) {
                //Read the number key pressed and populates the amount field
                pricePeriodWithNumber(Character.toString(cTrans));
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO && !SystemProperty.PRICEWITH00) {
                m_jPrice.setText("0.");
                m_iNumberStatus = NUMBER_INPUTZERODEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTZERO) {
                m_jPrice.setText("");
                m_iNumberStatus = NUMBER_INPUTZERO;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT && !SystemProperty.PRICEWITH00) {
                m_jPrice.setText(m_jPrice.getText() + ".");
                m_iNumberStatus = NUMBER_INPUTDEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_INPUTINT) {
                pricePeriodWithNumber("00");
                m_iNumberStatus = NUMBER_INPUTINT;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) {
                pricePeriodWithNumber(Character.toString(cTrans));
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_INPUTZERODEC || m_iNumberStatus == NUMBER_INPUTDEC)) {
                m_jPrice.setText(m_jPrice.getText() + cTrans);
                m_iNumberStatus = NUMBER_INPUTDEC;
                m_iNumberStatusInput = NUMBERVALID;
            } else if (cTrans == '*' && (m_iNumberStatus == NUMBER_INPUTINT || m_iNumberStatus == NUMBER_INPUTDEC)) {
                // populates the multiplier field with x
                m_jQty.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;
            } else if (cTrans == '*' && (m_iNumberStatus == NUMBER_INPUTZERO || m_iNumberStatus == NUMBER_INPUTZERODEC)) {
                m_jPrice.setText("0");
                m_jQty.setText("x");
                m_iNumberStatus = NUMBER_PORZERO;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_PORZERO)) {
                m_jQty.setText("x0");
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_PORZERO)) {
                m_jQty.setText("x" + Character.toString(cTrans));
                m_iNumberStatus = NUMBER_PORINT;
                m_iNumberStatusQty = NUMBERVALID;
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_PORINT)) {
                m_jQty.setText(m_jQty.getText() + cTrans);
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO && !SystemProperty.PRICEWITH00) {
                m_jQty.setText("x0.");
                m_iNumberStatus = NUMBER_PORZERODEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORZERO) {
                m_jQty.setText("x");
                m_iNumberStatus = NUMBERVALID;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT && !SystemProperty.PRICEWITH00) {
                m_jQty.setText(m_jQty.getText() + ".");
                m_iNumberStatus = NUMBER_PORDEC;
            } else if (cTrans == '.' && m_iNumberStatus == NUMBER_PORINT) {
                m_jQty.setText(m_jQty.getText() + "00");
                m_iNumberStatus = NUMBERVALID;
            } else if ((cTrans == '0') && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) {
                m_jQty.setText(m_jQty.getText() + cTrans);
            } else if (Character.isDigit(cTrans) && (m_iNumberStatus == NUMBER_PORZERODEC || m_iNumberStatus == NUMBER_PORDEC)) {
                m_jQty.setText(m_jQty.getText() + cTrans);
                m_iNumberStatus = NUMBER_PORDEC;
                m_iNumberStatusQty = NUMBERVALID;
            } else if (cTrans == '\u00a7' && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERZERO) {
                readWeightFromScales();
            } else if (cTrans == '\u00a7' && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusQty == NUMBERZERO) {
                getScaleWeightFromUserDialogInput();
            } else if (cTrans == '+' && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusQty == NUMBERZERO) {
                //+ key pressed to increase line count
                increaseSelectedTicketLine();
                //minus on keypad    
            } else if (cTrans == '-' && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusQty == NUMBERZERO && AppUser.hasPermission("sales.EditLines")) {
                decreaseSelectedTicketLine();
            } else if (cTrans == '+' && m_iNumberStatusInput == NUMBERZERO && m_iNumberStatusQty == NUMBERVALID) {
                increaseSelectedTicketLineByMultiplier();
            } else if (cTrans == '-' && (m_iNumberStatusInput == NUMBERZERO || m_iNumberStatusQty == NUMBERVALID) && AppUser.hasPermission("sales.EditLines")) {
                decreaseSelectedTicketLineByMultiplier();

            } else if (cTrans == '+' && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERZERO && AppUser.hasPermission("sales.EditLines")) {
                // + key to add the physical amount enter as default product to the ticketlines
                ProductInfoExt product = getInputProduct();
                addTicketDefaultLine(product, 1.0);
                if (!SystemProperty.HIDEDEFAULTPRODUCT) {
                    m_jEditLine.doClick();
                }
            } else if (cTrans == '-' && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERZERO && AppUser.hasPermission("sales.EditLines") && fromNumberPad) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, -product.getPriceSell());
            } else if (cTrans == '+' && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERVALID && AppUser.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), product.getPriceSell());
            } else if (cTrans == '-' && m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERVALID && AppUser.hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), -product.getPriceSell());
            } else if (cTrans == ' ' || cTrans == '=') {
                processTicketLineReceipt();
            } else if (!Character.isDigit(cTrans)) {
                m_iNumberStatusInput = NUMBERINVALID;
            }
        }
    }

    private void btnCustomerAction() {
        AutoLogoff.getInstance().deactivateTimer();

        if (m_oTicket.hasCustomer()) {
            removeCustomer();
            return;
        }

// Prompt for a customer       
        JCustomerFinder finder = new JCustomerFinder(dlCustomers);
        finder.search(m_oTicket.getCustomer());
        finder.setLocationRelativeTo(this);
        finder.setLocation(finder.getX(), finder.getY() - 200);
        finder.setVisible(true);

//No customer selected -> return       
        if (finder.getSelectedCustomer() == null) {
            return;
        }

// Get the selected customer details        
        CustomerInfoExt newCustomer;

        try {
            newCustomer = dlSales.loadCustomerExt(finder.getSelectedCustomer().getId());
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);
            return;
        }
        processCustomer(newCustomer);
    }

    private Boolean removeCustomer() {
        int result = JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("dialog.removecustomer"), 16,
                new Dimension(125, 50), JAlertPane.YES_NO_OPTION);
        if (result == 5) {
            m_oTicket.setCustomer(null);

// Clean up the ticket
            for (TicketLineInfo line : m_oTicket.getLines()) {

// remove all discounts applied to the ticket                    
                if (line.isDiscounted()) {
                    line.setProperty("product.name", line.getProductOrigName());
                    line.setDiscounted("no");
                    line.removeProperty("edited");
                    line.setSoldPrice(line.getSellingPrice());
                    line.setPrice(line.getPrice());
                    line.setPriceExcl(line.getProductPriceExc());
                    line.setSoldPriceExc(line.getProductPriceExc());
                }
// remove any tax exemption details applied to the ticket
                if (m_oTicket.isTaxExempt()) {
                    m_oTicket.setTaxExempt(false);
                    ProductInfoExt product = line.getProductInfoExt();
                    try {
                        line.setTaxInfo(dlSales.getTaxByCategoryID(product.getTaxCategoryID()));
                        line.setPrice(Double.parseDouble(line.getProperty("SellingPrice")));
                    } catch (BasicException ex) {
                    }
                }
            }

            m_oTicket.setTicketDiscountRate(0.00);
            m_oTicket.setTicketDiscount(0.00);
            refreshTicket();
            return true;
        }
        return false;
    }

    private void processCustomer(CustomerInfoExt newCustomer) {
        Integer customerStatus = newCustomer.getCustomerStatus();
        String alertMessage = AppLocal.getIntString("message.customername", newCustomer.getName());
        String customMessage = "";

        m_oTicket.setTaxExempt(false);

        switch (customerStatus) {
// Customer with discount only            
            case 1:
                customMessage = AppLocal.getIntString("message.customerdiscountrate", newCustomer.getCustomerDiscount() * 100);
                break;
// tax exempt
            case 2:
                if (SystemProperty.TAXEXEMPTION) {
                    customMessage = AppLocal.getIntString("message.taxexemption");
                    m_oTicket.setTaxExempt(true);
                }
                break;
// Tax exempt with discount                
            case 3:
                if (SystemProperty.TAXEXEMPTION) {
                    customMessage = AppLocal.getIntString("message.taxexemptionwithdiscount", newCustomer.getCustomerDiscount() * 100);
                    m_oTicket.setTaxExempt(true);
                } else {
                    customMessage = AppLocal.getIntString("message.customerdiscountrate", newCustomer.getCustomerDiscount() * 100);
                }
                break;
// Tax exempt expired                
            case 6:
                if (SystemProperty.TAXEXEMPTION) {
                    customMessage = AppLocal.getIntString("message.exemptionexpired");
                }
                break;
            case 7:
                if (SystemProperty.TAXEXEMPTION) {
                    customMessage = AppLocal.getIntString("message.exemptionexpiredwithdiscount", newCustomer.getCustomerDiscount() * 100);
                } else {
                    customMessage = AppLocal.getIntString("message.customerdiscountrate", newCustomer.getCustomerDiscount() * 100);
                }
        }
        JAlertPane.messageBox(JAlertPane.INFORMATION, alertMessage + customMessage, 16,
                new Dimension(125, 50), JAlertPane.OK_OPTION);

// Change to tax exempt if applicable
        if (m_oTicket.isTaxExempt()) {
            for (TicketLineInfo line : m_oTicket.getLines()) {
                applyTaxExemption(line);
            }
        }

// Apply ticket discount 
        if ((customerStatus & 1) == 1) {
            m_oTicket.setTicketDiscountRate(newCustomer.getCustomerDiscount());
            for (TicketLineInfo line : m_oTicket.getLines()) {
                applyDiscount(line);
            }
        }

        refreshTicket();

        m_oTicket.setCustomer(newCustomer);
        m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));

        String loyaltyNumber = DataLogicLoyalty.getCardByCustomerId(newCustomer.getId());

        if (DataLogicLoyalty.isCardAvailable(loyaltyNumber)) {
            if (loyaltyCard == null) {
                if (SystemProperty.LOYALTYTYPE.equals("earnx")) {
                    loyaltyCard = new CollectTransactionLoyaltyPoints(loyaltyNumber);
                } else {
                    loyaltyCard = new CollectItemLoyaltyPoints(loyaltyNumber);
                }
                m_oTicket.setLoyaltyCard(loyaltyCard);

                if (loyaltyCard != null) {
                    m_jLblLoyaltyCard.setText("Loyalty card : " + loyaltyNumber);
                    loyaltyCard.showCardStatus(this);
                }
            }
        }

        if ("restaurant".equals(TerminalInfo.getPosType())) {
            restDB.setCustomerNameInTableByTicketId(newCustomer.getId(), m_oTicket.getId());
        }

        AutoLogoff.getInstance().activateTimer();
        refreshTicket();
    }

    private void applyDiscount(TicketLineInfo line) {
        if (line.discountAllowed() && !line.isDiscounted()) {
            processDiscount(line, m_oTicket.getTicketDiscountRate());
        }
    }

    private void processDiscount(TicketLineInfo line, Double discountRate) {
        if (line.discountAllowed() && !line.isDiscounted()) {
            Double appliedRate = (1 - discountRate >= 1 - line.getMaxDiscountRate() / 100)
                    ? 1 - discountRate : 1 - line.getMaxDiscountRate() / 100;
            Double rate = (discountRate >= line.getMaxDiscountRate() / 100)
                    ? line.getMaxDiscountRate() : discountRate * 100;
            line.setProperty("product.oldname", line.getProductName());
            line.setProperty("product.name", line.getProductName() + " - " + Formats.PERCENT.formatValue(rate / 100));
            line.setProperty("prediscount", line.getPrice());
            line.setDiscounted("yes");
            m_oTicket.addTicketDiscount(line.getPrice() - (double) Math.rint(line.getPrice() * appliedRate * 100) / 100d);
            line.setProperty("edited", "1");
            line.setPrice((double) Math.rint(line.getPrice() * appliedRate * 100) / 100d);
            line.setPriceExcl((double) Math.rint(line.getPriceExc() * appliedRate * 100) / 100d);
        }
    }

    private void applyTaxExemption(TicketLineInfo line) {
// Get tax exempt id from tax table      
        if (!SystemProperty.TAXEXEMPTION) {
            return;
        }
        String taxExempt = dlSales.getExemptTaxDetails();
        ProductInfoExt product = line.getProductInfoExt();
        TaxInfo tax;
        try {
            tax = dlSales.getTaxByID(taxExempt);
            line.setTaxInfo(tax);
            line.setProperty("SellingPrice", line.getPrice());
            line.setPrice(product.getPriceSell());
            line.setPriceExcl(product.getPriceSell());
        } catch (BasicException ex) {
        }

    }

    private void jbtnLogout() {
        AutoLogoff.getInstance().deactivateTimer();
        deactivate();
        try {
            ((JRootApp) m_App).closeAppView();
        } catch (Exception ex) {
        }
    }

    public class ScriptObject {

        private final TicketInfo ticket;
        private final Object ticketext;
        private int selectedindex;

        private ScriptObject(TicketInfo ticket, Object ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }

        public double getInputValue() {
            if (m_iNumberStatusInput == NUMBERVALID && m_iNumberStatusQty == NUMBERZERO) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }

        public int getSelectedIndex() {
            return selectedindex;
        }

        public void setSelectedIndex(int i) {
            selectedindex = i;
        }

        public void kitchenOrderScreen() {
            JPanelTicket.this.kitchenOrderScreen(kitchenOrderId(), 1, false);
        }

        public void kitchenOrderScreen(String id) {
            JPanelTicket.this.kitchenOrderScreen(id, 1, false);
        }

        public void kitchenOrderScreen(Integer display, Boolean primary) {
            JPanelTicket.this.kitchenOrderScreen(kitchenOrderId(), display, primary);
        }

        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext, false);
        }

        public Object evalScript(String code, ScriptArg... args) throws ScriptException {
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            try {
                script.put("hostname", DbUtils.getTerminalName());
                script.put("dbPassword", AppConfig.getClearDatabasePassword());
                script.put("ticket", ticket);
                script.put("place", ticketext);
                script.put("user", m_App.getAppUserView().getUser());
                script.put("sales", this);
                script.put("warranty", receiptRequired);
                script.put("pickupid", getPickupString(ticket));
                script.put("m_App", m_App);
                script.put("m_TTP", m_TTP);
                script.put("dlSystem", dlSystem);
                script.put("dlSales", dlSales);
                script.put("pickupcode", "P" + ticket.getId().substring(24));
                script.put("taxincluded", SystemProperty.TAXINCLUDED);
                script.put("pickupenabled", SystemProperty.PICKUPENABLED);
                script.put("receipt", paymentdialog);
                script.put("cardType", new String[]{"", LocalResource.getString("paymentdescription.cmagcard1"), LocalResource.getString("paymentdescription.cmagcard2"),
                    LocalResource.getString("paymentdescription.cmagcard3"), LocalResource.getString("paymentdescription.cmagcard4"), LocalResource.getString("transpayment.cmagcard5")});

                script.put("cardRefundType", new String[]{"", LocalResource.getString("paymentdescription.cmagcard1refund"), LocalResource.getString("paymentdescription.cmagcard2refund"),
                    LocalResource.getString("paymentdescription.cmagcard3refund"), LocalResource.getString("paymentdescription.cmagcard4refund"), LocalResource.getString("paymentdescription.cmagcard5refund")});

                ReceiptTaxesInfo ltr = new ReceiptTaxesInfo();
                List<ReceiptTaxesInfo> lines = ltr.getReceiptTaxLines(dlSales.getLineTaxRates(ticket.getId()));
                script.put("nett", ltr.printReceiptSubTotal(lines, ticket.getTicketTotal()));
                script.put("tickettaxdetails", lines);
                script.put("deliveryinfo", deliveryInfo);

                for (ScriptArg arg : args) {
                    script.put(arg.getKey(), arg.getValue());
                }

            } catch (BasicException ex) {
                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
            }
            return script.eval(code);
        }

        public void applyDiscount(TicketLineInfo line, Double discountRate) {
            processDiscount(line, discountRate);
        }

        public void removeDiscount(TicketLineInfo line) {
            line.setProperty("product.name", line.getProperty("product.oldname"));
            line.setDiscounted("no");
            line.setProperty("edited", "0");
            ticket.minusTicketDiscount(Double.parseDouble(line.getProperty("prediscount")) - line.getPrice());
            line.setPrice(Double.parseDouble(line.getProperty("prediscount")));
            line.setProperty("discountrate", "");
        }
    }

    public static class ScriptArg {

        private final String key;
        private final Object value;

        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }
    }

    private class logout extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent ae) {
            switch (TerminalInfo.getPosType()) {
                case "restaurant":
                    if (!SystemProperty.AUTOLOGOFFTOTABLES && SystemProperty.ENABLEAUTOLOGOFF) {
                        deactivate();
                        ((JRootApp) m_App).closeAppView();
                        break;
                    }
                    deactivate();
                    setActiveTicket(null, null);
                    if (AutoLogoff.getInstance().getActiveFrame() != null) {
                        AutoLogoff.getInstance().getActiveFrame().dispose();
                        AutoLogoff.getInstance().setActiveFrame(null);
                    }
                    break;
                default:
                    deactivate();
                    if (AutoLogoff.getInstance().getActiveFrame() != null) {
                        AutoLogoff.getInstance().getActiveFrame().dispose();
                        AutoLogoff.getInstance().setActiveFrame(null);
                    }
                    ((JRootApp) m_App).closeAppView();
            }
        }
    }

    private void btnReprintAction() {
        AutoLogoff.getInstance().deactivateTimer();
// test if there is valid ticket in the system at this till to be printed
        if (AppConfig.getString("lastticket.number") != null) {
            try {
                TicketInfo ticket = dlSales.loadTicket(Integer.parseInt((AppConfig.getString("lastticket.type"))), Integer.parseInt((AppConfig.getString("lastticket.number"))));
                if (ticket == null) {
                    JAlertPane.messageBox(JAlertPane.INFORMATION, AppLocal.getIntString("message.notexiststicket"), 16,
                            new Dimension(125, 50), JAlertPane.OK_OPTION);
                } else {
                    m_ticket = ticket;
                    m_ticketCopy = null;
                    try {
                        taxeslogic.calculateTaxes(m_ticket);
                    } catch (TaxesException ex) {
                    }
                    deliveryInfo = dlCustomers.fetchCustomerDelivery(m_ticket.getId());
                    printTicket("Printer.ReprintLastTicket", m_ticket, null, false);
                }
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadticket"), e);
                msg.show(this);
            }
        }
        AutoLogoff.getInstance().activateTimer();
    }

    private void resetCache() {
        System.out.println("Reseting !!!!!!!");
        dlSystem.resetResourcesCache();
    }

    public void btnKitchenPrtAction(String script) {
        // John L - replace older SendOrder script        
        AutoLogoff.getInstance().deactivateTimer();
        if (!m_oTicket.isRefund()) {
            String rScript = (dlSystem.getResourceAsText(script));
            Interpreter i = new Interpreter();
            try {
                i.set("ticket", m_oTicket);
                i.set("place", m_oTicketExt);
                i.set("user", m_App.getAppUserView().getUser());
                i.set("sales", this);
                i.set("pickupid", m_oTicket.getPickupId());
                i.set("pickupcode", "P" + m_oTicket.getId().substring(24));
                i.set("deliveryinfo", deliveryInfo);

                Object result;
                result = i.eval(rScript);

            } catch (EvalError ex) {
                Logger.getLogger(JPanelTicket.class.getName()).log(Level.SEVERE, null, ex);
            }

            AutoLogoff.getInstance().activateTimer();
            // Autologoff after sending to kitchen if required
            // lets check what mode we are operating in               
            switch (TerminalInfo.getPosType()) {
                case "restaurant":
                    if (SystemProperty.ENABLEAUTOLOGOFF && SystemProperty.AUTOLOGOFFAFTERKITCHEN) {
                        if (SystemProperty.AUTOLOGOFFTOTABLES) {
                            deactivate();
                            setActiveTicket(null, null);
                            break;
                        } else {
                            deactivate();
                            ((JRootApp) m_App).closeAppView();
                            break;
                        }
                    }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        m_jPanContainer = new javax.swing.JPanel();
        m_jOptions = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnActionMenu = new javax.swing.JButton();
        m_jButtons = new javax.swing.JPanel();
        btnCustomer = new javax.swing.JButton();
        btnSplit = new javax.swing.JButton();
        btnReprint = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        m_jPanelScripts = new javax.swing.JPanel();
        m_jButtonsExt = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jbtnScale = new javax.swing.JButton();
        j_btnKitchenPrt = new javax.swing.JButton();
        m_jPanelBag = new javax.swing.JPanel();
        m_jPanTicket = new javax.swing.JPanel();
        ticketActions = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jUp = new javax.swing.JButton();
        m_jDown = new javax.swing.JButton();
        m_jDelete = new javax.swing.JButton();
        m_jList = new javax.swing.JButton();
        jEditAttributes = new javax.swing.JButton();
        m_jEditLine = new javax.swing.JButton();
        ticketPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_jTicketId = new javax.swing.JLabel();
        m_jLblLoyaltyCard = new javax.swing.JLabel();
        m_jPanTotals = new javax.swing.JPanel();
        m_jLblTotalEuros3 = new javax.swing.JLabel();
        m_jLblTotalEuros2 = new javax.swing.JLabel();
        m_jLblTotalEuros1 = new javax.swing.JLabel();
        m_jSubtotalEuros = new javax.swing.JLabel();
        m_jTaxesEuros = new javax.swing.JLabel();
        m_jTotalEuros = new javax.swing.JLabel();
        m_jContEntries = new javax.swing.JPanel();
        m_jPanEntries = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        m_jPrice = new javax.swing.JLabel();
        m_jQty = new javax.swing.JLabel();
        m_jEnter = new javax.swing.JButton();
        m_jNumberKey = new uk.chromis.beans.JNumberKeys();
        m_jKeyFactory = new javax.swing.JTextField();
        m_jPanEntriesE = new javax.swing.JPanel();
        catcontainer = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 204, 153));
        setLayout(new java.awt.CardLayout());

        m_jPanContainer.setLayout(new java.awt.BorderLayout());

        m_jOptions.setLayout(new java.awt.BorderLayout());

        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel6.setPreferredSize(new java.awt.Dimension(58, 40));

        btnActionMenu.setIcon(IconFactory.getIcon("menu.png"));
        btnActionMenu.setFocusPainted(false);
        btnActionMenu.setFocusable(false);
        btnActionMenu.setMargin(new java.awt.Insets(0, 0, 0, 4));
        btnActionMenu.setMaximumSize(new java.awt.Dimension(52, 40));
        btnActionMenu.setMinimumSize(new java.awt.Dimension(52, 40));
        btnActionMenu.setPreferredSize(new java.awt.Dimension(52, 40));
        btnActionMenu.setRequestFocusEnabled(false);
        btnActionMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenu(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(btnActionMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(btnActionMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel7.add(jPanel6, java.awt.BorderLayout.LINE_START);

        m_jButtons.setPreferredSize(new java.awt.Dimension(240, 56));
        m_jButtons.setRequestFocusEnabled(false);

        btnCustomer.setIcon(IconFactory.getIcon("customer_sml.png"));
        btnCustomer.setToolTipText("");
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnCustomer.setMaximumSize(new java.awt.Dimension(50, 40));
        btnCustomer.setMinimumSize(new java.awt.Dimension(50, 40));
        btnCustomer.setPreferredSize(new java.awt.Dimension(52, 40));
        btnCustomer.setRequestFocusEnabled(false);

        btnSplit.setIcon(IconFactory.getIcon("sale_split_sml.png"));
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnSplit.setMaximumSize(new java.awt.Dimension(50, 40));
        btnSplit.setMinimumSize(new java.awt.Dimension(50, 40));
        btnSplit.setPreferredSize(new java.awt.Dimension(52, 40));
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitTicket(evt);
            }
        });

        btnReprint.setIcon(IconFactory.getIcon("reprint.png"));
        btnReprint.setFocusPainted(false);
        btnReprint.setFocusable(false);
        btnReprint.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnReprint.setMaximumSize(new java.awt.Dimension(50, 40));
        btnReprint.setMinimumSize(new java.awt.Dimension(50, 40));
        btnReprint.setPreferredSize(new java.awt.Dimension(52, 40));
        btnReprint.setRequestFocusEnabled(false);

        btnLogout.setIcon(IconFactory.getIcon("logout.png"));
        btnLogout.setFocusPainted(false);
        btnLogout.setFocusable(false);
        btnLogout.setMargin(new java.awt.Insets(0, 4, 0, 4));
        btnLogout.setMaximumSize(new java.awt.Dimension(50, 40));
        btnLogout.setMinimumSize(new java.awt.Dimension(50, 40));
        btnLogout.setPreferredSize(new java.awt.Dimension(52, 40));
        btnLogout.setRequestFocusEnabled(false);

        javax.swing.GroupLayout m_jButtonsLayout = new javax.swing.GroupLayout(m_jButtons);
        m_jButtons.setLayout(m_jButtonsLayout);
        m_jButtonsLayout.setHorizontalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReprint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(68, 68, 68))
        );
        m_jButtonsLayout.setVerticalGroup(
            m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(m_jButtonsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(m_jButtonsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReprint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel7.add(m_jButtons, java.awt.BorderLayout.CENTER);

        m_jOptions.add(jPanel7, java.awt.BorderLayout.LINE_START);

        m_jPanelScripts.setLayout(new java.awt.BorderLayout());

        m_jButtonsExt.setLayout(new javax.swing.BoxLayout(m_jButtonsExt, javax.swing.BoxLayout.LINE_AXIS));

        jPanel1.setMinimumSize(new java.awt.Dimension(235, 50));

        m_jbtnScale.setFont(ChromisFonts.DEFAULTFONT.deriveFont(14f));
        m_jbtnScale.setIcon(IconFactory.getIcon("scale.png"));
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jbtnScale.setMaximumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setMinimumSize(new java.awt.Dimension(85, 44));
        m_jbtnScale.setPreferredSize(new java.awt.Dimension(85, 40));
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openScalesPanel(evt);
            }
        });
        jPanel1.add(m_jbtnScale);

        j_btnKitchenPrt.setIcon(IconFactory.getIcon("networkprt.png"));
        j_btnKitchenPrt.setMargin(new java.awt.Insets(0, 4, 0, 4));
        j_btnKitchenPrt.setMaximumSize(new java.awt.Dimension(50, 40));
        j_btnKitchenPrt.setMinimumSize(new java.awt.Dimension(50, 40));
        j_btnKitchenPrt.setPreferredSize(new java.awt.Dimension(52, 40));
        j_btnKitchenPrt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendToKitchen(evt);
            }
        });
        jPanel1.add(j_btnKitchenPrt);

        m_jButtonsExt.add(jPanel1);

        m_jPanelScripts.add(m_jButtonsExt, java.awt.BorderLayout.LINE_END);

        m_jOptions.add(m_jPanelScripts, java.awt.BorderLayout.LINE_END);

        m_jPanelBag.setPreferredSize(new java.awt.Dimension(0, 50));
        m_jPanelBag.setLayout(new java.awt.BorderLayout());
        m_jOptions.add(m_jPanelBag, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jOptions, java.awt.BorderLayout.NORTH);

        m_jPanTicket.setLayout(new java.awt.BorderLayout());

        ticketActions.setFont(ChromisFonts.DEFAULTFONT.deriveFont(14f));
        ticketActions.setPreferredSize(new java.awt.Dimension(60, 200));
        ticketActions.setLayout(new java.awt.BorderLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        jPanel2.setLayout(new java.awt.GridLayout(0, 1, 5, 5));

        m_jUp.setIcon(IconFactory.getIcon("1uparrow.png"));
        m_jUp.setFocusPainted(false);
        m_jUp.setFocusable(false);
        m_jUp.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jUp.setMaximumSize(new java.awt.Dimension(52, 36));
        m_jUp.setMinimumSize(new java.awt.Dimension(52, 36));
        m_jUp.setPreferredSize(new java.awt.Dimension(52, 36));
        m_jUp.setRequestFocusEnabled(false);
        m_jUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTicketLineAbove(evt);
            }
        });
        jPanel2.add(m_jUp);

        m_jDown.setIcon(IconFactory.getIcon("1downarrow.png"));
        m_jDown.setFocusPainted(false);
        m_jDown.setFocusable(false);
        m_jDown.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDown.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDown.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDown.setPreferredSize(new java.awt.Dimension(52, 36));
        m_jDown.setRequestFocusEnabled(false);
        m_jDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectTicketLineBelow(evt);
            }
        });
        jPanel2.add(m_jDown);

        m_jDelete.setIcon(IconFactory.getIcon("editdelete.png"));
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jDelete.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jDelete.setPreferredSize(new java.awt.Dimension(52, 36));
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeTicketLine(evt);
            }
        });
        jPanel2.add(m_jDelete);

        m_jList.setIcon(IconFactory.getIcon("search32.png"));
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jList.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jList.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jList.setPreferredSize(new java.awt.Dimension(52, 36));
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchForProduct(evt);
            }
        });
        jPanel2.add(m_jList);

        jEditAttributes.setIcon(IconFactory.getIcon("attributes.png"));
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setMargin(new java.awt.Insets(8, 14, 8, 14));
        jEditAttributes.setMaximumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setMinimumSize(new java.awt.Dimension(42, 36));
        jEditAttributes.setPreferredSize(new java.awt.Dimension(52, 36));
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setTicketLineAttributes(evt);
            }
        });
        jPanel2.add(jEditAttributes);

        m_jEditLine.setIcon(IconFactory.getIcon("sale_editline.png"));
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setMargin(new java.awt.Insets(8, 14, 8, 14));
        m_jEditLine.setMaximumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setMinimumSize(new java.awt.Dimension(42, 36));
        m_jEditLine.setPreferredSize(new java.awt.Dimension(52, 36));
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editTicketLine(evt);
            }
        });
        jPanel2.add(m_jEditLine);

        ticketActions.add(jPanel2, java.awt.BorderLayout.NORTH);

        m_jPanTicket.add(ticketActions, java.awt.BorderLayout.LINE_END);

        ticketPanel.setFont(ChromisFonts.DEFAULTFONT);
        ticketPanel.setPreferredSize(new java.awt.Dimension(450, 250));
        ticketPanel.setLayout(new java.awt.BorderLayout());

        jPanel4.setAlignmentX(3.0F);
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(675, 60));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.GridLayout(2, 1, 4, 1));

        m_jTicketId.setFont(ChromisFonts.DEFAULTFONT);
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jPanel3.add(m_jTicketId);

        m_jLblLoyaltyCard.setFont(ChromisFonts.DEFAULTFONT);
        m_jLblLoyaltyCard.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        m_jLblLoyaltyCard.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel3.add(m_jLblLoyaltyCard);

        jPanel4.add(jPanel3, java.awt.BorderLayout.CENTER);

        m_jPanTotals.setPreferredSize(new java.awt.Dimension(375, 60));
        m_jPanTotals.setLayout(new java.awt.GridLayout(2, 3, 4, 0));

        m_jLblTotalEuros3.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jLblTotalEuros3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros3.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros3.setText(LocalResource.getString("label.subtotalcash"));
        m_jPanTotals.add(m_jLblTotalEuros3);

        m_jLblTotalEuros2.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jLblTotalEuros2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros2.setLabelFor(m_jSubtotalEuros);
        m_jLblTotalEuros2.setText(LocalResource.getString("label.taxcash"));
        m_jPanTotals.add(m_jLblTotalEuros2);

        m_jLblTotalEuros1.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jLblTotalEuros1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jLblTotalEuros1.setLabelFor(m_jTotalEuros);
        m_jLblTotalEuros1.setText(LocalResource.getString("label.total"));
        m_jPanTotals.add(m_jLblTotalEuros1);

        m_jSubtotalEuros.setBackground(m_jEditLine.getBackground());
        m_jSubtotalEuros.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(22f)
        );
        m_jSubtotalEuros.setForeground(m_jEditLine.getForeground());
        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jSubtotalEuros.setLabelFor(m_jSubtotalEuros);
        m_jSubtotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jSubtotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jSubtotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setOpaque(true);
        m_jSubtotalEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jSubtotalEuros);

        m_jTaxesEuros.setBackground(m_jEditLine.getBackground());
        m_jTaxesEuros.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(22f)
        );
        m_jTaxesEuros.setForeground(m_jEditLine.getForeground());
        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTaxesEuros.setLabelFor(m_jTaxesEuros);
        m_jTaxesEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTaxesEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTaxesEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setOpaque(true);
        m_jTaxesEuros.setPreferredSize(new java.awt.Dimension(80, 25));
        m_jTaxesEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jTaxesEuros);

        m_jTotalEuros.setBackground(m_jEditLine.getBackground());
        m_jTotalEuros.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(22f)
        );
        m_jTotalEuros.setForeground(m_jEditLine.getForeground());
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTotalEuros.setLabelFor(m_jTotalEuros);
        m_jTotalEuros.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jTotalEuros.setMaximumSize(new java.awt.Dimension(125, 25));
        m_jTotalEuros.setMinimumSize(new java.awt.Dimension(80, 25));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setPreferredSize(new java.awt.Dimension(100, 25));
        m_jTotalEuros.setRequestFocusEnabled(false);
        m_jPanTotals.add(m_jTotalEuros);

        jPanel4.add(m_jPanTotals, java.awt.BorderLayout.LINE_END);

        ticketPanel.add(jPanel4, java.awt.BorderLayout.SOUTH);

        m_jPanTicket.add(ticketPanel, java.awt.BorderLayout.CENTER);

        m_jPanContainer.add(m_jPanTicket, java.awt.BorderLayout.CENTER);

        m_jContEntries.setFont(ChromisFonts.DEFAULTFONT.deriveFont(14f)
        );
        m_jContEntries.setLayout(new java.awt.BorderLayout());

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 5, 0));
        jPanel9.setMaximumSize(new java.awt.Dimension(250, 60));
        jPanel9.setMinimumSize(new java.awt.Dimension(250, 60));
        jPanel9.setPreferredSize(new java.awt.Dimension(250, 60));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        m_jPrice.setBackground(m_jEditLine.getBackground());
        m_jPrice.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jPrice.setForeground(m_jEditLine.getForeground());
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setText("price");
        m_jPrice.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jPrice.setMaximumSize(new java.awt.Dimension(125, 40));
        m_jPrice.setMinimumSize(new java.awt.Dimension(125, 40));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(125, 40));
        m_jPrice.setRequestFocusEnabled(false);
        m_jPrice.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel9.add(m_jPrice, gridBagConstraints);

        m_jQty.setBackground(m_jEditLine.getBackground());
        m_jQty.setFont(ChromisFonts.DEFAULTFONTBOLD);
        m_jQty.setForeground(m_jEditLine.getForeground());
        m_jQty.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jQty.setText("qty");
        m_jQty.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
        m_jQty.setMaximumSize(new java.awt.Dimension(45, 40));
        m_jQty.setMinimumSize(new java.awt.Dimension(45, 40));
        m_jQty.setOpaque(true);
        m_jQty.setPreferredSize(new java.awt.Dimension(45, 40));
        m_jQty.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jQty, gridBagConstraints);

        m_jEnter.setIcon(IconFactory.getIcon("barcode.png"));
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setIconTextGap(1);
        m_jEnter.setInheritsPopupMenu(true);
        m_jEnter.setMargin(new java.awt.Insets(2, 2, 2, 2));
        m_jEnter.setMaximumSize(new java.awt.Dimension(55, 40));
        m_jEnter.setMinimumSize(new java.awt.Dimension(55, 40));
        m_jEnter.setPreferredSize(new java.awt.Dimension(55, 40));
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processManualBarcode(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jEnter, gridBagConstraints);

        m_jPanEntries.add(jPanel9);

        m_jNumberKey.setMinimumSize(new java.awt.Dimension(200, 200));
        m_jNumberKey.setPreferredSize(new java.awt.Dimension(250, 250));
        m_jNumberKey.addJNumberEventListener(new uk.chromis.beans.JNumberEventListener() {
            public void keyPerformed(uk.chromis.beans.JNumberEvent evt) {
                processKeypadEntry(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKey);

        m_jKeyFactory.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
        });
        m_jPanEntries.add(m_jKeyFactory);

        m_jContEntries.add(m_jPanEntries, java.awt.BorderLayout.NORTH);

        m_jPanEntriesE.setLayout(new java.awt.BorderLayout());
        m_jContEntries.add(m_jPanEntriesE, java.awt.BorderLayout.LINE_END);

        m_jPanContainer.add(m_jContEntries, java.awt.BorderLayout.LINE_END);

        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        m_jPanContainer.add(catcontainer, java.awt.BorderLayout.SOUTH);

        add(m_jPanContainer, "ticket");
    }// </editor-fold>//GEN-END:initComponents

    private void openScalesPanel(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openScalesPanel
        stateTransition('\u00a7');
    }//GEN-LAST:event_openScalesPanel

    private void editTicketLine(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editTicketLine
        AutoLogoff.getInstance().deactivateTimer();
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else {
            try {
                TicketLineInfo newline = new JProductLineEdit(m_App, m_oTicket.getLine(i)).getTicketLine();

                if (newline != null) {
                    // line has been modified
                    Double newPrice = newline.getPrice();
                    Double newPricExec = newline.getSoldPriceExe();
                    newline.setProductInfoExt(dlSales.getProductInfo(newline.getProductID()));

                    newline.setPrice(newPrice);
                    // newline.setPriceExec(newPricExec);
                    newline.setProperty("edited", "1");

                    if (newline.getProductID().equalsIgnoreCase("giftcard-sale") || newline.getProductID().equalsIgnoreCase("giftcard-topup")) {
                        newline.setProperty("cardbalance", Formats.CURRENCY.formatValue(giftCard.getCardBalance(barCode) + newline.getPrice()));
                    };

                    paintTicketLine(i, newline);

                    //is price update to catalog allowed
                    if (newline.getUpdated()) {
                        reLoadCatalog();
                    }
                }
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }

        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_editTicketLine

    private void processManualBarcode(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processManualBarcode
        if (m_jPrice.getText().trim().isEmpty()) {
            return;
        }
        stateTransition('\n');
    }//GEN-LAST:event_processManualBarcode

    private void processKeypadEntry(uk.chromis.beans.JNumberEvent evt) {//GEN-FIRST:event_processKeypadEntry
        stateTransition(evt.getKey());
    }//GEN-LAST:event_processKeypadEntry

    private void removeTicketLine(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeTicketLine
        int i = ticketLines.getSelectedIndex();
        if (i == -1) {
            return;
        }
        Double qty = m_oTicket.getLine(i).getMultiply();
        String name = m_oTicket.getLine(i).getProductName();

        if (m_oTicket.getLine(i).isServiceCharge()) {
            if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.deleteservicecharge"), 16,
                    new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {
                return;
            }
            m_oTicket.setNoSC("0");
        }

        if (m_oTicket.getLine(i).isDeliveryCharge()) {
            if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.deletedeliverycharge"), 16,
                    new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {
                return;
            }
            m_oTicket.setNoDelivery("0");
            deliveryInfo = null;
        }

        if ((m_oTicket.isRefund()) && (!m_oTicket.getLine(i).isProductCom())) {
            JRefundLines.addBackLine(m_oTicket.getLine(i).printName(), m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getPrice(), m_oTicket.getLine(i).getProperty("orgLine"));
            removeTicketLine(i, true);
            while (i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                JRefundLines.addBackLine(m_oTicket.getLine(i).printName(), m_oTicket.getLine(i).getMultiply(), m_oTicket.getLine(i).getPrice(), m_oTicket.getLine(i).getProperty("orgLine"));
                removeTicketLine(i, true);
            }
        } else if (m_oTicket.isRefund()) {

            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(null,
                    AppLocal.getIntString("message.deleteauxiliaryitem"),
                    "auxiliary Item", JOptionPane.WARNING_MESSAGE);
        } else if (i < 0) {
            errorBeep();
        } else {
            if (JAlertPane.messageBox(JAlertPane.WARNING, AppLocal.getIntString("message.finallineitem"), 16,
                    new Dimension(125, 50), JAlertPane.YES_NO_OPTION) == 6) {
                return;
            }
            String id = m_oTicket.getLine(i).getProductID();
            removeTicketLine(i, true);
            checkSellingPrices(m_oTicket, true, qty, name, id);
            checkServiceCharge(m_oTicket);
            refreshTicket();
        }
    }//GEN-LAST:event_removeTicketLine

    private void selectTicketLineAbove(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTicketLineAbove
        ticketLines.selectionUp();
    }//GEN-LAST:event_selectTicketLineAbove

    private void selectTicketLineBelow(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectTicketLineBelow
        ticketLines.selectionDown();
    }//GEN-LAST:event_selectTicketLineBelow

    private void searchForProduct(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchForProduct
        AutoLogoff.getInstance().deactivateTimer();
        JProductFinder productFinder = new JProductFinder(dlSales);
        productFinder.setVisible(true);

        ProductInfoExt prod = productFinder.getSelectedProduct();
        if (prod != null) {
            buttonTransition(prod);
        }
        AutoLogoff.getInstance().activateTimer();
    }//GEN-LAST:event_searchForProduct

    private void splitTicket(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitTicket
        AutoLogoff.getInstance().deactivateTimer();
        if (m_oTicket.getArticlesCount() > 1) {
            //read resource ticket.line and execute
            ReceiptSplit splitdialog;
            if (SystemProperty.TAXINCLUDED) {
                splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.LineIncTaxes"), dlSales, dlCustomers, taxeslogic);
            } else {
                splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.LineExclTaxes"), dlSales, dlCustomers, taxeslogic);
            }
            splitdialog.setLocationRelativeTo(this);

            TicketInfo ticket1 = m_oTicket.copyTicket();
            TicketInfo ticket2 = new TicketInfo();
            ticket2.setCustomer(m_oTicket.getCustomer());

            if (splitdialog.showDialog(ticket1, ticket2, m_oTicketExt)) {
                executeEvent(ticket2, m_oTicketExt, "ticket.change");
                if (closeTicket(ticket2, m_oTicketExt)) {
                    setActiveTicket(ticket1, m_oTicketExt);
                    executeEventAndRefresh("ticket.pretotals");
                    executeEventAndRefresh("ticket.change");
                }
            }
        }
        AutoLogoff.getInstance().activateTimer();

}//GEN-LAST:event_splitTicket

    private void setTicketLineAttributes(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setTicketLineAttributes
        // AutoLogoff.getInstance().deactivateTimer();
        int i = ticketLines.getSelectedIndex();
        if (i < 0) {
            errorBeep();
        } else {
            try {
                TicketLineInfo line = m_oTicket.getLine(i);
                JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setLocationRelativeTo(this);
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    // The user pressed OK
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    paintTicketLine(i, line);
                }
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
                AutoLogoff.getInstance().activateTimer();
            }
        }
        //  AutoLogoff.getInstance().activateTimer();
}//GEN-LAST:event_setTicketLineAttributes

    private void sendToKitchen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendToKitchen
        btnKitchenPrtAction("script.SendOrder");
    }//GEN-LAST:event_sendToKitchen

    private void openMenu(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenu
        principalApp.setMenuVisible(btnActionMenu);
    }//GEN-LAST:event_openMenu

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_jKeyFactoryKeyTyped
        mappedKeys.put("VK_F1", "class:uk.chromis.pos.sales.JPanelTicketEdits");
        mappedKeys.put("VK_F2", "class:uk.chromis.pos.customers.CustomersPayment");
        mappedKeys.put("VK_F3", "class:uk.chromis.pos.panels.JPanelPayments");
        mappedKeys.put("VK_F4", "class:uk.chromis.pos.panels.JPanelCloseMoney");
        mappedKeys.put("VK_F5", "action:btnCustomerAction");
        mappedKeys.put("VK_F6", "action:btnReprintAction");
        mappedKeys.put("VK_F7", "instance:uk.chromis.pos.sales.shared.JTicketsBagShared:newTicket:m_ticketsbag");
        mappedKeys.put("VK_F8", "instance:uk.chromis.pos.sales.shared.JTicketsBagShared:delTicketAction:m_ticketsbag");
        mappedKeys.put("VK_F9", "instance:uk.chromis.pos.sales.shared.JTicketsBagShared:selectLayaway:m_ticketsbag");
        mappedKeys.put("VK_F10", "instance:uk.chromis.pos.sales.JTicketsBag:deleteAllShared:m_ticketsbag");
        mappedKeys.put("VK_F11", "action:resetCache");
        mappedKeys.put("VK_F12", "instance:uk.chromis.pos.forms.JPrincipalApp:changePassword:principalApp");
        // instance = instance:name of class:methods to execute:instance name in this class

        if (evt.isControlDown() && KeyEvent.getKeyText(evt.getKeyCode()).equalsIgnoreCase("s")) {
            AppConfig.putInt("POSwidth", JRootFrame.PARENTFRAME.getWidth());
            AppConfig.putInt("POSheight", JRootFrame.PARENTFRAME.getHeight());
            AppConfig.putInt("POSx", JRootFrame.PARENTFRAME.getX());
            AppConfig.putInt("POSy", JRootFrame.PARENTFRAME.getY());
            return;
        }

        StringBuilder newTask = new StringBuilder();
        if (evt.isAltDown()) {
            newTask.append("Alt + ");
        }
        if (evt.isShiftDown()) {
            newTask.append("Shift + ");
        }
        if (evt.isControlDown()) {
            newTask.append("Ctrl + ");
        }

        newTask.append("VK_");
        newTask.append(KeyEvent.getKeyText(evt.getKeyCode()));
        String executionTask = mappedKeys.get(newTask.toString());

        if (executionTask != null) {
            if (executionTask.startsWith("class:")) {
                principalApp.showTask(executionTask.substring(6));

            } else if (executionTask.startsWith("action:")) {
                try {
                    Method method = uk.chromis.pos.sales.JPanelTicket.class
                            .getDeclaredMethod(executionTask.substring(7));
                    method.invoke(this);

                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(JPanelTicket.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            } else if (executionTask.startsWith("instance:")) {
                try {
                    int position = 0;
                    position = executionTask.indexOf(":", 10);
                    Class<?> clazz = Class.forName(executionTask.substring(9, position));
                    position++;
                    Method method = clazz.getDeclaredMethod(executionTask.substring(position, executionTask.indexOf(":", position)));
                    position = executionTask.indexOf(":", position) + 1;
                    method.invoke(clazz.cast(instanceMap.get(executionTask.substring(position))));

                } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
                    Logger.getLogger(JPanelTicket.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
            m_jKeyFactory.setText(null);
            return;
        }

        if (SystemProperty.SCANWITHDASHES) {
            fromNumberPad = false;
        }

        int code = evt.getKeyCode();
        m_jKeyFactory.setText(null);
        if ((code >= 16 && code <= 18) || (code >= 37 && code <= 40)) {
            return;
        }

        if (evt.getModifiersEx() == 0) {
            stateTransition(evt.getKeyChar());
        }
        fromNumberPad = true;
    }//GEN-LAST:event_m_jKeyFactoryKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnActionMenu;
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnReprint;
    private javax.swing.JButton btnSplit;
    protected javax.swing.JPanel catcontainer;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JPanel jPanel3;
    protected javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JButton j_btnKitchenPrt;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    protected javax.swing.JPanel m_jContEntries;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    protected javax.swing.JLabel m_jLblLoyaltyCard;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JButton m_jList;
    private uk.chromis.beans.JNumberKeys m_jNumberKey;
    private javax.swing.JPanel m_jOptions;
    protected javax.swing.JPanel m_jPanContainer;
    protected javax.swing.JPanel m_jPanEntries;
    protected javax.swing.JPanel m_jPanEntriesE;
    protected javax.swing.JPanel m_jPanTicket;
    protected javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelScripts;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jQty;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JLabel m_jTaxesEuros;
    protected javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jUp;
    private javax.swing.JButton m_jbtnScale;
    protected javax.swing.JPanel ticketActions;
    private javax.swing.JPanel ticketPanel;
    // End of variables declaration//GEN-END:variables

}
