/*
 * org.nrg.xnat.initialization.tasks.MigrateDatabaseTables
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xnat.initialization.tasks;

import com.google.common.base.Joiner;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xnat.services.XnatAppInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class MigrateDatabaseTables extends AbstractInitializingTask {
    @Autowired
    public MigrateDatabaseTables(final JdbcTemplate template, final TransactionTemplate transactionTemplate, final XnatAppInfo appInfo) {
        super();
        _db = new DatabaseHelper(template, transactionTemplate);
        _appInfo = appInfo;
    }

    @Override
    public String getTaskName() {
        return "Migrate XNAT database tables";
    }

    @Override
    public void run() {
        try {
            final Map<String, Map<String, String>> tables = new HashMap<>();
            for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/migration/**/*-tables.properties")) {
                final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
                for (final String property : properties.stringPropertyNames()) {
                    final String[] atoms = property.split("\\.", 2);
                    if (atoms.length < 2) {
                        _log.error("The properties file {} contains a malformed key: {}. Keys in table migration properties files should take the form: \"table.column=column_type\".", resource.getFilename(), property);
                        continue;
                    }

                    final String table  = atoms[0];
                    final String column = atoms[1];

                    final Map<String, String> columns;
                    if (tables.containsKey(table)) {
                        columns = tables.get(table);
                    } else {
                        columns = new HashMap<>();
                        tables.put(table, columns);
                    }
                    if (columns.containsKey(column)) {
                        _log.error("The properties for table {} defines the column {} as column type {}. This column has already been defined elsewhere as type: {}.", table, column, columns.get(column));
                        continue;
                    }
                    columns.put(column, properties.getProperty(property));
                }
            }
            for (final String table : tables.keySet()) {
                final Map<String, String> columns = tables.get(table);
                for (final String column : columns.keySet()) {
                    final String value = columns.get(column);
                    try {
                        _db.setColumnDatatype(table, column, value);
                    } catch (SQLWarning e) {
                        final String message = e.getMessage();
                        if (message.startsWith(SQL_WARNING_TABLE)) {
                            _log.error("The table {} was defined, but that table doesn't appear to exist in the database. The following columns were to be checked: {}", table, Joiner.on(", ").join(columns.keySet()));
                        } else {
                            _log.error("The column {}.{} was defined, but that column doesn't appear to exist. Note that the table migration does not create new columns. The column was defined as: {}", table, column, value);
                        }
                    }
                }
            }
            if (_appInfo.isPrimaryNode()) {
                _log.info("This service is the primary XNAT node, checking whether database updates are required.");

                PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction();
                try {
                    transaction.start();
                    //create the views defined in the display documents
                    _log.info("Initializing database views...");
                    try {
                        transaction.execute(DisplayManager.GetCreateViewsSQL().get(0));
                    }
                    catch(Exception e){
                        transaction.execute(DisplayManager.GetCreateViewsSQL().get(1));//drop all
                        transaction.execute(DisplayManager.GetCreateViewsSQL().get(0));//then try to create all
                    }
                    transaction.commit();
                } catch (Exception e) {
                    try {
                        transaction.rollback();
                    } catch (SQLException e1) {
                        _log.error("", e1);
                    }
                    _log.error("", e);
                    return;
                } finally {
                    transaction.close();
                }

            }
            complete();
        } catch (IOException e) {
            _log.error("An error occurred attempting to read table migration properties files", e);
        } catch (SQLException e) {
            _log.error("An error occurred accessing the database", e);
        }
    }

    private static final Logger _log              = LoggerFactory.getLogger(MigrateDatabaseTables.class);
    private static final String SQL_WARNING_TABLE = "The requested table";

    private final DatabaseHelper _db;
    private final XnatAppInfo _appInfo;
}
