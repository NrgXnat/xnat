//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jul 26, 2004
 */
package org.nrg.xft.utils.DBTools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 */
public class DBCopy {
	static org.apache.log4j.Logger logger = Logger.getLogger(DBCopy.class);
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
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void cleanDestinationDB()
	{
		logger.info("Clean Destination DB: " + props.getProperty("dest.db.url"));
		try {
			StringBuffer sb = new StringBuffer();
			ArrayList tableNames = StringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
			Iterator iter = tableNames.iterator();
			while (iter.hasNext())
			{
				String table = iter.next().toString();
				logger.info("Cleaning " + table + "...");
				sb.append("DELETE FROM " + table + ";");
			}
			this.execDestinationSQL(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void copyDB()
	{
		logger.info("Copy Source DB: " + props.getProperty("src.db.url"));
		logger.info("Copy Destination DB: " + props.getProperty("dest.db.url"));
		Connection con = null;
		try {
			File sourceDirinsert = new File(XFTManager.GetInstance().getSourceDir()+ "inserts");
			if (! sourceDirinsert.exists())
			{
				sourceDirinsert.mkdir();
			}
			StringBuffer sb = new StringBuffer();
			Class.forName(props.getProperty("src.db.driver"));
			Class.forName(props.getProperty("dest.db.driver"));
			con = DriverManager.getConnection(props.getProperty("src.db.url"),props.getProperty("src.db.user"),props.getProperty("src.db.password"));
			Statement stmt = con.createStatement();
			
			ArrayList tableNames = StringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
			Iterator iter = tableNames.iterator();
			while (iter.hasNext())
			{
				String table = (String)iter.next();
logger.info("Copying " + table + " ...");
				ArrayList columns = new ArrayList();
				ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
				
				String query = "INSERT INTO "+ table +" (";
				int counter = 0;
				for (int i=1;i<=(rs.getMetaData().getColumnCount());i++)
				{
					String colName = rs.getMetaData().getColumnName(i);
					if (!colName.equalsIgnoreCase("objectdata"))
					{
						if (counter == 0)
						{
							query += colName;
						}else
						{
							query += "," + colName;
						}
						columns.add(colName);
						counter++;
					}
				}
				query +=") VALUES ";
				
				String coreQuery = query.toString();
				
				int rowCounter = 0;
				while (rs.next())
				{
					query = coreQuery.toString();
					if (rowCounter == 0)
					{
						query += "(";
					}
					else
						query += "(";
					
					for (int i=0;i<columns.size();i++)
					{
						if (i==0)
						{
							query += getValue(rs.getObject(columns.get(i).toString()));
						}else
						{
							query += "," + getValue(rs.getObject(columns.get(i).toString()));
						}
					}
					query += ")";

					sb.append(query).append(";\n");
					
					if (rowCounter++ == 10000)
					{
						FileUtils.OutputToFile(sb.toString(),XFTManager.GetInstance().getSourceDir()+ "inserts" + File.separator + table +"_inserts.sql");
						this.execDestinationSQL(sb.toString());
						sb = new StringBuffer();
						rowCounter=0;
					}
				}
				FileUtils.OutputToFile(sb.toString(),XFTManager.GetInstance().getSourceDir()+ "inserts" + File.separator + table +"_inserts.sql");
				this.execDestinationSQL(sb.toString());
				sb = new StringBuffer();
			}
			
		} catch (ClassNotFoundException e) {
			logger.error("",e);
		} catch (SQLException e) {
			logger.error("",e);
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("",e);
		} catch (Exception e) {
			logger.error("",e);
		}finally
		{
			try {
				if(con!=null)con.close();
			} catch (SQLException e1) {
				logger.error("",e1);
			}
		}
	}
	
	public String getValue(Object o)
	{
		if (props.getProperty("src.db.type").equalsIgnoreCase("postgresql"))
		{
			// SOURCE DB IS POSTGRES
			if (props.getProperty("dest.db.type").equalsIgnoreCase("postgresql"))
			{
				//DESTINATION DB IS POSTGRES
				if (o == null)
				{
					return "NULL";
				}else
				{
					if (o.getClass().getName().equalsIgnoreCase("java.lang.String"))
					{
						String temp = o.toString();
						if (temp.indexOf("\\") != -1)
						{
							temp =StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"\\","*#*"),"*#*","\\\\");
						}
						
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'::TIMESTAMP";
					}else if(o.getClass().getName().equalsIgnoreCase("[B"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr((new String((byte[])o)),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Time"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.lang.Float"))
					{
						Float value = (Float)o;
						if (value.isNaN())
							return "'NaN'";
						else
							return o.toString();
					}
					else
					{
						return o.toString();
					}
				}
			}else
			{
				//DESTINATION DB IS MYSQL
				if (o == null)
				{
					return "NULL";
				}else
				{
					if (o.getClass().getName().equalsIgnoreCase("java.lang.String"))
					{
						String temp = o.toString();
						if (temp.indexOf("\\") != -1)
						{
							temp =StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"\\","*#*"),"*#*","\\\\");
						}
						
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'::TIMESTAMP";
					}else if(o.getClass().getName().equalsIgnoreCase("[B"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr((new String((byte[])o)),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Time"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.lang.Float"))
					{
						Float value = (Float)o;
						if (value.isNaN())
							return "'NaN'";
						else
							return o.toString();
					}
					else
					{
						return o.toString();
					}
				}
			}
		}else
		{
			// SOURCE DB IS MYSQL
			if (props.getProperty("dest.db.type").equalsIgnoreCase("postgresql"))
			{
				//DESTINATION DB IS POSTGRES
				if (o == null)
				{
					return "NULL";
				}else
				{
					if (o.getClass().getName().equalsIgnoreCase("java.lang.String"))
					{
						String temp = o.toString();
						if (temp.indexOf("\\") != -1)
						{
							temp =StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"\\","*#*"),"*#*","\\\\");
						}
						
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'::TIMESTAMP";
					}else if(o.getClass().getName().equalsIgnoreCase("[B"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr((new String((byte[])o)),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Time"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.lang.Float"))
					{
						Float value = (Float)o;
						if (value.isNaN())
							return "'NaN'";
						else
							return o.toString();
					}else
					{
						return o.toString();
					}
				}
			}else
			{
				//DESTINATION DB IS MYSQL
				if (o == null)
				{
					return "NULL";
				}else
				{
					if (o.getClass().getName().equalsIgnoreCase("java.lang.String"))
					{
						String temp = o.toString();
						if (temp.indexOf("\\") != -1)
						{
							temp =StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"\\","*#*"),"*#*","\\\\");
						}
						
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(temp,"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'::TIMESTAMP";
					}else if(o.getClass().getName().equalsIgnoreCase("[B"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr((new String((byte[])o)),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Date"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.sql.Time"))
					{
						return "'" + StringUtils.ReplaceStr(StringUtils.ReplaceStr(o.toString(),"'","*#*"),"*#*","''") + "'";
					}else if(o.getClass().getName().equalsIgnoreCase("java.lang.Float"))
					{
						Float value = (Float)o;
						if (value.isNaN())
							return "'NaN'";
						else
							return o.toString();
					}
					else
					{
						return o.toString();
					}
				}
			}
		}
	}
	
