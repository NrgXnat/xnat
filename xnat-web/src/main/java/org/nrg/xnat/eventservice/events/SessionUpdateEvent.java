package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SessionUpdateEvent")
public class SessionUpdateEvent extends CombinedEventServiceEvent<SessionUpdateEvent, XnatImagesessiondataI> {


    public SessionUpdateEvent(){};

    public SessionUpdateEvent(XnatImagesessiondataI payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Session Updated";
    }

    @Override
    public String getDescription() {
        return "Session Update Event";
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
        return new SessionUpdateEvent();
    }
}
