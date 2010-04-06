//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jul 28, 2004
 */
package org.nrg.xft.utils.DBTools;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.db.PoolDBUtils;

/**
 * @author Tim
 */
public class DBValidator {
	public static boolean CompareTable(DBCopy dbProps,String tableName,boolean withMeta) throws Exception
	{
		boolean match = true;
		
		Connection srcCon = null;
		Connection destCon = null;
		ResultSet srcRS = null;
		ResultSet destRS = null;
		Statement srcStmt = null;
		Statement destStmt = null;
		ResultSet rs = null;
		try {
			Class.forName(dbProps.getProps().getProperty("src.db.driver"));
			Class.forName(dbProps.getProps().getProperty("dest.db.driver"));
			
			srcCon = DriverManager.getConnection(dbProps.getProps().getProperty("src.db.url"),dbProps.getProps().getProperty("src.db.user"),dbProps.getProps().getProperty("src.db.password"));
			srcStmt = srcCon.createStatement();
						
			destCon = DriverManager.getConnection(dbProps.getProps().getProperty("dest.db.url"),dbProps.getProps().getProperty("dest.db.user"),dbProps.getProps().getProperty("dest.db.password"));
			destStmt = destCon.createStatement();
			
			rs = destStmt.executeQuery("SELECT * FROM " + tableName + " LIMIT 1;");
			
			String orderByCols = "";
			ArrayList columns = new ArrayList();
			
			for(int i=1;i<=rs.getMetaData().getColumnCount();i++)
			{
				String colName = rs.getMetaData().getColumnName(i);
				if ((!colName.equalsIgnoreCase("objectdata")) && (withMeta || (!colName.endsWith("_info"))))
					columns.add(colName);
				
				if (colName.indexOf("id") != -1 && (withMeta || (!colName.endsWith("_info"))))
				{
					if (orderByCols.equalsIgnoreCase(""))
					{
						orderByCols = " " + colName + " ASC";
					}else
					{
						orderByCols += ", " + colName + " ASC";
					}
				}
			}
			
			if (orderByCols.equalsIgnoreCase(""))
			{
				for (int j=0;j<columns.size();j++)
				{
					if (orderByCols.equalsIgnoreCase(""))
					{
						orderByCols = " " + columns.get(j) + " ASC";
					}else
					{
						orderByCols += ", " + columns.get(j) + " ASC";
					}
				}
			}
			
			String cols = "";
			Iterator iter = columns.iterator();
			while(iter.hasNext())
			{
				String col = (String)iter.next();
				if (cols.equalsIgnoreCase(""))
				{
					cols = " " + col;
				}else
				{
					cols += ", " + col;
				}
			}
			
			srcRS = srcStmt.executeQuery("SELECT " + cols + " FROM " + tableName + " ORDER BY " + orderByCols);
			destRS = destStmt.executeQuery("SELECT " + cols + " FROM " + tableName + " ORDER BY " + orderByCols);
			
			if (PoolDBUtils.GetResultSetSize(srcRS) != PoolDBUtils.GetResultSetSize(destRS))
			{
				throw new Exception(tableName +" size mis-match. SRC:" + PoolDBUtils.GetResultSetSize(srcRS) + " DEST:" + PoolDBUtils.GetResultSetSize(destRS));
			}else
			{
				srcRS.beforeFirst();
				destRS.beforeFirst();
				
				while (srcRS.next())
				{
					destRS.next();
					for(int k=1;k<=srcRS.getMetaData().getColumnCount();k++)
					{
						CompareFields(srcRS.getObject(k),destRS.getObject(k),tableName,srcRS.getMetaData().getColumnName(k));
					}
				}
			}
			
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
			try {
				srcRS.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				destRS.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			try {
				rs.close();
			} catch (SQLException e3) {
				e3.printStackTrace();
			}
			try {
				srcStmt.close();
			} catch (SQLException e4) {
				e4.printStackTrace();
			}
			try {
				destStmt.close();
			} catch (SQLException e5) {
				e5.printStackTrace();
			}
			try {
				srcCon.close();
			} catch (SQLException e6) {
				e6.printStackTrace();
			}
			try {
				destCon.close();
			} catch (SQLException e7) {
				e7.printStackTrace();
			}
		}
		
		return match;
	}

    @SuppressWarnings("deprecation")
	public static boolean CompareFields(Object src, Object dest,String tableName, String colName) throws Exception
	{
		if (src == null && dest == null)
		{
	
		}else if (src == null || dest == null){
			throw new Exception("TABLE:" + tableName + " FIELD:" + colName + " VALUE (MIS-MATCH):" + src + "=" + dest);
		}else
		{			
			if (src.getClass().getName().equalsIgnoreCase("java.sql.Timestamp"))
			{
				java.sql.Timestamp srcTime = (java.sql.Timestamp)src;
				java.sql.Timestamp destTime = (java.sql.Timestamp)dest;
				
				String srcField = srcTime.getMonth()+":" + srcTime.getDate()+":" + srcTime.getYear()+":" + srcTime.getHours()+":" + srcTime.getMinutes()+":" + srcTime.getSeconds();
				String destField = destTime.getMonth()+":" + destTime.getDate()+":" + destTime.getYear()+":" + destTime.getHours()+":" + destTime.getMinutes()+":" + destTime.getSeconds();

				if (srcField.equalsIgnoreCase("10:30:-1898:0:0:0") && destField.equalsIgnoreCase("1:11:130:0:0:0"))
				{
					
				}else if (!srcField.equalsIgnoreCase(destField))
				{
					throw new Exception("TABLE:" + tableName + " FIELD:" + colName + " VALUE (MIS-MATCH):" + srcField + "=" + destField);
				}
			}else if(src.getClass().getName().equalsIgnoreCase("[b") || dest.getClass().getName().equalsIgnoreCase("[b"))
			{
				String srcField = src.toString();
				String destField = dest.toString();
				
				if (src.getClass().getName().equalsIgnoreCase("[b"))
				{
					srcField = new String((byte[])src);
				}

				if (dest.getClass().getName().equalsIgnoreCase("[b"))
				{
					destField = new String((byte[])dest);
				}
				
				if (!srcField.equalsIgnoreCase(destField))
				{
					throw new Exception("TABLE:" + tableName + " FIELD:" + colName + " VALUE (MIS-MATCH):" + srcField + "=" + destField);
				}
			}
			else
			{
				String srcField = src.toString();
				String destField = dest.toString();
	
				if (!srcField.equalsIgnoreCase(destField))
				{
					throw new Exception("TABLE:" + tableName + " FIELD:" + colName + " VALUE (MIS-MATCH):" + srcField + "=" + destField);
				}
			}
		}
		
		return true;
	}
}

