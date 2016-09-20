/*
 * core: org.nrg.xdat.bean.XdatElementActionTypeBean
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
public class XdatElementActionTypeBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatElementActionTypeI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatElementActionTypeBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:element_action_type";

	public String getSchemaElementName(){
		return "element_action_type";
	}

	public String getFullSchemaElementName(){
		return "xdat:element_action_type";
	}

	//FIELD

	private String _ElementActionName=null;

	/**
	 * @return Returns the element_action_name.
	 */
	public String getElementActionName(){
		return _ElementActionName;
	}

	/**
	 * Sets the value for element_action_name.
	 * @param v Value to Set.
	 */
	public void setElementActionName(String v){
		_ElementActionName=v;
	}

	//FIELD

	private String _DisplayName=null;

	/**
	 * @return Returns the display_name.
	 */
	public String getDisplayName(){
		return _DisplayName;
	}

	/**
	 * Sets the value for display_name.
	 * @param v Value to Set.
	 */
	public void setDisplayName(String v){
		_DisplayName=v;
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
	 * Sets the value for xdat:element_action_type/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) {
		_Sequence=v;
	}

	/**
	 * Sets the value for xdat:element_action_type/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(String v)  {
		_Sequence=formatInteger(v);
	}

	//FIELD

	private String _Image=null;

	/**
	 * @return Returns the image.
	 */
	public String getImage(){
		return _Image;
	}

	/**
	 * Sets the value for image.
	 * @param v Value to Set.
	 */
	public void setImage(String v){
		_Image=v;
	}

	//FIELD

	private String _Popup=null;

	/**
	 * @return Returns the popup.
	 */
	public String getPopup(){
		return _Popup;
	}

	/**
	 * Sets the value for popup.
	 * @param v Value to Set.
	 */
	public void setPopup(String v){
		_Popup=v;
	}

	//FIELD

	private String _Secureaccess=null;

	/**
	 * @return Returns the secureAccess.
	 */
	public String getSecureaccess(){
		return _Secureaccess;
	}

	/**
	 * Sets the value for secureAccess.
	 * @param v Value to Set.
	 */
	public void setSecureaccess(String v){
		_Secureaccess=v;
	}

	//FIELD

	private String _Securefeature=null;

	/**
	 * @return Returns the secureFeature.
	 */
	public String getSecurefeature(){
		return _Securefeature;
	}

	/**
	 * Sets the value for secureFeature.
	 * @param v Value to Set.
	 */
	public void setSecurefeature(String v){
		_Securefeature=v;
	}

	//FIELD

	private String _Parameterstring=null;

	/**
	 * @return Returns the parameterString.
	 */
	public String getParameterstring(){
		return _Parameterstring;
	}

	/**
	 * Sets the value for parameterString.
	 * @param v Value to Set.
	 */
	public void setParameterstring(String v){
		_Parameterstring=v;
	}

	//FIELD

	private String _Grouping=null;

	/**
	 * @return Returns the grouping.
	 */
	public String getGrouping(){
		return _Grouping;
	}

	/**
	 * Sets the value for grouping.
	 * @param v Value to Set.
	 */
	public void setGrouping(String v){
		_Grouping=v;
	}

	//FIELD

	private Integer _XdatElementActionTypeId=null;

	/**
	 * @return Returns the xdat_element_action_type_id.
	 */
	public Integer getXdatElementActionTypeId() {
		return _XdatElementActionTypeId;
	}

	/**
	 * Sets the value for xdat_element_action_type_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementActionTypeId(Integer v){
		_XdatElementActionTypeId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("element_action_name")){
			setElementActionName(v);
		}else if (xmlPath.equals("display_name")){
			setDisplayName(v);
		}else if (xmlPath.equals("sequence")){
			setSequence(v);
		}else if (xmlPath.equals("image")){
			setImage(v);
		}else if (xmlPath.equals("popup")){
			setPopup(v);
		}else if (xmlPath.equals("secureAccess")){
			setSecureaccess(v);
		}else if (xmlPath.equals("secureFeature")){
			setSecurefeature(v);
		}else if (xmlPath.equals("parameterString")){
			setParameterstring(v);
		}else if (xmlPath.equals("grouping")){
			setGrouping(v);
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
		if (xmlPath.equals("element_action_name")){
			return getElementActionName();
		}else if (xmlPath.equals("display_name")){
			return getDisplayName();
		}else if (xmlPath.equals("sequence")){
			return getSequence();
		}else if (xmlPath.equals("image")){
			return getImage();
		}else if (xmlPath.equals("popup")){
			return getPopup();
		}else if (xmlPath.equals("secureAccess")){
			return getSecureaccess();
		}else if (xmlPath.equals("secureFeature")){
			return getSecurefeature();
		}else if (xmlPath.equals("parameterString")){
			return getParameterstring();
		}else if (xmlPath.equals("grouping")){
			return getGrouping();
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
		if (xmlPath.equals("element_action_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("display_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("sequence")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("image")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("popup")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secureAccess")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secureFeature")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("parameterString")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("grouping")){
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
		all_fields.add("element_action_name");
		all_fields.add("display_name");
		all_fields.add("sequence");
		all_fields.add("image");
		all_fields.add("popup");
		all_fields.add("secureAccess");
		all_fields.add("secureFeature");
		all_fields.add("parameterString");
		all_fields.add("grouping");
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
		writer.write("\n<xdat:element_action_type");
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
		writer.write("\n</xdat:element_action_type>");
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
		if (_ElementActionName!=null)
			map.put("element_action_name",ValueParser(_ElementActionName,"string"));
		else map.put("element_action_name","");//REQUIRED FIELD

		if (_DisplayName!=null)
			map.put("display_name",ValueParser(_DisplayName,"string"));
		else map.put("display_name","");//REQUIRED FIELD

		if (_Sequence!=null)
			map.put("sequence",ValueParser(_Sequence,"integer"));
		//NOT REQUIRED FIELD

		if (_Image!=null)
			map.put("image",ValueParser(_Image,"string"));
		//NOT REQUIRED FIELD

		if (_Popup!=null)
			map.put("popup",ValueParser(_Popup,"string"));
		//NOT REQUIRED FIELD

		if (_Secureaccess!=null)
			map.put("secureAccess",ValueParser(_Secureaccess,"string"));
		//NOT REQUIRED FIELD

		if (_Securefeature!=null)
			map.put("secureFeature",ValueParser(_Securefeature,"string"));
		//NOT REQUIRED FIELD

		if (_Parameterstring!=null)
			map.put("parameterString",ValueParser(_Parameterstring,"string"));
		//NOT REQUIRED FIELD

		if (_Grouping!=null)
			map.put("grouping",ValueParser(_Grouping,"string"));
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
