/*
 * automation: org.nrg.automation.services.ScriptService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.services;

import org.nrg.automation.entities.Script;
import org.nrg.framework.orm.hibernate.BaseHibernateService;

import java.util.List;

/**
 * ScriptService interface.
 */
public interface ScriptService extends BaseHibernateService<Script> {
    /**
     * A convenience test for the existence of a script with the indicated script ID.
     *
     * @param scriptId The ID of the script to test for.
     * @return <b>true</b> if a script with the indicated ID exists, <b>false</b> otherwise.
     */
    boolean hasScript(final String scriptId);

    /**
     * Retrieves the {@link Script} with the indicated script ID.
     *
     * @param scriptId The {@link Script#getScriptId() script ID} of the script to retrieve.
     * @return The script with the indicated scriptId, if it exists, <b>null</b> otherwise.
     */
    Script getByScriptId(final String scriptId);

    List<String> getVersions(final String scriptId);

    Object getVersion(final String scriptId, final String version);

    void writeScriptToFile(final String scriptId, final String filePath);
}
