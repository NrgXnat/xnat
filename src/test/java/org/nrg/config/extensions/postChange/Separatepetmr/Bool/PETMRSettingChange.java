/*
 * config: org.nrg.config.extensions.postChange.Separatepetmr.Bool.PETMRSettingChange
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.extensions.postChange.Separatepetmr.Bool;

import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.constants.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class PETMRSettingChange implements DefaultConfigService.ConfigurationModificationListenerI {

    public static int getChanges() {
        return _changes;
    }

    @Override
    public void execute(Configuration config) throws ConfigServiceException {
        //config is site wide when config.getProject()==null
        _log.info("PET-MR setting modified to " + config.getContents() + " for " + (config.getScope() == Scope.Site ? "site-wide configuration" : config.getEntityId()) + ". This is change #" + ++_changes);
    }

    private static final Logger _log = LoggerFactory.getLogger(PETMRSettingChange.class);

    private static int _changes = 0;
}
