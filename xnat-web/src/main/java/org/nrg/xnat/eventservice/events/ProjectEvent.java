package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ProjectEvent")
public class ProjectEvent extends AbstractEventServiceEvent<XnatProjectdataI> {

    public enum Status {CREATED, DELETED, SCHEDULED};

    private final String displayName = "Project";
    private final String description = "Project created or deleted.";
    private String payloadId = null;

    public ProjectEvent(){};

    public ProjectEvent(final XnatProjectdataI payload, final String eventUser, final Status status) {
        super(payload, eventUser, status, payload.getId(), (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getId();
    }

    public ProjectEvent(final XnatProjectdataI payload, final String eventUser, final Status status, final Long subscriptionId) {
        super(payload, eventUser, status, payload.getId(), (payload != null ? payload.getXSIType() : null), subscriptionId);
        payloadId = payload.getId();
    }

    @Override
    public XnatProjectdataI getObject(UserI user) {
        return XnatProjectdata.getXnatProjectdatasById(payloadId, user, false);
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
