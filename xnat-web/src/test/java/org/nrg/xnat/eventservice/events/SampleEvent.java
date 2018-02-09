package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;

import java.util.Date;
import java.util.UUID;

@XnatEventServiceEvent
public class SampleEvent implements EventServiceEvent {

    Date eventDetectedTimestamp;
    UUID eventUUID = UUID.randomUUID();

    public SampleEvent() {
        eventDetectedTimestamp = new Date();
    }

    @Override

    public String getId() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public String getDisplayName() {
        return "Sample Event";
    }

    @Override
    public String getDescription() {
        return "Sample Event for Integration Testing";
    }

    @Override
    public Object getObject() {
        return Object.class;
    }

    @Override
    public String getObjectClass() { return getObject().getClass().getName(); }

    @Override
    public String getPayloadXnatType() {
        return null;
    }

    @Override
    public Boolean isPayloadXsiType() {
        return false;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public Date getEventTimestamp() {
        return eventDetectedTimestamp;
    }

    @Override
    public UUID getEventUUID() {
        return eventUUID;
    }
}