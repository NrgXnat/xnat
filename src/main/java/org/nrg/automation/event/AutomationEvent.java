package org.nrg.automation.event;

import java.util.Map;

import javax.persistence.Transient;

import org.nrg.framework.event.StructuredEvent;
import org.python.google.common.collect.Maps;
import org.nrg.automation.event.entities.AutomationCompletionEvent;

/**
 * The abstract class AutomationEvent.
 * 
 * This class can be used to provide implementation for AutomationEventImplementerI classes, however it is not required.  
 * The requirement for automation events is that they implement AutomationEventImplementerI.
 * 
 */
public abstract class AutomationEvent extends StructuredEvent implements AutomationEventImplementerI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6426741957267293144L;

	/** The automation completion event. */
	private AutomationCompletionEvent automationCompletionEvent;
	
	/** The parameter map. Initialize it wit empty map, to avoid NPE */
	private Map<String,Object> parameterMap = Maps.newHashMap();
	
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

	/* (non-Javadoc)
	 * @see org.nrg.automation.event.AutomationEventImplementerI#getParameterMap()
	 */
	@Override
	@Transient
	public Map<String, Object> getParameterMap() {
		return this.parameterMap;
	}

	/* (non-Javadoc)
	 * @see org.nrg.automation.event.AutomationEventImplementerI#setParameterMap(java.util.Map)
	 */
	@Override
	public void setParameterMap(Map<String, Object> parameterMap) {
		this.parameterMap = parameterMap;
	}

	/* (non-Javadoc)
	 * @see org.nrg.automation.event.AutomationEventImplementerI#addParameterToParameterMap(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addParameterToParameterMap(String parameter, Object value) {
		this.parameterMap.put(parameter, value);
	}

}
