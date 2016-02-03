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
public abstract class AutoXnatAygtssdata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatAygtssdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAygtssdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat_a:ygtssData";

	public AutoXnatAygtssdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatAygtssdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAygtssdata(UserI user)
	 **/
	public AutoXnatAygtssdata(){}

	public AutoXnatAygtssdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat_a:ygtssData";
	}
	 private org.nrg.xdat.om.XnatSubjectassessordata _Subjectassessordata =null;

	/**
	 * subjectAssessorData
	 * @return org.nrg.xdat.om.XnatSubjectassessordata
	 */
	public org.nrg.xdat.om.XnatSubjectassessordata getSubjectassessordata() {
		try{
			if (_Subjectassessordata==null){
				_Subjectassessordata=((XnatSubjectassessordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("subjectAssessorData")));
				return _Subjectassessordata;
			}else {
				return _Subjectassessordata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for subjectAssessorData.
	 * @param v Value to Set.
	 */
	public void setSubjectassessordata(ItemI v) throws Exception{
		_Subjectassessordata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subjectAssessorData
	 * set org.nrg.xdat.model.XnatSubjectassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectassessordataI> void setSubjectassessordata(A item) throws Exception{
	setSubjectassessordata((ItemI)item);
	}

	/**
	 * Removes the subjectAssessorData.
	 * */
	public void removeSubjectassessordata() {
		_Subjectassessordata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Filledoutby=null;

	/**
	 * @return Returns the filledOutBy.
	 */
	public String getFilledoutby(){
		try{
			if (_Filledoutby==null){
				_Filledoutby=getStringProperty("filledOutBy");
				return _Filledoutby;
			}else {
				return _Filledoutby;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for filledOutBy.
	 * @param v Value to Set.
	 */
	public void setFilledoutby(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/filledOutBy",v);
		_Filledoutby=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Period=null;

	/**
	 * @return Returns the period.
	 */
	public String getPeriod(){
		try{
			if (_Period==null){
				_Period=getStringProperty("period");
				return _Period;
			}else {
				return _Period;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for period.
	 * @param v Value to Set.
	 */
	public void setPeriod(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/period",v);
		_Period=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Worsteverage=null;

	/**
	 * @return Returns the worstEverAge.
	 */
	public Double getWorsteverage() {
		try{
			if (_Worsteverage==null){
				_Worsteverage=getDoubleProperty("worstEverAge");
				return _Worsteverage;
			}else {
				return _Worsteverage;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for worstEverAge.
	 * @param v Value to Set.
	 */
	public void setWorsteverage(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/worstEverAge",v);
		_Worsteverage=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motor_number=null;

	/**
	 * @return Returns the motor/number.
	 */
	public Integer getMotor_number() {
		try{
			if (_Motor_number==null){
				_Motor_number=getIntegerProperty("motor/number");
				return _Motor_number;
			}else {
				return _Motor_number;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/number.
	 * @param v Value to Set.
	 */
	public void setMotor_number(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/number",v);
		_Motor_number=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motor_frequency=null;

	/**
	 * @return Returns the motor/frequency.
	 */
	public Integer getMotor_frequency() {
		try{
			if (_Motor_frequency==null){
				_Motor_frequency=getIntegerProperty("motor/frequency");
				return _Motor_frequency;
			}else {
				return _Motor_frequency;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/frequency.
	 * @param v Value to Set.
	 */
	public void setMotor_frequency(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/frequency",v);
		_Motor_frequency=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motor_intensity=null;

	/**
	 * @return Returns the motor/intensity.
	 */
	public Integer getMotor_intensity() {
		try{
			if (_Motor_intensity==null){
				_Motor_intensity=getIntegerProperty("motor/intensity");
				return _Motor_intensity;
			}else {
				return _Motor_intensity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/intensity.
	 * @param v Value to Set.
	 */
	public void setMotor_intensity(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/intensity",v);
		_Motor_intensity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motor_complexity=null;

	/**
	 * @return Returns the motor/complexity.
	 */
	public Integer getMotor_complexity() {
		try{
			if (_Motor_complexity==null){
				_Motor_complexity=getIntegerProperty("motor/complexity");
				return _Motor_complexity;
			}else {
				return _Motor_complexity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/complexity.
	 * @param v Value to Set.
	 */
	public void setMotor_complexity(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/complexity",v);
		_Motor_complexity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Motor_interference=null;

	/**
	 * @return Returns the motor/interference.
	 */
	public Integer getMotor_interference() {
		try{
			if (_Motor_interference==null){
				_Motor_interference=getIntegerProperty("motor/interference");
				return _Motor_interference;
			}else {
				return _Motor_interference;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/interference.
	 * @param v Value to Set.
	 */
	public void setMotor_interference(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/interference",v);
		_Motor_interference=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Motor_inventory=null;

	/**
	 * @return Returns the motor/inventory.
	 */
	public String getMotor_inventory(){
		try{
			if (_Motor_inventory==null){
				_Motor_inventory=getStringProperty("motor/inventory");
				return _Motor_inventory;
			}else {
				return _Motor_inventory;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motor/inventory.
	 * @param v Value to Set.
	 */
	public void setMotor_inventory(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motor/inventory",v);
		_Motor_inventory=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Phonic_number=null;

	/**
	 * @return Returns the phonic/number.
	 */
	public Integer getPhonic_number() {
		try{
			if (_Phonic_number==null){
				_Phonic_number=getIntegerProperty("phonic/number");
				return _Phonic_number;
			}else {
				return _Phonic_number;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/number.
	 * @param v Value to Set.
	 */
	public void setPhonic_number(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/number",v);
		_Phonic_number=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Phonic_frequency=null;

	/**
	 * @return Returns the phonic/frequency.
	 */
	public Integer getPhonic_frequency() {
		try{
			if (_Phonic_frequency==null){
				_Phonic_frequency=getIntegerProperty("phonic/frequency");
				return _Phonic_frequency;
			}else {
				return _Phonic_frequency;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/frequency.
	 * @param v Value to Set.
	 */
	public void setPhonic_frequency(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/frequency",v);
		_Phonic_frequency=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Phonic_intensity=null;

	/**
	 * @return Returns the phonic/intensity.
	 */
	public Integer getPhonic_intensity() {
		try{
			if (_Phonic_intensity==null){
				_Phonic_intensity=getIntegerProperty("phonic/intensity");
				return _Phonic_intensity;
			}else {
				return _Phonic_intensity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/intensity.
	 * @param v Value to Set.
	 */
	public void setPhonic_intensity(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/intensity",v);
		_Phonic_intensity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Phonic_complexity=null;

	/**
	 * @return Returns the phonic/complexity.
	 */
	public Integer getPhonic_complexity() {
		try{
			if (_Phonic_complexity==null){
				_Phonic_complexity=getIntegerProperty("phonic/complexity");
				return _Phonic_complexity;
			}else {
				return _Phonic_complexity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/complexity.
	 * @param v Value to Set.
	 */
	public void setPhonic_complexity(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/complexity",v);
		_Phonic_complexity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Phonic_interference=null;

	/**
	 * @return Returns the phonic/interference.
	 */
	public Integer getPhonic_interference() {
		try{
			if (_Phonic_interference==null){
				_Phonic_interference=getIntegerProperty("phonic/interference");
				return _Phonic_interference;
			}else {
				return _Phonic_interference;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/interference.
	 * @param v Value to Set.
	 */
	public void setPhonic_interference(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/interference",v);
		_Phonic_interference=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Phonic_inventory=null;

	/**
	 * @return Returns the phonic/inventory.
	 */
	public String getPhonic_inventory(){
		try{
			if (_Phonic_inventory==null){
				_Phonic_inventory=getStringProperty("phonic/inventory");
				return _Phonic_inventory;
			}else {
				return _Phonic_inventory;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phonic/inventory.
	 * @param v Value to Set.
	 */
	public void setPhonic_inventory(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phonic/inventory",v);
		_Phonic_inventory=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Impairment=null;

	/**
	 * @return Returns the impairment.
	 */
	public Integer getImpairment() {
		try{
			if (_Impairment==null){
				_Impairment=getIntegerProperty("impairment");
				return _Impairment;
			}else {
				return _Impairment;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for impairment.
	 * @param v Value to Set.
	 */
	public void setImpairment(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/impairment",v);
		_Impairment=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAygtssdata> getAllXnatAygtssdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAygtssdata> al = new ArrayList<org.nrg.xdat.om.XnatAygtssdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAygtssdata> getXnatAygtssdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAygtssdata> al = new ArrayList<org.nrg.xdat.om.XnatAygtssdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAygtssdata> getXnatAygtssdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAygtssdata> al = new ArrayList<org.nrg.xdat.om.XnatAygtssdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAygtssdata getXnatAygtssdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat_a:ygtssData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAygtssdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //subjectAssessorData
	        XnatSubjectassessordata childSubjectassessordata = (XnatSubjectassessordata)this.getSubjectassessordata();
	            if (childSubjectassessordata!=null){
	              for(ResourceFile rf: ((XnatSubjectassessordata)childSubjectassessordata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subjectAssessorData[" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subjectAssessorData/" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
