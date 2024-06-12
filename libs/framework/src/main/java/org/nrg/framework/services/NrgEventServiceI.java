package org.nrg.framework.services;

import org.nrg.framework.event.EventI;
import reactor.bus.Bus;
import reactor.bus.Event;

@SuppressWarnings("unused")
public interface NrgEventServiceI {
    void triggerEvent(String description, EventI event, boolean notifyClassListeners);

    void triggerEvent(String description, EventI event);

    void triggerEvent(EventI event);

    void triggerEvent(EventI event, Object replyTo);

    void triggerEvent(String description, Event event, boolean notifyClassListeners);

    void triggerEvent(String description, Event event);

    void triggerEvent(Event event);

    void sendEvent(Event event);

    void sendEvent(String description, EventI event, Bus replyTo, boolean notifyClassListeners);

    void sendEvent(String description, EventI event, Bus replyTo);

    void sendEvent(EventI event, Bus replyTo);
}
