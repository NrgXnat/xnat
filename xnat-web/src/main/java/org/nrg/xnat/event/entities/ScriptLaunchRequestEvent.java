package org.nrg.xnat.event.entities;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.AutomationCompletionEvent;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.framework.event.EventClass;
import org.nrg.framework.event.persist.PersistentEventImplementerI;

/**
 * The Class AutomationLaunchRequestEvent.
 */
@Entity
@PrimaryKeyJoinColumn(name="ID", referencedColumnName="ID")
@EventClass(displayName="Script Launch Request Event")
public class ScriptLaunchRequestEvent extends PersistentEvent implements PersistentEventImplementerI, AutomationEventImplementerI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7465778737330635218L;
	
	/** The automation completion event. */
	private AutomationCompletionEvent automationCompletionEvent;
	
	private Map<String,Object> parameterMap;
	
	/**
	 * Instantiates a new automation launch request event.
	 */
	public ScriptLaunchRequestEvent() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.AutomationEventImplementerI#getAutomationCompletionEvent()
	 */
	@Override
	@Transient
	public AutomationCompletionEvent getAutomationCompletionEvent() {
		return automationCompletionEvent;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.AutomationEventImplementerI#setAutomationCompletionEvent(org.nrg.xft.event.entities.AutomationCompletionEvent)
	 */
	@Override
	public void setAutomationCompletionEvent(AutomationCompletionEvent automationCompletionEvent) {
		this.automationCompletionEvent = automationCompletionEvent;
	}

	@Override
	@Transient
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
