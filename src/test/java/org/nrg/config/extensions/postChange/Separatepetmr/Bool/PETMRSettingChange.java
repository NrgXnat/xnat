/*
 * config: org.nrg.config.extensions.postChange.Separatepetmr.Bool.PETMRSettingChange
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.config.extensions.postChange.Separatepetmr.Bool;

import lombok.extern.slf4j.Slf4j;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.impl.DefaultConfigService;
import org.nrg.framework.constants.Scope;

@SuppressWarnings("unused")
@Slf4j
public class PETMRSettingChange implements DefaultConfigService.ConfigurationModificationListenerI {

    public static int getChanges() {
        return _changes;
    }

    @Override
    public void execute(Configuration config) {
        //config is site wide when config.getProject()==null
        log.info("PET-MR setting modified to {} for {}. This is change #{}", config.getContents(), config.getScope() == Scope.Site ? "site-wide configuration" : config.getEntityId(), ++_changes);
    }

    private static int _changes = 0;
}
