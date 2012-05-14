//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.event.persist;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.axis.utils.StringUtils;
import org.nrg.xdat.security.XDATUser;
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
	
	public static PersistentWorkflowI getWorkflowByEventId(final XDATUser user,final Integer id){
		return  getWorkflowBuilder(user).getWorkflowByEventId(user,id);
	}
	
	public static Collection<? extends PersistentWorkflowI> getOpenWorkflows(final XDATUser user,final String ID){		
		return getWorkflowBuilder(user).getOpenWorkflows(user,ID);
	}
	
	public static Collection<? extends PersistentWorkflowI> getWorkflows(final XDATUser user,final String ID){		
		return getWorkflowBuilder(user).getWorkflows(user,ID);
	}
	
	public static Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(final XDATUser user,final String ID){		
		return getWorkflowBuilder(user).getWorkflowsByExternalId(user,ID);
	}
	
	public static Collection<? extends PersistentWorkflowI> getWorkflows(final XDATUser user,final List<String> IDs){		
		return getWorkflowBuilder(user).getWorkflows(user,IDs);
	}
	
	private static PersistentWorkflowI buildOpenWorkflow(final XDATUser user, final String xsiType,final String ID,final String project_id) throws FieldNotFoundException, IDAbsent{
		PersistentWorkflowI workflow = getWorkflowBuilder(user).getPersistentWorkflowI(user);
		workflow.setDataType(xsiType);
		workflow.setExternalid(project_id);
		workflow.setId(ID);
		if(StringUtils.isEmpty(workflow.getId())){
			throw new IDAbsent();
		}
		workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
		workflow.setLaunchTime(Calendar.getInstance().getTime());

		return workflow;
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
	
	public static PersistentWorkflowI buildOpenWorkflow(final XDATUser user, final String xsiType,final String ID,final String project_id,final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		try {
			PersistentWorkflowI workflow = buildOpenWorkflow(user, xsiType, ID, project_id);

			workflow.setType(event.getType());
			workflow.setComments(event.getComment());
			workflow.setCategory(event.getCategory());
			
			if( (XFT.REQUIRE_REASON && XFT.SHOW_REASON) && StringUtils.isEmpty(event.getReason()) && requiresReason(xsiType, project_id)){
				throw new JustificationAbsent();
			}
			workflow.setJustification(event.getReason());
			if(StringUtils.isEmpty(event.getAction())){
				if(XFT.REQUIRE_EVENT_NAME){
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
	
	public static PersistentWorkflowI buildOpenWorkflow(final XDATUser user, final XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		try {
			return buildOpenWorkflow(user, expt.getXSIType(), expt.getStringProperty("ID"), expt.getStringProperty("project"),event);
		} catch (FieldNotFoundException e) {
			return null;
		} catch (XFTInitException e) {
			return null;
		} catch (ElementNotFoundException e) {
			return null;
		}
	}
	
	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, XDATUser user,XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}
		
		try {
			if(ci==null){
				ci=buildOpenWorkflow(user, expt.getXSIType(), expt.getPKValueString(), (expt.getStringProperty("project")!=null)?expt.getStringProperty("project"):"ADMIN",event);
			}

		}catch (FieldNotFoundException e) {
			return null;
		} catch (XFTInitException e) {
			return null;
		} catch (ElementNotFoundException e) {
			return null;
		}
		
		return ci;
	}
	
	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, XDATUser user, String xsiType, String id, String project, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}
		
		if(ci==null){
			ci=buildOpenWorkflow(user, xsiType,id,project,event);
		}
		
		return ci;
	}

	public static void complete(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		wrk.setStatus(COMPLETE);
		save(wrk,c);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c) throws Exception{
		wrk.save((c==null)?null:c.getUser(), false, false, c);
	}

	public static EventMetaI setStep(PersistentWorkflowI wrk, String s){
			wrk.setStepDescription(s);
			return wrk.buildEvent();
	}

	public static void fail(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		wrk.setStatus(FAILED);
		save(wrk,c);
	}

	public static final String FAILED = "Failed";
	public static final String COMPLETE = "Complete";
	public static final String IN_PROGRESS="In Progress";
	public static final String RUNNING="Running";
	public static final String QUEUED="Queued";

}
