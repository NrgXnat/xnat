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
public class XdatCriteriaBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatCriteriaI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatCriteriaBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:criteria";

	public String getSchemaElementName(){
		return "criteria";
	}

	public String getFullSchemaElementName(){
		return "xdat:criteria";
	}

	//FIELD

	private String _SchemaField=null;

	/**
	 * @return Returns the schema_field.
	 */
	public String getSchemaField(){
		return _SchemaField;
	}

	/**
	 * Sets the value for schema_field.
	 * @param v Value to Set.
	 */
	public void setSchemaField(String v){
		_SchemaField=v;
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

	private String _CustomSearch=null;

	/**
	 * @return Returns the custom_search.
	 */
	public String getCustomSearch(){
		return _CustomSearch;
	}

	/**
	 * Sets the value for custom_search.
	 * @param v Value to Set.
	 */
	public void setCustomSearch(String v){
		_CustomSearch=v;
	}

	//FIELD

	private String _Value=null;

	/**
	 * @return Returns the value.
	 */
	public String getValue(){
		return _Value;
	}

	/**
	 * Sets the value for value.
	 * @param v Value to Set.
	 */
	public void setValue(String v){
		_Value=v;
	}

	//FIELD

	private Boolean _OverrideValueFormatting=null;

	/**
	 * @return Returns the override_value_formatting.
	 */
	public Boolean getOverrideValueFormatting() {
		return _OverrideValueFormatting;
	}

	/**
	 * Sets the value for override_value_formatting.
	 * @param v Value to Set.
	 */
	public void setOverrideValueFormatting(Object v){
		if(v instanceof Boolean){
			_OverrideValueFormatting=(Boolean)v;
		}else if(v instanceof String){
			_OverrideValueFormatting=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Integer _XdatCriteriaId=null;

	/**
	 * @return Returns the xdat_criteria_id.
	 */
	public Integer getXdatCriteriaId() {
		return _XdatCriteriaId;
	}

	/**
	 * Sets the value for xdat_criteria_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaId(Integer v){
		_XdatCriteriaId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("schema_field")){
			setSchemaField(v);
		}else if (xmlPath.equals("comparison_type")){
			setComparisonType(v);
		}else if (xmlPath.equals("custom_search")){
			setCustomSearch(v);
		}else if (xmlPath.equals("value")){
			setValue(v);
		}else if (xmlPath.equals("override_value_formatting")){
			setOverrideValueFormatting(v);
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
	 * @param v Value to Set.
	 */
	public Object getDataFieldValue(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("schema_field")){
			return getSchemaField();
		}else if (xmlPath.equals("comparison_type")){
			return getComparisonType();
		}else if (xmlPath.equals("custom_search")){
			return getCustomSearch();
		}else if (xmlPath.equals("value")){
			return getValue();
		}else if (xmlPath.equals("override_value_formatting")){
			return getOverrideValueFormatting();
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
			return super.getReferenceField(xmlPath);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public String getReferenceFieldName(String xmlPath) throws BaseElement.UnknownFieldException{
			return super.getReferenceFieldName(xmlPath);
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("schema_field")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("comparison_type")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("custom_search")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("value")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("override_value_formatting")){
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
		all_fields.add("schema_field");
		all_fields.add("comparison_type");
		all_fields.add("custom_search");
		all_fields.add("value");
		all_fields.add("override_value_formatting");
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
		writer.write("\n<xdat:criteria");
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
		writer.write("\n</xdat:criteria>");
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
		if (_OverrideValueFormatting!=null)
			map.put("override_value_formatting",ValueParser(_OverrideValueFormatting,"boolean"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		if (_SchemaField!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:schema_field");
			writer.write(">");
			writer.write(ValueParser(_SchemaField,"string"));
			writer.write("</xdat:schema_field>");
			header--;
		}
		if (_ComparisonType!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:comparison_type");
			writer.write(">");
			writer.write(ValueParser(_ComparisonType,"string"));
			writer.write("</xdat:comparison_type>");
			header--;
		}
		if (_CustomSearch!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:custom_search");
			writer.write(">");
			writer.write(ValueParser(_CustomSearch,"string"));
			writer.write("</xdat:custom_search>");
			header--;
		}
		if (_Value!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:value");
			writer.write(">");
			writer.write(ValueParser(_Value,"string"));
			writer.write("</xdat:value>");
			header--;
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_SchemaField!=null) return true;
		if (_ComparisonType!=null) return true;
		if (_CustomSearch!=null) return true;
		if (_Value!=null) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
