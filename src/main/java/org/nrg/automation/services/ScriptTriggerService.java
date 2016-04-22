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
	
    ScriptTrigger getById(final String id);
    
    ScriptTrigger getByTriggerId(final String triggerId);

    List<ScriptTrigger> getByScriptId(final String scriptId);

    List<ScriptTrigger> getSiteTriggers();

    List<ScriptTrigger> getSiteTriggersForEvent(final String eventClass, final String event);

    List<ScriptTrigger> getByScope(final Scope scope, final String entityId);

    List<ScriptTrigger> getByEvent(final String eventClass, final String eventId);
    
    List<ScriptTrigger> getByEventAndFilters(final String eventClass, final String eventId, final Map<String,String> filterMap);

    List<ScriptTrigger> getByAssociationAndEvent(final String association, final String eventClass, final String event);
    
    List<ScriptTrigger> getByScopeEntityAndEvent(final Scope scope, final String entityId, final String eventClass, final String event);
    
    ScriptTrigger getByScopeEntityAndEventAndFilters(final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,String> filterMap);

    void setDefaultTriggerIdFormat(final String defaultTriggerIdFormat);

    String getDefaultTriggerName(final String scriptId, final Scope scope, final String entityId, final String eventClass, final String event, final Map<String,List<String>> eventFilters);
}
