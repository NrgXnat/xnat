package org.nrg.xft.event.listeners;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.XftItemEventHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selectors;
import reactor.fn.Consumer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class XftItemEventHandler implements Consumer<Event<XftItemEventI>> {
    public XftItemEventHandler(final EventBus eventBus) {
        eventBus.on(Selectors.type(XftItemEvent.class), this);
    }

    /**
     * Accepts the {@link XftItemEvent} from the event bus and handles it by polling all handler methods to
     * find those that {@link XftItemEventHandlerMethod#matches(XftItemEventI) match} the event, then calls
     * {@link XftItemEventHandlerMethod#handleEvent(XftItemEventI)} for each method.
     *
     * @param event The event to be handled.
     */
    @Override
    public void accept(final Event<XftItemEventI> event) {
        final XftItemEventI xftItemEvent = event.getData();
        log.debug("Accepted XFTItem event: {}", xftItemEvent.toString());

        final List<XftItemEventHandlerMethod> methods = getMethodsForEvent(xftItemEvent);
        log.debug("Found {} methods to handle XFTItem event '{}'", methods.size(), xftItemEvent);

        for (final XftItemEventHandlerMethod method : methods) {
            log.debug("Handling XFTItem event {} with method {}", xftItemEvent, method.getClass().getName());
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
        log.debug("A total of {} XFTItem event handler methods were found", methods.size());
        _methods.addAll(methods);
    }

    /**
     * Returns all configured methods that match the submitted event.
     *
     * @param event The event to test.
     *
     * @return The list of methods that match the event.
     */
    private List<XftItemEventHandlerMethod> getMethodsForEvent(final XftItemEventI event) {
        return FluentIterable.from(_methods).filter(new Predicate<XftItemEventHandlerMethod>() {
            @Override
            public boolean apply(@Nullable final XftItemEventHandlerMethod method) {
                return method != null && method.matches(event);
            }
        }).toList();
    }

    private final List<XftItemEventHandlerMethod> _methods = new ArrayList<>();
}
