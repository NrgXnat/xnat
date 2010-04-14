//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 28, 2004
 */
package org.nrg.xft.db;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;
/**
 * Class used to store information about one of the databases used in the application and used to
 * create a connection from the connection pool DataSource.
 *
 * <BR><BR>The org.apache.commons.dbcp.BasicDataSource is used to manage the connection pool.
 * @author Tim
 */
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
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException
	{
		if (dataSource == null)
		{
			BasicDataSource ds = new BasicDataSource();
			ds.setDriverClassName(driver);
			ds.setUsername(user);
			ds.setPassword(pass);
			ds.setUrl(url);
			ds.setMaxActive(maxConnections);
			dataSource = ds;
		}
		return dataSource.getConnection();
	}

	/**
	 * @return
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @return
	 */
	public int getMaxConnections() {
		return maxConnections;
	}

	/**
	 * @return
	 */
	public String getPass() {
		return pass;
	}

	/**
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param string
	 */
	public void setDriver(String string) {
		driver = string;
	}

	/**
	 * @param i
	 */
	public void setMaxConnections(int i) {
		maxConnections = i;
	}

	/**
	 * @param string
	 */
	public void setPass(String string) {
		pass = string;
	}

	/**
	 * @param string
	 */
	public void setUrl(String string) {
		url = string;
	}

	public String getName()
	{
		return getUrl().substring(getUrl().lastIndexOf("/")+1);
	}

	/**
	 * @param string
	 */
	public void setUser(String string) {
		user = string;
	}

	/**
	 * @return
	 */
	public String getDbIdentifier() {
		return dbIdentifier;
	}

	/**
	 * @param string
	 */
	public void setDbIdentifier(String string) {
		dbIdentifier = string;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

	public void closeConnections() throws SQLException
	{
	    if (dataSource != null)
		{
			dataSource.close();
		}
	}

	public void reset(){
		dataSource=null;
	}
}

