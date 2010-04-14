//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 11, 2005
 *
 */
package org.nrg.xft.search;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
/**
 * @author Tim
 *
 */
public class CriteriaCollection implements SQLClause{
	static org.apache.log4j.Logger logger = Logger.getLogger(CriteriaCollection.class);
	private ArrayList collection = new ArrayList();
	private String joinType = "AND";

	public String getElementName()
	{
	    if (size()> 0)
	    {
	        return ((SQLClause)collection.get(0)).getElementName();
	    }else{
	        return "";
	    }
	}

	public CriteriaCollection(String join)
	{
		joinType=join;
	}

	public String getSQLClause(QueryOrganizerI qo) throws Exception
	{
		String clause = "\n (";
		Iterator clauses = getClauses();
		int counter = 0;
		while (clauses.hasNext())
		{
			SQLClause c = (SQLClause)clauses.next();
			if (counter++ != 0)
			{
				clause += " " + joinType + " ";
			}
			clause +=c.getSQLClause(qo);
		}
		clause += ")";
		return clause;
	}


    public ArrayList getSchemaFields() throws Exception
	{
	    ArrayList al = new ArrayList();
	    Iterator clauses = getClauses();
		int counter = 0;
		while (clauses.hasNext())
		{
			SQLClause c = (SQLClause)clauses.next();
			al.addAll(c.getSchemaFields());
		}
	    return al;
	}

    public ArrayList<DisplayCriteria> getSubQueries() throws Exception
	{
	    ArrayList<DisplayCriteria> al = new ArrayList<DisplayCriteria>();
	    Iterator clauses = getClauses();
		int counter = 0;
		while (clauses.hasNext())
		{
			SQLClause c = (SQLClause)clauses.next();
			al.addAll(c.getSubQueries());
		}
	    return al;
	}

	/**
	 * Can be SearchCriteria or CriteriaCollection
	 * @return
	 */
	public Iterator getClauses()
	{
		return collection.iterator();
	}

	public ArrayList toArrayList()
	{
	    return this.collection;
	}

	public void addClause(String xmlPath,Object value)
	{
	    try {
            SearchCriteria c = new SearchCriteria();
            c.setFieldWXMLPath(xmlPath);
            c.setValue(value);
            this.add(c);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
	}



	public void addClause(String xmlPath,String comparison,Object value)
	{
	    try {
            SearchCriteria c = new SearchCriteria();
            c.setFieldWXMLPath(xmlPath);
            if (comparison.trim().equalsIgnoreCase("LIKE"))
    		{
    		    comparison = " LIKE ";
    	        String temp = value.toString();
    		    if (temp.startsWith("'"))
    		    {
    		        temp =temp.substring(1);
    		    }
    		    if (temp.endsWith("'"))
    		    {
    		        temp=temp.substring(0,temp.length()-1);
    		    }

    		    if (temp.indexOf("%") == -1)
    		    {
    		        temp = "%" + temp + "%";
    		    }

    		    value = temp ;

    		    //c.setOverrideFormatting(true);
    		}
            c.setValue(value);
            c.setComparison_type(comparison);
            this.add(c);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }
	}


    public void addClause(String xmlPath,String comparison,Object value,boolean overrideFormatting)
    {
        if (overrideFormatting){
            try {
                SearchCriteria c = new SearchCriteria();
                c.setFieldWXMLPath(xmlPath);
                if (comparison.trim().equalsIgnoreCase("LIKE"))
                {
                    comparison = " LIKE ";
                }
                c.setOverrideFormatting(overrideFormatting);
                c.setValue(value);
                c.setComparison_type(comparison);
                this.add(c);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }else{
            addClause(xmlPath,comparison,value);
        }
    }

	/**
	 * @param o
	 */
	public void addClause(SQLClause o)
	{
		collection.add(o);
	}

	public void add(SQLClause o)
	{
		addClause(o);
	}

	public int size()
	{
		return numClauses();
	}

	public int numClauses(){
	    int size = 0;
	    Iterator iter = collection.iterator();
	    while (iter.hasNext())
	    {
	        Object o = iter.next();
	        if (o instanceof CriteriaCollection)
	        {
	            size += ((CriteriaCollection)o).numClauses();
	        }else{
	            size++;
	        }
	    }
	    return size;
	}

    public int numSchemaClauses(){
        int size = 0;
        Iterator iter = collection.iterator();
        while (iter.hasNext())
        {
            Object o = iter.next();
            if (o instanceof CriteriaCollection)
            {
                size += ((CriteriaCollection)o).numSchemaClauses();
            }else{
                if (o instanceof SearchCriteria){
                    size++;
                }
            }
        }
        return size;
    }

	public Iterator iterator()
	{
		return collection.iterator();
	}

	public void addCriteria(ArrayList list)
	{
	   Iterator iter = list.iterator();
	   while(iter.hasNext())
	   {
	       SQLClause c = (SQLClause)iter.next();
	       add(c);
	   }
	}

	public void addCriteria(SQLClause clause)
	{
	    add(clause);
	}

	/**
	 * 'AND' or 'OR'
	 * @return
	 */
	public String getJoinType() {
		return joinType;
	}

	/**
	 * 'AND' or 'OR'
	 * @param string
	 */
	public void setJoinType(String string) {
		joinType = string;
	}

	public String toString()
	{
	    try {
            return this.getSQLClause(null);
        } catch (Exception e) {
            return "error";
        }
	}
}

