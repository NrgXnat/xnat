/**
 * ScriptTriggerRepository
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.repositories;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.services.ScriptTriggerService;
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
     private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerRepository.class);

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

    public ScriptTrigger getById(final String id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Attempting to find script trigger by ID: {}", id);
        }
        Long longId;
        try {
        	longId = Long.valueOf(id);
        } catch (NumberFormatException e) {
        	return null;
        }
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        criteria.add(Restrictions.eq("id", longId));
        final List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (ScriptTrigger) list.get(0);
    }

    public ScriptTrigger getByTriggerId(final String triggerId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Attempting to find script trigger by trigger ID: {}", triggerId);
        }
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        criteria.add(Restrictions.eq("triggerId", triggerId));
        final List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (ScriptTrigger) list.get(0);
    }
}
