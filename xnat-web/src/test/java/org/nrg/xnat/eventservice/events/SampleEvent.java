package org.nrg.xnat.eventservice.events;

import com.google.common.reflect.TypeToken;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xft.security.UserI;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@XnatEventServiceEvent(name = "SomethingHappenedEvent")
public class SampleEvent implements EventServiceEvent {

    private Status status;
    private Class object;
    private String eventUser;
    Date eventDetectedTimestamp = new Date();
    UUID eventUUID = UUID.randomUUID();
    public enum Status {CREATED, UPDATED, DELETED};

    private final TypeToken<String> typeToken = new TypeToken<String>(getClass()) { };

    public SampleEvent(){};

    public SampleEvent(final Class object, final String eventUser, final Status status){
        this.object = object;
        this.eventUser = eventUser;
        this.status = status;
    }

    @Override
    public String getType() {
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
    public Object getObject(UserI user) {
        return Object.class;
    }

    @Override
    public Class getObjectClass() { return typeToken.getRawType(); }

    @Override
    public String getPayloadXnatType() {
        return "xnat:someXnatDataType";
    }

    @Override
    public Boolean isPayloadXsiType() {
        return false;
    }

    @Override
    public Boolean filterablePayload() {
        return false;
    }

    @Override
    public String getPayloadSignatureObject() {
        return null;
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

    @Override
    public List<String> getStatiStates() { return Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList()); }

    @Override
    public Enum getCurrentStatus() {
        return status;
    }

    @Override
    public String getProjectId() { return null; }

    @Override
    public List<EventScope> getEventScope() {return Arrays.asList(EventScope.PROJECT);}
}