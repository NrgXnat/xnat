package org.nrg.xnat.workflow.listeners;

import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.Event;
import org.nrg.xnat.workflow.PipelineEmailHandlerAbst;

import java.util.Map;

/**
 * Created by flavin on 3/2/15.
 */
public class DicomToNiftiEmailHandler extends PipelineEmailHandlerAbst {
    final static Logger logger = Logger.getLogger(DicomToNiftiEmailHandler.class);

    private final String PIPELINE_NAME = "mricron/DicomToNifti.xml";
    private final String PIPELINE_NAME_PRETTY = "DicomToNifti";


    public void handleEvent(Event e, WrkWorkflowdata wrk) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("pipelineName",PIPELINE_NAME_PRETTY);
        if (completed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_SUCCESS, DEFAULT_SUBJECT_SUCCESS, "processed.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "processed.lst", params);
        }
    }
}
