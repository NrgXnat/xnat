/*
 * web: org.nrg.xnat.event.listeners.AutoRunEmailHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.Maps;

import org.nrg.xdat.preferences.NotificationsPreferences;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.springframework.stereotype.Service;

import static reactor.bus.selector.Selectors.R;

import java.util.Map;

import javax.inject.Inject;

/**
 * The Class AutoRunEmailHandler.
 */
@Service
public class AutoRunEmailHandler extends PipelineEmailHandlerAbst implements Consumer<Event<WorkflowStatusEvent>> {

    /**
     * Instantiates a new auto run email handler.
     *
     * @param eventBus    the event bus
     * @param preferences The notifications preferences object.
     */
    @Inject
    public AutoRunEmailHandler(EventBus eventBus, final NotificationsPreferences preferences) {
        _preferences = preferences;
        eventBus.on(R(WorkflowStatusEvent.class.getName() + "[.]?(" + PersistentWorkflowUtils.COMPLETE + "|" + PersistentWorkflowUtils.FAILED + ")"), this);
    }

    /* (non-Javadoc)
     * @see reactor.fn.Consumer#accept(java.lang.Object)
     */
    @Override
    public void accept(Event<WorkflowStatusEvent> event) {

        final WorkflowStatusEvent wfsEvent = event.getData();
        if (wfsEvent.getWorkflow() instanceof WrkWorkflowdata) {
            handleEvent(wfsEvent);
        }

    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.event.listeners.WorkflowStatusEventHandlerAbst#handleEvent(org.nrg.xft.event.WorkflowStatusEvent)
     */
    public void handleEvent(WorkflowStatusEvent e) {
        Map<String, Object> params = Maps.newHashMap();
        /*
      The pipeline name pretty.
     */
        final String PIPELINE_NAME_PRETTY = "AutoRun";
        params.put("pipelineName", PIPELINE_NAME_PRETTY);
        params.put("contactEmail", _preferences.getHelpContactInfo());
        if (!(e.getWorkflow() instanceof WrkWorkflowdata)) {
            return;
        }
        /*
      The pipeline name.
     */
        final String PIPELINE_NAME = "xnat_tools/AutoRun.xml";
        if (completed(e)) {
            standardPipelineEmailImpl(e, (WrkWorkflowdata) e.getWorkflow(), PIPELINE_NAME, "/screens/PipelineEmail_AutoRun_success.vm", " archiving complete", "archival.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, (WrkWorkflowdata) e.getWorkflow(), PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "archival.lst", params);
        }
    }

    private final NotificationsPreferences _preferences;
}
