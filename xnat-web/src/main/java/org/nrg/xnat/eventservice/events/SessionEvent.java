package org.nrg.xnat.eventservice.events;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@XnatEventServiceEvent(name="SessionEvent")
public class SessionEvent extends AbstractEventServiceEvent<XnatImagesessiondataI> {

    public enum Status {CREATED, DELETED};

    private final String displayName = "Session";
    private final String description = "Session created or deleted.";
    private String payloadId = null;

        public SessionEvent(){};

    public SessionEvent(final XnatImagesessiondataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getId();
    }

    @Override
    public XnatImagesessiondataI getObject(UserI user) {
        return XnatImagesessiondata.getXnatImagesessiondatasById(payloadId, user, false);
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
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }
}
