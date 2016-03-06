/*
 * org.nrg.xdat.om.base.auto.AutoXdatUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/11/13 3:34 PM
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatRoleType;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.om.XdatUserI;
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
public abstract class AutoXdatUser extends org.nrg.xdat.base.BaseElement implements XdatUserI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatUser.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:user";

	public AutoXdatUser(ItemI item)
	{
		super(item);
	}

	public AutoXdatUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatUser(UserI user)
	 **/
	public AutoXdatUser(){}

	public AutoXdatUser(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	/**
	 * {@inheritDoc}
     */
	public String getSchemaElementName(){
		return "xdat:user";
	}

	//FIELD

	private String _Login=null;

	/**
	 * {@inheritDoc}
	 */
	public String getLogin(){
		try{
			if (_Login==null){
				_Login=getStringProperty("login");
				return _Login;
			}else {
				return _Login;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLogin(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/login",v);
		_Login=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Firstname=null;

	/**
	 * {@inheritDoc}
	 */
	public String getFirstname(){
		try{
			if (_Firstname==null){
				_Firstname=getStringProperty("firstname");
				return _Firstname;
			}else {
				return _Firstname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFirstname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/firstname",v);
		_Firstname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Lastname=null;

	/**
	 * {@inheritDoc}
	 */
	public String getLastname(){
		try{
			if (_Lastname==null){
				_Lastname=getStringProperty("lastname");
				return _Lastname;
			}else {
				return _Lastname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLastname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/lastname",v);
		_Lastname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Email=null;

	/**
	 * {@inheritDoc}
	 */
	public String getEmail(){
		try{
			if (_Email==null){
				_Email=getStringProperty("email");
				return _Email;
			}else {
				return _Email;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEmail(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/email",v);
		_Email=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _PrimaryPassword=null;

	/**
	 * {@inheritDoc}
	 */
	public String getPrimaryPassword(){
		try{
			if (_PrimaryPassword==null){
				_PrimaryPassword=getStringProperty("primary_password");
				return _PrimaryPassword;
			}else {
				return _PrimaryPassword;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrimaryPassword(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/primary_password",v);
		_PrimaryPassword=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _PrimaryPassword_encrypt=null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean getPrimaryPassword_encrypt() {
		try{
			if (_PrimaryPassword_encrypt==null){
				_PrimaryPassword_encrypt=getBooleanProperty("primary_password/encrypt");
				return _PrimaryPassword_encrypt;
			}else {
				return _PrimaryPassword_encrypt;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPrimaryPassword_encrypt(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/primary_password/encrypt",v);
		_PrimaryPassword_encrypt=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<XdatElementAccess> _ElementAccess =null;

    //FIELD

    private String _Salt=null;

    /**
     * {@inheritDoc}
     */
    public String getSalt(){
        try{
            if (_Salt==null){
            	_Salt=getStringProperty("salt");
                return _Salt;
            }else {
                return _Salt;
            }
        } catch (Exception e1) {logger.error(e1);return null;}
    }

    /**
     * {@inheritDoc}
     */
    public void setSalt(String v){
        try{
            setProperty(SCHEMA_ELEMENT_NAME + "/salt",v);
            _Salt=null;
        } catch (Exception e1) {logger.error(e1);}
    }

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<XdatElementAccess> getElementAccess() {
		try{
			if (_ElementAccess==null){
				_ElementAccess=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("element_access"));
				return _ElementAccess;
			}else {
				return _ElementAccess;
			}
		} catch (Exception e1) {return new ArrayList<>();}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setElementAccess(ItemI v) throws Exception{
		_ElementAccess =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_access",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_access",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the element_access of the given index.
	 * @param index Index of child to remove.
	 */
    @SuppressWarnings("unused")
	public void removeElementAccess(int index) throws java.lang.IndexOutOfBoundsException {
		_ElementAccess =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/element_access",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatRoleType> _AssignedRoles_assignedRole =null;

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<org.nrg.xdat.om.XdatRoleType> getAssignedRoles_assignedRole() {
		try{
			if (_AssignedRoles_assignedRole==null){
				_AssignedRoles_assignedRole=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("assigned_roles/assigned_role"));
				return _AssignedRoles_assignedRole;
			}else {
				return _AssignedRoles_assignedRole;
			}
		} catch (Exception e1) {return new ArrayList<>();}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAssignedRoles_assignedRole(ItemI v) throws Exception{
		_AssignedRoles_assignedRole =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/assigned_roles/assigned_role",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/assigned_roles/assigned_role",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the assigned_roles/assigned_role of the given index.
	 * @param index Index of child to remove.
	 */
    @SuppressWarnings("unused")
	public void removeAssignedRoles_assignedRole(int index) throws java.lang.IndexOutOfBoundsException {
		_AssignedRoles_assignedRole =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/assigned_roles/assigned_role",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _QuarantinePath=null;

	/**
	 * {@inheritDoc}
	 */
	public String getQuarantinePath(){
		try{
			if (_QuarantinePath==null){
				_QuarantinePath=getStringProperty("quarantine_path");
				return _QuarantinePath;
			}else {
				return _QuarantinePath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setQuarantinePath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/quarantine_path",v);
		_QuarantinePath=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatUserGroupid> _Groups_groupid =null;

	/**
	 * groups/groupID
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatUserGroupid
	 */
	public ArrayList<org.nrg.xdat.om.XdatUserGroupid> getGroups_groupid() {
		try{
			if (_Groups_groupid==null){
				_Groups_groupid=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("groups/groupID"));
				return _Groups_groupid;
			}else {
				return _Groups_groupid;
			}
		} catch (Exception e1) {return new ArrayList<>();}
	}

	/**
	 * Sets the value for groups/groupID.
	 * @param v Value to Set.
     * @throws Exception When an error occurs.
	 */
    @SuppressWarnings("unused")
    public void setGroups_groupid(ItemI v) throws Exception{
		_Groups_groupid =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/groups/groupID",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/groups/groupID",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the groups/groupID of the given index.
	 * @param index Index of child to remove.
	 */
    @SuppressWarnings("unused")
    public void removeGroups_groupid(int index) throws java.lang.IndexOutOfBoundsException {
		_Groups_groupid =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/groups/groupID",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Enabled=null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean getEnabled() {
		try{
			if (_Enabled==null){
				_Enabled=getBooleanProperty("enabled");
				return _Enabled;
			}else {
				return _Enabled;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEnabled(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/enabled",v);
		_Enabled=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	
	//FIELD

	private Boolean _Verified=null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean getVerified() {
		try{
			if (_Verified==null){
				_Verified=getBooleanProperty("verified");
				return _Verified;
			}else {
				return _Verified;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setVerified(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/verified",v);
		_Verified=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatUserId=null;

	/**
	 * {@inheritDoc}
	 */
	public Integer getXdatUserId() {
		try{
			if (_XdatUserId==null){
				_XdatUserId=getIntegerProperty("xdat_user_id");
				return _XdatUserId;
			}else {
				return _XdatUserId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setXdatUserId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_user_id",v);
		_XdatUserId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<XdatUser> getAllXdatUsers(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<XdatUser> al = new ArrayList<>();

        try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<XdatUser> getXdatUsersByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<XdatUser> al = new ArrayList<>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

    @SuppressWarnings("unused")
    public static ArrayList<XdatUser> getXdatUsersByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<XdatUser> al = new ArrayList<>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatUser getXdatUsersByXdatUserId(Integer value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:user/xdat_user_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatUser) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XdatUser getXdatUsersByLogin(String value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:user/login",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatUser) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArrayList wrapItems(ArrayList items)
	{
		return BaseElement.WrapItems(items);
	}

    public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items) {
        return wrapItems(items.getItems());
    }

    public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop) {
        ArrayList<ResourceFile> _return   = new ArrayList<>();
        boolean                 localLoop = preventLoop;

        //element_access
        for (XdatElementAccess childElementAccess : this.getElementAccess()) {
            for (ResourceFile rf : childElementAccess.getFileResources(rootPath, localLoop)) {
                rf.setXpath("element_access[" + childElementAccess.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("element_access/" + childElementAccess.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        localLoop = preventLoop;

        //assigned_roles/assigned_role
        for (XdatRoleType childAssignedRoles_assignedRole : this.getAssignedRoles_assignedRole()) {
            for (ResourceFile rf : childAssignedRoles_assignedRole.getFileResources(rootPath, localLoop)) {
                rf.setXpath("assigned_roles/assigned_role[" + childAssignedRoles_assignedRole.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("assigned_roles/assigned_role/" + childAssignedRoles_assignedRole.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        localLoop = preventLoop;

        //groups/groupID
        for (XdatUserGroupid childGroups_groupid : this.getGroups_groupid()) {
            for (ResourceFile rf : childGroups_groupid.getFileResources(rootPath, localLoop)) {
                rf.setXpath("groups/groupID[" + childGroups_groupid.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("groups/groupID/" + childGroups_groupid.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        return _return;
    }
}
