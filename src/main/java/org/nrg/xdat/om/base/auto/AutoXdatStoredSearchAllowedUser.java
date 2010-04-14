// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatStoredSearchAllowedUser;
import org.nrg.xdat.om.XdatStoredSearchAllowedUserI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatStoredSearchAllowedUser extends org.nrg.xdat.base.BaseElement implements XdatStoredSearchAllowedUserI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatStoredSearchAllowedUser.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:stored_search_allowed_user";

	public AutoXdatStoredSearchAllowedUser(ItemI item)
	{
		super(item);
	}

	public AutoXdatStoredSearchAllowedUser(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatStoredSearchAllowedUser(UserI user)
	 **/
	public AutoXdatStoredSearchAllowedUser(){}

	public AutoXdatStoredSearchAllowedUser(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:stored_search_allowed_user";
	}

	//FIELD

	private String _Login=null;

	/**
	 * @return Returns the login.
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
	 * Sets the value for login.
	 * @param v Value to Set.
	 */
	public void setLogin(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/login",v);
		_Login=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatStoredSearchAllowedUserId=null;

	/**
	 * @return Returns the xdat_stored_search_allowed_user_id.
	 */
	public Integer getXdatStoredSearchAllowedUserId() {
		try{
			if (_XdatStoredSearchAllowedUserId==null){
				_XdatStoredSearchAllowedUserId=getIntegerProperty("xdat_stored_search_allowed_user_id");
				return _XdatStoredSearchAllowedUserId;
			}else {
				return _XdatStoredSearchAllowedUserId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_stored_search_allowed_user_id.
	 * @param v Value to Set.
	 */
	public void setXdatStoredSearchAllowedUserId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_stored_search_allowed_user_id",v);
		_XdatStoredSearchAllowedUserId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> getAllXdatStoredSearchAllowedUsers(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> getXdatStoredSearchAllowedUsersByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> getXdatStoredSearchAllowedUsersByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatStoredSearchAllowedUser getXdatStoredSearchAllowedUsersByXdatStoredSearchAllowedUserId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:stored_search_allowed_user/xdat_stored_search_allowed_user_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatStoredSearchAllowedUser) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	return _return;
}
}
