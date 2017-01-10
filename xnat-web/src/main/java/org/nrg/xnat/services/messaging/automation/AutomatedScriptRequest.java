/*
 * web: org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.automation;

import org.json.JSONObject;
import org.nrg.automation.entities.Script;
import org.nrg.automation.event.AutomationCompletionEventI;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * The Class AutomatedScriptRequest.
 */
public class AutomatedScriptRequest implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5425712284737600869L;
	
	/** The _src event id. */
	private final String _srcEventId;
	
	/** The _src event class. */
	private final String _srcEventClass;
	
	/** The _user. */
	private final UserI  _user;
	
	/** The _script id. */
	private final String _scriptId;
	
	/** The _event. */
	private final String _event;
	
	/** The _script workflow id. */
	private final String _scriptWorkflowId;
	
	/** The _external id. */
	private final String _externalId;
	
	/** The _data type. */
	private final String _dataType;
	
	/** The _data id. */
	private final String _dataId;
	
	/** The _argument map. */
	private final Map<String,Object> _argumentMap = Maps.newHashMap();
	
	/** The _automation completion event. */
	private final AutomationCompletionEventI _automationCompletionEvent;
	
	/** The _automation event. */
	private final AutomationEventImplementerI _automationEvent;

	/**
	 * Instantiates a new automated script request.
	 *
	 * @param srcEventId the src event id
	 * @param srcEventClass the src event class
	 * @param user the user
	 * @param scriptId the script id
	 * @param event the event
	 * @param scriptWorkflow the script workflow
	 * @param dataType the data type
	 * @param dataId the data id
	 * @param externalId the external id
	 * @param argumentMap the argument map
	 * @param automationCompletionEvent the automation completion event
	 */
	public AutomatedScriptRequest(String srcEventId, String srcEventClass, UserI user, String scriptId,
			String event, String scriptWorkflow, String dataType, String dataId, String externalId,
			Map<String, Object> argumentMap, AutomationCompletionEventI automationCompletionEvent) {
		_srcEventId = srcEventId;
		_srcEventClass = srcEventClass;
		_user = user;
		_scriptId = scriptId;
		_event = event;
		_scriptWorkflowId = scriptWorkflow;
		_dataType = dataType;
		_dataId = dataId;
		_externalId = externalId;
		_automationCompletionEvent = automationCompletionEvent;
		_automationEvent = null;
		if (argumentMap != null) {
			_argumentMap.putAll(argumentMap);
		}
	}
	
	/**
	 * Instantiates a new automated script request.
	 *
	 * @param srcEventId the src event id
	 * @param srcEventClass the src event class
	 * @param user the user
	 * @param scriptId the script id
	 * @param event the event
	 * @param scriptWorkflow the script workflow
	 * @param dataType the data type
	 * @param dataId the data id
	 * @param externalId the external id
	 */
	public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId) {
		this(srcEventId, srcEventClass, user, scriptId, event, scriptWorkflow, dataType, dataId, externalId, null, null);
	}
	
	/**
	 * Instantiates a new automated script request.
	 *
	 * @param srcEventId the src event id
	 * @param srcEventClass the src event class
	 * @param user the user
	 * @param scriptId the script id
	 * @param event the event
	 * @param scriptWorkflow the script workflow
	 * @param dataType the data type
	 * @param dataId the data id
	 * @param externalId the external id
	 * @param argumentMap the argument map
	 */
	public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId, Map<String,Object> argumentMap) {
		this(srcEventId, srcEventClass, user, scriptId, event, scriptWorkflow, dataType, dataId, externalId, argumentMap, null);
	}	

	/**
	 * Instantiates a new automated script request.
	 *
	 * @param automationEvent the automation event
	 * @param eventName the event name
	 * @param user the user
	 * @param script the script
	 * @param scriptWrk the script wrk
	 */
	public AutomatedScriptRequest(AutomationEventImplementerI automationEvent, String eventName, UserI user, Script script, PersistentWorkflowI scriptWrk) {
		this._automationEvent = automationEvent;
        this._srcEventId = automationEvent.getSrcStringifiedId();
        this._srcEventClass = automationEvent.getSrcEventClass();
        this._user = user;
        this._scriptId = script.getScriptId();
        this._event = eventName;
        this._scriptWorkflowId = scriptWrk.getWorkflowId().toString();
        this._dataType = automationEvent.getEntityType();
        this._dataId = automationEvent.getEntityId();
        this._externalId = automationEvent.getExternalId();
        this._automationCompletionEvent = automationEvent.getAutomationCompletionEvent();
        if (automationEvent.getParameterMap()!=null) {
        	this._argumentMap.putAll(automationEvent.getParameterMap());
        }
	}

	/**
	 * Gets the src event id.
	 *
	 * @return the src event id
	 */
	public String getSrcEventId() {
		return _srcEventId;
	}
	
	/**
	 * Gets the src event class.
	 *
	 * @return the src event class
	 */
	public String getSrcEventClass() {
		return _srcEventClass;
	}

	/**
	 * Gets the user.
	 *
	 * @return the user
	 */
	public UserI getUser() {
		return _user;
	}

	/**
	 * Gets the script id.
	 *
	 * @return the script id
	 */
	public String getScriptId() {
		return _scriptId;
	}

	/**
	 * Gets the event.
	 *
	 * @return the event
	 */
	public String getEvent() {
		return _event;
	}

	/**
	 * Gets the script workflow id.
	 *
	 * @return the script workflow id
	 */
	public String getScriptWorkflowId() {
		return _scriptWorkflowId;
	}

	/**
	 * Gets the external id.
	 *
	 * @return the external id
	 */
	public String getExternalId() {
		return _externalId;
	}

	/**
	 * Gets the data type.
	 *
	 * @return the data type
	 */
	public String getDataType() {
		return _dataType;
	}

	/**
	 * Gets the data id.
	 *
	 * @return the data id
	 */
	public String getDataId() {
		return _dataId;
	}

	/**
	 * Gets the argument map.
	 *
	 * @return the argument map
	 */
	public Map<String,Object> getArgumentMap() {
		return _argumentMap;
	}
	
	
	/**
	 * Gets the automation completion event.
	 *
	 * @return the automation completion event
	 */
	public AutomationCompletionEventI getAutomationCompletionEvent() {
		return _automationCompletionEvent;
	}
	
	
	/**
	 * Gets the automation event.
	 *
	 * @return the automation event
	 */
	public AutomationEventImplementerI getAutomationEvent() {
		return _automationEvent;
	}

	/**
	 * Gets the argument json.
	 *
	 * @return the argument json
	 */
	public String getArgumentJson() {
		return new JSONObject(_argumentMap).toString();
	}
}
