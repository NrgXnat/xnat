package org.nrg.xnat.eventservice.events;

import org.nrg.framework.event.XnatEventServiceEvent;

import java.util.Date;
import java.util.UUID;

@XnatEventServiceEvent(name = "SomethingHappenedEvent")
public class SampleEvent implements EventServiceEvent {

    private Class object;
    private String eventUser;
    Date eventDetectedTimestamp = new Date();
    UUID eventUUID = UUID.randomUUID();

    public SampleEvent(){};

    public SampleEvent(final Class object, final String eventUser){
        this.object = object;
        this.eventUser = eventUser;
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
        return "xnat:someXnatDataType";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return false;
    }

    @Override
    public String getUser() {
        return eventUser;
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