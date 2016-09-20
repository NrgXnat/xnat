/*
 * org.nrg.automation.event.AutomationEventImplementerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event;

import java.util.Map;

import org.nrg.framework.event.StructuredEventI;

/**
 * The Interface AutomationEventImplementerI.
 * 
 * This interface should be implemented for all XNAT events that wish to trigger an automation event.  The AutomationEventScriptHandler
 * listens for events implementing this interface and launches scripts that have handlers associated with your event.  
 */
public interface AutomationEventImplementerI extends StructuredEventI {
	
	/**
	 * Gets the automation completion event.
	 *
	 * @return the automation completion event
	 */
	AutomationCompletionEventI getAutomationCompletionEvent(); 
	
	/**
	 * Sets the automation completion event.
	 *
	 * @param automationCompletionEvent the new automation completion event
	 */
	void setAutomationCompletionEvent(AutomationCompletionEventI automationCompletionEvent); 
	
	/**
	 * Gets the parameter map.
	 *
	 * @return the parameter map
	 */
	Map<String,Object> getParameterMap(); 
	
	/**
	 * Sets the parameter map.
	 *
	 * @param passMap the pass map
	 */
	void setParameterMap(Map<String, Object> passMap); 
	
	/**
	 * Adds the parameter to parameter map.
	 *
	 * @param parameter the parameter
	 * @param value the value
	 */
	void addParameterToParameterMap(String parameter, Object value); 
	
}
