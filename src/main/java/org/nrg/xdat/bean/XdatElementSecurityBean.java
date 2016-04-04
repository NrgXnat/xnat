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
public class XdatElementSecurityBean extends BaseElement implements java.io.Serializable, org.nrg.xdat.model.XdatElementSecurityI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(XdatElementSecurityBean.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:element_security";

	public String getSchemaElementName(){
		return "element_security";
	}

	public String getFullSchemaElementName(){
		return "xdat:element_security";
	}
	 private List<org.nrg.xdat.bean.XdatPrimarySecurityFieldBean> _PrimarySecurityFields_primarySecurityField =new ArrayList<org.nrg.xdat.bean.XdatPrimarySecurityFieldBean>();

	/**
	 * primary_security_fields/primary_security_field
	 * @return Returns an List of org.nrg.xdat.bean.XdatPrimarySecurityFieldBean
	 */
	public <A extends org.nrg.xdat.model.XdatPrimarySecurityFieldI> List<A> getPrimarySecurityFields_primarySecurityField() {
		return (List<A>) _PrimarySecurityFields_primarySecurityField;
	}

	/**
	 * Sets the value for primary_security_fields/primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityFields_primarySecurityField(ArrayList<org.nrg.xdat.bean.XdatPrimarySecurityFieldBean> v){
		_PrimarySecurityFields_primarySecurityField=v;
	}

	/**
	 * Adds the value for primary_security_fields/primary_security_field.
	 * @param v Value to Set.
	 */
	public void addPrimarySecurityFields_primarySecurityField(org.nrg.xdat.bean.XdatPrimarySecurityFieldBean v){
		_PrimarySecurityFields_primarySecurityField.add(v);
	}

	/**
	 * primary_security_fields/primary_security_field
	 * Adds org.nrg.xdat.model.XdatPrimarySecurityFieldI
	 */
	public <A extends org.nrg.xdat.model.XdatPrimarySecurityFieldI> void addPrimarySecurityFields_primarySecurityField(A item) throws Exception{
	_PrimarySecurityFields_primarySecurityField.add((org.nrg.xdat.bean.XdatPrimarySecurityFieldBean)item);
	}

	/**
	 * Adds the value for primary_security_fields/primary_security_field.
	 * @param v Value to Set.
	 */
	public void addPrimarySecurityFields_primarySecurityField(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatPrimarySecurityFieldBean)
			_PrimarySecurityFields_primarySecurityField.add((org.nrg.xdat.bean.XdatPrimarySecurityFieldBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatPrimarySecurityFieldBean");
	}
	 private List<org.nrg.xdat.bean.XdatElementActionTypeBean> _ElementActions_elementAction =new ArrayList<org.nrg.xdat.bean.XdatElementActionTypeBean>();

	/**
	 * element_actions/element_action
	 * @return Returns an List of org.nrg.xdat.bean.XdatElementActionTypeBean
	 */
	public <A extends org.nrg.xdat.model.XdatElementActionTypeI> List<A> getElementActions_elementAction() {
		return (List<A>) _ElementActions_elementAction;
	}

	/**
	 * Sets the value for element_actions/element_action.
	 * @param v Value to Set.
	 */
	public void setElementActions_elementAction(ArrayList<org.nrg.xdat.bean.XdatElementActionTypeBean> v){
		_ElementActions_elementAction=v;
	}

	/**
	 * Adds the value for element_actions/element_action.
	 * @param v Value to Set.
	 */
	public void addElementActions_elementAction(org.nrg.xdat.bean.XdatElementActionTypeBean v){
		_ElementActions_elementAction.add(v);
	}

	/**
	 * element_actions/element_action
	 * Adds org.nrg.xdat.model.XdatElementActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatElementActionTypeI> void addElementActions_elementAction(A item) throws Exception{
	_ElementActions_elementAction.add((org.nrg.xdat.bean.XdatElementActionTypeBean)item);
	}

	/**
	 * Adds the value for element_actions/element_action.
	 * @param v Value to Set.
	 */
	public void addElementActions_elementAction(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatElementActionTypeBean)
			_ElementActions_elementAction.add((org.nrg.xdat.bean.XdatElementActionTypeBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatElementActionTypeBean");
	}
	 private List<org.nrg.xdat.bean.XdatElementSecurityListingActionBean> _ListingActions_listingAction =new ArrayList<org.nrg.xdat.bean.XdatElementSecurityListingActionBean>();

	/**
	 * listing_actions/listing_action
	 * @return Returns an List of org.nrg.xdat.bean.XdatElementSecurityListingActionBean
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityListingActionI> List<A> getListingActions_listingAction() {
		return (List<A>) _ListingActions_listingAction;
	}

	/**
	 * Sets the value for listing_actions/listing_action.
	 * @param v Value to Set.
	 */
	public void setListingActions_listingAction(ArrayList<org.nrg.xdat.bean.XdatElementSecurityListingActionBean> v){
		_ListingActions_listingAction=v;
	}

	/**
	 * Adds the value for listing_actions/listing_action.
	 * @param v Value to Set.
	 */
	public void addListingActions_listingAction(org.nrg.xdat.bean.XdatElementSecurityListingActionBean v){
		_ListingActions_listingAction.add(v);
	}

	/**
	 * listing_actions/listing_action
	 * Adds org.nrg.xdat.model.XdatElementSecurityListingActionI
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityListingActionI> void addListingActions_listingAction(A item) throws Exception{
	_ListingActions_listingAction.add((org.nrg.xdat.bean.XdatElementSecurityListingActionBean)item);
	}

	/**
	 * Adds the value for listing_actions/listing_action.
	 * @param v Value to Set.
	 */
	public void addListingActions_listingAction(Object v){
		if (v instanceof org.nrg.xdat.bean.XdatElementSecurityListingActionBean)
			_ListingActions_listingAction.add((org.nrg.xdat.bean.XdatElementSecurityListingActionBean)v);
		else
			throw new IllegalArgumentException("Must be a valid org.nrg.xdat.bean.XdatElementSecurityListingActionBean");
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

	private Boolean _SecondaryPassword=null;

	/**
	 * @return Returns the secondary_password.
	 */
	public Boolean getSecondaryPassword() {
		return _SecondaryPassword;
	}

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(Object v){
		if(v instanceof Boolean){
			_SecondaryPassword=(Boolean)v;
		}else if(v instanceof String){
			_SecondaryPassword=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _SecureIp=null;

	/**
	 * @return Returns the secure_ip.
	 */
	public Boolean getSecureIp() {
		return _SecureIp;
	}

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(Object v){
		if(v instanceof Boolean){
			_SecureIp=(Boolean)v;
		}else if(v instanceof String){
			_SecureIp=formatBoolean((String)v);
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

	private Boolean _Browse=null;

	/**
	 * @return Returns the browse.
	 */
	public Boolean getBrowse() {
		return _Browse;
	}

	/**
	 * Sets the value for browse.
	 * @param v Value to Set.
	 */
	public void setBrowse(Object v){
		if(v instanceof Boolean){
			_Browse=(Boolean)v;
		}else if(v instanceof String){
			_Browse=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
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
	 * Sets the value for xdat:element_security/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v) {
		_Sequence=v;
	}

	/**
	 * Sets the value for xdat:element_security/sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(String v)  {
		_Sequence=formatInteger(v);
	}

	//FIELD

	private Boolean _Quarantine=null;

	/**
	 * @return Returns the quarantine.
	 */
	public Boolean getQuarantine() {
		return _Quarantine;
	}

	/**
	 * Sets the value for quarantine.
	 * @param v Value to Set.
	 */
	public void setQuarantine(Object v){
		if(v instanceof Boolean){
			_Quarantine=(Boolean)v;
		}else if(v instanceof String){
			_Quarantine=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _PreLoad=null;

	/**
	 * @return Returns the pre_load.
	 */
	public Boolean getPreLoad() {
		return _PreLoad;
	}

	/**
	 * Sets the value for pre_load.
	 * @param v Value to Set.
	 */
	public void setPreLoad(Object v){
		if(v instanceof Boolean){
			_PreLoad=(Boolean)v;
		}else if(v instanceof String){
			_PreLoad=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _Searchable=null;

	/**
	 * @return Returns the searchable.
	 */
	public Boolean getSearchable() {
		return _Searchable;
	}

	/**
	 * Sets the value for searchable.
	 * @param v Value to Set.
	 */
	public void setSearchable(Object v){
		if(v instanceof Boolean){
			_Searchable=(Boolean)v;
		}else if(v instanceof String){
			_Searchable=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _SecureRead=null;

	/**
	 * @return Returns the secure_read.
	 */
	public Boolean getSecureRead() {
		return _SecureRead;
	}

	/**
	 * Sets the value for secure_read.
	 * @param v Value to Set.
	 */
	public void setSecureRead(Object v){
		if(v instanceof Boolean){
			_SecureRead=(Boolean)v;
		}else if(v instanceof String){
			_SecureRead=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _SecureEdit=null;

	/**
	 * @return Returns the secure_edit.
	 */
	public Boolean getSecureEdit() {
		return _SecureEdit;
	}

	/**
	 * Sets the value for secure_edit.
	 * @param v Value to Set.
	 */
	public void setSecureEdit(Object v){
		if(v instanceof Boolean){
			_SecureEdit=(Boolean)v;
		}else if(v instanceof String){
			_SecureEdit=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _SecureCreate=null;

	/**
	 * @return Returns the secure_create.
	 */
	public Boolean getSecureCreate() {
		return _SecureCreate;
	}

	/**
	 * Sets the value for secure_create.
	 * @param v Value to Set.
	 */
	public void setSecureCreate(Object v){
		if(v instanceof Boolean){
			_SecureCreate=(Boolean)v;
		}else if(v instanceof String){
			_SecureCreate=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _SecureDelete=null;

	/**
	 * @return Returns the secure_delete.
	 */
	public Boolean getSecureDelete() {
		return _SecureDelete;
	}

	/**
	 * Sets the value for secure_delete.
	 * @param v Value to Set.
	 */
	public void setSecureDelete(Object v){
		if(v instanceof Boolean){
			_SecureDelete=(Boolean)v;
		}else if(v instanceof String){
			_SecureDelete=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private Boolean _Accessible=null;

	/**
	 * @return Returns the accessible.
	 */
	public Boolean getAccessible() {
		return _Accessible;
	}

	/**
	 * Sets the value for accessible.
	 * @param v Value to Set.
	 */
	public void setAccessible(Object v){
		if(v instanceof Boolean){
			_Accessible=(Boolean)v;
		}else if(v instanceof String){
			_Accessible=formatBoolean((String)v);
		}else if(v!=null){
			throw new IllegalArgumentException();
		}
	}

	//FIELD

	private String _Usage=null;

	/**
	 * @return Returns the usage.
	 */
	public String getUsage(){
		return _Usage;
	}

	/**
	 * Sets the value for usage.
	 * @param v Value to Set.
	 */
	public void setUsage(String v){
		_Usage=v;
	}

	//FIELD

	private String _Singular=null;

	/**
	 * @return Returns the singular.
	 */
	public String getSingular(){
		return _Singular;
	}

	/**
	 * Sets the value for singular.
	 * @param v Value to Set.
	 */
	public void setSingular(String v){
		_Singular=v;
	}

	//FIELD

	private String _Plural=null;

	/**
	 * @return Returns the plural.
	 */
	public String getPlural(){
		return _Plural;
	}

	/**
	 * Sets the value for plural.
	 * @param v Value to Set.
	 */
	public void setPlural(String v){
		_Plural=v;
	}

	//FIELD

	private String _Category=null;

	/**
	 * @return Returns the category.
	 */
	public String getCategory(){
		return _Category;
	}

	/**
	 * Sets the value for category.
	 * @param v Value to Set.
	 */
	public void setCategory(String v){
		_Category=v;
	}

	//FIELD

	private String _Code=null;

	/**
	 * @return Returns the code.
	 */
	public String getCode(){
		return _Code;
	}

	/**
	 * Sets the value for code.
	 * @param v Value to Set.
	 */
	public void setCode(String v){
		_Code=v;
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	public void setDataField(String xmlPath,String v) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("element_name")){
			setElementName(v);
		}else if (xmlPath.equals("secondary_password")){
			setSecondaryPassword(v);
		}else if (xmlPath.equals("secure_ip")){
			setSecureIp(v);
		}else if (xmlPath.equals("secure")){
			setSecure(v);
		}else if (xmlPath.equals("browse")){
			setBrowse(v);
		}else if (xmlPath.equals("sequence")){
			setSequence(v);
		}else if (xmlPath.equals("quarantine")){
			setQuarantine(v);
		}else if (xmlPath.equals("pre_load")){
			setPreLoad(v);
		}else if (xmlPath.equals("searchable")){
			setSearchable(v);
		}else if (xmlPath.equals("secure_read")){
			setSecureRead(v);
		}else if (xmlPath.equals("secure_edit")){
			setSecureEdit(v);
		}else if (xmlPath.equals("secure_create")){
			setSecureCreate(v);
		}else if (xmlPath.equals("secure_delete")){
			setSecureDelete(v);
		}else if (xmlPath.equals("accessible")){
			setAccessible(v);
		}else if (xmlPath.equals("usage")){
			setUsage(v);
		}else if (xmlPath.equals("singular")){
			setSingular(v);
		}else if (xmlPath.equals("plural")){
			setPlural(v);
		}else if (xmlPath.equals("category")){
			setCategory(v);
		}else if (xmlPath.equals("code")){
			setCode(v);
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
		if (xmlPath.equals("primary_security_fields/primary_security_field")){
			addPrimarySecurityFields_primarySecurityField(v);
		}else if (xmlPath.equals("element_actions/element_action")){
			addElementActions_elementAction(v);
		}else if (xmlPath.equals("listing_actions/listing_action")){
			addListingActions_listingAction(v);
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
		if (xmlPath.equals("element_name")){
			return getElementName();
		}else if (xmlPath.equals("secondary_password")){
			return getSecondaryPassword();
		}else if (xmlPath.equals("secure_ip")){
			return getSecureIp();
		}else if (xmlPath.equals("secure")){
			return getSecure();
		}else if (xmlPath.equals("browse")){
			return getBrowse();
		}else if (xmlPath.equals("sequence")){
			return getSequence();
		}else if (xmlPath.equals("quarantine")){
			return getQuarantine();
		}else if (xmlPath.equals("pre_load")){
			return getPreLoad();
		}else if (xmlPath.equals("searchable")){
			return getSearchable();
		}else if (xmlPath.equals("secure_read")){
			return getSecureRead();
		}else if (xmlPath.equals("secure_edit")){
			return getSecureEdit();
		}else if (xmlPath.equals("secure_create")){
			return getSecureCreate();
		}else if (xmlPath.equals("secure_delete")){
			return getSecureDelete();
		}else if (xmlPath.equals("accessible")){
			return getAccessible();
		}else if (xmlPath.equals("usage")){
			return getUsage();
		}else if (xmlPath.equals("singular")){
			return getSingular();
		}else if (xmlPath.equals("plural")){
			return getPlural();
		}else if (xmlPath.equals("category")){
			return getCategory();
		}else if (xmlPath.equals("code")){
			return getCode();
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
		if (xmlPath.equals("primary_security_fields/primary_security_field")){
			return getPrimarySecurityFields_primarySecurityField();
		}else if (xmlPath.equals("element_actions/element_action")){
			return getElementActions_elementAction();
		}else if (xmlPath.equals("listing_actions/listing_action")){
			return getListingActions_listingAction();
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
		if (xmlPath.equals("primary_security_fields/primary_security_field")){
			return "http://nrg.wustl.edu/security:primary_security_field";
		}else if (xmlPath.equals("element_actions/element_action")){
			return "http://nrg.wustl.edu/security:element_action_type";
		}else if (xmlPath.equals("listing_actions/listing_action")){
			return "http://nrg.wustl.edu/security:element_security_listing_action";
		}
		else{
			return super.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	public String getFieldType(String xmlPath) throws BaseElement.UnknownFieldException{
		if (xmlPath.equals("primary_security_fields/primary_security_field")){
			return BaseElement.field_NO_CHILD;
		}else if (xmlPath.equals("element_actions/element_action")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("listing_actions/listing_action")){
			return BaseElement.field_multi_reference;
		}else if (xmlPath.equals("element_name")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secondary_password")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_ip")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("browse")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("sequence")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("quarantine")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("pre_load")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("searchable")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_read")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_edit")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_create")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("secure_delete")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("accessible")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("usage")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("singular")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("plural")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("category")){
			return BaseElement.field_data;
		}else if (xmlPath.equals("code")){
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
		all_fields.add("primary_security_fields/primary_security_field");
		all_fields.add("element_actions/element_action");
		all_fields.add("listing_actions/listing_action");
		all_fields.add("element_name");
		all_fields.add("secondary_password");
		all_fields.add("secure_ip");
		all_fields.add("secure");
		all_fields.add("browse");
		all_fields.add("sequence");
		all_fields.add("quarantine");
		all_fields.add("pre_load");
		all_fields.add("searchable");
		all_fields.add("secure_read");
		all_fields.add("secure_edit");
		all_fields.add("secure_create");
		all_fields.add("secure_delete");
		all_fields.add("accessible");
		all_fields.add("usage");
		all_fields.add("singular");
		all_fields.add("plural");
		all_fields.add("category");
		all_fields.add("code");
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
		writer.write("\n<xdat:element_security");
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
		writer.write("\n</xdat:element_security>");
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
		if (_ElementName!=null)
			map.put("element_name",ValueParser(_ElementName,"string"));
		else map.put("element_name","");//REQUIRED FIELD

		if (_SecondaryPassword!=null)
			map.put("secondary_password",ValueParser(_SecondaryPassword,"boolean"));
		//NOT REQUIRED FIELD

		if (_SecureIp!=null)
			map.put("secure_ip",ValueParser(_SecureIp,"boolean"));
		//NOT REQUIRED FIELD

		if (_Secure!=null)
			map.put("secure",ValueParser(_Secure,"boolean"));
		//NOT REQUIRED FIELD

		if (_Browse!=null)
			map.put("browse",ValueParser(_Browse,"boolean"));
		//NOT REQUIRED FIELD

		if (_Sequence!=null)
			map.put("sequence",ValueParser(_Sequence,"integer"));
		//NOT REQUIRED FIELD

		if (_Quarantine!=null)
			map.put("quarantine",ValueParser(_Quarantine,"boolean"));
		//NOT REQUIRED FIELD

		if (_PreLoad!=null)
			map.put("pre_load",ValueParser(_PreLoad,"boolean"));
		//NOT REQUIRED FIELD

		if (_Searchable!=null)
			map.put("searchable",ValueParser(_Searchable,"boolean"));
		//NOT REQUIRED FIELD

		if (_SecureRead!=null)
			map.put("secure_read",ValueParser(_SecureRead,"boolean"));
		//NOT REQUIRED FIELD

		if (_SecureEdit!=null)
			map.put("secure_edit",ValueParser(_SecureEdit,"boolean"));
		//NOT REQUIRED FIELD

		if (_SecureCreate!=null)
			map.put("secure_create",ValueParser(_SecureCreate,"boolean"));
		//NOT REQUIRED FIELD

		if (_SecureDelete!=null)
			map.put("secure_delete",ValueParser(_SecureDelete,"boolean"));
		//NOT REQUIRED FIELD

		if (_Accessible!=null)
			map.put("accessible",ValueParser(_Accessible,"boolean"));
		//NOT REQUIRED FIELD

		if (_Usage!=null)
			map.put("usage",ValueParser(_Usage,"string"));
		//NOT REQUIRED FIELD

		if (_Singular!=null)
			map.put("singular",ValueParser(_Singular,"string"));
		//NOT REQUIRED FIELD

		if (_Plural!=null)
			map.put("plural",ValueParser(_Plural,"string"));
		//NOT REQUIRED FIELD

		if (_Category!=null)
			map.put("category",ValueParser(_Category,"string"));
		//NOT REQUIRED FIELD

		if (_Code!=null)
			map.put("code",ValueParser(_Code,"string"));
		//NOT REQUIRED FIELD

		return map;
	}


	protected boolean addXMLBody(java.io.Writer writer, int header) throws java.io.IOException{
		super.addXMLBody(writer,header);
			int child0=0;
			int att0=0;
			child0+=_PrimarySecurityFields_primarySecurityField.size();
			if(child0>0 || att0>0){
				writer.write("\n" + createHeader(header++) + "<xdat:primary_security_fields");
			if(child0==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM element_security -> primary_security_fields/primary_security_field
		java.util.Iterator iter1=_PrimarySecurityFields_primarySecurityField.iterator();
		while(iter1.hasNext()){
			org.nrg.xdat.bean.XdatPrimarySecurityFieldBean child = (org.nrg.xdat.bean.XdatPrimarySecurityFieldBean)iter1.next();
			writer.write("\n" + createHeader(header++) + "<xdat:primary_security_field");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:primary_security_field")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return2 =child.addXMLBody(writer,header);
				if(return2){
					writer.write("\n" + createHeader(--header) + "</xdat:primary_security_field>");
				}else{
					writer.write("</xdat:primary_security_field>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:primary_security_fields>");
			}
			}

			int child2=0;
			int att2=0;
			child2+=_ElementActions_elementAction.size();
			if(child2>0 || att2>0){
				writer.write("\n" + createHeader(header++) + "<xdat:element_actions");
			if(child2==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM element_security -> element_actions/element_action
		java.util.Iterator iter3=_ElementActions_elementAction.iterator();
		while(iter3.hasNext()){
			org.nrg.xdat.bean.XdatElementActionTypeBean child = (org.nrg.xdat.bean.XdatElementActionTypeBean)iter3.next();
			writer.write("\n" + createHeader(header++) + "<xdat:element_action");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:element_action_type")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return4 =child.addXMLBody(writer,header);
				if(return4){
					writer.write("\n" + createHeader(--header) + "</xdat:element_action>");
				}else{
					writer.write("</xdat:element_action>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:element_actions>");
			}
			}

			int child4=0;
			int att4=0;
			child4+=_ListingActions_listingAction.size();
			if(child4>0 || att4>0){
				writer.write("\n" + createHeader(header++) + "<xdat:listing_actions");
			if(child4==0){
				writer.write("/>");
			}else{
				writer.write(">");
		//REFERENCE FROM element_security -> listing_actions/listing_action
		java.util.Iterator iter5=_ListingActions_listingAction.iterator();
		while(iter5.hasNext()){
			org.nrg.xdat.bean.XdatElementSecurityListingActionBean child = (org.nrg.xdat.bean.XdatElementSecurityListingActionBean)iter5.next();
			writer.write("\n" + createHeader(header++) + "<xdat:listing_action");
			child.addXMLAtts(writer);
			if(!child.getFullSchemaElementName().equals("xdat:element_security_listing_action")){
				writer.write(" xsi:type=\"" + child.getFullSchemaElementName() + "\"");
			}
			if (child.hasXMLBodyContent()){
				writer.write(">");
				boolean return6 =child.addXMLBody(writer,header);
				if(return6){
					writer.write("\n" + createHeader(--header) + "</xdat:listing_action>");
				}else{
					writer.write("</xdat:listing_action>");
					header--;
				}
			}else {writer.write("/>");header--;}
		}
				writer.write("\n" + createHeader(--header) + "</xdat:listing_actions>");
			}
			}

	return true;
	}


	protected boolean hasXMLBodyContent(){
			if(_PrimarySecurityFields_primarySecurityField.size()>0)return true;
			if(_ElementActions_elementAction.size()>0)return true;
			if(_ListingActions_listingAction.size()>0)return true;
		if(super.hasXMLBodyContent())return true;
		return false;
	}
}
