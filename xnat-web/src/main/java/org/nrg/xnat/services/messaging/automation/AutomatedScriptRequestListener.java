package org.nrg.xnat.services.messaging.automation;

import org.nrg.automation.entities.ScriptOutput;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xnat.utils.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class AutomatedScriptRequestListener {

    @SuppressWarnings("unused")
    public void onRequest(final AutomatedScriptRequest request) throws Exception {
        final PersistentWorkflowI workflow = WorkflowUtils.getUniqueWorkflow(request.getUser(), request.getScriptWorkflowId());
        workflow.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
        WorkflowUtils.save(workflow, workflow.buildEvent());

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("user", request.getUser());
        parameters.put("scriptId", request.getScriptId());
        parameters.put("event", request.getEvent());
        parameters.put("srcWorkflowId", request.getSrcWorkflowId());
        parameters.put("scriptWorkflowId", request.getScriptWorkflowId());
        parameters.put("dataType", request.getDataType());
        parameters.put("dataId", request.getDataId());
        parameters.put("externalId", request.getExternalId());
        parameters.put("workflow", workflow);

        try {
            _service.runScript(_service.getScript(request.getScriptId()), parameters);

            if (PersistentWorkflowUtils.IN_PROGRESS.equals(workflow.getStatus())) {
                WorkflowUtils.complete(workflow, workflow.buildEvent());
            }
        } catch (NrgServiceException e) {
            final String message = String.format("Failed running the script %s by user %s for event %s on data type %s instance %s from project %s",
                    request.getScriptId(),
                    request.getUser().getLogin(),
                    request.getEvent(),
                    request.getDataType(),
                    request.getDataId(),
                    request.getExternalId());
            AdminUtils.sendAdminEmail("Script execution failure", message);
            logger.error(message, e);
            if (PersistentWorkflowUtils.IN_PROGRESS.equals(workflow.getStatus())) {
                WorkflowUtils.fail(workflow, workflow.buildEvent());
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(AutomatedScriptRequestListener.class);

    @Inject
    private ScriptRunnerService _service;
}
