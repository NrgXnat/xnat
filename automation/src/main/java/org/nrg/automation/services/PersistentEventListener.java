/*
 * automation: org.nrg.automation.services.PersistentEventListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import groovy.util.logging.Slf4j;
import org.nrg.automation.event.AutomationEventImplementerI;
import org.nrg.framework.event.StructuredEventI;
import org.nrg.framework.event.persist.PersistentEventImplementerI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import static reactor.bus.selector.Selectors.type;

/**
 * The listener interface for receiving persistentEvent events.
 * The class that is interested in processing a persistentEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addPersistentEventListener</code> method. When
 * the persistentEvent event occurs, that object's appropriate
 * method is invoked.
 */
@Service
@Slf4j
public class PersistentEventListener implements Consumer<Event<PersistentEventImplementerI>> {
    private final PersistentEventService _persistentEventService;
    private final boolean                _automationEnabled;

    /**
     * Instantiates a new persistent event listener.
     *
     * @param eventBus the event bus
     */
    @Autowired
    public PersistentEventListener(final AutomationService automationService, final EventBus eventBus, final PersistentEventService persistentEventService) {
        _persistentEventService = persistentEventService;
        _automationEnabled      = automationService.getAutomationEnabled();
        eventBus.on(type(PersistentEventImplementerI.class), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Event<PersistentEventImplementerI> event) {
        if (!_automationEnabled) {
            return;
        }

        // Let the automation event do the persisting if this is an AutomationEventImplementer
        //if (AutomationEventImplementerI.class.isAssignableFrom(event.getData().getClass())) {
        if (event.getData() instanceof AutomationEventImplementerI) {
            return;
        }
        StructuredEventI persistentEvent = event.getData();
        if (persistentEvent != null) {
            _persistentEventService.create(event.getData());
        }
    }
}
