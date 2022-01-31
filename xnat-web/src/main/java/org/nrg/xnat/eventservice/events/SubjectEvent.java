package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="SubjectEvent")
public class SubjectEvent extends AbstractEventServiceEvent<XnatSubjectdataI> {

    public enum Status {CREATED, DELETED, SCHEDULED};

    private final String displayName = "Subject";
    private final String description = "Subject created, updated, or deleted.";
    private String payloadId = null;

    public SubjectEvent(){};

    public SubjectEvent(final XnatSubjectdataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getId();
    }

    public SubjectEvent(final XnatSubjectdataI payload, final String eventUser, final Status status, final String projectId, Long subscriptionId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null), subscriptionId);
        payloadId = payload.getId();
    }

    @Override
    public XnatSubjectdataI getObject(UserI user) {
        return XnatSubjectdata.getXnatSubjectdatasById(payloadId, user, false);
    }

    @Override
    public Class getObjectClass() { return XnatSubjectdata.class;}

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
