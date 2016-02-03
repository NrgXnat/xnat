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
public abstract class AutoCatDcmentry extends CatEntry implements org.nrg.xdat.model.CatDcmentryI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatDcmentry.class);
	public static String SCHEMA_ELEMENT_NAME="cat:dcmEntry";

	public AutoCatDcmentry(ItemI item)
	{
		super(item);
	}

	public AutoCatDcmentry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatDcmentry(UserI user)
	 **/
	public AutoCatDcmentry(){}

	public AutoCatDcmentry(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:dcmEntry";
	}
	 private org.nrg.xdat.om.CatEntry _Entry =null;

	/**
	 * entry
	 * @return org.nrg.xdat.om.CatEntry
	 */
	public org.nrg.xdat.om.CatEntry getEntry() {
		try{
			if (_Entry==null){
				_Entry=((CatEntry)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("entry")));
				return _Entry;
			}else {
				return _Entry;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for entry.
	 * @param v Value to Set.
	 */
	public void setEntry(ItemI v) throws Exception{
		_Entry =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/entry",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/entry",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * entry
	 * set org.nrg.xdat.model.CatEntryI
	 */
	public <A extends org.nrg.xdat.model.CatEntryI> void setEntry(A item) throws Exception{
	setEntry((ItemI)item);
	}

	/**
	 * Removes the entry.
	 * */
	public void removeEntry() {
		_Entry =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/entry",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Uid=null;

	/**
	 * @return Returns the UID.
	 */
	public String getUid(){
		try{
			if (_Uid==null){
				_Uid=getStringProperty("UID");
				return _Uid;
			}else {
				return _Uid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for UID.
	 * @param v Value to Set.
	 */
	public void setUid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/UID",v);
		_Uid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Instancenumber=null;

	/**
	 * @return Returns the instanceNumber.
	 */
	public Integer getInstancenumber() {
		try{
			if (_Instancenumber==null){
				_Instancenumber=getIntegerProperty("instanceNumber");
				return _Instancenumber;
			}else {
				return _Instancenumber;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for instanceNumber.
	 * @param v Value to Set.
	 */
	public void setInstancenumber(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/instanceNumber",v);
		_Instancenumber=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatDcmentry> getAllCatDcmentrys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatDcmentry> al = new ArrayList<org.nrg.xdat.om.CatDcmentry>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatDcmentry> getCatDcmentrysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatDcmentry> al = new ArrayList<org.nrg.xdat.om.CatDcmentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatDcmentry> getCatDcmentrysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatDcmentry> al = new ArrayList<org.nrg.xdat.om.CatDcmentry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatDcmentry getCatDcmentrysByCatEntryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:dcmEntry/cat_entry_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatDcmentry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //entry
	        CatEntry childEntry = (CatEntry)this.getEntry();
	            if (childEntry!=null){
	              for(ResourceFile rf: ((CatEntry)childEntry).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("entry[" + ((CatEntry)childEntry).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("entry/" + ((CatEntry)childEntry).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
