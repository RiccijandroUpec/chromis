/*
**    Chromis Administration  - Open Source Point of Sale
**
**    This file is part of Chromis Administration Version Chromis V1.5.0
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
package uk.chromis.pos.repair;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.chromis.basic.BasicException;
import uk.chromis.connectionpool.ConnectionPoolFactory;
import uk.chromis.data.loader.Datas;
import uk.chromis.data.loader.PreparedSentence;
import uk.chromis.data.loader.SerializerReadInteger;
import uk.chromis.data.loader.SerializerWriteBasicExt;
import uk.chromis.data.loader.Session;
import uk.chromis.data.loader.SessionFactory;
import uk.chromis.data.loader.StaticSentence;
import uk.chromis.pos.forms.LocalResource;

/**
 *
 * @author John
 */
public class DatabaseRepair {

    private static final Session s = SessionFactory.getSession();

    /**
     * Creates a new instance of DatabaseRepair this will be removed in a later
     * version only required to allow report fix
     */
    public DatabaseRepair() {
    }

    public static void repairPayments() {

        try {

            Object m_result = new StaticSentence(s,
                    "select count(*) from payments where description is null or description =''",
                    null, SerializerReadInteger.INSTANCE).find();
            if ((Integer) m_result == 0) {
                return;
            }

            new PreparedSentence(s, "drop trigger if exists update_payments;", null).exec();

            Connection connection;
            Statement statement = null; 
            ResultSet resultSet = null;
            try {
                connection = ConnectionPoolFactory.getConnection();
                statement = connection.createStatement();
                resultSet = statement.executeQuery("select * from payments");
                while (resultSet.next()) {
                    new PreparedSentence(s, "update payments set description = ? where id = ? ",
                            new SerializerWriteBasicExt(new Datas[]{
                        Datas.OBJECT, Datas.STRING,
                        Datas.OBJECT, Datas.STRING}, new int[]{0, 1})).exec(LocalResource.getString("paymentdescription." + resultSet.getString("payment")), resultSet.getString("id"));
                }
            } catch (SQLException ex) {

            } finally {
                try {
                    resultSet.close();
                    statement.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            String sql = " create definer = current_user "
                    + " trigger update_payments before update on payments "
                    + " for each row"
                    + " begin "
                    + " SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'UPDATE cancelled payments';"
                    + " end; ";

            new PreparedSentence(s, sql, null).exec();

        } catch (BasicException ex) {
            Logger.getLogger(DatabaseRepair.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void repairSiteGuid() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ConnectionPoolFactory.getConnection();
            stmt = conn.createStatement();
            
            // 1. Create siteguid table if missing
            try {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS siteguid (" +
                    "  guid VARCHAR(36) NOT NULL," +
                    "  PRIMARY KEY (guid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
                );
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error creating siteguid table", ex);
            }

            // 2. Ensure siteguid has at least one record
            String siteGuid = null;
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT guid FROM siteguid LIMIT 1");
                if (rs.next()) {
                    siteGuid = rs.getString("guid");
                }
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error reading siteguid", ex);
            } finally {
                if (rs != null) {
                    try { rs.close(); } catch (SQLException e) {}
                }
            }

            if (siteGuid == null) {
                siteGuid = java.util.UUID.randomUUID().toString();
                try {
                    stmt.execute("INSERT INTO siteguid (guid) VALUES ('" + siteGuid + "')");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error inserting siteguid", ex);
                }
            }

            // 3. Ensure people table has siteguid and iswaiter columns
            try {
                stmt.execute("ALTER TABLE people ADD COLUMN siteguid VARCHAR(36) NULL");
            } catch (SQLException ex) {
                // Column probably already exists, ignore
            }
            try {
                stmt.execute("ALTER TABLE people ADD COLUMN iswaiter BOOLEAN DEFAULT FALSE");
            } catch (SQLException ex) {
                // Column probably already exists, ignore
            }

            // 4. Update people siteguid if null or empty
            if (siteGuid != null) {
                try {
                    stmt.execute("UPDATE people SET siteguid = '" + siteGuid + "' WHERE siteguid IS NULL OR siteguid = ''");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error updating people siteguid", ex);
                }
            }

            // 5. Ensure resources table has siteguid column
            try {
                stmt.execute("ALTER TABLE resources ADD COLUMN siteguid VARCHAR(36) NULL");
            } catch (SQLException ex) {
                // Column probably already exists, ignore
            }
            if (siteGuid != null) {
                try {
                    stmt.execute("UPDATE resources SET siteguid = '" + siteGuid + "' WHERE siteguid IS NULL OR siteguid = ''");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error updating resources siteguid", ex);
                }
            }

            // 6. Ensure roles table exists and has siteguid column
            try {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS roles (" +
                    "  id VARCHAR(255) NOT NULL," +
                    "  name VARCHAR(255) NOT NULL," +
                    "  permissions MEDIUMBLOB," +
                    "  siteguid VARCHAR(36) NULL," +
                    "  PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
                );
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error creating roles table", ex);
            }
            try {
                stmt.execute("ALTER TABLE roles ADD COLUMN siteguid VARCHAR(36) NULL");
            } catch (SQLException ex) {
                // Column probably already exists, ignore
            }
            if (siteGuid != null) {
                try {
                    stmt.execute("UPDATE roles SET siteguid = '" + siteGuid + "' WHERE siteguid IS NULL OR siteguid = ''");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error updating roles siteguid", ex);
                }
            }

            // 7. Ensure terminals table exists and has all columns
            try {
                stmt.execute(
                    "CREATE TABLE IF NOT EXISTS terminals (" +
                    "  id VARCHAR(255) NOT NULL," +
                    "  terminal_name VARCHAR(255) NOT NULL," +
                    "  terminal_key VARCHAR(255) NOT NULL," +
                    "  PRIMARY KEY (id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
                );
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error creating terminals table", ex);
            }
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN terminal_location VARCHAR(255) NULL"); } catch (SQLException ex) {}
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN active_cash VARCHAR(255) NULL"); } catch (SQLException ex) {}
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN appversion VARCHAR(255) NULL"); } catch (SQLException ex) {}
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN active_session BOOLEAN DEFAULT FALSE"); } catch (SQLException ex) {}
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN active_user VARCHAR(255) NULL"); } catch (SQLException ex) {}
            try { stmt.execute("ALTER TABLE terminals ADD COLUMN siteguid VARCHAR(36) NULL"); } catch (SQLException ex) {}
            if (siteGuid != null) {
                try {
                    stmt.execute("UPDATE terminals SET siteguid = '" + siteGuid + "' WHERE siteguid IS NULL OR siteguid = ''");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error updating terminals siteguid", ex);
                }
            }

            // 8. Ensure closedcash table has siteguid column
            try {
                stmt.execute("ALTER TABLE closedcash ADD COLUMN siteguid VARCHAR(36) NULL");
            } catch (SQLException ex) {
                // Column probably already exists, ignore
            }
            if (siteGuid != null) {
                try {
                    stmt.execute("UPDATE closedcash SET siteguid = '" + siteGuid + "' WHERE siteguid IS NULL OR siteguid = ''");
                } catch (SQLException ex) {
                    Logger.getLogger(DatabaseRepair.class.getName()).log(Level.WARNING, "Error updating closedcash siteguid", ex);
                }
            }

            // 9. Ensure ticketlines table has all required columns
            String[] ticketlinesCols = {
                "ALTER TABLE ticketlines ADD COLUMN linetype VARCHAR(255) NULL",
                "ALTER TABLE ticketlines ADD COLUMN soldprice DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN soldpriceexc DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN priceinc DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN priceexc DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN buyprice DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN refundqty DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN taxinclusive BOOLEAN DEFAULT TRUE",
                "ALTER TABLE ticketlines ADD COLUMN taxrate DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN taxamount DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN commission DOUBLE DEFAULT 0.0",
                "ALTER TABLE ticketlines ADD COLUMN cardid VARCHAR(255) NULL",
                "ALTER TABLE ticketlines ADD COLUMN discounted BOOLEAN DEFAULT FALSE"
            };
            for (String sql : ticketlinesCols) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }
            }

            // 10. Ensure tickets table has all required columns
            String[] ticketsCols = {
                "ALTER TABLE tickets ADD COLUMN waiter VARCHAR(255) NULL",
                "ALTER TABLE tickets ADD COLUMN terminal VARCHAR(255) NULL",
                "ALTER TABLE tickets ADD COLUMN taxinclusive BOOLEAN DEFAULT TRUE",
                "ALTER TABLE tickets ADD COLUMN ecardnumber VARCHAR(255) NULL",
                "ALTER TABLE tickets ADD COLUMN ecardbalance INT DEFAULT 0",
                "ALTER TABLE tickets ADD COLUMN earnpoints INT DEFAULT 0",
                "ALTER TABLE tickets ADD COLUMN burnpoints INT DEFAULT 0",
                "ALTER TABLE tickets ADD COLUMN currentdebt DOUBLE DEFAULT 0.0",
                "ALTER TABLE tickets ADD COLUMN location VARCHAR(255) NULL",
                "ALTER TABLE tickets ADD COLUMN ticketdiscount DOUBLE DEFAULT 0.0",
                "ALTER TABLE tickets ADD COLUMN cardfees DOUBLE DEFAULT 0.0",
                "ALTER TABLE tickets ADD COLUMN tlvcode VARCHAR(1000) NULL",
                "ALTER TABLE tickets ADD COLUMN ticketowner VARCHAR(255) NULL",
                "ALTER TABLE tickets ADD COLUMN tabledetails VARCHAR(1000) NULL",
                "ALTER TABLE tickets ADD COLUMN pickupid INT DEFAULT 0"
            };
            for (String sql : ticketsCols) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }
            }

