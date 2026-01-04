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

import java.awt.Dimension;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import uk.chromis.commons.dialogs.JAlertPane;
import uk.chromis.commons.utils.TerminalInfo;
import uk.chromis.data.loader.ConnectionFactory;
import uk.chromis.pos.forms.AppConfig;
import uk.chromis.pos.forms.AppLocal;

/**
 *
 *
 */
public class DbUtils {

    private static final Connection connection = ConnectionFactory.getInstance().getConnection();
    private static int rowCount = 0;

    public static String getTerminalName() {
        String terminal = TerminalInfo.getTerminalName();
        if (terminal.equalsIgnoreCase("Unknown")) {
            try {
                terminal = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                terminal = "POS-" + TerminalInfo.getTerminalID().substring(0, Math.min(TerminalInfo.getTerminalID().length(), 8));
            }
            TerminalInfo.setTerminalName(terminal);
        }

        try {
            PreparedStatement pstmt = connection.prepareStatement("select count(*) from terminals where terminal_key = ? ");
            pstmt.setString(1, TerminalInfo.getTerminalID());
            ResultSet rsTables = pstmt.executeQuery();
            if (rsTables.next()) {
                if (rsTables.getInt(1) == 0) {
                    pstmt = connection.prepareStatement("insert into terminals (id, terminal_name, terminal_key, terminal_location) values (?, ?, ?, ?)");
                    pstmt.setString(1, TerminalInfo.getTerminalName());
                    pstmt.setString(2, TerminalInfo.getTerminalName());
                    pstmt.setString(3, TerminalInfo.getTerminalID());
                    pstmt.setString(4, TerminalInfo.getLocation());
                    pstmt.executeUpdate();
                } else {
                    pstmt = connection.prepareStatement("update terminals set id = ?, terminal_name = ?, terminal_location = ? where  terminal_key = ?");
                    pstmt.setString(1, TerminalInfo.getTerminalName());
                    pstmt.setString(2, TerminalInfo.getTerminalName());
                    pstmt.setString(3, TerminalInfo.getLocation());
                    pstmt.setString(4, TerminalInfo.getTerminalID());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException ex) {

        }
        AppConfig.put("terminalID", TerminalInfo.getTerminalName());
        return AppConfig.getString("terminalID");
    }

    public static Integer getTriggerCount() {
        try {
            String sql = "select count(*) from INFORMATION_SCHEMA.TRIGGERS where trigger_schema = DATABASE() AND trigger_name NOT IN ('giftcard_insert','gift_trans_insert','loyalty_insert','loyalty_trans_insert' )";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rowCount = rs.getInt(1);
            }
            return rowCount;
        } catch (SQLException ex) {
        }
        return 0;
    }

    public static Integer getViewCount() {
        try {
            String sql = "select count(*) from INFORMATION_SCHEMA.VIEWS where TABLE_SCHEMA = DATABASE()  and TABLE_NAME = 'recipes'";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                rowCount = rs.getInt(1);
            }
            return rowCount;
        } catch (SQLException ex) {
        }
        return 0;
    }
}
