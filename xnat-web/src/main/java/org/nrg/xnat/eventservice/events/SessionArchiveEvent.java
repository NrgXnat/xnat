package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(
        name="SessionArchiveEvent",
        displayName = "Session Archived",
        description="Session Archive Event",
        object = "Session",
        operation = "Archived")
public class SessionArchiveEvent extends CombinedEventServiceEvent<SessionArchiveEvent, XnatImagesessiondata> {


    public SessionArchiveEvent(){};

    public SessionArchiveEvent(XnatImagesessiondata payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "SessionArchiveEvent";
    }

    @Override
    public String getDescription() {
        return "Session Archive Event";
    }

    @Override
    public String getPayloadXnatType() {
        return "xnat:imageSessionData";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public EventServiceListener getInstance() {
        return new SessionArchiveEvent();
    }
}
