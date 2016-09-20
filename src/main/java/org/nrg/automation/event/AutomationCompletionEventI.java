/*
 * org.nrg.automation.event.AutomationCompletionEventI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.event;

import java.util.List;

import org.nrg.automation.entities.ScriptOutput;
import org.nrg.framework.event.EventI;

/**
 * The Interface AutomationCompletionEventI.
 */
public interface AutomationCompletionEventI extends EventI {
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId();

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id);
	
	/**
	 * Gets the script outputs.
	 *
	 * @return the script outputs
	 */
	public List<ScriptOutput> getScriptOutputs();
	
	/**
	 * Sets the script outputs.
	 *
	 * @param scriptOutputs the new script outputs
	 */
	public void setScriptOutputs(List<ScriptOutput> scriptOutputs);
	
	/**
	 * Adds the script output.
	 *
	 * @param scriptOutput the script output
	 */
	public void addScriptOutput(ScriptOutput scriptOutput);
	
	/**
	 * Gets the event completion time.
	 *
	 * @return the event completion time
	 */
	public Long getEventCompletionTime();
	
	/**
	 * Sets the event completion time.
	 *
	 * @param eventCompletionTime the new event completion time
	 */
	public void setEventCompletionTime(Long eventCompletionTime);
	
	/**
	 * Gets the notification list.
	 *
	 * @return the notification list
	 */
	public List<String> getNotificationList();
	
	/**
	 * Sets the notification list.
	 *
	 * @param notificationList the new notification list
	 */
	public void setNotificationList(List<String> notificationList);
	
}
