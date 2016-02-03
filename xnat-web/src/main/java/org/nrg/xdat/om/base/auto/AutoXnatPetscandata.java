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
public abstract class AutoXnatPetscandata extends XnatImagescandata implements org.nrg.xdat.model.XnatPetscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPetscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:petScanData";

	public AutoXnatPetscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatPetscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPetscandata(UserI user)
	 **/
	public AutoXnatPetscandata(){}

	public AutoXnatPetscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:petScanData";
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

	private Integer _Parameters_orientation=null;

	/**
	 * @return Returns the parameters/orientation.
	 */
	public Integer getParameters_orientation() {
		try{
			if (_Parameters_orientation==null){
				_Parameters_orientation=getIntegerProperty("parameters/orientation");
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
	public void setParameters_orientation(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/orientation",v);
		_Parameters_orientation=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAddfield> _Parameters_addparam =null;

	/**
	 * parameters/addParam
	 * @return Returns an List of org.nrg.xdat.om.XnatAddfield
	 */
	public <A extends org.nrg.xdat.model.XnatAddfieldI> List<A> getParameters_addparam() {
		try{
			if (_Parameters_addparam==null){
				_Parameters_addparam=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/addParam"));
			}
			return (List<A>) _Parameters_addparam;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAddfield>();}
	}

	/**
	 * Sets the value for parameters/addParam.
	 * @param v Value to Set.
	 */
	public void setParameters_addparam(ItemI v) throws Exception{
		_Parameters_addparam =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/addParam
	 * Adds org.nrg.xdat.model.XnatAddfieldI
	 */
	public <A extends org.nrg.xdat.model.XnatAddfieldI> void addParameters_addparam(A item) throws Exception{
	setParameters_addparam((ItemI)item);
	}

	/**
	 * Removes the parameters/addParam of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_addparam(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_addparam =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_originalfilename=null;

	/**
	 * @return Returns the parameters/originalFileName.
	 */
	public String getParameters_originalfilename(){
		try{
			if (_Parameters_originalfilename==null){
				_Parameters_originalfilename=getStringProperty("parameters/originalFileName");
				return _Parameters_originalfilename;
			}else {
				return _Parameters_originalfilename;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/originalFileName.
	 * @param v Value to Set.
	 */
	public void setParameters_originalfilename(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/originalFileName",v);
		_Parameters_originalfilename=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_systemtype=null;

	/**
	 * @return Returns the parameters/systemType.
	 */
	public Integer getParameters_systemtype() {
		try{
			if (_Parameters_systemtype==null){
				_Parameters_systemtype=getIntegerProperty("parameters/systemType");
				return _Parameters_systemtype;
			}else {
				return _Parameters_systemtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/systemType.
	 * @param v Value to Set.
	 */
	public void setParameters_systemtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/systemType",v);
		_Parameters_systemtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_filetype=null;

	/**
	 * @return Returns the parameters/fileType.
	 */
	public Integer getParameters_filetype() {
		try{
			if (_Parameters_filetype==null){
				_Parameters_filetype=getIntegerProperty("parameters/fileType");
				return _Parameters_filetype;
			}else {
				return _Parameters_filetype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/fileType.
	 * @param v Value to Set.
	 */
	public void setParameters_filetype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/fileType",v);
		_Parameters_filetype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_transaxialfov=null;

	/**
	 * @return Returns the parameters/transaxialFOV.
	 */
	public Double getParameters_transaxialfov() {
		try{
			if (_Parameters_transaxialfov==null){
				_Parameters_transaxialfov=getDoubleProperty("parameters/transaxialFOV");
				return _Parameters_transaxialfov;
			}else {
				return _Parameters_transaxialfov;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/transaxialFOV.
	 * @param v Value to Set.
	 */
	public void setParameters_transaxialfov(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/transaxialFOV",v);
		_Parameters_transaxialfov=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_acqtype=null;

	/**
	 * @return Returns the parameters/acqType.
	 */
	public Integer getParameters_acqtype() {
		try{
			if (_Parameters_acqtype==null){
				_Parameters_acqtype=getIntegerProperty("parameters/acqType");
				return _Parameters_acqtype;
			}else {
				return _Parameters_acqtype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/acqType.
	 * @param v Value to Set.
	 */
	public void setParameters_acqtype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/acqType",v);
		_Parameters_acqtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_facility=null;

	/**
	 * @return Returns the parameters/facility.
	 */
	public String getParameters_facility(){
		try{
			if (_Parameters_facility==null){
				_Parameters_facility=getStringProperty("parameters/facility");
				return _Parameters_facility;
			}else {
				return _Parameters_facility;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/facility.
	 * @param v Value to Set.
	 */
	public void setParameters_facility(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/facility",v);
		_Parameters_facility=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_numplanes=null;

	/**
	 * @return Returns the parameters/numPlanes.
	 */
	public Integer getParameters_numplanes() {
		try{
			if (_Parameters_numplanes==null){
				_Parameters_numplanes=getIntegerProperty("parameters/numPlanes");
				return _Parameters_numplanes;
			}else {
				return _Parameters_numplanes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/numPlanes.
	 * @param v Value to Set.
	 */
	public void setParameters_numplanes(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/numPlanes",v);
		_Parameters_numplanes=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> _Parameters_frames_frame =null;

	/**
	 * parameters/frames/frame
	 * @return Returns an List of org.nrg.xdat.om.XnatPetscandataFrame
	 */
	public <A extends org.nrg.xdat.model.XnatPetscandataFrameI> List<A> getParameters_frames_frame() {
		try{
			if (_Parameters_frames_frame==null){
				_Parameters_frames_frame=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/frames/frame"));
			}
			return (List<A>) _Parameters_frames_frame;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatPetscandataFrame>();}
	}

	/**
	 * Sets the value for parameters/frames/frame.
	 * @param v Value to Set.
	 */
	public void setParameters_frames_frame(ItemI v) throws Exception{
		_Parameters_frames_frame =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/frames/frame",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/frames/frame",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/frames/frame
	 * Adds org.nrg.xdat.model.XnatPetscandataFrameI
	 */
	public <A extends org.nrg.xdat.model.XnatPetscandataFrameI> void addParameters_frames_frame(A item) throws Exception{
	setParameters_frames_frame((ItemI)item);
	}

	/**
	 * Removes the parameters/frames/frame of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_frames_frame(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_frames_frame =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/frames/frame",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Parameters_frames_numframes=null;

	/**
	 * @return Returns the parameters/frames/numFrames.
	 */
	public Object getParameters_frames_numframes(){
		try{
			if (_Parameters_frames_numframes==null){
				_Parameters_frames_numframes=getProperty("parameters/frames/numFrames");
				return _Parameters_frames_numframes;
			}else {
				return _Parameters_frames_numframes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/frames/numFrames.
	 * @param v Value to Set.
	 */
	public void setParameters_frames_numframes(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/frames/numFrames",v);
		_Parameters_frames_numframes=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_numgates=null;

	/**
	 * @return Returns the parameters/numGates.
	 */
	public Integer getParameters_numgates() {
		try{
			if (_Parameters_numgates==null){
				_Parameters_numgates=getIntegerProperty("parameters/numGates");
				return _Parameters_numgates;
			}else {
				return _Parameters_numgates;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/numGates.
	 * @param v Value to Set.
	 */
	public void setParameters_numgates(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/numGates",v);
		_Parameters_numgates=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_planeseparation=null;

	/**
	 * @return Returns the parameters/planeSeparation.
	 */
	public Double getParameters_planeseparation() {
		try{
			if (_Parameters_planeseparation==null){
				_Parameters_planeseparation=getDoubleProperty("parameters/planeSeparation");
				return _Parameters_planeseparation;
			}else {
				return _Parameters_planeseparation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/planeSeparation.
	 * @param v Value to Set.
	 */
	public void setParameters_planeseparation(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/planeSeparation",v);
		_Parameters_planeseparation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_binsize=null;

	/**
	 * @return Returns the parameters/binSize.
	 */
	public Double getParameters_binsize() {
		try{
			if (_Parameters_binsize==null){
				_Parameters_binsize=getDoubleProperty("parameters/binSize");
				return _Parameters_binsize;
			}else {
				return _Parameters_binsize;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/binSize.
	 * @param v Value to Set.
	 */
	public void setParameters_binsize(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/binSize",v);
		_Parameters_binsize=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_datatype=null;

	/**
	 * @return Returns the parameters/dataType.
	 */
	public Integer getParameters_datatype() {
		try{
			if (_Parameters_datatype==null){
				_Parameters_datatype=getIntegerProperty("parameters/dataType");
				return _Parameters_datatype;
			}else {
				return _Parameters_datatype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dataType.
	 * @param v Value to Set.
	 */
	public void setParameters_datatype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dataType",v);
		_Parameters_datatype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_dimensions_x=null;

	/**
	 * @return Returns the parameters/dimensions/x.
	 */
	public Integer getParameters_dimensions_x() {
		try{
			if (_Parameters_dimensions_x==null){
				_Parameters_dimensions_x=getIntegerProperty("parameters/dimensions/x");
				return _Parameters_dimensions_x;
			}else {
				return _Parameters_dimensions_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dimensions/x.
	 * @param v Value to Set.
	 */
	public void setParameters_dimensions_x(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dimensions/x",v);
		_Parameters_dimensions_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_dimensions_y=null;

	/**
	 * @return Returns the parameters/dimensions/y.
	 */
	public Integer getParameters_dimensions_y() {
		try{
			if (_Parameters_dimensions_y==null){
				_Parameters_dimensions_y=getIntegerProperty("parameters/dimensions/y");
				return _Parameters_dimensions_y;
			}else {
				return _Parameters_dimensions_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dimensions/y.
	 * @param v Value to Set.
	 */
	public void setParameters_dimensions_y(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dimensions/y",v);
		_Parameters_dimensions_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_dimensions_z=null;

	/**
	 * @return Returns the parameters/dimensions/z.
	 */
	public Integer getParameters_dimensions_z() {
		try{
			if (_Parameters_dimensions_z==null){
				_Parameters_dimensions_z=getIntegerProperty("parameters/dimensions/z");
				return _Parameters_dimensions_z;
			}else {
				return _Parameters_dimensions_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dimensions/z.
	 * @param v Value to Set.
	 */
	public void setParameters_dimensions_z(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dimensions/z",v);
		_Parameters_dimensions_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_dimensions_num=null;

	/**
	 * @return Returns the parameters/dimensions/num.
	 */
	public Integer getParameters_dimensions_num() {
		try{
			if (_Parameters_dimensions_num==null){
				_Parameters_dimensions_num=getIntegerProperty("parameters/dimensions/num");
				return _Parameters_dimensions_num;
			}else {
				return _Parameters_dimensions_num;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dimensions/num.
	 * @param v Value to Set.
	 */
	public void setParameters_dimensions_num(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dimensions/num",v);
		_Parameters_dimensions_num=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_offset_x=null;

	/**
	 * @return Returns the parameters/offset/x.
	 */
	public Double getParameters_offset_x() {
		try{
			if (_Parameters_offset_x==null){
				_Parameters_offset_x=getDoubleProperty("parameters/offset/x");
				return _Parameters_offset_x;
			}else {
				return _Parameters_offset_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/offset/x.
	 * @param v Value to Set.
	 */
	public void setParameters_offset_x(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/offset/x",v);
		_Parameters_offset_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_offset_y=null;

	/**
	 * @return Returns the parameters/offset/y.
	 */
	public Double getParameters_offset_y() {
		try{
			if (_Parameters_offset_y==null){
				_Parameters_offset_y=getDoubleProperty("parameters/offset/y");
				return _Parameters_offset_y;
			}else {
				return _Parameters_offset_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/offset/y.
	 * @param v Value to Set.
	 */
	public void setParameters_offset_y(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/offset/y",v);
		_Parameters_offset_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_offset_z=null;

	/**
	 * @return Returns the parameters/offset/z.
	 */
	public Double getParameters_offset_z() {
		try{
			if (_Parameters_offset_z==null){
				_Parameters_offset_z=getDoubleProperty("parameters/offset/z");
				return _Parameters_offset_z;
			}else {
				return _Parameters_offset_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/offset/z.
	 * @param v Value to Set.
	 */
	public void setParameters_offset_z(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/offset/z",v);
		_Parameters_offset_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_reconzoom=null;

	/**
	 * @return Returns the parameters/reconZoom.
	 */
	public Double getParameters_reconzoom() {
		try{
			if (_Parameters_reconzoom==null){
				_Parameters_reconzoom=getDoubleProperty("parameters/reconZoom");
				return _Parameters_reconzoom;
			}else {
				return _Parameters_reconzoom;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/reconZoom.
	 * @param v Value to Set.
	 */
	public void setParameters_reconzoom(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/reconZoom",v);
		_Parameters_reconzoom=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_pixelsize_x=null;

	/**
	 * @return Returns the parameters/pixelSize/x.
	 */
	public Double getParameters_pixelsize_x() {
		try{
			if (_Parameters_pixelsize_x==null){
				_Parameters_pixelsize_x=getDoubleProperty("parameters/pixelSize/x");
				return _Parameters_pixelsize_x;
			}else {
				return _Parameters_pixelsize_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelSize/x.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelsize_x(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelSize/x",v);
		_Parameters_pixelsize_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_pixelsize_y=null;

	/**
	 * @return Returns the parameters/pixelSize/y.
	 */
	public Double getParameters_pixelsize_y() {
		try{
			if (_Parameters_pixelsize_y==null){
				_Parameters_pixelsize_y=getDoubleProperty("parameters/pixelSize/y");
				return _Parameters_pixelsize_y;
			}else {
				return _Parameters_pixelsize_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelSize/y.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelsize_y(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelSize/y",v);
		_Parameters_pixelsize_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_pixelsize_z=null;

	/**
	 * @return Returns the parameters/pixelSize/z.
	 */
	public Double getParameters_pixelsize_z() {
		try{
			if (_Parameters_pixelsize_z==null){
				_Parameters_pixelsize_z=getDoubleProperty("parameters/pixelSize/z");
				return _Parameters_pixelsize_z;
			}else {
				return _Parameters_pixelsize_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelSize/z.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelsize_z(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelSize/z",v);
		_Parameters_pixelsize_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_filtercode=null;

	/**
	 * @return Returns the parameters/filterCode.
	 */
	public Integer getParameters_filtercode() {
		try{
			if (_Parameters_filtercode==null){
				_Parameters_filtercode=getIntegerProperty("parameters/filterCode");
				return _Parameters_filtercode;
			}else {
				return _Parameters_filtercode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/filterCode.
	 * @param v Value to Set.
	 */
	public void setParameters_filtercode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/filterCode",v);
		_Parameters_filtercode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_resolution_x=null;

	/**
	 * @return Returns the parameters/resolution/x.
	 */
	public Double getParameters_resolution_x() {
		try{
			if (_Parameters_resolution_x==null){
				_Parameters_resolution_x=getDoubleProperty("parameters/resolution/x");
				return _Parameters_resolution_x;
			}else {
				return _Parameters_resolution_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/resolution/x.
	 * @param v Value to Set.
	 */
	public void setParameters_resolution_x(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/resolution/x",v);
		_Parameters_resolution_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_resolution_y=null;

	/**
	 * @return Returns the parameters/resolution/y.
	 */
	public Double getParameters_resolution_y() {
		try{
			if (_Parameters_resolution_y==null){
				_Parameters_resolution_y=getDoubleProperty("parameters/resolution/y");
				return _Parameters_resolution_y;
			}else {
				return _Parameters_resolution_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/resolution/y.
	 * @param v Value to Set.
	 */
	public void setParameters_resolution_y(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/resolution/y",v);
		_Parameters_resolution_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_resolution_z=null;

	/**
	 * @return Returns the parameters/resolution/z.
	 */
	public Double getParameters_resolution_z() {
		try{
			if (_Parameters_resolution_z==null){
				_Parameters_resolution_z=getDoubleProperty("parameters/resolution/z");
				return _Parameters_resolution_z;
			}else {
				return _Parameters_resolution_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/resolution/z.
	 * @param v Value to Set.
	 */
	public void setParameters_resolution_z(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/resolution/z",v);
		_Parameters_resolution_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_numrelements=null;

	/**
	 * @return Returns the parameters/numRElements.
	 */
	public Double getParameters_numrelements() {
		try{
			if (_Parameters_numrelements==null){
				_Parameters_numrelements=getDoubleProperty("parameters/numRElements");
				return _Parameters_numrelements;
			}else {
				return _Parameters_numrelements;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/numRElements.
	 * @param v Value to Set.
	 */
	public void setParameters_numrelements(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/numRElements",v);
		_Parameters_numrelements=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_numangles=null;

	/**
	 * @return Returns the parameters/numAngles.
	 */
	public Double getParameters_numangles() {
		try{
			if (_Parameters_numangles==null){
				_Parameters_numangles=getDoubleProperty("parameters/numAngles");
				return _Parameters_numangles;
			}else {
				return _Parameters_numangles;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/numAngles.
	 * @param v Value to Set.
	 */
	public void setParameters_numangles(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/numAngles",v);
		_Parameters_numangles=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_zrotationangle=null;

	/**
	 * @return Returns the parameters/ZRotationAngle.
	 */
	public Double getParameters_zrotationangle() {
		try{
			if (_Parameters_zrotationangle==null){
				_Parameters_zrotationangle=getDoubleProperty("parameters/ZRotationAngle");
				return _Parameters_zrotationangle;
			}else {
				return _Parameters_zrotationangle;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ZRotationAngle.
	 * @param v Value to Set.
	 */
	public void setParameters_zrotationangle(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ZRotationAngle",v);
		_Parameters_zrotationangle=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_processingcode=null;

	/**
	 * @return Returns the parameters/processingCode.
	 */
	public Integer getParameters_processingcode() {
		try{
			if (_Parameters_processingcode==null){
				_Parameters_processingcode=getIntegerProperty("parameters/processingCode");
				return _Parameters_processingcode;
			}else {
				return _Parameters_processingcode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/processingCode.
	 * @param v Value to Set.
	 */
	public void setParameters_processingcode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/processingCode",v);
		_Parameters_processingcode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_gateduration=null;

	/**
	 * @return Returns the parameters/gateDuration.
	 */
	public Integer getParameters_gateduration() {
		try{
			if (_Parameters_gateduration==null){
				_Parameters_gateduration=getIntegerProperty("parameters/gateDuration");
				return _Parameters_gateduration;
			}else {
				return _Parameters_gateduration;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/gateDuration.
	 * @param v Value to Set.
	 */
	public void setParameters_gateduration(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/gateDuration",v);
		_Parameters_gateduration=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_rwaveoffset=null;

	/**
	 * @return Returns the parameters/rWaveOffset.
	 */
	public Integer getParameters_rwaveoffset() {
		try{
			if (_Parameters_rwaveoffset==null){
				_Parameters_rwaveoffset=getIntegerProperty("parameters/rWaveOffset");
				return _Parameters_rwaveoffset;
			}else {
				return _Parameters_rwaveoffset;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/rWaveOffset.
	 * @param v Value to Set.
	 */
	public void setParameters_rwaveoffset(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/rWaveOffset",v);
		_Parameters_rwaveoffset=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_numacceptedbeats=null;

	/**
	 * @return Returns the parameters/numAcceptedBeats.
	 */
	public Integer getParameters_numacceptedbeats() {
		try{
			if (_Parameters_numacceptedbeats==null){
				_Parameters_numacceptedbeats=getIntegerProperty("parameters/numAcceptedBeats");
				return _Parameters_numacceptedbeats;
			}else {
				return _Parameters_numacceptedbeats;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/numAcceptedBeats.
	 * @param v Value to Set.
	 */
	public void setParameters_numacceptedbeats(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/numAcceptedBeats",v);
		_Parameters_numacceptedbeats=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_filter_cutoff=null;

	/**
	 * @return Returns the parameters/filter/cutoff.
	 */
	public Double getParameters_filter_cutoff() {
		try{
			if (_Parameters_filter_cutoff==null){
				_Parameters_filter_cutoff=getDoubleProperty("parameters/filter/cutoff");
				return _Parameters_filter_cutoff;
			}else {
				return _Parameters_filter_cutoff;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/filter/cutoff.
	 * @param v Value to Set.
	 */
	public void setParameters_filter_cutoff(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/filter/cutoff",v);
		_Parameters_filter_cutoff=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_annotation=null;

	/**
	 * @return Returns the parameters/annotation.
	 */
	public String getParameters_annotation(){
		try{
			if (_Parameters_annotation==null){
				_Parameters_annotation=getStringProperty("parameters/annotation");
				return _Parameters_annotation;
			}else {
				return _Parameters_annotation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/annotation.
	 * @param v Value to Set.
	 */
	public void setParameters_annotation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/annotation",v);
		_Parameters_annotation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt11=null;

	/**
	 * @return Returns the parameters/MT_1_1.
	 */
	public Double getParameters_mt11() {
		try{
			if (_Parameters_mt11==null){
				_Parameters_mt11=getDoubleProperty("parameters/MT_1_1");
				return _Parameters_mt11;
			}else {
				return _Parameters_mt11;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_1_1.
	 * @param v Value to Set.
	 */
	public void setParameters_mt11(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_1_1",v);
		_Parameters_mt11=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt12=null;

	/**
	 * @return Returns the parameters/MT_1_2.
	 */
	public Double getParameters_mt12() {
		try{
			if (_Parameters_mt12==null){
				_Parameters_mt12=getDoubleProperty("parameters/MT_1_2");
				return _Parameters_mt12;
			}else {
				return _Parameters_mt12;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_1_2.
	 * @param v Value to Set.
	 */
	public void setParameters_mt12(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_1_2",v);
		_Parameters_mt12=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt13=null;

	/**
	 * @return Returns the parameters/MT_1_3.
	 */
	public Double getParameters_mt13() {
		try{
			if (_Parameters_mt13==null){
				_Parameters_mt13=getDoubleProperty("parameters/MT_1_3");
				return _Parameters_mt13;
			}else {
				return _Parameters_mt13;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_1_3.
	 * @param v Value to Set.
	 */
	public void setParameters_mt13(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_1_3",v);
		_Parameters_mt13=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt14=null;

	/**
	 * @return Returns the parameters/MT_1_4.
	 */
	public Double getParameters_mt14() {
		try{
			if (_Parameters_mt14==null){
				_Parameters_mt14=getDoubleProperty("parameters/MT_1_4");
				return _Parameters_mt14;
			}else {
				return _Parameters_mt14;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_1_4.
	 * @param v Value to Set.
	 */
	public void setParameters_mt14(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_1_4",v);
		_Parameters_mt14=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt21=null;

	/**
	 * @return Returns the parameters/MT_2_1.
	 */
	public Double getParameters_mt21() {
		try{
			if (_Parameters_mt21==null){
				_Parameters_mt21=getDoubleProperty("parameters/MT_2_1");
				return _Parameters_mt21;
			}else {
				return _Parameters_mt21;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_2_1.
	 * @param v Value to Set.
	 */
	public void setParameters_mt21(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_2_1",v);
		_Parameters_mt21=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt22=null;

	/**
	 * @return Returns the parameters/MT_2_2.
	 */
	public Double getParameters_mt22() {
		try{
			if (_Parameters_mt22==null){
				_Parameters_mt22=getDoubleProperty("parameters/MT_2_2");
				return _Parameters_mt22;
			}else {
				return _Parameters_mt22;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_2_2.
	 * @param v Value to Set.
	 */
	public void setParameters_mt22(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_2_2",v);
		_Parameters_mt22=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt23=null;

	/**
	 * @return Returns the parameters/MT_2_3.
	 */
	public Double getParameters_mt23() {
		try{
			if (_Parameters_mt23==null){
				_Parameters_mt23=getDoubleProperty("parameters/MT_2_3");
				return _Parameters_mt23;
			}else {
				return _Parameters_mt23;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_2_3.
	 * @param v Value to Set.
	 */
	public void setParameters_mt23(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_2_3",v);
		_Parameters_mt23=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt24=null;

	/**
	 * @return Returns the parameters/MT_2_4.
	 */
	public Double getParameters_mt24() {
		try{
			if (_Parameters_mt24==null){
				_Parameters_mt24=getDoubleProperty("parameters/MT_2_4");
				return _Parameters_mt24;
			}else {
				return _Parameters_mt24;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_2_4.
	 * @param v Value to Set.
	 */
	public void setParameters_mt24(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_2_4",v);
		_Parameters_mt24=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt31=null;

	/**
	 * @return Returns the parameters/MT_3_1.
	 */
	public Double getParameters_mt31() {
		try{
			if (_Parameters_mt31==null){
				_Parameters_mt31=getDoubleProperty("parameters/MT_3_1");
				return _Parameters_mt31;
			}else {
				return _Parameters_mt31;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_3_1.
	 * @param v Value to Set.
	 */
	public void setParameters_mt31(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_3_1",v);
		_Parameters_mt31=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt32=null;

	/**
	 * @return Returns the parameters/MT_3_2.
	 */
	public Double getParameters_mt32() {
		try{
			if (_Parameters_mt32==null){
				_Parameters_mt32=getDoubleProperty("parameters/MT_3_2");
				return _Parameters_mt32;
			}else {
				return _Parameters_mt32;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_3_2.
	 * @param v Value to Set.
	 */
	public void setParameters_mt32(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_3_2",v);
		_Parameters_mt32=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt33=null;

	/**
	 * @return Returns the parameters/MT_3_3.
	 */
	public Double getParameters_mt33() {
		try{
			if (_Parameters_mt33==null){
				_Parameters_mt33=getDoubleProperty("parameters/MT_3_3");
				return _Parameters_mt33;
			}else {
				return _Parameters_mt33;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_3_3.
	 * @param v Value to Set.
	 */
	public void setParameters_mt33(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_3_3",v);
		_Parameters_mt33=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_mt34=null;

	/**
	 * @return Returns the parameters/MT_3_4.
	 */
	public Double getParameters_mt34() {
		try{
			if (_Parameters_mt34==null){
				_Parameters_mt34=getDoubleProperty("parameters/MT_3_4");
				return _Parameters_mt34;
			}else {
				return _Parameters_mt34;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/MT_3_4.
	 * @param v Value to Set.
	 */
	public void setParameters_mt34(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/MT_3_4",v);
		_Parameters_mt34=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_rfilter_cutoff=null;

	/**
	 * @return Returns the parameters/RFilter/cutoff.
	 */
	public Double getParameters_rfilter_cutoff() {
		try{
			if (_Parameters_rfilter_cutoff==null){
				_Parameters_rfilter_cutoff=getDoubleProperty("parameters/RFilter/cutoff");
				return _Parameters_rfilter_cutoff;
			}else {
				return _Parameters_rfilter_cutoff;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/RFilter/cutoff.
	 * @param v Value to Set.
	 */
	public void setParameters_rfilter_cutoff(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/RFilter/cutoff",v);
		_Parameters_rfilter_cutoff=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_rfilter_resolution=null;

	/**
	 * @return Returns the parameters/RFilter/resolution.
	 */
	public Double getParameters_rfilter_resolution() {
		try{
			if (_Parameters_rfilter_resolution==null){
				_Parameters_rfilter_resolution=getDoubleProperty("parameters/RFilter/resolution");
				return _Parameters_rfilter_resolution;
			}else {
				return _Parameters_rfilter_resolution;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/RFilter/resolution.
	 * @param v Value to Set.
	 */
	public void setParameters_rfilter_resolution(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/RFilter/resolution",v);
		_Parameters_rfilter_resolution=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_rfilter_code=null;

	/**
	 * @return Returns the parameters/RFilter/code.
	 */
	public Integer getParameters_rfilter_code() {
		try{
			if (_Parameters_rfilter_code==null){
				_Parameters_rfilter_code=getIntegerProperty("parameters/RFilter/code");
				return _Parameters_rfilter_code;
			}else {
				return _Parameters_rfilter_code;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/RFilter/code.
	 * @param v Value to Set.
	 */
	public void setParameters_rfilter_code(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/RFilter/code",v);
		_Parameters_rfilter_code=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_rfilter_order=null;

	/**
	 * @return Returns the parameters/RFilter/order.
	 */
	public Integer getParameters_rfilter_order() {
		try{
			if (_Parameters_rfilter_order==null){
				_Parameters_rfilter_order=getIntegerProperty("parameters/RFilter/order");
				return _Parameters_rfilter_order;
			}else {
				return _Parameters_rfilter_order;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/RFilter/order.
	 * @param v Value to Set.
	 */
	public void setParameters_rfilter_order(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/RFilter/order",v);
		_Parameters_rfilter_order=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_zfilter_cutoff=null;

	/**
	 * @return Returns the parameters/ZFilter/cutoff.
	 */
	public Double getParameters_zfilter_cutoff() {
		try{
			if (_Parameters_zfilter_cutoff==null){
				_Parameters_zfilter_cutoff=getDoubleProperty("parameters/ZFilter/cutoff");
				return _Parameters_zfilter_cutoff;
			}else {
				return _Parameters_zfilter_cutoff;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ZFilter/cutoff.
	 * @param v Value to Set.
	 */
	public void setParameters_zfilter_cutoff(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ZFilter/cutoff",v);
		_Parameters_zfilter_cutoff=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_zfilter_resolution=null;

	/**
	 * @return Returns the parameters/ZFilter/resolution.
	 */
	public Double getParameters_zfilter_resolution() {
		try{
			if (_Parameters_zfilter_resolution==null){
				_Parameters_zfilter_resolution=getDoubleProperty("parameters/ZFilter/resolution");
				return _Parameters_zfilter_resolution;
			}else {
				return _Parameters_zfilter_resolution;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ZFilter/resolution.
	 * @param v Value to Set.
	 */
	public void setParameters_zfilter_resolution(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ZFilter/resolution",v);
		_Parameters_zfilter_resolution=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_zfilter_code=null;

	/**
	 * @return Returns the parameters/ZFilter/code.
	 */
	public Integer getParameters_zfilter_code() {
		try{
			if (_Parameters_zfilter_code==null){
				_Parameters_zfilter_code=getIntegerProperty("parameters/ZFilter/code");
				return _Parameters_zfilter_code;
			}else {
				return _Parameters_zfilter_code;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ZFilter/code.
	 * @param v Value to Set.
	 */
	public void setParameters_zfilter_code(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ZFilter/code",v);
		_Parameters_zfilter_code=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_zfilter_order=null;

	/**
	 * @return Returns the parameters/ZFilter/order.
	 */
	public Integer getParameters_zfilter_order() {
		try{
			if (_Parameters_zfilter_order==null){
				_Parameters_zfilter_order=getIntegerProperty("parameters/ZFilter/order");
				return _Parameters_zfilter_order;
			}else {
				return _Parameters_zfilter_order;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ZFilter/order.
	 * @param v Value to Set.
	 */
	public void setParameters_zfilter_order(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ZFilter/order",v);
		_Parameters_zfilter_order=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_scattertype=null;

	/**
	 * @return Returns the parameters/scatterType.
	 */
	public Integer getParameters_scattertype() {
		try{
			if (_Parameters_scattertype==null){
				_Parameters_scattertype=getIntegerProperty("parameters/scatterType");
				return _Parameters_scattertype;
			}else {
				return _Parameters_scattertype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/scatterType.
	 * @param v Value to Set.
	 */
	public void setParameters_scattertype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/scatterType",v);
		_Parameters_scattertype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_recontype=null;

	/**
	 * @return Returns the parameters/reconType.
	 */
	public Integer getParameters_recontype() {
		try{
			if (_Parameters_recontype==null){
				_Parameters_recontype=getIntegerProperty("parameters/reconType");
				return _Parameters_recontype;
			}else {
				return _Parameters_recontype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/reconType.
	 * @param v Value to Set.
	 */
	public void setParameters_recontype(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/reconType",v);
		_Parameters_recontype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_reconviews=null;

	/**
	 * @return Returns the parameters/reconViews.
	 */
	public Integer getParameters_reconviews() {
		try{
			if (_Parameters_reconviews==null){
				_Parameters_reconviews=getIntegerProperty("parameters/reconViews");
				return _Parameters_reconviews;
			}else {
				return _Parameters_reconviews;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/reconViews.
	 * @param v Value to Set.
	 */
	public void setParameters_reconviews(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/reconViews",v);
		_Parameters_reconviews=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_bedposition=null;

	/**
	 * @return Returns the parameters/bedPosition.
	 */
	public Double getParameters_bedposition() {
		try{
			if (_Parameters_bedposition==null){
				_Parameters_bedposition=getDoubleProperty("parameters/bedPosition");
				return _Parameters_bedposition;
			}else {
				return _Parameters_bedposition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/bedPosition.
	 * @param v Value to Set.
	 */
	public void setParameters_bedposition(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/bedPosition",v);
		_Parameters_bedposition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_ecatcalibrationfactor=null;

	/**
	 * @return Returns the parameters/ecatCalibrationFactor.
	 */
	public Double getParameters_ecatcalibrationfactor() {
		try{
			if (_Parameters_ecatcalibrationfactor==null){
				_Parameters_ecatcalibrationfactor=getDoubleProperty("parameters/ecatCalibrationFactor");
				return _Parameters_ecatcalibrationfactor;
			}else {
				return _Parameters_ecatcalibrationfactor;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ecatCalibrationFactor.
	 * @param v Value to Set.
	 */
	public void setParameters_ecatcalibrationfactor(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ecatCalibrationFactor",v);
		_Parameters_ecatcalibrationfactor=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Ecatvalidation=null;

	/**
	 * @return Returns the ecatValidation.
	 */
	public String getEcatvalidation(){
		try{
			if (_Ecatvalidation==null){
				_Ecatvalidation=getStringProperty("ecatValidation");
				return _Ecatvalidation;
			}else {
				return _Ecatvalidation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ecatValidation.
	 * @param v Value to Set.
	 */
	public void setEcatvalidation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ecatValidation",v);
		_Ecatvalidation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Ecatvalidation_status=null;

	/**
	 * @return Returns the ecatValidation/status.
	 */
	public Boolean getEcatvalidation_status() {
		try{
			if (_Ecatvalidation_status==null){
				_Ecatvalidation_status=getBooleanProperty("ecatValidation/status");
				return _Ecatvalidation_status;
			}else {
				return _Ecatvalidation_status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ecatValidation/status.
	 * @param v Value to Set.
	 */
	public void setEcatvalidation_status(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/ecatValidation/status",v);
		_Ecatvalidation_status=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandata> getAllXnatPetscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandata> getXnatPetscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandata> getXnatPetscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPetscandata getXnatPetscandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:petScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPetscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //parameters/addParam
	        for(org.nrg.xdat.model.XnatAddfieldI childParameters_addparam : this.getParameters_addparam()){
	            if (childParameters_addparam!=null){
	              for(ResourceFile rf: ((XnatAddfield)childParameters_addparam).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/addParam[" + ((XnatAddfield)childParameters_addparam).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/addParam/" + ((XnatAddfield)childParameters_addparam).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //parameters/frames/frame
	        for(org.nrg.xdat.model.XnatPetscandataFrameI childParameters_frames_frame : this.getParameters_frames_frame()){
	            if (childParameters_frames_frame!=null){
	              for(ResourceFile rf: ((XnatPetscandataFrame)childParameters_frames_frame).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/frames/frame[" + ((XnatPetscandataFrame)childParameters_frames_frame).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/frames/frame/" + ((XnatPetscandataFrame)childParameters_frames_frame).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
