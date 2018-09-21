package org.nrg.xnat.eventservice.events;

import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.event.XnatEventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Service;
import reactor.bus.Event;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

// ** Extend this class to implement a Reactor Event and Listener in one class ** //
@Slf4j
@Service
public abstract class CombinedEventServiceEvent<EventT extends EventServiceEvent, EventObjectT>
        implements EventServiceEvent<EventObjectT>, EventServiceListener<EventT> {

    String eventUser;
    EventObjectT object;
    UUID eventUUID = UUID.randomUUID();
    Date eventCreatedTimestamp = new Date();
    UUID listenerId = UUID.randomUUID();
    Date eventDetectedTimestamp = null;
    Enum status = null;
    String projectId = null;

    private final TypeToken<EventObjectT> eventObjectTTypeToken = new TypeToken<EventObjectT>(getClass()) { };


    @Autowired @Lazy
    EventService eventService;

    public CombinedEventServiceEvent() {}

    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status) {
        this(object, eventUser, status, null);
    }

    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser, final Enum status, final String projectId) {
        this.object = object;
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = new Date();
        this.status = status;
        this.projectId = projectId;
    }

    @Override
    public String getType() { return this.getClass().getCanonicalName(); }

    @Override
    public UUID getInstanceId() {return listenerId;}

    @Override
    public String getEventType() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public  EventObjectT getObject() {
        return object;
    }


    @Override
    public Class getObjectClass() { return eventObjectTTypeToken.getRawType(); }

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
    public Date getDetectedTimestamp() {
        return eventDetectedTimestamp;
    }

    public void setEventService(EventService eventService){
        this.eventService = eventService;
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
    public void accept(Event<EventT> event){
        if( event.getData() instanceof EventServiceEvent) {
            this.eventDetectedTimestamp = new Date();
            if(eventService != null) {
                eventService.processEvent(this, event);
            } else {
                log.error("Event Listener: {} is missing reference to eventService. Should have been set at activation.", this.getClass().getCanonicalName());
            }
        }
    }

    @Override
    public String toString() {
        return "CombinedEventServiceEvent{" +
                "eventUser='" + eventUser + '\'' +
                ", object=" + object.getClass().getSimpleName() +
                ", eventCreatedTimestamp=" + eventCreatedTimestamp.toString() +
                ", status=" + status.toString() +
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
//                event.setDisplayName(properties.containsKey(XnatEventServiceEvent.EVENT_DISPLAY_NAME) ? properties.get(XnatEventServiceEvent.EVENT_DISPLAY_NAME).toString() : "");
//                event.setDescription(properties.containsKey(XnatEventServiceEvent.EVENT_DESC) ? properties.get(XnatEventServiceEvent.EVENT_DESC).toString() : "");
//                event.setEventObject(properties.containsKey(XnatEventServiceEvent.EVENT_OBJECT) ? properties.get(XnatEventServiceEvent.EVENT_OBJECT).toString() : "");
//                event.setEventOperation(properties.containsKey(XnatEventServiceEvent.EVENT_OPERATION) ? properties.get(XnatEventServiceEvent.EVENT_OPERATION).toString() : "");
            } catch (NoSuchMethodException e){
              throw new NoSuchMethodException("Can't find default constructor for " + clazz.getName());
            }
        }
        return event;
    }

}
