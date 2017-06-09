package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(
        name="SubjectCreatedEvent",
        displayName = "Subject Created Event",
        description="Subject Created Event",
        object = "Subject",
        operation = "Created")
public class SubjectCreatedEvent extends CombinedEventServiceEvent<SubjectCreatedEvent, XnatSubjectdata> {

    public SubjectCreatedEvent(){};

    public SubjectCreatedEvent(XnatSubjectdata payload, Integer eventUserId) {
        super(payload, eventUserId);
    }


    @Override
    public String getDisplayName() {
        return "SubjectCreatedEvent";
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
