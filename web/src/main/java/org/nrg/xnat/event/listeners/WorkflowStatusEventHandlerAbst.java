/*
 * web: org.nrg.xnat.event.listeners.WorkflowStatusEventHandlerAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 *	Extend this class to create a workflow status event handler.  Your class should be implemented as a service in:
 *	<b>org.nrg.xnat.event.listeners</b>.  Optionally, your class can be placed in a package specified by configuration in:
 * <b>org.nrg.xnat.configuration.ReactorConfig</b>
 *  this class will be executed everytime a workflow object is saved.
 *  
 *  Be careful to make sure you are only responding to events once and when you intend to.  For example, if a workflow is marked as Failed and saved,
 *  but then resaved with the same status, that might result in your object being called twice.  You may want to track the workflow IDs of the
 *  events that you have responded to.
 *  
 *  a sample implementation would be something like:
 *  
 *  static List<Integer> HANDLED_ALREADY=Lists.newArrayList(); 
 *  
 *  public void handleEvent(Event e, WrkWorkflowdata wrk){
 *  	if(completed(e) && 
 *  		!HANDLED_ALREADY.contains(wrk.getWrkWorkflowdataId())){
 *  			HANDLED_ALREADY.put(wrk.getWrkWorkflowdataId());
 *  			//do something the first time this workflow entry is marked as complete
 *  	}
 *  }
 *  
 */
public abstract class WorkflowStatusEventHandlerAbst { 
	final static Logger logger = Logger.getLogger(WorkflowStatusEventHandlerAbst.class);
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.event.EventListener#handleEvent(org.nrg.xft.event.Event)
	 */
	public abstract void handleEvent(WorkflowStatusEvent e);
	
	/**
	 * Returns true if this event represents a failed event
	 * @param e
	 * @return
	 */
	public boolean failed(WorkflowStatusEvent e){
		return StringUtils.equals(e.getWorkflow().getStatus(),PersistentWorkflowUtils.FAILED);
	}

	/**
	 * Returns true if this event represents a completed event
	 * @param e
	 * @return
	 */
	public boolean completed(WorkflowStatusEvent e){
		return StringUtils.equals(e.getWorkflow().getStatus(),PersistentWorkflowUtils.COMPLETE);
	}
	
}
