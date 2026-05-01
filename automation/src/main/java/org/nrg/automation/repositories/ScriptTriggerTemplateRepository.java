/*
 * automation: org.nrg.automation.repositories.ScriptTriggerTemplateRepository
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.repositories;

import lombok.extern.slf4j.Slf4j;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ScriptTriggerRepository class.
 *
 * @author Rick Herrick
 */
@Repository
@Slf4j
public class ScriptTriggerTemplateRepository extends AbstractHibernateDAO<ScriptTriggerTemplate> {
    public List<ScriptTriggerTemplate> getTemplatesForEntity(final String entityId) {
        log.debug("Finding templates associated with the entity ID {}", entityId);
        return createQuery("from org.nrg.automation.entities.ScriptTriggerTemplate as template where :entityId in elements(template.associatedEntities)").setParameter("entityId", entityId).list();
    }

    public List<ScriptTriggerTemplate> getTemplatesForTrigger(ScriptTrigger trigger) {
        log.debug("Finding templates associated with the trigger {}", trigger.getTriggerId());
        return createQuery("from org.nrg.automation.entities.ScriptTriggerTemplate template where :trigger in elements(template.triggers)").setParameter("trigger", trigger).list();
    }
}
