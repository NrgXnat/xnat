/*
 * web: org.nrg.xnat.event.listeners.AutomationCompletionEventListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.Lists;
import org.nrg.automation.event.entities.AutomationCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

import static reactor.bus.selector.Selectors.type;

/**
 * The Class AutomatedScriptHandler.
 */
@Service
public class AutomationCompletionEventListener implements Consumer<Event<AutomationCompletionEvent>> {

    /**
     * The Constant logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AutomationCompletionEventListener.class);
    /**
     * cache of completed events
     */
    private List<AutomationCompletionEvent> completedCache = Lists.newArrayList();
    /**
     * HOW LONG WILL WE LET OBJECTS STAY IN CACHE?
     */
    int CACHE_TIME_MILLIS = 60000;

    /**
     * Instantiates a new automated script handler.
     *
     * @param eventBus the event bus
     */
    @Inject
    public AutomationCompletionEventListener(final EventBus eventBus) {
        eventBus.on(type(AutomationCompletionEvent.class), this);
    }

    @Override
    public void accept(Event<AutomationCompletionEvent> event) {
        cleanUpCache();
        if (logger.isDebugEnabled()) {
            logger.debug("Received event " + event.getId() + " - CURRENT TIME: " + System.currentTimeMillis());
        }
        if (event.getData().getEventCompletionTime() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("WARNING:  AutomationCompletionEvent - eventCompletionTime is null");
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
                if (logger.isDebugEnabled()) {
                    logger.debug("cleanUpCache - removed item " + thisEvent.getId() + " - CURRENT TIME: " + currentTime);
                }
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
        if (logger.isDebugEnabled()) {
            logger.debug("getEvent - item not found " + id);
        }
        return null;
    }

}
