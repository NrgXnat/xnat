package org.nrg.xft.event.methods;

import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.listeners.XftItemEventHandler;
import org.springframework.scheduling.annotation.Async;
import reactor.bus.Event;

import java.util.concurrent.Future;

/**
 * Defines the interface for {@link XftItemEventI} handler methods. Handler methods are referenced by the
 * {@link XftItemEventHandler#accept(Event)} method, first by calling {@link #matches(XftItemEventI)} to
 * determine if the method is interested in the event and then {@link #handleEvent(XftItemEventI)} when
 * appropriate.
 */
public interface XftItemEventHandlerMethod {
    /**
     * Provides a name for this handler method.
     *
     * @return The method name.
     */
    String getName();
    /**
     * Indicates whether this handler method wants to handle the submitted event.
     *
     * @param event The event to test.
     *
     * @return Returns true if this method can handle the event, false otherwise.
     */
    boolean matches(final XftItemEventI event);

    /**
     * Handles the specified event.
     *
     * @param event The event to handle.
     */
    @Async
    Future<Boolean> handleEvent(final XftItemEventI event);
}
