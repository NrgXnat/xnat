package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@XnatEventServiceEvent(name="ResourceEvent")
public class ResourceEvent extends CombinedEventServiceEvent<ResourceEvent, XnatResourcecatalogI>  {

    public enum Status {CREATED};

    final String displayName = "Resource Status Change";
    final String description ="New resource catalog saved to session.";

    public ResourceEvent(){};

    public ResourceEvent(final XnatResourcecatalogI payload, final String eventUser, final Status status, final String projectId) {
        super(payload, eventUser, status, projectId);
    }

    @Override
    public String getDisplayName() { return displayName; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getPayloadXnatType() {
        return "xnat:resourceCatalog";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return true;
    }

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public EventServiceListener getInstance() {
        return new ResourceEvent();
    }

}
