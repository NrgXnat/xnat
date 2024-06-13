/*
 * automation: org.nrg.automation.repositories.ScriptTriggerRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.repositories;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ScriptTriggerRepository class.
 *
 * @author Rick Herrick
 */
@Repository
public class ScriptTriggerRepository extends AbstractHibernateDAO<ScriptTrigger> {
     
     /** The Constant _log. */
     private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerRepository.class);

	/**
	 * Update old style triggers.
	 */
	// This method will update any pre-1.7 ScriptTriggers, which were based on workflows
    public void updateOldStyleTriggers() {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.isNull("srcEventClass"));
        @SuppressWarnings("unchecked")
		final List<ScriptTrigger> list = criteria.list();
        for (final ScriptTrigger trigger : list) {
        	trigger.setSrcEventClass(ScriptTrigger.DEFAULT_CLASS);
        	trigger.setEventFiltersAsMap(ScriptTrigger.DEFAULT_FILTERS);
        	getSession().saveOrUpdate(trigger);
        }
    }

    /**
     * Gets the by id.
     *
     * @param id the id
     * @return the by id
     */
    public ScriptTrigger getById(final String id) {
        _log.debug("Attempting to find script trigger by ID: {}", id);
        try {
            final Criteria criteria = getCriteriaForType();
            criteria.add(Restrictions.eq("enabled", true));
            criteria.add(Restrictions.eq("id", Long.valueOf(id)));
            final List list = criteria.list();
            if (list == null || list.isEmpty()) {
                _log.warn("Requested script trigger with ID {}, but that doesn't exist.", id);
                return null;
            }
            final ScriptTrigger trigger = (ScriptTrigger) list.get(0);
            _log.debug("Requested trigger of ID {} with event {} was found.", id, trigger.getEvent());
            return trigger;
        } catch (NumberFormatException e) {
            _log.error("The specified ID value {} can't be converted to a valid ID.", id);
        	return null;
        }
    }

    /**
     * Gets the by trigger id.
     *
     * @param triggerId the trigger id
     * @return the by trigger id
     */
    public ScriptTrigger getByTriggerId(final String triggerId) {
        _log.debug("Attempting to find script trigger by trigger ID: {}", triggerId);
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        criteria.add(Restrictions.eq("triggerId", triggerId));
        final List list = criteria.list();
        if (list == null || list.isEmpty()) {
            _log.warn("Requested script trigger with ID {}, but that doesn't exist.", triggerId);
            return null;
        }
        final ScriptTrigger trigger = (ScriptTrigger) list.get(0);
        _log.debug("Requested trigger of ID {} with event {} was found.", triggerId, trigger.getEvent());
        return trigger;
    }
}
