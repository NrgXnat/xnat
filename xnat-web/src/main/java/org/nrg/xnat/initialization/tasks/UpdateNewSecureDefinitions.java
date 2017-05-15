/*
 * web: org.nrg.xnat.initialization.tasks.UpdateNewSecureDefinitions
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xft.schema.XFTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class UpdateNewSecureDefinitions extends AbstractInitializingTask {
    @Autowired
    public UpdateNewSecureDefinitions(final JdbcTemplate template, final FeatureRepositoryServiceI featureRepositoryService) {
        super();
        _helper = new DatabaseHelper(template);
        _featureRepositoryService = featureRepositoryService;
    }

    @Override
    public String getTaskName() {
        return "Update new secure definitions";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            if (!_helper.tableExists("xdat_element_security")) {
                throw new InitializingTaskException(InitializingTaskException.Level.RequiresInitialization);
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An SQL error occurred trying to test for the existence of the xdat_user table.", e);
        }

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

    private final DatabaseHelper            _helper;
    private final FeatureRepositoryServiceI _featureRepositoryService;
}
