package org.nrg.xnat.eventservice.services;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xnat.eventservice.events.CombinedEventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SubjectUpdatedEvent")
public class SubjectUpdatedEvent extends CombinedEventServiceEvent<SubjectUpdatedEvent, XnatSubjectdataI> {

    public SubjectUpdatedEvent(){};

    public SubjectUpdatedEvent(XnatSubjectdataI payload, String eventUser) {
        super(payload, eventUser);
    }

    @Override
    public String getDisplayName() {
        return "Subject Updated";
    }

    @Override
    public String getDescription() {
        return "Subject updated via editing or addition of resource folder.";
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
        return new SubjectUpdatedEvent();
    }
}
