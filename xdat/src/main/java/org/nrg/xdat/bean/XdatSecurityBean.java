/*
 * core: org.nrg.xdat.bean.XdatSecurityBean
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
public class XdatSecurityBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatSecurityI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatSecurityBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:security";

	public String getSchemaElementName(){
		return "security";
	}

	public String getFullSchemaElementName(){
		return "xdat:security";
	}
	 private List<org.nrg.xdat.bean.XdatUsergroupBean> _Groups_group =new ArrayList<org.nrg.xdat.bean.XdatUsergroupBean>();

	/**
	 * groups/group
	 * @return Returns an List of org.nrg.xdat.bean.XdatUsergroupBean
	 */
	public <A extends org.nrg.xdat.model.XdatUsergroupI> List<A> getGroups_group() {
		return (List<A>) _Groups_group;
	}

	/**
	 * Sets the value for groups/group.
	 * @param v Value to Set.
	 */
	public void setGroups_group(ArrayList<org.nrg.xdat.bean.XdatUsergroupBean> v){
		_Groups_group=v;
	}

	/**
	 * Adds the value for groups/group.
	 * @param v Value to Set.
	 */
	public void addGroups_group(org.nrg.xdat.bean.XdatUsergroupBean v){
		_Groups_group.add(v);
	}

	/**
	 * groups/group
	 * Adds org.nrg.xdat.model.XdatUsergroupI
	 */
	public <A extends org.nrg.xdat.model.XdatUsergroupI> void addGroups_group(A item) throws Exception{
	_Groups_group.add((org.nrg.xdat.bean.XdatUsergroupBean)item);
	}

	/**
	 * Adds the value for groups/group.
	 * @param v Value to Set.
	 */
	public void addGroups_group(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatUsergroupBean)
			_Groups_group.add((org.nrg.xdat.bean.XdatUsergroupBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatUsergroupBean");
	}
	 private List<org.nrg.xdat.bean.XdatUserBean> _Users_user =new ArrayList<org.nrg.xdat.bean.XdatUserBean>();

	/**
	 * users/user
	 * @return Returns an List of org.nrg.xdat.bean.XdatUserBean
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> List<A> getUsers_user() {
		return (List<A>) _Users_user;
	}

	/**
	 * Sets the value for users/user.
	 * @param v Value to Set.
	 */
	public void setUsers_user(ArrayList<org.nrg.xdat.bean.XdatUserBean> v){
		_Users_user=v;
	}

	/**
	 * Adds the value for users/user.
	 * @param v Value to Set.
	 */
	public void addUsers_user(org.nrg.xdat.bean.XdatUserBean v){
		_Users_user.add(v);
	}

	/**
	 * users/user
	 * Adds org.nrg.xdat.model.XdatUserI
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> void addUsers_user(A item) throws Exception{
	_Users_user.add((org.nrg.xdat.bean.XdatUserBean)item);
	}

	/**
	 * Adds the value for users/user.
	 * @param v Value to Set.
	 */
	public void addUsers_user(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatUserBean)
			_Users_user.add((org.nrg.xdat.bean.XdatUserBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatUserBean");
	}
	 private List<org.nrg.xdat.bean.XdatRoleTypeBean> _Roles_role =new ArrayList<org.nrg.xdat.bean.XdatRoleTypeBean>();

	/**
	 * roles/role
	 * @return Returns an List of org.nrg.xdat.bean.XdatRoleTypeBean
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> List<A> getRoles_role() {
		return (List<A>) _Roles_role;
	}

	/**
	 * Sets the value for roles/role.
	 * @param v Value to Set.
	 */
	public void setRoles_role(ArrayList<org.nrg.xdat.bean.XdatRoleTypeBean> v){
		_Roles_role=v;
	}

	/**
	 * Adds the value for roles/role.
	 * @param v Value to Set.
	 */
	public void addRoles_role(org.nrg.xdat.bean.XdatRoleTypeBean v){
		_Roles_role.add(v);
	}

	/**
	 * roles/role
	 * Adds org.nrg.xdat.model.XdatRoleTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> void addRoles_role(A item) throws Exception{
	_Roles_role.add((org.nrg.xdat.bean.XdatRoleTypeBean)item);
	}

	/**
	 * Adds the value for roles/role.
	 * @param v Value to Set.
	 */
	public void addRoles_role(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatRoleTypeBean)
			_Roles_role.add((org.nrg.xdat.bean.XdatRoleTypeBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatRoleTypeBean");
	}
	 private List<org.nrg.xdat.bean.XdatActionTypeBean> _Actions_action =new ArrayList<org.nrg.xdat.bean.XdatActionTypeBean>();

	/**
	 * actions/action
	 * @return Returns an List of org.nrg.xdat.bean.XdatActionTypeBean
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> List<A> getActions_action() {
		return (List<A>) _Actions_action;
	}

	/**
	 * Sets the value for actions/action.
	 * @param v Value to Set.
	 */
	public void setActions_action(ArrayList<org.nrg.xdat.bean.XdatActionTypeBean> v){
		_Actions_action=v;
	}

	/**
	 * Adds the value for actions/action.
	 * @param v Value to Set.
	 */
	public void addActions_action(org.nrg.xdat.bean.XdatActionTypeBean v){
		_Actions_action.add(v);
	}

	/**
	 * actions/action
	 * Adds org.nrg.xdat.model.XdatActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> void addActions_action(A item) throws Exception{
	_Actions_action.add((org.nrg.xdat.bean.XdatActionTypeBean)item);
	}

	/**
	 * Adds the value for actions/action.
	 * @param v Value to Set.
	 */
	public void addActions_action(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatActionTypeBean)
			_Actions_action.add((org.nrg.xdat.bean.XdatActionTypeBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatActionTypeBean");
	}
	 private List<org.nrg.xdat.bean.XdatElementSecurityBean> _ElementSecuritySet_elementSecurity =new ArrayList<org.nrg.xdat.bean.XdatElementSecurityBean>();

	/**
	 * element_security_set/element_security
	 * @return Returns an List of org.nrg.xdat.bean.XdatElementSecurityBean
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityI> List<A> getElementSecuritySet_elementSecurity() {
		return (List<A>) _ElementSecuritySet_elementSecurity;
	}

	/**
	 * Sets the value for element_security_set/element_security.
	 * @param v Value to Set.
	 */
	public void setElementSecuritySet_elementSecurity(ArrayList<org.nrg.xdat.bean.XdatElementSecurityBean> v){
		_ElementSecuritySet_elementSecurity=v;
	}

	/**
	 * Adds the value for element_security_set/element_security.
	 * @param v Value to Set.
	 */
	public void addElementSecuritySet_elementSecurity(org.nrg.xdat.bean.XdatElementSecurityBean v){
		_ElementSecuritySet_elementSecurity.add(v);
	}

	/**
	 * element_security_set/element_security
	 * Adds org.nrg.xdat.model.XdatElementSecurityI
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityI> void addElementSecuritySet_elementSecurity(A item) throws Exception{
	_ElementSecuritySet_elementSecurity.add((org.nrg.xdat.bean.XdatElementSecurityBean)item);
	}

	/**
	 * Adds the value for element_security_set/element_security.
	 * @param v Value to Set.
	 */
	public void addElementSecuritySet_elementSecurity(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatElementSecurityBean)
			_ElementSecuritySet_elementSecurity.add((org.nrg.xdat.bean.XdatElementSecurityBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatElementSecurityBean");
	}
	 private org.nrg.xdat.bean.XdatNewsentryBean _Newslist_news =null;

	/**
	 * newsList/news
	 * @return org.nrg.xdat.bean.XdatNewsentryBean
	 */
	public org.nrg.xdat.bean.XdatNewsentryBean getNewslist_news() {
		return _Newslist_news;
	}

	/**
	 * Sets the value for newsList/news.
	 * @param v Value to Set.
	 */
	public void setNewslist_news(org.nrg.xdat.bean.XdatNewsentryBean v){
		_Newslist_news =v;
	}

	/**
	 * Sets the value for newsList/news.
	 * @param v Value to Set.
	 */
	public void setNewslist_news(Object v) {
		if (v instanceof org.nrg.xdat.bean.XdatNewsentryBean)
			_Newslist_news =(org.nrg.xdat.bean.XdatNewsentryBean)v;
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatNewsentryBean");
	}

	/**
	 * newsList/news
	 */
	public <A extends org.nrg.xdat.model.XdatNewsentryI> void setNewslist_news(A item) throws Exception{
	setNewslist_news((org.nrg.xdat.bean.XdatNewsentryBean)item);
	}

	//FIELD

	private Integer _Newslist_newsFK=null;

	/**
	 * @return Returns the xdat:security/newslist_news_xdat_newsentry_id.
	 */
	public Integer getNewslist_newsFK(){
		return _Newslist_newsFK;
	}

	/**
	 * Sets the value for xdat:security/newslist_news_xdat_newsentry_id.
	 * @param v Value to Set.
	 */
	public void setNewslist_newsFK(Integer v) {
		_Newslist_newsFK=v;
	}
	 private org.nrg.xdat.bean.XdatInfoentryBean _Infolist_info =null;

	/**
	 * infoList/info
	 * @return org.nrg.xdat.bean.XdatInfoentryBean
	 */
	public org.nrg.xdat.bean.XdatInfoentryBean getInfolist_info() {
		return _Infolist_info;
	}

	/**
	 * Sets the value for infoList/info.
	 * @param v Value to Set.
	 */
	public void setInfolist_info(org.nrg.xdat.bean.XdatInfoentryBean v){
		_Infolist_info =v;
	}

	/**
	 * Sets the value for infoList/info.
	 * @param v Value to Set.
	 */
	public void setInfolist_info(Object v) {
		if (v instanceof org.nrg.xdat.bean.XdatInfoentryBean)
			_Infolist_info =(org.nrg.xdat.bean.XdatInfoentryBean)v;
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatInfoentryBean");
	}

	/**
	 * infoList/info
	 */
	public <A extends org.nrg.xdat.model.XdatInfoentryI> void setInfolist_info(A item) throws Exception{
	setInfolist_info((org.nrg.xdat.bean.XdatInfoentryBean)item);
	}

	//FIELD

	private Integer _Infolist_infoFK=null;

	/**
	 * @return Returns the xdat:security/infolist_info_xdat_infoentry_id.
	 */
	public Integer getInfolist_infoFK(){
		return _Infolist_infoFK;
	}

	/**
	 * Sets the value for xdat:security/infolist_info_xdat_infoentry_id.
	 * @param v Value to Set.
	 */
	public void setInfolist_infoFK(Integer v) {
		_Infolist_infoFK=v;
	}

	//FIELD

	private String _System=null;

	/**
	 * @return Returns the system.
	 */
	public String getSystem(){
		return _System;
	}

	/**
	 * Sets the value for system.
	 * @param v Value to Set.
	 */
	public void setSystem(String v){
		_System=v;
	}

	//FIELD

	private Boolean _RequireLogin=null;

	/**
	 * @return Returns the require_login.
	 */
	public Boolean getRequireLogin() {
		return _RequireLogin;
	}

	/**
	 * Sets the value for require_login.
	 * @param v Value to Set.
	 */
	public void setRequireLogin(Object v){
		if(v instanceof Boolean){
			_RequireLogin=(Boolean)v;
		}else if(v instanceof String){
			_RequireLogin=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Integer _XdatSecurityId=null;

	/**
	 * @return Returns the xdat_security_id.
	 */
	public Integer getXdatSecurityId() {
		return _XdatSecurityId;
	}

	/**
	 * Sets the value for xdat_security_id.
	 * @param v Value to Set.
	 */
	public void setXdatSecurityId(Integer v){
		_XdatSecurityId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("system")){
			setSystem(v);
		}else if (xmlPath.equals("require_login")){
			setRequireLogin(v);
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
		if (xmlPath.equals("groups/group")){
			addGroups_group(v);
		}else if (xmlPath.equals("users/user")){
			addUsers_user(v);
		}else if (xmlPath.equals("roles/role")){
			addRoles_role(v);
		}else if (xmlPath.equals("actions/action")){
			addActions_action(v);
		}else if (xmlPath.equals("element_security_set/element_security")){
			addElementSecuritySet_elementSecurity(v);
		}else if (xmlPath.equals("newsList/news")){
			setNewslist_news(v);
		}else if (xmlPath.equals("infoList/info")){
			setInfolist_info(v);
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
		if (xmlPath.equals("system")){
			return getSystem();
		}else if (xmlPath.equals("require_login")){
			return getRequireLogin();
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
		if (xmlPath.equals("groups/group")){
			return getGroups_group();
		}else if (xmlPath.equals("users/user")){
			return getUsers_user();
		}else if (xmlPath.equals("roles/role")){
			return getRoles_role();
		}else if (xmlPath.equals("actions/action")){
			return getActions_action();
		}else if (xmlPath.equals("element_security_set/element_security")){
			return getElementSecuritySet_elementSecurity();
		}else if (xmlPath.equals("newsList/news")){
			return getNewslist_news();
		}else if (xmlPath.equals("infoList/info")){
			return getInfolist_info();
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
		if (xmlPath.equals("groups/group")){
			return "http://nrg.wustl.edu/security:userGroup";
		}else if (xmlPath.equals("users/user")){
			return "http://nrg.wustl.edu/security:user";
		}else if (xmlPath.equals("roles/role")){
			return "http://nrg.wustl.edu/security:role_type";
		}else if (xmlPath.equals("actions/action")){
			return "http://nrg.wustl.edu/security:action_type";
		}else if (xmlPath.equals("element_security_set/element_security")){
			return "http://nrg.wustl.edu/security:element_security";
		}else if (xmlPath.equals("newsList/news")){
			return "http://nrg.wustl.edu/security:newsEntry";
		}else if (xmlPath.equals("infoList/info")){
			return "http://nrg.wustl.edu/security:infoEntry";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("groups/group")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("users/user")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("roles/role")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("actions/action")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("element_security_set/element_security")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("newsList/news")){
			return BaseElement.field_single_reference;
		}else if (xmlPath.equals("infoList/info")){
			return BaseElement.field_single_reference;
		}else if (xmlPath.equals("system")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("require_login")){
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
		all_fields.add("groups/group");
		all_fields.add("users/user");
		all_fields.add("roles/role");
		all_fields.add("actions/action");
		all_fields.add("element_security_set/element_security");
		all_fields.add("newsList/news");
		all_fields.add("infoList/info");
		all_fields.add("system");
		all_fields.add("require_login");
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
		writer.write("\n<xdat:security");
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
		writer.write("\n</xdat:security>");
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
		if (_System!=null)
			map.put("system",ValueParser(_System,"string"));
		else map.put("system","");//REQUIRED FIELD

		if (_RequireLogin!=null)
			map.put("require_login",ValueParser(_RequireLogin,"boolean"));
		else map.put("require_login","");//REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
			int child0=0;
			int att0=0;
			child0+=_Groups_group.size();
			if(child0>0 || att0>0){
				writer.write("\n" + createHeader(header++) + "<xdat:groups");
			if(child0==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> groups/group
		java.util.Iterator iter1=_Groups_group.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatUsergroupBean child = (org.nrg.xdat.bean.XdatUsergroupBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:group");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:userGroup")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:group>");
				}else{
					writer.write("</xdat:group>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:groups>");
			}
			}

			int child2=0;
			int att2=0;
			child2+=_Users_user.size();
			if(child2>0 || att2>0){
				writer.write("\n" + createHeader(header++) + "<xdat:users");
			if(child2==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> users/user
		java.util.Iterator iter3=_Users_user.iterator();
		while(iter3.hasNext()){
			org.nrg.xdat.bean.XdatUserBean child = (org.nrg.xdat.bean.XdatUserBean)iter3.next();
			writer.write("\n" + createHeader(header++) + "<xdat:user");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:user")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return4 =child.addXMLBody(writer,header);
				if(return4){
					writer.write("\n" + createHeader(--header) + "</xdat:user>");
				}else{
					writer.write("</xdat:user>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:users>");
			}
			}

			int child4=0;
			int att4=0;
			child4+=_Roles_role.size();
			if(child4>0 || att4>0){
				writer.write("\n" + createHeader(header++) + "<xdat:roles");
			if(child4==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> roles/role
		java.util.Iterator iter5=_Roles_role.iterator();
		while(iter5.hasNext()){
			org.nrg.xdat.bean.XdatRoleTypeBean child = (org.nrg.xdat.bean.XdatRoleTypeBean)iter5.next();
			writer.write("\n" + createHeader(header++) + "<xdat:role");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:role_type")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return6 =child.addXMLBody(writer,header);
				if(return6){
					writer.write("\n" + createHeader(--header) + "</xdat:role>");
				}else{
					writer.write("</xdat:role>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:roles>");
			}
			}

			int child6=0;
			int att6=0;
			child6+=_Actions_action.size();
			if(child6>0 || att6>0){
				writer.write("\n" + createHeader(header++) + "<xdat:actions");
			if(child6==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> actions/action
		java.util.Iterator iter7=_Actions_action.iterator();
		while(iter7.hasNext()){
			org.nrg.xdat.bean.XdatActionTypeBean child = (org.nrg.xdat.bean.XdatActionTypeBean)iter7.next();
			writer.write("\n" + createHeader(header++) + "<xdat:action");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:action_type")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return8 =child.addXMLBody(writer,header);
				if(return8){
					writer.write("\n" + createHeader(--header) + "</xdat:action>");
				}else{
					writer.write("</xdat:action>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:actions>");
			}
			}

			int child8=0;
			int att8=0;
			child8+=_ElementSecuritySet_elementSecurity.size();
			if(child8>0 || att8>0){
				writer.write("\n" + createHeader(header++) + "<xdat:element_security_set");
			if(child8==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> element_security_set/element_security
		java.util.Iterator iter9=_ElementSecuritySet_elementSecurity.iterator();
		while(iter9.hasNext()){
			org.nrg.xdat.bean.XdatElementSecurityBean child = (org.nrg.xdat.bean.XdatElementSecurityBean)iter9.next();
			writer.write("\n" + createHeader(header++) + "<xdat:element_security");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:element_security")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return10 =child.addXMLBody(writer,header);
				if(return10){
					writer.write("\n" + createHeader(--header) + "</xdat:element_security>");
				}else{
					writer.write("</xdat:element_security>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:element_security_set>");
			}
			}

			int child10=0;
			int att10=0;
			if(_Newslist_news!=null)
			child10++;
			if(child10>0 || att10>0){
				writer.write("\n" + createHeader(header++) + "<xdat:newsList");
			if(child10==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> newsList/news
		//DATA-FIELD FROM security -> newsList/news
		if (_Newslist_news!=null){
		//NEW ELEMENT
			writer.write("\n" + createHeader(header++) + "<xdat:news");
			_Newslist_news.addXMLAtts(writer);
			if(!_Newslist_news.getFullSchemaElementName().equals("xdat:newsEntry")){
				writer.write(" xsi:type=\"" + _Newslist_news.getFullSchemaElementName() + "\"");
			}
			if (_Newslist_news.hasXMLBodyContent()){
				writer.write(">");
				boolean return11 =_Newslist_news.addXMLBody(writer,header);
				if(return11){
					writer.write("\n" + createHeader(--header) + "</xdat:news>");
				}else{
					writer.write("</xdat:news>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//NOT REQUIRED

				writer.write("\n" + createHeader(--header) + "</xdat:newsList>");
			}
			}

			int child11=0;
			int att11=0;
			if(_Infolist_info!=null)
			child11++;
			if(child11>0 || att11>0){
				writer.write("\n" + createHeader(header++) + "<xdat:infoList");
			if(child11==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM security -> infoList/info
		//DATA-FIELD FROM security -> infoList/info
		if (_Infolist_info!=null){
		//NEW ELEMENT
			writer.write("\n" + createHeader(header++) + "<xdat:info");
			_Infolist_info.addXMLAtts(writer);
			if(!_Infolist_info.getFullSchemaElementName().equals("xdat:infoEntry")){
				writer.write(" xsi:type=\"" + _Infolist_info.getFullSchemaElementName() + "\"");
			}
			if (_Infolist_info.hasXMLBodyContent()){
				writer.write(">");
				boolean return12 =_Infolist_info.addXMLBody(writer,header);
				if(return12){
					writer.write("\n" + createHeader(--header) + "</xdat:info>");
				}else{
					writer.write("</xdat:info>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
		//NOT REQUIRED

				writer.write("\n" + createHeader(--header) + "</xdat:infoList>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
			if(_Groups_group.size()>0)return true;
			if(_Users_user.size()>0)return true;
			if(_Roles_role.size()>0)return true;
			if(_Actions_action.size()>0)return true;
			if(_ElementSecuritySet_elementSecurity.size()>0)return true;
			if(_Newslist_news!=null) return true;
			if(_Infolist_info!=null) return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
