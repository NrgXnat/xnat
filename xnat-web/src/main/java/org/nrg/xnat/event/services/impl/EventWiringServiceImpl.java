package org.nrg.xnat.event.services.impl;

import org.nrg.framework.event.EventI;
import org.nrg.xnat.event.EventListener;
import org.nrg.xnat.event.services.EventWiringService;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static reactor.bus.selector.Selectors.type;

@Service
public class EventWiringServiceImpl implements EventWiringService {
    @Autowired
    public EventWiringServiceImpl(final EventBus eventBus,
                                  final List<? extends Consumer<Event<? extends EventI>>> eventListeners) {
        for (Consumer<Event<? extends EventI>> listener : eventListeners) {
            wireEventForListener(eventBus, listener);
        }
    }

    private void wireEventForListener(final EventBus eventBus, Consumer<Event<? extends EventI>> listener) {
        Class<?> listenerClass = AopUtils.getTargetClass(listener);
        if (!listenerClass.isAnnotationPresent(EventListener.class)) {
            // already handled, likely in autowired constructor
            return;
        }
        ParameterizedType consumer = null;
        for (Type t : listenerClass.getGenericInterfaces()) {
            if (!(t instanceof ParameterizedType)) {
                continue;
            }
            Type rt = ((ParameterizedType) t).getRawType();
            if (rt instanceof Class<?> && Consumer.class.isAssignableFrom((Class<?>) rt)) {
                consumer = (ParameterizedType) t;
                break;
            }
        }
        if (consumer == null) {
            // Not something we can handle
            return;
        }
        final ParameterizedType parameterizedEvent = (ParameterizedType) consumer.getActualTypeArguments()[0];
        final Class<? extends EventI> eventType = ((Class<?>) parameterizedEvent.getActualTypeArguments()[0])
                .asSubclass(EventI.class);
        eventBus.on(type(eventType), listener);
    }
}
