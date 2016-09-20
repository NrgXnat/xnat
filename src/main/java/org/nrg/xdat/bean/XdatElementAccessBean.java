/*
 * core: org.nrg.xdat.bean.XdatElementAccessBean
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
public class XdatElementAccessBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatElementAccessI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatElementAccessBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:element_access";

	public String getSchemaElementName(){
		return "element_access";
	}

	public String getFullSchemaElementName(){
		return "xdat:element_access";
	}

	//FIELD

	private String _SecondaryPassword=null;

	/**
	 * @return Returns the secondary_password.
	 */
	public String getSecondaryPassword(){
		return _SecondaryPassword;
	}

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(String v){
		_SecondaryPassword=v;
	}

	//FIELD

	private Boolean _SecondaryPassword_encrypt=null;

	/**
	 * @return Returns the secondary_password/encrypt.
	 */
	public Boolean getSecondaryPassword_encrypt() {
		return _SecondaryPassword_encrypt;
	}

	/**
	 * Sets the value for secondary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword_encrypt(Object v){
		if(v instanceof Boolean){
			_SecondaryPassword_encrypt=(Boolean)v;
		}else if(v instanceof String){
			_SecondaryPassword_encrypt=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}
	 private List<org.nrg.xdat.bean.XdatElementAccessSecureIpBean> _SecureIp =new ArrayList<org.nrg.xdat.bean.XdatElementAccessSecureIpBean>();

	/**
	 * secure_ip
	 * @return Returns an List of org.nrg.xdat.bean.XdatElementAccessSecureIpBean
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessSecureIpI> List<A> getSecureIp() {
		return (List<A>) _SecureIp;
	}

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(ArrayList<org.nrg.xdat.bean.XdatElementAccessSecureIpBean> v){
		_SecureIp=v;
	}

	/**
	 * Adds the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void addSecureIp(org.nrg.xdat.bean.XdatElementAccessSecureIpBean v){
		_SecureIp.add(v);
	}

	/**
	 * secure_ip
	 * Adds org.nrg.xdat.model.XdatElementAccessSecureIpI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessSecureIpI> void addSecureIp(A item) throws Exception{
	_SecureIp.add((org.nrg.xdat.bean.XdatElementAccessSecureIpBean)item);
	}

	/**
	 * Adds the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void addSecureIp(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatElementAccessSecureIpBean)
			_SecureIp.add((org.nrg.xdat.bean.XdatElementAccessSecureIpBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatElementAccessSecureIpBean");
	}
	 private List<org.nrg.xdat.bean.XdatFieldMappingSetBean> _Permissions_allowSet =new ArrayList<org.nrg.xdat.bean.XdatFieldMappingSetBean>();

	/**
	 * permissions/allow_set
	 * @return Returns an List of org.nrg.xdat.bean.XdatFieldMappingSetBean
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> List<A> getPermissions_allowSet() {
		return (List<A>) _Permissions_allowSet;
	}

	/**
	 * Sets the value for permissions/allow_set.
	 * @param v Value to Set.
	 */
	public void setPermissions_allowSet(ArrayList<org.nrg.xdat.bean.XdatFieldMappingSetBean> v){
		_Permissions_allowSet=v;
	}

	/**
	 * Adds the value for permissions/allow_set.
	 * @param v Value to Set.
	 */
	public void addPermissions_allowSet(org.nrg.xdat.bean.XdatFieldMappingSetBean v){
		_Permissions_allowSet.add(v);
	}

	/**
	 * permissions/allow_set
	 * Adds org.nrg.xdat.model.XdatFieldMappingSetI
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> void addPermissions_allowSet(A item) throws Exception{
	_Permissions_allowSet.add((org.nrg.xdat.bean.XdatFieldMappingSetBean)item);
	}

	/**
	 * Adds the value for permissions/allow_set.
	 * @param v Value to Set.
	 */
	public void addPermissions_allowSet(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatFieldMappingSetBean)
			_Permissions_allowSet.add((org.nrg.xdat.bean.XdatFieldMappingSetBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatFieldMappingSetBean");
	}

	//FIELD

	private String _ElementName=null;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName(){
		return _ElementName;
	}

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v){
		_ElementName=v;
	}

	//FIELD

	private Integer _XdatElementAccessId=null;

	/**
	 * @return Returns the xdat_element_access_id.
	 */
	public Integer getXdatElementAccessId() {
		return _XdatElementAccessId;
	}

	/**
	 * Sets the value for xdat_element_access_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementAccessId(Integer v){
		_XdatElementAccessId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("secondary_password")){
			setSecondaryPassword(v);
		}else if (xmlPath.equals("secondary_password/encrypt")){
			setSecondaryPassword_encrypt(v);
		}else if (xmlPath.equals("element_name")){
			setElementName(v);
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
		if (xmlPath.equals("secure_ip")){
			addSecureIp(v);
		}else if (xmlPath.equals("permissions/allow_set")){
			addPermissions_allowSet(v);
		}
		else{
			super.setReferenceField(xmlPath,v);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("secondary_password")){
			return getSecondaryPassword();
		}else if (xmlPath.equals("secondary_password/encrypt")){
			return getSecondaryPassword_encrypt();
		}else if (xmlPath.equals("element_name")){
			return getElementName();
		}
		else{
			return super.getDataFieldValue(xmlPath);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public Object getReferenceField(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("secure_ip")){
			return getSecureIp();
		}else if (xmlPath.equals("permissions/allow_set")){
			return getPermissions_allowSet();
		}
		else{
			return super.getReferenceField(xmlPath);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("secure_ip")){
			return "http://nrg.wustl.edu/security:element_access_secure_ip";
		}else if (xmlPath.equals("permissions/allow_set")){
			return "http://nrg.wustl.edu/security:field_mapping_set";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("secondary_password")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secondary_password/encrypt")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_ip")){
			return BaseElement.field_inline_repeater;
		}else if (xmlPath.equals("permissions/allow_set")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("element_name")){
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
		all_fields.add("secondary_password");
		all_fields.add("secondary_password/encrypt");
		all_fields.add("secure_ip");
		all_fields.add("permissions/allow_set");
		all_fields.add("element_name");
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
		writer.write("\n<xdat:element_access");
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
		writer.write("\n</xdat:element_access>");
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
		if (_ElementName!=null)
			map.put("element_name",ValueParser(_ElementName,"string"));
		else map.put("element_name","");//REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		TreeMap SecondaryPasswordATTMap = new TreeMap();
		String SecondaryPasswordATT = new String();
		if (_SecondaryPassword_encrypt!=null)
			SecondaryPasswordATTMap.put("encrypt",ValueParser(_SecondaryPassword_encrypt,"boolean"));
		java.util.Iterator iter0 =SecondaryPasswordATTMap.keySet().iterator();
		while(iter0.hasNext()){
			String key = (String)iter0.next();
			SecondaryPasswordATT +=" " + key + "=\"" + SecondaryPasswordATTMap.get(key) + "\"";
		}
		if (_SecondaryPassword!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:secondary_password");
			writer.write(SecondaryPasswordATT);
			writer.write(">");
			writer.write(ValueParser(_SecondaryPassword,"string"));
			writer.write("</xdat:secondary_password>");
			header--;
		}
		else if(!SecondaryPasswordATT.equals("")){
			writer.write("\n" + createHeader(header++) + "<xdat:secondary_password");
			writer.write(SecondaryPasswordATT);
			writer.write("/>");
			header--;
		}

		//REFERENCE FROM element_access -> secure_ip
		//IN-LINE REPEATER
		java.util.Iterator iter1=_SecureIp.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatElementAccessSecureIpBean child = (org.nrg.xdat.bean.XdatElementAccessSecureIpBean)iter1.next();
			child.addXMLBody(writer,header);
		}
			int child2=0;
			int att2=0;
			child2+=_Permissions_allowSet.size();
			if(child2>0 || att2>0){
				writer.write("\n" + createHeader(header++) + "<xdat:permissions");
			if(child2==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM element_access -> permissions/allow_set
		java.util.Iterator iter3=_Permissions_allowSet.iterator();
		while(iter3.hasNext()){
			org.nrg.xdat.bean.XdatFieldMappingSetBean child = (org.nrg.xdat.bean.XdatFieldMappingSetBean)iter3.next();
			writer.write("\n" + createHeader(header++) + "<xdat:allow_set");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:field_mapping_set")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return4 =child.addXMLBody(writer,header);
				if(return4){
					writer.write("\n" + createHeader(--header) + "</xdat:allow_set>");
				}else{
					writer.write("</xdat:allow_set>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:permissions>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_SecondaryPassword_encrypt!=null)
			return true;
		if (_SecondaryPassword!=null) return true;
		if(_SecureIp.size()>0) return true;
			if(_Permissions_allowSet.size()>0)return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
