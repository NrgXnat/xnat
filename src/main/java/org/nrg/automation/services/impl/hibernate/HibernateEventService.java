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
 *
 * @author Rick Herrick <rick.herrick@wustl.edu> on 7/24/2015.
 */
@SuppressWarnings({"JpaQlInspection", "SqlDialectInspection"})
@Service
public class HibernateEventService extends AbstractHibernateEntityService<Event, EventRepository> implements EventService {

    /**
     * A convenience test for the existence of a event with the indicated event ID.
     *
     * @param eventId The ID of the event to test for.
     *
     * @return <b>true</b> if a event with the indicated ID exists, <b>false</b> otherwise.
     */
    @Override
    @Transactional
    public boolean hasEvent(final String eventId) {
        final Session session = _sessionFactory.getCurrentSession();
        final Query query = session.createQuery("select count(*) from Event where eventId = :eventId and enabled = true").setString("eventId", eventId);
        return ((Long) query.uniqueResult()) > 0;
    }

    /**
     * Retrieves the {@link Event} with the indicated event ID.
     *
     * @param eventId The {@link Event#getEventId() event ID} of the event to
     *                 retrieve.
     *
     * @return The event with the indicated eventId, if it exists, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public Event getByEventId(final String eventId) {
        return getDao().findByUniqueProperty("eventId", eventId);
    }

    /**
     * Deletes the {@link Event event} with the indicated ID. If the cascade flag is set to false, the event will only
     * be deleted if there are no {@link ScriptTrigger script triggers} that reference the event. Otherwise, the
     * {@link EventReferencedException} will be thrown. If the cascade flag is set to true, the event and any associated
     * script triggers will be deleted.
     *
     * @param eventId The event ID of the event to delete.
     * @param cascade Whether the delete operation should cascade.
     * @throws EventReferencedException
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

    @SuppressWarnings("unused")
    private static final Logger _log = LoggerFactory.getLogger(HibernateEventService.class);

    @Inject
    private ScriptTriggerService _scriptTriggerService;

    @Inject
    private SessionFactory _sessionFactory;
}
