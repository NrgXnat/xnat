/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:06 UTC 2016
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
public abstract class AutoXnatCtscandataFocalspot extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatCtscandataFocalspotI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatCtscandataFocalspot.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:ctScanData_focalSpot";

	public AutoXnatCtscandataFocalspot(ItemI item)
	{
		super(item);
	}

	public AutoXnatCtscandataFocalspot(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatCtscandataFocalspot(UserI user)
	 **/
	public AutoXnatCtscandataFocalspot(){}

	public AutoXnatCtscandataFocalspot(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:ctScanData_focalSpot";
	}

	//FIELD

	private Double _Focalspot=null;

	/**
	 * @return Returns the focalSpot.
	 */
	public Double getFocalspot() {
		try{
			if (_Focalspot==null){
				_Focalspot=getDoubleProperty("focalSpot");
				return _Focalspot;
			}else {
				return _Focalspot;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for focalSpot.
	 * @param v Value to Set.
	 */
	public void setFocalspot(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/focalSpot",v);
		_Focalspot=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatCtscandataFocalspotId=null;

	/**
	 * @return Returns the xnat_ctScanData_focalSpot_id.
	 */
	public Integer getXnatCtscandataFocalspotId() {
		try{
			if (_XnatCtscandataFocalspotId==null){
				_XnatCtscandataFocalspotId=getIntegerProperty("xnat_ctScanData_focalSpot_id");
				return _XnatCtscandataFocalspotId;
			}else {
				return _XnatCtscandataFocalspotId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_ctScanData_focalSpot_id.
	 * @param v Value to Set.
	 */
	public void setXnatCtscandataFocalspotId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_ctScanData_focalSpot_id",v);
		_XnatCtscandataFocalspotId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> getAllXnatCtscandataFocalspots(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> al = new ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> getXnatCtscandataFocalspotsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> al = new ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> getXnatCtscandataFocalspotsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> al = new ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatCtscandataFocalspot getXnatCtscandataFocalspotsByXnatCtscandataFocalspotId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:ctScanData_focalSpot/xnat_ctScanData_focalSpot_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatCtscandataFocalspot) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
