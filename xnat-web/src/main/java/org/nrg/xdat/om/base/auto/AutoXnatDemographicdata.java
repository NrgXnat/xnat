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
public abstract class AutoXnatDemographicdata extends XnatAbstractdemographicdata implements org.nrg.xdat.model.XnatDemographicdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatDemographicdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:demographicData";

	public AutoXnatDemographicdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatDemographicdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatDemographicdata(UserI user)
	 **/
	public AutoXnatDemographicdata(){}

	public AutoXnatDemographicdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:demographicData";
	}
	 private org.nrg.xdat.om.XnatAbstractdemographicdata _Abstractdemographicdata =null;

	/**
	 * abstractDemographicData
	 * @return org.nrg.xdat.om.XnatAbstractdemographicdata
	 */
	public org.nrg.xdat.om.XnatAbstractdemographicdata getAbstractdemographicdata() {
		try{
			if (_Abstractdemographicdata==null){
				_Abstractdemographicdata=((XnatAbstractdemographicdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractDemographicData")));
				return _Abstractdemographicdata;
			}else {
				return _Abstractdemographicdata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractDemographicData.
	 * @param v Value to Set.
	 */
	public void setAbstractdemographicdata(ItemI v) throws Exception{
		_Abstractdemographicdata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractDemographicData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractDemographicData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractDemographicData
	 * set org.nrg.xdat.model.XnatAbstractdemographicdataI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractdemographicdataI> void setAbstractdemographicdata(A item) throws Exception{
	setAbstractdemographicdata((ItemI)item);
	}

	/**
	 * Removes the abstractDemographicData.
	 * */
	public void removeAbstractdemographicdata() {
		_Abstractdemographicdata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractDemographicData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Dob=null;

	/**
	 * @return Returns the dob.
	 */
	public Object getDob(){
		try{
			if (_Dob==null){
				_Dob=getProperty("dob");
				return _Dob;
			}else {
				return _Dob;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dob.
	 * @param v Value to Set.
	 */
	public void setDob(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dob",v);
		_Dob=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Yob=null;

	/**
	 * @return Returns the yob.
	 */
	public Integer getYob() {
		try{
			if (_Yob==null){
				_Yob=getIntegerProperty("yob");
				return _Yob;
			}else {
				return _Yob;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for yob.
	 * @param v Value to Set.
	 */
	public void setYob(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/yob",v);
		_Yob=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Age=null;

	/**
	 * @return Returns the age.
	 */
	public Integer getAge() {
		try{
			if (_Age==null){
				_Age=getIntegerProperty("age");
				return _Age;
			}else {
				return _Age;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for age.
	 * @param v Value to Set.
	 */
	public void setAge(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/age",v);
		_Age=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Gender=null;

	/**
	 * @return Returns the gender.
	 */
	public String getGender(){
		try{
			if (_Gender==null){
				_Gender=getStringProperty("gender");
				return _Gender;
			}else {
				return _Gender;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for gender.
	 * @param v Value to Set.
	 */
	public void setGender(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/gender",v);
		_Gender=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Handedness=null;

	/**
	 * @return Returns the handedness.
	 */
	public String getHandedness(){
		try{
			if (_Handedness==null){
				_Handedness=getStringProperty("handedness");
				return _Handedness;
			}else {
				return _Handedness;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for handedness.
	 * @param v Value to Set.
	 */
	public void setHandedness(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/handedness",v);
		_Handedness=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Ses=null;

	/**
	 * @return Returns the ses.
	 */
	public Integer getSes() {
		try{
			if (_Ses==null){
				_Ses=getIntegerProperty("ses");
				return _Ses;
			}else {
				return _Ses;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ses.
	 * @param v Value to Set.
	 */
	public void setSes(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ses",v);
		_Ses=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Employment=null;

	/**
	 * @return Returns the employment.
	 */
	public Integer getEmployment() {
		try{
			if (_Employment==null){
				_Employment=getIntegerProperty("employment");
				return _Employment;
			}else {
				return _Employment;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for employment.
	 * @param v Value to Set.
	 */
	public void setEmployment(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/employment",v);
		_Employment=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Education=null;

	/**
	 * @return Returns the education.
	 */
	public Integer getEducation() {
		try{
			if (_Education==null){
				_Education=getIntegerProperty("education");
				return _Education;
			}else {
				return _Education;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for education.
	 * @param v Value to Set.
	 */
	public void setEducation(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/education",v);
		_Education=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Educationdesc=null;

	/**
	 * @return Returns the educationDesc.
	 */
	public String getEducationdesc(){
		try{
			if (_Educationdesc==null){
				_Educationdesc=getStringProperty("educationDesc");
				return _Educationdesc;
			}else {
				return _Educationdesc;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for educationDesc.
	 * @param v Value to Set.
	 */
	public void setEducationdesc(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/educationDesc",v);
		_Educationdesc=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race=null;

	/**
	 * @return Returns the race.
	 */
	public String getRace(){
		try{
			if (_Race==null){
				_Race=getStringProperty("race");
				return _Race;
			}else {
				return _Race;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race.
	 * @param v Value to Set.
	 */
	public void setRace(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race",v);
		_Race=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race2=null;

	/**
	 * @return Returns the race2.
	 */
	public String getRace2(){
		try{
			if (_Race2==null){
				_Race2=getStringProperty("race2");
				return _Race2;
			}else {
				return _Race2;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race2.
	 * @param v Value to Set.
	 */
	public void setRace2(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race2",v);
		_Race2=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race3=null;

	/**
	 * @return Returns the race3.
	 */
	public String getRace3(){
		try{
			if (_Race3==null){
				_Race3=getStringProperty("race3");
				return _Race3;
			}else {
				return _Race3;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race3.
	 * @param v Value to Set.
	 */
	public void setRace3(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race3",v);
		_Race3=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race4=null;

	/**
	 * @return Returns the race4.
	 */
	public String getRace4(){
		try{
			if (_Race4==null){
				_Race4=getStringProperty("race4");
				return _Race4;
			}else {
				return _Race4;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race4.
	 * @param v Value to Set.
	 */
	public void setRace4(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race4",v);
		_Race4=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race5=null;

	/**
	 * @return Returns the race5.
	 */
	public String getRace5(){
		try{
			if (_Race5==null){
				_Race5=getStringProperty("race5");
				return _Race5;
			}else {
				return _Race5;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race5.
	 * @param v Value to Set.
	 */
	public void setRace5(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race5",v);
		_Race5=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Race6=null;

	/**
	 * @return Returns the race6.
	 */
	public String getRace6(){
		try{
			if (_Race6==null){
				_Race6=getStringProperty("race6");
				return _Race6;
			}else {
				return _Race6;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for race6.
	 * @param v Value to Set.
	 */
	public void setRace6(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/race6",v);
		_Race6=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Ethnicity=null;

	/**
	 * @return Returns the ethnicity.
	 */
	public String getEthnicity(){
		try{
			if (_Ethnicity==null){
				_Ethnicity=getStringProperty("ethnicity");
				return _Ethnicity;
			}else {
				return _Ethnicity;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ethnicity.
	 * @param v Value to Set.
	 */
	public void setEthnicity(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ethnicity",v);
		_Ethnicity=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Weight=null;

	/**
	 * @return Returns the weight.
	 */
	public Double getWeight() {
		try{
			if (_Weight==null){
				_Weight=getDoubleProperty("weight");
				return _Weight;
			}else {
				return _Weight;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for weight.
	 * @param v Value to Set.
	 */
	public void setWeight(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/weight",v);
		_Weight=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Weight_units=null;

	/**
	 * @return Returns the weight/units.
	 */
	public String getWeight_units(){
		try{
			if (_Weight_units==null){
				_Weight_units=getStringProperty("weight/units");
				return _Weight_units;
			}else {
				return _Weight_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for weight/units.
	 * @param v Value to Set.
	 */
	public void setWeight_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/weight/units",v);
		_Weight_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Height=null;

	/**
	 * @return Returns the height.
	 */
	public Double getHeight() {
		try{
			if (_Height==null){
				_Height=getDoubleProperty("height");
				return _Height;
			}else {
				return _Height;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for height.
	 * @param v Value to Set.
	 */
	public void setHeight(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/height",v);
		_Height=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Height_units=null;

	/**
	 * @return Returns the height/units.
	 */
	public String getHeight_units(){
		try{
			if (_Height_units==null){
				_Height_units=getStringProperty("height/units");
				return _Height_units;
			}else {
				return _Height_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for height/units.
	 * @param v Value to Set.
	 */
	public void setHeight_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/height/units",v);
		_Height_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _GestationalAge=null;

	/**
	 * @return Returns the gestational_age.
	 */
	public Double getGestationalAge() {
		try{
			if (_GestationalAge==null){
				_GestationalAge=getDoubleProperty("gestational_age");
				return _GestationalAge;
			}else {
				return _GestationalAge;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for gestational_age.
	 * @param v Value to Set.
	 */
	public void setGestationalAge(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/gestational_age",v);
		_GestationalAge=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _PostMenstrualAge=null;

	/**
	 * @return Returns the post_menstrual_age.
	 */
	public Double getPostMenstrualAge() {
		try{
			if (_PostMenstrualAge==null){
				_PostMenstrualAge=getDoubleProperty("post_menstrual_age");
				return _PostMenstrualAge;
			}else {
				return _PostMenstrualAge;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for post_menstrual_age.
	 * @param v Value to Set.
	 */
	public void setPostMenstrualAge(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/post_menstrual_age",v);
		_PostMenstrualAge=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _BirthWeight=null;

	/**
	 * @return Returns the birth_weight.
	 */
	public Double getBirthWeight() {
		try{
			if (_BirthWeight==null){
				_BirthWeight=getDoubleProperty("birth_weight");
				return _BirthWeight;
			}else {
				return _BirthWeight;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for birth_weight.
	 * @param v Value to Set.
	 */
	public void setBirthWeight(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/birth_weight",v);
		_BirthWeight=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatDemographicdata> getAllXnatDemographicdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatDemographicdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDemographicdata> getXnatDemographicdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatDemographicdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDemographicdata> getXnatDemographicdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDemographicdata> al = new ArrayList<org.nrg.xdat.om.XnatDemographicdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatDemographicdata getXnatDemographicdatasByXnatAbstractdemographicdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:demographicData/xnat_abstractdemographicdata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatDemographicdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractDemographicData
	        XnatAbstractdemographicdata childAbstractdemographicdata = (XnatAbstractdemographicdata)this.getAbstractdemographicdata();
	            if (childAbstractdemographicdata!=null){
	              for(ResourceFile rf: ((XnatAbstractdemographicdata)childAbstractdemographicdata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractDemographicData[" + ((XnatAbstractdemographicdata)childAbstractdemographicdata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractDemographicData/" + ((XnatAbstractdemographicdata)childAbstractdemographicdata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
