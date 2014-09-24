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

import org.hibernate.Session;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
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
public class ScriptTriggerTemplateRepository extends AbstractHibernateDAO<ScriptTriggerTemplate> {
    private static final Logger _log = LoggerFactory.getLogger(ScriptTriggerTemplateRepository.class);

    @SuppressWarnings("unchecked")
    public List<ScriptTriggerTemplate> getTemplatesForEntity(final Long entityId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Finding templates associated with the entity ID {}", entityId);
        }
        return getSession().createQuery("from org.nrg.automation.entities.ScriptTriggerTemplate as template where :entityId in elements(template.associatedEntities)").setLong("entityId", entityId).list();
    }

    @SuppressWarnings("unchecked")
    public List<ScriptTriggerTemplate> getTemplatesForTrigger(ScriptTrigger trigger) {
        if (_log.isDebugEnabled()) {
            _log.debug("Finding templates associated with the trigger {}", trigger.getName());
        }
        return getSession().createQuery("from org.nrg.automation.entities.ScriptTriggerTemplate template where :trigger in elements(template.triggers)").setEntity("trigger",  trigger).list();
    }
}
