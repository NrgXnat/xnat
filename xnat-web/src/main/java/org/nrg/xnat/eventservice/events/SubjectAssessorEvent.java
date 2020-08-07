package org.nrg.xnat.eventservice.events;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectassessordataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@XnatEventServiceEvent(name="SubjectAssessorEvent")
public class SubjectAssessorEvent extends CombinedEventServiceEvent<SubjectAssessorEvent, XnatSubjectassessordataI> {

    public enum Status {CREATED, DELETED};

    public SubjectAssessorEvent(){};

    public SubjectAssessorEvent(final XnatSubjectassessordataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
    }

    @Override
    public String getDisplayName() {
        return "Subject Assessor Event";
    }

    @Override
    public String getDescription() {
        return "Subject Assessor created or deleted.";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new SubjectAssessorEvent();
    }
}
