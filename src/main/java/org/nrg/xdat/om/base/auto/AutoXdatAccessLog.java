/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatAccessLog
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatAccessLog;
import org.nrg.xdat.om.XdatAccessLogI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatAccessLog extends org.nrg.xdat.base.BaseElement implements XdatAccessLogI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatAccessLog.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:access_log";

	public AutoXdatAccessLog(ItemI item)
	{
		super(item);
	}

	public AutoXdatAccessLog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatAccessLog(UserI user)
	 **/
	public AutoXdatAccessLog(){}

	public AutoXdatAccessLog(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:access_log";
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

	private Object _AccessDate=null;

	/**
	 * @return Returns the access_date.
	 */
	public Object getAccessDate(){
		try{
			if (_AccessDate==null){
				_AccessDate=getProperty("access_date");
				return _AccessDate;
			}else {
				return _AccessDate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for access_date.
	 * @param v Value to Set.
	 */
	public void setAccessDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/access_date",v);
		_AccessDate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Ip=null;

	/**
	 * @return Returns the ip.
	 */
	public String getIp(){
		try{
			if (_Ip==null){
				_Ip=getStringProperty("ip");
				return _Ip;
			}else {
				return _Ip;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ip.
	 * @param v Value to Set.
	 */
	public void setIp(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ip",v);
		_Ip=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Method=null;

	/**
	 * @return Returns the method.
	 */
	public String getMethod(){
		try{
			if (_Method==null){
				_Method=getStringProperty("method");
				return _Method;
			}else {
				return _Method;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for method.
	 * @param v Value to Set.
	 */
	public void setMethod(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/method",v);
		_Method=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatAccessLogId=null;

	/**
	 * @return Returns the xdat_access_log_id.
	 */
	public Integer getXdatAccessLogId() {
		try{
			if (_XdatAccessLogId==null){
				_XdatAccessLogId=getIntegerProperty("xdat_access_log_id");
				return _XdatAccessLogId;
			}else {
				return _XdatAccessLogId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_access_log_id.
	 * @param v Value to Set.
	 */
	public void setXdatAccessLogId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_access_log_id",v);
		_XdatAccessLogId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatAccessLog> getAllXdatAccessLogs(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatAccessLog> al = new ArrayList<org.nrg.xdat.om.XdatAccessLog>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatAccessLog> getXdatAccessLogsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatAccessLog> al = new ArrayList<org.nrg.xdat.om.XdatAccessLog>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatAccessLog> getXdatAccessLogsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatAccessLog> al = new ArrayList<org.nrg.xdat.om.XdatAccessLog>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatAccessLog getXdatAccessLogsByXdatAccessLogId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:access_log/xdat_access_log_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatAccessLog) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
