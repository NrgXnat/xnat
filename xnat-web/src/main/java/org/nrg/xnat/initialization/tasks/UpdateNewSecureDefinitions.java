/*
 * web: org.nrg.xnat.initialization.tasks.UpdateNewSecureDefinitions
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateNewSecureDefinitions extends AbstractInitializingTask {
    @Autowired
    public UpdateNewSecureDefinitions(final FeatureRepositoryServiceI featureRepositoryService) {
        super();
        _featureRepositoryService = featureRepositoryService;
    }

    @Override
    public String getTaskName() {
        return "Update new secure definitions";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            if (ElementSecurity.GetElementSecurities() != null) {
                _log.debug("Found element securities, running update new secure definitions.");
                _featureRepositoryService.updateNewSecureDefinitions();
            }
        } catch (Exception ignore) {
            throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "An error occurred retrieving element security settings. This usually just means they haven't yet been initialized.");
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(UpdateNewSecureDefinitions.class);

    private final FeatureRepositoryServiceI _featureRepositoryService;
}
