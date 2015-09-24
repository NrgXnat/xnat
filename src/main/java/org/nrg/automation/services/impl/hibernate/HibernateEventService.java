package org.nrg.automation.services.impl.hibernate;

import org.apache.commons.lang.StringUtils;
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
import org.nrg.framework.utilities.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        _jdbcTemplate = new JdbcTemplate(_dataSource);

        final TransactionTemplate transactionTemplate = new TransactionTemplate(_transactionManager);

        // This code converts configurations that use the deprecated project ID (which is actually the projectdata_info attribute
        // for XNAT project objects) to use the scope and entity ID instead. It also backfills the project attribute for configurations
        // created without the projectdata_info attribute. This code should be deprecated and removed eventually, maybe converted to a
        // step in a migration script.
        final TransactionCallbackWithoutResult callback = new TransactionCallbackWithoutResult() {
            @SuppressWarnings("SqlNoDataSourceInspection")
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                try {
                    final int eventCount = _jdbcTemplate.queryForObject("SELECT COUNT(*) AS total FROM xhbm_event", Integer.class);
                    if (eventCount == 0) {
                        final Map<String, String> events = new HashMap<>();
                        try {
                            final Object bean = getContext().getBean("defaultEvents");
                            if (bean != null) {
                                final int size;
                                if (bean instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    final Map<String, String> defaultEvents = (Map<String, String>) bean;
                                    size = defaultEvents.size();
                                    for (final String event : defaultEvents.keySet()) {
                                        events.put(event, defaultEvents.get(event));
                                    }
                                } else if (bean instanceof List) {
                                    @SuppressWarnings("unchecked")
                                    final List<String> defaultEvents = (List<String>) bean;
                                    size = defaultEvents.size();
                                    for (final String event : defaultEvents) {
                                        events.put(event, event);
                                    }
                                } else {
                                    size = 0;
                                }
                                _log.info("Processed " + size + " events from the defaultEvents list.");
                            } else {
                                _log.info("No default events source found.");
                            }
                        } catch (NoSuchBeanDefinitionException ignored) {
                            // We don't care, this just means it wasn't defined anywhere.
                        }
                        try {
                            final String populateEventsQuery = getContext().getBean("populateEventsQuery", String.class);
                            if (StringUtils.isNotBlank(populateEventsQuery)) {
                                final List<Integer> existing = _jdbcTemplate.query(populateEventsQuery, new RowMapper<Integer>() {
                                    @Override
                                    public Integer mapRow(final ResultSet result, final int rowNum) throws SQLException {
                                        final String eventId = result.getString("event_id");
                                        final String eventLabel = result.getString("event_label");
                                        final int total = result.getInt("total");
                                        if (!Patterns.EMAIL.matcher(eventId).find()) {
                                            _log.debug("Adding event with ID " + eventId + " and label " + eventLabel + ", which occurred " + total + " times previously.");
                                            events.put(eventId, eventLabel);
                                        } else {
                                            _log.debug("Blocking event with ID " + eventId + " and label " + eventLabel + " because it looks like it has an email address in it.");
                                        }
                                        return total;
                                    }
                                });
                                _log.info("Processed " + (existing != null ? existing.size() : 0) + " events from the populateEventsQuery results.");
                            }
                        } catch (NoSuchBeanDefinitionException ignored) {
                            // We don't care, this just means it wasn't defined anywhere.
                        }
                        if (events.size() > 0) {
                            final List<String> inserts = new ArrayList<>();
                            for (final String event : events.keySet()) {
                                inserts.add(String.format("INSERT INTO xhbm_event (created, disabled, timestamp, event_id, event_label) VALUES (now(), to_timestamp(0), now(), '%s', '%s')", event.replaceAll("'", "''"), events.get(event).replaceAll("'", "''")));
                                _log.debug("Creating new event entry " + event + " with the label " + events.get(event));
                            }
                            _jdbcTemplate.batchUpdate(inserts.toArray(new String[inserts.size()]));
                            _log.info("Created " + inserts.size() + " new event objects.");
                        } else {
                            _log.info("Found no existing events, but couldn't retrieve the default list of events nor the populateEventsQuery query to bootstrap the events.");
                        }
                    }
                } catch (DataAccessException exception) {
                    _log.error("There was an issue trying to initialize the event data table, rolling back all transactions", exception);
                    status.setRollbackOnly();
                }
            }
        };
        transactionTemplate.execute(callback);
    }

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

    @Inject
    private PlatformTransactionManager _transactionManager;

    @Inject
    private DataSource _dataSource;

    private JdbcTemplate _jdbcTemplate;
}
