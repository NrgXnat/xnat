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
public class XdatUsergroupBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatUsergroupI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatUsergroupBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:userGroup";

	public String getSchemaElementName(){
		return "userGroup";
	}

	public String getFullSchemaElementName(){
		return "xdat:userGroup";
	}
	 private List<org.nrg.xdat.bean.XdatElementAccessBean> _ElementAccess =new ArrayList<org.nrg.xdat.bean.XdatElementAccessBean>();

	/**
	 * element_access
	 * @return Returns an List of org.nrg.xdat.bean.XdatElementAccessBean
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessI> List<A> getElementAccess() {
		return (List<A>) _ElementAccess;
	}

	/**
	 * Sets the value for element_access.
	 * @param v Value to Set.
	 */
	public void setElementAccess(ArrayList<org.nrg.xdat.bean.XdatElementAccessBean> v){
		_ElementAccess=v;
	}

	/**
	 * Adds the value for element_access.
	 * @param v Value to Set.
	 */
	public void addElementAccess(org.nrg.xdat.bean.XdatElementAccessBean v){
		_ElementAccess.add(v);
	}

	/**
	 * element_access
	 * Adds org.nrg.xdat.model.XdatElementAccessI
	 */
	public <A extends org.nrg.xdat.model.XdatElementAccessI> void addElementAccess(A item) throws Exception{
	_ElementAccess.add((org.nrg.xdat.bean.XdatElementAccessBean)item);
	}

	/**
	 * Adds the value for element_access.
	 * @param v Value to Set.
	 */
	public void addElementAccess(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatElementAccessBean)
			_ElementAccess.add((org.nrg.xdat.bean.XdatElementAccessBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatElementAccessBean");
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		return _Id;
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		_Id=v;
	}

	//FIELD

	private String _Displayname=null;

	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayname(){
		return _Displayname;
	}

	/**
	 * Sets the value for displayName.
	 * @param v Value to Set.
	 */
	public void setDisplayname(String v){
		_Displayname=v;
	}

	//FIELD

	private String _Tag=null;

	/**
	 * @return Returns the tag.
	 */
	public String getTag(){
		return _Tag;
	}

	/**
	 * Sets the value for tag.
	 * @param v Value to Set.
	 */
	public void setTag(String v){
		_Tag=v;
	}

	//FIELD

	private Integer _XdatUsergroupId=null;

	/**
	 * @return Returns the xdat_userGroup_id.
	 */
	public Integer getXdatUsergroupId() {
		return _XdatUsergroupId;
	}

	/**
	 * Sets the value for xdat_userGroup_id.
	 * @param v Value to Set.
	 */
	public void setXdatUsergroupId(Integer v){
		_XdatUsergroupId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("ID")){
			setId(v);
		}else if (xmlPath.equals("displayName")){
			setDisplayname(v);
		}else if (xmlPath.equals("tag")){
			setTag(v);
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
		if (xmlPath.equals("element_access")){
			addElementAccess(v);
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
		if (xmlPath.equals("ID")){
			return getId();
		}else if (xmlPath.equals("displayName")){
			return getDisplayname();
		}else if (xmlPath.equals("tag")){
			return getTag();
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
		if (xmlPath.equals("element_access")){
			return getElementAccess();
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
		if (xmlPath.equals("element_access")){
			return "http://nrg.wustl.edu/security:element_access";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("element_access")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("ID")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("displayName")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("tag")){
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
		all_fields.add("element_access");
		all_fields.add("ID");
		all_fields.add("displayName");
		all_fields.add("tag");
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
		writer.write("\n<xdat:UserGroup");
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
		writer.write("\n</xdat:UserGroup>");
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
		if (_Id!=null)
			map.put("ID",ValueParser(_Id,"string"));
		//NOT REQUIRED FIELD

		if (_Displayname!=null)
			map.put("displayName",ValueParser(_Displayname,"string"));
		//NOT REQUIRED FIELD

		if (_Tag!=null)
			map.put("tag",ValueParser(_Tag,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		//REFERENCE FROM userGroup -> element_access
		java.util.Iterator iter0=_ElementAccess.iterator();
		while(iter0.hasNext()){
			org.nrg.xdat.bean.XdatElementAccessBean child = (org.nrg.xdat.bean.XdatElementAccessBean)iter0.next();
			writer.write("\n" + createHeader(header++) + "<xdat:element_access");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:element_access")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return1 =child.addXMLBody(writer,header);
				if(return1){
					writer.write("\n" + createHeader(--header) + "</xdat:element_access>");
				}else{
					writer.write("</xdat:element_access>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
	return true;
	}


	protected boolean hasXMLBodyContent(){
		if(_ElementAccess.size()>0) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
