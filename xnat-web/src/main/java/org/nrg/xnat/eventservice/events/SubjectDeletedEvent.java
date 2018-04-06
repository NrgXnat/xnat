package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="SubjectDeletedEvent")
public class SubjectDeletedEvent extends CombinedEventServiceEvent<SubjectDeletedEvent, XnatSubjectdataI> {

    public SubjectDeletedEvent(){};

    public SubjectDeletedEvent(XnatSubjectdataI payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Subject Deleted";
    }

    @Override
    public String getDescription() {
        return "Subject Deleted Event";
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
        return new SubjectDeletedEvent();
    }
}
