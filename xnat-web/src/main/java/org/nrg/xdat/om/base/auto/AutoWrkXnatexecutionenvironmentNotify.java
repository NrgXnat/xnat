/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:04 UTC 2016
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
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoWrkXnatexecutionenvironmentNotify extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.WrkXnatexecutionenvironmentNotifyI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoWrkXnatexecutionenvironmentNotify.class);
	public static String SCHEMA_ELEMENT_NAME="wrk:xnatExecutionEnvironment_notify";

	public AutoWrkXnatexecutionenvironmentNotify(ItemI item)
	{
		super(item);
	}

	public AutoWrkXnatexecutionenvironmentNotify(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoWrkXnatexecutionenvironmentNotify(UserI user)
	 **/
	public AutoWrkXnatexecutionenvironmentNotify(){}

	public AutoWrkXnatexecutionenvironmentNotify(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "wrk:xnatExecutionEnvironment_notify";
	}

	//FIELD

	private String _Notify=null;

	/**
	 * @return Returns the notify.
	 */
	public String getNotify(){
		try{
			if (_Notify==null){
				_Notify=getStringProperty("notify");
				return _Notify;
			}else {
				return _Notify;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for notify.
	 * @param v Value to Set.
	 */
	public void setNotify(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/notify",v);
		_Notify=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _WrkXnatexecutionenvironmentNotifyId=null;

	/**
	 * @return Returns the wrk_xnatExecutionEnvironment_notify_id.
	 */
	public Integer getWrkXnatexecutionenvironmentNotifyId() {
		try{
			if (_WrkXnatexecutionenvironmentNotifyId==null){
				_WrkXnatexecutionenvironmentNotifyId=getIntegerProperty("wrk_xnatExecutionEnvironment_notify_id");
				return _WrkXnatexecutionenvironmentNotifyId;
			}else {
				return _WrkXnatexecutionenvironmentNotifyId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrk_xnatExecutionEnvironment_notify_id.
	 * @param v Value to Set.
	 */
	public void setWrkXnatexecutionenvironmentNotifyId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/wrk_xnatExecutionEnvironment_notify_id",v);
		_WrkXnatexecutionenvironmentNotifyId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> getAllWrkXnatexecutionenvironmentNotifys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> getWrkXnatexecutionenvironmentNotifysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> getWrkXnatexecutionenvironmentNotifysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static WrkXnatexecutionenvironmentNotify getWrkXnatexecutionenvironmentNotifysByWrkXnatexecutionenvironmentNotifyId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("wrk:xnatExecutionEnvironment_notify/wrk_xnatExecutionEnvironment_notify_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (WrkXnatexecutionenvironmentNotify) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
