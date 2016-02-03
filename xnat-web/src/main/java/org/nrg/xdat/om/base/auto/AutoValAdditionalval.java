/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:05 UTC 2016
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
public abstract class AutoValAdditionalval extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ValAdditionalvalI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoValAdditionalval.class);
	public static String SCHEMA_ELEMENT_NAME="val:additionalVal";

	public AutoValAdditionalval(ItemI item)
	{
		super(item);
	}

	public AutoValAdditionalval(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoValAdditionalval(UserI user)
	 **/
	public AutoValAdditionalval(){}

	public AutoValAdditionalval(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "val:additionalVal";
	}

	//FIELD

	private Integer _ValAdditionalvalId=null;

	/**
	 * @return Returns the val_additionalVal_id.
	 */
	public Integer getValAdditionalvalId() {
		try{
			if (_ValAdditionalvalId==null){
				_ValAdditionalvalId=getIntegerProperty("val_additionalVal_id");
				return _ValAdditionalvalId;
			}else {
				return _ValAdditionalvalId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for val_additionalVal_id.
	 * @param v Value to Set.
	 */
	public void setValAdditionalvalId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/val_additionalVal_id",v);
		_ValAdditionalvalId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ValAdditionalval> getAllValAdditionalvals(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValAdditionalval> al = new ArrayList<org.nrg.xdat.om.ValAdditionalval>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValAdditionalval> getValAdditionalvalsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValAdditionalval> al = new ArrayList<org.nrg.xdat.om.ValAdditionalval>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValAdditionalval> getValAdditionalvalsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValAdditionalval> al = new ArrayList<org.nrg.xdat.om.ValAdditionalval>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ValAdditionalval getValAdditionalvalsByValAdditionalvalId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("val:additionalVal/val_additionalVal_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ValAdditionalval) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
