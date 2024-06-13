/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateScriptTriggerService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.repositories.ScriptTriggerRepository;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nrg.automation.services.Filtering.propertiesMatchFilterMap;

/**
 * HibernateScriptTriggerTemplateService class.
 *
 * @author Rick Herrick
 */
@Service
public class HibernateScriptTriggerService extends AbstractHibernateEntityService<ScriptTrigger, ScriptTriggerRepository> implements ScriptTriggerService {

    /**
     * Update old style script triggers.
     */
    @Transactional
    public void updateOldStyleScriptTriggers() {
    	getDao().updateOldStyleTriggers();
    }

	/**
     * Overrides the default polymorphic {@link AbstractHibernateEntityService#create(Object...)} method to support
     * translating a string parameter into an event ID and retrieving or creating an {@link Event} object to associate
     * with the new {@link ScriptTrigger} object.
     * @param parameters    The parameters passed to the entity constructor
     * @return A new {@link ScriptTrigger} instance.
     */
    @Override
    @Transactional
    public ScriptTrigger newEntity(final Object... parameters) {
        return super.newEntity(parameters);
    }

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated trigger ID.
     *
     * @param id the id
     * @return The trigger with the indicated ID, if it exists, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public ScriptTrigger getById(String id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving script trigger by ID: {}", id);
        }
        return getDao().getById(id);
    }

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated trigger ID.
     *
     * @param triggerId The {@link ScriptTrigger#getTriggerId()} trigger ID} of the trigger to retrieve.
     *
     * @return The trigger with the indicated ID, if it exists, <b>null</b> otherwise.
     */
    @Override
    @Transactional
    public ScriptTrigger getByTriggerId(String triggerId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving script trigger by ID: {}", triggerId);
        }
        return getDao().getByTriggerId(triggerId);
    }

    /**
     * Returns the script triggers associated with the indicated script ID.
     *
     * @param scriptId The script ID for which to locate triggers.
     *
     * @return A list of all {@link ScriptTrigger script triggers} that are associated with the indicated script ID.
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getByScriptId(final String scriptId) {
        final ScriptTrigger example = new ScriptTrigger();
        example.setScriptId(scriptId);
        return getDao().findByExample(example, EXCLUDE_PROPS_SCRIPT_ID);
    }

    /**
     * Retrieves all triggers associated with the site scope. This is basically a convenience wrapper around the full
     * scope-entity get implemented in {@link #getByScope(org.nrg.framework.constants.Scope, String)}.
     *
     * @return All triggers associated with the site scope.
     * @see #getByScope(org.nrg.framework.constants.Scope, String)
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getSiteTriggers() {
        return getByScope(Scope.Site, null);
    }

    /**
     * Retrieves all triggers associated with the site scope and indicated event. This is basically a convenience
     * wrapper around the full scope-entity-event get implemented in {@link #getByScopeEntityAndEvent(Scope, String,
     * String, String)}.
     *
     * @param eventClass the event class
     * @param event The event associated with the trigger.
     * @return All triggers associated with the site scope and indicated event.
     * @see #getByScopeEntityAndEvent(Scope, String, String, String)
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getSiteTriggersForEvent(final String eventClass, final String event) {
        return getByScopeEntityAndEvent(Scope.Site, null, eventClass, event);
    }

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     *
     * @return All triggers associated with the indicated scope and entity.
     * @see #getSiteTriggers()
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getByScope(final Scope scope, final String entityId) {
        final ScriptTrigger example = new ScriptTrigger();
        example.setAssociation(Scope.encode(scope, entityId));
        final List<ScriptTrigger> results = getDao().findByExample(example, EXCLUDE_PROPS_SCOPE);
        if (_log.isDebugEnabled()) {
            _log.debug("Found {} triggers for scope {} and entity ID {}", results == null ? "no" : results.size(), scope, entityId);
        }
        return results;
    }

    /**
     * Gets the by event.
     *
     * @param eventClass the event class
     * @param eventId the event id
     * @return the by event
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getByEvent(final String eventClass, final String eventId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("srcEventClass", eventClass);
        properties.put("event", eventId);
        final List<ScriptTrigger> results = getDao().findByProperties(properties);
        _log.debug("Found {} triggers for event {}", results == null ? "no" : results.size(), eventId);
        return results;
    }

	/**
	 * Gets the by event and filters.
	 *
	 * @param eventClass the event class
	 * @param eventId the event id
	 * @param filterMap the filter map
	 * @return the by event and filters
	 */
	@Override
    @Transactional
    public List<ScriptTrigger> getByEventAndFilters(String eventClass, String eventId, Map<String, String> filterMap) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("srcEventClass", eventClass);
        properties.put("event", eventId);
        final List<ScriptTrigger> results = getDao().findByProperties(properties);
        if (results == null) {
            _log.debug("Found no triggers for event {}", eventId);
            return null;
        }
        final List<ScriptTrigger> triggers = new ArrayList<>();
        for (final ScriptTrigger trigger : results) {
            final Map<String, List<String>> eventFiltersMap = trigger.getEventFiltersAsMap();
            if (propertiesMatchFilterMap(filterMap, eventFiltersMap)) {
                triggers.add(trigger);
            }
        }
        _log.debug("Found {} triggers for event {}", triggers.size(), eventId);
        return triggers;
    }

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     * @param eventClass the event class
     * @param event    The event associated with the trigger.
     * @return All triggers associated with the indicated scope and entity and event
     * @see #getSiteTriggers()
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getByScopeEntityAndEvent(final Scope scope, final String entityId, final String eventClass, final String event) {
        return getByAssociationAndEvent(Scope.encode(scope, entityId), eventClass, event);
    }

	/**
	 * Gets the by scope entity and event and filters.
	 *
	 * @param scope the scope
	 * @param entityId the entity id
	 * @param eventClass the event class
	 * @param event the event
	 * @param filterMap the filter map
	 * @return the by scope entity and event and filters
	 */
	@Override
    @Transactional
	public ScriptTrigger getByScopeEntityAndEventAndFilters(Scope scope, String entityId, String eventClass, String event, Map<String, String> filterMap) {
        final List<ScriptTrigger> results = getByAssociationAndEvent(Scope.encode(scope, entityId), eventClass, event);
        if (results != null) {
            for (final ScriptTrigger trigger : results) {
            	final Map<String, List<String>> eventFiltersMap = trigger.getEventFiltersAsMap();
                if (propertiesMatchFilterMap(filterMap, eventFiltersMap)) {
                    return trigger;
                }
            }
        }
        return null;
	}

    /**
     * Sets the default trigger id format.
     *
     * @param defaultTriggerIdFormat the new default trigger id format
     */
    @Override
    public void setDefaultTriggerIdFormat(final String defaultTriggerIdFormat) {
        _defaultTriggerIdTemplate = defaultTriggerIdFormat;
    }

    /**
     * Gets the default trigger name.
     *
     * @param scriptId the script id
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @param eventFilters the event filters
     * @return the default trigger name
     */
    @Override
    public String getDefaultTriggerName(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters) {
        final Map<String, String> values = new HashMap<>();
        values.put("scriptId", scriptId.replace("-", "_"));
        values.put("srcEventClass", Integer.toString(eventClass.hashCode()));
        values.put("event", event.replace(" ", "_").replace("/", "_").replace(":", "_").replace("-", "_"));
        values.put("association", Scope.encode(scope, entityId).replace(" ", "_").replace("/", "_").replace(":", "_").replace("-", "_"));
        values.put("eventFilters", Integer.toString(eventFilters.hashCode()));
        return new StrSubstitutor(values, "%(", ")").replace(_defaultTriggerIdTemplate);
    }

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated association and event. Generally the association
     * is an encoded {@link Scope#code() scope code} and entity ID. This search performs no scope fail-over.
     *
     * @param association The association for the trigger. Association
     * @param eventClass the event class
     * @param eventId       The event associated with the trigger.
     * @return The requested script trigger, if it exists.
     */
    @Override
    @Transactional
    public List<ScriptTrigger> getByAssociationAndEvent(final String association, final String eventClass, final String eventId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("association", association);
        properties.put("srcEventClass", eventClass);
        properties.put("event", eventId);
        final List<ScriptTrigger> triggers = getDao().findByProperties(properties);
        if (triggers == null || triggers.isEmpty()) {
            _log.debug("Found no trigger for association {} and event {}", association, eventId);
        }
        return triggers;
    }

    /**
     * Are all params strings.
     *
     * @param parameters the parameters
     * @return true, if successful
     */
    @SuppressWarnings("unused")
	private static boolean areAllParamsStrings(final Object[] parameters) {
        for (final Object parameter : parameters) {
            if (!parameter.getClass().equals(String.class)) {
                return false;
            }
        }
        return true;
    }

    /** The Constant EXCLUDE_PROPS_SCOPE. */
    private static final String[] EXCLUDE_PROPS_SCOPE = AbstractHibernateEntity.getExcludedProperties("triggerId", "description", "scriptId", "event", "srcEventClass", "eventFilters");

    /** The Constant EXCLUDE_PROPS_SCRIPT_ID. */
    private static final String[] EXCLUDE_PROPS_SCRIPT_ID = AbstractHibernateEntity.getExcludedProperties("triggerId", "description", "association", "event", "srcEventClass", "eventFilters");

    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(HibernateScriptTriggerService.class);

    /** The _default trigger id template. */
    private String _defaultTriggerIdTemplate = "%(scriptId)-%(association)-%(event)-%(srcEventClass)-%(eventFilters)";
}
