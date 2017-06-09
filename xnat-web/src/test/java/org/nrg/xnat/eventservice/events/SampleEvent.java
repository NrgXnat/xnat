package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;

@XnatEventServiceEvent(name="SampleEvent")
public class SampleEvent implements EventServiceEvent {

    @Override
    public String getId() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public String getObjectClass() {
        return null;
    }

    @Override
    public String getPayloadXnatType() {
        return null;
    }

    @Override
    public Boolean isPayloadXsiType() {
        return null;
    }

    @Override
    public Integer getUser() {
        return null;
    }
}