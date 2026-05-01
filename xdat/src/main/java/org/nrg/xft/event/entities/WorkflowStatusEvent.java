/*
 * core: org.nrg.xft.event.entities.WorkflowStatusEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.event.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nrg.automation.event.AutomationEvent;
import org.nrg.framework.event.EventClass;
import org.nrg.framework.event.Filterable;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;

import java.util.Map;

/**
 * The Class WorkflowStatusEvent.
 */
@EventClass(name = "WorkflowStatusEvent", description = "Workflow Status Event")
public class WorkflowStatusEvent extends AutomationEvent {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7465778737330635218L;
	
	/** The workflow. */
	@JsonIgnore
	PersistentWorkflowI workflow;
	
	/** The status. */
	private String status;
	
	/** The justification. */
	private String justification;
	
	/**
	 * Instantiates a new workflow status event.
	 */
	public WorkflowStatusEvent() {
		super();
	}
	
	/**
	 * Instantiates a new workflow status event.  u
	 *
	 * @param workflow the workflow
	 */
	public WorkflowStatusEvent(PersistentWorkflowI workflow) {
		this();
		this.workflow = workflow;
		this.setEventId(workflow.getPipelineName());
		this.setSrcEventClass(this.getClass().getName());
		final String project = workflow.getExternalid();
		this.setExternalId(project);
		this.setEntityId(workflow.getId());
		this.setEntityType(workflow.getDataType());
		this.setStatus(workflow.getStatus());
		this.setJustification(workflow.getJustification());
		final Map<String,String> eventSpecificMap = Maps.newHashMap();
		eventSpecificMap.put("status", status);
		eventSpecificMap.put("justification", justification);
		this.setEventSpecificFieldsAsMap(eventSpecificMap);
		this.setUserId(workflow.getUserId());
	}
	
	/**
	 * Gets the workflow.
	 *
	 * @return the workflow
	 */
	public PersistentWorkflowI getWorkflow() {
		return workflow;
	}
	
	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	@Filterable(initialValues = { "Complete", "Failed" }, defaultValue = "Complete", filterRequired = true, includeValuesFromDatabase = false)
	public String getStatus() {
		return status;
	}
	
	/**
	 * Sets the justification.
	 *
	 * @param justification the new justification
	 */
	public void setJustification(String justification) {
		this.justification = justification;
	}
	
	/**
	 * Gets the justification.
	 *
	 * @return the justification
	 */
	public String getJustification() {
		return justification;
	}

	@Override
	public String toString() {
		return workflow != null ? "Workflow " + workflow.getId() + " status: " + workflow.getStatus() : ("Status: " + status + (StringUtils.isNotBlank(justification) ? " justification: \"" + justification + "\"" : ""));
	}
}
