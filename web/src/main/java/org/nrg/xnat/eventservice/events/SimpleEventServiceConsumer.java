package org.nrg.xnat.eventservice.events;

import org.nrg.xnat.eventservice.listeners.EventServiceListener;
import org.nrg.xnat.eventservice.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;

@Deprecated
@Service
public abstract class SimpleEventServiceConsumer<EventT> implements EventServiceListener<EventT>{

    @Autowired
    EventService eventService;

    Long id = 0L;

    public SimpleEventServiceConsumer(){}

    public SimpleEventServiceConsumer(long id){
        this.id = id;
    }

    @Override
    public String getEventType() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public void accept(Event<EventT> event) {
        eventService.processEvent(this, event);
    }

    public abstract EventServiceListener Builder(long id);
}
