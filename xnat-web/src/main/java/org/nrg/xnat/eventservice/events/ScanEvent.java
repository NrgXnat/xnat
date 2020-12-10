package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagescandataI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ScanEvent")
public class ScanEvent extends CombinedEventServiceEvent<XnatImagescandataI>  {

    public enum Status {CREATED};

    public ScanEvent(){};

    public ScanEvent(final XnatImagescandataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
    }

    @Override
    public String getDisplayName() {
        return "Scan Event";
    }

    @Override
    public String getDescription() {
        return "Scan Created.";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }
}