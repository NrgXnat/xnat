/*
 * core: org.nrg.xdat.bean.XdatUserLoginBean
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
public class XdatUserLoginBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatUserLoginI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatUserLoginBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:user_login";

	public String getSchemaElementName(){
		return "user_login";
	}

	public String getFullSchemaElementName(){
		return "xdat:user_login";
	}

	//FIELD

	private Date _LoginDate=null;

	/**
	 * @return Returns the login_date.
	 */
	public Date getLoginDate(){
		return _LoginDate;
	}

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
	public void setLoginDate(Date v){
		_LoginDate=v;
	}

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
	public void setLoginDate(Object v){
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
	public void setLoginDate(String v)  {
		_LoginDate=formatDateTime(v);
	}

	//FIELD

	private Date _LogoutDate=null;

	/**
	 * @return Returns the logout_date.
	 */
	public Date getLogoutDate(){
		return _LogoutDate;
	}

	/**
	 * Sets the value for logout_date.
	 * @param v Value to Set.
	 */
	public void setLogoutDate(Date v){
		_LogoutDate=v;
	}

	/**
	 * Sets the value for logout_date.
	 * @param v Value to Set.
	 */
	public void setLogoutDate(Object v){
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the value for logout_date.
	 * @param v Value to Set.
	 */
	public void setLogoutDate(String v)  {
		_LogoutDate=formatDateTime(v);
	}

	//FIELD

	private String _SessionId=null;

	/**
	 * @return Returns the session_id.
	 */
	public String getSessionId(){
		return _SessionId;
	}

	/**
	 * Sets the value for session_id.
	 * @param v Value to Set.
	 */
	public void setSessionId(String v){
		_SessionId=v;
	}

	//FIELD

	private String _IpAddress=null;

	/**
	 * @return Returns the ip_address.
	 */
	public String getIpAddress(){
		return _IpAddress;
	}

	/**
	 * Sets the value for ip_address.
	 * @param v Value to Set.
	 */
	public void setIpAddress(String v){
		_IpAddress=v;
	}
	 private org.nrg.xdat.bean.XdatUserBean _userProperty =null;

	/**
	 * user
	 * @return org.nrg.xdat.bean.XdatUserBean
	 */
	public org.nrg.xdat.bean.XdatUserBean getUserProperty() {
		return _userProperty;
	}

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
	 */
	public void setUserProperty(org.nrg.xdat.bean.XdatUserBean v){
		_userProperty =v;
	}

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
	 */
	public void setUserProperty(Object v) {
		if (v instanceof org.nrg.xdat.bean.XdatUserBean)
			_userProperty =(org.nrg.xdat.bean.XdatUserBean)v;
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatUserBean");
	}

	/**
	 * user
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> void setUserProperty(A item) throws Exception{
	setUserProperty((org.nrg.xdat.bean.XdatUserBean)item);
	}

	//FIELD

	private Integer _userPropertyFK=null;

	/**
	 * @return Returns the xdat:user_login/user_xdat_user_id.
	 */
	public Integer getUserPropertyFK(){
		return _userPropertyFK;
	}

	/**
	 * Sets the value for xdat:user_login/user_xdat_user_id.
	 * @param v Value to Set.
	 */
	public void setuserPropertyFK(Integer v) {
		_userPropertyFK=v;
	}

	//FIELD

	private Integer _XdatUserLoginId=null;

	/**
	 * @return Returns the xdat_user_login_id.
	 */
	public Integer getXdatUserLoginId() {
		return _XdatUserLoginId;
	}

	/**
	 * Sets the value for xdat_user_login_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserLoginId(Integer v){
		_XdatUserLoginId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login_date")){
			setLoginDate(v);
		}else if (xmlPath.equals("logout_date")){
			setLogoutDate(v);
		}else if (xmlPath.equals("session_id")){
			setSessionId(v);
		}else if (xmlPath.equals("ip_address")){
			setIpAddress(v);
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
		if (xmlPath.equals("user")){
			setUserProperty(v);
		}
		else{
			super.setReferenceField(xmlPath,v);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param xmlPath XML path to check.
	 */
	public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login_date")){
			return getLoginDate();
		}else if (xmlPath.equals("logout_date")){
			return getLogoutDate();
		}else if (xmlPath.equals("session_id")){
			return getSessionId();
		}else if (xmlPath.equals("ip_address")){
			return getIpAddress();
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
		if (xmlPath.equals("user")){
			return getUserProperty();
		}
		else{
			return super.getReferenceField(xmlPath);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param xmlPath XML path to check.
	 */
	public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("user")){
			return "http://nrg.wustl.edu/security:user";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login_date")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("logout_date")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("session_id")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("ip_address")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("user")){
			return BaseElement.field_single_reference;
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
		all_fields.add("login_date");
		all_fields.add("logout_date");
		all_fields.add("session_id");
		all_fields.add("ip_address");
		all_fields.add("user");
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
		writer.write("\n<xdat:user_login");
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
		writer.write("\n</xdat:user_login>");
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
		if (_LoginDate!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:login_date");
			writer.write(">");
			writer.write(ValueParser(_LoginDate,"dateTime"));
			writer.write("</xdat:login_date>");
			header--;
		}
		if (_LogoutDate!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:logout_date");
			writer.write(">");
			writer.write(ValueParser(_LogoutDate,"dateTime"));
			writer.write("</xdat:logout_date>");
			header--;
		}
		if (_SessionId!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:session_id");
			writer.write(">");
			writer.write(ValueParser(_SessionId,"string"));
			writer.write("</xdat:session_id>");
			header--;
		}
		if (_IpAddress!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:ip_address");
			writer.write(">");
			writer.write(ValueParser(_IpAddress,"string"));
			writer.write("</xdat:ip_address>");
			header--;
		}
		//REFERENCE FROM user_login -> user
		//DATA-FIELD FROM user_login -> user
		if (_userProperty!=null){
		//NEW ELEMENT
			writer.write("\n" + createHeader(header++) + "<xdat:user");
			_userProperty.addXMLAtts(writer);
			if(!_userProperty.getFullSchemaElementName().equals("xdat:user")){
				writer.write(" xsi:type=\"" + _userProperty.getFullSchemaElementName() + "\"");
			}
			if (_userProperty.hasXMLBodyContent()){
				writer.write(">");
				boolean return0 =_userProperty.addXMLBody(writer,header);
				if(return0){
					writer.write("\n" + createHeader(--header) + "</xdat:user>");
				}else{
					writer.write("</xdat:user>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		else{
			writer.write("\n" + createHeader(header) + "<xdat:user/>");//REQUIRED
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_LoginDate!=null) return true;
		if (_LogoutDate!=null) return true;
		if (_SessionId!=null) return true;
		if (_IpAddress!=null) return true;
		if (_userProperty!=null){
			if (_userProperty.hasXMLBodyContent()) return true;
		}
		return true;//REQUIRED user
	}
}
