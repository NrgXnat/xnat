/*
 * web: org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.messaging.automation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.nrg.automation.entities.Script;
import org.nrg.automation.event.AutomationCompletionEventI;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class AutomatedScriptRequest.
 */
@Getter
@Accessors(prefix = "_")
@RequiredArgsConstructor
@Slf4j
public class AutomatedScriptRequest implements Serializable {
    /**
     * Instantiates a new automated script request.
     *
     * @param srcEventId                the src event id
     * @param srcEventClass             the src event class
     * @param user                      the user
     * @param scriptId                  the script id
     * @param event                     the event
     * @param scriptWorkflow            the script workflow
     * @param dataType                  the data type
     * @param dataId                    the data id
     * @param externalId                the external id
     * @param argumentMap               the argument map
     * @param automationCompletionEvent the automation completion event
     */
    public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId,
                                  final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId,
                                  final Map<String, Object> argumentMap, final AutomationCompletionEventI automationCompletionEvent) {
        this(srcEventId, srcEventClass, user.getUsername(), scriptId, event, scriptWorkflow, externalId, dataType, dataId, automationCompletionEvent, null, argumentMap != null ? new HashMap<>(argumentMap) : new HashMap<String, Object>());
    }

    /**
     * Instantiates a new automated script request.
     *
     * @param srcEventId     the src event id
     * @param srcEventClass  the src event class
     * @param user           the user
     * @param scriptId       the script id
     * @param event          the event
     * @param scriptWorkflow the script workflow
     * @param dataType       the data type
     * @param dataId         the data id
     * @param externalId     the external id
     */
    public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId) {
        this(srcEventId, srcEventClass, user.getUsername(), scriptId, event, scriptWorkflow, externalId, dataType, dataId, null, null, null);
    }

    /**
     * Instantiates a new automated script request.
     *
     * @param srcEventId     the src event id
     * @param srcEventClass  the src event class
     * @param user           the user
     * @param scriptId       the script id
     * @param event          the event
     * @param scriptWorkflow the script workflow
     * @param dataType       the data type
     * @param dataId         the data id
     * @param externalId     the external id
     * @param argumentMap    the argument map
     */
    public AutomatedScriptRequest(final String srcEventId, final String srcEventClass, final UserI user, final String scriptId, final String event, final String scriptWorkflow, final String dataType, final String dataId, final String externalId, Map<String, Object> argumentMap) {
        this(srcEventId, srcEventClass, user.getUsername(), scriptId, event, scriptWorkflow, externalId, dataType, dataId, null, null, argumentMap);
    }

    /**
     * Instantiates a new automated script request.
     *
     * @param automationEvent the automation event
     * @param eventName       the event name
     * @param user            the user
     * @param script          the script
     * @param scriptWorkflow  the script wrk
     */
    public AutomatedScriptRequest(final AutomationEventImplementerI automationEvent, final String eventName, final UserI user, final Script script, final PersistentWorkflowI scriptWorkflow) {
        this(automationEvent.getSrcStringifiedId(), automationEvent.getSrcEventClass(), user.getUsername(), script.getScriptId(), eventName, scriptWorkflow.getWorkflowId().toString(),
             automationEvent.getExternalId(), automationEvent.getEntityType(), automationEvent.getEntityId(), automationEvent.getAutomationCompletionEvent(), automationEvent,
             automationEvent.getParameterMap() != null ? new HashMap<>(automationEvent.getParameterMap()) : new HashMap<String, Object>());
    }

    /**
     * Gets the argument json.
     *
     * @return the argument json
     */

    public String getArgumentJson() {
        return new JSONObject(_argumentMap).toString();
    }

    private final String                      _srcEventId;
    private final String                      _srcEventClass;
    private final String                      _username;
    private final String                      _scriptId;
    private final String                      _event;
    private final String                      _scriptWorkflowId;
    private final String                      _externalId;
    private final String                      _dataType;
    private final String                      _dataId;
    private final AutomationCompletionEventI  _automationCompletionEvent;
    private final AutomationEventImplementerI _automationEvent;
    private final Map<String, Object>         _argumentMap;
}
