/*
 * org.nrg.xdat.om.base.auto.AutoXdatStoredSearchGroupid
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

import org.nrg.xdat.om.XdatStoredSearchGroupid;
import org.nrg.xdat.om.XdatStoredSearchGroupidI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatStoredSearchGroupid extends org.nrg.xdat.base.BaseElement implements XdatStoredSearchGroupidI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatStoredSearchGroupid.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:stored_search_groupID";

	public AutoXdatStoredSearchGroupid(ItemI item)
	{
		super(item);
	}

	public AutoXdatStoredSearchGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatStoredSearchGroupid(UserI user)
	 **/
	public AutoXdatStoredSearchGroupid(){}

	public AutoXdatStoredSearchGroupid(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:stored_search_groupID";
	}

	//FIELD

	private String _Groupid=null;

	/**
	 * @return Returns the groupID.
	 */
	public String getGroupid(){
		try{
			if (_Groupid==null){
				_Groupid=getStringProperty("groupID");
				return _Groupid;
			}else {
				return _Groupid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for groupID.
	 * @param v Value to Set.
	 */
	public void setGroupid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/groupID",v);
		_Groupid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatStoredSearchGroupidId=null;

	/**
	 * @return Returns the xdat_stored_search_groupID_id.
	 */
	public Integer getXdatStoredSearchGroupidId() {
		try{
			if (_XdatStoredSearchGroupidId==null){
				_XdatStoredSearchGroupidId=getIntegerProperty("xdat_stored_search_groupID_id");
				return _XdatStoredSearchGroupidId;
			}else {
				return _XdatStoredSearchGroupidId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_stored_search_groupID_id.
	 * @param v Value to Set.
	 */
	public void setXdatStoredSearchGroupidId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_stored_search_groupID_id",v);
		_XdatStoredSearchGroupidId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> getAllXdatStoredSearchGroupids(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> getXdatStoredSearchGroupidsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> getXdatStoredSearchGroupidsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> al = new ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatStoredSearchGroupid getXdatStoredSearchGroupidsByXdatStoredSearchGroupidId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:stored_search_groupID/xdat_stored_search_groupID_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatStoredSearchGroupid) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
