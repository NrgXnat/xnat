package org.nrg.xft.event.entities;

import java.util.List;

import org.nrg.automation.entities.ScriptOutput;
import org.nrg.xft.event.EventI;

import com.google.common.collect.Lists;

/**
 * The Class AutomationCompletionEvent.
 */
public class AutomationCompletionEvent implements EventI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7136676406371991268L;

	/** The id. */
	private String id;
	
	/** The script outputs. */
	private List<ScriptOutput> scriptOutputs = Lists.newArrayList();
	
	/** The event completion time. */
	private Long eventCompletionTime;
	
	/** The notification list. */
	private List<String> notificationList = Lists.newArrayList();
	
	/**
	 * Instantiates a new automation completion event.
	 *
	 * @param id the id
	 */
	public AutomationCompletionEvent(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Gets the script outputs.
	 *
	 * @return the script outputs
	 */
	public List<ScriptOutput> getScriptOutputs() {
		return scriptOutputs;
	}
	
	/**
	 * Sets the script outputs.
	 *
	 * @param scriptOutputs the new script outputs
	 */
	public void setScriptOutputs(List<ScriptOutput> scriptOutputs) {
		if (scriptOutputs!=null) {
			this.scriptOutputs = scriptOutputs;
		}
	}
	
	/**
	 * Adds script output to the output list
	 *
	 * @param scriptOutput to add to the output list
	 */
	public void addScriptOutput(ScriptOutput scriptOutput) {
		this.scriptOutputs.add(scriptOutput); 
	}
	
	/**
	 * Gets the event completion time.
	 *
	 * @return the event completion time
	 */
	public Long getEventCompletionTime() {
		return eventCompletionTime;
	}
	
	/**
	 * Sets the event completion time.
	 *
	 * @param eventCompletionTime the new event completion time
	 */
	public void setEventCompletionTime(Long eventCompletionTime) {
		this.eventCompletionTime = eventCompletionTime;
	}
	
	/**
	 * Gets the notification list.
	 *
	 * @return the notification list
	 */
	public List<String> getNotificationList() {
		return notificationList;
	}
	
	/**
	 * Sets the notification list.
	 *
	 * @param notificationList the new notification list
	 */
	public void setNotificationList(List<String> notificationList) {
		if (notificationList!=null) {
			this.notificationList = notificationList;
		}
	}
	
	/**
	 * Adds email address to the notification list.
	 *
	 * @param emailAddr email address to add to the notification list
	 */
	public void addNotificationEmailAddr(String emailAddr) {
		if (emailAddr!=null && emailAddr.contains("@")) {
			notificationList.add(emailAddr);
			
		}
	}
	
}
