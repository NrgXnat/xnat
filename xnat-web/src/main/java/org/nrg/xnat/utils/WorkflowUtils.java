/*
 * web: org.nrg.xnat.utils.WorkflowUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.persist.PersistentWorkflowBuilderAbst;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.ActionNameAbsent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.IDAbsent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils.JustificationAbsent;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;

public class WorkflowUtils extends PersistentWorkflowBuilderAbst {
	final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WorkflowUtils.class);
	
	public PersistentWorkflowI getPersistentWorkflowI(UserI user){
		return new WrkWorkflowdata((UserI)user);
	}
		
	public PersistentWorkflowI getWorkflowByEventId(final UserI user,final Integer id){
		if(id==null)return null;
		return  WrkWorkflowdata.getWrkWorkflowdatasByWrkWorkflowdataId(id, user, false);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.utils.PersistentWorkflowBuilderI#getOpenWorkflows(org.nrg.xdat.security.UserI, java.lang.String)
	 */
	@Override
	public Collection<? extends PersistentWorkflowI> getOpenWorkflows(final UserI user,final String ID){		
		//check to see if a process is already running.
		final CriteriaCollection cc= new CriteriaCollection("AND");
		cc.addClause("wrk:workFlowData.ID",ID);
		
		final CriteriaCollection cc2= new CriteriaCollection("OR");
		cc2.addClause("wrk:workFlowData.status",PersistentWorkflowUtils.IN_PROGRESS);
		cc2.addClause("wrk:workFlowData.status",PersistentWorkflowUtils.RUNNING);
		cc2.addClause("wrk:workFlowData.status",PersistentWorkflowUtils.QUEUED);
		
		cc.add(cc2);
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false);
		return al;

	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xnat.utils.PersistentWorkflowBuilderI#getWorkflows(org.nrg.xdat.security.UserI, java.lang.String)
	 */
	@Override
	public Collection<? extends PersistentWorkflowI> getWorkflows(final UserI user,final String ID){		
		//check to see if a process is already running.
		final CriteriaCollection cc= new CriteriaCollection("AND");
		cc.addClause("wrk:workFlowData.ID",ID);
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false);
		return al;

	}
	
	public static PersistentWorkflowI getUniqueWorkflow(final UserI user, final String pipeline_name, final String ID, final Date launch_time){
		final CriteriaCollection cc = new CriteriaCollection("AND");
		cc.addClause("wrk:workFlowData.ID", ID);
		cc.addClause("wrk:workFlowData.pipeline_name", pipeline_name);
		cc.addClause("wrk:workflowData.launch_time", launch_time);
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false); 
		return (al == null || al.size() == 0) ? null : al.iterator().next();
	}
	
	public static PersistentWorkflowI getUniqueWorkflow(final UserI user, final String workflow_id){
		final CriteriaCollection cc = new CriteriaCollection("AND");
		cc.addClause("wrk:workFlowData.wrk_workflowdata_id", workflow_id);
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false); 
		return (al == null || al.size() == 0) ? null : al.iterator().next();
	}
	
	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final String xsiType,final String ID,final String project_id, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		return PersistentWorkflowUtils.buildOpenWorkflow(user, xsiType, ID, project_id, event);
	}
	
	public static PersistentWorkflowI buildOpenWorkflow(final UserI user, final XFTItem expt, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		return PersistentWorkflowUtils.buildOpenWorkflow(user, expt, event);
	}
	
	public static PersistentWorkflowI buildProjectWorkflow(final UserI user, final XnatProjectdata project,  final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		return PersistentWorkflowUtils.buildOpenWorkflow(user, XnatProjectdata.SCHEMA_ELEMENT_NAME,project.getId(),project.getId(), event);
	}
	
	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user,XFTItem expt,  final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		return PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, expt, event);
	}
	
	public static PersistentWorkflowI getOrCreateWorkflowData(Integer eventId, UserI user, String xsiType, String id, String project, final EventDetails event) throws JustificationAbsent,ActionNameAbsent,IDAbsent{
		return PersistentWorkflowUtils.getOrCreateWorkflowData(eventId, user, xsiType, id, project, event);
	}

	public static void complete(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		PersistentWorkflowUtils.complete(wrk, c);
	}

	public static void save(PersistentWorkflowI wrk, EventMetaI c) throws Exception{
		PersistentWorkflowUtils.save(wrk, c);
	}

	public static EventMetaI setStep(PersistentWorkflowI wrk, String s){
		return PersistentWorkflowUtils.setStep(wrk, s);
	}

	public static void fail(PersistentWorkflowI wrk,EventMetaI c) throws Exception{
		PersistentWorkflowUtils.fail(wrk, c);
	}

	@Override
	public Collection<? extends PersistentWorkflowI> getWorkflows(
			UserI user, List<String> IDs) {
		final CriteriaCollection cc= new CriteriaCollection("OR");
		for(String ID:IDs){
			cc.addClause("wrk:workFlowData.ID",ID);
		}
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false);
		return al;
	}

	@Override
	public Collection<? extends PersistentWorkflowI> getWorkflowsByExternalId(
			UserI user, String ID) {
		final CriteriaCollection cc= new CriteriaCollection("AND");
		cc.addClause("wrk:workFlowData.ExternalID",ID);
		
		final Collection<? extends PersistentWorkflowI> al = WrkWorkflowdata.getWrkWorkflowdatasByField(cc, user, false);
		return al;
	}
}
