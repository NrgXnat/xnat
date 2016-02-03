/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:03 UTC 2016
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
public abstract class AutoWrkWorkflowdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.WrkWorkflowdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoWrkWorkflowdata.class);
	public static String SCHEMA_ELEMENT_NAME="wrk:workflowData";

	public AutoWrkWorkflowdata(ItemI item)
	{
		super(item);
	}

	public AutoWrkWorkflowdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoWrkWorkflowdata(UserI user)
	 **/
	public AutoWrkWorkflowdata(){}

	public AutoWrkWorkflowdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "wrk:workflowData";
	}
	 private org.nrg.xdat.om.WrkAbstractexecutionenvironment _Executionenvironment =null;

	/**
	 * executionEnvironment
	 * @return org.nrg.xdat.om.WrkAbstractexecutionenvironment
	 */
	public org.nrg.xdat.om.WrkAbstractexecutionenvironment getExecutionenvironment() {
		try{
			if (_Executionenvironment==null){
				_Executionenvironment=((WrkAbstractexecutionenvironment)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("executionEnvironment")));
				return _Executionenvironment;
			}else {
				return _Executionenvironment;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for executionEnvironment.
	 * @param v Value to Set.
	 */
	public void setExecutionenvironment(ItemI v) throws Exception{
		_Executionenvironment =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/executionEnvironment",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/executionEnvironment",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * executionEnvironment
	 * set org.nrg.xdat.model.WrkAbstractexecutionenvironmentI
	 */
	public <A extends org.nrg.xdat.model.WrkAbstractexecutionenvironmentI> void setExecutionenvironment(A item) throws Exception{
	setExecutionenvironment((ItemI)item);
	}

	/**
	 * Removes the executionEnvironment.
	 * */
	public void removeExecutionenvironment() {
		_Executionenvironment =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/executionEnvironment",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ExecutionenvironmentFK=null;

	/**
	 * @return Returns the wrk:workflowData/executionenvironment_wrk_abstractexecutionenvironment_id.
	 */
	public Integer getExecutionenvironmentFK(){
		try{
			if (_ExecutionenvironmentFK==null){
				_ExecutionenvironmentFK=getIntegerProperty("wrk:workflowData/executionenvironment_wrk_abstractexecutionenvironment_id");
				return _ExecutionenvironmentFK;
			}else {
				return _ExecutionenvironmentFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrk:workflowData/executionenvironment_wrk_abstractexecutionenvironment_id.
	 * @param v Value to Set.
	 */
	public void setExecutionenvironmentFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/executionenvironment_wrk_abstractexecutionenvironment_id",v);
		_ExecutionenvironmentFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Comments=null;

	/**
	 * @return Returns the comments.
	 */
	public String getComments(){
		try{
			if (_Comments==null){
				_Comments=getStringProperty("comments");
				return _Comments;
			}else {
				return _Comments;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for comments.
	 * @param v Value to Set.
	 */
	public void setComments(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/comments",v);
		_Comments=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Details=null;

	/**
	 * @return Returns the details.
	 */
	public String getDetails(){
		try{
			if (_Details==null){
				_Details=getStringProperty("details");
				return _Details;
			}else {
				return _Details;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for details.
	 * @param v Value to Set.
	 */
	public void setDetails(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/details",v);
		_Details=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Justification=null;

	/**
	 * @return Returns the justification.
	 */
	public String getJustification(){
		try{
			if (_Justification==null){
				_Justification=getStringProperty("justification");
				return _Justification;
			}else {
				return _Justification;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for justification.
	 * @param v Value to Set.
	 */
	public void setJustification(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/justification",v);
		_Justification=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
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

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		try{
			if (_Type==null){
				_Type=getStringProperty("type");
				return _Type;
			}else {
				return _Type;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/type",v);
		_Type=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Category=null;

	/**
	 * @return Returns the category.
	 */
	public String getCategory(){
		try{
			if (_Category==null){
				_Category=getStringProperty("category");
				return _Category;
			}else {
				return _Category;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for category.
	 * @param v Value to Set.
	 */
	public void setCategory(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/category",v);
		_Category=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _DataType=null;

	/**
	 * @return Returns the data_type.
	 */
	public String getDataType(){
		try{
			if (_DataType==null){
				_DataType=getStringProperty("data_type");
				return _DataType;
			}else {
				return _DataType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for data_type.
	 * @param v Value to Set.
	 */
	public void setDataType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/data_type",v);
		_DataType=null;
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

	private String _Externalid=null;

	/**
	 * @return Returns the ExternalID.
	 */
	public String getExternalid(){
		try{
			if (_Externalid==null){
				_Externalid=getStringProperty("ExternalID");
				return _Externalid;
			}else {
				return _Externalid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ExternalID.
	 * @param v Value to Set.
	 */
	public void setExternalid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ExternalID",v);
		_Externalid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _CurrentStepLaunchTime=null;

	/**
	 * @return Returns the current_step_launch_time.
	 */
	public Object getCurrentStepLaunchTime(){
		try{
			if (_CurrentStepLaunchTime==null){
				_CurrentStepLaunchTime=getProperty("current_step_launch_time");
				return _CurrentStepLaunchTime;
			}else {
				return _CurrentStepLaunchTime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for current_step_launch_time.
	 * @param v Value to Set.
	 */
	public void setCurrentStepLaunchTime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/current_step_launch_time",v);
		_CurrentStepLaunchTime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _CurrentStepId=null;

	/**
	 * @return Returns the current_step_id.
	 */
	public String getCurrentStepId(){
		try{
			if (_CurrentStepId==null){
				_CurrentStepId=getStringProperty("current_step_id");
				return _CurrentStepId;
			}else {
				return _CurrentStepId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for current_step_id.
	 * @param v Value to Set.
	 */
	public void setCurrentStepId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/current_step_id",v);
		_CurrentStepId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Status=null;

	/**
	 * @return Returns the status.
	 */
	public String getStatus(){
		try{
			if (_Status==null){
				_Status=getStringProperty("status");
				return _Status;
			}else {
				return _Status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for status.
	 * @param v Value to Set.
	 */
	public void setStatus(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/status",v);
		_Status=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _CreateUser=null;

	/**
	 * @return Returns the create_user.
	 */
	public String getCreateUser(){
		try{
			if (_CreateUser==null){
				_CreateUser=getStringProperty("create_user");
				return _CreateUser;
			}else {
				return _CreateUser;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for create_user.
	 * @param v Value to Set.
	 */
	public void setCreateUser(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/create_user",v);
		_CreateUser=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _PipelineName=null;

	/**
	 * @return Returns the pipeline_name.
	 */
	public String getPipelineName(){
		try{
			if (_PipelineName==null){
				_PipelineName=getStringProperty("pipeline_name");
				return _PipelineName;
			}else {
				return _PipelineName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipeline_name.
	 * @param v Value to Set.
	 */
	public void setPipelineName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipeline_name",v);
		_PipelineName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _NextStepId=null;

	/**
	 * @return Returns the next_step_id.
	 */
	public String getNextStepId(){
		try{
			if (_NextStepId==null){
				_NextStepId=getStringProperty("next_step_id");
				return _NextStepId;
			}else {
				return _NextStepId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for next_step_id.
	 * @param v Value to Set.
	 */
	public void setNextStepId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/next_step_id",v);
		_NextStepId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _StepDescription=null;

	/**
	 * @return Returns the step_description.
	 */
	public String getStepDescription(){
		try{
			if (_StepDescription==null){
				_StepDescription=getStringProperty("step_description");
				return _StepDescription;
			}else {
				return _StepDescription;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for step_description.
	 * @param v Value to Set.
	 */
	public void setStepDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/step_description",v);
		_StepDescription=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _LaunchTime=null;

	/**
	 * @return Returns the launch_time.
	 */
	public Object getLaunchTime(){
		try{
			if (_LaunchTime==null){
				_LaunchTime=getProperty("launch_time");
				return _LaunchTime;
			}else {
				return _LaunchTime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for launch_time.
	 * @param v Value to Set.
	 */
	public void setLaunchTime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/launch_time",v);
		_LaunchTime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Percentagecomplete=null;

	/**
	 * @return Returns the percentageComplete.
	 */
	public String getPercentagecomplete(){
		try{
			if (_Percentagecomplete==null){
				_Percentagecomplete=getStringProperty("percentageComplete");
				return _Percentagecomplete;
			}else {
				return _Percentagecomplete;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for percentageComplete.
	 * @param v Value to Set.
	 */
	public void setPercentagecomplete(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/percentageComplete",v);
		_Percentagecomplete=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Jobid=null;

	/**
	 * @return Returns the jobID.
	 */
	public String getJobid(){
		try{
			if (_Jobid==null){
				_Jobid=getStringProperty("jobID");
				return _Jobid;
			}else {
				return _Jobid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for jobID.
	 * @param v Value to Set.
	 */
	public void setJobid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/jobID",v);
		_Jobid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _WrkWorkflowdataId=null;

	/**
	 * @return Returns the wrk_workflowData_id.
	 */
	public Integer getWrkWorkflowdataId() {
		try{
			if (_WrkWorkflowdataId==null){
				_WrkWorkflowdataId=getIntegerProperty("wrk_workflowData_id");
				return _WrkWorkflowdataId;
			}else {
				return _WrkWorkflowdataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrk_workflowData_id.
	 * @param v Value to Set.
	 */
	public void setWrkWorkflowdataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/wrk_workflowData_id",v);
		_WrkWorkflowdataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.WrkWorkflowdata> getAllWrkWorkflowdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkWorkflowdata> al = new ArrayList<org.nrg.xdat.om.WrkWorkflowdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkWorkflowdata> getWrkWorkflowdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkWorkflowdata> al = new ArrayList<org.nrg.xdat.om.WrkWorkflowdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkWorkflowdata> getWrkWorkflowdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkWorkflowdata> al = new ArrayList<org.nrg.xdat.om.WrkWorkflowdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static WrkWorkflowdata getWrkWorkflowdatasByWrkWorkflowdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("wrk:workflowData/wrk_workflowData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (WrkWorkflowdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //executionEnvironment
	        WrkAbstractexecutionenvironment childExecutionenvironment = (WrkAbstractexecutionenvironment)this.getExecutionenvironment();
	            if (childExecutionenvironment!=null){
	              for(ResourceFile rf: ((WrkAbstractexecutionenvironment)childExecutionenvironment).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("executionEnvironment[" + ((WrkAbstractexecutionenvironment)childExecutionenvironment).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("executionEnvironment/" + ((WrkAbstractexecutionenvironment)childExecutionenvironment).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
