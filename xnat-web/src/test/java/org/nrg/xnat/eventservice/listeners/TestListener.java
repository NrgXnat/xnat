package org.nrg.xnat.eventservice.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.stereotype.Service;
import reactor.bus.Event;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class TestListener implements EventServiceListener<EventServiceEvent> {

    UUID listenerId = UUID.randomUUID();
    Date eventDetectedTimestamp = null;

    EventService eventService;

    @Override
    public String getType() { return this.getClass().getCanonicalName(); }

    @Override
    public String getEventType() {
        return null;
    }

    @Override
    public EventServiceListener getInstance() {
        return new TestListener();
    }

    @Override
    public UUID getInstanceId() {
        return listenerId;
    }

    @Override
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Override
    public Date getDetectedTimestamp() {
        return eventDetectedTimestamp;
    }

    public void clearDetectedTimestamp(){
        eventDetectedTimestamp = null;
    }

    @Override
    public void accept(Event<EventServiceEvent> event) {
        this.eventDetectedTimestamp = new Date();
        if (event.getData() instanceof EventServiceEvent) {
            if (eventService != null) {
                eventService.processEvent(this, event);
            } else {
                log.error("Event Listener: {} is missing reference to eventService. Should have been set at activation.", this.getClass().getCanonicalName());
            }
        }
        synchronized (this) {
            notifyAll();
        }
    }
}
