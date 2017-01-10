/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatFieldMappingSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingSet;
import org.nrg.xdat.om.XdatFieldMappingSetI;
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
public abstract class AutoXdatFieldMappingSet extends org.nrg.xdat.base.BaseElement implements XdatFieldMappingSetI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatFieldMappingSet.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:field_mapping_set";

	public AutoXdatFieldMappingSet(ItemI item)
	{
		super(item);
	}

	public AutoXdatFieldMappingSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatFieldMappingSet(UserI user)
	 **/
	public AutoXdatFieldMappingSet(){}

	public AutoXdatFieldMappingSet(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:field_mapping_set";
	}
	 private ArrayList<org.nrg.xdat.om.XdatFieldMapping> _Allow =null;

	/**
	 * allow
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMapping
	 */
	public ArrayList<org.nrg.xdat.om.XdatFieldMapping> getAllow() {
		try{
			if (_Allow==null){
				_Allow=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("allow"));
				return _Allow;
			}else {
				return _Allow;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatFieldMapping>();}
	}

	/**
	 * Sets the value for allow.
	 * @param v Value to Set.
	 */
	public void setAllow(ItemI v) throws Exception{
		_Allow =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/allow",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/allow",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the allow of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAllow(int index) throws java.lang.IndexOutOfBoundsException {
		_Allow =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/allow",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> _SubSet =null;

	/**
	 * sub_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatFieldMappingSet
	 */
	public ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> getSubSet() {
		try{
			if (_SubSet==null){
				_SubSet=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sub_set"));
				return _SubSet;
			}else {
				return _SubSet;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatFieldMappingSet>();}
	}

	/**
	 * Sets the value for sub_set.
	 * @param v Value to Set.
	 */
	public void setSubSet(ItemI v) throws Exception{
		_SubSet =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sub_set",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sub_set",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the sub_set of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSubSet(int index) throws java.lang.IndexOutOfBoundsException {
		_SubSet =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sub_set",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
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

	private Integer _XdatFieldMappingSetId=null;

	/**
	 * @return Returns the xdat_field_mapping_set_id.
	 */
	public Integer getXdatFieldMappingSetId() {
		try{
			if (_XdatFieldMappingSetId==null){
				_XdatFieldMappingSetId=getIntegerProperty("xdat_field_mapping_set_id");
				return _XdatFieldMappingSetId;
			}else {
				return _XdatFieldMappingSetId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_field_mapping_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingSetId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_field_mapping_set_id",v);
		_XdatFieldMappingSetId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> getAllXdatFieldMappingSets(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> al = new ArrayList<org.nrg.xdat.om.XdatFieldMappingSet>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> getXdatFieldMappingSetsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> al = new ArrayList<org.nrg.xdat.om.XdatFieldMappingSet>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> getXdatFieldMappingSetsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMappingSet> al = new ArrayList<org.nrg.xdat.om.XdatFieldMappingSet>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatFieldMappingSet getXdatFieldMappingSetsByXdatFieldMappingSetId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:field_mapping_set/xdat_field_mapping_set_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatFieldMappingSet) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //allow
	        for(XdatFieldMapping childAllow : this.getAllow()){
	            for(ResourceFile rf: childAllow.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("allow[" + childAllow.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("allow/" + childAllow.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //sub_set
	        for(XdatFieldMappingSet childSubSet : this.getSubSet()){
	            for(ResourceFile rf: childSubSet.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sub_set[" + childSubSet.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sub_set/" + childSubSet.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
