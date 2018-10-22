/*
 * web: org.nrg.xnat.initialization.tasks.CreateOrUpdateDatabaseViews
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;

@Component
@Slf4j
public class FixMismatchedMappingElements extends AbstractInitializingTask {
    @Autowired
    public FixMismatchedMappingElements(final XnatAppInfo appInfo, final NamedParameterJdbcTemplate template, final TransactionTemplate transactionTemplate) {
        _appInfo = appInfo;
        _helper = new DatabaseHelper(template, transactionTemplate);
    }

    @Override
    public String getTaskName() {
        return "Fixes mismatched field mapping elements.";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (_appInfo.isPrimaryNode()) {
            log.info("This service is the primary XNAT node, checking for mismatched field mapping elements.");

            try {
                Users.getGuest();
                if (!_helper.tablesExist("xdat_field_mapping", "xdat_field_mapping_set", "xdat_element_access", "xdat_user", "xdat_usergroup", "xdat_primary_security_field")) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The tables \"xdat_field_mapping\", \"xdat_field_mapping_set\", \"xdat_element_access\", \"xdat_user\", \"xdat_usergroup\", or \"xdat_primary_security_field\" do not yet exist. Deferring execution.");
                }

                if (!_helper.tablesExist("data_type_views_%")) {
                    final String script = IOUtils.toString(BasicXnatResourceLocator.getResource("classpath:META-INF/xnat/data-type-access-functions.sql").getInputStream(), Charset.defaultCharset());
                    log.info("Initializing data-type access functions with SQL: {}", script);
                    _helper.executeScript(script);
                }
                log.info("Preparing to check for and fix any mismatched data-type permissions.");
                _helper.callFunction("fix_mismatched_data_type_permissions", Boolean.class);
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table 'xdat_search.xs_item_access'.", e);
            } catch (UserNotFoundException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "Didn't find the guest user. Will defer execution until that exists.", e);
            } catch (UserInitException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to retrieve the guest user. This isn't just that the user doesn't exist, so may indicate a more serious issue.", e);
            } catch (IOException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to retrieve the data type access views and functions SQL. This isn't just that it wasn't found, so may indicate a more serious issue.", e);
            }
        }
    }

    private final XnatAppInfo                _appInfo;
    private final DatabaseHelper             _helper;
}
