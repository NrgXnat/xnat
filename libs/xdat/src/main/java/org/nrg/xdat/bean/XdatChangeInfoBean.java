/*
 * core: org.nrg.xdat.bean.XdatChangeInfoBean
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
public class XdatChangeInfoBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatChangeInfoI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatChangeInfoBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:change_info";

	public String getSchemaElementName(){
		return "change_info";
	}

	public String getFullSchemaElementName(){
		return "xdat:change_info";
	}
	 private org.nrg.xdat.bean.XdatUserBean _ChangeUser =null;

	/**
	 * change_user
	 * @return org.nrg.xdat.bean.XdatUserBean
	 */
	public org.nrg.xdat.bean.XdatUserBean getChangeUser() {
		return _ChangeUser;
	}

	/**
	 * Sets the value for change_user.
	 * @param v Value to Set.
	 */
	public void setChangeUser(org.nrg.xdat.bean.XdatUserBean v){
		_ChangeUser =v;
	}

	/**
	 * Sets the value for change_user.
	 * @param v Value to Set.
	 */
	public void setChangeUser(Object v) {
		if (v instanceof org.nrg.xdat.bean.XdatUserBean)
			_ChangeUser =(org.nrg.xdat.bean.XdatUserBean)v;
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatUserBean");
	}

	/**
	 * change_user
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> void setChangeUser(A item) throws Exception{
	setChangeUser((org.nrg.xdat.bean.XdatUserBean)item);
	}

	//FIELD

	private Integer _ChangeUserFK=null;

	/**
	 * @return Returns the xdat:change_info/change_user.
	 */
	public Integer getChangeUserFK(){
		return _ChangeUserFK;
	}

	/**
	 * Sets the value for xdat:change_info/change_user.
	 * @param v Value to Set.
	 */
	public void setChangeUserFK(Integer v) {
		_ChangeUserFK=v;
	}

	//FIELD

	private String _Comment=null;

	/**
	 * @return Returns the comment.
	 */
	public String getComment(){
		return _Comment;
	}

	/**
	 * Sets the value for comment.
	 * @param v Value to Set.
	 */
	public void setComment(String v){
		_Comment=v;
	}

	//FIELD

	private Date _ChangeDate=null;

	/**
	 * @return Returns the change_date.
	 */
	public Date getChangeDate(){
		return _ChangeDate;
	}

	/**
	 * Sets the value for change_date.
	 * @param v Value to Set.
	 */
	public void setChangeDate(Date v){
		_ChangeDate=v;
	}

	/**
	 * Sets the value for change_date.
	 * @param v Value to Set.
	 */
	public void setChangeDate(Object v){
		throw new IllegalArgumentException();
	}

	/**
	 * Sets the value for change_date.
	 * @param v Value to Set.
	 */
	public void setChangeDate(String v)  {
		_ChangeDate=formatDateTime(v);
	}

	//FIELD

	private Integer _EventId=null;

	/**
	 * @return Returns the event_id.
	 */
	public Integer getEventId(){
		return _EventId;
	}

	/**
	 * Sets the value for xdat:change_info/event_id.
	 * @param v Value to Set.
	 */
	public void setEventId(Integer v) {
		_EventId=v;
	}

	/**
	 * Sets the value for xdat:change_info/event_id.
	 * @param v Value to Set.
	 */
	public void setEventId(String v)  {
		_EventId=formatInteger(v);
	}

	//FIELD

	private Object _XdatChangeInfoId=null;

	/**
	 * @return Returns the xdat_change_info_id.
	 */
	public Object getXdatChangeInfoId(){
		return _XdatChangeInfoId;
	}

	/**
	 * Sets the value for xdat_change_info_id.
	 * @param v Value to Set.
	 */
	public void setXdatChangeInfoId(Object v){
		_XdatChangeInfoId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("comment")){
			setComment(v);
		}else if (xmlPath.equals("change_date")){
			setChangeDate(v);
		}else if (xmlPath.equals("event_id")){
			setEventId(v);
		}else if (xmlPath.equals("xdat_change_info_id")){
			setXdatChangeInfoId(v);
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
		if (xmlPath.equals("change_user")){
			setChangeUser(v);
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
		if (xmlPath.equals("comment")){
			return getComment();
		}else if (xmlPath.equals("change_date")){
			return getChangeDate();
		}else if (xmlPath.equals("event_id")){
			return getEventId();
		}else if (xmlPath.equals("xdat_change_info_id")){
			return getXdatChangeInfoId();
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
		if (xmlPath.equals("change_user")){
			return getChangeUser();
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
		if (xmlPath.equals("change_user")){
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
		if (xmlPath.equals("change_user")){
			return BaseElement.field_single_reference;
		}else if (xmlPath.equals("comment")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("change_date")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("event_id")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("xdat_change_info_id")){
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
		all_fields.add("change_user");
		all_fields.add("comment");
		all_fields.add("change_date");
		all_fields.add("event_id");
		all_fields.add("xdat_change_info_id");
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
		writer.write("\n<xdat:change_info");
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
		writer.write("\n</xdat:change_info>");
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
		if (_ChangeDate!=null)
			map.put("change_date",ValueParser(_ChangeDate,"dateTime"));
		//NOT REQUIRED FIELD

		if (_EventId!=null)
			map.put("event_id",ValueParser(_EventId,"integer"));
		//NOT REQUIRED FIELD

		if (_XdatChangeInfoId!=null)
			map.put("xdat_change_info_id",ValueParser(_XdatChangeInfoId,"long"));
		else map.put("xdat_change_info_id","");//REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		//REFERENCE FROM change_info -> change_user
		//DATA-FIELD FROM change_info -> change_user
		if (_ChangeUser!=null){
		//NEW ELEMENT
			writer.write("\n" + createHeader(header++) + "<xdat:change_user");
			_ChangeUser.addXMLAtts(writer);
			if(!_ChangeUser.getFullSchemaElementName().equals("xdat:user")){
				writer.write(" xsi:type=\"" + _ChangeUser.getFullSchemaElementName() + "\"");
			}
			if (_ChangeUser.hasXMLBodyContent()){
				writer.write(">");
				boolean return0 =_ChangeUser.addXMLBody(writer,header);
				if(return0){
					writer.write("\n" + createHeader(--header) + "</xdat:change_user>");
				}else{
					writer.write("</xdat:change_user>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//NOT REQUIRED

		if (_Comment!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:comment");
			writer.write(">");
			writer.write(ValueParser(_Comment,"string"));
			writer.write("</xdat:comment>");
			header--;
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_ChangeUser!=null){
			if (_ChangeUser.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (_Comment!=null) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
