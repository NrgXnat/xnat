/*
 * org.nrg.xnat.restlet.resources.QueryOrganizerResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet.resources;

import org.nrg.xft.TypeConverter.JavaMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

public abstract class QueryOrganizerResource extends SecureResource {

	public QueryOrganizerResource(Context context, Request _request, Response _response) {
		super(context, _request, _response);
	}

	
	public CriteriaCollection processStringQuery(String xmlPath, String values){
		ArrayList<String> al=StringUtils.CommaDelimitedStringToArrayList(values);
		CriteriaCollection cc= new CriteriaCollection("OR");
		for(String value:al){
			if(value.contains("%") || value.contains("*")){
				value=StringUtils.ReplaceStr(value, "*", "%");
				cc.addClause(xmlPath, "LIKE", value);
			}else{
				cc.addClause(xmlPath, value);
			}
		}
		return cc;
	}
	
	public CriteriaCollection processDateQuery(String column, String dates){
		ArrayList<String> al=StringUtils.CommaDelimitedStringToArrayList(dates);
		CriteriaCollection cc= new CriteriaCollection("OR");
		for(String date:al){
			if(date.contains("-")){
				String date1;
				try {
					date1=DateUtils.parseDate(date.substring(0,date.indexOf("-"))).toString();
				} catch (ParseException e) {
					date1=date.substring(0,date.indexOf("-"));
				}
	
				String date2;
				try {
					date2=DateUtils.parseDate(date.substring(date.indexOf("-")+1)).toString();
				} catch (ParseException e) {
					date2=date.substring(date.indexOf("-")+1);
				}

				CriteriaCollection subCC = new CriteriaCollection("AND");
				subCC.addClause(column, ">=", date1);
				subCC.addClause(column, "<=", date2);
				cc.add(subCC);
			}else{
				String date1;
				try {
					date1=DateUtils.parseDate(date).toString();
				} catch (ParseException e) {
					date1=date;
				}
				cc.addClause(column, date1);
			}
		}
		return cc;
	}
	
	public CriteriaCollection processNumericQuery(String column, String values){
		ArrayList<String> al=StringUtils.CommaDelimitedStringToArrayList(values);
		CriteriaCollection cc= new CriteriaCollection("OR");
		for(String date:al){
			if(date.contains("-")){
				String date1=date.substring(0,date.indexOf("-"));
	
				String date2=date.substring(date.indexOf("-")+1);
				CriteriaCollection subCC = new CriteriaCollection("AND");
				subCC.addClause(column, ">=", date1);
				subCC.addClause(column, "<=", date2);
				cc.add(subCC);
			}else{
				cc.addClause(column, date);
			}
		}
		return cc;
	}
	
	public CriteriaCollection processBooleanQuery(String column, String values){
		ArrayList<String> al=StringUtils.CommaDelimitedStringToArrayList(values);
		CriteriaCollection cc= new CriteriaCollection("OR");
		for(String value:al){
			cc.addClause(column, value);
		}
		return cc;
	}
	
	public CriteriaCollection processQueryCriteria(String xPath, String values){
		CriteriaCollection cc= new CriteriaCollection("OR");
		try {
			GenericWrapperField gwf =GenericWrapperElement.GetFieldForXMLPath(xPath);
            assert gwf != null;
            String type=gwf.getType(new TypeConverter(new JavaMapping("")));

            switch (type) {
                case STRING:
                    cc.add(this.processStringQuery(xPath, values));
                    break;
                case DOUBLE:
                    cc.add(this.processNumericQuery(xPath, values));
                    break;
                case INTEGER:
                    cc.add(this.processNumericQuery(xPath, values));
                    break;
                case DATE:
                    cc.add(this.processDateQuery(xPath, values));
                    break;
                case BOOL:
                    cc.add(this.processBooleanQuery(xPath, values));
                    break;
            }
		} catch (XFTInitException e) {
			logger.error("An error occurred during XFT initialization",e);
		} catch (ElementNotFoundException e) {
			logger.error("Couldn't find an element in the xPath " + xPath,e);
		} catch (FieldNotFoundException e) {
			logger.error("Couldn't find the field specified by the xPath " + xPath,e);
		}
		return cc;
	}
	
	public abstract ArrayList<String> getDefaultFields(GenericWrapperElement e);
	
	public ArrayList<String> columns=null;
	
	public void populateQuery(QueryOrganizer qo){
		if(hasQueryVariable("columns") && !getQueryVariable("columns").equals("DEFAULT")){ 
			try {
				columns=StringUtils.CommaDelimitedStringToArrayList(URLDecoder.decode(getQueryVariable("columns"), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				logger.error("",e);
				columns=getDefaultFields(qo.getRootElement());
			}
		}else{
			columns=getDefaultFields(qo.getRootElement());
		}
		
		for(String key: columns){
			try {
				if(key.contains("/")){
					qo.addField(key);
				}else if(this.fieldMapping.containsKey(key)){
					qo.addField(this.fieldMapping.get(key));
				}else{
					System.out.println("Unknown Alias: "+ key);
				}
			} catch (ElementNotFoundException e) {
				logger.error("",e);
			}
		}
		
		CriteriaCollection cc = new CriteriaCollection("AND");
		
		if(this.fieldMapping.size()>0){
			for(String key: fieldMapping.keySet()){
				if(!key.equals("xsiType") && hasQueryVariable(key)){
					cc.add(this.processQueryCriteria(this.fieldMapping.get(key),getQueryVariable(key)));
				}
			}
		}
		
		for(String key:getQueryVariableKeys()){
			if(key.contains("/")){
				cc.add(this.processQueryCriteria(key,getQueryVariable(key)));
			}
		}
		
		if(isQueryVariable("req_format", "form", false))
		{
			if(this.fieldMapping.size()>0){
				for(String key: fieldMapping.keySet()){
					if(hasBodyVariable(key)){
						cc.add(this.processQueryCriteria(this.fieldMapping.get(key),getBodyVariable(key)));
					}
				}
			}
			
			for(String key:getBodyVariableKeys()){
				if(key.contains("/")){
					cc.add(this.processQueryCriteria(key,getBodyVariable(key)));
				}
			}
			
			if(hasBodyVariable("columns")){
				columns=StringUtils.CommaDelimitedStringToArrayList(getBodyVariable("columns"));
				for(String col:columns){
					if(col.contains("/")){
						try {
							qo.addField(col);
						} catch (ElementNotFoundException e) {
							logger.error("",e);
						}
					}else if(this.fieldMapping.containsKey(col)){
						try {
							qo.addField(this.fieldMapping.get(col));
						} catch (ElementNotFoundException e) {
							logger.error("",e);
						}
					}
				}
			}
		}
		
		if(cc.size()>0){
			qo.setWhere(cc);
		}
	}
	
	public XFTTable formatHeaders(XFTTable table,QueryOrganizer qo,String idpath,String URIpath){
		final ArrayList<String> newColumns = new ArrayList<>();
		for(String column:table.getColumns()){
			String xPath=qo.getXPATHforAlias(column.toLowerCase());
			if(xPath==null){
				newColumns.add(column);
			}else{
				String key=this.getLabelForFieldMapping(xPath);
				if(key==null){
					newColumns.add(xPath);
				}else{
					newColumns.add(key);
				}
			}
		}
		
		int idIndex=table.getColumnIndex(qo.getFieldAlias(idpath));
		
		if(URIpath!=null)
			newColumns.add("URI");
		
		XFTTable clone = new XFTTable();			
		clone.initTable(newColumns);
		for(Object[]row :table.rows()){
			Object[]newRow;
			if(URIpath!=null)
				newRow=new Object[row.length+1];
			else
				newRow=new Object[row.length];

            System.arraycopy(row, 0, newRow, 0, row.length);
			
			String id=(String)row[idIndex];

			if(URIpath!=null)newRow[row.length]=URIpath+id ;
			
			clone.insertRow(newRow);
		}
		
		return clone;
	}
	
	private final static String STRING="java.lang.String";
	private final static String DOUBLE="java.lang.Double";
	private final static String INTEGER="java.lang.Integer";
	private final static String DATE="java.util.Date";
	private final static String BOOL="java.lang.Boolean";
	
	public abstract String getDefaultElementName();

	public String getRootElementName(){
		try {
			GenericWrapperElement rootElementName=GenericWrapperElement.GetElement(this.getDefaultElementName());
			if(this.getQueryVariable("xsiType")!=null && !getQueryVariable("xsiType").contains(",")){
				return this.getQueryVariable("xsiType");
			}

			ArrayList<String> fields= new ArrayList<>();
			
			for(String key:getQueryVariableKeys()){
				if(key.contains("/")){
					fields.add(key);
				}else if(this.fieldMapping.containsKey(key)){
					fields.add(this.fieldMapping.get(key));
				}else if(key.equals("columns")){
					for(String col:StringUtils.CommaDelimitedStringToArrayList(getQueryVariable("columns"))){
						if(col.contains("/")){
							fields.add(col);
						}else if(this.fieldMapping.containsKey(col)){
							fields.add(this.fieldMapping.get(col));
						}
					}
				}
			}
			
			for(String field:fields){
				try {
					GenericWrapperElement ge=StringUtils.GetRootElement(field);
                    assert ge != null;
                    if(!ge.getXSIType().equals(rootElementName.getXSIType()) && ge.isExtensionOf(rootElementName)){
						rootElementName=ge;
					}
				} catch (ElementNotFoundException e) {
					logger.error("",e);
				}
			}
			
			return rootElementName.getXSIType();
		} catch (Throwable e) {
			logger.error("",e);
			return this.getDefaultElementName();
		}
	}
	
	@Override
	public Representation represent(Variant variant) {
		try {
			if(this.getQueryVariable("fields")!=null){
                XFTTable table=new XFTTable();
				String[] headers = {"key","xpath"};
				table.initTable(headers);
				
				for(Map.Entry<String,String> e: this.fieldMapping.entrySet()){
					Object[] row= new Object[2];
					row[0]=e.getKey();
					row[1]=e.getValue();
					table.rows().add(row);
				}
								
				return this.representTable(table, this.overrideVariant(variant), null);
			}else{
				return null;				
			}
		} catch (Exception e) {
			logger.error("",e);
			return null;
		}
	}
}
