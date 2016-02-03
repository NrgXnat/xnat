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
public abstract class AutoXnatCtscandata extends XnatImagescandata implements org.nrg.xdat.model.XnatCtscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatCtscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:ctScanData";

	public AutoXnatCtscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatCtscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatCtscandata(UserI user)
	 **/
	public AutoXnatCtscandata(){}

	public AutoXnatCtscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:ctScanData";
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

	private String _Parameters_rescale_intercept=null;

	/**
	 * @return Returns the parameters/rescale/intercept.
	 */
	public String getParameters_rescale_intercept(){
		try{
			if (_Parameters_rescale_intercept==null){
				_Parameters_rescale_intercept=getStringProperty("parameters/rescale/intercept");
				return _Parameters_rescale_intercept;
			}else {
				return _Parameters_rescale_intercept;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/rescale/intercept.
	 * @param v Value to Set.
	 */
	public void setParameters_rescale_intercept(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/rescale/intercept",v);
		_Parameters_rescale_intercept=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_rescale_slope=null;

	/**
	 * @return Returns the parameters/rescale/slope.
	 */
	public String getParameters_rescale_slope(){
		try{
			if (_Parameters_rescale_slope==null){
				_Parameters_rescale_slope=getStringProperty("parameters/rescale/slope");
				return _Parameters_rescale_slope;
			}else {
				return _Parameters_rescale_slope;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/rescale/slope.
	 * @param v Value to Set.
	 */
	public void setParameters_rescale_slope(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/rescale/slope",v);
		_Parameters_rescale_slope=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_kvp=null;

	/**
	 * @return Returns the parameters/kvp.
	 */
	public Double getParameters_kvp() {
		try{
			if (_Parameters_kvp==null){
				_Parameters_kvp=getDoubleProperty("parameters/kvp");
				return _Parameters_kvp;
			}else {
				return _Parameters_kvp;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/kvp.
	 * @param v Value to Set.
	 */
	public void setParameters_kvp(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/kvp",v);
		_Parameters_kvp=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_acquisitionnumber=null;

	/**
	 * @return Returns the parameters/acquisitionNumber.
	 */
	public Integer getParameters_acquisitionnumber() {
		try{
			if (_Parameters_acquisitionnumber==null){
				_Parameters_acquisitionnumber=getIntegerProperty("parameters/acquisitionNumber");
				return _Parameters_acquisitionnumber;
			}else {
				return _Parameters_acquisitionnumber;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/acquisitionNumber.
	 * @param v Value to Set.
	 */
	public void setParameters_acquisitionnumber(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/acquisitionNumber",v);
		_Parameters_acquisitionnumber=null;
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

	private Double _Parameters_collectiondiameter=null;

	/**
	 * @return Returns the parameters/collectionDiameter.
	 */
	public Double getParameters_collectiondiameter() {
		try{
			if (_Parameters_collectiondiameter==null){
				_Parameters_collectiondiameter=getDoubleProperty("parameters/collectionDiameter");
				return _Parameters_collectiondiameter;
			}else {
				return _Parameters_collectiondiameter;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/collectionDiameter.
	 * @param v Value to Set.
	 */
	public void setParameters_collectiondiameter(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/collectionDiameter",v);
		_Parameters_collectiondiameter=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_distancesourcetodetector=null;

	/**
	 * @return Returns the parameters/distanceSourceToDetector.
	 */
	public Double getParameters_distancesourcetodetector() {
		try{
			if (_Parameters_distancesourcetodetector==null){
				_Parameters_distancesourcetodetector=getDoubleProperty("parameters/distanceSourceToDetector");
				return _Parameters_distancesourcetodetector;
			}else {
				return _Parameters_distancesourcetodetector;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/distanceSourceToDetector.
	 * @param v Value to Set.
	 */
	public void setParameters_distancesourcetodetector(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/distanceSourceToDetector",v);
		_Parameters_distancesourcetodetector=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_distancesourcetopatient=null;

	/**
	 * @return Returns the parameters/distanceSourceToPatient.
	 */
	public Double getParameters_distancesourcetopatient() {
		try{
			if (_Parameters_distancesourcetopatient==null){
				_Parameters_distancesourcetopatient=getDoubleProperty("parameters/distanceSourceToPatient");
				return _Parameters_distancesourcetopatient;
			}else {
				return _Parameters_distancesourcetopatient;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/distanceSourceToPatient.
	 * @param v Value to Set.
	 */
	public void setParameters_distancesourcetopatient(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/distanceSourceToPatient",v);
		_Parameters_distancesourcetopatient=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_gantrytilt=null;

	/**
	 * @return Returns the parameters/gantryTilt.
	 */
	public Double getParameters_gantrytilt() {
		try{
			if (_Parameters_gantrytilt==null){
				_Parameters_gantrytilt=getDoubleProperty("parameters/gantryTilt");
				return _Parameters_gantrytilt;
			}else {
				return _Parameters_gantrytilt;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/gantryTilt.
	 * @param v Value to Set.
	 */
	public void setParameters_gantrytilt(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/gantryTilt",v);
		_Parameters_gantrytilt=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_tableheight=null;

	/**
	 * @return Returns the parameters/tableHeight.
	 */
	public Double getParameters_tableheight() {
		try{
			if (_Parameters_tableheight==null){
				_Parameters_tableheight=getDoubleProperty("parameters/tableHeight");
				return _Parameters_tableheight;
			}else {
				return _Parameters_tableheight;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/tableHeight.
	 * @param v Value to Set.
	 */
	public void setParameters_tableheight(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/tableHeight",v);
		_Parameters_tableheight=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_rotationdirection=null;

	/**
	 * @return Returns the parameters/rotationDirection.
	 */
	public String getParameters_rotationdirection(){
		try{
			if (_Parameters_rotationdirection==null){
				_Parameters_rotationdirection=getStringProperty("parameters/rotationDirection");
				return _Parameters_rotationdirection;
			}else {
				return _Parameters_rotationdirection;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/rotationDirection.
	 * @param v Value to Set.
	 */
	public void setParameters_rotationdirection(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/rotationDirection",v);
		_Parameters_rotationdirection=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_exposuretime=null;

	/**
	 * @return Returns the parameters/exposureTime.
	 */
	public Double getParameters_exposuretime() {
		try{
			if (_Parameters_exposuretime==null){
				_Parameters_exposuretime=getDoubleProperty("parameters/exposureTime");
				return _Parameters_exposuretime;
			}else {
				return _Parameters_exposuretime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/exposureTime.
	 * @param v Value to Set.
	 */
	public void setParameters_exposuretime(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/exposureTime",v);
		_Parameters_exposuretime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_xraytubecurrent=null;

	/**
	 * @return Returns the parameters/xrayTubeCurrent.
	 */
	public Double getParameters_xraytubecurrent() {
		try{
			if (_Parameters_xraytubecurrent==null){
				_Parameters_xraytubecurrent=getDoubleProperty("parameters/xrayTubeCurrent");
				return _Parameters_xraytubecurrent;
			}else {
				return _Parameters_xraytubecurrent;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/xrayTubeCurrent.
	 * @param v Value to Set.
	 */
	public void setParameters_xraytubecurrent(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/xrayTubeCurrent",v);
		_Parameters_xraytubecurrent=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_exposure=null;

	/**
	 * @return Returns the parameters/exposure.
	 */
	public Double getParameters_exposure() {
		try{
			if (_Parameters_exposure==null){
				_Parameters_exposure=getDoubleProperty("parameters/exposure");
				return _Parameters_exposure;
			}else {
				return _Parameters_exposure;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/exposure.
	 * @param v Value to Set.
	 */
	public void setParameters_exposure(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/exposure",v);
		_Parameters_exposure=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_filter=null;

	/**
	 * @return Returns the parameters/filter.
	 */
	public String getParameters_filter(){
		try{
			if (_Parameters_filter==null){
				_Parameters_filter=getStringProperty("parameters/filter");
				return _Parameters_filter;
			}else {
				return _Parameters_filter;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/filter.
	 * @param v Value to Set.
	 */
	public void setParameters_filter(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/filter",v);
		_Parameters_filter=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_generatorpower=null;

	/**
	 * @return Returns the parameters/generatorPower.
	 */
	public Double getParameters_generatorpower() {
		try{
			if (_Parameters_generatorpower==null){
				_Parameters_generatorpower=getDoubleProperty("parameters/generatorPower");
				return _Parameters_generatorpower;
			}else {
				return _Parameters_generatorpower;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/generatorPower.
	 * @param v Value to Set.
	 */
	public void setParameters_generatorpower(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/generatorPower",v);
		_Parameters_generatorpower=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot> _Parameters_focalspots_focalspot =null;

	/**
	 * parameters/focalSpots/focalSpot
	 * @return Returns an List of org.nrg.xdat.om.XnatCtscandataFocalspot
	 */
	public <A extends org.nrg.xdat.model.XnatCtscandataFocalspotI> List<A> getParameters_focalspots_focalspot() {
		try{
			if (_Parameters_focalspots_focalspot==null){
				_Parameters_focalspots_focalspot=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/focalSpots/focalSpot"));
			}
			return (List<A>) _Parameters_focalspots_focalspot;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatCtscandataFocalspot>();}
	}

	/**
	 * Sets the value for parameters/focalSpots/focalSpot.
	 * @param v Value to Set.
	 */
	public void setParameters_focalspots_focalspot(ItemI v) throws Exception{
		_Parameters_focalspots_focalspot =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/focalSpots/focalSpot",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/focalSpots/focalSpot",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/focalSpots/focalSpot
	 * Adds org.nrg.xdat.model.XnatCtscandataFocalspotI
	 */
	public <A extends org.nrg.xdat.model.XnatCtscandataFocalspotI> void addParameters_focalspots_focalspot(A item) throws Exception{
	setParameters_focalspots_focalspot((ItemI)item);
	}

	/**
	 * Removes the parameters/focalSpots/focalSpot of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_focalspots_focalspot(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_focalspots_focalspot =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/focalSpots/focalSpot",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_convolutionkernel=null;

	/**
	 * @return Returns the parameters/convolutionKernel.
	 */
	public String getParameters_convolutionkernel(){
		try{
			if (_Parameters_convolutionkernel==null){
				_Parameters_convolutionkernel=getStringProperty("parameters/convolutionKernel");
				return _Parameters_convolutionkernel;
			}else {
				return _Parameters_convolutionkernel;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/convolutionKernel.
	 * @param v Value to Set.
	 */
	public void setParameters_convolutionkernel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/convolutionKernel",v);
		_Parameters_convolutionkernel=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_collimationwidth_single=null;

	/**
	 * @return Returns the parameters/collimationWidth/single.
	 */
	public Double getParameters_collimationwidth_single() {
		try{
			if (_Parameters_collimationwidth_single==null){
				_Parameters_collimationwidth_single=getDoubleProperty("parameters/collimationWidth/single");
				return _Parameters_collimationwidth_single;
			}else {
				return _Parameters_collimationwidth_single;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/collimationWidth/single.
	 * @param v Value to Set.
	 */
	public void setParameters_collimationwidth_single(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/collimationWidth/single",v);
		_Parameters_collimationwidth_single=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_collimationwidth_total=null;

	/**
	 * @return Returns the parameters/collimationWidth/total.
	 */
	public Double getParameters_collimationwidth_total() {
		try{
			if (_Parameters_collimationwidth_total==null){
				_Parameters_collimationwidth_total=getDoubleProperty("parameters/collimationWidth/total");
				return _Parameters_collimationwidth_total;
			}else {
				return _Parameters_collimationwidth_total;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/collimationWidth/total.
	 * @param v Value to Set.
	 */
	public void setParameters_collimationwidth_total(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/collimationWidth/total",v);
		_Parameters_collimationwidth_total=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_tablespeed=null;

	/**
	 * @return Returns the parameters/tableSpeed.
	 */
	public Double getParameters_tablespeed() {
		try{
			if (_Parameters_tablespeed==null){
				_Parameters_tablespeed=getDoubleProperty("parameters/tableSpeed");
				return _Parameters_tablespeed;
			}else {
				return _Parameters_tablespeed;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/tableSpeed.
	 * @param v Value to Set.
	 */
	public void setParameters_tablespeed(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/tableSpeed",v);
		_Parameters_tablespeed=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_tablefeedperrotation=null;

	/**
	 * @return Returns the parameters/tableFeedPerRotation.
	 */
	public Double getParameters_tablefeedperrotation() {
		try{
			if (_Parameters_tablefeedperrotation==null){
				_Parameters_tablefeedperrotation=getDoubleProperty("parameters/tableFeedPerRotation");
				return _Parameters_tablefeedperrotation;
			}else {
				return _Parameters_tablefeedperrotation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/tableFeedPerRotation.
	 * @param v Value to Set.
	 */
	public void setParameters_tablefeedperrotation(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/tableFeedPerRotation",v);
		_Parameters_tablefeedperrotation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_pitchfactor=null;

	/**
	 * @return Returns the parameters/pitchFactor.
	 */
	public Double getParameters_pitchfactor() {
		try{
			if (_Parameters_pitchfactor==null){
				_Parameters_pitchfactor=getDoubleProperty("parameters/pitchFactor");
				return _Parameters_pitchfactor;
			}else {
				return _Parameters_pitchfactor;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pitchFactor.
	 * @param v Value to Set.
	 */
	public void setParameters_pitchfactor(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pitchFactor",v);
		_Parameters_pitchfactor=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_estimateddosesaving_modulation=null;

	/**
	 * @return Returns the parameters/estimatedDoseSaving/modulation.
	 */
	public String getParameters_estimateddosesaving_modulation(){
		try{
			if (_Parameters_estimateddosesaving_modulation==null){
				_Parameters_estimateddosesaving_modulation=getStringProperty("parameters/estimatedDoseSaving/modulation");
				return _Parameters_estimateddosesaving_modulation;
			}else {
				return _Parameters_estimateddosesaving_modulation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/estimatedDoseSaving/modulation.
	 * @param v Value to Set.
	 */
	public void setParameters_estimateddosesaving_modulation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/estimatedDoseSaving/modulation",v);
		_Parameters_estimateddosesaving_modulation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_estimateddosesaving=null;

	/**
	 * @return Returns the parameters/estimatedDoseSaving.
	 */
	public Double getParameters_estimateddosesaving() {
		try{
			if (_Parameters_estimateddosesaving==null){
				_Parameters_estimateddosesaving=getDoubleProperty("parameters/estimatedDoseSaving");
				return _Parameters_estimateddosesaving;
			}else {
				return _Parameters_estimateddosesaving;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/estimatedDoseSaving.
	 * @param v Value to Set.
	 */
	public void setParameters_estimateddosesaving(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/estimatedDoseSaving",v);
		_Parameters_estimateddosesaving=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_ctdivol=null;

	/**
	 * @return Returns the parameters/ctDIvol.
	 */
	public Double getParameters_ctdivol() {
		try{
			if (_Parameters_ctdivol==null){
				_Parameters_ctdivol=getDoubleProperty("parameters/ctDIvol");
				return _Parameters_ctdivol;
			}else {
				return _Parameters_ctdivol;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ctDIvol.
	 * @param v Value to Set.
	 */
	public void setParameters_ctdivol(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ctDIvol",v);
		_Parameters_ctdivol=null;
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
	 * @return Returns the xnat:ctScanData/parameters_contrastbolus_xnat_contrastbolus_id.
	 */
	public Integer getParameters_contrastbolusFK(){
		try{
			if (_Parameters_contrastbolusFK==null){
				_Parameters_contrastbolusFK=getIntegerProperty("xnat:ctScanData/parameters_contrastbolus_xnat_contrastbolus_id");
				return _Parameters_contrastbolusFK;
			}else {
				return _Parameters_contrastbolusFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:ctScanData/parameters_contrastbolus_xnat_contrastbolus_id.
	 * @param v Value to Set.
	 */
	public void setParameters_contrastbolusFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters_contrastbolus_xnat_contrastbolus_id",v);
		_Parameters_contrastbolusFK=null;
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

	public static ArrayList<org.nrg.xdat.om.XnatCtscandata> getAllXnatCtscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandata> al = new ArrayList<org.nrg.xdat.om.XnatCtscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatCtscandata> getXnatCtscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandata> al = new ArrayList<org.nrg.xdat.om.XnatCtscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatCtscandata> getXnatCtscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatCtscandata> al = new ArrayList<org.nrg.xdat.om.XnatCtscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatCtscandata getXnatCtscandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:ctScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatCtscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //parameters/focalSpots/focalSpot
	        for(org.nrg.xdat.model.XnatCtscandataFocalspotI childParameters_focalspots_focalspot : this.getParameters_focalspots_focalspot()){
	            if (childParameters_focalspots_focalspot!=null){
	              for(ResourceFile rf: ((XnatCtscandataFocalspot)childParameters_focalspots_focalspot).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/focalSpots/focalSpot[" + ((XnatCtscandataFocalspot)childParameters_focalspots_focalspot).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/focalSpots/focalSpot/" + ((XnatCtscandataFocalspot)childParameters_focalspots_focalspot).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
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
