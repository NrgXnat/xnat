/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatSearch;
import org.nrg.xdat.om.XdatSearchI;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.om.XdatStoredSearchI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatSearch extends XdatStoredSearch implements XdatSearchI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatSearch.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:Search";

	public AutoXdatSearch(ItemI item)
	{
		super(item);
	}

	public AutoXdatSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatSearch(UserI user)
	 **/
	public AutoXdatSearch(){}

	public AutoXdatSearch(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:Search";
	}
	 private org.nrg.xdat.om.XdatStoredSearchI _StoredSearch =null;

	/**
	 * stored_search
	 * @return org.nrg.xdat.om.XdatStoredSearchI
	 */
	public org.nrg.xdat.om.XdatStoredSearchI getStoredSearch() {
		try{
			if (_StoredSearch==null){
				_StoredSearch=((XdatStoredSearchI)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("stored_search")));
				return _StoredSearch;
			}else {
				return _StoredSearch;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for stored_search.
	 * @param v Value to Set.
	 */
	public void setStoredSearch(ItemI v) throws Exception{
		_StoredSearch =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/stored_search",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/stored_search",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the stored_search.
	 * */
	public void removeStoredSearch() {
		_StoredSearch =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/stored_search",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Page=null;

	/**
	 * @return Returns the page.
	 */
	public Integer getPage() {
		try{
			if (_Page==null){
				_Page=getIntegerProperty("page");
				return _Page;
			}else {
				return _Page;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for page.
	 * @param v Value to Set.
	 */
	public void setPage(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/page",v);
		_Page=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearch> getAllXdatSearchs(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearch> al = new ArrayList<org.nrg.xdat.om.XdatSearch>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearch> getXdatSearchsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearch> al = new ArrayList<org.nrg.xdat.om.XdatSearch>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearch> getXdatSearchsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearch> al = new ArrayList<org.nrg.xdat.om.XdatSearch>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatSearch getXdatSearchsById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:Search/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatSearch) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //stored_search
	        XdatStoredSearch childStoredSearch = (XdatStoredSearch)this.getStoredSearch();
	            for(ResourceFile rf: childStoredSearch.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("stored_search[" + childStoredSearch.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("stored_search/" + childStoredSearch.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
