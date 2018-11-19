/*
 * framework: org.nrg.framework.services.NrgEventService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.services;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.event.EventI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Bus;
import reactor.bus.Event;
import reactor.bus.EventBus;

import javax.annotation.Nonnull;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.framework.utilities.ExceptionUtils.getStackTraceDisplay;

/**
 * The Class NrgEventService.
 */
@Service
@Slf4j
@Getter(PRIVATE)
@Accessors(prefix = "_")
public class NrgEventService {
    /**
     * Instantiates a new xft event service.
     *
     * @param eventBus The event bus for the service.
     */
    @Autowired
    public NrgEventService(final EventBus eventBus) {
        _eventBus = eventBus;
    }

    /**
     * Trigger event.
     *
     * @param description          the event desc
     * @param event                the event
     * @param notifyClassListeners Notify class listeners?
     */
    public void triggerEvent(final String description, final EventI event, final boolean notifyClassListeners) {
        triggerEventInternal(Event.wrap(event), description, notifyClassListeners);
    }

    /**
     * Trigger event.
     *
     * @param description the event desc
     * @param event       the event
     */
    public void triggerEvent(final String description, final EventI event) {
        triggerEventInternal(Event.wrap(event), description, true);
    }

    /**
     * Trigger event.
     *
     * @param event the event
     */
    public void triggerEvent(final EventI event) {
        triggerEventInternal(Event.wrap(event), null, null);
    }

    /**
     * Trigger event.
     *
     * @param event   the event
     * @param replyTo the reply to
     */
    public void triggerEvent(final EventI event, final Object replyTo) {
        if (replyTo == null) {
            throw new IllegalArgumentException("Event replyTo object cannot be null");
        }
        triggerEventInternal(Event.wrap(event, replyTo), null, null);
    }

    /**
     * Trigger event.
     *
     * @param description          the event desc
     * @param event                the event
     * @param notifyClassListeners Notify class event listeners?
     */
    @SuppressWarnings("rawtypes")
    public void triggerEvent(final String description, final Event event, final boolean notifyClassListeners) {
        triggerEventInternal(event, description, notifyClassListeners);
    }

    /**
     * Trigger event.
     *
     * @param description the event desc
     * @param event       the event
     */
    @SuppressWarnings("rawtypes")
    public void triggerEvent(final String description, final Event event) {
        triggerEventInternal(event, description, true);
    }

    /**
     * Trigger event.
     *
     * @param event the event
     */
    @SuppressWarnings("rawtypes")
    public void triggerEvent(final Event event) {
        triggerEventInternal(event, null, null);
    }

    /**
     * Send event.
     *
     * @param event the event
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public void sendEvent(final Event event) {
        sendEventInternal(event, null, null, null, false);
    }

    /**
     * Send event.
     *
     * @param description          the event desc
     * @param event                the event
     * @param notifyClassListeners Notify class listeners?
     * @param replyTo              the reply to
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public void sendEvent(final String description, final EventI event, final Bus replyTo, final boolean notifyClassListeners) {
        sendEventInternal(Event.wrap(event), null, description, replyTo, notifyClassListeners);
    }

    /**
     * Send event.
     *
     * @param description the event desc
     * @param event       the event
     * @param replyTo     the reply to
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public void sendEvent(final String description, final EventI event, final Bus replyTo) {
        sendEventInternal(Event.wrap(event), null, description, replyTo, true);
    }

    /**
     * Send event.
     *
     * @param event   the event
     * @param replyTo the reply to
     */
    @SuppressWarnings({"rawtypes", "unused"})
    public void sendEvent(final EventI event, final Bus replyTo) {
        sendEventInternal(Event.wrap(event), event.getClass(), null, replyTo, false);
    }

    private void triggerEventInternal(@Nonnull final Event event, final String description, final Boolean notifyClassListeners) {
        final Object   data = event.getData();
        final Class<?> dataClass;
        if (data != null && EventI.class.isAssignableFrom(data.getClass())) {
            dataClass = data.getClass();
        } else {
            dataClass = event.getClass();
        }

        if (log.isTraceEnabled()) {
            final String stackTraceDisplay = getStackTraceDisplay("^org\\.nrg\\.*$");
            log.trace("Triggering '{}' event: {}\n{}", StringUtils.isNotBlank(description) ? description : dataClass.getName(), ObjectUtils.defaultIfNull(data, event), stackTraceDisplay);
        } else {
            log.debug("Triggering '{}' event: {}", StringUtils.isNotBlank(description) ? description : dataClass.getName(), ObjectUtils.defaultIfNull(data, event));
        }

        if (StringUtils.isNotBlank(description)) {
            getEventBus().notify(description, event);
            if (BooleanUtils.toBooleanDefaultIfNull(notifyClassListeners, false)) {
                getEventBus().notify(dataClass, Event.wrap(data));
            }
        } else {
            getEventBus().notify(dataClass, event);
        }
    }

    private void sendEventInternal(@Nonnull final Event event, final Class<? extends EventI> eventClass, final String description, final Bus replyTo, final boolean notifyClassListeners) {
        final boolean hasReplyTo = replyTo != null;
        if (!hasReplyTo && event.getReplyTo() == null) {
            throw new IllegalArgumentException("Event replyTo object cannot be null");
        }
        final boolean  hasDescription = StringUtils.isNotBlank(description);
        final Class<?> classToSend    = ObjectUtils.defaultIfNull(eventClass, event.getClass());
        if (!hasDescription) {
            log.debug("Sending event {} as class {}", event, classToSend);
            if (hasReplyTo) {
                getEventBus().send(classToSend, event, replyTo);
            } else {
                getEventBus().send(classToSend, event);
            }
        } else {
            if (!hasReplyTo) {
                getEventBus().send(description, event);
                if (notifyClassListeners) {
                    getEventBus().send(classToSend, Event.wrap(event.getData()));
                }
            } else {
                getEventBus().send(description, event, replyTo);
                if (notifyClassListeners) {
                    getEventBus().send(classToSend, Event.wrap(event.getData()), replyTo);
                }
            }
        }
    }

    private final EventBus _eventBus;
}
