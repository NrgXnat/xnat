package org.nrg.automation.services;

import org.nrg.automation.entities.Script;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

/**
 * ScriptService interface.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu> on 9/25/2014.
 */
public interface ScriptService extends BaseHibernateService<Script> {

    /**
     * A convenience test for the existence of a script with the indicated script ID.
     *
     * @param scriptId The ID of the script to test for.
     *
     * @return <b>true</b> if a script with the indicated ID exists, <b>false</b> otherwise.
     */
    public abstract boolean hasScript(final String scriptId);

    /**
     * Retrieves the {@link Script} with the indicated script ID.
     *
     * @param scriptId The {@link Script#getScriptId() script ID} of the script to retrieve.
     *
     * @return The script with the indicated scriptId, if it exists, <b>null</b> otherwise.
     */
    public abstract Script getByScriptId(final String scriptId);
}
