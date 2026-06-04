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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import uk.chromis.globals.SystemProperty;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.ChromisFonts;
import uk.chromis.pos.scripting.ScriptEngine;
import uk.chromis.pos.scripting.ScriptException;
import uk.chromis.pos.scripting.ScriptFactory;
import uk.chromis.pos.ticket.TicketLineInfo;

public class JTicketLines extends javax.swing.JPanel {
    
    private static final Logger logger = Logger.getLogger("uk.chromis.pos.sales.JTicketLines");
    private static SAXParser m_sp = null;
    private final TicketTableModel m_jTableModel;
    private final Color bgcolour;
    private final Color selbgcolour;
    private final Color fgcolour;
    private final Color selfgcolour;
    
    private static Color decodeColor(String value, Color fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Color.decode(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Color waitingSelected = decodeColor(SystemProperty.WAITINGSELECTEDCOLOUR, Color.LIGHT_GRAY);
    private Color sentSelected = decodeColor(SystemProperty.SENTSELECTEDCOLOUR, Color.LIGHT_GRAY);
    private Color waitingColour = decodeColor(SystemProperty.WAITINGBACKGROUNDCOLOUR, Color.WHITE);
    private Color sentColour = decodeColor(SystemProperty.SENTBACKGROUNDCOLOUR, Color.WHITE);
    private Color scaleColour = decodeColor("#ff8080", Color.RED);
    private final HashMap<Integer, String> processed;
    
    public JTicketLines(String ticketline) {
        initComponents();
        ColumnTicket[] acolumns = new ColumnTicket[0];
        
        if (ticketline != null) {
            try {
                if (m_sp == null) {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    m_sp = spf.newSAXParser();
                }
                ColumnsHandler columnshandler = new ColumnsHandler();
                m_sp.parse(new InputSource(new StringReader(ticketline)), columnshandler);
                acolumns = columnshandler.getColumns();
                
            } catch (ParserConfigurationException ePC) {
                logger.log(Level.WARNING, AppLocal.getIntString("exception.parserconfig"), ePC);
            } catch (SAXException eSAX) {
                logger.log(Level.WARNING, AppLocal.getIntString("exception.xmlfile"), eSAX);
            } catch (IOException eIO) {
                logger.log(Level.WARNING, AppLocal.getIntString("exception.iofile"), eIO);
            }
        }
        
        m_jTableModel = new TicketTableModel(acolumns);
        m_jTicketTable.setModel(m_jTableModel);
        
        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        for (int i = 0; i < acolumns.length; i++) {
            jColumns.getColumn(i).setPreferredWidth(acolumns[i].width);
            jColumns.getColumn(i).setResizable(false);
        }
        
        m_jScrollTableTicket.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));
        
        m_jTicketTable.getTableHeader().setReorderingAllowed(false);
        m_jTicketTable.setDefaultRenderer(Object.class, new TicketCellRenderer(acolumns));
        
        int rowHeight = SystemProperty.LINESIZE;
        m_jTicketTable.setRowHeight(rowHeight < 1 ? 30 : rowHeight);
        
        m_jTicketTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        m_jTableModel.clear();
        
        processed = new HashMap();
        bgcolour = m_jTicketTable.getBackground();
        fgcolour = m_jTicketTable.getForeground();
        selfgcolour = m_jTicketTable.getSelectionForeground();
        selbgcolour = m_jTicketTable.getSelectionBackground();
        
    }
    
    public void addListSelectionListener(ListSelectionListener l) {
        m_jTicketTable.getSelectionModel().addListSelectionListener(l);
    }
    
    public void removeListSelectionListener(ListSelectionListener l) {
        m_jTicketTable.getSelectionModel().removeListSelectionListener(l);
    }
    
    public void clearTicketLines() {
        m_jTableModel.clear();
    }
    
    public void setTicketLine(int index, TicketLineInfo oLine) {
        
        m_jTableModel.setRow(index, oLine);
    }
    
    public void addTicketLine(TicketLineInfo oLine) {
        
        m_jTableModel.addRow(oLine);
        setSelectedIndex(m_jTableModel.getRowCount() - 1);
    }
    
    public void insertTicketLine(int index, TicketLineInfo oLine) {
        m_jTableModel.insertRow(index, oLine);
        setSelectedIndex(index);
    }
    
    public void removeTicketLine(int i) {
        m_jTableModel.removeRow(i);
        
        int rowcount = m_jTableModel.getRowCount();
        int j;
        if (i < rowcount) {
            for (j = i; j <= rowcount - 1; j++) {
                processed.put(j, processed.get(j + 1));
            }
            processed.remove(rowcount + 1);
        }
        
        if (i >= m_jTableModel.getRowCount()) {
            i = m_jTableModel.getRowCount() - 1;
        }
        
        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }
    
    public void setSelectedIndex(int i) {
        m_jTicketTable.getSelectionModel().setSelectionInterval(i, i);
        
        Rectangle oRect = m_jTicketTable.getCellRect(i, 0, true);
        m_jTicketTable.scrollRectToVisible(oRect);
    }
    
    public int getSelectedIndex() {
        return m_jTicketTable.getSelectionModel().getMinSelectionIndex();
    }
    
