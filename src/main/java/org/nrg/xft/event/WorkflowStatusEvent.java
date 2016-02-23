package org.nrg.xft.event;

import java.io.Serializable;

import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;

/**
 * The Class WorkflowStatusEvent.
 */
public class WorkflowStatusEvent implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6839844949642616545L;
	
	/** The workflow. */
	final PersistentWorkflowI workflow;
	
	/**
	 * Instantiates a new workflow status event.
	 *
	 * @param workflow the workflow
	 */
	public WorkflowStatusEvent(PersistentWorkflowI workflow) {
		this.workflow = workflow;
	}
	
	/**
	 * Gets the workflow.
	 *
	 * @return the workflow
	 */
	public PersistentWorkflowI getWorkflow() {
		return workflow;
	}
	
}
