package org.nrg.xnat.eventservice.events;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectassessordata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@XnatEventServiceEvent(name="SubjectAssessorEvent")
public class SubjectAssessorEvent extends AbstractEventServiceEvent<XnatSubjectassessordataI> {

    public enum Status {CREATED, DELETED, SCHEDULED};

    private final String displayName = "Subject Assessor";
    private final String description = "Subject Assessor created or deleted.";
    private String payloadId = null;

    public SubjectAssessorEvent(){};

    public SubjectAssessorEvent(final XnatSubjectassessordataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getId();
    }

    public SubjectAssessorEvent(final XnatSubjectassessordataI payload, final String eventUser, final Status status, final String projectId, final Long subscriptionId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null), subscriptionId);
        payloadId = payload.getId();
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public XnatSubjectassessordataI getObject(UserI user) {
        XnatSubjectassessordataI xnatSubjectassessordata = XnatSubjectassessordata.getXnatSubjectassessordatasById(payloadId, user, false);
        if(xnatSubjectassessordata != null){
            return xnatSubjectassessordata;
        }

        XnatExperimentdata xnatExperimentdata = XnatExperimentdata.getXnatExperimentdatasById(payloadId, user, false);
        return xnatExperimentdata instanceof XnatSubjectassessordataI ?
                (XnatSubjectassessordataI) xnatExperimentdata :
                null;
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }
}
