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

/**
 * ScriptTriggerService class.
 *
 * @author Rick Herrick
 */
public interface ScriptTriggerService extends BaseHibernateService<ScriptTrigger> {
    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated trigger ID.
     *
     * @param triggerId The {@link ScriptTrigger#getTriggerId()} trigger ID} of the trigger to retrieve.
     *
     * @return The trigger with the indicated ID, if it exists, <b>null</b> otherwise.
     */
    ScriptTrigger getByTriggerId(final String triggerId);

    /**
     * Returns the script triggers associated with the indicated script ID.
     *
     * @param scriptId The script ID for which to locate triggers.
     *
     * @return A list of all {@link ScriptTrigger script triggers} that are associated with the indicated script ID.
     */
    List<ScriptTrigger> getByScriptId(final String scriptId);

    /**
     * Retrieves all triggers associated with the site scope. This is basically a convenience wrapper around the full
     * scope-entity get implemented in {@link #getByScope(Scope, String)}.
     *
     * @return All triggers associated with the site scope.
     * @see #getByScope(Scope, String)
     */
    List<ScriptTrigger> getSiteTriggers();

    /**
     * Retrieves all triggers associated with the site scope and indicated event. This is basically a convenience
     * wrapper around the full scope-entity-event get implemented in {@link #getByScopeEntityAndEvent(Scope, String,
     * String)}.
     *
     * @param event The event associated with the trigger.
     *
     * @return All triggers associated with the site scope and indicated event.
     * @see #getByScopeEntityAndEvent(Scope, String, String)
     */
    @SuppressWarnings("unused")
    ScriptTrigger getSiteTrigger(final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     *
     * @return All triggers associated with the indicated scope and entity.
     * @see #getSiteTriggers()
     */
    List<ScriptTrigger> getByScope(final Scope scope, final String entityId);

    /**
     * Gets all triggers associated with the indicated event.
     * @param eventId    The event associated with the trigger.
     * @return All triggers associated with the indicated event.
     */
    List<ScriptTrigger> getByEvent(final String eventId);

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated association and event. Generally the association
     * is an encoded {@link Scope#code() scope code} and entity ID. This search performs no scope fail-over.
     *
     * @param association The association for the trigger.
     * @param event       The event associated with the trigger.
     *
     * @return All triggers associated with the indicated association and event
     */
    ScriptTrigger getByAssociationAndEvent(final String association, final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     * @param event    The event associated with the trigger.
     *
     * @return The trigger associated with the indicated scope and entity and event, if any.
     */
    ScriptTrigger getByScopeEntityAndEvent(final Scope scope, final String entityId, final String event);

    /**
     * Sets the default trigger ID format. This is used when composing trigger IDs from trigger metadata.
     *
     * @param defaultTriggerIdFormat The format string for composing trigger IDs.
     */
    @SuppressWarnings("unused")
    void setDefaultTriggerIdFormat(final String defaultTriggerIdFormat);

    /**
     * Gets the default trigger name based on a format string.
     * @param scriptId    The script ID for the trigger.
     * @param scope       The scope for the trigger.
     * @param entityId    The associated entity ID.
     * @param event       The event associated with the trigger.
     * @return A trigger name based on the submitted parameters and configured template.
     */
    String getDefaultTriggerName(final String scriptId, final Scope scope, final String entityId, final String event);
}
