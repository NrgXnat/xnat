// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.exception.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatUserLogin extends org.nrg.xdat.base.BaseElement implements XdatUserLoginI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatUserLogin.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:user_login";

	public AutoXdatUserLogin(ItemI item)
	{
		super(item);
	}

	public AutoXdatUserLogin(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatUserLogin(UserI user)
	 **/
	public AutoXdatUserLogin(){}

	public AutoXdatUserLogin(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:user_login";
	}

	//FIELD

	private Object _LoginDate=null;

	/**
	 * @return Returns the login_date.
	 */
	public Object getLoginDate(){
		try{
			if (_LoginDate==null){
				_LoginDate=getProperty("login_date");
				return _LoginDate;
			}else {
				return _LoginDate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for login_date.
	 * @param v Value to Set.
	 */
	public void setLoginDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/login_date",v);
		_LoginDate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _IpAddress=null;

	/**
	 * @return Returns the ip_address.
	 */
	public String getIpAddress(){
		try{
			if (_IpAddress==null){
				_IpAddress=getStringProperty("ip_address");
				return _IpAddress;
			}else {
				return _IpAddress;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ip_address.
	 * @param v Value to Set.
	 */
	public void setIpAddress(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ip_address",v);
		_IpAddress=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XdatUserI _userProperty =null;

	/**
	 * user
	 * @return org.nrg.xdat.om.XdatUserI
	 */
	public org.nrg.xdat.om.XdatUserI getuserProperty() {
		try{
			if (_userProperty==null){
				_userProperty=((XdatUserI)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("user")));
				return _userProperty;
			}else {
				return _userProperty;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
	 */
	public void setuserProperty(ItemI v) throws Exception{
		_userProperty =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/user",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/user",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the user.
	 * */
	public void removeuserProperty() {
		_userProperty =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/user",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _userPropertyFK=null;

	/**
	 * @return Returns the xdat:user_login/user_xdat_user_id.
	 */
	public Integer getuserPropertyFK(){
		try{
			if (_userPropertyFK==null){
				_userPropertyFK=getIntegerProperty("xdat:user_login/user_xdat_user_id");
				return _userPropertyFK;
			}else {
				return _userPropertyFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat:user_login/user_xdat_user_id.
	 * @param v Value to Set.
	 */
	public void setuserPropertyFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/user_xdat_user_id",v);
		_userPropertyFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatUserLoginId=null;

	/**
	 * @return Returns the xdat_user_login_id.
	 */
	public Integer getXdatUserLoginId() {
		try{
			if (_XdatUserLoginId==null){
				_XdatUserLoginId=getIntegerProperty("xdat_user_login_id");
				return _XdatUserLoginId;
			}else {
				return _XdatUserLoginId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_user_login_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserLoginId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_user_login_id",v);
		_XdatUserLoginId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserLogin> getAllXdatUserLogins(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserLogin> al = new ArrayList<org.nrg.xdat.om.XdatUserLogin>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserLogin> getXdatUserLoginsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserLogin> al = new ArrayList<org.nrg.xdat.om.XdatUserLogin>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserLogin> getXdatUserLoginsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserLogin> al = new ArrayList<org.nrg.xdat.om.XdatUserLogin>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatUserLogin getXdatUserLoginsByXdatUserLoginId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:user_login/xdat_user_login_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatUserLogin) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //user
	        XdatUser childuserProperty = (XdatUser)this.getuserProperty();
	            for(ResourceFile rf: childuserProperty.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("user[" + childuserProperty.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("user/" + childuserProperty.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
