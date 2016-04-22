package org.nrg.xft.event;

import java.util.Map;

import org.nrg.xft.event.entities.AutomationCompletionEvent;

/**
 * The Interface AutomationEventImplementerI.
 */
public interface AutomationEventImplementerI extends StructuredEventI {
	
	/**
	 * Gets the automation completion event.
	 *
	 * @return the automation completion event
	 */
	AutomationCompletionEvent getAutomationCompletionEvent(); 
	
	/**
	 * Sets the automation completion event.
	 *
	 * @param automationCompletionEvent the new automation completion event
	 */
	void setAutomationCompletionEvent(AutomationCompletionEvent automationCompletionEvent); 
	
	Map<String,Object> getParameterMap(); 
	
	void setParameterMap(Map<String, Object> passMap); 
	
	void addParameterToParameterMap(String parameter, Object value); 
	
}
