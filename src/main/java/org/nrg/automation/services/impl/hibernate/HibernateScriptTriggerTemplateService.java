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

import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.automation.repositories.ScriptTriggerTemplateRepository;
import org.nrg.automation.services.ScriptTriggerTemplateService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * HibernateScriptTriggerTemplateService class.
 *
 * @author Rick Herrick
 */
@Service
public class HibernateScriptTriggerTemplateService extends AbstractHibernateEntityService<ScriptTriggerTemplate, ScriptTriggerTemplateRepository> implements ScriptTriggerTemplateService {
    private static final Logger _log = LoggerFactory.getLogger(HibernateScriptTriggerTemplateService.class);

    @Override
    @Transactional
    public List<ScriptTriggerTemplate> getTemplatesForEntity(final String entityId) {
        if (_log.isDebugEnabled()) {
            _log.debug("Finding templates associated with the entity ID {}", entityId);
        }
        return getDao().getTemplatesForEntity(entityId);
    }

    @Override
    @Transactional
    public List<ScriptTriggerTemplate> getTemplatesForTrigger(ScriptTrigger trigger) {
        if (_log.isDebugEnabled()) {
            _log.debug("Finding templates associated with the trigger {}", trigger.getTriggerId());
        }
        return getDao().getTemplatesForTrigger(trigger);
    }

    @Override
    @Transactional
    public ScriptTriggerTemplate getByName(String name) {
        if (_log.isDebugEnabled()) {
            _log.debug("Finding template with the name {}", name);
        }
        return getDao().findByUniqueProperty("name", name);
    }
}
