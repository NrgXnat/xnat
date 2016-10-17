/*
 * core: org.nrg.xdat.bean.XdatRoleTypeBean
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
public class XdatRoleTypeBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatRoleTypeI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatRoleTypeBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:role_type";

	public String getSchemaElementName(){
		return "role_type";
	}

	public String getFullSchemaElementName(){
		return "xdat:role_type";
	}
	 private List<org.nrg.xdat.bean.XdatActionTypeBean> _AllowedActions_allowedAction =new ArrayList<org.nrg.xdat.bean.XdatActionTypeBean>();

	/**
	 * allowed_actions/allowed_action
	 * @return Returns an List of org.nrg.xdat.bean.XdatActionTypeBean
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> List<A> getAllowedActions_allowedAction() {
		return (List<A>) _AllowedActions_allowedAction;
	}

	/**
	 * Sets the value for allowed_actions/allowed_action.
	 * @param v Value to Set.
	 */
	public void setAllowedActions_allowedAction(ArrayList<org.nrg.xdat.bean.XdatActionTypeBean> v){
		_AllowedActions_allowedAction=v;
	}

	/**
	 * Adds the value for allowed_actions/allowed_action.
	 * @param v Value to Set.
	 */
	public void addAllowedActions_allowedAction(org.nrg.xdat.bean.XdatActionTypeBean v){
		_AllowedActions_allowedAction.add(v);
	}

	/**
	 * allowed_actions/allowed_action
	 * Adds org.nrg.xdat.model.XdatActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> void addAllowedActions_allowedAction(A item) throws Exception{
	_AllowedActions_allowedAction.add((org.nrg.xdat.bean.XdatActionTypeBean)item);
	}

	/**
	 * Adds the value for allowed_actions/allowed_action.
	 * @param v Value to Set.
	 */
	public void addAllowedActions_allowedAction(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatActionTypeBean)
			_AllowedActions_allowedAction.add((org.nrg.xdat.bean.XdatActionTypeBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatActionTypeBean");
	}

	//FIELD

	private String _RoleName=null;

	/**
	 * @return Returns the role_name.
	 */
	public String getRoleName(){
		return _RoleName;
	}

	/**
	 * Sets the value for role_name.
	 * @param v Value to Set.
	 */
	public void setRoleName(String v){
		_RoleName=v;
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

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence(){
		return _Sequence;
	}

	/**
	 * Sets the value for xdat:role_type/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) {
		_Sequence=v;
	}

	/**
	 * Sets the value for xdat:role_type/sequence.
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
		if (xmlPath.equals("role_name")){
			setRoleName(v);
		}else if (xmlPath.equals("description")){
			setDescription(v);
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
		if (xmlPath.equals("allowed_actions/allowed_action")){
			addAllowedActions_allowedAction(v);
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
		if (xmlPath.equals("role_name")){
			return getRoleName();
		}else if (xmlPath.equals("description")){
			return getDescription();
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
		if (xmlPath.equals("allowed_actions/allowed_action")){
			return getAllowedActions_allowedAction();
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
		if (xmlPath.equals("allowed_actions/allowed_action")){
			return "http://nrg.wustl.edu/security:action_type";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("allowed_actions/allowed_action")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("role_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("description")){
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
		all_fields.add("allowed_actions/allowed_action");
		all_fields.add("role_name");
		all_fields.add("description");
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
		writer.write("\n<xdat:role_type");
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
		writer.write("\n</xdat:role_type>");
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
		if (_RoleName!=null)
			map.put("role_name",ValueParser(_RoleName,"string"));
		else map.put("role_name","");//REQUIRED FIELD

		if (_Description!=null)
			map.put("description",ValueParser(_Description,"string"));
		//NOT REQUIRED FIELD

		if (_Sequence!=null)
			map.put("sequence",ValueParser(_Sequence,"integer"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
			int child0=0;
			int att0=0;
			child0+=_AllowedActions_allowedAction.size();
			if(child0>0 || att0>0){
				writer.write("\n" + createHeader(header++) + "<xdat:allowed_actions");
			if(child0==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM role_type -> allowed_actions/allowed_action
		java.util.Iterator iter1=_AllowedActions_allowedAction.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatActionTypeBean child = (org.nrg.xdat.bean.XdatActionTypeBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:allowed_action");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:action_type")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:allowed_action>");
				}else{
					writer.write("</xdat:allowed_action>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:allowed_actions>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
			if(_AllowedActions_allowedAction.size()>0)return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
