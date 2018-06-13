package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="ImageAssessorSaveEvent")
public class ImageAssessorSaveEvent extends CombinedEventServiceEvent<SessionUpdateEvent, XnatImageassessordataI> {

    public ImageAssessorSaveEvent(){};

    public ImageAssessorSaveEvent(XnatImageassessordataI payload, String eventUser) {
        super(payload, eventUser);
    }


    @Override
    public String getDisplayName() {
        return "Image Assessor Saved";
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
    public EventServiceListener getInstance() {
        return new ImageAssessorSaveEvent();
    }
}
