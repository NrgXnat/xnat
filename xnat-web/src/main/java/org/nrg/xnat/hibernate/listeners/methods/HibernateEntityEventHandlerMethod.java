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
     * Handles the specified event. Note that in some cases, such as when the event is
     * dispatched by a service other than Hibernate&mdash;such as the distributed events
     * plugin's Hibernate topic listener&mdash;many of the standard event properties may
     * be <pre>null</pre> or empty, including:
     *
     * <ul>
     *     <li><pre>state</pre> for both insert and update events</li>
     *     <li><pre>oldState</pre> for update events</li>
     *     <li><pre>entityPersister</pre> and <pre>session</pre>for insert, update, and delete events</li>
     *     <li><pre>deletedState</pre> and <pre>entity</pre> for delete events</li>
     * </ul>
     * <p.>
     * Make sure to do a <pre>null</pre> check on these properties before referencing them in any
     * implementations of this method.
     * <p/>
     * <b>Note:</b> Delete events that are dispatched by services other than Hibernate&mdash;again, such as
     * the distributed events plugin's Hibernate topic listener&mdash;may have set a <pre>String</pre> value
     * for the <pre>entity</pre> property set. This value will be the class name of the deleted entity and
     * can be used to match handler methods for handling a particular delete event.
     *
     * @param event The event to handle.
     */
    @Async
    Future<Boolean> handleEvent(final AbstractEvent event);
}
