/*
 * prefs: org.nrg.prefs.events.AbstractPreferenceHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.events;

import org.nrg.framework.event.StructuredEvent;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import java.util.*;
import java.util.concurrent.Future;

import static reactor.bus.selector.Selectors.type;

public abstract class AbstractPreferenceHandler<T extends StructuredEvent> extends AbstractParameterizedWorker implements Consumer<Event<T>> {

    protected AbstractPreferenceHandler(final EventBus eventBus) {
        eventBus.on(type(getParameterizedType()), this);
    }

    public abstract String getToolId();

    public abstract void setToolId(String toolId);

    public abstract List<PreferenceHandlerMethod> getMethods();

    public abstract void addMethod(PreferenceHandlerMethod method);

    @Override
    public void accept(final Event<T> event) {
        handleEvent(event.getData());
    }

    @Autowired
    public void setAvailableMethods(final List<PreferenceHandlerMethod> methods) {
        for (final PreferenceHandlerMethod method : methods) {
            if (method.getToolIds().contains(getToolId())) {
                addMethod(method);
            }
        }
    }

    @Async
    public Future<Void> handleEvent(final T event) {
        final Map<String, String> fields = event.getEventSpecificFieldsAsMap();
        if (fields != null) {
            final Set<String>                               preferences = fields.keySet();
            final Map<PreferenceHandlerMethod, Set<String>> methods     = getMethodsForPreferences(new ArrayList<>(preferences));
            for (final PreferenceHandlerMethod method : methods.keySet()) {
                final Set<String>         handled = methods.get(method);
                final Map<String, String> values  = new HashMap<>();
                for (final String preference : handled) {
                    values.put(preference, fields.get(preference));
                }
                method.handlePreferences(values);
            }
        }

        return new AsyncResult<>(null);
    }

    private Map<PreferenceHandlerMethod, Set<String>> getMethodsForPreferences(final List<String> preferences) {
        final Map<PreferenceHandlerMethod, Set<String>> resolved = new HashMap<>();
        for (final PreferenceHandlerMethod method : getMethods()) {
            if (!resolved.containsKey(method)) {
                final Set<String> handled = method.findHandledPreferences(preferences);
                if (handled.size() > 0) {
                    resolved.put(method, handled);
                }
            }
        }
        return resolved;
    }
}
