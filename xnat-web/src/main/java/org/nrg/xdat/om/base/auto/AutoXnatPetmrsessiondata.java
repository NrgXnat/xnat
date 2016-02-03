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
public abstract class AutoXnatPetmrsessiondata extends XnatImagesessiondata implements org.nrg.xdat.model.XnatPetmrsessiondataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPetmrsessiondata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:petmrSessionData";

	public AutoXnatPetmrsessiondata(ItemI item)
	{
		super(item);
	}

	public AutoXnatPetmrsessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPetmrsessiondata(UserI user)
	 **/
	public AutoXnatPetmrsessiondata(){}

	public AutoXnatPetmrsessiondata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:petmrSessionData";
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

	//FIELD

	private String _Studytype=null;

	/**
	 * @return Returns the studyType.
	 */
	public String getStudytype(){
		try{
			if (_Studytype==null){
				_Studytype=getStringProperty("studyType");
				return _Studytype;
			}else {
				return _Studytype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for studyType.
	 * @param v Value to Set.
	 */
	public void setStudytype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/studyType",v);
		_Studytype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Patientid=null;

	/**
	 * @return Returns the patientID.
	 */
	public String getPatientid(){
		try{
			if (_Patientid==null){
				_Patientid=getStringProperty("patientID");
				return _Patientid;
			}else {
				return _Patientid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for patientID.
	 * @param v Value to Set.
	 */
	public void setPatientid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/patientID",v);
		_Patientid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Patientname=null;

	/**
	 * @return Returns the patientName.
	 */
	public String getPatientname(){
		try{
			if (_Patientname==null){
				_Patientname=getStringProperty("patientName");
				return _Patientname;
			}else {
				return _Patientname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for patientName.
	 * @param v Value to Set.
	 */
	public void setPatientname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/patientName",v);
		_Patientname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tracer_dose_units=null;

	/**
	 * @return Returns the tracer/dose/units.
	 */
	public String getTracer_dose_units(){
		try{
			if (_Tracer_dose_units==null){
				_Tracer_dose_units=getStringProperty("tracer/dose/units");
				return _Tracer_dose_units;
			}else {
				return _Tracer_dose_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/dose/units.
	 * @param v Value to Set.
	 */
	public void setTracer_dose_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/dose/units",v);
		_Tracer_dose_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Tracer_dose=null;

	/**
	 * @return Returns the tracer/dose.
	 */
	public Double getTracer_dose() {
		try{
			if (_Tracer_dose==null){
				_Tracer_dose=getDoubleProperty("tracer/dose");
				return _Tracer_dose;
			}else {
				return _Tracer_dose;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/dose.
	 * @param v Value to Set.
	 */
	public void setTracer_dose(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/dose",v);
		_Tracer_dose=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Tracer_specificactivity=null;

	/**
	 * @return Returns the tracer/specificActivity.
	 */
	public Double getTracer_specificactivity() {
		try{
			if (_Tracer_specificactivity==null){
				_Tracer_specificactivity=getDoubleProperty("tracer/specificActivity");
				return _Tracer_specificactivity;
			}else {
				return _Tracer_specificactivity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/specificActivity.
	 * @param v Value to Set.
	 */
	public void setTracer_specificactivity(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/specificActivity",v);
		_Tracer_specificactivity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tracer_totalmass_units=null;

	/**
	 * @return Returns the tracer/totalMass/units.
	 */
	public String getTracer_totalmass_units(){
		try{
			if (_Tracer_totalmass_units==null){
				_Tracer_totalmass_units=getStringProperty("tracer/totalMass/units");
				return _Tracer_totalmass_units;
			}else {
				return _Tracer_totalmass_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/totalMass/units.
	 * @param v Value to Set.
	 */
	public void setTracer_totalmass_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/totalMass/units",v);
		_Tracer_totalmass_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Tracer_totalmass=null;

	/**
	 * @return Returns the tracer/totalMass.
	 */
	public Double getTracer_totalmass() {
		try{
			if (_Tracer_totalmass==null){
				_Tracer_totalmass=getDoubleProperty("tracer/totalMass");
				return _Tracer_totalmass;
			}else {
				return _Tracer_totalmass;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/totalMass.
	 * @param v Value to Set.
	 */
	public void setTracer_totalmass(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/totalMass",v);
		_Tracer_totalmass=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tracer_intermediate_units=null;

	/**
	 * @return Returns the tracer/intermediate/units.
	 */
	public String getTracer_intermediate_units(){
		try{
			if (_Tracer_intermediate_units==null){
				_Tracer_intermediate_units=getStringProperty("tracer/intermediate/units");
				return _Tracer_intermediate_units;
			}else {
				return _Tracer_intermediate_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/intermediate/units.
	 * @param v Value to Set.
	 */
	public void setTracer_intermediate_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/intermediate/units",v);
		_Tracer_intermediate_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Tracer_intermediate=null;

	/**
	 * @return Returns the tracer/intermediate.
	 */
	public Double getTracer_intermediate() {
		try{
			if (_Tracer_intermediate==null){
				_Tracer_intermediate=getDoubleProperty("tracer/intermediate");
				return _Tracer_intermediate;
			}else {
				return _Tracer_intermediate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/intermediate.
	 * @param v Value to Set.
	 */
	public void setTracer_intermediate(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/intermediate",v);
		_Tracer_intermediate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Tracer_isotope_halfLife=null;

	/**
	 * @return Returns the tracer/isotope/half-life.
	 */
	public Double getTracer_isotope_halfLife() {
		try{
			if (_Tracer_isotope_halfLife==null){
				_Tracer_isotope_halfLife=getDoubleProperty("tracer/isotope/half-life");
				return _Tracer_isotope_halfLife;
			}else {
				return _Tracer_isotope_halfLife;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/isotope/half-life.
	 * @param v Value to Set.
	 */
	public void setTracer_isotope_halfLife(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/isotope/half-life",v);
		_Tracer_isotope_halfLife=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tracer_isotope=null;

	/**
	 * @return Returns the tracer/isotope.
	 */
	public String getTracer_isotope(){
		try{
			if (_Tracer_isotope==null){
				_Tracer_isotope=getStringProperty("tracer/isotope");
				return _Tracer_isotope;
			}else {
				return _Tracer_isotope;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/isotope.
	 * @param v Value to Set.
	 */
	public void setTracer_isotope(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/isotope",v);
		_Tracer_isotope=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Tracer_transmissions=null;

	/**
	 * @return Returns the tracer/transmissions.
	 */
	public Integer getTracer_transmissions() {
		try{
			if (_Tracer_transmissions==null){
				_Tracer_transmissions=getIntegerProperty("tracer/transmissions");
				return _Tracer_transmissions;
			}else {
				return _Tracer_transmissions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/transmissions.
	 * @param v Value to Set.
	 */
	public void setTracer_transmissions(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/transmissions",v);
		_Tracer_transmissions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Tracer_transmissionsStarttime=null;

	/**
	 * @return Returns the tracer/transmissions_starttime.
	 */
	public Object getTracer_transmissionsStarttime(){
		try{
			if (_Tracer_transmissionsStarttime==null){
				_Tracer_transmissionsStarttime=getProperty("tracer/transmissions_starttime");
				return _Tracer_transmissionsStarttime;
			}else {
				return _Tracer_transmissionsStarttime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/transmissions_starttime.
	 * @param v Value to Set.
	 */
	public void setTracer_transmissionsStarttime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/transmissions_starttime",v);
		_Tracer_transmissionsStarttime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Tracer_name=null;

	/**
	 * @return Returns the tracer/name.
	 */
	public String getTracer_name(){
		try{
			if (_Tracer_name==null){
				_Tracer_name=getStringProperty("tracer/name");
				return _Tracer_name;
			}else {
				return _Tracer_name;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/name.
	 * @param v Value to Set.
	 */
	public void setTracer_name(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/name",v);
		_Tracer_name=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Tracer_starttime=null;

	/**
	 * @return Returns the tracer/startTime.
	 */
	public Object getTracer_starttime(){
		try{
			if (_Tracer_starttime==null){
				_Tracer_starttime=getProperty("tracer/startTime");
				return _Tracer_starttime;
			}else {
				return _Tracer_starttime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tracer/startTime.
	 * @param v Value to Set.
	 */
	public void setTracer_starttime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tracer/startTime",v);
		_Tracer_starttime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _StartTime=null;

	/**
	 * @return Returns the start_time.
	 */
	public Object getStartTime(){
		try{
			if (_StartTime==null){
				_StartTime=getProperty("start_time");
				return _StartTime;
			}else {
				return _StartTime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for start_time.
	 * @param v Value to Set.
	 */
	public void setStartTime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/start_time",v);
		_StartTime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _StartTimeScan=null;

	/**
	 * @return Returns the start_time_scan.
	 */
	public Object getStartTimeScan(){
		try{
			if (_StartTimeScan==null){
				_StartTimeScan=getProperty("start_time_scan");
				return _StartTimeScan;
			}else {
				return _StartTimeScan;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for start_time_scan.
	 * @param v Value to Set.
	 */
	public void setStartTimeScan(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/start_time_scan",v);
		_StartTimeScan=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _StartTimeInjection=null;

	/**
	 * @return Returns the start_time_injection.
	 */
	public Object getStartTimeInjection(){
		try{
			if (_StartTimeInjection==null){
				_StartTimeInjection=getProperty("start_time_injection");
				return _StartTimeInjection;
			}else {
				return _StartTimeInjection;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for start_time_injection.
	 * @param v Value to Set.
	 */
	public void setStartTimeInjection(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/start_time_injection",v);
		_StartTimeInjection=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _BloodGlucose=null;

	/**
	 * @return Returns the blood_glucose.
	 */
	public Double getBloodGlucose() {
		try{
			if (_BloodGlucose==null){
				_BloodGlucose=getDoubleProperty("blood_glucose");
				return _BloodGlucose;
			}else {
				return _BloodGlucose;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for blood_glucose.
	 * @param v Value to Set.
	 */
	public void setBloodGlucose(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/blood_glucose",v);
		_BloodGlucose=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _BloodGlucoseUnits=null;

	/**
	 * @return Returns the blood_glucose_units.
	 */
	public String getBloodGlucoseUnits(){
		try{
			if (_BloodGlucoseUnits==null){
				_BloodGlucoseUnits=getStringProperty("blood_glucose_units");
				return _BloodGlucoseUnits;
			}else {
				return _BloodGlucoseUnits;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for blood_glucose_units.
	 * @param v Value to Set.
	 */
	public void setBloodGlucoseUnits(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/blood_glucose_units",v);
		_BloodGlucoseUnits=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _BloodGlucoseTime=null;

	/**
	 * @return Returns the blood_glucose_time.
	 */
	public Object getBloodGlucoseTime(){
		try{
			if (_BloodGlucoseTime==null){
				_BloodGlucoseTime=getProperty("blood_glucose_time");
				return _BloodGlucoseTime;
			}else {
				return _BloodGlucoseTime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for blood_glucose_time.
	 * @param v Value to Set.
	 */
	public void setBloodGlucoseTime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/blood_glucose_time",v);
		_BloodGlucoseTime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> getAllXnatPetmrsessiondatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> getXnatPetmrsessiondatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> getXnatPetmrsessiondatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata> al = new ArrayList<org.nrg.xdat.om.XnatPetmrsessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPetmrsessiondata getXnatPetmrsessiondatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:petmrSessionData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPetmrsessiondata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
