package org.nrg.xnat.hibernate.listeners.methods;

import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.nrg.xnat.hibernate.entities.Entity2;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
public class Entity2InsertHandlerMethod extends AbstractTrackingHibernateEntityEventHandlerMethod {
    public Entity2InsertHandlerMethod() {
        super(Action.INSERT, Entity2.class);
    }

    @Override
    public Future<Boolean> handleEvent(final AbstractEvent event) {
        if (!(event instanceof final PostInsertEvent postInsertEvent)) {
            throw new IllegalArgumentException("Expected a PostInsertEvent, but got a " + event.getClass().getName());
        }

        final Object object = postInsertEvent.getEntity();
        if (!(object instanceof final Entity2 entity)) {
            throw new IllegalArgumentException("Expected an Entity2, but got a " + object.getClass().getName());
        }

        getTestEventTracker().addEvent(Action.INSERT, Entity2.class, entity.getId());

        return new AsyncResult<>(true);
    }
}
