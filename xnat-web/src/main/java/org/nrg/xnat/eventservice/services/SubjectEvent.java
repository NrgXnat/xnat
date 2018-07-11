package org.nrg.xnat.eventservice.services;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xnat.eventservice.events.CombinedEventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="SubjectEvent")
public class SubjectEvent extends CombinedEventServiceEvent<SubjectEvent, XnatSubjectdataI> {

    public enum Status {CREATED, UPDATED, DELETED};

    public SubjectEvent(){};

    public SubjectEvent(final XnatSubjectdataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() {
        return "Subject Status Change";
    }

    @Override
    public String getDescription() {
        return "Subject created, updated, or deleted.";
    }

    @Override
    public String getPayloadXnatType() {
        return "xnat:subjectData";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new SubjectEvent();
    }
}
