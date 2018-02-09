package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SubjectCreatedEvent")
public class SubjectCreatedEvent extends CombinedEventServiceEvent<SubjectCreatedEvent, XnatSubjectdata> {

    public SubjectCreatedEvent(){};

    public SubjectCreatedEvent(XnatSubjectdata payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Subject Created";
    }

    @Override
    public String getDescription() {
        return "Subject Created Event";
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
    public EventServiceListener getInstance() {
        return new SubjectCreatedEvent();
    }
}