            // 11. Ensure receipts table has all required columns
            String[] receiptsCols = {
                "ALTER TABLE receipts ADD COLUMN datenew DATETIME NULL",
                "ALTER TABLE receipts ADD COLUMN attributes MEDIUMBLOB NULL",
                "ALTER TABLE receipts ADD COLUMN person VARCHAR(36) NULL"
            };
            for (String sql : receiptsCols) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }
            }

            // 12. Ensure payments table has all required columns
            String[] paymentsCols = {
                "ALTER TABLE payments ADD COLUMN description VARCHAR(255) NULL",
                "ALTER TABLE payments ADD COLUMN transid VARCHAR(255) NULL",
                "ALTER TABLE payments ADD COLUMN returnmsg MEDIUMBLOB NULL",
                "ALTER TABLE payments ADD COLUMN tendered DOUBLE DEFAULT 0.0",
                "ALTER TABLE payments ADD COLUMN cardname VARCHAR(255) NULL",
                "ALTER TABLE payments ADD COLUMN ecardnumber VARCHAR(255) NULL",
                "ALTER TABLE payments ADD COLUMN ecardbalance DOUBLE DEFAULT 0.0"
            };
            for (String sql : paymentsCols) {
                try {
                    stmt.execute(sql);
                } catch (SQLException ex) {
                    // Column already exists, ignore
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseRepair.class.getName()).log(Level.SEVERE, "Database connection error in repairSiteGuid", ex);
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) {}
            }
        }
    }

}

