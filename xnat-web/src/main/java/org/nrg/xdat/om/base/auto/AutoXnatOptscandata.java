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
public abstract class AutoXnatOptscandata extends XnatImagescandata implements org.nrg.xdat.model.XnatOptscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatOptscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:optScanData";

	public AutoXnatOptscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatOptscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatOptscandata(UserI user)
	 **/
	public AutoXnatOptscandata(){}

	public AutoXnatOptscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:optScanData";
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

	private String _Parameters_voxelres_units=null;

	/**
	 * @return Returns the parameters/voxelRes/units.
	 */
	public String getParameters_voxelres_units(){
		try{
			if (_Parameters_voxelres_units==null){
				_Parameters_voxelres_units=getStringProperty("parameters/voxelRes/units");
				return _Parameters_voxelres_units;
			}else {
				return _Parameters_voxelres_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/voxelRes/units.
	 * @param v Value to Set.
	 */
	public void setParameters_voxelres_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/voxelRes/units",v);
		_Parameters_voxelres_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_voxelres_x=null;

	/**
	 * @return Returns the parameters/voxelRes/x.
	 */
	public Double getParameters_voxelres_x() {
		try{
			if (_Parameters_voxelres_x==null){
				_Parameters_voxelres_x=getDoubleProperty("parameters/voxelRes/x");
				return _Parameters_voxelres_x;
			}else {
				return _Parameters_voxelres_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/voxelRes/x.
	 * @param v Value to Set.
	 */
	public void setParameters_voxelres_x(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/voxelRes/x",v);
		_Parameters_voxelres_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_voxelres_y=null;

	/**
	 * @return Returns the parameters/voxelRes/y.
	 */
	public Double getParameters_voxelres_y() {
		try{
			if (_Parameters_voxelres_y==null){
				_Parameters_voxelres_y=getDoubleProperty("parameters/voxelRes/y");
				return _Parameters_voxelres_y;
			}else {
				return _Parameters_voxelres_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/voxelRes/y.
	 * @param v Value to Set.
	 */
	public void setParameters_voxelres_y(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/voxelRes/y",v);
		_Parameters_voxelres_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_voxelres_z=null;

	/**
	 * @return Returns the parameters/voxelRes/z.
	 */
	public Double getParameters_voxelres_z() {
		try{
			if (_Parameters_voxelres_z==null){
				_Parameters_voxelres_z=getDoubleProperty("parameters/voxelRes/z");
				return _Parameters_voxelres_z;
			}else {
				return _Parameters_voxelres_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/voxelRes/z.
	 * @param v Value to Set.
	 */
	public void setParameters_voxelres_z(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/voxelRes/z",v);
		_Parameters_voxelres_z=null;
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

	private String _Parameters_laterality=null;

	/**
	 * @return Returns the parameters/laterality.
	 */
	public String getParameters_laterality(){
		try{
			if (_Parameters_laterality==null){
				_Parameters_laterality=getStringProperty("parameters/laterality");
				return _Parameters_laterality;
			}else {
				return _Parameters_laterality;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/laterality.
	 * @param v Value to Set.
	 */
	public void setParameters_laterality(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/laterality",v);
		_Parameters_laterality=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_illuminationWavelength=null;

	/**
	 * @return Returns the parameters/illumination_wavelength.
	 */
	public String getParameters_illuminationWavelength(){
		try{
			if (_Parameters_illuminationWavelength==null){
				_Parameters_illuminationWavelength=getStringProperty("parameters/illumination_wavelength");
				return _Parameters_illuminationWavelength;
			}else {
				return _Parameters_illuminationWavelength;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/illumination_wavelength.
	 * @param v Value to Set.
	 */
	public void setParameters_illuminationWavelength(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/illumination_wavelength",v);
		_Parameters_illuminationWavelength=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_illuminationPower=null;

	/**
	 * @return Returns the parameters/illumination_power.
	 */
	public String getParameters_illuminationPower(){
		try{
			if (_Parameters_illuminationPower==null){
				_Parameters_illuminationPower=getStringProperty("parameters/illumination_power");
				return _Parameters_illuminationPower;
			}else {
				return _Parameters_illuminationPower;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/illumination_power.
	 * @param v Value to Set.
	 */
	public void setParameters_illuminationPower(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/illumination_power",v);
		_Parameters_illuminationPower=null;
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

	private String _Dcmvalidation=null;

	/**
	 * @return Returns the dcmValidation.
	 */
	public String getDcmvalidation(){
		try{
			if (_Dcmvalidation==null){
				_Dcmvalidation=getStringProperty("dcmValidation");
				return _Dcmvalidation;
			}else {
				return _Dcmvalidation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmValidation.
	 * @param v Value to Set.
	 */
	public void setDcmvalidation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcmValidation",v);
		_Dcmvalidation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Dcmvalidation_status=null;

	/**
	 * @return Returns the dcmValidation/status.
	 */
	public Boolean getDcmvalidation_status() {
		try{
			if (_Dcmvalidation_status==null){
				_Dcmvalidation_status=getBooleanProperty("dcmValidation/status");
				return _Dcmvalidation_status;
			}else {
				return _Dcmvalidation_status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmValidation/status.
	 * @param v Value to Set.
	 */
	public void setDcmvalidation_status(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/dcmValidation/status",v);
		_Dcmvalidation_status=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatOptscandata> getAllXnatOptscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOptscandata> al = new ArrayList<org.nrg.xdat.om.XnatOptscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatOptscandata> getXnatOptscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOptscandata> al = new ArrayList<org.nrg.xdat.om.XnatOptscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatOptscandata> getXnatOptscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatOptscandata> al = new ArrayList<org.nrg.xdat.om.XnatOptscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatOptscandata getXnatOptscandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:optScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatOptscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
