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
public abstract class AutoXnatExperimentdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatExperimentdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatExperimentdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:experimentData";

	public AutoXnatExperimentdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatExperimentdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatExperimentdata(UserI user)
	 **/
	public AutoXnatExperimentdata(){}

	public AutoXnatExperimentdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:experimentData";
	}
	 private ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> _Sharing_share =null;

	/**
	 * sharing/share
	 * @return Returns an List of org.nrg.xdat.om.XnatExperimentdataShare
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataShareI> List<A> getSharing_share() {
		try{
			if (_Sharing_share==null){
				_Sharing_share=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sharing/share"));
			}
			return (List<A>) _Sharing_share;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatExperimentdataShare>();}
	}

	/**
	 * Sets the value for sharing/share.
	 * @param v Value to Set.
	 */
	public void setSharing_share(ItemI v) throws Exception{
		_Sharing_share =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sharing/share",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sharing/share",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * sharing/share
	 * Adds org.nrg.xdat.model.XnatExperimentdataShareI
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataShareI> void addSharing_share(A item) throws Exception{
	setSharing_share((ItemI)item);
	}

	/**
	 * Removes the sharing/share of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSharing_share(int index) throws java.lang.IndexOutOfBoundsException {
		_Sharing_share =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sharing/share",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Date=null;

	/**
	 * @return Returns the date.
	 */
	public Object getDate(){
		try{
			if (_Date==null){
				_Date=getProperty("date");
				return _Date;
			}else {
				return _Date;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/date",v);
		_Date=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Time=null;

	/**
	 * @return Returns the time.
	 */
	public Object getTime(){
		try{
			if (_Time==null){
				_Time=getProperty("time");
				return _Time;
			}else {
				return _Time;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for time.
	 * @param v Value to Set.
	 */
	public void setTime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/time",v);
		_Time=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Duration=null;

	/**
	 * @return Returns the duration.
	 */
	public Object getDuration(){
		try{
			if (_Duration==null){
				_Duration=getProperty("duration");
				return _Duration;
			}else {
				return _Duration;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for duration.
	 * @param v Value to Set.
	 */
	public void setDuration(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/duration",v);
		_Duration=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Delay=null;

	/**
	 * @return Returns the delay.
	 */
	public Integer getDelay() {
		try{
			if (_Delay==null){
				_Delay=getIntegerProperty("delay");
				return _Delay;
			}else {
				return _Delay;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for delay.
	 * @param v Value to Set.
	 */
	public void setDelay(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/delay",v);
		_Delay=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Delay_refExptId=null;

	/**
	 * @return Returns the delay/ref_expt_id.
	 */
	public String getDelay_refExptId(){
		try{
			if (_Delay_refExptId==null){
				_Delay_refExptId=getStringProperty("delay/ref_expt_id");
				return _Delay_refExptId;
			}else {
				return _Delay_refExptId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for delay/ref_expt_id.
	 * @param v Value to Set.
	 */
	public void setDelay_refExptId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/delay/ref_expt_id",v);
		_Delay_refExptId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Note=null;

	/**
	 * @return Returns the note.
	 */
	public String getNote(){
		try{
			if (_Note==null){
				_Note=getStringProperty("note");
				return _Note;
			}else {
				return _Note;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for note.
	 * @param v Value to Set.
	 */
	public void setNote(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/note",v);
		_Note=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatInvestigatordata _Investigator =null;

	/**
	 * investigator
	 * @return org.nrg.xdat.om.XnatInvestigatordata
	 */
	public org.nrg.xdat.om.XnatInvestigatordata getInvestigator() {
		try{
			if (_Investigator==null){
				_Investigator=((XnatInvestigatordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("investigator")));
				return _Investigator;
			}else {
				return _Investigator;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for investigator.
	 * @param v Value to Set.
	 */
	public void setInvestigator(ItemI v) throws Exception{
		_Investigator =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/investigator",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/investigator",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * investigator
	 * set org.nrg.xdat.model.XnatInvestigatordataI
	 */
	public <A extends org.nrg.xdat.model.XnatInvestigatordataI> void setInvestigator(A item) throws Exception{
	setInvestigator((ItemI)item);
	}

	/**
	 * Removes the investigator.
	 * */
	public void removeInvestigator() {
		_Investigator =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/investigator",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _InvestigatorFK=null;

	/**
	 * @return Returns the xnat:experimentData/investigator_xnat_investigatordata_id.
	 */
	public Integer getInvestigatorFK(){
		try{
			if (_InvestigatorFK==null){
				_InvestigatorFK=getIntegerProperty("xnat:experimentData/investigator_xnat_investigatordata_id");
				return _InvestigatorFK;
			}else {
				return _InvestigatorFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:experimentData/investigator_xnat_investigatordata_id.
	 * @param v Value to Set.
	 */
	public void setInvestigatorFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/investigator_xnat_investigatordata_id",v);
		_InvestigatorFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatValidationdata _Validation =null;

	/**
	 * validation
	 * @return org.nrg.xdat.om.XnatValidationdata
	 */
	public org.nrg.xdat.om.XnatValidationdata getValidation() {
		try{
			if (_Validation==null){
				_Validation=((XnatValidationdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("validation")));
				return _Validation;
			}else {
				return _Validation;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for validation.
	 * @param v Value to Set.
	 */
	public void setValidation(ItemI v) throws Exception{
		_Validation =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/validation",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/validation",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * validation
	 * set org.nrg.xdat.model.XnatValidationdataI
	 */
	public <A extends org.nrg.xdat.model.XnatValidationdataI> void setValidation(A item) throws Exception{
	setValidation((ItemI)item);
	}

	/**
	 * Removes the validation.
	 * */
	public void removeValidation() {
		_Validation =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/validation",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ValidationFK=null;

	/**
	 * @return Returns the xnat:experimentData/validation_xnat_validationdata_id.
	 */
	public Integer getValidationFK(){
		try{
			if (_ValidationFK==null){
				_ValidationFK=getIntegerProperty("xnat:experimentData/validation_xnat_validationdata_id");
				return _ValidationFK;
			}else {
				return _ValidationFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:experimentData/validation_xnat_validationdata_id.
	 * @param v Value to Set.
	 */
	public void setValidationFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/validation_xnat_validationdata_id",v);
		_ValidationFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresource> _Resources_resource =null;

	/**
	 * resources/resource
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresource
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> List<A> getResources_resource() {
		try{
			if (_Resources_resource==null){
				_Resources_resource=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("resources/resource"));
			}
			return (List<A>) _Resources_resource;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();}
	}

	/**
	 * Sets the value for resources/resource.
	 * @param v Value to Set.
	 */
	public void setResources_resource(ItemI v) throws Exception{
		_Resources_resource =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resources/resource",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resources/resource",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * resources/resource
	 * Adds org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void addResources_resource(A item) throws Exception{
	setResources_resource((ItemI)item);
	}

	/**
	 * Removes the resources/resource of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeResources_resource(int index) throws java.lang.IndexOutOfBoundsException {
		_Resources_resource =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/resources/resource",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatExperimentdataField> _Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an List of org.nrg.xdat.om.XnatExperimentdataField
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataFieldI> List<A> getFields_field() {
		try{
			if (_Fields_field==null){
				_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fields/field"));
			}
			return (List<A>) _Fields_field;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatExperimentdataField>();}
	}

	/**
	 * Sets the value for fields/field.
	 * @param v Value to Set.
	 */
	public void setFields_field(ItemI v) throws Exception{
		_Fields_field =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * fields/field
	 * Adds org.nrg.xdat.model.XnatExperimentdataFieldI
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataFieldI> void addFields_field(A item) throws Exception{
	setFields_field((ItemI)item);
	}

	/**
	 * Removes the fields/field of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeFields_field(int index) throws java.lang.IndexOutOfBoundsException {
		_Fields_field =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/fields/field",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _AcquisitionSite=null;

	/**
	 * @return Returns the acquisition_site.
	 */
	public String getAcquisitionSite(){
		try{
			if (_AcquisitionSite==null){
				_AcquisitionSite=getStringProperty("acquisition_site");
				return _AcquisitionSite;
			}else {
				return _AcquisitionSite;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for acquisition_site.
	 * @param v Value to Set.
	 */
	public void setAcquisitionSite(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/acquisition_site",v);
		_AcquisitionSite=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("ID");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ID",v);
		_Id=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Project=null;

	/**
	 * @return Returns the project.
	 */
	public String getProject(){
		try{
			if (_Project==null){
				_Project=getStringProperty("project");
				return _Project;
			}else {
				return _Project;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for project.
	 * @param v Value to Set.
	 */
	public void setProject(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/project",v);
		_Project=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _VisitId=null;

	/**
	 * @return Returns the visit_id.
	 */
	public String getVisitId(){
		try{
			if (_VisitId==null){
				_VisitId=getStringProperty("visit_id");
				return _VisitId;
			}else {
				return _VisitId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for visit_id.
	 * @param v Value to Set.
	 */
	public void setVisitId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visit_id",v);
		_VisitId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Visit=null;

	/**
	 * @return Returns the visit.
	 */
	public String getVisit(){
		try{
			if (_Visit==null){
				_Visit=getStringProperty("visit");
				return _Visit;
			}else {
				return _Visit;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for visit.
	 * @param v Value to Set.
	 */
	public void setVisit(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visit",v);
		_Visit=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Version=null;

	/**
	 * @return Returns the version.
	 */
	public Integer getVersion() {
		try{
			if (_Version==null){
				_Version=getIntegerProperty("version");
				return _Version;
			}else {
				return _Version;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for version.
	 * @param v Value to Set.
	 */
	public void setVersion(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/version",v);
		_Version=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Original=null;

	/**
	 * @return Returns the original.
	 */
	public String getOriginal(){
		try{
			if (_Original==null){
				_Original=getStringProperty("original");
				return _Original;
			}else {
				return _Original;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for original.
	 * @param v Value to Set.
	 */
	public void setOriginal(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/original",v);
		_Original=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Protocol=null;

	/**
	 * @return Returns the protocol.
	 */
	public String getProtocol(){
		try{
			if (_Protocol==null){
				_Protocol=getStringProperty("protocol");
				return _Protocol;
			}else {
				return _Protocol;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for protocol.
	 * @param v Value to Set.
	 */
	public void setProtocol(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/protocol",v);
		_Protocol=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Label=null;

	/**
	 * @return Returns the label.
	 */
	public String getLabel(){
		try{
			if (_Label==null){
				_Label=getStringProperty("label");
				return _Label;
			}else {
				return _Label;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for label.
	 * @param v Value to Set.
	 */
	public void setLabel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/label",v);
		_Label=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdata> getAllXnatExperimentdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdata> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdata> getXnatExperimentdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdata> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdata> getXnatExperimentdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdata> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatExperimentdata getXnatExperimentdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:experimentData/ID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatExperimentdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //sharing/share
	        for(org.nrg.xdat.model.XnatExperimentdataShareI childSharing_share : this.getSharing_share()){
	            if (childSharing_share!=null){
	              for(ResourceFile rf: ((XnatExperimentdataShare)childSharing_share).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sharing/share[" + ((XnatExperimentdataShare)childSharing_share).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sharing/share/" + ((XnatExperimentdataShare)childSharing_share).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //investigator
	        XnatInvestigatordata childInvestigator = (XnatInvestigatordata)this.getInvestigator();
	            if (childInvestigator!=null){
	              for(ResourceFile rf: ((XnatInvestigatordata)childInvestigator).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("investigator[" + ((XnatInvestigatordata)childInvestigator).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("investigator/" + ((XnatInvestigatordata)childInvestigator).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //validation
	        XnatValidationdata childValidation = (XnatValidationdata)this.getValidation();
	            if (childValidation!=null){
	              for(ResourceFile rf: ((XnatValidationdata)childValidation).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("validation[" + ((XnatValidationdata)childValidation).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("validation/" + ((XnatValidationdata)childValidation).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //resources/resource
	        for(org.nrg.xdat.model.XnatAbstractresourceI childResources_resource : this.getResources_resource()){
	            if (childResources_resource!=null){
	              int counterResources_resource=0;
	              for(java.io.File f: ((XnatAbstractresource)childResources_resource).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("resources/resource[xnat_abstractresource_id=" + ((XnatAbstractresource)childResources_resource).getXnatAbstractresourceId() + "]/file/" + counterResources_resource +"");
	                 rf.setXdatPath("resources/resource/" + ((XnatAbstractresource)childResources_resource).getXnatAbstractresourceId() + "/" + counterResources_resource++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //fields/field
	        for(org.nrg.xdat.model.XnatExperimentdataFieldI childFields_field : this.getFields_field()){
	            if (childFields_field!=null){
	              for(ResourceFile rf: ((XnatExperimentdataField)childFields_field).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fields/field[" + ((XnatExperimentdataField)childFields_field).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fields/field/" + ((XnatExperimentdataField)childFields_field).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
