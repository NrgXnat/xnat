package org.nrg.xnat.eventservice.listeners;

import org.nrg.xnat.eventservice.events.EventServiceEvent;
import org.nrg.xnat.eventservice.services.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.bus.Event;

import java.util.Date;
import java.util.UUID;

@Service
public class TestListener implements EventServiceListener<EventServiceEvent> {

    UUID listenerId = UUID.randomUUID();
    Date eventDetectedTimestamp = null;

    EventService eventService;

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public String getId() { return this.getClass().getCanonicalName(); }

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

    @Override
    public void accept(Event<EventServiceEvent> event) {
        if (event.getData() instanceof EventServiceEvent)
            this.eventDetectedTimestamp = new Date();
            if(eventService != null) {
                eventService.processEvent(this, event);
            } else {
                log.error("Event Listener: {} is missing reference to eventService. Should have been set at activation.", this.getClass().getCanonicalName());
            }
    }
}