    public void selectionDown() {
        int i = m_jTicketTable.getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            i = 0;
        } else {
            i++;
            if (i >= m_jTableModel.getRowCount()) {
                i = m_jTableModel.getRowCount() - 1;
            }
        }
        
        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }
    
    public void selectionUp() {
        
        int i = m_jTicketTable.getSelectionModel().getMinSelectionIndex();
        if (i < 0) {
            i = m_jTableModel.getRowCount() - 1;
        } else {
            i--;
            if (i < 0) {
                i = 0;
            }
        }
        
        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }
    
    private class TicketCellRenderer extends DefaultTableCellRenderer {
        
        private final ColumnTicket[] m_acolumns;
        
        public TicketCellRenderer(ColumnTicket[] acolumns) {
            m_acolumns = acolumns;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel aux = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            aux.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            aux.setHorizontalAlignment(m_acolumns[column].align);
            aux.setFont(ChromisFonts.DEFAULTFONT);
            this.setBackground(processRowColour(row, isSelected));
            this.setForeground(getContrastColor(processRowColour(row, isSelected)));
            return aux;
        }
    }
    
    private Color processRowColour(int row, boolean isSelected) {
        if (SystemProperty.COLOURTICKETLINES && processed.get(row) != null) {
            switch (processed.get(row).toLowerCase()) {
                case "ok":
                    if (isSelected) {
                        return sentSelected;
                    } else {
                        return sentColour;
                    }
                case "waiting":
                    if (isSelected) {
                        return waitingSelected;
                    } else {
                        return waitingColour;
                    }
                case "scale":
                    if (isSelected) {
                        return scaleColour;
                    } else {
                        return bgcolour;
                    }
                default:
                    if (isSelected) {
                        return selbgcolour;
                    } else {
                        return bgcolour;
                    }
            }
        } else if (isSelected) {
            return selbgcolour;
        } else {
            return bgcolour;
        }
    }
    
    public static Color getContrastColor(Color color) {
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        return y >= 128 ? Color.black : Color.white;
    }
    
    private class TicketTableModel extends AbstractTableModel {
        
        private final ColumnTicket[] m_acolumns;
        private final ArrayList m_rows = new ArrayList();
        
        public TicketTableModel(ColumnTicket[] acolumns) {
            m_acolumns = acolumns;
        }
        
        @Override
        public int getRowCount() {
            return m_rows.size();
        }
        
        @Override
        public int getColumnCount() {
            return m_acolumns.length;
        }
        
        @Override
        public String getColumnName(int column) {
            return AppLocal.getIntString(m_acolumns[column].name);
        }
        
        @Override
        public Object getValueAt(int row, int column) {
            return ((String[]) m_rows.get(row))[column];
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
        public void clear() {
            int old = getRowCount();
            if (old > 0) {
                m_rows.clear();
                fireTableRowsDeleted(0, old - 1);
            }
        }
        
        public void setRow(int index, TicketLineInfo oLine) {
            
            String[] row = (String[]) m_rows.get(index);
            for (int i = 0; i < m_acolumns.length; i++) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticketline", oLine);
                    row[i] = script.eval(m_acolumns[i].value).toString();
                } catch (ScriptException e) {
                    row[i] = null;
                }
                setProperties(oLine.getProperties(), index);
                fireTableCellUpdated(index, i);
            }
        }
        
        public void addRow(TicketLineInfo oLine) {
            
            insertRow(m_rows.size(), oLine);
        }
        
        public void insertRow(int index, TicketLineInfo oLine) {
            String[] row = new String[m_acolumns.length];
            for (int i = 0; i < m_acolumns.length; i++) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticketline", oLine);
                    row[i] = script.eval(m_acolumns[i].value).toString();
                } catch (ScriptException e) {
                    row[i] = null;
                }
            }
            setProperties(oLine.getProperties(), index);
            m_rows.add(index, row);
            fireTableRowsInserted(index, index);
        }
        
        public void removeRow(int row) {
            m_rows.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }
    
    private void setProperties(Properties attributes, int index) {
        if (attributes.getProperty("sendstatus") == null) {
            attributes.setProperty("sendstatus", "No");
        }
        processed.put(index, attributes.getProperty("sendstatus"));
        if (((Boolean.valueOf(attributes.getProperty("product.kitchen")).booleanValue()) || (Boolean.valueOf(attributes.getProperty("usedisplay")).booleanValue())) && (attributes.getProperty("sendstatus").equals("No"))) {
            processed.put(index, "waiting");
        }
    }
    
    private static class ColumnsHandler extends DefaultHandler {
        
        private ArrayList m_columns = null;
        
        public ColumnTicket[] getColumns() {
            return (ColumnTicket[]) m_columns.toArray(new ColumnTicket[m_columns.size()]);
        }
        
        @Override
        public void startDocument() throws SAXException {
            m_columns = new ArrayList();
        }
        
        @Override
        public void endDocument() throws SAXException {
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("column".equals(qName)) {
                ColumnTicket c = new ColumnTicket();
                c.name = attributes.getValue("name");
                c.width = Integer.parseInt(attributes.getValue("width"));
                String sAlign = attributes.getValue("align");
                switch (sAlign) {
                    case "right":
                        c.align = javax.swing.SwingConstants.RIGHT;
                        break;
                    case "center":
                        c.align = javax.swing.SwingConstants.CENTER;
                        break;
                    default:
                        c.align = javax.swing.SwingConstants.LEFT;
                        break;
                }
                c.value = attributes.getValue("value");
                m_columns.add(c);
            }
        }
        
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }
        
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }
    
    private static class ColumnTicket {
        
        public String name;
        public int width;
        public int align;
        public String value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        m_jScrollTableTicket.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollTableTicket.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        m_jScrollTableTicket.setFocusable(false);
        m_jScrollTableTicket.setFont(ChromisFonts.DEFAULTFONTBOLD.deriveFont(22f));

        m_jTicketTable.setFont(ChromisFonts.DEFAULTFONT.deriveFont(12f)
        );
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jTicketTable.setShowVerticalLines(false);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);

        add(m_jScrollTableTicket, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JTable m_jTicketTable;
    // End of variables declaration//GEN-END:variables

}
