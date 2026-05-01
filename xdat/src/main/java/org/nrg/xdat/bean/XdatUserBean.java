/*
 * core: org.nrg.xdat.bean.XdatUserBean
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
public class XdatUserBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatUserI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatUserBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:user";

	public String getSchemaElementName(){
		return "user";
	}

	public String getFullSchemaElementName(){
		return "xdat:user";
	}

	//FIELD

	private String _Login=null;

	/**
	 * @return Returns the login.
	 */
	public String getLogin(){
		return _Login;
	}

	/**
	 * Sets the value for login.
	 * @param v Value to Set.
	 */
	public void setLogin(String v){
		_Login=v;
	}

	//FIELD

	private String _Firstname=null;

	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname(){
		return _Firstname;
	}

	/**
	 * Sets the value for firstname.
	 * @param v Value to Set.
	 */
	public void setFirstname(String v){
		_Firstname=v;
	}

	//FIELD

	private String _Lastname=null;

	/**
	 * @return Returns the lastname.
	 */
	public String getLastname(){
		return _Lastname;
	}

	/**
	 * Sets the value for lastname.
	 * @param v Value to Set.
	 */
	public void setLastname(String v){
		_Lastname=v;
	}

	//FIELD

	private String _Email=null;

	/**
	 * @return Returns the email.
	 */
	public String getEmail(){
		return _Email;
	}

	/**
	 * Sets the value for email.
	 * @param v Value to Set.
	 */
	public void setEmail(String v){
		_Email=v;
	}

	//FIELD

	private String _PrimaryPassword=null;

	/**
	 * @return Returns the primary_password.
	 */
	public String getPrimaryPassword(){
		return _PrimaryPassword;
	}

	/**
	 * Sets the value for primary_password.
	 * @param v Value to Set.
	 */
	public void setPrimaryPassword(String v){
		_PrimaryPassword=v;
	}

	//FIELD

	private Boolean _PrimaryPassword_encrypt=null;

	/**
	 * @return Returns the primary_password/encrypt.
	 */
	public Boolean getPrimaryPassword_encrypt() {
		return _PrimaryPassword_encrypt;
	}

	/**
	 * Sets the value for primary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setPrimaryPassword_encrypt(Object v){
		if(v instanceof Boolean){
			_PrimaryPassword_encrypt=(Boolean)v;
		}else if(v instanceof String){
			_PrimaryPassword_encrypt=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
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
	 private List<org.nrg.xdat.bean.XdatRoleTypeBean> _AssignedRoles_assignedRole =new ArrayList<org.nrg.xdat.bean.XdatRoleTypeBean>();

	/**
	 * assigned_roles/assigned_role
	 * @return Returns an List of org.nrg.xdat.bean.XdatRoleTypeBean
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> List<A> getAssignedRoles_assignedRole() {
		return (List<A>) _AssignedRoles_assignedRole;
	}

	/**
	 * Sets the value for assigned_roles/assigned_role.
	 * @param v Value to Set.
	 */
	public void setAssignedRoles_assignedRole(ArrayList<org.nrg.xdat.bean.XdatRoleTypeBean> v){
		_AssignedRoles_assignedRole=v;
	}

	/**
	 * Adds the value for assigned_roles/assigned_role.
	 * @param v Value to Set.
	 */
	public void addAssignedRoles_assignedRole(org.nrg.xdat.bean.XdatRoleTypeBean v){
		_AssignedRoles_assignedRole.add(v);
	}

	/**
	 * assigned_roles/assigned_role
	 * Adds org.nrg.xdat.model.XdatRoleTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> void addAssignedRoles_assignedRole(A item) throws Exception{
	_AssignedRoles_assignedRole.add((org.nrg.xdat.bean.XdatRoleTypeBean)item);
	}

	/**
	 * Adds the value for assigned_roles/assigned_role.
	 * @param v Value to Set.
	 */
	public void addAssignedRoles_assignedRole(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatRoleTypeBean)
			_AssignedRoles_assignedRole.add((org.nrg.xdat.bean.XdatRoleTypeBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatRoleTypeBean");
	}

	//FIELD

	private String _QuarantinePath=null;

	/**
	 * @return Returns the quarantine_path.
	 */
	public String getQuarantinePath(){
		return _QuarantinePath;
	}

	/**
	 * Sets the value for quarantine_path.
	 * @param v Value to Set.
	 */
	public void setQuarantinePath(String v){
		_QuarantinePath=v;
	}
	 private List<org.nrg.xdat.bean.XdatUserGroupidBean> _Groups_groupid =new ArrayList<org.nrg.xdat.bean.XdatUserGroupidBean>();

	/**
	 * groups/groupID
	 * @return Returns an List of org.nrg.xdat.bean.XdatUserGroupidBean
	 */
	public <A extends org.nrg.xdat.model.XdatUserGroupidI> List<A> getGroups_groupid() {
		return (List<A>) _Groups_groupid;
	}

	/**
	 * Sets the value for groups/groupID.
	 * @param v Value to Set.
	 */
	public void setGroups_groupid(ArrayList<org.nrg.xdat.bean.XdatUserGroupidBean> v){
		_Groups_groupid=v;
	}

	/**
	 * Adds the value for groups/groupID.
	 * @param v Value to Set.
	 */
	public void addGroups_groupid(org.nrg.xdat.bean.XdatUserGroupidBean v){
		_Groups_groupid.add(v);
	}

	/**
	 * groups/groupID
	 * Adds org.nrg.xdat.model.XdatUserGroupidI
	 */
	public <A extends org.nrg.xdat.model.XdatUserGroupidI> void addGroups_groupid(A item) throws Exception{
	_Groups_groupid.add((org.nrg.xdat.bean.XdatUserGroupidBean)item);
	}

	/**
	 * Adds the value for groups/groupID.
	 * @param v Value to Set.
	 */
	public void addGroups_groupid(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatUserGroupidBean)
			_Groups_groupid.add((org.nrg.xdat.bean.XdatUserGroupidBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatUserGroupidBean");
	}

	//FIELD

	private Boolean _Enabled=null;

	/**
	 * @return Returns the enabled.
	 */
	public Boolean getEnabled() {
		return _Enabled;
	}

	/**
	 * Sets the value for enabled.
	 * @param v Value to Set.
	 */
	public void setEnabled(Object v){
		if(v instanceof Boolean){
			_Enabled=(Boolean)v;
		}else if(v instanceof String){
			_Enabled=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _Verified=null;

	/**
	 * @return Returns the verified.
	 */
	public Boolean getVerified() {
		return _Verified;
	}

	/**
	 * Sets the value for verified.
	 * @param v Value to Set.
	 */
	public void setVerified(Object v){
		if(v instanceof Boolean){
			_Verified=(Boolean)v;
		}else if(v instanceof String){
			_Verified=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private String _Salt=null;

	/**
	 * @return Returns the salt.
	 */
	public String getSalt(){
		return _Salt;
	}

	/**
	 * Sets the value for salt.
	 * @param v Value to Set.
	 */
	public void setSalt(String v){
		_Salt=v;
	}

	//FIELD

	private Integer _XdatUserId=null;

	/**
	 * @return Returns the xdat_user_id.
	 */
	public Integer getXdatUserId() {
		return _XdatUserId;
	}

	/**
	 * Sets the value for xdat_user_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserId(Integer v){
		_XdatUserId=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login")){
			setLogin(v);
		}else if (xmlPath.equals("firstname")){
			setFirstname(v);
		}else if (xmlPath.equals("lastname")){
			setLastname(v);
		}else if (xmlPath.equals("email")){
			setEmail(v);
		}else if (xmlPath.equals("primary_password")){
			setPrimaryPassword(v);
		}else if (xmlPath.equals("primary_password/encrypt")){
			setPrimaryPassword_encrypt(v);
		}else if (xmlPath.equals("quarantine_path")){
			setQuarantinePath(v);
		}else if (xmlPath.equals("enabled")){
			setEnabled(v);
		}else if (xmlPath.equals("verified")){
			setVerified(v);
		}else if (xmlPath.equals("salt")){
			setSalt(v);
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
		}else if (xmlPath.equals("assigned_roles/assigned_role")){
			addAssignedRoles_assignedRole(v);
		}else if (xmlPath.equals("groups/groupID")){
			addGroups_groupid(v);
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
		if (xmlPath.equals("login")){
			return getLogin();
		}else if (xmlPath.equals("firstname")){
			return getFirstname();
		}else if (xmlPath.equals("lastname")){
			return getLastname();
		}else if (xmlPath.equals("email")){
			return getEmail();
		}else if (xmlPath.equals("primary_password")){
			return getPrimaryPassword();
		}else if (xmlPath.equals("primary_password/encrypt")){
			return getPrimaryPassword_encrypt();
		}else if (xmlPath.equals("quarantine_path")){
			return getQuarantinePath();
		}else if (xmlPath.equals("enabled")){
			return getEnabled();
		}else if (xmlPath.equals("verified")){
			return getVerified();
		}else if (xmlPath.equals("salt")){
			return getSalt();
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
		if (xmlPath.equals("element_access")){
			return getElementAccess();
		}else if (xmlPath.equals("assigned_roles/assigned_role")){
			return getAssignedRoles_assignedRole();
		}else if (xmlPath.equals("groups/groupID")){
			return getGroups_groupid();
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
		if (xmlPath.equals("element_access")){
			return "http://nrg.wustl.edu/security:element_access";
		}else if (xmlPath.equals("assigned_roles/assigned_role")){
			return "http://nrg.wustl.edu/security:role_type";
		}else if (xmlPath.equals("groups/groupID")){
			return "http://nrg.wustl.edu/security:user_groupID";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("login")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("firstname")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("lastname")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("email")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("primary_password")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("primary_password/encrypt")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("element_access")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("assigned_roles/assigned_role")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("quarantine_path")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("groups/groupID")){
			return BaseElement.field_inline_repeater;
		}else if (xmlPath.equals("enabled")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("verified")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("salt")){
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
		all_fields.add("login");
		all_fields.add("firstname");
		all_fields.add("lastname");
		all_fields.add("email");
		all_fields.add("primary_password");
		all_fields.add("primary_password/encrypt");
		all_fields.add("element_access");
		all_fields.add("assigned_roles/assigned_role");
		all_fields.add("quarantine_path");
		all_fields.add("groups/groupID");
		all_fields.add("enabled");
		all_fields.add("verified");
		all_fields.add("salt");
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
		writer.write("\n<xdat:XDATUser");
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
		writer.write("\n</xdat:XDATUser>");
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
		if (_Enabled!=null)
			map.put("enabled",ValueParser(_Enabled,"boolean"));
		//NOT REQUIRED FIELD

		if (_Verified!=null)
			map.put("verified",ValueParser(_Verified,"boolean"));
		//NOT REQUIRED FIELD

		if (_Salt!=null)
			map.put("salt",ValueParser(_Salt,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
		if (_Login!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:login");
			writer.write(">");
			writer.write(ValueParser(_Login,"string"));
			writer.write("</xdat:login>");
			header--;
		}
		if (_Firstname!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:firstname");
			writer.write(">");
			writer.write(ValueParser(_Firstname,"string"));
			writer.write("</xdat:firstname>");
			header--;
		}
		if (_Lastname!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:lastname");
			writer.write(">");
			writer.write(ValueParser(_Lastname,"string"));
			writer.write("</xdat:lastname>");
			header--;
		}
		if (_Email!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:email");
			writer.write(">");
			writer.write(ValueParser(_Email,"string"));
			writer.write("</xdat:email>");
			header--;
		}
		TreeMap PrimaryPasswordATTMap = new TreeMap();
		String PrimaryPasswordATT = new String();
		if (_PrimaryPassword_encrypt!=null)
			PrimaryPasswordATTMap.put("encrypt",ValueParser(_PrimaryPassword_encrypt,"boolean"));
		java.util.Iterator iter0 =PrimaryPasswordATTMap.keySet().iterator();
		while(iter0.hasNext()){
			String key = (String)iter0.next();
			PrimaryPasswordATT +=" " + key + "=\"" + PrimaryPasswordATTMap.get(key) + "\"";
		}
		if (_PrimaryPassword!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:primary_password");
			writer.write(PrimaryPasswordATT);
			writer.write(">");
			writer.write(ValueParser(_PrimaryPassword,"string"));
			writer.write("</xdat:primary_password>");
			header--;
		}
		else if(!PrimaryPasswordATT.equals("")){
			writer.write("\n" + createHeader(header++) + "<xdat:primary_password");
			writer.write(PrimaryPasswordATT);
			writer.write("/>");
			header--;
		}

		//REFERENCE FROM user -> element_access
		java.util.Iterator iter1=_ElementAccess.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatElementAccessBean child = (org.nrg.xdat.bean.XdatElementAccessBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:element_access");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:element_access")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:element_access>");
				}else{
					writer.write("</xdat:element_access>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
			int child2=0;
			int att2=0;
			child2+=_AssignedRoles_assignedRole.size();
			if(child2>0 || att2>0){
				writer.write("\n" + createHeader(header++) + "<xdat:assigned_roles");
			if(child2==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM user -> assigned_roles/assigned_role
		java.util.Iterator iter3=_AssignedRoles_assignedRole.iterator();
		while(iter3.hasNext()){
			org.nrg.xdat.bean.XdatRoleTypeBean child = (org.nrg.xdat.bean.XdatRoleTypeBean)iter3.next();
			writer.write("\n" + createHeader(header++) + "<xdat:assigned_role");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:role_type")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return4 =child.addXMLBody(writer,header);
				if(return4){
					writer.write("\n" + createHeader(--header) + "</xdat:assigned_role>");
				}else{
					writer.write("</xdat:assigned_role>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:assigned_roles>");
			}
			}

		if (_QuarantinePath!=null){
			writer.write("\n" + createHeader(header++) + "<xdat:quarantine_path");
			writer.write(">");
			writer.write(ValueParser(_QuarantinePath,"string"));
			writer.write("</xdat:quarantine_path>");
			header--;
		}
			int child4=0;
			int att4=0;
			child4+=_Groups_groupid.size();
			if(child4>0 || att4>0){
				writer.write("\n" + createHeader(header++) + "<xdat:groups");
			if(child4==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM user -> groups/groupID
		//IN-LINE REPEATER
		java.util.Iterator iter5=_Groups_groupid.iterator();
		while(iter5.hasNext()){
			org.nrg.xdat.bean.XdatUserGroupidBean child = (org.nrg.xdat.bean.XdatUserGroupidBean)iter5.next();
			child.addXMLBody(writer,header);
		}
				writer.write("\n" + createHeader(--header) + "</xdat:groups>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
		if (_Login!=null) return true;
		if (_Firstname!=null) return true;
		if (_Lastname!=null) return true;
		if (_Email!=null) return true;
		if (_PrimaryPassword_encrypt!=null)
			return true;
		if (_PrimaryPassword!=null) return true;
		if(_ElementAccess.size()>0) return true;
			if(_AssignedRoles_assignedRole.size()>0)return true;
		if (_QuarantinePath!=null) return true;
			if(_Groups_groupid.size()>0)return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
