//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 18, 2004
 */
package org.nrg.xft.search;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.security.PermissionCriteriaI;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.utils.StringUtils;

/**
 * Object used to describe one search clause to be performed in the SearchService Class.
 *
 * @author Tim
 */
public class SearchCriteria implements SQLClause {
	static org.apache.log4j.Logger logger = Logger.getLogger(SearchCriteria.class);
	private String elementName = "";
	private String xmlPath = "";
	private String field_name = "";
	private Object value = "";
	private String comparison_type = "=";
	private GenericWrapperField field = null;
	private String cleanedType = "";
	private boolean overrideFormatting = false;

    /**
     * @return Returns the overrideFormatting.
     */
    public boolean overrideFormatting() {
        return overrideFormatting;
    }
    /**
     * @param overrideFormatting The overrideFormatting to set.
     */
    public void setOverrideFormatting(boolean overrideFormatting) {
        this.overrideFormatting = overrideFormatting;
    }
	public String getSQLClause() throws Exception
	{
	    if (overrideFormatting)
	    {
		    return " (" + getField_name() + getComparison_type() +  getValue() + ")";
	    }else{
		    return " (" + getField_name() + getComparison_type() +  valueToDB() + ")";
	    }
	}


	public String getSQLClause(QueryOrganizerI qo) throws Exception
	{
	    if (qo==null)
	    {
	        return getSQLClause();
	    }else{
	        if (overrideFormatting)
	        {
	            if (getComparison_type().toLowerCase().indexOf("like")==-1)
	            {
	            	String tCT=getComparison_type().trim().toUpperCase();
	            	String tV=(String)getValue();
	            	if(tV!=null){
	            		tV=tV.trim().toUpperCase();
	            	}
	            	if((tCT.equals("IS NULL")) || (tCT.equals("IS") && tV.equals("NULL"))){
	            		if(this.getCleanedType()!=null && this.getCleanedType().equals("string")){
	            			return " (" + qo.translateXMLPath(this.getXMLPath()) + " IS NULL OR " + qo.translateXMLPath(this.getXMLPath()) + "='')";
	    	            }else{
	            			return " (" + qo.translateXMLPath(this.getXMLPath()) + " IS NULL) ";
	    	            }
	            	}else if((tCT.equals("IS NOT NULL")) || (tCT.equals("IS NOT") && tV.equals("NULL")) || (tCT.equals("IS") && tV.equals("NOT NULL"))){
	            		if(this.getCleanedType()!=null && this.getCleanedType().equals("string")){
	            			return " NOT(" + qo.translateXMLPath(this.getXMLPath()) + " IS NULL OR " + qo.translateXMLPath(this.getXMLPath()) + "='')";
	    		        }else{
	    		        	return " NOT(" + qo.translateXMLPath(this.getXMLPath()) + " IS NULL) ";
	    		        }
	            	}else{
	            		return " (" + qo.translateXMLPath(this.getXMLPath()) + getComparison_type() +  getValue() + ")";
	    	       	}
				}else{

					return " (" + qo.translateXMLPath(this.getXMLPath()) + " " + getComparison_type() + " " +  getValue() + ")";
	            }
	        }else{
	            if (getComparison_type().toLowerCase().indexOf("like")==-1)
	            {
                    String v = valueToDB();
                    if(v==null && getComparison_type().trim().equals("=")){
                        StringBuffer where=new StringBuffer("");
                    	where.append(qo.translateXMLPath(this.getXMLPath()));
                        where.append(" IS NULL");
                        where.append(" OR ");
                        where.append(qo.translateXMLPath(this.getXMLPath()));
                        where.append(getComparison_type());
                        where.append("''");
                        return where.toString();
                    }else if (v.trim().equals("*"))
                    {
                        return " (" + qo.translateXMLPath(this.getXMLPath()) + " IS NOT NULL)";
                    }else{
                        return " (" + qo.translateXMLPath(this.getXMLPath()) + getComparison_type() +  v + ")";
                    }
	            }else{
	                String v = valueToDB();
	                if (v.indexOf("*")!=-1)
	                {
	                    v = StringUtils.ReplaceStr(v,"*","%");
	                }
	                if (v.indexOf("%")==-1)
	                {
	                    //remove single quotes
	                    v = v.substring(1,v.length()-1);

	                    v = "'%" + v + "%'";
	                }
					return " (LOWER(" + qo.translateXMLPath(this.getXMLPath()) + ") " + getComparison_type() + " " +  v.toLowerCase() + ")";
	            }
	        }
	    }
	}

	public SearchCriteria(){}

	/**
	 * Populates the field, field_name, cleanedType, and value properties.
	 * @param f
	 * @param v
	 */
	public SearchCriteria(GenericWrapperField f,Object v) throws Exception
	{
		field = f;
		xmlPath = f.getXMLPathString(f.getParentElement().getFullXMLName());
		this.setValue(v);
		field_name = f.getSQLName();
		cleanedType = f.getXMLType().getLocalType();
	}

