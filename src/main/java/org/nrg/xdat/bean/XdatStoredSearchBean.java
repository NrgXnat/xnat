/*
 * core: org.nrg.xdat.bean.XdatStoredSearchBean
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
public class XdatStoredSearchBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatStoredSearchI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatStoredSearchBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:stored_search";

	public String getSchemaElementName(){
		return "stored_search";
	}

	public String getFullSchemaElementName(){
		return "xdat:stored_search";
	}

	//FIELD

	private String _RootElementName=null;

	/**
	 * @return Returns the root_element_name.
	 */
	public String getRootElementName(){
		return _RootElementName;
	}

	/**
	 * Sets the value for root_element_name.
	 * @param v Value to Set.
	 */
	public void setRootElementName(String v){
		_RootElementName=v;
	}
	 private List<org.nrg.xdat.bean.XdatSearchFieldBean> _SearchField =new ArrayList<org.nrg.xdat.bean.XdatSearchFieldBean>();

	/**
	 * search_field
	 * @return Returns an List of org.nrg.xdat.bean.XdatSearchFieldBean
	 */
	public <A extends org.nrg.xdat.model.XdatSearchFieldI> List<A> getSearchField() {
		return (List<A>) _SearchField;
	}

	/**
	 * Sets the value for search_field.
	 * @param v Value to Set.
	 */
	public void setSearchField(ArrayList<org.nrg.xdat.bean.XdatSearchFieldBean> v){
		_SearchField=v;
	}

	/**
	 * Adds the value for search_field.
	 * @param v Value to Set.
	 */
	public void addSearchField(org.nrg.xdat.bean.XdatSearchFieldBean v){
		_SearchField.add(v);
	}

	/**
	 * search_field
	 * Adds org.nrg.xdat.model.XdatSearchFieldI
	 */
	public <A extends org.nrg.xdat.model.XdatSearchFieldI> void addSearchField(A item) throws Exception{
	_SearchField.add((org.nrg.xdat.bean.XdatSearchFieldBean)item);
	}

	/**
	 * Adds the value for search_field.
	 * @param v Value to Set.
	 */
	public void addSearchField(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatSearchFieldBean)
			_SearchField.add((org.nrg.xdat.bean.XdatSearchFieldBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatSearchFieldBean");
	}
	 private List<org.nrg.xdat.bean.XdatCriteriaSetBean> _SearchWhere =new ArrayList<org.nrg.xdat.bean.XdatCriteriaSetBean>();

	/**
	 * search_where
	 * @return Returns an List of org.nrg.xdat.bean.XdatCriteriaSetBean
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> List<A> getSearchWhere() {
		return (List<A>) _SearchWhere;
	}

	/**
	 * Sets the value for search_where.
	 * @param v Value to Set.
	 */
	public void setSearchWhere(ArrayList<org.nrg.xdat.bean.XdatCriteriaSetBean> v){
		_SearchWhere=v;
	}

	/**
	 * Adds the value for search_where.
	 * @param v Value to Set.
	 */
	public void addSearchWhere(org.nrg.xdat.bean.XdatCriteriaSetBean v){
		_SearchWhere.add(v);
	}

	/**
	 * search_where
	 * Adds org.nrg.xdat.model.XdatCriteriaSetI
	 */
	public <A extends org.nrg.xdat.model.XdatCriteriaSetI> void addSearchWhere(A item) throws Exception{
	_SearchWhere.add((org.nrg.xdat.bean.XdatCriteriaSetBean)item);
	}

	/**
	 * Adds the value for search_where.
	 * @param v Value to Set.
	 */
	public void addSearchWhere(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatCriteriaSetBean)
			_SearchWhere.add((org.nrg.xdat.bean.XdatCriteriaSetBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatCriteriaSetBean");
	}

	//FIELD

	private String _SortBy_elementName=null;

	/**
	 * @return Returns the sort_by/element_name.
	 */
	public String getSortBy_elementName(){
		return _SortBy_elementName;
	}

	/**
	 * Sets the value for sort_by/element_name.
	 * @param v Value to Set.
	 */
	public void setSortBy_elementName(String v){
		_SortBy_elementName=v;
	}

	//FIELD

	private String _SortBy_fieldId=null;

	/**
	 * @return Returns the sort_by/field_ID.
	 */
	public String getSortBy_fieldId(){
		return _SortBy_fieldId;
	}

	/**
	 * Sets the value for sort_by/field_ID.
	 * @param v Value to Set.
	 */
	public void setSortBy_fieldId(String v){
		_SortBy_fieldId=v;
	}
	 private List<org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean> _AllowedUser =new ArrayList<org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean>();

	/**
	 * allowed_user
	 * @return Returns an List of org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchAllowedUserI> List<A> getAllowedUser() {
		return (List<A>) _AllowedUser;
	}

	/**
	 * Sets the value for allowed_user.
	 * @param v Value to Set.
	 */
	public void setAllowedUser(ArrayList<org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean> v){
		_AllowedUser=v;
	}

	/**
	 * Adds the value for allowed_user.
	 * @param v Value to Set.
	 */
	public void addAllowedUser(org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean v){
		_AllowedUser.add(v);
	}

	/**
	 * allowed_user
	 * Adds org.nrg.xdat.model.XdatStoredSearchAllowedUserI
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchAllowedUserI> void addAllowedUser(A item) throws Exception{
	_AllowedUser.add((org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean)item);
	}

	/**
	 * Adds the value for allowed_user.
	 * @param v Value to Set.
	 */
	public void addAllowedUser(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean)
			_AllowedUser.add((org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean");
	}
	 private List<org.nrg.xdat.bean.XdatStoredSearchGroupidBean> _AllowedGroups_groupid =new ArrayList<org.nrg.xdat.bean.XdatStoredSearchGroupidBean>();

	/**
	 * allowed_groups/groupID
	 * @return Returns an List of org.nrg.xdat.bean.XdatStoredSearchGroupidBean
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchGroupidI> List<A> getAllowedGroups_groupid() {
		return (List<A>) _AllowedGroups_groupid;
	}

	/**
	 * Sets the value for allowed_groups/groupID.
	 * @param v Value to Set.
	 */
	public void setAllowedGroups_groupid(ArrayList<org.nrg.xdat.bean.XdatStoredSearchGroupidBean> v){
		_AllowedGroups_groupid=v;
	}

	/**
	 * Adds the value for allowed_groups/groupID.
	 * @param v Value to Set.
	 */
	public void addAllowedGroups_groupid(org.nrg.xdat.bean.XdatStoredSearchGroupidBean v){
		_AllowedGroups_groupid.add(v);
	}

	/**
	 * allowed_groups/groupID
	 * Adds org.nrg.xdat.model.XdatStoredSearchGroupidI
	 */
	public <A extends org.nrg.xdat.model.XdatStoredSearchGroupidI> void addAllowedGroups_groupid(A item) throws Exception{
	_AllowedGroups_groupid.add((org.nrg.xdat.bean.XdatStoredSearchGroupidBean)item);
	}

	/**
	 * Adds the value for allowed_groups/groupID.
	 * @param v Value to Set.
	 */
	public void addAllowedGroups_groupid(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatStoredSearchGroupidBean)
			_AllowedGroups_groupid.add((org.nrg.xdat.bean.XdatStoredSearchGroupidBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatStoredSearchGroupidBean");
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

	private String _Layeredsequence=null;

	/**
	 * @return Returns the layeredSequence.
	 */
	public String getLayeredsequence(){
		return _Layeredsequence;
	}

	/**
	 * Sets the value for layeredSequence.
	 * @param v Value to Set.
	 */
	public void setLayeredsequence(String v){
		_Layeredsequence=v;
	}

	//FIELD

	private Boolean _AllowDiffColumns=null;

	/**
	 * @return Returns the allow-diff-columns.
	 */
	public Boolean getAllowDiffColumns() {
		return _AllowDiffColumns;
	}

	/**
	 * Sets the value for allow-diff-columns.
	 * @param v Value to Set.
	 */
	public void setAllowDiffColumns(Object v){
		if(v instanceof Boolean){
			_AllowDiffColumns=(Boolean)v;
		}else if(v instanceof String){
			_AllowDiffColumns=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _Secure=null;

	/**
	 * @return Returns the secure.
	 */
	public Boolean getSecure() {
		return _Secure;
	}

	/**
	 * Sets the value for secure.
	 * @param v Value to Set.
	 */
	public void setSecure(Object v){
		if(v instanceof Boolean){
			_Secure=(Boolean)v;
		}else if(v instanceof String){
			_Secure=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private String _BriefDescription=null;

	/**
	 * @return Returns the brief-description.
	 */
	public String getBriefDescription(){
		return _BriefDescription;
	}

	/**
	 * Sets the value for brief-description.
	 * @param v Value to Set.
	 */
	public void setBriefDescription(String v){
		_BriefDescription=v;
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

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("root_element_name")){
			setRootElementName(v);
		}else if (xmlPath.equals("sort_by/element_name")){
			setSortBy_elementName(v);
		}else if (xmlPath.equals("sort_by/field_ID")){
			setSortBy_fieldId(v);
		}else if (xmlPath.equals("ID")){
			setId(v);
		}else if (xmlPath.equals("description")){
			setDescription(v);
		}else if (xmlPath.equals("layeredSequence")){
			setLayeredsequence(v);
		}else if (xmlPath.equals("allow-diff-columns")){
			setAllowDiffColumns(v);
		}else if (xmlPath.equals("secure")){
			setSecure(v);
		}else if (xmlPath.equals("brief-description")){
			setBriefDescription(v);
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
		if (xmlPath.equals("search_field")){
			addSearchField(v);
		}else if (xmlPath.equals("search_where")){
			addSearchWhere(v);
		}else if (xmlPath.equals("allowed_user")){
			addAllowedUser(v);
		}else if (xmlPath.equals("allowed_groups/groupID")){
			addAllowedGroups_groupid(v);
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
		if (xmlPath.equals("root_element_name")){
			return getRootElementName();
		}else if (xmlPath.equals("sort_by/element_name")){
			return getSortBy_elementName();
		}else if (xmlPath.equals("sort_by/field_ID")){
			return getSortBy_fieldId();
		}else if (xmlPath.equals("ID")){
			return getId();
		}else if (xmlPath.equals("description")){
			return getDescription();
		}else if (xmlPath.equals("layeredSequence")){
			return getLayeredsequence();
		}else if (xmlPath.equals("allow-diff-columns")){
			return getAllowDiffColumns();
		}else if (xmlPath.equals("secure")){
			return getSecure();
		}else if (xmlPath.equals("brief-description")){
			return getBriefDescription();
		}else if (xmlPath.equals("tag")){
			return getTag();
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
		if (xmlPath.equals("search_field")){
			return getSearchField();
		}else if (xmlPath.equals("search_where")){
			return getSearchWhere();
		}else if (xmlPath.equals("allowed_user")){
			return getAllowedUser();
		}else if (xmlPath.equals("allowed_groups/groupID")){
			return getAllowedGroups_groupid();
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
		if (xmlPath.equals("search_field")){
			return "http://nrg.wustl.edu/security:search_field";
		}else if (xmlPath.equals("search_where")){
			return "http://nrg.wustl.edu/security:criteria_set";
		}else if (xmlPath.equals("allowed_user")){
			return "http://nrg.wustl.edu/security:stored_search_allowed_user";
		}else if (xmlPath.equals("allowed_groups/groupID")){
			return "http://nrg.wustl.edu/security:stored_search_groupID";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("root_element_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("search_field")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("search_where")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("sort_by/element_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("sort_by/field_ID")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("allowed_user")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("allowed_groups/groupID")){
			return BaseElement.field_inline_repeater;
		}else if (xmlPath.equals("ID")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("description")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("layeredSequence")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("allow-diff-columns")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("brief-description")){
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
		all_fields.add("root_element_name");
		all_fields.add("search_field");
		all_fields.add("search_where");
		all_fields.add("sort_by/element_name");
		all_fields.add("sort_by/field_ID");
		all_fields.add("allowed_user");
		all_fields.add("allowed_groups/groupID");
		all_fields.add("ID");
		all_fields.add("description");
		all_fields.add("layeredSequence");
		all_fields.add("allow-diff-columns");
		all_fields.add("secure");
		all_fields.add("brief-description");
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
		writer.write("\n<xdat:bundle");
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
		writer.write("\n</xdat:bundle>");
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
		else map.put("ID","");//REQUIRED FIELD

		if (_Description!=null)
			map.put("description",ValueParser(_Description,"string"));
		//NOT REQUIRED FIELD

		if (_Layeredsequence!=null)
			map.put("layeredSequence",ValueParser(_Layeredsequence,"string"));
		//NOT REQUIRED FIELD

		if (_AllowDiffColumns!=null)
			map.put("allow-diff-columns",ValueParser(_AllowDiffColumns,"boolean"));
		//NOT REQUIRED FIELD

		if (_Secure!=null)
			map.put("secure",ValueParser(_Secure,"boolean"));
		//NOT REQUIRED FIELD

		if (_BriefDescription!=null)
			map.put("brief-description",ValueParser(_BriefDescription,"string"));
		//NOT REQUIRED FIELD

		if (_Tag!=null)
			map.put("tag",ValueParser(_Tag,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		if (_RootElementName!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:root_element_name");
			writer.write(">");
			writer.write(ValueParser(_RootElementName,"string"));
			writer.write("</xdat:root_element_name>");
			header--;
		}
		//REFERENCE FROM stored_search -> search_field
		java.util.Iterator iter0=_SearchField.iterator();
		while(iter0.hasNext()){
			org.nrg.xdat.bean.XdatSearchFieldBean child = (org.nrg.xdat.bean.XdatSearchFieldBean)iter0.next();
			writer.write("\n" + createHeader(header++) + "<xdat:search_field");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:search_field")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return1 =child.addXMLBody(writer,header);
				if(return1){
					writer.write("\n" + createHeader(--header) + "</xdat:search_field>");
				}else{
					writer.write("</xdat:search_field>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//REFERENCE FROM stored_search -> search_where
		java.util.Iterator iter1=_SearchWhere.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatCriteriaSetBean child = (org.nrg.xdat.bean.XdatCriteriaSetBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:search_where");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:criteria_set")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:search_where>");
				}else{
					writer.write("</xdat:search_where>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
			int child2=0;
			int att2=0;
			if(_SortBy_fieldId!=null)
			child2++;
			if(_SortBy_elementName!=null)
			child2++;
			if(child2>0 || att2>0){
				writer.write("\n" + createHeader(header++) + "<xdat:sort_by");
			if(child2==0){
				writer.write("/>");
			}else{
				writer.write(">");
		if (_SortBy_elementName!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:element_name");
			writer.write(">");
			writer.write(ValueParser(_SortBy_elementName,"string"));
			writer.write("</xdat:element_name>");
			header--;
		}
		if (_SortBy_fieldId!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:field_ID");
			writer.write(">");
			writer.write(ValueParser(_SortBy_fieldId,"string"));
			writer.write("</xdat:field_ID>");
			header--;
		}
				writer.write("\n" + createHeader(--header) + "</xdat:sort_by>");
			}
			}

		//REFERENCE FROM stored_search -> allowed_user
		java.util.Iterator iter3=_AllowedUser.iterator();
		while(iter3.hasNext()){
			org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean child = (org.nrg.xdat.bean.XdatStoredSearchAllowedUserBean)iter3.next();
			writer.write("\n" + createHeader(header++) + "<xdat:allowed_user");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:stored_search_allowed_user")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return4 =child.addXMLBody(writer,header);
				if(return4){
					writer.write("\n" + createHeader(--header) + "</xdat:allowed_user>");
				}else{
					writer.write("</xdat:allowed_user>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
			int child4=0;
			int att4=0;
			child4+=_AllowedGroups_groupid.size();
			if(child4>0 || att4>0){
				writer.write("\n" + createHeader(header++) + "<xdat:allowed_groups");
			if(child4==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM stored_search -> allowed_groups/groupID
		//IN-LINE REPEATER
		java.util.Iterator iter5=_AllowedGroups_groupid.iterator();
		while(iter5.hasNext()){
			org.nrg.xdat.bean.XdatStoredSearchGroupidBean child = (org.nrg.xdat.bean.XdatStoredSearchGroupidBean)iter5.next();
			child.addXMLBody(writer,header);
		}
				writer.write("\n" + createHeader(--header) + "</xdat:allowed_groups>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_RootElementName!=null) return true;
		if(_SearchField.size()>0) return true;
		if(_SearchWhere.size()>0) return true;
			if(_SortBy_fieldId!=null) return true;
			if(_SortBy_elementName!=null) return true;
		if(_AllowedUser.size()>0) return true;
			if(_AllowedGroups_groupid.size()>0)return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
