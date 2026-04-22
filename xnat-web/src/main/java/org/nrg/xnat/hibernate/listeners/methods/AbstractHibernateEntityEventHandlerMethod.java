package org.nrg.xnat.hibernate.listeners.methods;

import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public abstract class AbstractHibernateEntityEventHandlerMethod implements HibernateEntityEventHandlerMethod {
    private final Set<Action>   handledActions       = new HashSet<>();
    private final Set<Class<?>> handledEntityClasses = new HashSet<>();

    private final boolean handlesInserts;
    private final boolean handlesUpdates;
    private final boolean handlesDeletes;

    protected AbstractHibernateEntityEventHandlerMethod(final Action action, final Class<?> entityClass) {
        this(Collections.singleton(action), Collections.singleton(entityClass));
    }

    protected AbstractHibernateEntityEventHandlerMethod(final Collection<Action> actions, final Collection<Class<?>> entityClasses) {
        handledActions.addAll(actions);
        handledEntityClasses.addAll(entityClasses);
        handlesInserts = actions.contains(Action.INSERT);
        handlesUpdates = actions.contains(Action.UPDATE);
        handlesDeletes = actions.contains(Action.DELETE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Future<Boolean> handleEvent(final AbstractEvent event);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(final AbstractEvent event) {
        if (event instanceof PostInsertEvent) {
            return matchInsert((PostInsertEvent) event);
        }
        if (event instanceof PostUpdateEvent) {
            return matchUpdate((PostUpdateEvent) event);
        }
        if (event instanceof PostDeleteEvent) {
            return matchDelete((PostDeleteEvent) event);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Action> getHandledActions() {
        return handledActions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getHandledEntityClasses() {
        return handledEntityClasses;
    }

    /**
     * In some cases, notably when this method is handling a Hibernate entity that was deleted on another
     * node in a multi-node system, it can be difficult to retrieve the actual entity object. In those
     * cases the "entity" may actually be a string that contains the name of the entity class, rather
     * than the entity itself. In those cases, the string is converted into an instance of that class.
     * Otherwise, the class itself is just returned.
     *
     * @param entity The entity object from the event, which may actually be a string containing the name of the entity class.
     *
     * @return The class of the affected entity
     */
    protected Class<?> getEntityClass(final Object entity) {
        if (entity instanceof String) {
            try {
                return Class.forName((String) entity);
            } catch (ClassNotFoundException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Got a string as an entity, but that string doesn't seem to match known classes: " + entity);
            }
        }
        return entity.getClass();
    }

    private boolean matchInsert(final PostInsertEvent event) {
        return matchesActionAndClass(handlesInserts, event.getEntity());
    }

    private boolean matchUpdate(final PostUpdateEvent event) {
        return matchesActionAndClass(handlesUpdates, event.getEntity());
    }

    private boolean matchDelete(final PostDeleteEvent event) {
        return matchesActionAndClass(handlesDeletes, event.getEntity());
    }

    private boolean matchesActionAndClass(final boolean handles, final Object entity) {
        return handles && getHandledEntityClasses().contains(getEntityClass(entity));
    }
}
