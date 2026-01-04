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
package uk.chromis.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import uk.chromis.pos.forms.AppConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 *
 * @author John Lewis
 */
public class ConnectionPoolFactory {

    //  private final static ComboPooledDataSource comboPooledDataSource = new ComboPooledDataSource();
    public static ComboPooledDataSource comboPooledDataSource;

    static {
        comboPooledDataSource = new ComboPooledDataSource();
        
        comboPooledDataSource.setJdbcUrl(AppConfig.getDatabaseURL());
        comboPooledDataSource.setUser(AppConfig.getDatabaseUser());
        // Accept plain or encrypted password
        comboPooledDataSource.setPassword(AppConfig.getClearDatabasePassword());
        comboPooledDataSource.setMinPoolSize(2);
        comboPooledDataSource.setAcquireIncrement(2);
        comboPooledDataSource.setMaxPoolSize(10);
        comboPooledDataSource.setMaxIdleTime(1);
        comboPooledDataSource.setMaxStatements(180); //set PreaperedStatementPooling
       // comboPooledDataSource.setMaxConnectionAge(5);
        comboPooledDataSource.setAutomaticTestTable("c3p0testing");
    }

    private ConnectionPoolFactory() {

    }

    public static int getMaxConnections() {
        try {
            return comboPooledDataSource.getNumConnectionsDefaultUser();
        } catch (SQLException ex) {
            return 0;
        }
    }

    public static int getBusyConnections() {
        try {
            return comboPooledDataSource.getNumBusyConnectionsDefaultUser();
        } catch (SQLException ex) {
            return 0;
        }
    }

    public static int getIdleConnections() {
        try {
            return comboPooledDataSource.getNumIdleConnectionsDefaultUser();
        } catch (SQLException ex) {
            return 0;
        }
    }

    public void updateMaxConnections(int max) {
        comboPooledDataSource.setMaxPoolSize(max);
    }

    public static Connection getConnection() {
        try {
            System.err.println("num_connections: " + comboPooledDataSource.getNumConnectionsDefaultUser());
            System.err.println("num_busy_connections: " + comboPooledDataSource.getNumBusyConnectionsDefaultUser());
            System.err.println("num_idle_connections: " + comboPooledDataSource.getNumIdleConnectionsDefaultUser());
            System.err.println();
            return comboPooledDataSource.getConnection();
        } catch (SQLException ex) {
            System.out.println(ex);
        }
        return null;
    }

}
