/*
 * core: org.nrg.xft.event.persist.PersistentWorkflowUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.persist;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

@Slf4j
public class PersistentWorkflowUtils {
	public static final String ADMIN_EXTERNAL_ID = "ADMIN";
	public static final String FAILED            = "Failed";
	public static final String COMPLETE          = "Complete";
	public static final String IN_PROGRESS       = "In Progress";
	@SuppressWarnings("unused")
	public static final String RUNNING           = "Running";
	@SuppressWarnings("unused")
	public static final String QUEUED            = "Queued";

	public synchronized static PersistentWorkflowBuilderAbst getWorkflowBuilder(@SuppressWarnings("unused") UserI user) {
		if(builder==null){
			try {
				builder= Class.forName("org.nrg.xnat.utils.WorkflowUtils").asSubclass(PersistentWorkflowBuilderAbst.class).newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				log.error("", e);
			}
		}
		return builder;
	}

	public static PersistentWorkflowI getWorkflowByEventId(final UserI user,final Integer id){
		return  getWorkflowBuilder(user).getWorkflowByEventId(user,id);
	}

	@SuppressWarnings("unused")
	public static Collection<? extends PersistentWorkflowI> getOpenWorkflows(final UserI user, final String ID){
		return getWorkflowBuilder(user).getOpenWorkflows(user,ID);
	}

	@SuppressWarnings("unused")
	public static Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user, final String ID){
		return getWorkflowBuilder(user).getWorkflows(user,ID);
	}

	@SuppressWarnings("unused")
	public static Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(final UserI user, final String ID){
		return getWorkflowBuilder(user).getWorkflowsByExternalId(user,ID);
	}

	@SuppressWarnings("unused")
	public static Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user, final List<String> IDs){
		return getWorkflowBuilder(user).getWorkflows(user,IDs);
	}

	public static void confirmID(final XFTItem item, final PersistentWorkflowI wrk){
		if(wrk.getId()==null || wrk.getId().equals("NULL")){
			wrk.setId(getID(item));
		}
	}

	public static String getID(final XFTItem item){
		return StringUtils.defaultIfBlank(item.getPKValueString(), "NULL");
	}

	public static boolean requiresReason(final String xsiType, @SuppressWarnings("unused") final String projectId) {
		return xsiType.startsWith("xnat") && !xsiType.equals("xnat:projectData");
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

	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID,final String projectId,final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent {
		return buildOpenWorkflow(user, xsiType, ID, null, projectId, event);
	}

	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID, final String scanId, final String projectId,final EventDetails event) throws JustificationAbsent, ActionNameAbsent {
		if( (XDAT.getSiteConfigPreferences().getRequireChangeJustification() && XDAT.getSiteConfigPreferences().getShowChangeJustification()) && StringUtils.isBlank(event.getReason()) && requiresReason(xsiType, projectId)){
			throw new JustificationAbsent();
		}
		final boolean hasAction = StringUtils.isNotBlank(event.getAction());
		if(!hasAction && XDAT.getSiteConfigPreferences().getRequireEventName()) {
			throw new ActionNameAbsent();
		}
		try {
			final PersistentWorkflowI workflow = buildOpenWorkflow(user, xsiType, ID, scanId, projectId);
			workflow.setType(event.getType());
			workflow.setComments(event.getComment());
			workflow.setCategory(event.getCategory());
			workflow.setJustification(event.getReason());
			workflow.setPipelineName(hasAction ? event.getAction() : EventUtils.UNKNOWN);
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
			final String id;
			final String projectId;
			if (expt.getItem().instanceOf("xnat:experimentData") || expt.getItem().instanceOf("xnat:subjectData")) {
				id = expt.getStringProperty("ID");
				projectId = expt.getStringProperty("project");
			} else {
				id = StringUtils.defaultIfBlank(expt.getPKValueString(), expt.getStringProperty("ID"));
				projectId = getExternalId(expt);
			}
		   return buildOpenWorkflow(user, expt.getXSIType(), id, projectId, event);
		} catch (FieldNotFoundException e) {
			log.error("Field {} not found while building workflow for {}/ID={} for user {}", e.FIELD, expt.getXSIType(), expt.getPK(), user.getUsername(), e);
		} catch (XFTInitException e) {
			log.error("An error occurred trying to initialize XFT while building workflow for {}/ID={} for user {}", expt.getXSIType(), expt.getPK(), user.getUsername(), e);
		} catch (ElementNotFoundException e) {
			log.error("Element {} not found while building workflow for {}/ID={} for user {}", e.ELEMENT, expt.getXSIType(), expt.getPK(), user.getUsername(), e);
		}
		return null;
	}

	public static String getExternalId(ItemI expt){
		try {
			if (StringUtils.isNotBlank(expt.getStringProperty("project"))) {
				return expt.getStringProperty("project");
			}
		} catch (Exception ignored) {
		}
		return ADMIN_EXTERNAL_ID;
	}

	public static String getExternalId(@SuppressWarnings("unused") UserI user){
		return ADMIN_EXTERNAL_ID;
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user,XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent {
		return getOrCreateWorkflowData(eventId, user, expt, null, event);
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user, XFTItem expt, final String scanId, final EventDetails event) throws JustificationAbsent,ActionNameAbsent {
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}

		if(ci==null){
			ci=buildOpenWorkflow(user, expt.getXSIType(), expt.getPKValueString(), scanId, getExternalId(expt),event);
		}

		return ci;
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user, String xsiType, String id, String project, final EventDetails event) throws JustificationAbsent,ActionNameAbsent {
		return getOrCreateWorkflowData(eventId, user, xsiType, id, null, project, event);
	}

	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user, String xsiType, String id, String scanId, String project, final EventDetails event) throws JustificationAbsent,ActionNameAbsent {
		PersistentWorkflowI ci=null;
		if(eventId!=null){
			ci=getWorkflowByEventId(user, eventId);
		}
		if(ci==null){
			ci=buildOpenWorkflow(user, xsiType,id,scanId,project,event);
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

	public static void save(PersistentWorkflowI wrk, EventMetaI c, @SuppressWarnings("unused") boolean overrideSecurity) throws Exception{
		save(wrk,c,false,true);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c,boolean overrideSecurity,boolean triggerEvent) throws Exception{
		if(StringUtils.isEmpty(wrk.getId())){
			log.error("Error saving audit trail entry for workflow item", new Exception());
			//set this to a value that is save-able... to prevent unnecessary failures.
			wrk.setId("ERROR");
		}
		if(!wrk.getDataType().equals("wrk:workflowData")){//prevent recursive events (events of events)
			wrk.save((c==null)?null:c.getUser(), overrideSecurity, false, c);
			wrk.postSave(triggerEvent);
		}
	}

	@SuppressWarnings("unused")
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

	private static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID,final String scanId, final String project_id) throws FieldNotFoundException {
		final PersistentWorkflowI workflow = getWorkflowBuilder(user).getPersistentWorkflowI(user);
		workflow.setDataType(xsiType);
		workflow.setExternalid(project_id);
		workflow.setId(ID);
		if (StringUtils.isNotBlank(scanId)) {
			workflow.setScanId(scanId);
		}
		if(StringUtils.isEmpty(workflow.getId())){
			workflow.setId("NULL");
		}
		workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
		workflow.setLaunchTime(Calendar.getInstance().getTime());

		return workflow;
	}

	private static PersistentWorkflowBuilderAbst builder=null;
}
