package org.nrg.xnat.eventservice.events;


import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatAbstractprojectassetI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@XnatEventServiceEvent(name="ProjectAsset")
public class ProjectAssetEvent extends CombinedEventServiceEvent<ProjectAssetEvent, XnatAbstractprojectassetI> {

    public enum Status {CREATED};

    public ProjectAssetEvent(){};

    public ProjectAssetEvent(final XnatAbstractprojectassetI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId, (payload != null ? payload.getXSIType() : null));
    }

    @Override
    public String getDisplayName() {
        return "Project Asset Event";
    }

    @Override
    public String getDescription() {
        return "Project Asset created.";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new ProjectAssetEvent();
    }
}
