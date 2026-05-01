/*
 * automation: org.nrg.automation.repositories.ScriptTriggerRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.repositories;

import lombok.extern.slf4j.Slf4j;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

/**
 * ScriptTriggerRepository class.
 *
 * @author Rick Herrick
 */
@Repository
@Slf4j
public class ScriptTriggerRepository extends AbstractHibernateDAO<ScriptTrigger> {
    /**
     * Update old style triggers.
     */
    // This method will update any pre-1.7 ScriptTriggers, which were based on workflows
    public void updateOldStyleTriggers() {
        findByProperty("srcEventClass", null).forEach(trigger -> {
            trigger.setSrcEventClass(ScriptTrigger.DEFAULT_CLASS);
            trigger.setEventFiltersAsMap(ScriptTrigger.DEFAULT_FILTERS);
            getSession().saveOrUpdate(trigger);
        });
    }

    /**
     * Gets the by id.
     *
     * @param id the id
     *
     * @return the by id
     */
    public ScriptTrigger getById(final String id) {
        log.debug("Attempting to find script trigger by ID: {}", id);
        ScriptTrigger trigger = findByUniqueProperty("id", Long.valueOf(id));
        if (trigger == null) {
            log.warn("Requested script trigger with ID {}, but that doesn't exist.", id);
            return null;
        }
        log.debug("Requested trigger of ID {} with event {} was found.", id, trigger.getEvent());
        return trigger;
    }

    /**
     * Gets the by trigger id.
     *
     * @param triggerId the trigger id
     *
     * @return the by trigger id
     */
    public ScriptTrigger getByTriggerId(final String triggerId) {
        log.debug("Attempting to find script trigger by trigger ID: {}", triggerId);
        ScriptTrigger trigger = findByUniqueProperty("triggerId", triggerId);
        if (trigger == null) {
            log.warn("Requested script trigger with trigger ID {}, but that doesn't exist.", triggerId);
            return null;
        }
        log.debug("Requested trigger with trigger ID {} with event {} was found.", triggerId, trigger.getEvent());
        return trigger;
    }
}
