/*
 * org.nrg.xdat.om.base.auto.AutoXdatActionType
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

import org.nrg.xdat.om.XdatActionType;
import org.nrg.xdat.om.XdatActionTypeI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatActionType extends org.nrg.xdat.base.BaseElement implements XdatActionTypeI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatActionType.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:action_type";

	public AutoXdatActionType(ItemI item)
	{
		super(item);
	}

	public AutoXdatActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatActionType(UserI user)
	 **/
	public AutoXdatActionType(){}

	public AutoXdatActionType(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:action_type";
	}

	//FIELD

	private String _ActionName=null;

	/**
	 * @return Returns the action_name.
	 */
	public String getActionName(){
		try{
			if (_ActionName==null){
				_ActionName=getStringProperty("action_name");
				return _ActionName;
			}else {
				return _ActionName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for action_name.
	 * @param v Value to Set.
	 */
	public void setActionName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/action_name",v);
		_ActionName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _DisplayName=null;

	/**
	 * @return Returns the display_name.
	 */
	public String getDisplayName(){
		try{
			if (_DisplayName==null){
				_DisplayName=getStringProperty("display_name");
				return _DisplayName;
			}else {
				return _DisplayName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for display_name.
	 * @param v Value to Set.
	 */
	public void setDisplayName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/display_name",v);
		_DisplayName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence() {
		try{
			if (_Sequence==null){
				_Sequence=getIntegerProperty("sequence");
				return _Sequence;
			}else {
				return _Sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sequence",v);
		_Sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatActionType> getAllXdatActionTypes(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatActionType> al = new ArrayList<org.nrg.xdat.om.XdatActionType>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatActionType> getXdatActionTypesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatActionType> al = new ArrayList<org.nrg.xdat.om.XdatActionType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatActionType> getXdatActionTypesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatActionType> al = new ArrayList<org.nrg.xdat.om.XdatActionType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatActionType getXdatActionTypesByActionName(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:action_type/action_name",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatActionType) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
