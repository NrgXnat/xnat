package org.nrg.xnat.hibernate.listeners.methods;

import org.hibernate.event.spi.AbstractEvent;
import org.nrg.xnat.hibernate.listeners.HibernateEntityEventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Defines the interface for {@link AbstractEvent Hibernate entity event} handler methods. Handler methods are
 * referenced by the {@link HibernateEntityEventListener} service, first by calling {@link #matches(AbstractEvent)}
 * to determine if the method is interested in the event and then {@link #handleEvent(AbstractEvent)} when
 * appropriate.
 */
public interface HibernateEntityEventHandlerMethod {
    /**
     * Indicates the type of action(s) the implementing class handles.
     */
    enum Action {
        INSERT, UPDATE, DELETE
    }

    /**
     * Indicates whether this handler method wants to handle the submitted event.
     *
     * @param event The event to test.
     *
     * @return Returns true if this method can handle the event, false otherwise.
     */
    boolean matches(final AbstractEvent event);

    /**
     * Indicates the action types handled by this method.
     *
     * @return A list of one or more {@link Action actions} that this method handles.
     */
    Set<Action> getHandledActions();

    /**
     * Indicates the entity classes handled by this method.
     *
     * @return A list of one or more entity classes that this method handles.
     */
    Set<Class<?>> getHandledEntityClasses();

    /**
     * Handles the specified event.
     *
     * @param event The event to handle.
     */
    @Async
    Future<Boolean> handleEvent(final AbstractEvent event);
}
