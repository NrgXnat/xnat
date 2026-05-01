/*
 * core: org.nrg.xdat.bean.XdatAccessLogBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
public class XdatAccessLogBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatAccessLogI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatAccessLogBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:access_log";

	public String getSchemaElementName(){
		return "access_log";
	}

	public String getFullSchemaElementName(){
		return "xdat:access_log";
	}

	//FIELD

	private String _Login=null;

	/**
	 * @return Returns the login.
	 */
	public String getLogin(){
		return _Login;
	}

	/**
	 * Sets the value for login.
	 * @param v Value to Set.
	 */
	public void setLogin(String v){
		_Login=v;
	}

	//FIELD

	private Date _AccessDate=null;

	/**
	 * @return Returns the access_date.
	 */
	public Date getAccessDate(){
		return _AccessDate;
	}

	/**
	 * Sets the value for access_date.
	 * @param v Value to Set.
	 */
	public void setAccessDate(Date v){
		_AccessDate=v;
	}

	/**
	 * Sets the value for access_date.
	 * @param v Value to Set.
	 */
	public void setAccessDate(Object v){
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the value for access_date.
	 * @param v Value to Set.
	 */
	public void setAccessDate(String v)  {
		_AccessDate=formatDateTime(v);
	}

	//FIELD

	private String _Ip=null;

	/**
	 * @return Returns the ip.
	 */
	public String getIp(){
		return _Ip;
	}

	/**
	 * Sets the value for ip.
	 * @param v Value to Set.
	 */
	public void setIp(String v){
		_Ip=v;
	}

	//FIELD

	private String _Method=null;

	/**
	 * @return Returns the method.
	 */
	public String getMethod(){
		return _Method;
	}

	/**
	 * Sets the value for method.
	 * @param v Value to Set.
	 */
	public void setMethod(String v){
		_Method=v;
	}

	//FIELD

	private Integer _XdatAccessLogId=null;

	/**
	 * @return Returns the xdat_access_log_id.
	 */
	public Integer getXdatAccessLogId() {
		return _XdatAccessLogId;
	}

	/**
	 * Sets the value for xdat_access_log_id.
	 * @param v Value to Set.
	 */
	public void setXdatAccessLogId(Integer v){
		_XdatAccessLogId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login")){
			setLogin(v);
		}else if (xmlPath.equals("access_date")){
			setAccessDate(v);
		}else if (xmlPath.equals("ip")){
			setIp(v);
		}else if (xmlPath.equals("method")){
			setMethod(v);
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
		if (xmlPath.equals("login")){
			return getLogin();
		}else if (xmlPath.equals("access_date")){
			return getAccessDate();
		}else if (xmlPath.equals("ip")){
			return getIp();
		}else if (xmlPath.equals("method")){
			return getMethod();
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
		if (xmlPath.equals("login")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("access_date")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("ip")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("method")){
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
		all_fields.add("login");
		all_fields.add("access_date");
		all_fields.add("ip");
		all_fields.add("method");
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
		writer.write("\n<xdat:access_log");
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
		writer.write("\n</xdat:access_log>");
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
		if (_Login!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:login");
			writer.write(">");
			writer.write(ValueParser(_Login,"string"));
			writer.write("</xdat:login>");
			header--;
		}
		if (_AccessDate!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:access_date");
			writer.write(">");
			writer.write(ValueParser(_AccessDate,"dateTime"));
			writer.write("</xdat:access_date>");
			header--;
		}
		if (_Ip!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:ip");
			writer.write(">");
			writer.write(ValueParser(_Ip,"string"));
			writer.write("</xdat:ip>");
			header--;
		}
		if (_Method!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:method");
			writer.write(">");
			writer.write(ValueParser(_Method,"string"));
			writer.write("</xdat:method>");
			header--;
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_Login!=null) return true;
		if (_AccessDate!=null) return true;
		if (_Ip!=null) return true;
		if (_Method!=null) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
