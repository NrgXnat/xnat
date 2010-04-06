//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Feb 7, 2005
 *
 */
package org.nrg.xft.search;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class TableSearch implements TableSearchI {
	static org.apache.log4j.Logger logger = Logger.getLogger(TableSearch.class);
	private UserI user = null;
	private GenericWrapperElement element = null;
	private CriteriaCollection criteriaCollection = new CriteriaCollection("AND");
	private boolean joined = false;
		
	public static XFTTable GetMappingTable(XFTManyToManyReference manyRef,CriteriaCollection cc,String userName) throws Exception
	{
		String query = "SELECT * FROM " + manyRef.getMappingTable();
		query += " WHERE " + cc.getSQLClause(null);
		query += ";";

		XFTTable table =TableSearch.Execute(query,manyRef.getElement1().getDbName(),userName);
		return table;
	}
	
	public TableSearch(){};
	
	public TableSearch(UserI u, GenericWrapperElement e, CriteriaCollection cc, boolean joined)
	{
		user = u;
		element = e;
		criteriaCollection = cc;
		this.joined = joined;
	}
	
	public XFTTableI execute(String userName) throws Exception
	{
		return execute(false,userName);
	}
	
	public XFTTable execute(boolean loadHistory, String userName) throws Exception
	{
		if (joined)
		{
			String query = element.getSelectFromView(true,loadHistory);
			if (criteriaCollection != null && criteriaCollection.size() > 0)
				query += " WHERE " + criteriaCollection.getSQLClause(null);
			query += ";";
			
			XFTTable table = Execute(query,element.getDbName(),userName);
			return table;
		}else{
			String query = element.getSelectFromView(false,loadHistory);
			if (criteriaCollection != null && criteriaCollection.size() > 0)
				query += " WHERE " + this.getCriteriaCollection().getSQLClause(null);
			
			query += ";";

			XFTTable table = Execute(query,element.getDbName(),userName);
			return table;
		}
	}
	

	
	public String getQuery(boolean allowMultiples,boolean loadHistory) throws Exception
	{
		String query = element.getSelectFromView(allowMultiples,loadHistory);
			if (criteriaCollection != null && criteriaCollection.size() > 0)
				query += " WHERE " + criteriaCollection.getSQLClause(null);
			query += ";";
		return query;
	}
	
	public static XFTTable Execute(String query, String dbName, String userName) throws Exception
	{
		//logger.debug(query);

		PoolDBUtils con = null;
		XFTTable table = null;
		try {
			con = new PoolDBUtils();
			table = con.executeSelectQuery(query,dbName,userName);
			table.resetRowCursor();
		} catch (DBPoolException e) {
			throw e;
		} catch (SQLException e) {
			throw e;
		}
		return table;
	}
	
	/**
	 * @return Returns the criteriaCollection.
	 */
	public CriteriaCollection getCriteriaCollection() {
		return criteriaCollection;
	}
	/**
	 * @param criteriaCollection The criteriaCollection to set.
	 */
	public void setCriteriaCollection(CriteriaCollection criteriaCollection) {
		this.criteriaCollection = criteriaCollection;
	}
	/**
	 * @return Returns the element.
	 */
	public GenericWrapperElement getElement() {
		return element;
	}
	/**
	 * @param element The element to set.
	 */
	public void setElement(GenericWrapperElement element) {
		this.element = element;
	}
	/**
	 * @return Returns the user.
	 */
	public UserI getUser() {
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	public void setUser(UserI user) {
		this.user = user;
	}
	/**
	 * @return Returns the joined.
	 */
	public boolean isJoined() {
		return joined;
	}
	/**
	 * @param joined The joined to set.
	 */
	public void setJoined(boolean joined) {
		this.joined = joined;
	}

	
	public void addCriteria(SQLClause c)
	{
		criteriaCollection.addClause(c);
	}
	
	public void add(SQLClause c)
	{
		addCriteria(c);
	}
	
	public void addCriteria(String xmlPath, Object value, String comparison)throws Exception
	{
		SearchCriteria c = new SearchCriteria();
		c.setFieldWXMLPath(xmlPath);
		c.setValue(value);
		c.setComparison_type(comparison);
		add(c);
	}
	
	public void addCriteria(String xmlPath, String comparisonType, Object value) throws Exception
	{
	    addCriteria(xmlPath,value,comparisonType);
	}
	
	public void addCriteria(String xmlPath, Object value)throws Exception
	{
		addCriteria(xmlPath,value,"=");
	}
	
	public void addCriteria(GenericWrapperField f, Object value, String comparison)throws Exception
	{
		SearchCriteria c = new SearchCriteria();
		c.setField(f);
		c.setValue(value);
		c.setComparison_type(comparison);
		add(c);
	}
	
	public void addCriteria(GenericWrapperField f, Object value)throws Exception
	{
		addCriteria(f,value,"=");
	}
	
	public static ArrayList GetUniqueValuesForField(String xmlPath,String userName) throws Exception
	{
	    GenericWrapperField gwf = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
	    GenericWrapperElement root = gwf.getParentElement().getGenericXFTElement();
	    
	    StringBuffer sb = new StringBuffer();
	    sb.append("SELECT DISTINCT ").append(gwf.getSQLName());
	    sb.append(" FROM ").append(root.getSQLName());
	    
	    XFTTable table = TableSearch.Execute(sb.toString(),root.getDbName(),userName);
	    return table.convertColumnToArrayList(gwf.getSQLName());
	}
	
}

