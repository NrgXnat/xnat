/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatCriteriaSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.om.XdatCriteriaSetI;
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
public abstract class AutoXdatCriteriaSet extends org.nrg.xdat.base.BaseElement implements XdatCriteriaSetI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatCriteriaSet.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:criteria_set";

	public AutoXdatCriteriaSet(ItemI item)
	{
		super(item);
	}

	public AutoXdatCriteriaSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatCriteriaSet(UserI user)
	 **/
	public AutoXdatCriteriaSet(){}

	public AutoXdatCriteriaSet(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:criteria_set";
	}
	 private ArrayList<org.nrg.xdat.om.XdatCriteria> _Criteria =null;

	/**
	 * criteria
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteria
	 */
	public ArrayList<org.nrg.xdat.om.XdatCriteria> getCriteria() {
		try{
			if (_Criteria==null){
				_Criteria=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("criteria"));
				return _Criteria;
			}else {
				return _Criteria;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatCriteria>();}
	}

	/**
	 * Sets the value for criteria.
	 * @param v Value to Set.
	 */
	public void setCriteria(ItemI v) throws Exception{
		_Criteria =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/criteria",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/criteria",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the criteria of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeCriteria(int index) throws java.lang.IndexOutOfBoundsException {
		_Criteria =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/criteria",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatCriteriaSet> _ChildSet =null;

	/**
	 * child_set
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatCriteriaSet
	 */
	public ArrayList<org.nrg.xdat.om.XdatCriteriaSet> getChildSet() {
		try{
			if (_ChildSet==null){
				_ChildSet=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("child_set"));
				return _ChildSet;
			}else {
				return _ChildSet;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatCriteriaSet>();}
	}

	/**
	 * Sets the value for child_set.
	 * @param v Value to Set.
	 */
	public void setChildSet(ItemI v) throws Exception{
		_ChildSet =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/child_set",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/child_set",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the child_set of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeChildSet(int index) throws java.lang.IndexOutOfBoundsException {
		_ChildSet =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/child_set",index);
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

	private Integer _XdatCriteriaSetId=null;

	/**
	 * @return Returns the xdat_criteria_set_id.
	 */
	public Integer getXdatCriteriaSetId() {
		try{
			if (_XdatCriteriaSetId==null){
				_XdatCriteriaSetId=getIntegerProperty("xdat_criteria_set_id");
				return _XdatCriteriaSetId;
			}else {
				return _XdatCriteriaSetId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_criteria_set_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaSetId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_criteria_set_id",v);
		_XdatCriteriaSetId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteriaSet> getAllXdatCriteriaSets(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteriaSet> al = new ArrayList<org.nrg.xdat.om.XdatCriteriaSet>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteriaSet> getXdatCriteriaSetsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteriaSet> al = new ArrayList<org.nrg.xdat.om.XdatCriteriaSet>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteriaSet> getXdatCriteriaSetsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteriaSet> al = new ArrayList<org.nrg.xdat.om.XdatCriteriaSet>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatCriteriaSet getXdatCriteriaSetsByXdatCriteriaSetId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:criteria_set/xdat_criteria_set_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatCriteriaSet) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //criteria
	        for(XdatCriteria childCriteria : this.getCriteria()){
	            for(ResourceFile rf: childCriteria.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("criteria[" + childCriteria.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("criteria/" + childCriteria.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //child_set
	        for(XdatCriteriaSet childChildSet : this.getChildSet()){
	            for(ResourceFile rf: childChildSet.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("child_set[" + childChildSet.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("child_set/" + childChildSet.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
