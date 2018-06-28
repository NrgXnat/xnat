package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Service
@XnatEventServiceEvent(name="ImageAssessorEvent")
public class ImageAssessorEvent extends CombinedEventServiceEvent<SessionEvent, XnatImageassessordataI> {

    public enum Status {CREATED};

    public ImageAssessorEvent(){};

    public ImageAssessorEvent(final XnatImageassessordataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() {
        return "Image Assessor Status Change";
    }

    @Override
    public String getDescription() {
        return "Image assessor saved.";
    }

    @Override
    public String getPayloadXnatType() {
        return "xnat:imageAssessorData";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public EnumSet getStatiStates() { return EnumSet.allOf(SessionEvent.Status.class); }

    @Override
    public EventServiceListener getInstance() {
        return new ImageAssessorEvent();
    }
}
