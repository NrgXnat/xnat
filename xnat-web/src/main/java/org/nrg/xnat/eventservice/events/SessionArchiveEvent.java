package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SessionArchiveEvent")
public class SessionArchiveEvent extends CombinedEventServiceEvent<SessionArchiveEvent, XnatImagesessiondataI> {


    public SessionArchiveEvent(){};

    public SessionArchiveEvent(XnatImagesessiondataI payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Session Archived";
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
