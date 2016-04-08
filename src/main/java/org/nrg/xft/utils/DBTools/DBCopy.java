/*
 * org.nrg.xft.utils.DBTools.DBCopy
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 12:24 PM
 */
package org.nrg.xft.utils.DBTools;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Tim
 */
@SuppressWarnings("SqlDialectInspection")
public class DBCopy {
    private static final Logger logger = LoggerFactory.getLogger(DBCopy.class);
    private Properties props = null;

    /**
     *
     */
    public DBCopy(String propsLocation) {
        props = new Properties();
        try {
            InputStream propsIn = new FileInputStream(propsLocation);
            props.load(propsIn);
        } catch (FileNotFoundException e) {
            logger.error("File not found: " + propsLocation);
        } catch (IOException e) {
            logger.error("Error occurred accessing file: " + propsLocation, e);
        }
    }

    public void cleanDestinationDB() {
        logger.info("Clean Destination DB: " + props.getProperty("dest.db.url"));
        try {
            final StringBuilder sb = new StringBuilder();
            final List<String> tableNames = XftStringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
            for (final String table : tableNames) {
                logger.info("Cleaning " + table + "...");
                sb.append("DELETE FROM ").append(table).append(";");
            }
            execDestinationSQL(sb.toString());
        } catch (Exception e) {
            logger.error("Error occurred cleaning the destination database", e);
        }
    }

