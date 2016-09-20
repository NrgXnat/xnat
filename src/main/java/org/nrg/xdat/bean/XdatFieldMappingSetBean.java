/*
 * core: org.nrg.xdat.bean.XdatFieldMappingSetBean
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
public class XdatFieldMappingSetBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatFieldMappingSetI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatFieldMappingSetBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:field_mapping_set";

	public String getSchemaElementName(){
		return "field_mapping_set";
	}

	public String getFullSchemaElementName(){
		return "xdat:field_mapping_set";
	}
	 private List<org.nrg.xdat.bean.XdatFieldMappingBean> _Allow =new ArrayList<org.nrg.xdat.bean.XdatFieldMappingBean>();

	/**
	 * allow
	 * @return Returns an List of org.nrg.xdat.bean.XdatFieldMappingBean
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingI> List<A> getAllow() {
		return (List<A>) _Allow;
	}

	/**
	 * Sets the value for allow.
	 * @param v Value to Set.
	 */
	public void setAllow(ArrayList<org.nrg.xdat.bean.XdatFieldMappingBean> v){
		_Allow=v;
	}

	/**
	 * Adds the value for allow.
	 * @param v Value to Set.
	 */
	public void addAllow(org.nrg.xdat.bean.XdatFieldMappingBean v){
		_Allow.add(v);
	}

	/**
	 * allow
	 * Adds org.nrg.xdat.model.XdatFieldMappingI
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingI> void addAllow(A item) throws Exception{
	_Allow.add((org.nrg.xdat.bean.XdatFieldMappingBean)item);
	}

	/**
	 * Adds the value for allow.
	 * @param v Value to Set.
	 */
	public void addAllow(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatFieldMappingBean)
			_Allow.add((org.nrg.xdat.bean.XdatFieldMappingBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatFieldMappingBean");
	}
	 private List<org.nrg.xdat.bean.XdatFieldMappingSetBean> _SubSet =new ArrayList<org.nrg.xdat.bean.XdatFieldMappingSetBean>();

	/**
	 * sub_set
	 * @return Returns an List of org.nrg.xdat.bean.XdatFieldMappingSetBean
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> List<A> getSubSet() {
		return (List<A>) _SubSet;
	}

	/**
	 * Sets the value for sub_set.
	 * @param v Value to Set.
	 */
	public void setSubSet(ArrayList<org.nrg.xdat.bean.XdatFieldMappingSetBean> v){
		_SubSet=v;
	}

	/**
	 * Adds the value for sub_set.
	 * @param v Value to Set.
	 */
	public void addSubSet(org.nrg.xdat.bean.XdatFieldMappingSetBean v){
		_SubSet.add(v);
	}

	/**
	 * sub_set
	 * Adds org.nrg.xdat.model.XdatFieldMappingSetI
	 */
	public <A extends org.nrg.xdat.model.XdatFieldMappingSetI> void addSubSet(A item) throws Exception{
	_SubSet.add((org.nrg.xdat.bean.XdatFieldMappingSetBean)item);
	}

	/**
	 * Adds the value for sub_set.
	 * @param v Value to Set.
	 */
	public void addSubSet(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatFieldMappingSetBean)
			_SubSet.add((org.nrg.xdat.bean.XdatFieldMappingSetBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatFieldMappingSetBean");
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

	private Integer _XdatFieldMappingSetId=null;

	/**
	 * @return Returns the xdat_field_mapping_set_id.
	 */
	public Integer getXdatFieldMappingSetId() {
		return _XdatFieldMappingSetId;
	}

	/**
	 * Sets the value for xdat_field_mapping_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingSetId(Integer v){
		_XdatFieldMappingSetId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("method")){
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
		if (xmlPath.equals("allow")){
			addAllow(v);
		}else if (xmlPath.equals("sub_set")){
			addSubSet(v);
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
		if (xmlPath.equals("method")){
			return getMethod();
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
		if (xmlPath.equals("allow")){
			return getAllow();
		}else if (xmlPath.equals("sub_set")){
			return getSubSet();
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
		if (xmlPath.equals("allow")){
			return "http://nrg.wustl.edu/security:field_mapping";
		}else if (xmlPath.equals("sub_set")){
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
		if (xmlPath.equals("allow")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("sub_set")){
			return BaseElement.field_multi_reference;
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
		all_fields.add("allow");
		all_fields.add("sub_set");
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
		writer.write("\n<xdat:field_mapping_set");
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
		writer.write("\n</xdat:field_mapping_set>");
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
		if (_Method!=null)
			map.put("method",ValueParser(_Method,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		//REFERENCE FROM field_mapping_set -> allow
		java.util.Iterator iter0=_Allow.iterator();
		while(iter0.hasNext()){
			org.nrg.xdat.bean.XdatFieldMappingBean child = (org.nrg.xdat.bean.XdatFieldMappingBean)iter0.next();
			writer.write("\n" + createHeader(header++) + "<xdat:allow");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:field_mapping")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return1 =child.addXMLBody(writer,header);
				if(return1){
					writer.write("\n" + createHeader(--header) + "</xdat:allow>");
				}else{
					writer.write("</xdat:allow>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//REFERENCE FROM field_mapping_set -> sub_set
		java.util.Iterator iter1=_SubSet.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatFieldMappingSetBean child = (org.nrg.xdat.bean.XdatFieldMappingSetBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:sub_set");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:field_mapping_set")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:sub_set>");
				}else{
					writer.write("</xdat:sub_set>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if(_Allow.size()>0) return true;
		if(_SubSet.size()>0) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
