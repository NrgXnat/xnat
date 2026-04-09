package org.nrg.xnat.hibernate.listeners.methods;

import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;

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

    protected boolean matchInsert(final PostInsertEvent event) {
        return matchesActionAndClass(handlesInserts, event.getEntity().getClass());
    }

    private boolean matchUpdate(final PostUpdateEvent event) {
        return matchesActionAndClass(handlesUpdates, event.getEntity().getClass());
    }

    private boolean matchDelete(final PostDeleteEvent event) {
        return matchesActionAndClass(handlesDeletes, event.getEntity().getClass());
    }

    private boolean matchesActionAndClass(final boolean handles, final Class<?> entityClass) {
        return handles && handledEntityClasses.contains(entityClass);
    }
}
