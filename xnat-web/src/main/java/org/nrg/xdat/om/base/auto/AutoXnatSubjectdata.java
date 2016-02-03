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
public abstract class AutoXnatSubjectdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatSubjectdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectData";

	public AutoXnatSubjectdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectdata(UserI user)
	 **/
	public AutoXnatSubjectdata(){}

	public AutoXnatSubjectdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectData";
	}
	 private ArrayList<org.nrg.xdat.om.XnatProjectparticipant> _Sharing_share =null;

	/**
	 * sharing/share
	 * @return Returns an List of org.nrg.xdat.om.XnatProjectparticipant
	 */
	public <A extends org.nrg.xdat.model.XnatProjectparticipantI> List<A> getSharing_share() {
		try{
			if (_Sharing_share==null){
				_Sharing_share=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sharing/share"));
			}
			return (List<A>) _Sharing_share;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatProjectparticipant>();}
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
	 * Adds org.nrg.xdat.model.XnatProjectparticipantI
	 */
	public <A extends org.nrg.xdat.model.XnatProjectparticipantI> void addSharing_share(A item) throws Exception{
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
	 * @return Returns the xnat:subjectData/investigator_xnat_investigatordata_id.
	 */
	public Integer getInvestigatorFK(){
		try{
			if (_InvestigatorFK==null){
				_InvestigatorFK=getIntegerProperty("xnat:subjectData/investigator_xnat_investigatordata_id");
				return _InvestigatorFK;
			}else {
				return _InvestigatorFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:subjectData/investigator_xnat_investigatordata_id.
	 * @param v Value to Set.
	 */
	public void setInvestigatorFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/investigator_xnat_investigatordata_id",v);
		_InvestigatorFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatAbstractdemographicdata _Demographics =null;

	/**
	 * demographics
	 * @return org.nrg.xdat.om.XnatAbstractdemographicdata
	 */
	public org.nrg.xdat.om.XnatAbstractdemographicdata getDemographics() {
		try{
			if (_Demographics==null){
				_Demographics=((XnatAbstractdemographicdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("demographics")));
				return _Demographics;
			}else {
				return _Demographics;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for demographics.
	 * @param v Value to Set.
	 */
	public void setDemographics(ItemI v) throws Exception{
		_Demographics =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/demographics",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/demographics",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * demographics
	 * set org.nrg.xdat.model.XnatAbstractdemographicdataI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractdemographicdataI> void setDemographics(A item) throws Exception{
	setDemographics((ItemI)item);
	}

	/**
	 * Removes the demographics.
	 * */
	public void removeDemographics() {
		_Demographics =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/demographics",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _DemographicsFK=null;

	/**
	 * @return Returns the xnat:subjectData/demographics_xnat_abstractdemographicdata_id.
	 */
	public Integer getDemographicsFK(){
		try{
			if (_DemographicsFK==null){
				_DemographicsFK=getIntegerProperty("xnat:subjectData/demographics_xnat_abstractdemographicdata_id");
				return _DemographicsFK;
			}else {
				return _DemographicsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:subjectData/demographics_xnat_abstractdemographicdata_id.
	 * @param v Value to Set.
	 */
	public void setDemographicsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/demographics_xnat_abstractdemographicdata_id",v);
		_DemographicsFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatAbstractsubjectmetadata _Metadata =null;

	/**
	 * metadata
	 * @return org.nrg.xdat.om.XnatAbstractsubjectmetadata
	 */
	public org.nrg.xdat.om.XnatAbstractsubjectmetadata getMetadata() {
		try{
			if (_Metadata==null){
				_Metadata=((XnatAbstractsubjectmetadata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("metadata")));
				return _Metadata;
			}else {
				return _Metadata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for metadata.
	 * @param v Value to Set.
	 */
	public void setMetadata(ItemI v) throws Exception{
		_Metadata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/metadata",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/metadata",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * metadata
	 * set org.nrg.xdat.model.XnatAbstractsubjectmetadataI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractsubjectmetadataI> void setMetadata(A item) throws Exception{
	setMetadata((ItemI)item);
	}

	/**
	 * Removes the metadata.
	 * */
	public void removeMetadata() {
		_Metadata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/metadata",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _MetadataFK=null;

	/**
	 * @return Returns the xnat:subjectData/metadata_xnat_abstractsubjectmetadata_id.
	 */
	public Integer getMetadataFK(){
		try{
			if (_MetadataFK==null){
				_MetadataFK=getIntegerProperty("xnat:subjectData/metadata_xnat_abstractsubjectmetadata_id");
				return _MetadataFK;
			}else {
				return _MetadataFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:subjectData/metadata_xnat_abstractsubjectmetadata_id.
	 * @param v Value to Set.
	 */
	public void setMetadataFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/metadata_xnat_abstractsubjectmetadata_id",v);
		_MetadataFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> _Addid =null;

	/**
	 * addID
	 * @return Returns an List of org.nrg.xdat.om.XnatSubjectdataAddid
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectdataAddidI> List<A> getAddid() {
		try{
			if (_Addid==null){
				_Addid=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("addID"));
			}
			return (List<A>) _Addid;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid>();}
	}

	/**
	 * Sets the value for addID.
	 * @param v Value to Set.
	 */
	public void setAddid(ItemI v) throws Exception{
		_Addid =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/addID",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/addID",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * addID
	 * Adds org.nrg.xdat.model.XnatSubjectdataAddidI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectdataAddidI> void addAddid(A item) throws Exception{
	setAddid((ItemI)item);
	}

	/**
	 * Removes the addID of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAddid(int index) throws java.lang.IndexOutOfBoundsException {
		_Addid =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/addID",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatSubjectdataField> _Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an List of org.nrg.xdat.om.XnatSubjectdataField
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectdataFieldI> List<A> getFields_field() {
		try{
			if (_Fields_field==null){
				_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fields/field"));
			}
			return (List<A>) _Fields_field;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatSubjectdataField>();}
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
	 * Adds org.nrg.xdat.model.XnatSubjectdataFieldI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectdataFieldI> void addFields_field(A item) throws Exception{
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
	 private ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> _Experiments_experiment =null;

	/**
	 * experiments/experiment
	 * @return Returns an List of org.nrg.xdat.om.XnatSubjectassessordata
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectassessordataI> List<A> getExperiments_experiment() {
		try{
			if (_Experiments_experiment==null){
				_Experiments_experiment=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("experiments/experiment"));
			}
			return (List<A>) _Experiments_experiment;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatSubjectassessordata>();}
	}

	/**
	 * Sets the value for experiments/experiment.
	 * @param v Value to Set.
	 */
	public void setExperiments_experiment(ItemI v) throws Exception{
		_Experiments_experiment =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experiments/experiment",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experiments/experiment",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * experiments/experiment
	 * Adds org.nrg.xdat.model.XnatSubjectassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectassessordataI> void addExperiments_experiment(A item) throws Exception{
	setExperiments_experiment((ItemI)item);
	}

	/**
	 * Removes the experiments/experiment of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeExperiments_experiment(int index) throws java.lang.IndexOutOfBoundsException {
		_Experiments_experiment =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/experiments/experiment",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
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

	private String _Group=null;

	/**
	 * @return Returns the group.
	 */
	public String getGroup(){
		try{
			if (_Group==null){
				_Group=getStringProperty("group");
				return _Group;
			}else {
				return _Group;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for group.
	 * @param v Value to Set.
	 */
	public void setGroup(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/group",v);
		_Group=null;
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

	//FIELD

	private String _Src=null;

	/**
	 * @return Returns the src.
	 */
	public String getSrc(){
		try{
			if (_Src==null){
				_Src=getStringProperty("src");
				return _Src;
			}else {
				return _Src;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for src.
	 * @param v Value to Set.
	 */
	public void setSrc(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/src",v);
		_Src=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Initials=null;

	/**
	 * @return Returns the initials.
	 */
	public String getInitials(){
		try{
			if (_Initials==null){
				_Initials=getStringProperty("initials");
				return _Initials;
			}else {
				return _Initials;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for initials.
	 * @param v Value to Set.
	 */
	public void setInitials(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/initials",v);
		_Initials=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdata> getAllXnatSubjectdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdata> getXnatSubjectdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdata> getXnatSubjectdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectdata getXnatSubjectdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectData/ID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	        for(org.nrg.xdat.model.XnatProjectparticipantI childSharing_share : this.getSharing_share()){
	            if (childSharing_share!=null){
	              for(ResourceFile rf: ((XnatProjectparticipant)childSharing_share).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sharing/share[" + ((XnatProjectparticipant)childSharing_share).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sharing/share/" + ((XnatProjectparticipant)childSharing_share).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
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
	
	        //demographics
	        XnatAbstractdemographicdata childDemographics = (XnatAbstractdemographicdata)this.getDemographics();
	            if (childDemographics!=null){
	              for(ResourceFile rf: ((XnatAbstractdemographicdata)childDemographics).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("demographics[" + ((XnatAbstractdemographicdata)childDemographics).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("demographics/" + ((XnatAbstractdemographicdata)childDemographics).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //metadata
	        XnatAbstractsubjectmetadata childMetadata = (XnatAbstractsubjectmetadata)this.getMetadata();
	            if (childMetadata!=null){
	              for(ResourceFile rf: ((XnatAbstractsubjectmetadata)childMetadata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("metadata[" + ((XnatAbstractsubjectmetadata)childMetadata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("metadata/" + ((XnatAbstractsubjectmetadata)childMetadata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //addID
	        for(org.nrg.xdat.model.XnatSubjectdataAddidI childAddid : this.getAddid()){
	            if (childAddid!=null){
	              for(ResourceFile rf: ((XnatSubjectdataAddid)childAddid).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("addID[" + ((XnatSubjectdataAddid)childAddid).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("addID/" + ((XnatSubjectdataAddid)childAddid).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //fields/field
	        for(org.nrg.xdat.model.XnatSubjectdataFieldI childFields_field : this.getFields_field()){
	            if (childFields_field!=null){
	              for(ResourceFile rf: ((XnatSubjectdataField)childFields_field).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fields/field[" + ((XnatSubjectdataField)childFields_field).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fields/field/" + ((XnatSubjectdataField)childFields_field).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //experiments/experiment
	        for(org.nrg.xdat.model.XnatSubjectassessordataI childExperiments_experiment : this.getExperiments_experiment()){
	            if (childExperiments_experiment!=null){
	              for(ResourceFile rf: ((XnatSubjectassessordata)childExperiments_experiment).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("experiments/experiment[" + ((XnatSubjectassessordata)childExperiments_experiment).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("experiments/experiment/" + ((XnatSubjectassessordata)childExperiments_experiment).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
