// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatUserGroupid;
import org.nrg.xdat.om.XdatUserGroupidI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatUserGroupid extends org.nrg.xdat.base.BaseElement implements XdatUserGroupidI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatUserGroupid.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:user_groupID";

	public AutoXdatUserGroupid(ItemI item)
	{
		super(item);
	}

	public AutoXdatUserGroupid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatUserGroupid(UserI user)
	 **/
	public AutoXdatUserGroupid(){}

	public AutoXdatUserGroupid(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:user_groupID";
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

	private Integer _XdatUserGroupidId=null;

	/**
	 * @return Returns the xdat_user_groupID_id.
	 */
	public Integer getXdatUserGroupidId() {
		try{
			if (_XdatUserGroupidId==null){
				_XdatUserGroupidId=getIntegerProperty("xdat_user_groupID_id");
				return _XdatUserGroupidId;
			}else {
				return _XdatUserGroupidId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_user_groupID_id.
	 * @param v Value to Set.
	 */
	public void setXdatUserGroupidId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_user_groupID_id",v);
		_XdatUserGroupidId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserGroupid> getAllXdatUserGroupids(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserGroupid> al = new ArrayList<org.nrg.xdat.om.XdatUserGroupid>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserGroupid> getXdatUserGroupidsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserGroupid> al = new ArrayList<org.nrg.xdat.om.XdatUserGroupid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatUserGroupid> getXdatUserGroupidsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatUserGroupid> al = new ArrayList<org.nrg.xdat.om.XdatUserGroupid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatUserGroupid getXdatUserGroupidsByXdatUserGroupidId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:user_groupID/xdat_user_groupID_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatUserGroupid) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
