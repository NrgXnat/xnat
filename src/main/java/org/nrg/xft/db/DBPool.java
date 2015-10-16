/*
 * org.nrg.xft.db.DBPool
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/28/13 3:19 PM
 */


package org.nrg.xft.db;

import org.nrg.xft.exception.DBPoolException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;

/**
 * This class is deprecated. Move to using abstracted database pool in Spring configuration.
 */
@Deprecated
public class DBPool {
	private static DBPool pool = null;
    private Hashtable<String, DBConfig> ds = new Hashtable<>();

	/**
	 * Gets a pooled database connection from the available DBConfigs with a
     * matching db identifier.
     *
     * @param db The database to which to connect.
     * @return A connection to the specified database.
	 * @throws DBPoolException
	 * @throws SQLException
	 */
    private Connection getConnection(String db) throws DBPoolException, SQLException {
        DBConfig dbc = ds.get(db);
        if (dbc == null) {
	        throw new DBPoolException();
	    }
		return dbc.getConnection();
	}

    public void resetConnections() {
        for (DBConfig dbc : ds.values()) {
			dbc.reset();
		}
	}

    private void addDBConfig(DBConfig db) {
        ds.put(db.getDbIdentifier(), db);
	}

    public Collection<DBConfig> getDBConfigs() {
		return ds.values();
	}

	/**
     * If the DBPool object has not been instantiated then it is instantiated and returned.
     *
     * @return The database pool object.
	 */
    public static DBPool GetPool() {
        if (pool == null) {
			pool = new DBPool();
		}
		return pool;
	}

	/**
	 * Gets a pooled database connection from the available DBConfigs with a
     * matching db identifier.
     *
     * @param db The database to which to connect.
     * @return A connection to the specified database.
	 * @throws SQLException
	 * @throws DBPoolException
	 */
    public static Connection GetConnection(String db) throws SQLException, DBPoolException {
		return GetPool().getConnection(db);
	}

	/**
	 * Add DBConfig to the available configs.
     *
     * @param db The database configuration to add to the pool.
	 */
    public static void AddDBConfig(DBConfig db) {
		GetPool().addDBConfig(db);
	}

	/**
	 * Gets the DBConfig with the matching db identifier.
     *
     * @param dbID The ID of the database configuration to retrieve from the pool.
     * @return The specified database configuration.
	 */
    public static DBConfig GetDBConfig(String dbID) {
        return (DBConfig) GetPool().getDS().get(dbID);
	}

	/**
     * Gets the database configuration storage map.
     *
     * @return The database configuration storage map.
	 */
	public Hashtable getDS() {
		return ds;
	}

    public void closeConnections() throws SQLException {
        for (final DBConfig config : ds.values()) {
	        config.closeConnections();
	    }
	}

    public static String GetDBUserName(String dbName) {
        DBConfig config = DBPool.GetDBConfig(dbName);
        if (config != null)
	        return config.getUser();
	    else
	        return "";
	}
}

