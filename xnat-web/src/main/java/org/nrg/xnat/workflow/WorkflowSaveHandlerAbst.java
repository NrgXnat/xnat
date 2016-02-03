/*
 * org.nrg.xnat.workflow.WorkflowSaveHandlerAbst
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventListener;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 *	Extend this class to create a workflow event handler.  If you place your class in org.nrg.xnat.workflow.listeners, 
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
 *  Also, the current implementation runs these classes in the same thread as the parent event.  So, beware that long operations will slow down the parent events.
 *  For longer actions, consider starting a new thread or use MQ to make an asynchronous response.
 */
public abstract class WorkflowSaveHandlerAbst implements EventListener{
	final static Logger logger = Logger.getLogger(WorkflowSaveHandlerAbst.class);
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.event.EventListener#handleEvent(org.nrg.xft.event.Event)
	 */
	public void handleEvent(Event e){
		if(e.getItem() instanceof WrkWorkflowdata){
			handleEvent(e,(WrkWorkflowdata)e.getItem());
		}
	}
	
	/**
	 * Returns true if this event represents a failed event
	 * @param e
	 * @return
	 */
	public boolean failed(Event e){
		return StringUtils.equals(e.getAction(),PersistentWorkflowUtils.FAILED);
	}

	/**
	 * Returns true if this event represents a completed event
	 * @param e
	 * @return
	 */
	public boolean completed(Event e){
		return StringUtils.equals(e.getAction(),PersistentWorkflowUtils.COMPLETE);
	}
	

	/**
	 * This is the event that should be extended to add customized handling of a workflow event.
	 * @param e
	 * @param wrk
	 */
	public abstract void handleEvent(Event e, WrkWorkflowdata wrk);
}