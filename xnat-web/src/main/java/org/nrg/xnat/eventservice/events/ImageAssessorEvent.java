package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="ImageAssessorEvent")
public class ImageAssessorEvent extends CombinedEventServiceEvent<SessionEvent, XnatImageassessordataI> {

    public enum Status {CREATED};

    public ImageAssessorEvent(){};

    public ImageAssessorEvent(final XnatImageassessordataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
    }

    @Override
    public String getDisplayName() {
        return "Image Assessor Event";
    }

    @Override
    public String getDescription() {
        return "Image assessor created.";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new ImageAssessorEvent();
    }
}
