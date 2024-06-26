package org.nrg.xnat.eventservice.listeners;


import org.nrg.xnat.eventservice.services.EventService;
import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.Date;
import java.util.UUID;

public interface EventServiceListener<T> extends Consumer<Event<T>> {
    String getType();
    String getEventType();
    EventServiceListener getInstance();
    UUID getInstanceId();
    void setEventService(EventService eventService);
    Date getDetectedTimestamp();
}
