package org.nrg.xnat.eventservice.events;

import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

// ** Extend this class to implement a Reactor Event and Listener in one class ** //
@Slf4j
public abstract class CombinedEventServiceEvent<EventObjectT>
        implements EventServiceEvent<EventObjectT> {

    String eventUser;
    EventObjectT object;
    UUID eventUUID = UUID.randomUUID();
    Date eventCreatedTimestamp = new Date();
    Enum status = null;
    String projectId = null;
    String xsiType = null;

    private final TypeToken<EventObjectT> eventObjectTTypeToken = new TypeToken<EventObjectT>(getClass()) { };


    public CombinedEventServiceEvent() {}

    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status) {
        this(object, eventUser, status, null);
    }

    @Deprecated
    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status, final String projectId) {
        this.object = object;
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = new Date();
        this.status = status;
        this.projectId = projectId;
    }

    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status, final String projectId, final String xsiType) {
        this.object = object;
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = new Date();
        this.status = status;
        this.projectId = projectId;
        this.xsiType = xsiType;
    }

    @Override
    public String getType() { return this.getClass().getCanonicalName(); }

    @Override
    public  EventObjectT getObject() {
        return object;
    }


    @Override
    public Class getObjectClass() { return eventObjectTTypeToken.getRawType(); }

    @Override
    public String getPayloadXnatType() { return xsiType; }

    public void setPayloadXnatType(String xsiType) {this.xsiType = xsiType;}

    @Override
    public String getUser() {
        return eventUser;
    }

    @Override
    public Date getEventTimestamp() {
        return eventCreatedTimestamp;
    }

    @Override
    public UUID getEventUUID() {
        return eventUUID;
    }

    @Override
    public Enum getCurrentStatus() {
        return status;
    }

    @Override
    public String getProjectId(){
        return projectId;
    }

    @Override
    public Boolean filterablePayload() { return false;}

    @Override
    public Object getPayloadSignatureObject() {return null;}


    @Override
    public List<EventScope> getEventScope() { return Arrays.asList(EventScope.PROJECT, EventScope.SITE); }

    @Override
    public String toString() {
        return "CombinedEventServiceEvent{" +
                "eventUser='" + eventUser + '\'' +
                ", object=" + (object != null ? object.getClass().getSimpleName() : "null") +
                ", eventCreatedTimestamp=" + (eventCreatedTimestamp != null ? eventCreatedTimestamp.toString() : "null") +
                ", status=" + (status != null ? status.toString() : "null") +
                ", projectId='" + projectId + '\'' +
                '}';
    }

    public static EventServiceEvent createFromResource(org.springframework.core.io.Resource resource)
            throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        EventServiceEvent event = null;
        final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
        Class<?> clazz = Class.forName(properties.get(XnatEventServiceEvent.EVENT_CLASS).toString());
        if (EventServiceEvent.class.isAssignableFrom(clazz) &&
                !clazz.isInterface() &&
                !Modifier.isAbstract(clazz.getModifiers())) {
            try {
                event = (EventServiceEvent) clazz.getConstructor().newInstance();
            } catch (NoSuchMethodException e){
              throw new NoSuchMethodException("Can't find default constructor for " + clazz.getName());
            }
        }
        return event;
    }

}
