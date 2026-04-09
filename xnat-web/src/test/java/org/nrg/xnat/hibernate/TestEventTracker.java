package org.nrg.xnat.hibernate;

import org.apache.commons.lang3.tuple.Triple;
import org.nrg.xnat.hibernate.listeners.methods.HibernateEntityEventHandlerMethod.Action;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;

@Service
public class TestEventTracker {
    private final LinkedHashMap<Instant, Triple<Action, Class<?>, Long>> testEvents = new LinkedHashMap<>();

    public void addEvent(final Action action, final Class<?> entityClass, final long id) {
        testEvents.put(Instant.now(), Triple.of(action, entityClass, id));
    }

    public Triple<Action, Class<?>, Long> getFirstEvent() {
        return testEvents.firstEntry().getValue();
    }

    public Triple<Action, Class<?>, Long> getLastEvent() {
        return testEvents.lastEntry().getValue();
    }

    public Triple<Action, Class<?>, Long> getEvent(final Instant timestamp) {
        return testEvents.get(timestamp);
    }
}
