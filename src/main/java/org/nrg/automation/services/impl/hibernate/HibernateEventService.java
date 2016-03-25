package org.nrg.automation.services.impl.hibernate;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.repositories.EventRepository;
import org.nrg.automation.services.EventReferencedException;
import org.nrg.automation.services.EventService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * The event service class provides the event ID and label mapping used by the automation system. On initialization,
 * this can be pre-populated with a query stored in the string bean in the application context. This query should return
 * two columns, both strings, named "event_label" and "event_id".
 */
@SuppressWarnings({"JpaQlInspection", "SqlDialectInspection", "WeakerAccess"})
@Service
public class HibernateEventService extends AbstractHibernateEntityService<Event, EventRepository> implements EventService {

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean hasEvent(final String eventId) {
        final Session session = _sessionFactory.getCurrentSession();
        final Query query = session.createQuery("select count(*) from Event where eventId = :eventId and enabled = true").setString("eventId", eventId);
        return ((Long) query.uniqueResult()) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Event getByEventId(final String eventId) {
        return getDao().findByUniqueProperty("eventId", eventId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(final String eventId, final boolean cascade) throws EventReferencedException {
        final List<ScriptTrigger> triggers = _scriptTriggerService.getByEvent(eventId);
        if (triggers.size() > 0) {
            if (!cascade) {
                throw new EventReferencedException(eventId, triggers.size());
            }
            for (final ScriptTrigger trigger : triggers) {
                _log.info("Deleting script trigger: " + trigger.getTriggerId());
                _scriptTriggerService.delete(trigger);
            }
        }
        delete(getByEventId(eventId));
    }

    private static final Logger _log = LoggerFactory.getLogger(HibernateEventService.class);

    @Inject
    private ScriptTriggerService _scriptTriggerService;

    @Inject
    private SessionFactory _sessionFactory;
}
