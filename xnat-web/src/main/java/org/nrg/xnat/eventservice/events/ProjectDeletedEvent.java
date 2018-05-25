package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="ProjectDeletedEvent")
public class ProjectDeletedEvent extends CombinedEventServiceEvent<ProjectDeletedEvent, XnatProjectdataI>  {
    final String displayName = "Project Deleted";
    final String description ="Project deleted.";

    public ProjectDeletedEvent(){};

    public ProjectDeletedEvent(final XnatProjectdataI payload, final String eventUser) {
        super(payload, eventUser);
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
    public EventServiceListener getInstance() {
        return new ProjectDeletedEvent();
    }

}
