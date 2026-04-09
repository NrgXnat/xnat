package org.nrg.xnat.hibernate.listeners.methods;

import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.nrg.xnat.hibernate.entities.Entity1;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
public class Entity1InsertHandlerMethod extends AbstractTrackingHibernateEntityEventHandlerMethod {
    public Entity1InsertHandlerMethod() {
        super(Action.INSERT, Entity1.class);
    }

    @Override
    public Future<Boolean> handleEvent(final AbstractEvent event) {
        if (!(event instanceof final PostInsertEvent postInsertEvent)) {
            throw new IllegalArgumentException("Expected a PostInsertEvent, but got a " + event.getClass().getName());
        }

        final Object object = postInsertEvent.getEntity();
        if (!(object instanceof final Entity1 entity)) {
            throw new IllegalArgumentException("Expected an Entity1, but got a " + object.getClass().getName());
        }

        getTestEventTracker().addEvent(Action.INSERT, Entity1.class, entity.getId());

        return new AsyncResult<>(true);
    }
}