    public void copyDB() {
        logger.info("Copy Source DB: " + props.getProperty("src.db.url"));
        logger.info("Copy Destination DB: " + props.getProperty("dest.db.url"));
        try {
            File sourceDirinsert = new File(XFTManager.GetInstance().getSourceDir() + "inserts");
            if (!sourceDirinsert.exists()) {
                sourceDirinsert.mkdir();
            }
        } catch (XFTInitException e) {
            logger.error("Error initializing XFT", e);
            throw new RuntimeException(e);
        }
        try {
            Class.forName(props.getProperty("src.db.driver"));
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: " + props.getProperty("src.db.driver"), e);
            throw new RuntimeException(e);
        }
        try {
            Class.forName(props.getProperty("dest.db.driver"));
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: " + props.getProperty("dest.db.driver"), e);
            throw new RuntimeException(e);
        }
        try (final Connection con = DriverManager.getConnection(props.getProperty("src.db.url"), props.getProperty("src.db.user"), props.getProperty("src.db.password"))){
            Statement stmt = con.createStatement();

            final List<String> tableNames = XftStringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
            for (final String table : tableNames) {
                logger.info("Copying " + table + " ...");
                final StringBuilder sb = new StringBuilder();
                final List<String> columns = new ArrayList<>();
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);

                String query = "INSERT INTO " + table + " (";
                int counter = 0;
                for (int i = 1; i <= (rs.getMetaData().getColumnCount()); i++) {
                    String colName = rs.getMetaData().getColumnName(i);
                    if (!colName.equalsIgnoreCase("objectdata")) {
                        if (counter == 0) {
                            query += colName;
                        } else {
                            query += "," + colName;
                        }
                        columns.add(colName);
                        counter++;
                    }
                }
                query += ") VALUES ";

                String coreQuery = query;

                int rowCounter = 0;
                while (rs.next()) {
                    query = coreQuery;
                    if (rowCounter == 0) {
                        query += "(";
                    } else
                        query += "(";

                    for (int i = 0; i < columns.size(); i++) {
                        if (i == 0) {
                            query += getValue(rs.getObject(columns.get(i)));
                        } else {
                            query += "," + getValue(rs.getObject(columns.get(i)));
                        }
                    }
                    query += ")";

                    sb.append(query).append(";\n");

                    if (rowCounter++ == 10000) {
                        FileUtils.OutputToFile(sb.toString(), XFTManager.GetInstance().getSourceDir() + "inserts" + File.separator + table + "_inserts.sql");
                        this.execDestinationSQL(sb.toString());
                        rowCounter = 0;
                    }
                }
                FileUtils.OutputToFile(sb.toString(), XFTManager.GetInstance().getSourceDir() + "inserts" + File.separator + table + "_inserts.sql");
                this.execDestinationSQL(sb.toString());
            }

        } catch (SQLException e) {
            logger.error("SQL error occurred", e);
        } catch (Exception e) {
            logger.error("Unknown exception", e);
        }
    }

    public String getValue(Object o) {
        if (props.getProperty("src.db.type").equalsIgnoreCase("postgresql")) {
            // SOURCE DB IS POSTGRES
            if (props.getProperty("dest.db.type").equalsIgnoreCase("postgresql")) {
                //DESTINATION DB IS POSTGRES
                if (o == null) {
                    return "NULL";
                } else {
                    if (o.getClass().getName().equalsIgnoreCase("java.lang.String")) {
                        String temp = o.toString();
                        if (temp.contains("\\")) {
                            temp = StringUtils.replace(StringUtils.replace(temp, "\\", "*#*"), "*#*", "\\\\");
                        }

                        return "'" + StringUtils.replace(StringUtils.replace(temp, "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'::TIMESTAMP";
                    } else if (o.getClass().getName().equalsIgnoreCase("[B")) {
                        return "'" + StringUtils.replace(StringUtils.replace((new String((byte[]) o)), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Time")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.lang.Float")) {
                        Float value = (Float) o;
                        if (value.isNaN())
                            return "'NaN'";
                        else
                            return o.toString();
                    } else {
                        return o.toString();
                    }
                }
            } else {
                //DESTINATION DB IS MYSQL
                if (o == null) {
                    return "NULL";
                } else {
                    if (o.getClass().getName().equalsIgnoreCase("java.lang.String")) {
                        String temp = o.toString();
                        if (temp.contains("\\")) {
                            temp = StringUtils.replace(StringUtils.replace(temp, "\\", "*#*"), "*#*", "\\\\");
                        }

                        return "'" + StringUtils.replace(StringUtils.replace(temp, "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'::TIMESTAMP";
                    } else if (o.getClass().getName().equalsIgnoreCase("[B")) {
                        return "'" + StringUtils.replace(StringUtils.replace((new String((byte[]) o)), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Time")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.lang.Float")) {
                        Float value = (Float) o;
                        if (value.isNaN())
                            return "'NaN'";
                        else
                            return o.toString();
                    } else {
                        return o.toString();
                    }
                }
            }
        } else {
            // SOURCE DB IS MYSQL
            if (props.getProperty("dest.db.type").equalsIgnoreCase("postgresql")) {
                //DESTINATION DB IS POSTGRES
                if (o == null) {
                    return "NULL";
                } else {
                    if (o.getClass().getName().equalsIgnoreCase("java.lang.String")) {
                        String temp = o.toString();
                        if (temp.contains("\\")) {
                            temp = StringUtils.replace(StringUtils.replace(temp, "\\", "*#*"), "*#*", "\\\\");
                        }

                        return "'" + StringUtils.replace(StringUtils.replace(temp, "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'::TIMESTAMP";
                    } else if (o.getClass().getName().equalsIgnoreCase("[B")) {
                        return "'" + StringUtils.replace(StringUtils.replace((new String((byte[]) o)), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Time")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.lang.Float")) {
                        Float value = (Float) o;
                        if (value.isNaN())
                            return "'NaN'";
                        else
                            return o.toString();
                    } else {
                        return o.toString();
                    }
                }
            } else {
                //DESTINATION DB IS MYSQL
                if (o == null) {
                    return "NULL";
                } else {
                    if (o.getClass().getName().equalsIgnoreCase("java.lang.String")) {
                        String temp = o.toString();
                        if (temp.contains("\\")) {
                            temp = StringUtils.replace(StringUtils.replace(temp, "\\", "*#*"), "*#*", "\\\\");
                        }

                        return "'" + StringUtils.replace(StringUtils.replace(temp, "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'::TIMESTAMP";
                    } else if (o.getClass().getName().equalsIgnoreCase("[B")) {
                        return "'" + StringUtils.replace(StringUtils.replace((new String((byte[]) o)), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Date")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.sql.Time")) {
                        return "'" + StringUtils.replace(StringUtils.replace(o.toString(), "'", "*#*"), "*#*", "''") + "'";
                    } else if (o.getClass().getName().equalsIgnoreCase("java.lang.Float")) {
                        Float value = (Float) o;
                        if (value.isNaN())
                            return "'NaN'";
                        else
                            return o.toString();
                    } else {
                        return o.toString();
                    }
                }
            }
        }
    }

    public boolean execDestinationSQL(String query) {
        try {
            Class.forName(props.getProperty("dest.db.driver"));
        } catch (ClassNotFoundException e) {
            logger.error("Class not found: " + props.getProperty("dest.db.driver"));
            return false;
        }
        try (final Connection con = DriverManager.getConnection(props.getProperty("dest.db.url"), props.getProperty("dest.db.user"), props.getProperty("dest.db.password"))){
            Statement stmt = con.createStatement();
            final boolean success = stmt.execute(query);
            stmt.close();
            return success;
        } catch (SQLException e) {
            logger.error("SQL exception occurred", e);
            return false;
        } catch (Exception e) {
            logger.error("Unknown exception occurred", e);
            return false;
        }
    }

    public boolean validateCopy() {
        logger.info("Validate Source DB: " + props.getProperty("src.db.url"));
        logger.info("Validate Destination DB: " + props.getProperty("dest.db.url"));
        final List<String> tableNames = XftStringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
        for (final String table : tableNames) {
            try {
                if (!DBValidator.CompareTable(this, table, false)) {
                    logger.info("TABLE:" + table + " ERROR: ResultSet mismatch.");
                    return false;
                } else {
                    logger.info("TABLE:" + table + " VALID");
                }
            } catch (Exception e) {
                logger.info("TABLE:" + table + " ERROR: ResultSet mismatch.", e);
                return false;
            }
        }
        return true;
    }


    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Arguments: <Properties File location>");
            return;
        }
        try {
            XFT.init("C:\\xdat\\projects\\cnda", false);
        } catch (ElementNotFoundException e) {
            e.printStackTrace();
        }
        DBCopy db = new DBCopy(args[0]);
        db.cleanDestinationDB();
        db.copyDB();
        db.validateCopy();
        DBAction.AdjustSequences();
        DBAction.InsertMetaDatas();

    }

    /**
     * Gets the database properties.
     * @return Gets the database properties.
     */
    public Properties getProps() {
        return props;
    }

    /**
     * Sets the database properties.
     * @param properties    The database properties to set.
     */
    public void setProps(Properties properties) {
        props = properties;
    }

}

