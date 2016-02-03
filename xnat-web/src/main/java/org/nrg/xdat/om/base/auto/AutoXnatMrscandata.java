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
public abstract class AutoXnatMrscandata extends XnatImagescandata implements org.nrg.xdat.model.XnatMrscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatMrscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:mrScanData";

	public AutoXnatMrscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatMrscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatMrscandata(UserI user)
	 **/
	public AutoXnatMrscandata(){}

	public AutoXnatMrscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:mrScanData";
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

	private Integer _Parameters_matrix_x=null;

	/**
	 * @return Returns the parameters/matrix/x.
	 */
	public Integer getParameters_matrix_x() {
		try{
			if (_Parameters_matrix_x==null){
				_Parameters_matrix_x=getIntegerProperty("parameters/matrix/x");
				return _Parameters_matrix_x;
			}else {
				return _Parameters_matrix_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/matrix/x.
	 * @param v Value to Set.
	 */
	public void setParameters_matrix_x(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/matrix/x",v);
		_Parameters_matrix_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_matrix_y=null;

	/**
	 * @return Returns the parameters/matrix/y.
	 */
	public Integer getParameters_matrix_y() {
		try{
			if (_Parameters_matrix_y==null){
				_Parameters_matrix_y=getIntegerProperty("parameters/matrix/y");
				return _Parameters_matrix_y;
			}else {
				return _Parameters_matrix_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/matrix/y.
	 * @param v Value to Set.
	 */
	public void setParameters_matrix_y(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/matrix/y",v);
		_Parameters_matrix_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_partitions=null;

	/**
	 * @return Returns the parameters/partitions.
	 */
	public Integer getParameters_partitions() {
		try{
			if (_Parameters_partitions==null){
				_Parameters_partitions=getIntegerProperty("parameters/partitions");
				return _Parameters_partitions;
			}else {
				return _Parameters_partitions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/partitions.
	 * @param v Value to Set.
	 */
	public void setParameters_partitions(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/partitions",v);
		_Parameters_partitions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_tr=null;

	/**
	 * @return Returns the parameters/tr.
	 */
	public Double getParameters_tr() {
		try{
			if (_Parameters_tr==null){
				_Parameters_tr=getDoubleProperty("parameters/tr");
				return _Parameters_tr;
			}else {
				return _Parameters_tr;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/tr.
	 * @param v Value to Set.
	 */
	public void setParameters_tr(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/tr",v);
		_Parameters_tr=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_te=null;

	/**
	 * @return Returns the parameters/te.
	 */
	public Double getParameters_te() {
		try{
			if (_Parameters_te==null){
				_Parameters_te=getDoubleProperty("parameters/te");
				return _Parameters_te;
			}else {
				return _Parameters_te;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/te.
	 * @param v Value to Set.
	 */
	public void setParameters_te(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/te",v);
		_Parameters_te=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_ti=null;

	/**
	 * @return Returns the parameters/ti.
	 */
	public Double getParameters_ti() {
		try{
			if (_Parameters_ti==null){
				_Parameters_ti=getDoubleProperty("parameters/ti");
				return _Parameters_ti;
			}else {
				return _Parameters_ti;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/ti.
	 * @param v Value to Set.
	 */
	public void setParameters_ti(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/ti",v);
		_Parameters_ti=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_flip=null;

	/**
	 * @return Returns the parameters/flip.
	 */
	public Integer getParameters_flip() {
		try{
			if (_Parameters_flip==null){
				_Parameters_flip=getIntegerProperty("parameters/flip");
				return _Parameters_flip;
			}else {
				return _Parameters_flip;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/flip.
	 * @param v Value to Set.
	 */
	public void setParameters_flip(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/flip",v);
		_Parameters_flip=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_sequence=null;

	/**
	 * @return Returns the parameters/sequence.
	 */
	public String getParameters_sequence(){
		try{
			if (_Parameters_sequence==null){
				_Parameters_sequence=getStringProperty("parameters/sequence");
				return _Parameters_sequence;
			}else {
				return _Parameters_sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/sequence.
	 * @param v Value to Set.
	 */
	public void setParameters_sequence(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/sequence",v);
		_Parameters_sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_origin=null;

	/**
	 * @return Returns the parameters/origin.
	 */
	public String getParameters_origin(){
		try{
			if (_Parameters_origin==null){
				_Parameters_origin=getStringProperty("parameters/origin");
				return _Parameters_origin;
			}else {
				return _Parameters_origin;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/origin.
	 * @param v Value to Set.
	 */
	public void setParameters_origin(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/origin",v);
		_Parameters_origin=null;
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

	private String _Parameters_scansequence=null;

	/**
	 * @return Returns the parameters/scanSequence.
	 */
	public String getParameters_scansequence(){
		try{
			if (_Parameters_scansequence==null){
				_Parameters_scansequence=getStringProperty("parameters/scanSequence");
				return _Parameters_scansequence;
			}else {
				return _Parameters_scansequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/scanSequence.
	 * @param v Value to Set.
	 */
	public void setParameters_scansequence(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/scanSequence",v);
		_Parameters_scansequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_seqvariant=null;

	/**
	 * @return Returns the parameters/seqVariant.
	 */
	public String getParameters_seqvariant(){
		try{
			if (_Parameters_seqvariant==null){
				_Parameters_seqvariant=getStringProperty("parameters/seqVariant");
				return _Parameters_seqvariant;
			}else {
				return _Parameters_seqvariant;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/seqVariant.
	 * @param v Value to Set.
	 */
	public void setParameters_seqvariant(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/seqVariant",v);
		_Parameters_seqvariant=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_scanoptions=null;

	/**
	 * @return Returns the parameters/scanOptions.
	 */
	public String getParameters_scanoptions(){
		try{
			if (_Parameters_scanoptions==null){
				_Parameters_scanoptions=getStringProperty("parameters/scanOptions");
				return _Parameters_scanoptions;
			}else {
				return _Parameters_scanoptions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/scanOptions.
	 * @param v Value to Set.
	 */
	public void setParameters_scanoptions(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/scanOptions",v);
		_Parameters_scanoptions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_acqtype=null;

	/**
	 * @return Returns the parameters/acqType.
	 */
	public String getParameters_acqtype(){
		try{
			if (_Parameters_acqtype==null){
				_Parameters_acqtype=getStringProperty("parameters/acqType");
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
	public void setParameters_acqtype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/acqType",v);
		_Parameters_acqtype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_coil=null;

	/**
	 * @return Returns the parameters/coil.
	 */
	public String getParameters_coil(){
		try{
			if (_Parameters_coil==null){
				_Parameters_coil=getStringProperty("parameters/coil");
				return _Parameters_coil;
			}else {
				return _Parameters_coil;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/coil.
	 * @param v Value to Set.
	 */
	public void setParameters_coil(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/coil",v);
		_Parameters_coil=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Parameters_dtiacqcount=null;

	/**
	 * @return Returns the parameters/dtiAcqCount.
	 */
	public Integer getParameters_dtiacqcount() {
		try{
			if (_Parameters_dtiacqcount==null){
				_Parameters_dtiacqcount=getIntegerProperty("parameters/dtiAcqCount");
				return _Parameters_dtiacqcount;
			}else {
				return _Parameters_dtiacqcount;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/dtiAcqCount.
	 * @param v Value to Set.
	 */
	public void setParameters_dtiacqcount(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/dtiAcqCount",v);
		_Parameters_dtiacqcount=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Parameters_pixelbandwidth=null;

	/**
	 * @return Returns the parameters/pixelBandwidth.
	 */
	public Double getParameters_pixelbandwidth() {
		try{
			if (_Parameters_pixelbandwidth==null){
				_Parameters_pixelbandwidth=getDoubleProperty("parameters/pixelBandwidth");
				return _Parameters_pixelbandwidth;
			}else {
				return _Parameters_pixelbandwidth;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/pixelBandwidth.
	 * @param v Value to Set.
	 */
	public void setParameters_pixelbandwidth(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/pixelBandwidth",v);
		_Parameters_pixelbandwidth=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_diffusion_bvalues=null;

	/**
	 * @return Returns the parameters/diffusion/bValues.
	 */
	public String getParameters_diffusion_bvalues(){
		try{
			if (_Parameters_diffusion_bvalues==null){
				_Parameters_diffusion_bvalues=getStringProperty("parameters/diffusion/bValues");
				return _Parameters_diffusion_bvalues;
			}else {
				return _Parameters_diffusion_bvalues;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/diffusion/bValues.
	 * @param v Value to Set.
	 */
	public void setParameters_diffusion_bvalues(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/diffusion/bValues",v);
		_Parameters_diffusion_bvalues=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_diffusion_directionality=null;

	/**
	 * @return Returns the parameters/diffusion/directionality.
	 */
	public String getParameters_diffusion_directionality(){
		try{
			if (_Parameters_diffusion_directionality==null){
				_Parameters_diffusion_directionality=getStringProperty("parameters/diffusion/directionality");
				return _Parameters_diffusion_directionality;
			}else {
				return _Parameters_diffusion_directionality;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/diffusion/directionality.
	 * @param v Value to Set.
	 */
	public void setParameters_diffusion_directionality(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/diffusion/directionality",v);
		_Parameters_diffusion_directionality=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_diffusion_orientations=null;

	/**
	 * @return Returns the parameters/diffusion/orientations.
	 */
	public String getParameters_diffusion_orientations(){
		try{
			if (_Parameters_diffusion_orientations==null){
				_Parameters_diffusion_orientations=getStringProperty("parameters/diffusion/orientations");
				return _Parameters_diffusion_orientations;
			}else {
				return _Parameters_diffusion_orientations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/diffusion/orientations.
	 * @param v Value to Set.
	 */
	public void setParameters_diffusion_orientations(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/diffusion/orientations",v);
		_Parameters_diffusion_orientations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameters_diffusion_anisotropytype=null;

	/**
	 * @return Returns the parameters/diffusion/anisotropyType.
	 */
	public String getParameters_diffusion_anisotropytype(){
		try{
			if (_Parameters_diffusion_anisotropytype==null){
				_Parameters_diffusion_anisotropytype=getStringProperty("parameters/diffusion/anisotropyType");
				return _Parameters_diffusion_anisotropytype;
			}else {
				return _Parameters_diffusion_anisotropytype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameters/diffusion/anisotropyType.
	 * @param v Value to Set.
	 */
	public void setParameters_diffusion_anisotropytype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameters/diffusion/anisotropyType",v);
		_Parameters_diffusion_anisotropytype=null;
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

	public static ArrayList<org.nrg.xdat.om.XnatMrscandata> getAllXnatMrscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrscandata> getXnatMrscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatMrscandata> getXnatMrscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatMrscandata> al = new ArrayList<org.nrg.xdat.om.XnatMrscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatMrscandata getXnatMrscandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:mrScanData/xnat_imagescandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatMrscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	return _return;
}
}
