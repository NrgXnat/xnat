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
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CreateOrUpdateDatabaseViews extends AbstractInitializingTask {
    @Autowired
    public CreateOrUpdateDatabaseViews(final XnatAppInfo appInfo, final JdbcTemplate template, @Qualifier("dbUsername") String dbUsername) {
        _appInfo = appInfo;
        _helper = new DatabaseHelper(template);
        _dbUsername = dbUsername;
    }

    @Override
    public String getTaskName() {
        return "Create or update database views";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        if (_appInfo.isPrimaryNode()) {
            log.info("This service is the primary XNAT node, checking whether database updates are required.");
            final Boolean shouldUpdateViews = XDATServlet.shouldUpdateViews();

            try {
                if (!_helper.tableExists("xdat_search", "xs_item_access") || !XFTManager.isInitialized() || shouldUpdateViews == null) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The table 'xdat_search.xs_item_access' does not yet exist. Deferring execution.");
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table 'xdat_search.xs_item_access'.", e);
            }

            try {
                if (_helper.tableExists("xs_item_cache") && StringUtils.isBlank(_helper.columnExists("xs_item_cache", "id"))) {
                    _helper.getJdbcTemplate().update(PoolDBUtils.QUERY_ITEM_CACHE_ADD_ID);
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table and column 'xs_item_cache.id'.", e);
            }

            if (!shouldUpdateViews) {
                log.info("XDATServlet indicates that views do not need to be updated, terminating task.");
                return;
            }

            try (final PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction()) {
                try {
                    transaction.start();
                } catch (SQLException | DBPoolException e) {
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to start the transaction.", e);
                }

                //create the views defined in the display documents
                log.info("Initializing database views...");
                try {
                    transaction.execute(DisplayManager.GetCreateViewsSQL());
                    log.info("View initialization complete.");
                } catch (Exception e) {
                    log.info("View initialization threw exception ({}).  We'll drop views and rebuild them.", e.toString());
                    transaction.rollback();
                    transaction.execute(SQLUpdateGenerator.getViewDropSql(_dbUsername));//drop all
                    log.info("Drop views step complete.  Begin rebuilding views.");
                    transaction.execute(DisplayManager.GetCreateViewsSQL());//then try to create all
                    log.info("View rebuild complete.");
                }
                try {
                    transaction.commit();
                } catch (SQLException e) {
                    transaction.rollback();
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to commit the transaction.", e);
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to roll back the transaction.", e);
            }
        }
    }

    private final XnatAppInfo    _appInfo;
    private final DatabaseHelper _helper;
    private final String         _dbUsername;
}
