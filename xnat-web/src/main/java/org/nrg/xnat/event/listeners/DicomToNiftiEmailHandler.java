/*
 * web: org.nrg.xnat.event.listeners.DicomToNiftiEmailHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.Maps;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;
import java.util.Map;

import static reactor.bus.selector.Selectors.R;

@Service
public class DicomToNiftiEmailHandler extends PipelineEmailHandlerAbst implements Consumer<Event<WorkflowStatusEvent>> {
	/**
	 * Instantiates a new dicom to nifti email handler.
	 *  @param eventBus the event bus
	 * @param preferences The site configuration preferences.
	 */
	@Autowired
	public DicomToNiftiEmailHandler(EventBus eventBus, final NotificationsPreferences preferences){
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
    	if (!(e.getWorkflow() instanceof WrkWorkflowdata)) { 
    		return;
    	}
    	WrkWorkflowdata wrk = (WrkWorkflowdata)e.getWorkflow();
        Map<String,Object> params = Maps.newHashMap();
        /* The pipeline name pretty. */
		final String PIPELINE_NAME_PRETTY = "DicomToNifti";
		params.put("pipelineName", PIPELINE_NAME_PRETTY);
		params.put("contactEmail", _preferences.getHelpContactInfo());
        /* The pipeline name. */
		final String PIPELINE_NAME = "mricron/DicomToNifti.xml";
		if (completed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_SUCCESS, DEFAULT_SUBJECT_SUCCESS, "processed.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "processed.lst", params);
        }
    }

    private final NotificationsPreferences _preferences;
}
