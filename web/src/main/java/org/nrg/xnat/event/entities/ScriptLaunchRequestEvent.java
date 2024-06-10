/*
 * web: org.nrg.xnat.event.entities.ScriptLaunchRequestEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.entities;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

import org.nrg.automation.event.AutomationCompletionEventI;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.automation.event.entities.PersistentEvent;
import org.nrg.framework.event.EventClass;

import com.google.common.collect.Maps;

/**
 * The Class AutomationLaunchRequestEvent.
 */
@Entity
@PrimaryKeyJoinColumn(name="ID", referencedColumnName="ID")
@EventClass(name="ScriptLaunchRequestEvent", description="Script Launch Request Event")
public class ScriptLaunchRequestEvent extends PersistentEvent implements AutomationEventImplementerI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 7465778737330635218L;
	
	/** The automation completion event. */
	private AutomationCompletionEventI automationCompletionEvent;
	
	/** The parameter map. */
	private Map<String,Object> parameterMap = Maps.newHashMap();
	
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
	public AutomationCompletionEventI getAutomationCompletionEvent() {
		return automationCompletionEvent;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.event.AutomationEventImplementerI#setAutomationCompletionEvent(org.nrg.xft.event.entities.AutomationCompletionEvent)
	 */
	@Override
	public void setAutomationCompletionEvent(AutomationCompletionEventI automationCompletionEvent) {
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
