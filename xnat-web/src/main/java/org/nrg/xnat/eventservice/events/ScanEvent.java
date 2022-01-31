package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ScanEvent")
public class ScanEvent extends AbstractEventServiceEvent<XnatImagescandataI> {

    public enum Status {CREATED, SCHEDULED};

    private final String displayName = "Scan";
    private final String description = "Scan Created.";
    private Integer payloadId = null;

    public ScanEvent(){};

    public ScanEvent(final XnatImagescandataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getXnatImagescandataId();
    }

    public ScanEvent(final XnatImagescandataI payload, final String eventUser, final Status status, final String projectId, final Long subscriptionId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null), subscriptionId);
        payloadId = payload.getXnatImagescandataId();
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public XnatImagescandataI getObject(UserI user) {
        return XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(payloadId, user, false);
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }
}