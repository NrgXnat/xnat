package org.nrg.xnat.workflow.listeners;

import com.google.common.collect.Maps;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.Event;
import org.nrg.xnat.workflow.PipelineEmailHandlerAbst;

import java.util.Map;

public class AutoRunEmailHandler extends PipelineEmailHandlerAbst {

    private final String PIPELINE_NAME = "xnat_tools/AutoRun.xml";
    private final String PIPELINE_NAME_PRETTY = "AutoRun";

    public void handleEvent(Event e, WrkWorkflowdata wrk) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("pipelineName",PIPELINE_NAME_PRETTY);
        if (completed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, "/screens/PipelineEmail_AutoRun_success.vm", " archiving complete", "archival.lst", params);
        } else if (failed(e)) {
            standardPipelineEmailImpl(e, wrk, PIPELINE_NAME, DEFAULT_TEMPLATE_FAILURE, DEFAULT_SUBJECT_FAILURE, "archival.lst", params);
        }
    }
}
