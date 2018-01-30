package org.nrg.xnat.eventservice.events;

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
@Service
public abstract class CombinedEventServiceEvent<EventT extends EventServiceEvent, EventObjectT>
        implements EventServiceEvent<EventObjectT>, EventServiceListener<EventT> {

    String eventUser;
    EventObjectT object;
    UUID eventUUID = UUID.randomUUID();
    Date eventCreatedTimestamp = null;
    UUID listenerId = UUID.randomUUID();
    Date eventDetectedTimestamp = null;


    @Autowired @Lazy
    EventService eventService;

    public CombinedEventServiceEvent() {}

    public CombinedEventServiceEvent(final EventObjectT object, final String eventUser) {
        this.object = object;
        this.eventUser = eventUser;
        this.eventCreatedTimestamp = new Date();
    }

    @Override
    public String getId() { return this.getClass().getCanonicalName(); }

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
    public String getObjectClass() {
        return getObject() == null ? null : getObject().getClass().getCanonicalName();
    }

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
    public void accept(Event<EventT> event){
        if( event.getData() instanceof EventServiceEvent) {
            this.eventDetectedTimestamp = new Date();
            eventService.processEvent(this, event);
        }
    }


    public static CombinedEventServiceEvent createFromResource(org.springframework.core.io.Resource resource)
            throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        CombinedEventServiceEvent event = null;
        final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
        Class<?> clazz = Class.forName(properties.get(XnatEventServiceEvent.EVENT_CLASS).toString());
        if (CombinedEventServiceEvent.class.isAssignableFrom(clazz) &&
                !clazz.isInterface() &&
                !Modifier.isAbstract(clazz.getModifiers())) {
            try {
                event = (CombinedEventServiceEvent) clazz.getConstructor().newInstance();
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
