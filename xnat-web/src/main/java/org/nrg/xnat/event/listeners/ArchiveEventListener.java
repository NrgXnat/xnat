/*
 * web: org.nrg.xnat.event.listeners.AutomationCompletionEventListener
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.framework.status.StatusProducerI;
import org.nrg.xnat.event.EventListener;
import org.nrg.xnat.event.archive.ArchiveEventI;
import org.nrg.xnat.tracking.TrackEvent;
import reactor.bus.Event;
import reactor.fn.Consumer;

import java.util.Collection;
import java.util.Map;

/**
 * Handles events fired as archive operations occur.
 */
@EventListener
@Slf4j
public class ArchiveEventListener implements Consumer<Event<ArchiveEventI>>, StatusProducerI {
    /**
     * {@inheritDoc}
     */
    @Override
    @TrackEvent
    public void accept(final Event<ArchiveEventI> busEvent) {
        log.debug("Received event {} for session archive event {}", busEvent.getId(), busEvent.getData());
        final ArchiveEventI event          = busEvent.getData();
        final String        archiveEventId = event.getArchiveEventId();
        final StatusMessage sm = new StatusMessage(busEvent.getId(), event.getStatus().status(), event.getMessage());
        for (final ArchiveOperationListener listener : _listeners.get(archiveEventId)) {
            log.debug("Notifying listener {} of event {}: {}) {}", listener.getArchiveOperationId(), busEvent.getId(), event.getStatus().status(), event.getMessage());
            listener.notify(sm);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStatusListener(final StatusListenerI statusListener) {
        // We're only interested in listeners listening for archive events.
        if (!ArchiveOperationListener.class.isAssignableFrom(statusListener.getClass())) {
            return;
        }
        final ArchiveOperationListener listener = (ArchiveOperationListener) statusListener;
        log.debug("Adding status listener {}: {}", listener.getArchiveOperationId(), listener);
        _listeners.put(listener.getArchiveOperationId(), listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeStatusListener(final StatusListenerI statusListener) {
        // We're only interested in listeners listening for archive events.
        if (!ArchiveOperationListener.class.isAssignableFrom(statusListener.getClass())) {
            return;
        }
        final ArchiveOperationListener listener = (ArchiveOperationListener) statusListener;
        log.debug("Removing status listener {}: {}", listener.getArchiveOperationId(), listener);
        _listeners.remove(listener.getArchiveOperationId(), listener);
    }

    /**
     * Returns the collection of {@link ArchiveOperationListener currently attached listeners}.
     *
     * @return A map of {@link ArchiveOperationListener currently attached listeners}, grouped by the {@link ArchiveOperationListener#getArchiveOperationId() archive operation ID}.
     */
    @SuppressWarnings("unused")
    public Map<String, Collection<ArchiveOperationListener>> getStatusListeners() {
        return _listeners.asMap();
    }

    private final Multimap<String, ArchiveOperationListener> _listeners = ArrayListMultimap.create();
}
