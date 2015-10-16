/*
 * org.nrg.xft.db.DBConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/28/13 3:19 PM
 */


package org.nrg.xft.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

public class DBConfig {
    private String driver = "";
    private String user = "";
    private String pass = "";
    private String url = "";
    private String type = "";
    private int maxConnections = 10;
    private BasicDataSource dataSource = null;

    private String dbIdentifier = "";

    /**
     * If the BasicDataSource for this DBConfiguration has not been initialized, then it is
     * initialized.  Then, a pooled connection is returned from the BasicDataSource.
     *
     * @return A database connection.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            BasicDataSource ds = new BasicDataSource();
            ds.setDriverClassName(driver);
            ds.setUsername(user);
            ds.setPassword(pass);
            ds.setUrl(url);
            ds.setMaxActive(maxConnections);
            ds.setValidationQuery("SELECT 1;");
            dataSource = ds;
        }
        return dataSource.getConnection();
    }

    public String getDriver() {
        return driver;
    }

    @SuppressWarnings("unused")
    public int getMaxConnections() {
        return maxConnections;
    }

    public String getPass() {
        return pass;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public void setDriver(String string) {
        driver = string;
    }

    public void setMaxConnections(int i) {
        maxConnections = i;
    }

    public void setPass(String string) {
        pass = string;
    }

    public void setUrl(String string) {
        url = string;
    }

    public String getName() {
        return getUrl().substring(getUrl().lastIndexOf("/") + 1);
    }

    public void setUser(String string) {
        user = string;
    }

    public String getDbIdentifier() {
        return dbIdentifier;
    }

    public void setDbIdentifier(String string) {
        dbIdentifier = string;
    }

    public String getType() {
        return type;
    }

    public void setType(String string) {
        type = string;
    }

    public void closeConnections() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void reset() {
        dataSource = null;
    }
}

