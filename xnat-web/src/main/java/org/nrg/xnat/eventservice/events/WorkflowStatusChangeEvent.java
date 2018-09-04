package org.nrg.xnat.eventservice.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.XDAT;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="WorkflowStatusChangeEvent")
public class WorkflowStatusChangeEvent extends CombinedEventServiceEvent<WorkflowStatusChangeEvent, WorkflowStatusEvent> {
    private static final Logger log = LoggerFactory.getLogger(WorkflowStatusChangeEvent.class);


    public enum Status {CHANGED}

    final String displayName = "Workflow Status";
    final String description = "XNAT Workflow status change detected.";

    final ObjectMapper mapper = XDAT.getContextService().getBeanSafely(ObjectMapper.class);

    public WorkflowStatusChangeEvent() {}

    ;

    public WorkflowStatusChangeEvent(final WorkflowStatusEvent payload, final String eventUser,
                                     final WorkflowStatusChangeEvent.Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getPayloadXnatType() { return "WorkflowStatusEvent"; }

    @Override
    public Boolean isPayloadXsiType() { return false; }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(WorkflowStatusChangeEvent.Status.values()).map(WorkflowStatusChangeEvent.Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new WorkflowStatusChangeEvent();
    }

    @Override
    public Boolean filterablePayload() { return true;}

    @Override
    public Object getPayloadSignatureObject() {
        Object payloadSignatureObject = null;
        try {
            if (getObject() != null && mapper != null && mapper.canSerialize(getObject().getClass())) {
                payloadSignatureObject = getObject();
            }
        } catch (Exception e) {
            log.error("Failed to return WorkflowStatusChangeEvent payload signature.\n" + e.getMessage());
        }
        return payloadSignatureObject;
    }
}