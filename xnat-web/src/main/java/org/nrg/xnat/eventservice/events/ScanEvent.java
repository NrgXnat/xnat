package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@XnatEventServiceEvent(name="ScanEvent")
public class ScanEvent extends CombinedEventServiceEvent<ScanEvent, XnatImagescandataI>  {

    public enum Status {CREATED};

    public ScanEvent(){};

    public ScanEvent(final XnatImagescandataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() {
        return "Scan Status Change";
    }

    @Override
    public String getDescription() {
        return "New scan saved";
    }

    @Override
    public String getPayloadXnatType() {
        return "xnat:imageScanData";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public EnumSet getStatiStates() { return EnumSet.allOf(SessionEvent.Status.class); }

    @Override
    public EventServiceListener getInstance() {
        return new ScanEvent();
    }
}