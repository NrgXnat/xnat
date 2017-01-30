/*
 * web: org.nrg.xnat.initialization.tasks.CreateOrUpdateDatabaseViews
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.servlet.XDATServlet;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import java.sql.SQLException;
import java.util.List;

@Component
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
            _log.info("This service is the primary XNAT node, checking whether database updates are required.");
            final Boolean shouldUpdateViews = XDATServlet.shouldUpdateViews();

            try {
                if (!_helper.tableExists("xdat_search", "xs_item_access") || !XFTManager.isInitialized() || shouldUpdateViews == null) {
                    throw new InitializingTaskException(InitializingTaskException.Level.SingleNotice, "The table 'xdat_search.xs_item_access' does not yet exist. Deferring execution.");
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to access the database to check for the table 'xdat_search.xs_item_access'.", e);
            }

            if (!shouldUpdateViews) {
                _log.info("XDATServlet indicates that views do not need to be updated, terminating task.");
            }

            final PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction();
            try {
                try {
                    transaction.start();
                } catch (SQLException | DBPoolException e) {
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to start the transaction.", e);
                }

                //create the views defined in the display documents
                _log.info("Initializing database views...");
                try {
                    transaction.execute(DisplayManager.GetCreateViewsSQL());
                	_log.info("View initialization complete.");
                } catch (Exception e) {
                	_log.info("View initialization threw exception (" + e.toString() + ").  We'll drop views and rebuild them.");
                    transaction.rollback();
                    transaction.execute(getViewDropSql(_dbUsername));//drop all
                	_log.info("Drop views step complete.  Begin rebuilding views.");
                    transaction.execute(DisplayManager.GetCreateViewsSQL());//then try to create all
                	_log.info("View rebuild complete.");
                }
                try {
                    transaction.commit();
                } catch (SQLException e) {
                    transaction.rollback();
                    throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to commit the transaction.", e);
                }
            } catch (SQLException e) {
                throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred trying to roll back the transaction.", e);
            } finally {
                transaction.close();
            }
        }
    }
    
    private List<String> getViewDropSql(String user) {
    	final List<String> dropSql = Lists.newArrayList();  
    	dropSql.add(
    	"CREATE OR REPLACE FUNCTION find_user_views(username TEXT) " +
    	  "RETURNS TABLE(table_schema NAME, view_name NAME) AS $$ " +
    	"BEGIN " +
    	  "RETURN QUERY " +
    	  "SELECT " +
    	    "n.nspname AS table_schema, " +
    	    "c.relname AS view_name " +
    	  "FROM pg_catalog.pg_class c " +
    	    "LEFT JOIN pg_catalog.pg_namespace n " +
    	      "ON (n.oid = c.relnamespace) " +
    	  "WHERE c.relkind = 'v' " +
    	        "AND c.relowner = (SELECT usesysid " +
    	                          "FROM pg_catalog.pg_user " +
    	                          "WHERE usename = $1); " +
    	"END$$ LANGUAGE plpgsql;");
    	dropSql.add(
    	"CREATE OR REPLACE FUNCTION drop_user_views(username TEXT) " +
    	  "RETURNS INTEGER AS $$ " +
    	"DECLARE " +
    	  "r RECORD; " +
    	  "s TEXT; " +
    	  "c INTEGER := 0; " +
    	"BEGIN " +
    	  "RAISE NOTICE 'Dropping views for user %', $1; " +
    	  "FOR r IN " +
    	    "SELECT * FROM find_user_views($1) " +
    	  "LOOP " +
    	    "S := 'DROP VIEW IF EXISTS ' || quote_ident(r.table_schema) || '.' || quote_ident(r.view_name) || ' CASCADE;'; " +
    	    "EXECUTE s; " +
    	    "c := c + 1; " +
    	    "RAISE NOTICE 's = % ', S; " +
    	  "END LOOP; " +
    	  "RETURN c; " +
    	"END$$ LANGUAGE plpgsql;"
    	);
    	dropSql.add(
    	"SELECT drop_user_views('" + user + "');"
    	);
    	return dropSql;
    }

    private static final Logger _log = LoggerFactory.getLogger(CreateOrUpdateDatabaseViews.class);

    private final XnatAppInfo    _appInfo;
    private final DatabaseHelper _helper;
    private final String _dbUsername;
}
