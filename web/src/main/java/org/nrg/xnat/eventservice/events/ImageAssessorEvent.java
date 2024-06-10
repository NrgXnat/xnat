package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name="ImageAssessorEvent")
public class ImageAssessorEvent extends AbstractEventServiceEvent<XnatImageassessordataI> {

    public enum Status {CREATED, UPDATED};

    private final String displayName = "Image Assessor";
    private final String description = "Image assessor created.";

    public ImageAssessorEvent(){};

    public ImageAssessorEvent(final XnatImageassessordataI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
        payloadId = payload.getId();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public XnatImageassessordataI getObject(UserI user) {
        return XnatImageassessordata.getXnatImageassessordatasById(payloadId, user, false);
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

}
