/*
 * core: org.nrg.xdat.bean.XdatActionTypeBean
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
public class XdatActionTypeBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatActionTypeI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatActionTypeBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:action_type";

	public String getSchemaElementName(){
		return "action_type";
	}

	public String getFullSchemaElementName(){
		return "xdat:action_type";
	}

	//FIELD

	private String _ActionName=null;

	/**
	 * @return Returns the action_name.
	 */
	public String getActionName(){
		return _ActionName;
	}

	/**
	 * Sets the value for action_name.
	 * @param v Value to Set.
	 */
	public void setActionName(String v){
		_ActionName=v;
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
	 * Sets the value for xdat:action_type/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) {
		_Sequence=v;
	}

	/**
	 * Sets the value for xdat:action_type/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(String v)  {
		_Sequence=formatInteger(v);
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("action_name")){
			setActionName(v);
		}else if (xmlPath.equals("display_name")){
			setDisplayName(v);
		}else if (xmlPath.equals("sequence")){
			setSequence(v);
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
		if (xmlPath.equals("action_name")){
			return getActionName();
		}else if (xmlPath.equals("display_name")){
			return getDisplayName();
		}else if (xmlPath.equals("sequence")){
			return getSequence();
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
		if (xmlPath.equals("action_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("display_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("sequence")){
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
		all_fields.add("action_name");
		all_fields.add("display_name");
		all_fields.add("sequence");
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
		writer.write("\n<xdat:action_type");
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
		writer.write("\n</xdat:action_type>");
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
		if (_ActionName!=null)
			map.put("action_name",ValueParser(_ActionName,"string"));
		else map.put("action_name","");//REQUIRED FIELD

		if (_DisplayName!=null)
			map.put("display_name",ValueParser(_DisplayName,"string"));
		//NOT REQUIRED FIELD

		if (_Sequence!=null)
			map.put("sequence",ValueParser(_Sequence,"integer"));
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
