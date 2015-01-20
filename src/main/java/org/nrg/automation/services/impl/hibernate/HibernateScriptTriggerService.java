/**
 * HibernateScriptTriggerTemplateService
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.services.impl.hibernate;

import org.apache.commons.lang.text.StrSubstitutor;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.repositories.ScriptTriggerRepository;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HibernateScriptTriggerTemplateService class.
 *
 * @author Rick Herrick
 */
@Service
public class HibernateScriptTriggerService extends AbstractHibernateEntityService<ScriptTrigger, ScriptTriggerRepository> implements ScriptTriggerService {

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
            _log.debug("Retrieving script trigger by ID: " + triggerId);
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
     * String)}.
     *
     * @param event The event associated with the trigger.
     *
     * @return All triggers associated with the site scope and indicated event.
     * @see #getByScopeEntityAndEvent(Scope, String, String)
     */
    @Override
    @Transactional
    public ScriptTrigger getSiteTrigger(final String event) {
        return getByScopeEntityAndEvent(Scope.Site, null, event);
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
        ScriptTrigger example = new ScriptTrigger();
        example.setAssociation(Scope.encode(scope, entityId));
        List<ScriptTrigger> results = getDao().findByExample(example, EXCLUDE_PROPS_SCOPE);
        if (_log.isDebugEnabled()) {
            _log.debug("Found {} triggers for scope {} and entity ID {}", results == null ? "no" : results.size(), scope, entityId);
        }
        return results;
    }

    /**
     * Retrieves all triggers for the indicated scope and entity.
     *
     * @param scope    The scope to search.
     * @param entityId The associated entity ID.
     * @param event    The event associated with the trigger.
     *
     * @return All triggers associated with the indicated scope and entity and event
     * @see #getSiteTriggers()
     */
    @Override
    @Transactional
    public ScriptTrigger getByScopeEntityAndEvent(final Scope scope, final String entityId, final String event) {
        return getByAssociationAndEvent(Scope.encode(scope, entityId), event);
    }

    @Override
    public void setDefaultTriggerIdFormat(final String defaultTriggerIdFormat) {
        _defaultTriggerIdTemplate = defaultTriggerIdFormat;
    }

    @Override
    public String getDefaultTriggerName(final String scriptId, final Scope scope, final String entityId, final String event) {
        final Map<String, String> values = new HashMap<String, String>();
        values.put("scriptId", scriptId);
        values.put("event", event.replace(" ", "_").replace("/", "_").replace(":", "_"));
        values.put("association", Scope.encode(scope, entityId).replace(" ", "_").replace("/", "_").replace(":", "_"));
        return new StrSubstitutor(values, "%(", ")").replace(_defaultTriggerIdTemplate);
    }


    /**
     * Retrieves the {@link ScriptTrigger trigger} with the indicated association and event. Generally the association
     * is an encoded {@link Scope#code() scope code} and entity ID. This search performs no scope fail-over.
     *
     * @param association The association for the trigger. Association
     * @param event       The event associated with the trigger.
     *
     * @return The requested script trigger, if it exists.
     */
    @Override
    @Transactional
    public ScriptTrigger getByAssociationAndEvent(final String association, final String event) {
        ScriptTrigger example = new ScriptTrigger();
        example.setAssociation(association);
        example.setEvent(event);
        List<ScriptTrigger> triggers = getDao().findByExample(example, EXCLUDE_PROPS_SCOPE_EVENT);
        if (triggers == null || triggers.size() == 0) {
            if (_log.isDebugEnabled()) {
                _log.debug("Found no trigger for association {} and event {}", association, event);
            }
            return null;
        }
        if (_log.isDebugEnabled()) {
            _log.debug("Found {} triggers for association {} and event {}, should be *1*.", triggers.size(), association, event);
        }
        return triggers.get(0);
    }

    private static final String[] EXCLUDE_PROPS_SCOPE = new String[]{"id", "verified", "created", "timestamp", "disabled", "triggerId", "description", "scriptId", "event"};
    private static final String[] EXCLUDE_PROPS_SCOPE_EVENT = new String[]{"id", "verified", "created", "timestamp", "disabled", "triggerId", "description", "scriptId"};
    private static final String[] EXCLUDE_PROPS_SCRIPT_ID = new String[]{"id", "verified", "created", "timestamp", "disabled", "triggerId", "description", "association", "event"};

    private static final Logger _log = LoggerFactory.getLogger(HibernateScriptTriggerService.class);

    private String _defaultTriggerIdTemplate = "%(scriptId)-%(association)-%(event)";
}
