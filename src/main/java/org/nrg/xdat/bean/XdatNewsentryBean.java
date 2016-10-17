/*
 * core: org.nrg.xdat.bean.XdatNewsentryBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Thu Mar 31 18:38:30 CDT 2016
 *
 */
package org.nrg.xdat.bean;
import org.nrg.xdat.bean.base.BaseElement;

import java.util.*;

/**
 * @author XDAT
 *
 *//*
 ******************************** 
 * DO NOT MODIFY THIS FILE 
 *
 ********************************/
@SuppressWarnings({"unchecked","rawtypes"})
public class XdatNewsentryBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatNewsentryI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatNewsentryBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:newsEntry";

	public String getSchemaElementName(){
		return "newsEntry";
	}

	public String getFullSchemaElementName(){
		return "xdat:newsEntry";
	}

	//FIELD

	private Date _Date=null;

	/**
	 * @return Returns the date.
	 */
	public Date getDate(){
		return _Date;
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Date v){
		_Date=v;
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Object v){
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(String v)  {
		_Date=formatDateTime(v);
	}

	//FIELD

	private String _Title=null;

	/**
	 * @return Returns the title.
	 */
	public String getTitle(){
		return _Title;
	}

	/**
	 * Sets the value for title.
	 * @param v Value to Set.
	 */
	public void setTitle(String v){
		_Title=v;
	}

	//FIELD

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		return _Description;
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		_Description=v;
	}

	//FIELD

	private String _Link=null;

	/**
	 * @return Returns the link.
	 */
	public String getLink(){
		return _Link;
	}

	/**
	 * Sets the value for link.
	 * @param v Value to Set.
	 */
	public void setLink(String v){
		_Link=v;
	}

	//FIELD

	private Integer _XdatNewsentryId=null;

	/**
	 * @return Returns the xdat_newsEntry_id.
	 */
	public Integer getXdatNewsentryId() {
		return _XdatNewsentryId;
	}

	/**
	 * Sets the value for xdat_newsEntry_id.
	 * @param v Value to Set.
	 */
	public void setXdatNewsentryId(Integer v){
		_XdatNewsentryId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("date")){
			setDate(v);
		}else if (xmlPath.equals("title")){
			setTitle(v);
		}else if (xmlPath.equals("description")){
			setDescription(v);
		}else if (xmlPath.equals("link")){
			setLink(v);
		}
		else{
			super.setDataField(xmlPath,v);
		}
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setReferenceField(String xmlPath,BaseElement v) throws BaseElement.UnknownFieldException{
			super.setReferenceField(xmlPath,v);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param xmlPath XML path to check.
	 */
	public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("date")){
			return getDate();
		}else if (xmlPath.equals("title")){
			return getTitle();
		}else if (xmlPath.equals("description")){
			return getDescription();
		}else if (xmlPath.equals("link")){
			return getLink();
		}
		else{
			return super.getDataFieldValue(xmlPath);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param xmlPath XML path to check.
	 */
	public Object getReferenceField(String xmlPath) throws BaseElement.UnknownFieldException{
			return super.getReferenceField(xmlPath);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param xmlPath XML path to check.
	 */
	public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{
			return super.getReferenceFieldName(xmlPath);
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("date")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("title")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("description")){
			return BaseElement.field_LONG_DATA;
		}else if (xmlPath.equals("link")){
			return BaseElement.field_data;
		}
		else{
			return super.getFieldType(xmlPath);
		}
	}

	/**
	 * Returns arraylist of all fields
	 */
	public ArrayList getAllFields() {
		ArrayList all_fields=new ArrayList();
		all_fields.add("date");
		all_fields.add("title");
		all_fields.add("description");
		all_fields.add("link");
		all_fields.addAll(super.getAllFields());
		return all_fields;
	}


	public String toString(){
		java.io.StringWriter sw = new java.io.StringWriter();
		try{this.toXML(sw,true);}catch(java.io.IOException e){}
		return sw.toString();
	}


	public void toXML(java.io.Writer writer,boolean prettyPrint) throws java.io.IOException{
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		writer.write("\n<xdat:News");
		TreeMap map = new TreeMap();
		map.putAll(getXMLAtts());
		map.put("xmlns:arc","http://nrg.wustl.edu/arc");
		map.put("xmlns:cat","http://nrg.wustl.edu/catalog");
		map.put("xmlns:pipe","http://nrg.wustl.edu/pipe");
		map.put("xmlns:prov","http://www.nbirn.net/prov");
		map.put("xmlns:scr","http://nrg.wustl.edu/scr");
		map.put("xmlns:val","http://nrg.wustl.edu/val");
		map.put("xmlns:wrk","http://nrg.wustl.edu/workflow");
		map.put("xmlns:xdat","http://nrg.wustl.edu/security");
		map.put("xmlns:xnat","http://nrg.wustl.edu/xnat");
		map.put("xmlns:xnat_a","http://nrg.wustl.edu/xnat_assessments");
		map.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		java.util.Iterator iter =map.keySet().iterator();
		while(iter.hasNext()){
			String key = (String)iter.next();
			writer.write(" " + key + "=\"" + map.get(key) + "\"");
		}
		int header = 0;
		if (prettyPrint)header++;
		writer.write(">");
		addXMLBody(writer,header);
		if (prettyPrint)header--;
		writer.write("\n</xdat:News>");
	}


	protected void addXMLAtts(java.io.Writer writer) throws java.io.IOException{
		TreeMap map = this.getXMLAtts();
		java.util.Iterator iter =map.keySet().iterator();
		while(iter.hasNext()){
			String key = (String)iter.next();
			writer.write(" " + key + "=\"" + map.get(key) + "\"");
		}
	}


	protected TreeMap getXMLAtts() {
		TreeMap map = super.getXMLAtts();
		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		if (_Date!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:date");
			writer.write(">");
			writer.write(ValueParser(_Date,"dateTime"));
			writer.write("</xdat:date>");
			header--;
		}
		if (_Title!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:title");
			writer.write(">");
			writer.write(ValueParser(_Title,"string"));
			writer.write("</xdat:title>");
			header--;
		}
		if (_Description!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:description");
			writer.write(">");
			writer.write(ValueParser(_Description,"string"));
			writer.write("</xdat:description>");
			header--;
		}
		if (_Link!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:link");
			writer.write(">");
			writer.write(ValueParser(_Link,"string"));
			writer.write("</xdat:link>");
			header--;
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_Date!=null) return true;
		if (_Title!=null) return true;
		if (_Description!=null) return true;
		if (_Link!=null) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
