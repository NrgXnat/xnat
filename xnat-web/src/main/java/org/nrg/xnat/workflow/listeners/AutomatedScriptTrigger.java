package org.nrg.xnat.workflow.listeners;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.automation.entities.Script;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.framework.constants.Scope;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnat.workflow.WorkflowSaveHandlerAbst;
import org.nrg.xnat.services.messaging.automation.AutomatedScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("unused")
public class AutomatedScriptTrigger extends WorkflowSaveHandlerAbst {
    private final ScriptRunnerService _service = XDAT.getContextService().getBean(ScriptRunnerService.class);
    private static final Logger logger = LoggerFactory.getLogger(AutomatedScriptTrigger.class);

    @Override
    public void handleEvent(Event e, WrkWorkflowdata wrk) {
        if (StringUtils.equals(PersistentWorkflowUtils.COMPLETE, wrk.getStatus()) && !StringUtils.equals(wrk.getExternalid(), PersistentWorkflowUtils.ADMIN_EXTERNAL_ID)) {
            //check to see if this has been handled before

            for (Script script : getScripts(wrk.getExternalid(), wrk.getPipelineName())) {
                try {
                    //create a queued workflow to track this script
                    final String action = "Executed script " + script.getScriptId();
                    final String justification = wrk.getJustification();
                    final String comment = "Executed script " + script.getScriptId() + " triggered by event " + wrk.getPipelineName();
                    PersistentWorkflowI scriptWrk = PersistentWorkflowUtils.buildOpenWorkflow(wrk.getUser(), wrk.getDataType(), wrk.getId(), wrk.getExternalid(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.PROCESS, action, StringUtils.isNotBlank(justification) ? justification : "Automated execution: " + comment, comment));
                    assert scriptWrk != null;
                    scriptWrk.setStatus(PersistentWorkflowUtils.QUEUED);
                    WorkflowUtils.save(scriptWrk, scriptWrk.buildEvent());

                    AutomatedScriptRequest request = new AutomatedScriptRequest(wrk.getWrkWorkflowdataId().toString(), wrk.getUser(), script.getScriptId(), wrk.getPipelineName(), scriptWrk.getWorkflowId().toString(), wrk.getDataType(), wrk.getId(), wrk.getExternalid());
                    XDAT.sendJmsRequest(request);
                } catch (Exception e1) {
                    logger.error("", e1);
                }
            }
        }
    }

    private List<Script> getScripts(final String projectId, final String event) {
        final List<Script> scripts = Lists.newArrayList();

        //project level scripts
        if (StringUtils.isNotBlank(projectId)) {
            Script script = _service.getScript(Scope.Project, projectId, event);
            if (script != null) {
                scripts.add(script);
            }
        }

        //site level scripts
        Script script = _service.getScript(Scope.Site, null, event);
        if (script != null) {
            scripts.add(script);
        }

        return scripts;
    }
}
