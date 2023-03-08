package org.nrg.xnat.eventservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.XDAT;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//@XnatEventServiceEvent(name="WorkflowStatusChangeEvent")
public class WorkflowStatusChangeEvent extends AbstractEventServiceEvent<PersistentWorkflowI> {

    public enum Status {CHANGED}

    private final String displayName = "Workflow Status";
    private final String description = "XNAT Workflow status change detected.";

    public WorkflowStatusChangeEvent() {};

    public WorkflowStatusChangeEvent(final PersistentWorkflowI payload, final String eventUser,
                                     final WorkflowStatusChangeEvent.Status status, final String projectId,
                                     final String xsiType) {
        super(payload, eventUser, status, projectId, xsiType);
        payloadId = payload.getId();
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public PersistentWorkflowI getObject(UserI user) {
        return WorkflowUtils.getUniqueWorkflow(user, payloadId);
    }

    @Override
    public String getPayloadXnatType() { return "WorkflowStatusEvent"; }

    @Override
    public Boolean isPayloadXsiType() { return false; }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(WorkflowStatusChangeEvent.Status.values()).map(WorkflowStatusChangeEvent.Status::name).collect(Collectors.toList()); }

    @Override
    public Boolean filterablePayload() { return true;}

    @Override
    public Object getPayloadSignatureObject() {
        Object payloadSignatureObject = null;
        if(payloadId != null) {
            try {
                final ObjectMapper mapper = XDAT.getContextService().getBeanSafely(ObjectMapper.class);
                if (mapper != null && mapper.canSerialize(getObjectClass())) {
                    payloadSignatureObject = getObject(null);
                }
            } catch (Exception e) {
                log.error("Failed to return WorkflowStatusChangeEvent payload signature.\n" + e.getMessage());
            }
        }
        return payloadSignatureObject;

    }


}