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
    private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerRepository.class);

    public ScriptTrigger getByName(final String name) {
        if (_log.isDebugEnabled()) {
            _log.debug("Attempting to find script trigger by name: {}", name);
        }
        Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        criteria.add(Restrictions.eq("name", name));
        List list = criteria.list();
        return (list == null || list.size() == 0) ? null : (ScriptTrigger) list.get(0);
    }
}
