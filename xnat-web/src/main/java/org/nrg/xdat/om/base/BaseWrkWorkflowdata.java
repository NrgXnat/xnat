/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseWrkWorkflowdata
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.om.base;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.WrkAbstractexecutionenvironmentI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.WrkXnatexecutionenvironment;
import org.nrg.xdat.om.base.auto.AutoWrkWorkflowdata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils.CATEGORY;
import org.nrg.xft.event.EventUtils.TYPE;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.restlet.resources.ScriptTriggerTemplateResource;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseWrkWorkflowdata extends AutoWrkWorkflowdata implements PersistentWorkflowI{

    @Serial
    private static final long serialVersionUID = 1;

	public static final String AWAITING_ACTION = "AWAITING ACTION";
	public static final String FAILED = "FAILED";
	public static final String RUNNING = "RUNNING";
	public static final String COMPLETE = "COMPLETE";
	public static final String ERROR = "ERROR";
	public static final String QUEUED = "QUEUED";
	public static final String FAILED_DISMISSED = "FAILED (DISMISSED)";
	
	private static final Logger _log = LoggerFactory.getLogger(BaseWrkWorkflowdata.class);

	public BaseWrkWorkflowdata(ItemI item)
	{
		super(item);
	}

	public BaseWrkWorkflowdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseWrkWorkflowdata(UserI user)
	 **/
	public BaseWrkWorkflowdata()
	{}

	public BaseWrkWorkflowdata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


	public boolean isActive(){
		return !(isComplete() || isFailed() || this.getStatus().equalsIgnoreCase(FAILED_DISMISSED));
	}

	public boolean isComplete() {
		return this.getStatus().equalsIgnoreCase(COMPLETE);
	}

	public boolean isFailed() {
		return this.getStatus().equalsIgnoreCase(ERROR) ||
				(this.getStatus().toUpperCase().startsWith(FAILED) && !this.getStatus().equalsIgnoreCase(FAILED_DISMISSED));
	}
	
	public boolean isFinished(){
		if(this.getStatus().equalsIgnoreCase(COMPLETE))
			return true;
		if(this.getStatus().equalsIgnoreCase(FAILED_DISMISSED))
			return true;

		return false;
	}
	
	public static ArrayList getWrkWorkflowdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad, String sortField, String sortOrder)
	{
		final ArrayList al = new ArrayList();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			Iterator iter;
			if (sortField != null && sortOrder != null) {
				iter = items.getItems(sortField,sortOrder).iterator();
			}else {
				iter = items.getItemIterator();
			}
			while (iter.hasNext())
			{
				final WrkWorkflowdata vrc = new WrkWorkflowdata((XFTItem)iter.next());
				al.add(vrc);
			}
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public String getOnlyPipelineName() {
		String rtn = getPipelineName();
		// prevent NPE
		if (rtn == null) {
			return null;
		}
		if (rtn.endsWith(File.separator)) rtn = rtn.substring(0,rtn.length());
		int lastIndexOfSlash = rtn.lastIndexOf(File.separator);
		if (lastIndexOfSlash != -1) {
			rtn = rtn.substring(lastIndexOfSlash + 1);
		}else {
		   lastIndexOfSlash = rtn.lastIndexOf("/");
		   if (lastIndexOfSlash != -1) 
			   rtn = rtn.substring(lastIndexOfSlash + 1);
		}
		int lastIndexOfDot = rtn.lastIndexOf(".");
		if (lastIndexOfDot != -1 ) {
			rtn = rtn.substring(0,lastIndexOfDot);
		}
		return rtn;
	}


	/**
	 * Constructs the XnatPipelineLauncher object for the most recent pipeline entry
	 *
	 * @param user The user who needs to relaunch the pipeline
	 *
	 * @return XnatPipelineLauncher to relaunch the pipeline or null if the pipeline is not waiting
	 */
	public  XnatPipelineLauncher getLatestLauncherByStatus(UserI user) {
	   XnatPipelineLauncher rtn = null;
	   //Look for the latest workflow entry for this pipeline
	   //If its status is matches then construct the workflow
	   final String _status = getStatus();
	   final WrkAbstractexecutionenvironmentI absExecutionEnv = getExecutionenvironment();
	   try {
		   final WrkXnatexecutionenvironment xnatExecutionEnv = (WrkXnatexecutionenvironment)absExecutionEnv;
		   if (_status.equalsIgnoreCase(AWAITING_ACTION)) {
			   rtn = xnatExecutionEnv.getLauncher(user, getNextStepId());
		   }else {
			   rtn = xnatExecutionEnv.getLauncher(user);
		   }
	   } catch(ClassCastException ignored) {}
	   return rtn;

	}

	/**
	 * Constructs the XnatPipelineLauncher object to be used to restart a FAILED pipeline
	 *
	 * @param user The user who needs to relaunch the pipeline
	 *
	 * @return XnatPipelineLauncher to relaunch the pipeline or null if the pipeline hasnt failed
	 */
	public XnatPipelineLauncher restartWorkflow(UserI user) {
		return getLatestLauncherByStatus(user);
	}

	/**
	 * Constructs the XnatPipelineLauncher object to be used to resume an awaiting pipeline
	 *
	 * @param user The user who needs to relaunch the pipeline
	 *
	 * @return XnatPipelineLauncher to relaunch the pipeline or null if the pipeline is not waiting
	 */
	public XnatPipelineLauncher resumeWorkflow(UserI user) {
	   return getLatestLauncherByStatus(user);
	}

	/**
	 * Returns the most recent workflow status
	 * @param id
	 * @param data_type
	 * @param external_id
	 * @param user
	 * @return
	 */

	public static String GetLatestWorkFlowStatus(String id, String data_type, String external_id, UserI user) {
		final ArrayList wrkFlows = GetWorkFlowsOrderByLaunchTimeDesc(id,data_type,external_id,null, user);
		String rtn = "";
		if (wrkFlows != null && wrkFlows.size() > 0) {
			rtn = ((WrkWorkflowdata)wrkFlows.getFirst()).getStatus();
		}
		return rtn;
	}

	/**
	 * Returns the most recent workflow status for a pipeline
	 * @param id
	 * @param data_type
	 * @param external_id
	 * @param user
	 * @return
	 */

	public static String GetLatestWorkFlowStatus(String id, String data_type, String external_id,String pipelineName,org.nrg.xft.security.UserI user) {
		final ArrayList wrkFlows = GetWorkFlowsOrderByLaunchTimeDesc(id,data_type,external_id,pipelineName,user);
		String rtn = "";
		if (wrkFlows != null && wrkFlows.size() > 0) {
			rtn = ((WrkWorkflowdata)wrkFlows.getFirst()).getStatus();
		}
		return rtn;
	}

	/**
	 * Returns the most recent workflow status for a pipeline
	 * @param id
	 * @param data_type
	 * @param external_id
	 * @param user
	 * @return
	 */

	public static String GetLatestWorkFlowStatusByPipeline(String id, String data_type, String pipelineName, String external_id,org.nrg.xft.security.UserI user) {
		final ArrayList wrkFlows = GetWorkFlowsOrderByLaunchTimeDesc(id,data_type,external_id, pipelineName,user);
		String rtn = "";
		if (wrkFlows != null && wrkFlows.size() > 0) {
			rtn = ((WrkWorkflowdata)wrkFlows.getFirst()).getStatus();
		}
		return rtn;
	}

	public static ArrayList GetWorkFlowsOrderByLaunchTimeDesc(String id, String dataType, String externalId, String pipelineName, org.nrg.xft.security.UserI user) {
		final ArrayList workflows = new ArrayList();
		org.nrg.xft.search.CriteriaCollection cc = new CriteriaCollection("AND");
		cc.addClause("wrk:workflowData.ID",id);
		cc.addClause("wrk:workflowData.data_type",dataType);
		if (externalId != null) cc.addClause("wrk:workflowData.ExternalID",externalId);
		if (pipelineName != null) cc.addClause("wrk:workflowData.pipeline_name",pipelineName);
		//Sort by Launch Time
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(cc,user,false);
			final ArrayList workitems = items.getItems("wrk:workflowData.launch_time","DESC");
			Iterator iter = workitems.iterator();
			while (iter.hasNext())
			{
				final WrkWorkflowdata vrc = new WrkWorkflowdata((XFTItem)iter.next());
				workflows.add(vrc);
			}
		}catch(Exception e) {
			logger.debug("",e);
		}
	   logger.info("Workflows by Ordered by Launch Time " + workflows.size());
		return workflows;
	}
	
	public synchronized EventMetaI buildEvent(){
		final Date d=Calendar.getInstance().getTime();
		return new WorkflowEvent(null, d, this.getUser(), this.getEventId(), FileUtils.getTimestamp(d));
	}
	
	public class WorkflowEvent implements EventMetaI, Serializable {
        @Serial
        private static final long serialVersionUID = 42L;
		final String message;
		final Date d;
		final UserI user;
		final Number id;
		final String timestamp;
		
		public WorkflowEvent(String message, Date d, UserI user, Number id,
				String timestamp) {
			super();
			this.message = message;
			this.d = d;
			this.user = user;
			this.id = id;
			this.timestamp = timestamp;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public Date getEventDate() {
			return d;
		}

		@Override
		public String getTimestamp() {
			return timestamp;
		}

		@Override
		public UserI getUser() {
			return user;
		}

		@Override
		public Number getEventId() {
			return id;
		}
		
	}

	public Number getEventId() {
		Number i= getWrkWorkflowdataId();
		if(i==null){
			try {
				i= getNextWorkflowID();
				setWrkWorkflowdataId(i.intValue());
			} catch (Exception e) {
				logger.error("",e);
			}
		}
		
		return i;
	}


	private static String __table=null;
	private static String __dbName=null;
	private static final String __pk="wrk_workflowData_id";
	private static String __sequence=null;
	private synchronized static Number getNextWorkflowID() throws Exception{
		if(__table==null){
			try {
				GenericWrapperElement element=GenericWrapperElement.GetElement(WrkWorkflowdata.SCHEMA_ELEMENT_NAME);
				__dbName=element.getDbName();
				__table=element.getSQLName();
				__sequence=element.getSequenceName();
			} catch (XFTInitException e) {
				logger.error("",e);
			} catch (ElementNotFoundException e) {
				logger.error("",e);
			}
		}
		return (Number)PoolDBUtils.GetNextID(__dbName, __table, __pk, __sequence);
	}

	@Override
	public Integer getWorkflowId() {
		return getWrkWorkflowdataId();
	}

	@Override
	public Date getLaunchTimeDate() {
		try {
			return getDateProperty("launch_time");
		} catch (Exception e) {
			logger.error("",e);
			return null;
		}
	}

	@Nullable
	public String getUsername(){
		UserI user = getWorkflowUser();
		return user == null ? null : user.getLogin();
	}

	@Nullable
	public Integer getUserId(){
		//XNAT-6568: No need to instanciate a new user object to get this most of the time.
		Integer i= null;
		try {
			i = (Integer)this.getItem().getMeta().getProperty("insert_user_xdat_user_id");
		} catch (XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		} catch (FieldNotFoundException e) {
			logger.error("",e);
		}
		if(i==null){
			i= this.getUser().getID();
		}
		return i;
	}

	@Nullable
	private UserI getWorkflowUser(){
		return ObjectUtils.firstNonNull(this.getInsertUser(), this.getUser());
	}

	@Override
	public void setType(TYPE v) {
		this.setType(v.toString());
	}

	@Override
	public void setCategory(CATEGORY v) {
		this.setCategory(v.toString());
	}

	@Override
	public Date getCurrentStepLaunchTimeDate() {
		try {
			return getDateProperty("current_step_launch_time");
		} catch (Exception e) {
			logger.error("",e);
			return null;
		}
	}


	private static Boolean extraSecurity=null;
	
	private static boolean requiresEnhancedSecurity(){
		if(extraSecurity==null){
			extraSecurity=XDAT.getBoolSiteConfigurationProperty("security.enhanced-workflow-security", false);
		}
		
		return extraSecurity;
	}
	
	private static List<String> fromError=Arrays.asList(FAILED,FAILED_DISMISSED,ERROR);
	private static List<String> locked=Arrays.asList(COMPLETE,FAILED_DISMISSED);
	
	@Override
	public void preSave() throws Exception {
		super.preSave();
		
		if(requiresEnhancedSecurity()){
			final XFTTable t;
			if(this.getWrkWorkflowdataId()!=null){
				t=XFTTable.Execute("SELECT UPPER(wrk.status), login, externalid, id FROM wrk_workflowdata wrk LEFT JOIN wrk_workflowData_meta_data meta ON wrk.workflowData_info=meta.meta_data_id LEFT JOIN xdat_user u ON meta.insert_user_xdat_user_id=u.xdat_user_id WHERE wrk_workflowdata_id=%1s;".formatted(this.getWrkWorkflowdataId()), null, null);
			}else if(this.getId()!=null && this.getPipelineName()!=null && this.getLaunchTime()!=null){
				t=XFTTable.Execute("SELECT UPPER(wrk.status), login, externalid, id FROM wrk_workflowdata wrk LEFT JOIN wrk_workflowData_meta_data meta ON wrk.workflowData_info=meta.meta_data_id LEFT JOIN xdat_user u ON meta.insert_user_xdat_user_id=u.xdat_user_id WHERE pipeline_name='%1s' AND id='%2s' AND launch_time='%3s';".formatted(this.getPipelineName(), this.getId(), (new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")).format(this.getLaunchTimeDate())), null, null);
			}else{
				throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"Missing required fields");
			}
			
			if(t!=null && t.size()>0){
				//modifying an existing entry
				String oldStatus=(String)t.rows().getFirst()[0];
				String insertUser=(String)t.rows().getFirst()[1];
				String externalid=(String)t.rows().getFirst()[2];
				String id=(String)t.rows().getFirst()[3];
				//Date launchTime=(Date)t.rows().get(0)[4];
				//String pipelineName=(String)t.rows().get(0)[5];
				
				//confirm they aren't trying to change key identifier fields
				if(StringUtils.isNotBlank(this.getExternalid()) && !StringUtils.equals(externalid, this.getExternalid())){
					throw new ClientException("Illegal modification of the External ID field.");
				}

				if(StringUtils.isNotBlank(this.getId()) && !StringUtils.equals(id, this.getId())){
					throw new ClientException("Illegal modification of the External ID field.");
				}
				
				if(locked.contains(oldStatus)){
					throw new ClientException(Status.CLIENT_ERROR_LOCKED,"Workflow entry is already " + oldStatus + " and cannot be modified.");
				}
				
				UserI currentUser=this.getUser();
				String newUser=currentUser.getUsername();
				
				final UserHelperServiceI userHelperService = UserHelper.getUserHelperService(currentUser);
				if (userHelperService == null) {
					_log.error("An error occurred trying to retrieve the user helper service. Can't proceed with permissions check.");
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to process your request. Please try again or, if the problem persists, contact your system administrator.");
				}
				
				//if is same user, or is site admin, or is owner
				if(StringUtils.equals(newUser, insertUser) || (Roles.isSiteAdmin(currentUser)) || ((StringUtils.isNotBlank(externalid))?userHelperService.isOwner(externalid):false)){
					String newStatus=StringUtils.upperCase(this.getStatus());
					if(StringUtils.isNotBlank(newStatus) && !StringUtils.equals(newStatus, oldStatus)){
						if(StringUtils.equals(ERROR, oldStatus)){
							//error entries can only become Failed or Failed (Dismissed)
							if(!fromError.contains(newStatus)){
								throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"Cannot modify workflow status from "+ oldStatus + " to "+ newStatus);
							}
						}else if(StringUtils.equals(FAILED, oldStatus)){
							//failed entries can only become Failed (Dismissed)
							if(!StringUtils.equals(FAILED_DISMISSED,newStatus)){
								throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST,"Cannot modify workflow status from "+ oldStatus + " to "+ newStatus);
							}
						}
					}
				}else{
					//entries can only be modified by the user who created it, or site administrators
					throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN,"User " + newUser + " is forbidden from motifying this workflow entry.");
				}
			}
		}
	}
	/*
	 * This method is called anytime a workflow entry is saved to the database.  
	 */
	@Override
	public void postSave() throws Exception {
		postSave(true);
	}
	
	@Override
	public void postSave(boolean triggerEvent) throws Exception {
		super.postSave();
		
		if(getStatus()!=null){			
			//status changed
			if(getWorkflowId()!=null && triggerEvent){
				XDAT.triggerEvent(WorkflowStatusEvent.class.getName() + getStatus(), new WorkflowStatusEvent(this));
			}
		}
	}
}
