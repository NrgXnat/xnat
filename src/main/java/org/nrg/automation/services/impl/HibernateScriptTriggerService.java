/**
 * HibernateScriptTriggerTemplateService
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.services.impl;

import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.automation.repositories.ScriptTriggerRepository;
import org.nrg.automation.repositories.ScriptTriggerTemplateRepository;
import org.nrg.automation.services.ScriptTriggerService;
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
public class HibernateScriptTriggerService extends AbstractHibernateEntityService<ScriptTrigger, ScriptTriggerRepository> implements ScriptTriggerService {
    private static final Logger _log = LoggerFactory.getLogger(HibernateScriptTriggerService.class);

    @Override
    @Transactional
    public ScriptTrigger getByName(String name) {
        return getDao().getByName(name);
    }
}
