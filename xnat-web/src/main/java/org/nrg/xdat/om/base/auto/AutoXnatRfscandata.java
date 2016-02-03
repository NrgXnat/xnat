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
public abstract class AutoXnatRfscandata extends XnatImagescandata implements org.nrg.xdat.model.XnatRfscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatRfscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:rfScanData";

	public AutoXnatRfscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatRfscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatRfscandata(UserI user)
	 **/
	public AutoXnatRfscandata(){}

	public AutoXnatRfscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:rfScanData";
	}
	 private org.nrg.xdat.om.XnatImagescandata _Imagescandata =null;

	/**
	 * imageScanData
	 * @return org.nrg.xdat.om.XnatImagescandata
	 */
	public org.nrg.xdat.om.XnatImagescandata getImagescandata() {
		try{
			if (_Imagescandata==null){
				_Imagescandata=((XnatImagescandata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("imageScanData")));
				return _Imagescandata;
			}else {
				return _Imagescandata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for imageScanData.
	 * @param v Value to Set.
	 */
	public void setImagescandata(ItemI v) throws Exception{
		_Imagescandata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageScanData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageScanData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * imageScanData
	 * set org.nrg.xdat.model.XnatImagescandataI
	 */
	public <A extends org.nrg.xdat.model.XnatImagescandataI> void setImagescandata(A item) throws Exception{
	setImagescandata((ItemI)item);
	}

	/**
	 * Removes the imageScanData.
	 * */
	public void removeImagescandata() {
		_Imagescandata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/imageScanData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatRfscandata> getAllXnatRfscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRfscandata> al = new ArrayList<org.nrg.xdat.om.XnatRfscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRfscandata> getXnatRfscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRfscandata> al = new ArrayList<org.nrg.xdat.om.XnatRfscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRfscandata> getXnatRfscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRfscandata> al = new ArrayList<org.nrg.xdat.om.XnatRfscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatRfscandata getXnatRfscandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:rfScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatRfscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //imageScanData
	        XnatImagescandata childImagescandata = (XnatImagescandata)this.getImagescandata();
	            if (childImagescandata!=null){
	              for(ResourceFile rf: ((XnatImagescandata)childImagescandata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("imageScanData[" + ((XnatImagescandata)childImagescandata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("imageScanData/" + ((XnatImagescandata)childImagescandata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
