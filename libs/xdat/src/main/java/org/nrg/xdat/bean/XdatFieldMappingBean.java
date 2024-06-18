/*
 * core: org.nrg.xdat.bean.XdatFieldMappingBean
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
public class XdatFieldMappingBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatFieldMappingI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatFieldMappingBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:field_mapping";

	public String getSchemaElementName(){
		return "field_mapping";
	}

	public String getFullSchemaElementName(){
		return "xdat:field_mapping";
	}

	//FIELD

	private String _Field=null;

	/**
	 * @return Returns the field.
	 */
	public String getField(){
		return _Field;
	}

	/**
	 * Sets the value for field.
	 * @param v Value to Set.
	 */
	public void setField(String v){
		_Field=v;
	}

	//FIELD

	private String _FieldValue=null;

	/**
	 * @return Returns the field_value.
	 */
	public String getFieldValue(){
		return _FieldValue;
	}

	/**
	 * Sets the value for field_value.
	 * @param v Value to Set.
	 */
	public void setFieldValue(String v){
		_FieldValue=v;
	}

	//FIELD

	private Boolean _CreateElement=null;

	/**
	 * @return Returns the create_element.
	 */
	public Boolean getCreateElement() {
		return _CreateElement;
	}

	/**
	 * Sets the value for create_element.
	 * @param v Value to Set.
	 */
	public void setCreateElement(Object v){
		if(v instanceof Boolean){
			_CreateElement=(Boolean)v;
		}else if(v instanceof String){
			_CreateElement=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _ReadElement=null;

	/**
	 * @return Returns the read_element.
	 */
	public Boolean getReadElement() {
		return _ReadElement;
	}

	/**
	 * Sets the value for read_element.
	 * @param v Value to Set.
	 */
	public void setReadElement(Object v){
		if(v instanceof Boolean){
			_ReadElement=(Boolean)v;
		}else if(v instanceof String){
			_ReadElement=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _EditElement=null;

	/**
	 * @return Returns the edit_element.
	 */
	public Boolean getEditElement() {
		return _EditElement;
	}

	/**
	 * Sets the value for edit_element.
	 * @param v Value to Set.
	 */
	public void setEditElement(Object v){
		if(v instanceof Boolean){
			_EditElement=(Boolean)v;
		}else if(v instanceof String){
			_EditElement=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _DeleteElement=null;

	/**
	 * @return Returns the delete_element.
	 */
	public Boolean getDeleteElement() {
		return _DeleteElement;
	}

	/**
	 * Sets the value for delete_element.
	 * @param v Value to Set.
	 */
	public void setDeleteElement(Object v){
		if(v instanceof Boolean){
			_DeleteElement=(Boolean)v;
		}else if(v instanceof String){
			_DeleteElement=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _ActiveElement=null;

	/**
	 * @return Returns the active_element.
	 */
	public Boolean getActiveElement() {
		return _ActiveElement;
	}

	/**
	 * Sets the value for active_element.
	 * @param v Value to Set.
	 */
	public void setActiveElement(Object v){
		if(v instanceof Boolean){
			_ActiveElement=(Boolean)v;
		}else if(v instanceof String){
			_ActiveElement=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private String _ComparisonType=null;

	/**
	 * @return Returns the comparison_type.
	 */
	public String getComparisonType(){
		return _ComparisonType;
	}

	/**
	 * Sets the value for comparison_type.
	 * @param v Value to Set.
	 */
	public void setComparisonType(String v){
		_ComparisonType=v;
	}

	//FIELD

	private Integer _XdatFieldMappingId=null;

	/**
	 * @return Returns the xdat_field_mapping_id.
	 */
	public Integer getXdatFieldMappingId() {
		return _XdatFieldMappingId;
	}

	/**
	 * Sets the value for xdat_field_mapping_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingId(Integer v){
		_XdatFieldMappingId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("field")){
			setField(v);
		}else if (xmlPath.equals("field_value")){
			setFieldValue(v);
		}else if (xmlPath.equals("create_element")){
			setCreateElement(v);
		}else if (xmlPath.equals("read_element")){
			setReadElement(v);
		}else if (xmlPath.equals("edit_element")){
			setEditElement(v);
		}else if (xmlPath.equals("delete_element")){
			setDeleteElement(v);
		}else if (xmlPath.equals("active_element")){
			setActiveElement(v);
		}else if (xmlPath.equals("comparison_type")){
			setComparisonType(v);
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
		if (xmlPath.equals("field")){
			return getField();
		}else if (xmlPath.equals("field_value")){
			return getFieldValue();
		}else if (xmlPath.equals("create_element")){
			return getCreateElement();
		}else if (xmlPath.equals("read_element")){
			return getReadElement();
		}else if (xmlPath.equals("edit_element")){
			return getEditElement();
		}else if (xmlPath.equals("delete_element")){
			return getDeleteElement();
		}else if (xmlPath.equals("active_element")){
			return getActiveElement();
		}else if (xmlPath.equals("comparison_type")){
			return getComparisonType();
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
		if (xmlPath.equals("field")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("field_value")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("create_element")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("read_element")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("edit_element")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("delete_element")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("active_element")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("comparison_type")){
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
		all_fields.add("field");
		all_fields.add("field_value");
		all_fields.add("create_element");
		all_fields.add("read_element");
		all_fields.add("edit_element");
		all_fields.add("delete_element");
		all_fields.add("active_element");
		all_fields.add("comparison_type");
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
		writer.write("\n<xdat:field_mapping");
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
		writer.write("\n</xdat:field_mapping>");
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
		if (_Field!=null)
			map.put("field",ValueParser(_Field,"string"));
		else map.put("field","");//REQUIRED FIELD

		if (_FieldValue!=null)
			map.put("field_value",ValueParser(_FieldValue,"string"));
		else map.put("field_value","");//REQUIRED FIELD

		if (_CreateElement!=null)
			map.put("create_element",ValueParser(_CreateElement,"boolean"));
		//NOT REQUIRED FIELD

		if (_ReadElement!=null)
			map.put("read_element",ValueParser(_ReadElement,"boolean"));
		//NOT REQUIRED FIELD

		if (_EditElement!=null)
			map.put("edit_element",ValueParser(_EditElement,"boolean"));
		//NOT REQUIRED FIELD

		if (_DeleteElement!=null)
			map.put("delete_element",ValueParser(_DeleteElement,"boolean"));
		//NOT REQUIRED FIELD

		if (_ActiveElement!=null)
			map.put("active_element",ValueParser(_ActiveElement,"boolean"));
		//NOT REQUIRED FIELD

		if (_ComparisonType!=null)
			map.put("comparison_type",ValueParser(_ComparisonType,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