	/**
	 * cleaned data type (i.e. string, integer, etc.) This is used to define the format of the
	 * value in the SQL WHERE clause (i.e. if it needs quotes).
	 * @return
	 */
	public String getCleanedType() {
		return cleanedType;
	}

	/**
	 * =,&#60;,&#60;=,&#62;,&#62;=
	 * @return
	 */
	public String getComparison_type() {
		return comparison_type;
	}

	/**
	 * corresponding GenericWrapperField (can be null)
	 * @return
	 */
	public GenericWrapperField getField() {
		return field;
	}

	/**
	 * exact sql name to use in the where clause.
	 * @return
	 */
	public String getField_name() {
		return field_name;
	}

	/**
	 * value to search for
	 * @return
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * cleaned data type (i.e. string, integer, etc.) This is used to define the format of the
	 * value in the SQL WHERE clause (i.e. if it needs quotes).
	 * @param string
	 */
	public void setCleanedType(String string) {
		cleanedType = string;
	}

	/**
	 * =,&#60;,&#60;=,&#62;,&#62;=
	 * @param string
	 */
	public void setComparison_type(String string) {
		comparison_type = string;
	}

	/**
	 * corresponding GenericWrapperField (can be null)
	 * @param field
	 */
	public void setField(GenericWrapperField field) {
		this.field = field;
	}

	/**
	 * exact sql name to use in the where clause.
	 * @param string
	 */
	public void setField_name(String string) {
		field_name = string;
	}

	/**
	 * value to search for.
	 * @param object
	 */
	public void setValue(Object v)throws Exception {
		if(v instanceof String)
		{
			String temp = (String)v;
			
	    	if(PoolDBUtils.HackCheck(temp)){
			    AdminUtils.sendAdminEmail("Possible SQL Injection Attempt", "VALUE:" + temp);
	    		throw new Exception("Invalid search value (" + temp + ")");
	    	}
	    	
			if(temp.contains("'"))
			{
				v=StringUtils.CleanForSQLValue(temp);
			}
		}
		value = v;
	}

	/**
	 * Parse the value using the cleanedType to output the correct format for SQL/
	 * @return
	 */
	public String valueToDB()
	{
	    if (getValue()==null)
	    {
	        return null;
	    }else{
			try {
				if(field!=null && field.getWrapped().getRule().getBaseType().equals("xs:anyURI")){
                    return DBAction.ValueParser(getValue(),"anyURI",true);
                }else{
    				return DBAction.ValueParser(getValue(),getCleanedType(),true);
                }
			} catch (InvalidValueException e) {
				logger.error("",e);
				return null;
			}
	    }
	}

	/**
	 * Uses an XMLName Dot-Syntax to specify what field within an element is to be searched on.
	 * (i.e. MrSession.Experiment.ExptDate)
	 * @param s
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 * @throws Exception
	 */
	public void setFieldWXMLPath(String s) throws ElementNotFoundException, XFTInitException, FieldNotFoundException,Exception
	{
	    xmlPath = StringUtils.StandardizeXMLPath(s);
		String rootElement = StringUtils.GetRootElementName(s);
		GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
		this.setElementName(root.getFullXMLName());
		s = root.getFullXMLName() + xmlPath.substring(xmlPath.indexOf(XFT.PATH_SEPERATOR));
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(s);

		String temp = ViewManager.GetViewColumnName(root,s);

		this.setField_name(temp);
		if (f.isReference())
		{
		    GenericWrapperElement gwe = ((GenericWrapperElement)f.getReferenceElement());
		    Iterator iter =gwe.getAllPrimaryKeys().iterator();
		    while (iter.hasNext())
		    {
		        GenericWrapperField gwf = (GenericWrapperField)iter.next();
		        this.setCleanedType(gwf.getXMLType().getLocalType());
		        break;
		    }
		}else{
			this.setCleanedType(f.getXMLType().getLocalType());
		}

	}

	public String getXMLPath()
	{
	    if (xmlPath.equalsIgnoreCase(""))
	    {
	        xmlPath = getElementName() +XFT.PATH_SEPERATOR + getField_name();
	    }
	    return xmlPath;
	}

	public int numClauses(){
	    return 1;
	}
    /**
     * @return Returns the elementName.
     */
    public String getElementName() {
        return elementName;
    }
    /**
     * @param elementName The elementName to set.
     */
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String toString()
    {
        try {
            return this.getSQLClause();
        } catch (Exception e) {
            return "error";
        }
    }

    public ArrayList getSchemaFields()
	{
	    ArrayList al = new ArrayList();
        Object[] o = new Object[2];
        o[0]=this.getXMLPath();
        o[1]=this.getField();
	    al.add(o);
	    return al;
	}

    public ArrayList<DisplayCriteria> getSubQueries() throws Exception
	{
	    return new ArrayList<DisplayCriteria>();
	}
    
    public static SearchCriteria buildCriteria(PermissionCriteriaI c){
    	final SearchCriteria newC = new SearchCriteria();
        try {
			newC.setFieldWXMLPath(c.getField());
			newC.setValue(c.getFieldValue());
		} catch (Exception e) {
			logger.error("",e);
		}
        
        return newC;
    }
}

