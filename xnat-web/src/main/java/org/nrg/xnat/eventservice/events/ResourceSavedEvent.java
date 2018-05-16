package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.springframework.stereotype.Service;

@Service
@XnatEventServiceEvent(name="ResourceSavedEvent")
public class ResourceSavedEvent extends CombinedEventServiceEvent<ResourceSavedEvent, XnatResourcecatalogI>  {
    final String displayName = "Resource Saved";
    final String description ="New resource catalog saved to session.";

    public ResourceSavedEvent(){};

    public ResourceSavedEvent(final XnatResourcecatalogI payload, final String eventUser) {
        super(payload, eventUser);
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
    public EventServiceListener getInstance() {
        return new ResourceSavedEvent();
    }

}
