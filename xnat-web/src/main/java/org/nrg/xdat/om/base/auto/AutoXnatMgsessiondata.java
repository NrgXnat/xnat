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
public abstract class AutoXnatMgsessiondata extends XnatImagesessiondata implements org.nrg.xdat.model.XnatMgsessiondataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatMgsessiondata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:mgSessionData";

	public AutoXnatMgsessiondata(ItemI item)
	{
		super(item);
	}

	public AutoXnatMgsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatMgsessiondata(UserI user)
	 **/
	public AutoXnatMgsessiondata(){}

	public AutoXnatMgsessiondata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:mgSessionData";
	}
	 private org.nrg.xdat.om.XnatImagesessiondata _Imagesessiondata =null;

	/**
	 * imageSessionData
	 * @return org.nrg.xdat.om.XnatImagesessiondata
	 */
	public org.nrg.xdat.om.XnatImagesessiondata getImagesessiondata() {
		try{
			if (_Imagesessiondata==null){
				_Imagesessiondata=((XnatImagesessiondata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("imageSessionData")));
				return _Imagesessiondata;
			}else {
				return _Imagesessiondata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for imageSessionData.
	 * @param v Value to Set.
	 */
	public void setImagesessiondata(ItemI v) throws Exception{
		_Imagesessiondata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageSessionData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageSessionData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * imageSessionData
	 * set org.nrg.xdat.model.XnatImagesessiondataI
	 */
	public <A extends org.nrg.xdat.model.XnatImagesessiondataI> void setImagesessiondata(A item) throws Exception{
	setImagesessiondata((ItemI)item);
	}

	/**
	 * Removes the imageSessionData.
	 * */
	public void removeImagesessiondata() {
		_Imagesessiondata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/imageSessionData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatMgsessiondata> getAllXnatMgsessiondatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMgsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMgsessiondata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMgsessiondata> getXnatMgsessiondatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMgsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMgsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMgsessiondata> getXnatMgsessiondatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMgsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMgsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatMgsessiondata getXnatMgsessiondatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:mgSessionData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatMgsessiondata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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

	public org.w3c.dom.Document toJoinedXML() throws Exception
	{
		ArrayList al = new ArrayList();
		al.add(this.getItem());
		al.add(org.nrg.xft.search.ItemSearch.GetItem("xnat:subjectData.ID",this.getItem().getProperty("xnat:mrSessionData.subject_ID"),getItem().getUser(),false));
		al.trimToSize();
		return org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);
	}
	public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //imageSessionData
	        XnatImagesessiondata childImagesessiondata = (XnatImagesessiondata)this.getImagesessiondata();
	            if (childImagesessiondata!=null){
	              for(ResourceFile rf: ((XnatImagesessiondata)childImagesessiondata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("imageSessionData[" + ((XnatImagesessiondata)childImagesessiondata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("imageSessionData/" + ((XnatImagesessiondata)childImagesessiondata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
