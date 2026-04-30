/*
 * automation: org.nrg.automation.services.impl.hibernate.HibernateScriptTriggerTemplateService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services.impl.hibernate;

import lombok.extern.slf4j.Slf4j;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.automation.repositories.ScriptTriggerTemplateRepository;
import org.nrg.automation.services.AutomationService;
import org.nrg.automation.services.ScriptTriggerTemplateService;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * HibernateScriptTriggerTemplateService class.
 *
 * @author Rick Herrick
 */
@Service
@Slf4j
public class HibernateScriptTriggerTemplateService extends AbstractHibernateEntityService<ScriptTriggerTemplate, ScriptTriggerTemplateRepository> implements ScriptTriggerTemplateService {
    private final boolean automationEnabled;

    @Autowired
    public HibernateScriptTriggerTemplateService(final AutomationService automationService) {
        automationEnabled = automationService.getAutomationEnabled();
    }

    @Override
    @Transactional
    public List<ScriptTriggerTemplate> getTemplatesForEntity(final String entityId) {
        if (!automationEnabled) {
            return Collections.emptyList();
        }
        log.debug("Finding templates associated with the entity ID {}", entityId);
        return getDao().getTemplatesForEntity(entityId);
    }

    @Override
    @Transactional
    public List<ScriptTriggerTemplate> getTemplatesForTrigger(ScriptTrigger trigger) {
        if (!automationEnabled) {
            return Collections.emptyList();
        }
        log.debug("Finding templates associated with the trigger {}", trigger.getTriggerId());
        return getDao().getTemplatesForTrigger(trigger);
    }

    @Override
    @Transactional
    public ScriptTriggerTemplate getByName(String name) {
        if (!automationEnabled) {
            return null;
        }
        log.debug("Finding template with the name {}", name);
        return getDao().findByUniqueProperty("name", name);
    }
}
