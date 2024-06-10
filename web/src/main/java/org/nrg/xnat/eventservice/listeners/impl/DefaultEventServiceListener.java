package org.nrg.xnat.eventservice.listeners.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.bus.Event;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class DefaultEventServiceListener implements EventServiceListener<DefaultEventServiceListener> {

    @Autowired @Lazy EventService eventService;

    UUID listenerId = UUID.randomUUID();
    Date eventDetectedTimestamp = null;

    public void setEventService(EventService eventService){
        this.eventService = eventService;
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
    public Date getDetectedTimestamp() {
        return eventDetectedTimestamp;
    }

    @Override
    public void accept(Event<DefaultEventServiceListener> event){
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
    public EventServiceListener getInstance() {
        return new DefaultEventServiceListener();
    }

}
