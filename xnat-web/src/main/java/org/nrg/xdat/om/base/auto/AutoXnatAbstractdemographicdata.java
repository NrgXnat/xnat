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
public abstract class AutoXnatAbstractdemographicdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatAbstractdemographicdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAbstractdemographicdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:abstractDemographicData";

	public AutoXnatAbstractdemographicdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatAbstractdemographicdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAbstractdemographicdata(UserI user)
	 **/
	public AutoXnatAbstractdemographicdata(){}

	public AutoXnatAbstractdemographicdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:abstractDemographicData";
	}

	//FIELD

	private Integer _XnatAbstractdemographicdataId=null;

	/**
	 * @return Returns the xnat_abstractDemographicData_id.
	 */
	public Integer getXnatAbstractdemographicdataId() {
		try{
			if (_XnatAbstractdemographicdataId==null){
				_XnatAbstractdemographicdataId=getIntegerProperty("xnat_abstractDemographicData_id");
				return _XnatAbstractdemographicdataId;
			}else {
				return _XnatAbstractdemographicdataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_abstractDemographicData_id.
	 * @param v Value to Set.
	 */
	public void setXnatAbstractdemographicdataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_abstractDemographicData_id",v);
		_XnatAbstractdemographicdataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> getAllXnatAbstractdemographicdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> getXnatAbstractdemographicdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> getXnatAbstractdemographicdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatAbstractdemographicdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAbstractdemographicdata getXnatAbstractdemographicdatasByXnatAbstractdemographicdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:abstractDemographicData/xnat_abstractDemographicData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAbstractdemographicdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        localLoop = preventLoop;
	
	return _return;
}
}
