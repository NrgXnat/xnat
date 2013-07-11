//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Dec 21, 2004
 *
 */
package org.nrg.xdat.search;

import java.util.ArrayList;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.search.QueryOrganizerI;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class DisplayCriteria implements SQLClause{
	private SearchCriteria xftCriteria = new SearchCriteria();

	private String element = null;
	private String field = null;
	private Object o = null;
	private String comparison_type = "=";
	private String where_value=null;

	private boolean overrideDataFormatting = false;

	public String getSQLClause() throws Exception
	{
		StringBuffer where = new StringBuffer(" (");
		SchemaElement e = SchemaElement.GetElement(getElement());
		DisplayField df = e.getDisplayField(getField());

		where.append(DisplayManager.DISPLAY_FIELDS_VIEW + e.getSQLName());
		where.append(".").append(df.getId()).append(getComparisonType());
		if (overrideDataFormatting)
		{
		    where.append(getValue().toString());
		}else if (df.needsSQLQuotes())
		{
			where.append("'").append(getValue()).append("'");
		}else{
			where.append(getValue());
		}
		where.append(")");
		return where.toString();
	}
	
	private String getSQLContent(DisplayField df, QueryOrganizerI qo) throws Exception{
		if(this.getWhere_value()!=null){
			if(!this.getElementName().equals(qo.getRootElement().getFullXMLName())){
				if(qo.translateStandardizedPath(this.getElementName() + ".SUBQUERYFIELD_"+ this.field + "."+this.getWhere_value())!=null)
					return qo.translateStandardizedPath(this.getElementName() + ".SUBQUERYFIELD_"+ this.field + "."+this.getWhere_value());
				else
					return this.field + "_" + DisplaySearch.cleanColumnName(this.getWhere_value());
			}else{
				return this.field + "_" + DisplaySearch.cleanColumnName(this.getWhere_value());
			}
		}else{
			return df.getSQLContent(qo);
		}
	}

	public String getSQLClause(QueryOrganizerI qo) throws Exception
	{
	    if (qo==null)
	    {
	        return getSQLClause();
	    }else{
	        StringBuffer where = new StringBuffer(" (");
			SchemaElement e = SchemaElement.GetElement(getElement());
			DisplayField df = e.getDisplayField(getField());

			String v = "";
			if(getValue()!=null){
				v=getValue().toString();
			}
			if (overrideDataFormatting)
			{
				String tCT=getComparisonType().trim().toUpperCase();
            	String tV=(String)getValue();
            	if(tV!=null){
            		tV=tV.trim().toUpperCase();
            	}
            	if((tCT.equals("IS NULL")) || (tCT.equals("IS") && tV.equals("NULL"))){
            		if(df.needsSQLEmptyQuotes()){
            			where.append(" (" + this.getSQLContent(df, qo) + " IS NULL OR " + this.getSQLContent(df, qo) + "='')");
                   	}else{
                   		where.append(" (" + this.getSQLContent(df, qo) + " IS NULL)");
                    }
            	}else if((tCT.equals("IS NOT NULL")) || (tCT.equals("IS NOT") && tV.equals("NULL")) || (tCT.equals("IS") && tV.equals("NOT NULL"))){
            		if(df.needsSQLEmptyQuotes()){
            			where.append(" NOT(" + this.getSQLContent(df, qo) + " IS NULL OR " + this.getSQLContent(df, qo) + "='')");
        	        }else{
                   		where.append(" NOT(" + this.getSQLContent(df, qo) + " IS NULL)");
        	        }
            	}else{
					where.append(this.getSQLContent(df, qo));
					where.append(getComparisonType());
				    where.append(getValue().toString());
            	}
			}else{
				if (getComparisonType().indexOf("LIKE") == -1)
			    {
					where.append(handleValues(v,df,qo,df.needsSQLQuotes()));
			    }else{
					where.append("LOWER(" + this.getSQLContent(df, qo) + ")");
					where.append(getComparisonType());
					where.append("'").append(getValue().toString().toLowerCase()).append("'");
                }
			}
			where.append(")");
			return where.toString();
	    }
	}
	
	private String handleValues(String v, DisplayField df, QueryOrganizerI qo,boolean needsQuotes) throws Exception{
		if(v.trim().equals("") && getComparisonType().trim().equals("=")){
			StringBuffer where=new StringBuffer();
			where.append(this.getSQLContent(df, qo));
            where.append(" IS NULL");
            if(df.needsSQLEmptyQuotes()){
	            where.append(" OR ");
				where.append(this.getSQLContent(df, qo));
	            where.append(getComparisonType());
	            where.append("''");
            }
    		return where.toString();
        }else if (getComparisonType().trim().equals("!=")){
            StringBuffer where=new StringBuffer();
            where.append("(");
            where.append(this.getSQLContent(df, qo));
            where.append(getComparisonType());
            if(needsQuotes){
                where.append("'").append(v).append("'");
            }else{
                where.append(v);
            }
            where.append(" OR ");
            where.append(this.getSQLContent(df, qo));
            where.append(" IS NULL)");
            return where.toString();
        }else if (v.trim().equals("*"))
        {
            return " (" + this.getSQLContent(df, qo) + " IS NOT NULL)";
        }else if (getComparisonType().trim().equals("IN"))
        {
        	String values="";
        	String[] tokens = v.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            org.apache.commons.lang.StringUtils.stripAll(tokens,"'\"");
        	int c=0;
            for(String t : tokens) {
                if(c++>0){
                	values+=",";
                };
                if(!PoolDBUtils.HackCheck(t)){
                	if(needsQuotes){
                		values+="'"+t+"'";
                	}else{
                		values+=t;
                	}
                }
            }

        	return " (" + this.getSQLContent(df, qo) + " IN ("+ values +"))";
        }else if (getComparisonType().trim().equals("BETWEEN"))
        {
        	int and=v.toUpperCase().indexOf(" AND ");
        	if(and==-1){
        		throw new InvalidValueException("BETWEEN clauses require an AND to separate values");
        	}
        	
        	String v1=v.substring(0,and);
        	String v2=v.substring(and+5);
        	
        	//remove any user quotes from beginning and end
        	org.apache.commons.lang.StringUtils.strip(v1,"'\"");
        	org.apache.commons.lang.StringUtils.strip(v2,"'\"");
        	
        	if(!PoolDBUtils.HackCheck(v1) && !PoolDBUtils.HackCheck(v2)){
            	return " (" + this.getSQLContent(df, qo) + " BETWEEN '"+ v1 +"' AND '"+ v2 +"')";//it appears to be OK to use quotes here even with numeric data
            }else{
            	throw new InvalidValueException("Invalid BETWEEN values");
            }

        }else{
        	if(PoolDBUtils.HackCheck(v)){
            	throw new InvalidValueException("Invalid BETWEEN values");
            }
        	
        	StringBuffer where=new StringBuffer();
    		where.append(this.getSQLContent(df, qo));
            where.append(getComparisonType());
            if(needsQuotes){
                where.append("'").append(v).append("'");
            }else{
                where.append(v);
            }
    		return where.toString();
        }
	}

    ArrayList schemaFields =null;
	public ArrayList getSchemaFields() throws Exception
	{
        if (schemaFields==null)
        {
            SchemaElement e = SchemaElement.GetElement(getElement());
            DisplayField df = e.getDisplayField(getField());

            schemaFields= df.getSchemaFields();
        }
        return schemaFields;
	}

    public ArrayList<DisplayCriteria> getSubQueries() throws Exception
	{
	    ArrayList<DisplayCriteria> al = new ArrayList<DisplayCriteria>();
        if(this.getWhere_value()!=null){
        	al.add(this);
        }
	    return al;
	}

	public String getElementName()
	{
	    return element;
	}

	/**
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * @return
	 */
	protected SearchCriteria getXftCriteria() {
		return xftCriteria;
	}

	public void setSearchFieldByDisplayField(String elementName,String fieldId)
	{
		field = fieldId;
		element = elementName;
	}

	public void setValue(Object v,boolean hackcheck) throws Exception
	{
		if(v instanceof String)
		{
			String temp = (String)v;
			
	    	if(hackcheck && PoolDBUtils.HackCheck(temp)){
			    AdminUtils.sendAdminEmail("Possible SQL Injection Attempt", "VALUE:" + temp);
	    		throw new Exception("Invalid search value (" + temp + ")");
	    	}
	    	
			if(temp.contains("'"))
			{
				v=StringUtils.CleanForSQLValue(temp);
			}
		}
		o=v;
	}

	public Object getValue()
	{
		return o;
	}
	/**
	 * @return
	 */
	public String getElement() {
		return element;
	}

	/**
	 * @return
	 */
	public String getComparisonType() {
		return comparison_type;
	}

	/**
	 * @param string
	 */
	public void setComparisonType(String string) {
		comparison_type = string;
	}

	public int numClauses(){
	    return 1;
	}

	public static DisplayCriteria addCriteria(String element, String displayField, String comparisonType, Object value) throws Exception
	{
	    DisplayCriteria dc = new DisplayCriteria();
	    if(displayField.indexOf("=")>-1){
	    	dc.setWhere_value(displayField.substring(displayField.indexOf("=")+1));
	    	displayField=displayField.substring(0,displayField.indexOf("="));
	    }
	    dc.setSearchFieldByDisplayField(element,displayField);
	    dc.setComparisonType(comparisonType);
	    dc.setValue(value,true);
	    return dc;
	}
    /**
     * @return Returns the overrideDataFormatting.
     */
    public boolean isOverrideDataFormatting() {
        return overrideDataFormatting;
    }
    /**
     * @param overrideDataFormatting The overrideDataFormatting to set.
     */
    public void setOverrideDataFormatting(boolean overrideDataFormatting) {
        this.overrideDataFormatting = overrideDataFormatting;
    }

	public String getWhere_value() {
		return where_value;
	}

	public void setWhere_value(String where_value) {
		this.where_value = where_value;
	}
    
    
}

