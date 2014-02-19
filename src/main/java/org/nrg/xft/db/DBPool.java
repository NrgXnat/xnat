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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.nrg.xft.exception.DBPoolException;
public class DBPool {
	private static DBPool pool = null;
	private Hashtable<String,DBConfig> ds = new Hashtable<String,DBConfig>();

	/**
	 * Gets a pooled database connection from the available DBConfigs with a
	 * matching db indentifier.
	 * @param db
	 * @return
	 * @throws DBPoolException
	 * @throws SQLException
	 */
	private Connection getConnection(String db) throws DBPoolException, SQLException
	{
	    DBConfig dbc = (DBConfig)ds.get(db);
	    if (dbc == null)
	    {
	        throw new DBPoolException();
	    }
		return dbc.getConnection();
	}
	
	public void resetConnections(){
		for(DBConfig dbc: ds.values()){
			dbc.reset();
		}
	}

	private void addDBConfig(DBConfig db)
	{
		ds.put(db.getDbIdentifier(),db);
	}

	public Collection<DBConfig> getDBConfigs()
	{
		return ds.values();
	}

	/**
	 * If the DBPool object has not been instanciated then it is instanciated and returned.
	 * @return
	 */
	public static DBPool GetPool()
	{
		if (pool == null)
		{
			pool = new DBPool();
		}
		return pool;
	}

	/**
	 * Gets a pooled database connection from the available DBConfigs with a
	 * matching db indentifier.
	 * @param db
	 * @return
	 * @throws SQLException
	 * @throws DBPoolException
	 */
	public static Connection GetConnection(String db) throws SQLException,DBPoolException
	{
		return GetPool().getConnection(db);
	}

	/**
	 * Add DBConfig to the available configs.
	 * @param db
	 */
	public static void AddDBConfig(DBConfig db)
	{
		GetPool().addDBConfig(db);
	}

	/**
	 * Gets the DBConfig with the matching db identifier.
	 * @param dbID
	 * @return
	 */
	public static DBConfig GetDBConfig(String dbID)
	{
		return (DBConfig)GetPool().getDS().get(dbID);
	}
	/**
	 * @return
	 */
	public Hashtable getDS() {
		return ds;
	}

	public void closeConnections() throws SQLException
	{
	    Iterator iter = ds.values().iterator();

	    while (iter.hasNext())
	    {
	        DBConfig config = (DBConfig) iter.next();
	        config.closeConnections();
	    }
	}

	public static String GetDBUserName(String dbName)
	{
	    DBConfig config =DBPool.GetDBConfig(dbName);
	    if (config !=null)
	        return config.getUser();
	    else
	        return "";
	}
}

