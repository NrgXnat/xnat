package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@XnatEventServiceEvent(name="ProjectEvent")
public class ProjectEvent extends CombinedEventServiceEvent<ProjectEvent, XnatProjectdataI>  {

    public enum Status {CREATED, DELETED};

    final String displayName = "Project Status Change";
    final String description = "Project created or deleted.";

    public ProjectEvent(){};

    public ProjectEvent(final XnatProjectdataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getPayloadXnatType() {
        return "xnat:projectData";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public EnumSet getStatiStates() { return EnumSet.allOf(SessionEvent.Status.class); }

    @Override
    public EventServiceListener getInstance() {
        return new ProjectEvent();
    }

}
