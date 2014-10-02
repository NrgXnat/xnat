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
    public abstract ScriptTrigger getByTriggerId(final String triggerId);

    /**
     * Returns the script triggers associated with the indicated script ID.
     *
     * @param scriptId The script ID for which to locate triggers.
     *
     * @return A list of all {@link ScriptTrigger script triggers} that are associated with the indicated script ID.
     */
    public abstract List<ScriptTrigger> getByScriptId(final String scriptId);

    /**
     * Retrieves all triggers associated with the site scope. This is basically a convenience wrapper around the full
     * scope-entity get implemented in {@link #getByScope(Scope, String)}.
     *
     * @return All triggers associated with the site scope.
     * @see #getByScope(Scope, String)
     */
    public abstract List<ScriptTrigger> getSiteTriggers();

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
    public abstract List<ScriptTrigger> getSiteTriggers(final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     *
     * @return All triggers associated with the indicated scope and entity.
     * @see #getSiteTriggers()
     */
    public abstract List<ScriptTrigger> getByScope(final Scope scope, final String entityId);

    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated association and event. Generally the association
     * is an encoded {@link Scope#code() scope code} and entity ID. This search performs no scope fail-over.
     *
     * @param association The association for the trigger.
     * @param event       The event associated with the trigger.
     *
     * @return All triggers associated with the indicated association and event
     */
    public abstract List<ScriptTrigger> getByAssociationAndEvent(final String association, final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     * @param event    The event associated with the trigger.
     *
     * @return All triggers associated with the indicated scope and entity and event
     * @see #getSiteTriggers(String)
     */
    public abstract List<ScriptTrigger> getByScopeEntityAndEvent(final Scope scope, final String entityId, final String event);

    /**
     * Retrieves all triggers for the indicated script, scope, and entity.
     *
     * @param scriptId The script ID for which to locate triggers.
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     * @param event    The event associated with the trigger.
     *
     * @return All triggers associated with the indicated scope and entity and event
     * @see #getSiteTriggers(String)
     */
    public abstract List<ScriptTrigger> getByScriptIdScopeEntityAndEvent(final String scriptId, final Scope scope, final String entityId, final String event);

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scriptId       The script ID for which to locate triggers.
     * @param association    The association for the trigger.
     * @param event          The event associated with the trigger.
     *
     * @return All triggers associated with the indicated scope and entity and event
     * @see #getSiteTriggers(String)
     */
    public abstract List<ScriptTrigger> getByScriptIdAssociationAndEvent(final String scriptId, final String association, final String event);
}
