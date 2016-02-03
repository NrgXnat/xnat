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
public abstract class AutoXnatReconstructedimagedataScanid extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatReconstructedimagedataScanidI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatReconstructedimagedataScanid.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:reconstructedImageData_scanID";

	public AutoXnatReconstructedimagedataScanid(ItemI item)
	{
		super(item);
	}

	public AutoXnatReconstructedimagedataScanid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatReconstructedimagedataScanid(UserI user)
	 **/
	public AutoXnatReconstructedimagedataScanid(){}

	public AutoXnatReconstructedimagedataScanid(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:reconstructedImageData_scanID";
	}

	//FIELD

	private String _Scanid=null;

	/**
	 * @return Returns the scanID.
	 */
	public String getScanid(){
		try{
			if (_Scanid==null){
				_Scanid=getStringProperty("scanID");
				return _Scanid;
			}else {
				return _Scanid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanID.
	 * @param v Value to Set.
	 */
	public void setScanid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanID",v);
		_Scanid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatReconstructedimagedataScanidId=null;

	/**
	 * @return Returns the xnat_reconstructedImageData_scanID_id.
	 */
	public Integer getXnatReconstructedimagedataScanidId() {
		try{
			if (_XnatReconstructedimagedataScanidId==null){
				_XnatReconstructedimagedataScanidId=getIntegerProperty("xnat_reconstructedImageData_scanID_id");
				return _XnatReconstructedimagedataScanidId;
			}else {
				return _XnatReconstructedimagedataScanidId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_reconstructedImageData_scanID_id.
	 * @param v Value to Set.
	 */
	public void setXnatReconstructedimagedataScanidId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_reconstructedImageData_scanID_id",v);
		_XnatReconstructedimagedataScanidId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> getAllXnatReconstructedimagedataScanids(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> getXnatReconstructedimagedataScanidsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> getXnatReconstructedimagedataScanidsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatReconstructedimagedataScanid getXnatReconstructedimagedataScanidsByXnatReconstructedimagedataScanidId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:reconstructedImageData_scanID/xnat_reconstructedImageData_scanID_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatReconstructedimagedataScanid) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
