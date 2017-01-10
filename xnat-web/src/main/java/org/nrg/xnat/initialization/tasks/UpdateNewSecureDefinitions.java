/*
 * web: org.nrg.xnat.initialization.tasks.UpdateNewSecureDefinitions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xft.schema.XFTManager;
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
            // Try to get te XFTManager instance here. If XFT isn't yet initialized, this will throw an XFTException
            // that will get caught below and used to defer processing for this task.
            XFTManager.GetInstance();
            if (ElementSecurity.GetElementSecurities() != null) {
                _log.debug("Found element securities, running update new secure definitions.");
                _featureRepositoryService.updateNewSecureDefinitions();
            }
        } catch (Exception ignore) {
            throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(UpdateNewSecureDefinitions.class);

    private final FeatureRepositoryServiceI _featureRepositoryService;
}
