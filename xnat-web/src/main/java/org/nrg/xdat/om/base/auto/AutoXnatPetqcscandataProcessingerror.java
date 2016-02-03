/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:09 UTC 2016
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
public abstract class AutoXnatPetqcscandataProcessingerror extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatPetqcscandataProcessingerrorI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPetqcscandataProcessingerror.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:petQcScanData_processingError";

	public AutoXnatPetqcscandataProcessingerror(ItemI item)
	{
		super(item);
	}

	public AutoXnatPetqcscandataProcessingerror(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPetqcscandataProcessingerror(UserI user)
	 **/
	public AutoXnatPetqcscandataProcessingerror(){}

	public AutoXnatPetqcscandataProcessingerror(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:petQcScanData_processingError";
	}

	//FIELD

	private String _Processingerror=null;

	/**
	 * @return Returns the processingError.
	 */
	public String getProcessingerror(){
		try{
			if (_Processingerror==null){
				_Processingerror=getStringProperty("processingError");
				return _Processingerror;
			}else {
				return _Processingerror;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for processingError.
	 * @param v Value to Set.
	 */
	public void setProcessingerror(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/processingError",v);
		_Processingerror=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatPetqcscandataProcessingerrorId=null;

	/**
	 * @return Returns the xnat_petQcScanData_processingError_id.
	 */
	public Integer getXnatPetqcscandataProcessingerrorId() {
		try{
			if (_XnatPetqcscandataProcessingerrorId==null){
				_XnatPetqcscandataProcessingerrorId=getIntegerProperty("xnat_petQcScanData_processingError_id");
				return _XnatPetqcscandataProcessingerrorId;
			}else {
				return _XnatPetqcscandataProcessingerrorId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_petQcScanData_processingError_id.
	 * @param v Value to Set.
	 */
	public void setXnatPetqcscandataProcessingerrorId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_petQcScanData_processingError_id",v);
		_XnatPetqcscandataProcessingerrorId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> getAllXnatPetqcscandataProcessingerrors(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> getXnatPetqcscandataProcessingerrorsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> getXnatPetqcscandataProcessingerrorsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPetqcscandataProcessingerror getXnatPetqcscandataProcessingerrorsByXnatPetqcscandataProcessingerrorId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:petQcScanData_processingError/xnat_petQcScanData_processingError_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPetqcscandataProcessingerror) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
