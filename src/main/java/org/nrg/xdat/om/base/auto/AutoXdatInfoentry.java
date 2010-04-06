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
public abstract class AutoXdatInfoentry extends org.nrg.xdat.base.BaseElement implements XdatInfoentryI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatInfoentry.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:infoEntry";

	public AutoXdatInfoentry(ItemI item)
	{
		super(item);
	}

	public AutoXdatInfoentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatInfoentry(UserI user)
	 **/
	public AutoXdatInfoentry(){}

	public AutoXdatInfoentry(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:infoEntry";
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

	private Integer _XdatInfoentryId=null;

	/**
	 * @return Returns the xdat_infoEntry_id.
	 */
	public Integer getXdatInfoentryId() {
		try{
			if (_XdatInfoentryId==null){
				_XdatInfoentryId=getIntegerProperty("xdat_infoEntry_id");
				return _XdatInfoentryId;
			}else {
				return _XdatInfoentryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_infoEntry_id.
	 * @param v Value to Set.
	 */
	public void setXdatInfoentryId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_infoEntry_id",v);
		_XdatInfoentryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatInfoentry> getAllXdatInfoentrys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatInfoentry> al = new ArrayList<org.nrg.xdat.om.XdatInfoentry>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatInfoentry> getXdatInfoentrysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatInfoentry> al = new ArrayList<org.nrg.xdat.om.XdatInfoentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatInfoentry> getXdatInfoentrysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatInfoentry> al = new ArrayList<org.nrg.xdat.om.XdatInfoentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatInfoentry getXdatInfoentrysByXdatInfoentryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:infoEntry/xdat_infoEntry_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatInfoentry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XdatInfoentry getXdatInfoentrysByTitle(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:infoEntry/title",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatInfoentry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
