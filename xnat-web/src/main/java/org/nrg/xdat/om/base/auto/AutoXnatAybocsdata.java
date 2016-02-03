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
public abstract class AutoXnatAybocsdata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatAybocsdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAybocsdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat_a:ybocsData";

	public AutoXnatAybocsdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatAybocsdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAybocsdata(UserI user)
	 **/
	public AutoXnatAybocsdata(){}

	public AutoXnatAybocsdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat_a:ybocsData";
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

	private String _Currentorworstever=null;

	/**
	 * @return Returns the currentOrWorstEver.
	 */
	public String getCurrentorworstever(){
		try{
			if (_Currentorworstever==null){
				_Currentorworstever=getStringProperty("currentOrWorstEver");
				return _Currentorworstever;
			}else {
				return _Currentorworstever;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for currentOrWorstEver.
	 * @param v Value to Set.
	 */
	public void setCurrentorworstever(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/currentOrWorstEver",v);
		_Currentorworstever=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Timeoccupiedwiththoughts=null;

	/**
	 * @return Returns the timeOccupiedWithThoughts.
	 */
	public Integer getTimeoccupiedwiththoughts() {
		try{
			if (_Timeoccupiedwiththoughts==null){
				_Timeoccupiedwiththoughts=getIntegerProperty("timeOccupiedWithThoughts");
				return _Timeoccupiedwiththoughts;
			}else {
				return _Timeoccupiedwiththoughts;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for timeOccupiedWithThoughts.
	 * @param v Value to Set.
	 */
	public void setTimeoccupiedwiththoughts(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/timeOccupiedWithThoughts",v);
		_Timeoccupiedwiththoughts=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Thoughtsinterferefunctioning=null;

	/**
	 * @return Returns the thoughtsInterfereFunctioning.
	 */
	public Integer getThoughtsinterferefunctioning() {
		try{
			if (_Thoughtsinterferefunctioning==null){
				_Thoughtsinterferefunctioning=getIntegerProperty("thoughtsInterfereFunctioning");
				return _Thoughtsinterferefunctioning;
			}else {
				return _Thoughtsinterferefunctioning;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for thoughtsInterfereFunctioning.
	 * @param v Value to Set.
	 */
	public void setThoughtsinterferefunctioning(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/thoughtsInterfereFunctioning",v);
		_Thoughtsinterferefunctioning=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Distresscaused=null;

	/**
	 * @return Returns the distressCaused.
	 */
	public Integer getDistresscaused() {
		try{
			if (_Distresscaused==null){
				_Distresscaused=getIntegerProperty("distressCaused");
				return _Distresscaused;
			}else {
				return _Distresscaused;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for distressCaused.
	 * @param v Value to Set.
	 */
	public void setDistresscaused(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/distressCaused",v);
		_Distresscaused=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Efforttoresistthoughts=null;

	/**
	 * @return Returns the effortToResistThoughts.
	 */
	public Integer getEfforttoresistthoughts() {
		try{
			if (_Efforttoresistthoughts==null){
				_Efforttoresistthoughts=getIntegerProperty("effortToResistThoughts");
				return _Efforttoresistthoughts;
			}else {
				return _Efforttoresistthoughts;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for effortToResistThoughts.
	 * @param v Value to Set.
	 */
	public void setEfforttoresistthoughts(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/effortToResistThoughts",v);
		_Efforttoresistthoughts=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Controloverthoughts=null;

	/**
	 * @return Returns the controlOverThoughts.
	 */
	public Integer getControloverthoughts() {
		try{
			if (_Controloverthoughts==null){
				_Controloverthoughts=getIntegerProperty("controlOverThoughts");
				return _Controloverthoughts;
			}else {
				return _Controloverthoughts;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for controlOverThoughts.
	 * @param v Value to Set.
	 */
	public void setControloverthoughts(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/controlOverThoughts",v);
		_Controloverthoughts=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Timeperforming=null;

	/**
	 * @return Returns the timePerforming.
	 */
	public Integer getTimeperforming() {
		try{
			if (_Timeperforming==null){
				_Timeperforming=getIntegerProperty("timePerforming");
				return _Timeperforming;
			}else {
				return _Timeperforming;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for timePerforming.
	 * @param v Value to Set.
	 */
	public void setTimeperforming(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/timePerforming",v);
		_Timeperforming=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Behaviorsinterferefunctioning=null;

	/**
	 * @return Returns the behaviorsInterfereFunctioning.
	 */
	public Integer getBehaviorsinterferefunctioning() {
		try{
			if (_Behaviorsinterferefunctioning==null){
				_Behaviorsinterferefunctioning=getIntegerProperty("behaviorsInterfereFunctioning");
				return _Behaviorsinterferefunctioning;
			}else {
				return _Behaviorsinterferefunctioning;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for behaviorsInterfereFunctioning.
	 * @param v Value to Set.
	 */
	public void setBehaviorsinterferefunctioning(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/behaviorsInterfereFunctioning",v);
		_Behaviorsinterferefunctioning=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Feelingifprevented=null;

	/**
	 * @return Returns the feelingIfPrevented.
	 */
	public Integer getFeelingifprevented() {
		try{
			if (_Feelingifprevented==null){
				_Feelingifprevented=getIntegerProperty("feelingIfPrevented");
				return _Feelingifprevented;
			}else {
				return _Feelingifprevented;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for feelingIfPrevented.
	 * @param v Value to Set.
	 */
	public void setFeelingifprevented(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/feelingIfPrevented",v);
		_Feelingifprevented=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Efforttoresistbehaviors=null;

	/**
	 * @return Returns the effortToResistBehaviors.
	 */
	public Integer getEfforttoresistbehaviors() {
		try{
			if (_Efforttoresistbehaviors==null){
				_Efforttoresistbehaviors=getIntegerProperty("effortToResistBehaviors");
				return _Efforttoresistbehaviors;
			}else {
				return _Efforttoresistbehaviors;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for effortToResistBehaviors.
	 * @param v Value to Set.
	 */
	public void setEfforttoresistbehaviors(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/effortToResistBehaviors",v);
		_Efforttoresistbehaviors=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Behaviordrivestrength=null;

	/**
	 * @return Returns the behaviorDriveStrength.
	 */
	public Integer getBehaviordrivestrength() {
		try{
			if (_Behaviordrivestrength==null){
				_Behaviordrivestrength=getIntegerProperty("behaviorDriveStrength");
				return _Behaviordrivestrength;
			}else {
				return _Behaviordrivestrength;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for behaviorDriveStrength.
	 * @param v Value to Set.
	 */
	public void setBehaviordrivestrength(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/behaviorDriveStrength",v);
		_Behaviordrivestrength=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Untiljustright=null;

	/**
	 * @return Returns the untilJustRight.
	 */
	public Boolean getUntiljustright() {
		try{
			if (_Untiljustright==null){
				_Untiljustright=getBooleanProperty("untilJustRight");
				return _Untiljustright;
			}else {
				return _Untiljustright;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for untilJustRight.
	 * @param v Value to Set.
	 */
	public void setUntiljustright(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/untilJustRight",v);
		_Untiljustright=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Untiljustrightawareness=null;

	/**
	 * @return Returns the untilJustRightAwareness.
	 */
	public String getUntiljustrightawareness(){
		try{
			if (_Untiljustrightawareness==null){
				_Untiljustrightawareness=getStringProperty("untilJustRightAwareness");
				return _Untiljustrightawareness;
			}else {
				return _Untiljustrightawareness;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for untilJustRightAwareness.
	 * @param v Value to Set.
	 */
	public void setUntiljustrightawareness(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/untilJustRightAwareness",v);
		_Untiljustrightawareness=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Untiljustrightperceptions=null;

	/**
	 * @return Returns the untilJustRightPerceptions.
	 */
	public String getUntiljustrightperceptions(){
		try{
			if (_Untiljustrightperceptions==null){
				_Untiljustrightperceptions=getStringProperty("untilJustRightPerceptions");
				return _Untiljustrightperceptions;
			}else {
				return _Untiljustrightperceptions;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for untilJustRightPerceptions.
	 * @param v Value to Set.
	 */
	public void setUntiljustrightperceptions(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/untilJustRightPerceptions",v);
		_Untiljustrightperceptions=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Whenstartuntiljustright=null;

	/**
	 * @return Returns the whenStartUntilJustRight.
	 */
	public String getWhenstartuntiljustright(){
		try{
			if (_Whenstartuntiljustright==null){
				_Whenstartuntiljustright=getStringProperty("whenStartUntilJustRight");
				return _Whenstartuntiljustright;
			}else {
				return _Whenstartuntiljustright;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for whenStartUntilJustRight.
	 * @param v Value to Set.
	 */
	public void setWhenstartuntiljustright(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/whenStartUntilJustRight",v);
		_Whenstartuntiljustright=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Frequencyuntiljustright=null;

	/**
	 * @return Returns the frequencyUntilJustRight.
	 */
	public String getFrequencyuntiljustright(){
		try{
			if (_Frequencyuntiljustright==null){
				_Frequencyuntiljustright=getStringProperty("frequencyUntilJustRight");
				return _Frequencyuntiljustright;
			}else {
				return _Frequencyuntiljustright;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for frequencyUntilJustRight.
	 * @param v Value to Set.
	 */
	public void setFrequencyuntiljustright(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/frequencyUntilJustRight",v);
		_Frequencyuntiljustright=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Firstuntiljustrightage=null;

	/**
	 * @return Returns the firstUntilJustRightAge.
	 */
	public Double getFirstuntiljustrightage() {
		try{
			if (_Firstuntiljustrightage==null){
				_Firstuntiljustrightage=getDoubleProperty("firstUntilJustRightAge");
				return _Firstuntiljustrightage;
			}else {
				return _Firstuntiljustrightage;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for firstUntilJustRightAge.
	 * @param v Value to Set.
	 */
	public void setFirstuntiljustrightage(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/firstUntilJustRightAge",v);
		_Firstuntiljustrightage=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAybocsdata> getAllXnatAybocsdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAybocsdata> al = new ArrayList<org.nrg.xdat.om.XnatAybocsdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAybocsdata> getXnatAybocsdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAybocsdata> al = new ArrayList<org.nrg.xdat.om.XnatAybocsdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAybocsdata> getXnatAybocsdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAybocsdata> al = new ArrayList<org.nrg.xdat.om.XnatAybocsdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAybocsdata getXnatAybocsdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat_a:ybocsData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAybocsdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
