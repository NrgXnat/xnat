/*
 * web: org.nrg.xnat.initialization.tasks.MigrateDatabaseTables
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import com.google.common.base.Joiner;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
public class MigrateDatabaseTables extends AbstractInitializingTask {
    @Autowired
    public MigrateDatabaseTables(final JdbcTemplate template, final TransactionTemplate transactionTemplate) throws IOException {
        super();
        _db = new DatabaseHelper(template, transactionTemplate);
        findTransformsAndColumns();
    }

    @Override
    public String getTaskName() {
        return "Migrate XNAT database tables";
    }

    @Override
    protected void callImpl() throws InitializingTaskException {
        try {
            for (final String table : _columns.keySet()) {
                final Map<String, String> columns = _columns.get(table);
                for (final String column : columns.keySet()) {
                    final String value = columns.get(column);
                    if (value.startsWith("transform:")) {
                        transform(table, column, value.replaceAll("transform:", ""));
                    } else {
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
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred accessing the database", e);
        }
    }

    private void findTransformsAndColumns() throws IOException {
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/migration/**/*-tables.ini")) {
            final INIConfiguration ini = new INIConfiguration();
            try {
                ini.read(new InputStreamReader(resource.getInputStream()));
            } catch (ConfigurationException e) {
                _log.error("The initialization file " + resource.getURI().toString() + " contains a configuration error", e);
                continue;
            }

            final SubnodeConfiguration transformConfig = ini.getSection("transforms");
            final Iterator<String> transformKeys = transformConfig.getKeys();
            while (transformKeys.hasNext()) {
                final String transformId = transformKeys.next();
                if (_transforms.containsKey(transformId)) {
                    _log.error("There is already a transform definition for the ID {} from the file {}. This transform will be ignored!", transformId, resource.getURI());
                }
                final String transformClassName = transformConfig.getString(transformId);
                try {
                    final Class<?> transformClass = Class.forName(transformClassName);
                    if (!Callable.class.isAssignableFrom(transformClass)) {
                        _log.error("The class specified for transform {} is not of type Callable: {}. This transform will be ignored!", transformId, transformClassName);
                    } else {
                        final Constructor<?> constructor = transformClass.getConstructor(DatabaseHelper.class, String.class, String.class);
                        try {
                            final Method call = transformClass.getMethod("call");
                            final Class<?> returnType = call.getReturnType();
                            if (!String.class.isAssignableFrom(returnType)) {
                                _log.error("The class specified for transform {} is not of type Callable<String>: {}. This transform will be ignored!", transformId, transformClassName);
                            } else {
                                //noinspection unchecked
                                _transforms.put(transformId, (Constructor<? extends Callable<String>>) constructor);
                            }
                        } catch (NoSuchMethodException | SecurityException ignore) {
                            // Actually this can't happen: we already checked that it was Callable and so the class
                            // couldn't have compiled without an accessible call() method.
                        }
                    }
                } catch (ClassNotFoundException e) {
                    _log.error("The class specified for transform {} was not found: {}. This transform will be ignored!", transformId, transformClassName);
                } catch (NoSuchMethodException e) {
                    _log.error("The class specified for transform {} does not have a constructor that takes a DatabaseHelper object and two strings (the table and column names to be transformed: {}. This transform will be ignored!", transformId, transformClassName);
                }
            }

            final SubnodeConfiguration columnsConfig = ini.getSection("columns");
            final Iterator<String> columnsKeys = columnsConfig.getKeys();
            while (columnsKeys.hasNext()) {
                final String columnId = columnsKeys.next();

                // We split on one or more periods because of how commons config treats periods as delimiters, see
                // https://commons.apache.org/proper/commons-configuration/userguide/howto_hierarchical.html#Escaping_special_characters
                final String[] atoms = columnId.split("\\.+", 2);
                if (atoms.length < 2) {
                    _log.error("The properties file {} contains a malformed key: {}. Keys in table migration properties files should take the form \"table.column=column_type\" or \"table.column=transform:scriptId\".", resource.getFilename(), columnId);
                    continue;
                }

                final String table = atoms[0];
                final String column = atoms[1];

                final Map<String, String> columns;
                if (_columns.containsKey(table)) {
                    columns = _columns.get(table);
                } else {
                    columns = new HashMap<>();
                    _columns.put(table, columns);
                }
                if (columns.containsKey(column)) {
                    _log.error("The properties for table {} defines the column {} as column type {}. This column has already been defined elsewhere as type: {}.", table, column, columns.get(column));
                    continue;
                }
                columns.put(column, columnsConfig.getString(columnId));
            }
        }
    }

    private void transform(final String table, final String column, final String value) {
        if (!_transforms.containsKey(value)) {
            _log.error("The transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
            return;
        }
        final Constructor<? extends Callable<String>> constructor = _transforms.get(value);
        try {
            final Callable<String> transform = constructor.newInstance(_db, table, column);
            final String result = _db.executeTransaction(transform);
            if (!StringUtils.isBlank(result)) {
                _log.warn("Something abnormal occurred while executing the transform. This may be OK, but verify the transform result in light of this message: {}", result);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            _log.error("The transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
        } catch (Exception e) {
            _log.error("An error occurred while performing the transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
        }
    }

    private static final Logger _log              = LoggerFactory.getLogger(MigrateDatabaseTables.class);
    private static final String SQL_WARNING_TABLE = "The requested table";

    private final DatabaseHelper _db;
    private final Map<String, Constructor<? extends Callable<String>>> _transforms = new HashMap<>();
    private final Map<String, Map<String, String>>                     _columns    = new HashMap<>();
}
