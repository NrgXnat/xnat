/*
 * core: org.nrg.xdat.bean.XdatCriteriaSetBean
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
public class XdatCriteriaSetBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatCriteriaSetI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatCriteriaSetBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:criteria_set";

	public String getSchemaElementName(){
		return "criteria_set";
	}

	public String getFullSchemaElementName(){
		return "xdat:criteria_set";
	}
	 private List<org.nrg.xdat.bean.XdatCriteriaBean> _Criteria =new ArrayList<org.nrg.xdat.bean.XdatCriteriaBean>();

	/**
	 * criteria
	 * @return Returns an List of org.nrg.xdat.bean.XdatCriteriaBean
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaI> List<A> getCriteria() {
		return (List<A>) _Criteria;
	}

	/**
	 * Sets the value for criteria.
	 * @param v Value to Set.
	 */
	public void setCriteria(ArrayList<org.nrg.xdat.bean.XdatCriteriaBean> v){
		_Criteria=v;
	}

	/**
	 * Adds the value for criteria.
	 * @param v Value to Set.
	 */
	public void addCriteria(org.nrg.xdat.bean.XdatCriteriaBean v){
		_Criteria.add(v);
	}

	/**
	 * criteria
	 * Adds org.nrg.xdat.model.XdatCriteriaI
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaI> void addCriteria(A item) throws Exception{
	_Criteria.add((org.nrg.xdat.bean.XdatCriteriaBean)item);
	}

	/**
	 * Adds the value for criteria.
	 * @param v Value to Set.
	 */
	public void addCriteria(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatCriteriaBean)
			_Criteria.add((org.nrg.xdat.bean.XdatCriteriaBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatCriteriaBean");
	}
	 private List<org.nrg.xdat.bean.XdatCriteriaSetBean> _ChildSet =new ArrayList<org.nrg.xdat.bean.XdatCriteriaSetBean>();

	/**
	 * child_set
	 * @return Returns an List of org.nrg.xdat.bean.XdatCriteriaSetBean
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> List<A> getChildSet() {
		return (List<A>) _ChildSet;
	}

	/**
	 * Sets the value for child_set.
	 * @param v Value to Set.
	 */
	public void setChildSet(ArrayList<org.nrg.xdat.bean.XdatCriteriaSetBean> v){
		_ChildSet=v;
	}

	/**
	 * Adds the value for child_set.
	 * @param v Value to Set.
	 */
	public void addChildSet(org.nrg.xdat.bean.XdatCriteriaSetBean v){
		_ChildSet.add(v);
	}

	/**
	 * child_set
	 * Adds org.nrg.xdat.model.XdatCriteriaSetI
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> void addChildSet(A item) throws Exception{
	_ChildSet.add((org.nrg.xdat.bean.XdatCriteriaSetBean)item);
	}

	/**
	 * Adds the value for child_set.
	 * @param v Value to Set.
	 */
	public void addChildSet(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatCriteriaSetBean)
			_ChildSet.add((org.nrg.xdat.bean.XdatCriteriaSetBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatCriteriaSetBean");
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

	private Integer _XdatCriteriaSetId=null;

	/**
	 * @return Returns the xdat_criteria_set_id.
	 */
	public Integer getXdatCriteriaSetId() {
		return _XdatCriteriaSetId;
	}

	/**
	 * Sets the value for xdat_criteria_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaSetId(Integer v){
		_XdatCriteriaSetId=v;
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
		if (xmlPath.equals("criteria")){
			addCriteria(v);
		}else if (xmlPath.equals("child_set")){
			addChildSet(v);
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
		if (xmlPath.equals("method")){
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
		if (xmlPath.equals("criteria")){
			return getCriteria();
		}else if (xmlPath.equals("child_set")){
			return getChildSet();
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
		if (xmlPath.equals("criteria")){
			return "http://nrg.wustl.edu/security:criteria";
		}else if (xmlPath.equals("child_set")){
			return "http://nrg.wustl.edu/security:criteria_set";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("criteria")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("child_set")){
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
		all_fields.add("criteria");
		all_fields.add("child_set");
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
		writer.write("\n<xdat:criteria_set");
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
		writer.write("\n</xdat:criteria_set>");
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
		else map.put("method","");//REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		//REFERENCE FROM criteria_set -> criteria
		java.util.Iterator iter0=_Criteria.iterator();
		while(iter0.hasNext()){
			org.nrg.xdat.bean.XdatCriteriaBean child = (org.nrg.xdat.bean.XdatCriteriaBean)iter0.next();
			writer.write("\n" + createHeader(header++) + "<xdat:criteria");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:criteria")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return1 =child.addXMLBody(writer,header);
				if(return1){
					writer.write("\n" + createHeader(--header) + "</xdat:criteria>");
				}else{
					writer.write("</xdat:criteria>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//REFERENCE FROM criteria_set -> child_set
		java.util.Iterator iter1=_ChildSet.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatCriteriaSetBean child = (org.nrg.xdat.bean.XdatCriteriaSetBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:child_set");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:criteria_set")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:child_set>");
				}else{
					writer.write("</xdat:child_set>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if(_Criteria.size()>0) return true;
		if(_ChildSet.size()>0) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
