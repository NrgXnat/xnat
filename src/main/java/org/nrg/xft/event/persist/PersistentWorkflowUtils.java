/*
 * core: org.nrg.xft.event.persist.PersistentWorkflowUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.persist;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

public class PersistentWorkflowUtils {
	public static final String ADMIN_EXTERNAL_ID = "ADMIN";

	final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PersistentWorkflowUtils.class);

	private static PersistentWorkflowBuilderAbst builder=null;
	public synchronized static PersistentWorkflowBuilderAbst getWorkflowBuilder(UserI user) {
		if(builder==null){
			try {
				Class<PersistentWorkflowBuilderAbst> c =(Class<PersistentWorkflowBuilderAbst>) Class.forName("org.nrg.xnat.utils.WorkflowUtils");
				builder=(PersistentWorkflowBuilderAbst)c.newInstance();
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			}
		}
		return builder;
	}

	public static PersistentWorkflowI getWorkflowByEventId(final UserI user,final Integer id){
		return  getWorkflowBuilder(user).getWorkflowByEventId(user,id);
	}

	public static Collection<? extends PersistentWorkflowI> getOpenWorkflows(final UserI user,final String ID){
		return getWorkflowBuilder(user).getOpenWorkflows(user,ID);
	}

	public static Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user,final String ID){
		return getWorkflowBuilder(user).getWorkflows(user,ID);
	}

	public static Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(final UserI user,final String ID){
		return getWorkflowBuilder(user).getWorkflowsByExternalId(user,ID);
	}

	public static Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user,final List<String> IDs){
		return getWorkflowBuilder(user).getWorkflows(user,IDs);
	}

	private static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID,final String project_id) throws FieldNotFoundException, IDAbsent{
		PersistentWorkflowI workflow = getWorkflowBuilder(user).getPersistentWorkflowI(user);
		workflow.setDataType(xsiType);
		workflow.setExternalid(project_id);
		workflow.setId(ID);
		if(StringUtils.isEmpty(workflow.getId())){
			workflow.setId("NULL");
		}
		workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
		workflow.setLaunchTime(Calendar.getInstance().getTime());

		return workflow;
	}

	public static void confirmID(final XFTItem item, final PersistentWorkflowI wrk){
		if(wrk.getId()==null || wrk.getId().equals("NULL")){
			wrk.setId(getID(item));
		}
	}

	public static String getID(final XFTItem item){
		String id=item.getPKValueString();
		if(StringUtils.isEmpty(id)){
			return "NULL";
		}else{
			return id;
		}
	}

	public static boolean requiresReason(final String xsiType,final String project_id){
		if(xsiType.startsWith("xnat") && !xsiType.equals("xnat:projectData")){
			return true;
		}else{
			return false;
		}
	}

	public static class EventRequirementAbsent extends Exception{
		public EventRequirementAbsent(String s){
			super(s);
		}
	}

	public static class ActionNameAbsent extends EventRequirementAbsent{
		public ActionNameAbsent(){
			super("Event name required.");
		}
	}

	public static class IDAbsent extends EventRequirementAbsent{
		public IDAbsent(){
			super("Event Object ID required.");
		}
	}

	public static class JustificationAbsent extends EventRequirementAbsent{
		public JustificationAbsent(){
			super("Justification required.");
		}
	}

	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID,final String project_id,final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		try {
			PersistentWorkflowI workflow = buildOpenWorkflow(user, xsiType, ID, project_id);

			workflow.setType(event.getType());
			workflow.setComments(event.getComment());
			workflow.setCategory(event.getCategory());

			if( (XDAT.getSiteConfigPreferences().getRequireChangeJustification() && XDAT.getSiteConfigPreferences().getShowChangeJustification()) && StringUtils.isEmpty(event.getReason()) && requiresReason(xsiType, project_id)){
				throw new JustificationAbsent();
			}
			workflow.setJustification(event.getReason());
			if(StringUtils.isEmpty(event.getAction())){
				if(XDAT.getSiteConfigPreferences().getRequireEventName()){
					throw new ActionNameAbsent();
				}else{
					workflow.setPipelineName(EventUtils.UNKNOWN);
				}
			}else{
				workflow.setPipelineName(event.getAction());
			}

			return workflow;
		} catch (FieldNotFoundException e) {
			return null;
		}
	}

	public static PersistentWorkflowI buildAdminWorkflow(final UserI user, final String xsiType, String id, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		return buildOpenWorkflow(user, xsiType, id, ADMIN_EXTERNAL_ID,event);
	}

	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		try {

			if(expt.getItem().instanceOf("xnat:experimentData") || expt.getItem().instanceOf("xnat:subjectData")){
				return buildOpenWorkflow(user, expt.getXSIType(), expt.getStringProperty("ID"), expt.getStringProperty("project"),event);
			}else{
				String id = expt.getPKValueString();
				if(StringUtils.isEmpty(id)){
					id = expt.getStringProperty("ID");
				}
				return buildOpenWorkflow(user, expt.getXSIType(), id, getExternalId(expt),event);
			}
		} catch (FieldNotFoundException e) {
			return null;
		} catch (XFTInitException e) {
			return null;
		} catch (ElementNotFoundException e) {
			return null;
		}
	}

	public static String getExternalId(ItemI expt){
		String externalID = ADMIN_EXTERNAL_ID;
		try {
			if(expt.getStringProperty("project")!=null){
				externalID = expt.getStringProperty("project");
			}
		} catch (Exception e) {
		}
		return externalID;
	}

	public static String getExternalId(UserI user){
		return ADMIN_EXTERNAL_ID;
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user,XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}

		if(ci==null){
			ci=buildOpenWorkflow(user, expt.getXSIType(), expt.getPKValueString(), getExternalId(expt),event);
		}

		return ci;
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user, String xsiType, String id, String project, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}

		if(ci==null){
			ci=buildOpenWorkflow(user, xsiType,id,project,event);
		}

		return ci;
	}

	public static void complete(PersistentWorkflowI wrk,EventMetaI c,boolean overrideSecurity) throws Exception{
		wrk.setStatus(COMPLETE);
		save(wrk,c,overrideSecurity);
	}


	public static void complete(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		wrk.setStatus(COMPLETE);
		save(wrk,c);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c) throws Exception{
		save(wrk,c,false);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c,boolean overrideSecurity) throws Exception{
		save(wrk,c,false,true);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c,boolean overrideSecurity,boolean triggerEvent) throws Exception{
		if(StringUtils.isEmpty(wrk.getId())){
			logger.error("Error saving audit trail entry for workflow item",new Exception());
			//set this to a value that is save-able... to prevent unnecessary failures.
			wrk.setId("ERROR");
		}
		if(!wrk.getDataType().equals("wrk:workflowData")){//prevent recursive events (events of events)
			wrk.save((c==null)?null:c.getUser(), overrideSecurity, false, c);
			wrk.postSave(triggerEvent);
		}
	}

	public static EventMetaI setStep(PersistentWorkflowI wrk, String s){
			wrk.setStepDescription(s);
			return wrk.buildEvent();
	}

	public static void fail(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		wrk.setStatus(FAILED);
		save(wrk,c);
	}

	public static void fail(PersistentWorkflowI wrk,EventMetaI c, boolean overrideSecurity) throws Exception{
		wrk.setStatus(FAILED);
		save(wrk,c,overrideSecurity);
	}

	public static final String FAILED = "Failed";
	public static final String COMPLETE = "Complete";
	public static final String IN_PROGRESS="In Progress";
	public static final String RUNNING="Running";
	public static final String QUEUED="Queued";

}
