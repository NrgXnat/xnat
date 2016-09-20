/*
 * core: org.nrg.xft.search.SearchService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.search;
public class SearchService {
	public final static String EQUALS = "=";
	public final static String LIKE = "LIKE";
	public final static String GREATER = ">";
	public final static String LESS = "<";
	public final static String GREATER_EQUAL = ">=";
	public final static String LESS_EQUAL = "<=";


	/**
	 * Performs a select-grand search for the given criteria.  It generates the fully-populated
	 * XFTItems from the XFTTable and returns them in an ArrayList.
	 * 
	 * @param element
	 * @param criteria ArrayList of SearchCriteria
	 * @return ArrayList of XFTItems
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 * @throws org.nrg.xft.exception.DBPoolException
	 * @throws java.sql.SQLException
	 */
//	public static ArrayList FindPopulatedXFTItemsByField(GenericWrapperElement element,ArrayList criteria) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,Exception
//	{
//		ArrayList al = new ArrayList();
//		XFTTable table = FindJoinedXFTTableByFields(element,criteria);
//		al.addAll(table.populateItems(element.getType().getFullForeignType()));
//			
//		al.trimToSize();
//		return al;
//	}
//	
//	/**
//	 * Performs a simple search for the given criteria.  It generates the un-populated simple
//	 * XFTItems from the XFTTable and returns them in an ArrayList.
//	 * @param element
//	 * @param criteria ArrayList of SearchCriteria
//	 * @return ArrayList of XFTItems
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws DBPoolException
//	 * @throws SQLException
//	 * @throws ElementNotFoundException
//	 */
//	public static ArrayList FindSimpleXFTItemsByFields(GenericWrapperElement element,ArrayList criteria) throws org.nrg.xft.exception.XFTInitException,DBPoolException,SQLException,ElementNotFoundException,FieldNotFoundException,Exception
//	{
//		ArrayList al = new ArrayList();
//		XFTTable table = SearchService.FindSimpleXFTTableByFields(element,criteria);
//		al.addAll(table.populateItems(element.getType().getFullForeignType()));
//			
//		al.trimToSize();
//		return al;
//	}
//	
//	/**
//	 * Performs a simple search for the given criteria.  It generates the simple
//	 * XFTTable.
//	 * @param element
//	 * @param criteria ArrayList of SearchCriteria
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws DBPoolException
//	 * @throws SQLException
//	 * @throws org.nrg.xft.exception.ElementNotFoundException
//	 */
//	public static XFTTable FindSimpleXFTTableByFields(GenericWrapperElement element,ArrayList criteria) throws XFTInitException,DBPoolException,SQLException, ElementNotFoundException,Exception
//	{
//		ArrayList keys = element.getAllPrimaryKeys();
//
//		String query = "SELECT * FROM " + element.getSQLName();
//		int fieldCount = 0;
//		Iterator crit = criteria.iterator();
//		while (crit.hasNext())
//		{
//			SQLClause c = (SQLClause)crit.next();
//			if (fieldCount++ ==0)
//			{
//				query += " WHERE " + c.getSQLClause();
//			}else{
//				query += " AND " + c.getSQLClause();
//			}
//		}
//		query += ";";
//
//		XFT.LogSQLInfo(query);
//
//		PoolDBUtils con = null;
//		XFTTable table = null;
//		try {
//			con = new PoolDBUtils();
//			table = con.executeSelectQuery(query,element.getDbName());
//			table.resetRowCursor();
//		} catch (DBPoolException e) {
//			throw e;
//		} catch (SQLException e) {
//			throw e;
//		}
//		return table;
//	}
//	
//	
//	
//	/**
//	 * Performs a simple search for the given query.  It generates the XFTTable.
//	 * @param query
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws DBPoolException
//	 * @throws SQLException
//	 */
//	public static XFTTable FindXFTTableByQuery(String query,String dbName) throws XFTInitException,DBPoolException,SQLException, ElementNotFoundException
//	{
//		XFT.LogSQLInfo(query);
//
//		PoolDBUtils con = null;
//		XFTTable table = null;
//		try {
//			con = new PoolDBUtils();
//			table = con.executeSelectQuery(query,dbName);
//			table.resetRowCursor();
//		} catch (DBPoolException e) {
//			throw e;
//		} catch (SQLException e) {
//			throw e;
//		}
//		return table;
//	}
//	
//	/**
//	 * Performs a select-grand search for the given criteria.  It generates the fully-populated XFTTable.
//	 * @param element
//	 * @param criteria ArrayList of SearchCriteria
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws DBPoolException
//	 * @throws SQLException
//	 * @throws org.nrg.xft.exception.ElementNotFoundException
//	 */
//	public static XFTTable FindJoinedXFTTableByFields(GenericWrapperElement element,ArrayList criteria) throws org.nrg.xft.exception.XFTInitException,DBPoolException,SQLException, org.nrg.xft.exception.ElementNotFoundException,Exception
//	{
//		String query = GetJoinedQuery(element,criteria);
//		query += ";";
//		
//		XFT.LogSQLInfo(query);
//
//		PoolDBUtils con = null;
//		XFTTable table = null;
//		try {
//			con = new PoolDBUtils();
//			table = con.executeSelectQuery(query,element.getDbName());
//			table.resetRowCursor();
//		} catch (DBPoolException e) {
//			throw e;
//		} catch (SQLException e) {
//			throw e;
//		}
//
//		return table;
//	}
//	
//	public static String GetJoinedQuery(GenericWrapperElement element,ArrayList criteria) throws XFTInitException,ElementNotFoundException,Exception
//	{
//		String query = element.getSelectFromView(true);
//		
//		return query + GetJoinedWhereClause(element,criteria);
//	}
//	
//	public static String GetJoinedWhereClause(GenericWrapperElement element,ArrayList criteria) throws Exception
//	{
//		String query = "";
//		int fieldCount = 0;
//		Iterator crit = criteria.iterator();
//		while (crit.hasNext())
//		{
//			SQLClause c = (SQLClause)crit.next();
//			if (fieldCount++ ==0)
//			{
//				query += " WHERE " + c.getSQLClause();
//			}else{
//				query += " AND " + c.getSQLClause();
//			}
//		}
//		return query;
//	}
//	
//	/**
//	 * Performs a search on a mapping table and returns a simple XFTTable.
//	 * @param XFTManyToManyReference
//	 * @param criteria ArrayList of SearchCriteria
//	 * @return
//	 * @throws org.nrg.xft.exception.XFTInitException
//	 * @throws DBPoolException
//	 * @throws SQLException
//	 * @throws org.nrg.xft.exception.ElementNotFoundException
//	 */
//	public static XFTTable FindXFTTableByFields(XFTManyToManyReference manyRef,ArrayList criteria) throws XFTInitException,DBPoolException,SQLException, ElementNotFoundException,Exception
//	{
//		String query = "SELECT * FROM " + manyRef.getMappingTable();
//		int fieldCount = 0;
//		Iterator crit = criteria.iterator();
//		while (crit.hasNext())
//		{
//			SQLClause c = (SQLClause)crit.next();
//			if (fieldCount++ ==0)
//			{
//				query += " WHERE " + c.getSQLClause();
//			}else{
//				query += " AND " + c.getSQLClause();
//			}
//		}
//		query += ";";
//
//		XFT.LogSQLInfo(query);
//
//		PoolDBUtils con = null;
//		XFTTable table = null;
//		try {
//			con = new PoolDBUtils();
//			table = con.executeSelectQuery(query,manyRef.getElement1().getDbName());
//			table.resetRowCursor();
//		} catch (DBPoolException e) {
//			throw e;
//		} catch (SQLException e) {
//			throw e;
//		}
//		return table;
//	}

}

