package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ProjectEvent")
public class ProjectEvent extends CombinedEventServiceEvent<XnatProjectdataI>  {

    public enum Status {CREATED, DELETED};

    final String displayName = "Project Event";
    final String description = "Project created or deleted.";

    public ProjectEvent(){};

    public ProjectEvent(final XnatProjectdataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public List<EventScope> getEventScope() { return Arrays.asList(EventScope.SITE); }
}
