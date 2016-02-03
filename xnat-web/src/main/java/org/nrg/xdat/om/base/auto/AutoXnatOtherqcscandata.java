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
public abstract class AutoXnatOtherqcscandata extends XnatQcscandata implements org.nrg.xdat.model.XnatOtherqcscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatOtherqcscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:otherQcScanData";

	public AutoXnatOtherqcscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatOtherqcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatOtherqcscandata(UserI user)
	 **/
	public AutoXnatOtherqcscandata(){}

	public AutoXnatOtherqcscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:otherQcScanData";
	}
	 private org.nrg.xdat.om.XnatQcscandata _Qcscandata =null;

	/**
	 * qcScanData
	 * @return org.nrg.xdat.om.XnatQcscandata
	 */
	public org.nrg.xdat.om.XnatQcscandata getQcscandata() {
		try{
			if (_Qcscandata==null){
				_Qcscandata=((XnatQcscandata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("qcScanData")));
				return _Qcscandata;
			}else {
				return _Qcscandata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for qcScanData.
	 * @param v Value to Set.
	 */
	public void setQcscandata(ItemI v) throws Exception{
		_Qcscandata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/qcScanData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/qcScanData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * qcScanData
	 * set org.nrg.xdat.model.XnatQcscandataI
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataI> void setQcscandata(A item) throws Exception{
	setQcscandata((ItemI)item);
	}

	/**
	 * Removes the qcScanData.
	 * */
	public void removeQcscandata() {
		_Qcscandata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/qcScanData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Other=null;

	/**
	 * @return Returns the other.
	 */
	public String getOther(){
		try{
			if (_Other==null){
				_Other=getStringProperty("other");
				return _Other;
			}else {
				return _Other;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for other.
	 * @param v Value to Set.
	 */
	public void setOther(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/other",v);
		_Other=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> getAllXnatOtherqcscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatOtherqcscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> getXnatOtherqcscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatOtherqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> getXnatOtherqcscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOtherqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatOtherqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatOtherqcscandata getXnatOtherqcscandatasByXnatQcscandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:otherQcScanData/xnat_qcscandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatOtherqcscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //qcScanData
	        XnatQcscandata childQcscandata = (XnatQcscandata)this.getQcscandata();
	            if (childQcscandata!=null){
	              for(ResourceFile rf: ((XnatQcscandata)childQcscandata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("qcScanData[" + ((XnatQcscandata)childQcscandata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("qcScanData/" + ((XnatQcscandata)childQcscandata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
