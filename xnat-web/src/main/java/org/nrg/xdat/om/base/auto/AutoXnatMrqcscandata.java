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
public abstract class AutoXnatMrqcscandata extends XnatQcscandata implements org.nrg.xdat.model.XnatMrqcscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatMrqcscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:mrQcScanData";

	public AutoXnatMrqcscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatMrqcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatMrqcscandata(UserI user)
	 **/
	public AutoXnatMrqcscandata(){}

	public AutoXnatMrqcscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:mrQcScanData";
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

	private String _Blurring=null;

	/**
	 * @return Returns the blurring.
	 */
	public String getBlurring(){
		try{
			if (_Blurring==null){
				_Blurring=getStringProperty("blurring");
				return _Blurring;
			}else {
				return _Blurring;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for blurring.
	 * @param v Value to Set.
	 */
	public void setBlurring(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/blurring",v);
		_Blurring=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Flow=null;

	/**
	 * @return Returns the flow.
	 */
	public String getFlow(){
		try{
			if (_Flow==null){
				_Flow=getStringProperty("flow");
				return _Flow;
			}else {
				return _Flow;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for flow.
	 * @param v Value to Set.
	 */
	public void setFlow(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/flow",v);
		_Flow=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Imagecontrast=null;

	/**
	 * @return Returns the imageContrast.
	 */
	public String getImagecontrast(){
		try{
			if (_Imagecontrast==null){
				_Imagecontrast=getStringProperty("imageContrast");
				return _Imagecontrast;
			}else {
				return _Imagecontrast;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for imageContrast.
	 * @param v Value to Set.
	 */
	public void setImagecontrast(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/imageContrast",v);
		_Imagecontrast=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Inhomogeneity=null;

	/**
	 * @return Returns the inhomogeneity.
	 */
	public String getInhomogeneity(){
		try{
			if (_Inhomogeneity==null){
				_Inhomogeneity=getStringProperty("inhomogeneity");
				return _Inhomogeneity;
			}else {
				return _Inhomogeneity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for inhomogeneity.
	 * @param v Value to Set.
	 */
	public void setInhomogeneity(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/inhomogeneity",v);
		_Inhomogeneity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Wrap=null;

	/**
	 * @return Returns the wrap.
	 */
	public String getWrap(){
		try{
			if (_Wrap==null){
				_Wrap=getStringProperty("wrap");
				return _Wrap;
			}else {
				return _Wrap;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrap.
	 * @param v Value to Set.
	 */
	public void setWrap(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/wrap",v);
		_Wrap=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Susceptibility=null;

	/**
	 * @return Returns the susceptibility.
	 */
	public String getSusceptibility(){
		try{
			if (_Susceptibility==null){
				_Susceptibility=getStringProperty("susceptibility");
				return _Susceptibility;
			}else {
				return _Susceptibility;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for susceptibility.
	 * @param v Value to Set.
	 */
	public void setSusceptibility(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/susceptibility",v);
		_Susceptibility=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Interpacmotion=null;

	/**
	 * @return Returns the interpacMotion.
	 */
	public String getInterpacmotion(){
		try{
			if (_Interpacmotion==null){
				_Interpacmotion=getStringProperty("interpacMotion");
				return _Interpacmotion;
			}else {
				return _Interpacmotion;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for interpacMotion.
	 * @param v Value to Set.
	 */
	public void setInterpacmotion(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/interpacMotion",v);
		_Interpacmotion=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrqcscandata> getAllXnatMrqcscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrqcscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrqcscandata> getXnatMrqcscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrqcscandata> getXnatMrqcscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatMrqcscandata getXnatMrqcscandatasByXnatQcscandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:mrQcScanData/xnat_qcscandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatMrqcscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
