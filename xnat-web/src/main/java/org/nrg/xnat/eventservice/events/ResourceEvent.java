package org.nrg.xnat.eventservice.events;


import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ResourceEvent")
public class ResourceEvent extends AbstractEventServiceEvent<XnatResourcecatalogI> {

    public enum Status {CREATED, UPDATED};

    private final String displayName = "Resource";
    private final String description = "Resource created.";

    public ResourceEvent(){};

    public ResourceEvent(final XnatResourcecatalogI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = Integer.toString(payload.getXnatAbstractresourceId());
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public XnatResourcecatalogI getObject(UserI user) {
        return XnatResourcecatalog.getXnatResourcecatalogsByXnatAbstractresourceId(Integer.valueOf(payloadId), user, false);
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

}