/**
 * ScriptTriggerService
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.services;

import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import java.util.List;
import java.util.Map;

/**
 * ScriptTriggerService class.
 *
 * @author Rick Herrick
 */
public interface ScriptTriggerService extends BaseHibernateService<ScriptTrigger> {
	
    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated ID.
     *
     * @param id the id
     * @return the by id
     */
    ScriptTrigger getById(final String id);
    
    /**
     * Returns the script trigger associated with the indicated trigger ID.
     *
     * @param triggerId the trigger id
     * @return the by trigger id
     */
    ScriptTrigger getByTriggerId(final String triggerId);

    /**
     * Returns the script triggers associated with the indicated script ID.
     *
     * @param scriptId the script id
     * @return the by script id
     */
    List<ScriptTrigger> getByScriptId(final String scriptId);

    /**
     * Retrieves all triggers associated with the site scope. This is basically a convenience wrapper around the full
     * scope-entity get implemented in {@link #getByScope(Scope, String)}.
     *
     * @return the site triggers
     */
    List<ScriptTrigger> getSiteTriggers();

    /**
     * Retrieves all triggers associated with the site scope and indicated event. This is basically a convenience
     * wrapper around the full scope-entity-event get implemented in {@link #getByScopeEntityAndEvent(Scope, String,
     * String, String)}.
     *
     * @param eventClass the event class
     * @param event the event
     * @return the site triggers for event
     */
    List<ScriptTrigger> getSiteTriggersForEvent(final String eventClass, final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope the scope
     * @param entityId the entity id
     * @return the by scope
     */
    List<ScriptTrigger> getByScope(final Scope scope, final String entityId);

    /**
     * G3ets all triggers associated with the indicated event.
     *
     * @param eventClass the event class
     * @param eventId the event id
     * @return the by event
     */
    List<ScriptTrigger> getByEvent(final String eventClass, final String eventId);
    
    /**
     * Gets the by event and filters.
     *
     * @param eventClass the event class
     * @param eventId the event id
     * @param filterMap the filter map
     * @return the by event and filters
     */
    List<ScriptTrigger> getByEventAndFilters(final String eventClass, final String eventId, final Map<String,String> filterMap);

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated association and event. Generally the association
     * is an encoded {@link Scope#code() scope code} and entity ID. This search performs no scope fail-over.
     *
     * @param association the association
     * @param eventClass the event class
     * @param event the event
     * @return the by association and event
     */
    List<ScriptTrigger> getByAssociationAndEvent(final String association, final String eventClass, final String event);
    
    /**
     * Gets the by scope entity and event.
     *
     * @param scope the scope
     * @param entityId the entity id
     * @param eventClass the event class
     * @param event the event
     * @return the by scope entity and event
     */
    List<ScriptTrigger> getByScopeEntityAndEvent(final Scope scope, final String entityId, final String eventClass, final String event);
    
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
    ScriptTrigger getByScopeEntityAndEventAndFilters(final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    /**
     * Sets the default trigger id format.
     *
     * @param defaultTriggerIdFormat the new default trigger id format
     */
    void setDefaultTriggerIdFormat(final String defaultTriggerIdFormat);

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
    String getDefaultTriggerName(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);
}
