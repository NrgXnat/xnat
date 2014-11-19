/*
 * org.nrg.xdat.om.base.auto.AutoXdatElementAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatElementAccessI;
import org.nrg.xdat.om.XdatElementAccessSecureIp;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatElementAccess extends org.nrg.xdat.base.BaseElement implements XdatElementAccessI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatElementAccess.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:element_access";

	public AutoXdatElementAccess(ItemI item)
	{
		super(item);
	}

	public AutoXdatElementAccess(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatElementAccess(UserI user)
	 **/
	public AutoXdatElementAccess(){}

	public AutoXdatElementAccess(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:element_access";
	}

	//FIELD

	private String _SecondaryPassword=null;

	/**
	 * @return Returns the secondary_password.
	 */
	public String getSecondaryPassword(){
		try{
			if (_SecondaryPassword==null){
				_SecondaryPassword=getStringProperty("secondary_password");
				return _SecondaryPassword;
			}else {
				return _SecondaryPassword;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/secondary_password",v);
		_SecondaryPassword=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecondaryPassword_encrypt=null;

	/**
	 * @return Returns the secondary_password/encrypt.
	 */
	public Boolean getSecondaryPassword_encrypt() {
		try{
			if (_SecondaryPassword_encrypt==null){
				_SecondaryPassword_encrypt=getBooleanProperty("secondary_password/encrypt");
				return _SecondaryPassword_encrypt;
			}else {
				return _SecondaryPassword_encrypt;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secondary_password/encrypt.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword_encrypt(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secondary_password/encrypt",v);
		_SecondaryPassword_encrypt=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> _SecureIp =null;

	/**
	 * secure_ip
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccessSecureIp
	 */
	public ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> getSecureIp() {
		try{
			if (_SecureIp==null){
				_SecureIp=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("secure_ip"));
				return _SecureIp;
			}else {
				return _SecureIp;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp>();}
	}

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(ItemI v) throws Exception{
		_SecureIp =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/secure_ip",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/secure_ip",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the secure_ip of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSecureIp(int index) throws java.lang.IndexOutOfBoundsException {
		_SecureIp =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/secure_ip",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> _Permissions_allowSet =null;

	/**
	 * permissions/allow_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSet
	 */
	public ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> getPermissions_allowSet() {
		try{
			if (_Permissions_allowSet==null){
				_Permissions_allowSet=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("permissions/allow_set"));
				return _Permissions_allowSet;
			}else {
				return _Permissions_allowSet;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatFieldMappingSet>();}
	}

	/**
	 * Sets the value for permissions/allow_set.
	 * @param v Value to Set.
	 */
	public void setPermissions_allowSet(ItemI v) throws Exception{
		_Permissions_allowSet =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/permissions/allow_set",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/permissions/allow_set",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the permissions/allow_set of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePermissions_allowSet(int index) throws java.lang.IndexOutOfBoundsException {
		_Permissions_allowSet =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/permissions/allow_set",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _ElementName=null;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName(){
		try{
			if (_ElementName==null){
				_ElementName=getStringProperty("element_name");
				return _ElementName;
			}else {
				return _ElementName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/element_name",v);
		_ElementName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatElementAccessId=null;

	/**
	 * @return Returns the xdat_element_access_id.
	 */
	public Integer getXdatElementAccessId() {
		try{
			if (_XdatElementAccessId==null){
				_XdatElementAccessId=getIntegerProperty("xdat_element_access_id");
				return _XdatElementAccessId;
			}else {
				return _XdatElementAccessId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_element_access_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementAccessId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_element_access_id",v);
		_XdatElementAccessId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccess> getAllXdatElementAccesss(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccess> al = new ArrayList<org.nrg.xdat.om.XdatElementAccess>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccess> getXdatElementAccesssByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccess> al = new ArrayList<org.nrg.xdat.om.XdatElementAccess>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccess> getXdatElementAccesssByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccess> al = new ArrayList<org.nrg.xdat.om.XdatElementAccess>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatElementAccess getXdatElementAccesssByXdatElementAccessId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:element_access/xdat_element_access_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatElementAccess) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArrayList wrapItems(ArrayList items)
	{
		ArrayList al = new ArrayList();
		al = org.nrg.xdat.base.BaseElement.WrapItems(items);
		al.trimToSize();
		return al;
	}

	public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items)
	{
		return wrapItems(items.getItems());
	}
public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
	ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //secure_ip
	        for(XdatElementAccessSecureIp childSecureIp : this.getSecureIp()){
	            for(ResourceFile rf: childSecureIp.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("secure_ip[" + childSecureIp.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("secure_ip/" + childSecureIp.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //permissions/allow_set
	        for(XdatFieldMappingSet childPermissions_allowSet : this.getPermissions_allowSet()){
	            for(ResourceFile rf: childPermissions_allowSet.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("permissions/allow_set[" + childPermissions_allowSet.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("permissions/allow_set/" + childPermissions_allowSet.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
