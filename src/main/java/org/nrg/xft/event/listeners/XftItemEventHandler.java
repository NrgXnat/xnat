package org.nrg.xft.event.listeners;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.methods.XftItemEventHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XftItemEventHandler implements Consumer<Event<XftItemEvent>> {
    public XftItemEventHandler(final EventBus eventBus) {
        eventBus.on(Selectors.type(XftItemEvent.class), this);
    }

    /**
     * Accepts the {@link XftItemEvent} from the event bus and handles it by polling all handler methods to
     * find those that {@link XftItemEventHandlerMethod#matches(XftItemEvent) match} the event, then calls
     * {@link XftItemEventHandlerMethod#handleEvent(XftItemEvent)} for each method.
     *
     * @param event The event to be handled.
     */
    @Override
    public void accept(final Event<XftItemEvent> event) {
        final XftItemEvent xftItemEvent = event.getData();
        final List<XftItemEventHandlerMethod> methods = getMethodsForEvent(xftItemEvent);
        for (final XftItemEventHandlerMethod method : methods) {
            method.handleEvent(xftItemEvent);
        }
    }

    /**
     * Sets the methods for the handler.
     *
     * @param methods The available {@link XftItemEventHandlerMethod handler methods}.
     */
    @Autowired
    public void setMethods(final List<XftItemEventHandlerMethod> methods) {
        _methods.addAll(methods);
    }

    private List<XftItemEventHandlerMethod> getMethodsForEvent(final XftItemEvent event) {
        final List<XftItemEventHandlerMethod> resolved = new ArrayList<>();
        for (final XftItemEventHandlerMethod method : _methods) {
            if (method.matches(event)) {
                resolved.add(method);
            }
        }
        return resolved;
    }

    private final List<XftItemEventHandlerMethod> _methods = new ArrayList<>();
}
