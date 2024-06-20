/*
 * web: org.nrg.xnat.event.listeners.AutomationCompletionEventListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.automation.event.entities.AutomationCompletionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static reactor.bus.selector.Selectors.type;

/**
 * The Class AutomatedScriptHandler.
 */
@Service
@Slf4j
public class AutomationCompletionEventListener implements Consumer<Event<AutomationCompletionEvent>> {
    /**
     * cache of completed events
     */
    private final List<AutomationCompletionEvent> completedCache = new ArrayList<>();
    /**
     * HOW LONG WILL WE LET OBJECTS STAY IN CACHE?
     */
    int CACHE_TIME_MILLIS = 60000;

    /**
     * Instantiates a new automated script handler.
     *
     * @param eventBus the event bus
     */
    @Autowired
    public AutomationCompletionEventListener(final EventBus eventBus) {
        eventBus.on(type(AutomationCompletionEvent.class), this);
    }

    @Override
    public void accept(Event<AutomationCompletionEvent> event) {
        cleanUpCache();
        log.debug("Received event {} - CURRENT TIME: {}", event.getId(), System.currentTimeMillis());
        if (event.getData().getEventCompletionTime() == null) {
            if (log.isDebugEnabled()) {
                log.debug("WARNING:  AutomationCompletionEvent - eventCompletionTime is null");
            }
        }
        completedCache.add(event.getData());
    }

    private synchronized void cleanUpCache() {
        final Iterator<AutomationCompletionEvent> i = completedCache.iterator();
        final long currentTime = System.currentTimeMillis();
        while (i.hasNext()) {
            final AutomationCompletionEvent thisEvent = i.next();
            if (thisEvent.getEventCompletionTime() == null || ((currentTime - thisEvent.getEventCompletionTime()) > CACHE_TIME_MILLIS)) {
                log.debug("cleanUpCache - removed item {} - CURRENT TIME: {}", thisEvent.getId(), currentTime);
                i.remove();
            }
        }
    }

    public synchronized AutomationCompletionEvent getEvent(String id) {
        final Iterator<AutomationCompletionEvent> i = completedCache.iterator();
        while (i.hasNext()) {
            final AutomationCompletionEvent thisEvent = i.next();
            if (thisEvent.getId().equals(id)) {
                i.remove();
                return thisEvent;
            }
        }
        log.debug("getEvent - item not found {}", id);
        return null;
    }

}
