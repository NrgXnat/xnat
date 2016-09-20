/*
 * org.nrg.automation.services.ScriptTriggerTemplateService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import java.util.List;

/**
 * ScriptTriggerService class.
 *
 * @author Rick Herrick
 */
public interface ScriptTriggerTemplateService extends BaseHibernateService<ScriptTriggerTemplate> {
    /**
     * Returns all of the templates that are associated with the indicated entity ID. This means that the value for the
     * entity ID is contained in the {@link org.nrg.automation.entities.ScriptTriggerTemplate#getAssociatedEntities()}
     * list.
     * @param entityId    The entity ID to be queried.
     * @return A list of any {@link org.nrg.automation.entities.ScriptTriggerTemplate templates} associated with the
     * submitted entity ID.
     */
    List<ScriptTriggerTemplate> getTemplatesForEntity(final String entityId);
    /**
     * Returns all of the templates with which the indicated {@link org.nrg.automation.entities.ScriptTrigger} is
     * associated. This means that the trigger is contained in the {@link org.nrg.automation.entities.ScriptTriggerTemplate#getTriggers()}
     * list.
     * @param     trigger    The trigger to be queried on.
     * @return A list of any {@link org.nrg.automation.entities.ScriptTriggerTemplate templates} associated with the
     * submitted trigger.
     */
    List<ScriptTriggerTemplate> getTemplatesForTrigger(final ScriptTrigger trigger);
    /**
     * Gets the script trigger template with the indicated name.
     * @param name    The name of the template to retrieve.
     * @return The template with the indicated name if it exists, <b>null</b> otherwise.
     */
    ScriptTriggerTemplate getByName(final String name);
}