	public boolean execDestinationSQL(String query)
	{
		boolean success = false;
		Connection con = null;
		try {
			Class.forName(props.getProperty("dest.db.driver"));
			con = DriverManager.getConnection(props.getProperty("dest.db.url"),props.getProperty("dest.db.user"),props.getProperty("dest.db.password"));
			Statement stmt = con.createStatement();
			success = stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally
		{
			try {
				con.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return success;
	}
	
	public boolean validateCopy()
	{
		boolean valid = true;
		logger.info("Validate Source DB: " + props.getProperty("src.db.url"));
		logger.info("Validate Destination DB: " + props.getProperty("dest.db.url"));
		ArrayList tableNames = StringUtils.CommaDelimitedStringToArrayList(props.getProperty("tableNames"));
		Iterator iter = tableNames.iterator();
		while (iter.hasNext())
		{
			String table = (String)iter.next();
			try {
				if (!DBValidator.CompareTable(this,table,false))
				{
					logger.info("TABLE:" + table + " ERROR: ResultSet mismatch.");
					return false;
				}else
				{
					logger.info("TABLE:" + table + " VALID");
				}
			} catch (Exception e) {
				logger.info("TABLE:" + table + " ERROR: ResultSet mismatch.");
				e.printStackTrace();
			}
		}
		return valid;
	}
	
	

	public static void main(String args[]) {
		if (args.length != 1){
			System.out.println("Arguments: <Properties File location>");
			return;
		}
		try {
			XFT.init("C:\\xdat\\projects\\cnda");
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
	 * @return
	 */
	public Properties getProps() {
		return props;
	}

	/**
	 * @param properties
	 */
	public void setProps(Properties properties) {
		props = properties;
	}

}

