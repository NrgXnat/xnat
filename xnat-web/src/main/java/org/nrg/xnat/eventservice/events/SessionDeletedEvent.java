package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SessionDeletedEvent")
public class SessionDeletedEvent extends CombinedEventServiceEvent<SessionDeletedEvent, XnatImagesessiondataI> {


    public SessionDeletedEvent(){};

    public SessionDeletedEvent(XnatImagesessiondataI payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Session Deleted";
    }

    @Override
    public String getDescription() {
        return "Session Deleted Event";
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
        return new SessionDeletedEvent();
    }
}
