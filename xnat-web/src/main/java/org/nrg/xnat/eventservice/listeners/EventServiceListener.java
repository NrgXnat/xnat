package org.nrg.xnat.eventservice.listeners;


import org.nrg.xnat.eventservice.services.EventService;
import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.UUID;

public interface EventServiceListener<T> extends Consumer<Event<T>> {
    String getId();
    String getEventType();
    EventServiceListener getInstance();
    UUID getInstanceId();
    void setEventService(EventService eventService);
}
