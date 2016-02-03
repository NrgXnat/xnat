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
public abstract class AutoWrkAbstractexecutionenvironment extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.WrkAbstractexecutionenvironmentI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoWrkAbstractexecutionenvironment.class);
	public static String SCHEMA_ELEMENT_NAME="wrk:abstractExecutionEnvironment";

	public AutoWrkAbstractexecutionenvironment(ItemI item)
	{
		super(item);
	}

	public AutoWrkAbstractexecutionenvironment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoWrkAbstractexecutionenvironment(UserI user)
	 **/
	public AutoWrkAbstractexecutionenvironment(){}

	public AutoWrkAbstractexecutionenvironment(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "wrk:abstractExecutionEnvironment";
	}

	//FIELD

	private Integer _WrkAbstractexecutionenvironmentId=null;

	/**
	 * @return Returns the wrk_abstractExecutionEnvironment_id.
	 */
	public Integer getWrkAbstractexecutionenvironmentId() {
		try{
			if (_WrkAbstractexecutionenvironmentId==null){
				_WrkAbstractexecutionenvironmentId=getIntegerProperty("wrk_abstractExecutionEnvironment_id");
				return _WrkAbstractexecutionenvironmentId;
			}else {
				return _WrkAbstractexecutionenvironmentId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrk_abstractExecutionEnvironment_id.
	 * @param v Value to Set.
	 */
	public void setWrkAbstractexecutionenvironmentId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/wrk_abstractExecutionEnvironment_id",v);
		_WrkAbstractexecutionenvironmentId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> getAllWrkAbstractexecutionenvironments(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> getWrkAbstractexecutionenvironmentsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> getWrkAbstractexecutionenvironmentsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkAbstractexecutionenvironment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static WrkAbstractexecutionenvironment getWrkAbstractexecutionenvironmentsByWrkAbstractexecutionenvironmentId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("wrk:abstractExecutionEnvironment/wrk_abstractExecutionEnvironment_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (WrkAbstractexecutionenvironment) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        localLoop = preventLoop;
	
	return _return;
}
}
