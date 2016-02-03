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
public abstract class AutoXnatMrsessiondata extends XnatImagesessiondata implements org.nrg.xdat.model.XnatMrsessiondataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatMrsessiondata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:mrSessionData";

	public AutoXnatMrsessiondata(ItemI item)
	{
		super(item);
	}

	public AutoXnatMrsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatMrsessiondata(UserI user)
	 **/
	public AutoXnatMrsessiondata(){}

	public AutoXnatMrsessiondata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:mrSessionData";
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

	//FIELD

	private String _Coil=null;

	/**
	 * @return Returns the coil.
	 */
	public String getCoil(){
		try{
			if (_Coil==null){
				_Coil=getStringProperty("coil");
				return _Coil;
			}else {
				return _Coil;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for coil.
	 * @param v Value to Set.
	 */
	public void setCoil(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/coil",v);
		_Coil=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Fieldstrength=null;

	/**
	 * @return Returns the fieldStrength.
	 */
	public String getFieldstrength(){
		try{
			if (_Fieldstrength==null){
				_Fieldstrength=getStringProperty("fieldStrength");
				return _Fieldstrength;
			}else {
				return _Fieldstrength;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for fieldStrength.
	 * @param v Value to Set.
	 */
	public void setFieldstrength(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/fieldStrength",v);
		_Fieldstrength=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Marker=null;

	/**
	 * @return Returns the marker.
	 */
	public String getMarker(){
		try{
			if (_Marker==null){
				_Marker=getStringProperty("marker");
				return _Marker;
			}else {
				return _Marker;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for marker.
	 * @param v Value to Set.
	 */
	public void setMarker(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/marker",v);
		_Marker=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Stabilization=null;

	/**
	 * @return Returns the stabilization.
	 */
	public String getStabilization(){
		try{
			if (_Stabilization==null){
				_Stabilization=getStringProperty("stabilization");
				return _Stabilization;
			}else {
				return _Stabilization;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for stabilization.
	 * @param v Value to Set.
	 */
	public void setStabilization(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/stabilization",v);
		_Stabilization=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrsessiondata> getAllXnatMrsessiondatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMrsessiondata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrsessiondata> getXnatMrsessiondatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMrsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrsessiondata> getXnatMrsessiondatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatMrsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatMrsessiondata getXnatMrsessiondatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:mrSessionData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatMrsessiondata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
