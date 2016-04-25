package org.nrg.xnat.event.listeners;

import com.google.common.collect.Maps;

import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import org.apache.log4j.Logger;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.springframework.stereotype.Service;

import static reactor.bus.selector.Selectors.R;

import java.util.Map;

import javax.inject.Inject;

/**
 * Created by flavin on 3/2/15.
 */
@Service
public class DicomToNiftiEmailHandler extends PipelineEmailHandlerAbst implements Consumer<Event<WorkflowStatusEvent>> {
    
    /** The Constant logger. */
    final static Logger logger = Logger.getLogger(DicomToNiftiEmailHandler.class);

    /** The pipeline name. */
    private final String PIPELINE_NAME = "mricron/DicomToNifti.xml";
    
    /** The pipeline name pretty. */
    private final String PIPELINE_NAME_PRETTY = "DicomToNifti";
	
	/**
	 * Instantiates a new dicom to nifti email handler.
	 *
	 * @param eventBus the event bus
	 */
	@Inject public DicomToNiftiEmailHandler( EventBus eventBus ){
		eventBus.on(R(WorkflowStatusEvent.class.getName() + "[.]("
				+ PersistentWorkflowUtils.COMPLETE + "|" + PersistentWorkflowUtils.FAILED + ")"), this);
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
        params.put("pipelineName",PIPELINE_NAME_PRETTY);
        if (completed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_SUCCESS, DEFAULT_SUBJECT_SUCCESS, "processed.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "processed.lst", params);
        }
    }
}
