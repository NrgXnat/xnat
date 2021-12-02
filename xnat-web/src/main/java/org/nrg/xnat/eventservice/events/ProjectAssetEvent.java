package org.nrg.xnat.eventservice.events;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatAbstractprojectassetI;
import org.nrg.xdat.om.XnatAbstractprojectasset;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@XnatEventServiceEvent(name="ProjectAsset")
public class ProjectAssetEvent extends AbstractEventServiceEvent<XnatAbstractprojectassetI> {

    public enum Status {CREATED};

    private final String displayName = "Project Asset";
    private final String description = "Project Asset created.";
    private String payloadId = null;

    public ProjectAssetEvent(){};

    public ProjectAssetEvent(final XnatAbstractprojectassetI payload, final String eventUser, final Status status, final String projectId) {
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
    public XnatAbstractprojectassetI getObject(UserI user) {
        return XnatAbstractprojectasset.getXnatAbstractprojectassetsById(payloadId, user, false);
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

}
