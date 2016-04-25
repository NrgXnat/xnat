package org.nrg.xft.event.entities;

import java.util.Map;

import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.AutomationEventImplementerI;
import org.nrg.xft.event.Filterable;
import org.nrg.xft.event.StructuredEvent;
import org.nrg.xft.event.EventClass;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.python.google.common.collect.Maps;

/**
 * The Class WorkflowStatusEvent.
 */
@EventClass(displayName = "Workflow Status Event")
public class WorkflowStatusEvent extends StructuredEvent implements AutomationEventImplementerI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7465778737330635218L;
	
	/** The workflow. */
	PersistentWorkflowI workflow;
	
	/** The status. */
	private String status;
	
	/** The justification. */
	private String justification;

	/** The automationCompletionEvent. */
	private AutomationCompletionEvent automationCompletionEvent;

	private Map<String,Object> parameterMap = Maps.newHashMap();
	
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
		UserI user;
		try {
			user = Users.getUser(workflow.getUsername());
			this.setUserId(user.getID());
		} catch (UserNotFoundException | UserInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	@Filterable(initialValues = { "Complete", "Failed" }, includeValuesFromDatabase = false)
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

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.AutomationEventImplementerI#setAutomationCompletionEvent(org.nrg.xft.event.entities.AutomationCompletionEvent)
	 */
	@Override
	public void setAutomationCompletionEvent(AutomationCompletionEvent automationCompletionEvent) {
		this.automationCompletionEvent = automationCompletionEvent; 
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.AutomationEventImplementerI#getAutomationCompletionEvent()
	 */
	@Override
	public AutomationCompletionEvent getAutomationCompletionEvent() {
		return automationCompletionEvent;
	}

	@Override
	public Map<String, Object> getParameterMap() {
		return this.parameterMap;
	}

	@Override
	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	@Override
	public void addParameterToParameterMap(String parameter, Object value) {
		this.parameterMap.put(parameter, value);
	}

}
