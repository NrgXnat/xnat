package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xft.event.entities.WorkflowStatusEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="WorkflowStatusChangeEvent")
public class WorkflowStatusChangeEvent extends CombinedEventServiceEvent<WorkflowStatusChangeEvent, WorkflowStatusEvent> {
    public enum Status {CHANGED}

    final String displayName = "Workflow Status Change";
    final String description = "XNAT Workflow status change detected.";

    public WorkflowStatusChangeEvent(){};

    public WorkflowStatusChangeEvent(final WorkflowStatusEvent payload, final String eventUser, final WorkflowStatusChangeEvent.Status status, final String projectId) {
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
}
