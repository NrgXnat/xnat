package org.nrg.automation.services.impl.hibernate;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.nrg.automation.entities.Event;
import org.nrg.automation.repositories.EventRepository;
import org.nrg.automation.services.EventService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * The event service class provides the event ID and label mapping used by the automation system. On initialization,
 * this can be pre-populated with a query stored in the string bean in the application context. This query should return
 * two columns, both strings, named "event_label" and "event_id".
 *
 * @author Rick Herrick <rick.herrick@wustl.edu> on 7/24/2015.
 */
@SuppressWarnings("JpaQlInspection")
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

    @SuppressWarnings("unused")
    private static final Logger _log = LoggerFactory.getLogger(HibernateEventService.class);

    @Inject
    private SessionFactory _sessionFactory;
}
