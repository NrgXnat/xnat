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
public class XdatSearchFieldBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatSearchFieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatSearchFieldBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:search_field";

	public String getSchemaElementName(){
		return "search_field";
	}

	public String getFullSchemaElementName(){
		return "xdat:search_field";
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

	private String _FieldId=null;

	/**
	 * @return Returns the field_ID.
	 */
	public String getFieldId(){
		return _FieldId;
	}

	/**
	 * Sets the value for field_ID.
	 * @param v Value to Set.
	 */
	public void setFieldId(String v){
		_FieldId=v;
	}

	//FIELD

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence(){
		return _Sequence;
	}

	/**
	 * Sets the value for xdat:search_field/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) {
		_Sequence=v;
	}

	/**
	 * Sets the value for xdat:search_field/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(String v)  {
		_Sequence=formatInteger(v);
	}

	//FIELD

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		return _Type;
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		_Type=v;
	}

	//FIELD

	private String _Header=null;

	/**
	 * @return Returns the header.
	 */
	public String getHeader(){
		return _Header;
	}

	/**
	 * Sets the value for header.
	 * @param v Value to Set.
	 */
	public void setHeader(String v){
		_Header=v;
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

	private Boolean _Visible=null;

	/**
	 * @return Returns the visible.
	 */
	public Boolean getVisible() {
		return _Visible;
	}

	/**
	 * Sets the value for visible.
	 * @param v Value to Set.
	 */
	public void setVisible(Object v){
		if(v instanceof Boolean){
			_Visible=(Boolean)v;
		}else if(v instanceof String){
			_Visible=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Integer _XdatSearchFieldId=null;

	/**
	 * @return Returns the xdat_search_field_id.
	 */
	public Integer getXdatSearchFieldId() {
		return _XdatSearchFieldId;
	}

	/**
	 * Sets the value for xdat_search_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatSearchFieldId(Integer v){
		_XdatSearchFieldId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("element_name")){
			setElementName(v);
		}else if (xmlPath.equals("field_ID")){
			setFieldId(v);
		}else if (xmlPath.equals("sequence")){
			setSequence(v);
		}else if (xmlPath.equals("type")){
			setType(v);
		}else if (xmlPath.equals("header")){
			setHeader(v);
		}else if (xmlPath.equals("value")){
			setValue(v);
		}else if (xmlPath.equals("visible")){
			setVisible(v);
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
		if (xmlPath.equals("element_name")){
			return getElementName();
		}else if (xmlPath.equals("field_ID")){
			return getFieldId();
		}else if (xmlPath.equals("sequence")){
			return getSequence();
		}else if (xmlPath.equals("type")){
			return getType();
		}else if (xmlPath.equals("header")){
			return getHeader();
		}else if (xmlPath.equals("value")){
			return getValue();
		}else if (xmlPath.equals("visible")){
			return getVisible();
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
		if (xmlPath.equals("element_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("field_ID")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("sequence")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("type")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("header")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("value")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("visible")){
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
		all_fields.add("element_name");
		all_fields.add("field_ID");
		all_fields.add("sequence");
		all_fields.add("type");
		all_fields.add("header");
		all_fields.add("value");
		all_fields.add("visible");
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
		writer.write("\n<xdat:search_field");
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
		writer.write("\n</xdat:search_field>");
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
		if (_Visible!=null)
			map.put("visible",ValueParser(_Visible,"boolean"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		if (_ElementName!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:element_name");
			writer.write(">");
			writer.write(ValueParser(_ElementName,"string"));
			writer.write("</xdat:element_name>");
			header--;
		}
		if (_FieldId!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:field_ID");
			writer.write(">");
			writer.write(ValueParser(_FieldId,"string"));
			writer.write("</xdat:field_ID>");
			header--;
		}
		if (_Sequence!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:sequence");
			writer.write(">");
			writer.write(ValueParser(_Sequence,"integer"));
			writer.write("</xdat:sequence>");
			header--;
		}
		else{
			writer.write("\n" + createHeader(header++) + "<xdat:sequence");
			writer.write("/>");
			header--;
		}

		if (_Type!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:type");
			writer.write(">");
			writer.write(ValueParser(_Type,"string"));
			writer.write("</xdat:type>");
			header--;
		}
		if (_Header!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:header");
			writer.write(">");
			writer.write(ValueParser(_Header,"string"));
			writer.write("</xdat:header>");
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
		if (_ElementName!=null) return true;
		if (_FieldId!=null) return true;
		if (_Sequence!=null) return true;
		return true;//REQUIRED sequence
	}
}
