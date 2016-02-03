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
public abstract class AutoXnatXascandata extends XnatImagescandata implements org.nrg.xdat.model.XnatXascandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatXascandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:xaScanData";

	public AutoXnatXascandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatXascandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatXascandata(UserI user)
	 **/
	public AutoXnatXascandata(){}

	public AutoXnatXascandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:xaScanData";
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

	//FIELD

	private String _Parameters_pixelres_units=null;

	/**
	 * @return Returns the parameters/pixelRes/units.
	 */
	public String getParameters_pixelres_units(){
		try{
			if (_Parameters_pixelres_units==null){
				_Parameters_pixelres_units=getStringProperty("parameters/pixelRes/units");
				return _Parameters_pixelres_units;
			}else {
				return _Parameters_pixelres_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelRes/units.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelres_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelRes/units",v);
		_Parameters_pixelres_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_pixelres_x=null;

	/**
	 * @return Returns the parameters/pixelRes/x.
	 */
	public Integer getParameters_pixelres_x() {
		try{
			if (_Parameters_pixelres_x==null){
				_Parameters_pixelres_x=getIntegerProperty("parameters/pixelRes/x");
				return _Parameters_pixelres_x;
			}else {
				return _Parameters_pixelres_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelRes/x.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelres_x(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelRes/x",v);
		_Parameters_pixelres_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_pixelres_y=null;

	/**
	 * @return Returns the parameters/pixelRes/y.
	 */
	public Integer getParameters_pixelres_y() {
		try{
			if (_Parameters_pixelres_y==null){
				_Parameters_pixelres_y=getIntegerProperty("parameters/pixelRes/y");
				return _Parameters_pixelres_y;
			}else {
				return _Parameters_pixelres_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelRes/y.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelres_y(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelRes/y",v);
		_Parameters_pixelres_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_orientation=null;

	/**
	 * @return Returns the parameters/orientation.
	 */
	public String getParameters_orientation(){
		try{
			if (_Parameters_orientation==null){
				_Parameters_orientation=getStringProperty("parameters/orientation");
				return _Parameters_orientation;
			}else {
				return _Parameters_orientation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/orientation.
	 * @param v Value to Set.
	 */
	public void setParameters_orientation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/orientation",v);
		_Parameters_orientation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_fov_x=null;

	/**
	 * @return Returns the parameters/fov/x.
	 */
	public Integer getParameters_fov_x() {
		try{
			if (_Parameters_fov_x==null){
				_Parameters_fov_x=getIntegerProperty("parameters/fov/x");
				return _Parameters_fov_x;
			}else {
				return _Parameters_fov_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/fov/x.
	 * @param v Value to Set.
	 */
	public void setParameters_fov_x(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/fov/x",v);
		_Parameters_fov_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_fov_y=null;

	/**
	 * @return Returns the parameters/fov/y.
	 */
	public Integer getParameters_fov_y() {
		try{
			if (_Parameters_fov_y==null){
				_Parameters_fov_y=getIntegerProperty("parameters/fov/y");
				return _Parameters_fov_y;
			}else {
				return _Parameters_fov_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/fov/y.
	 * @param v Value to Set.
	 */
	public void setParameters_fov_y(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/fov/y",v);
		_Parameters_fov_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_imagetype=null;

	/**
	 * @return Returns the parameters/imageType.
	 */
	public String getParameters_imagetype(){
		try{
			if (_Parameters_imagetype==null){
				_Parameters_imagetype=getStringProperty("parameters/imageType");
				return _Parameters_imagetype;
			}else {
				return _Parameters_imagetype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/imageType.
	 * @param v Value to Set.
	 */
	public void setParameters_imagetype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/imageType",v);
		_Parameters_imagetype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_options=null;

	/**
	 * @return Returns the parameters/options.
	 */
	public String getParameters_options(){
		try{
			if (_Parameters_options==null){
				_Parameters_options=getStringProperty("parameters/options");
				return _Parameters_options;
			}else {
				return _Parameters_options;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/options.
	 * @param v Value to Set.
	 */
	public void setParameters_options(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/options",v);
		_Parameters_options=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_derivation=null;

	/**
	 * @return Returns the parameters/derivation.
	 */
	public String getParameters_derivation(){
		try{
			if (_Parameters_derivation==null){
				_Parameters_derivation=getStringProperty("parameters/derivation");
				return _Parameters_derivation;
			}else {
				return _Parameters_derivation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/derivation.
	 * @param v Value to Set.
	 */
	public void setParameters_derivation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/derivation",v);
		_Parameters_derivation=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatContrastbolus _Parameters_contrastbolus =null;

	/**
	 * parameters/contrastBolus
	 * @return org.nrg.xdat.om.XnatContrastbolus
	 */
	public org.nrg.xdat.om.XnatContrastbolus getParameters_contrastbolus() {
		try{
			if (_Parameters_contrastbolus==null){
				_Parameters_contrastbolus=((XnatContrastbolus)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("parameters/contrastBolus")));
				return _Parameters_contrastbolus;
			}else {
				return _Parameters_contrastbolus;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for parameters/contrastBolus.
	 * @param v Value to Set.
	 */
	public void setParameters_contrastbolus(ItemI v) throws Exception{
		_Parameters_contrastbolus =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/contrastBolus",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/contrastBolus",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/contrastBolus
	 * set org.nrg.xdat.model.XnatContrastbolusI
	 */
	public <A extends org.nrg.xdat.model.XnatContrastbolusI> void setParameters_contrastbolus(A item) throws Exception{
	setParameters_contrastbolus((ItemI)item);
	}

	/**
	 * Removes the parameters/contrastBolus.
	 * */
	public void removeParameters_contrastbolus() {
		_Parameters_contrastbolus =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/contrastBolus",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_contrastbolusFK=null;

	/**
	 * @return Returns the xnat:xaScanData/parameters_contrastbolus_xnat_contrastbolus_id.
	 */
	public Integer getParameters_contrastbolusFK(){
		try{
			if (_Parameters_contrastbolusFK==null){
				_Parameters_contrastbolusFK=getIntegerProperty("xnat:xaScanData/parameters_contrastbolus_xnat_contrastbolus_id");
				return _Parameters_contrastbolusFK;
			}else {
				return _Parameters_contrastbolusFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:xaScanData/parameters_contrastbolus_xnat_contrastbolus_id.
	 * @param v Value to Set.
	 */
	public void setParameters_contrastbolusFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters_contrastbolus_xnat_contrastbolus_id",v);
		_Parameters_contrastbolusFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatXascandata> getAllXnatXascandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatXascandata> al = new ArrayList<org.nrg.xdat.om.XnatXascandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatXascandata> getXnatXascandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatXascandata> al = new ArrayList<org.nrg.xdat.om.XnatXascandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatXascandata> getXnatXascandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatXascandata> al = new ArrayList<org.nrg.xdat.om.XnatXascandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatXascandata getXnatXascandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:xaScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatXascandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //parameters/contrastBolus
	        XnatContrastbolus childParameters_contrastbolus = (XnatContrastbolus)this.getParameters_contrastbolus();
	            if (childParameters_contrastbolus!=null){
	              for(ResourceFile rf: ((XnatContrastbolus)childParameters_contrastbolus).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/contrastBolus[" + ((XnatContrastbolus)childParameters_contrastbolus).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/contrastBolus/" + ((XnatContrastbolus)childParameters_contrastbolus).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
