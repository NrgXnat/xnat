/**
 * ScriptTriggerService
 * (C) 2014 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 9/19/2014 by Rick Herrick
 */
package org.nrg.automation.services;

import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.entities.ScriptTriggerTemplate;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ScriptTriggerService class.
 *
 * @author Rick Herrick
 */
public interface ScriptTriggerService extends BaseHibernateService<ScriptTrigger> {
    /**
     * Retrieves the {@link org.nrg.automation.entities.ScriptTrigger trigger} with the indicated name.
     * @param name    The {@link org.nrg.automation.entities.ScriptTrigger#getName() name} of the trigger to retrieve.
     * @return The trigger with the indicated name, if it exists, <b>null</b> otherwise.
     */
    public abstract ScriptTrigger getByName(final String name);
}
