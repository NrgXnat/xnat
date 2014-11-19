/*
 * org.nrg.xdat.om.base.auto.AutoXdatNewsentry
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

import org.nrg.xdat.om.XdatNewsentry;
import org.nrg.xdat.om.XdatNewsentryI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatNewsentry extends org.nrg.xdat.base.BaseElement implements XdatNewsentryI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatNewsentry.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:newsEntry";

	public AutoXdatNewsentry(ItemI item)
	{
		super(item);
	}

	public AutoXdatNewsentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatNewsentry(UserI user)
	 **/
	public AutoXdatNewsentry(){}

	public AutoXdatNewsentry(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:newsEntry";
	}

	//FIELD

	private Object _Date=null;

	/**
	 * @return Returns the date.
	 */
	public Object getDate(){
		try{
			if (_Date==null){
				_Date=getProperty("date");
				return _Date;
			}else {
				return _Date;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/date",v);
		_Date=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Title=null;

	/**
	 * @return Returns the title.
	 */
	public String getTitle(){
		try{
			if (_Title==null){
				_Title=getStringProperty("title");
				return _Title;
			}else {
				return _Title;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for title.
	 * @param v Value to Set.
	 */
	public void setTitle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/title",v);
		_Title=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Link=null;

	/**
	 * @return Returns the link.
	 */
	public String getLink(){
		try{
			if (_Link==null){
				_Link=getStringProperty("link");
				return _Link;
			}else {
				return _Link;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for link.
	 * @param v Value to Set.
	 */
	public void setLink(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/link",v);
		_Link=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatNewsentryId=null;

	/**
	 * @return Returns the xdat_newsEntry_id.
	 */
	public Integer getXdatNewsentryId() {
		try{
			if (_XdatNewsentryId==null){
				_XdatNewsentryId=getIntegerProperty("xdat_newsEntry_id");
				return _XdatNewsentryId;
			}else {
				return _XdatNewsentryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_newsEntry_id.
	 * @param v Value to Set.
	 */
	public void setXdatNewsentryId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_newsEntry_id",v);
		_XdatNewsentryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatNewsentry> getAllXdatNewsentrys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatNewsentry> al = new ArrayList<org.nrg.xdat.om.XdatNewsentry>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatNewsentry> getXdatNewsentrysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatNewsentry> al = new ArrayList<org.nrg.xdat.om.XdatNewsentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatNewsentry> getXdatNewsentrysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatNewsentry> al = new ArrayList<org.nrg.xdat.om.XdatNewsentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatNewsentry getXdatNewsentrysByXdatNewsentryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:newsEntry/xdat_newsEntry_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatNewsentry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XdatNewsentry getXdatNewsentrysByTitle(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:newsEntry/title",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatNewsentry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
