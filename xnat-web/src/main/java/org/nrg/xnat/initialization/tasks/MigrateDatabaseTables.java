/*
 * web: org.nrg.xnat.initialization.tasks.MigrateDatabaseTables
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
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
                            log.info("Preparing to migrate the column {}.{} to {}", table, column, value);
                            _db.setColumnDatatype(table, column, value);
                        } catch (SQLWarning e) {
                            final String message = e.getMessage();
                            if (message.startsWith(SQL_WARNING_TABLE)) {
                                log.info("The table {} was defined, but that table doesn't appear to exist in the database. The following columns were to be checked: {}", table, String.join(", ", columns.keySet()));
                            } else {
                                log.error("The column {}.{} was defined, but that column doesn't appear to exist. Note that the table migration does not create new columns. The column was defined as: {}", table, column, value);
                            }
                        }
                    }
                }
            }
            for (final String table : _constraints.keySet()) {
                final Map<String, List<String>> constraints = _constraints.get(table);
                for (final String constraintKey : constraints.keySet()) {
                    final String  constraint;
                    final Matcher columnKey = COLUMNS_KEY.matcher(constraintKey);
                    if (columnKey.matches()) {
                        final List<String> existing = Arrays.asList(StringUtils.split(columnKey.group("columns"), "-"));
                        if (log.isDebugEnabled()) {
                            log.debug("Searching for table {} constraint with columns {}", table, String.join(", ", existing));
                        }
                        try {
                            constraint = _db.getJdbcTemplate().queryForObject(String.format(QUERY_CONSTRAINT_ID, table, String.join("', '", existing)), String.class);
                        } catch (EmptyResultDataAccessException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Searched for table {} constraint with columns {}, but didn't find a match.", table, String.join(", ", existing));
                            }
                            continue;
                        }
                    } else {
                        constraint = constraintKey;
                    }

                    log.debug("Working on {} constraint {}", table, constraint);
                    final boolean      exists  = _db.getParameterizedTemplate().queryForObject(QUERY_CONSTRAINT_EXISTS, new MapSqlParameterSource("table", table).addValue("constraint", constraint), Boolean.class);
                    final List<String> columns = constraints.get(constraintKey);
                    if (columns.isEmpty()) {
                        // This means delete the constraint
                        if (!exists) {
                            log.debug("I was asked to drop the constraint {} on table {}, but it doesn't exist.", table, constraint);
                            continue;
                        }
                        final int affected = _db.getJdbcTemplate().update(String.format(QUERY_DROP_CONSTRAINT, table, constraint));
                        log.info("Dropped constraint {} from table {}, affected {} items", constraint, table, affected);
                    } else if (exists) {
                        final int affected = _db.getJdbcTemplate().update(String.format(QUERY_MODIFY_CONSTRAINT, table, constraint, String.join(", ", columns)));
                        log.info("Altered constraint {} on table {}, affected {} items, now includes columns {}", constraint, table, affected, String.join(", ", columns));
                    } else {
                        final int affected = _db.getJdbcTemplate().update(String.format(QUERY_ADD_CONSTRAINT, table, constraint, String.join(", ", columns)));
                        log.info("Added constraint {} on table {}, affected {} items, includes columns {}", constraint, table, affected, String.join(", ", columns));
                    }
                }
            }
        } catch (SQLException e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred accessing the database", e);
        }
    }

    private void findTransformsAndColumns() throws IOException {
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/migration/**/*-migrations.ini")) {
            final URI uri = resource.getURI();
            try {
                final INIConfiguration ini = getIniConfiguration(resource);
                getTransforms(uri, ini.getSection("transforms"));
                getColumns(uri, ini.getSection("columns"));
                getConstraints(uri, ini.getSection("constraints"));
            } catch (ConfigurationException e) {
                log.error("The initialization file {} contains a configuration error", uri, e);
            }
        }
    }

    private INIConfiguration getIniConfiguration(final Resource resource) throws IOException, ConfigurationException {
        final INIConfiguration ini = new INIConfiguration();
        log.debug("Reading configuration from {}", resource.getURI());
        ini.read(new InputStreamReader(resource.getInputStream()));
        return ini;
    }

    private void getTransforms(final URI resourceUri, final SubnodeConfiguration configuration) {
        final Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            final String transformId = keys.next();
            if (_transforms.containsKey(transformId)) {
                log.error("There is already a transform definition for the ID {} from the file {}. This transform will be ignored!", transformId, resourceUri);
                continue;
            }
            final String transformClassName = configuration.getString(transformId);
            try {
                final Class<?> transformClass = Class.forName(transformClassName);
                if (!Callable.class.isAssignableFrom(transformClass)) {
                    log.error("The class specified for transform {} is not of type Callable: {}. This transform will be ignored!", transformId, transformClassName);
                    return;
                }
                final Constructor<?> constructor = transformClass.getConstructor(DatabaseHelper.class, String.class, String.class);
                try {
                    final Method   call       = transformClass.getMethod("call");
                    final Class<?> returnType = call.getReturnType();
                    if (!String.class.isAssignableFrom(returnType)) {
                        log.error("The class specified for transform {} is not of type Callable<String>: {}. This transform will be ignored!", transformId, transformClassName);
                        return;
                    }
                } catch (NoSuchMethodException | SecurityException ignore) {
                    // Actually this can't happen: we already checked that it was Callable and so the class
                    // couldn't have compiled without an accessible call() method.
                }
                //noinspection unchecked
                _transforms.put(transformId, (Constructor<? extends Callable<String>>) constructor);
            } catch (ClassNotFoundException e) {
                log.error("The class specified for transform {} was not found: {}. This transform will be ignored!", transformId, transformClassName);
            } catch (NoSuchMethodException e) {
                log.error("The class specified for transform {} does not have a constructor that takes a DatabaseHelper object and two strings (the table and column names to be transformed: {}. This transform will be ignored!", transformId, transformClassName);
            }
        }
    }

    private void getColumns(final URI resourceUri, final SubnodeConfiguration configuration) {
        final Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            final String columnId = keys.next();

            final Matcher matcher = COMPOUND_KEY.matcher(columnId);
            if (!matcher.matches()) {
                log.error("The properties file {} contains a malformed key: {}. Keys in in the [columns] section of table migration properties files should take the form \"table.column=column_type\" or \"table.column=transform:scriptId\".", resourceUri, columnId);
                continue;
            }

            final String table  = matcher.group("prefix");
            final String column = matcher.group("payload");

            if (!_columns.containsKey(table)) {
                _columns.put(table, new HashMap<>());
            }
            final Map<String, String> columns = _columns.get(table);

            final String columnType = configuration.getString(columnId);
            if (columns.containsKey(column)) {
                log.error("The properties for table {} defines the column {} as column type {}. This column has already been defined elsewhere as type: {}.", table, column, columnType, columns.get(column));
                continue;
            }
            columns.put(column, columnType);
        }
    }

    private void getConstraints(final URI resourceUri, final SubnodeConfiguration configuration) {
        final Iterator<String> keys = configuration.getKeys();
        while (keys.hasNext()) {
            final String constraintId = keys.next();

            final Matcher keyMatcher = COMPOUND_KEY.matcher(constraintId);
            if (!keyMatcher.matches()) {
                log.error("The properties file {} contains a malformed key: {}. Keys in in the [constraints] section of table migration properties files should take the form \"table.constraintId=column1, column2, ..., columnN\" or \"table.columns-column1-column2-columnN=columnA, columnB, ...\".", resourceUri, constraintId);
                continue;
            }

            final String table      = keyMatcher.group("prefix");
            final String constraint = keyMatcher.group("payload");

            if (!_constraints.containsKey(table)) {
                _constraints.put(table, new HashMap<>());
            }

            final Map<String, List<String>> constraints = _constraints.get(table);
            if (constraints.containsKey(constraint)) {
                log.error("The constraint {} for table {} has already been defined elsewhere with columns: {}.", constraint, table, String.join(", ", constraints.get(constraint)));
                continue;
            }

            final String       definition = configuration.getString(constraintId);
            final List<String> columns;
            if (StringUtils.isBlank(definition)) {
                log.debug("The {} constraint {} has no definition, which means the constraint should be deleted.", table, constraintId);
                columns = Collections.emptyList();
            } else {
                columns = Arrays.asList(StringUtils.split(definition, ", "));
            }
            constraints.put(constraint, columns);
        }
    }

    private void transform(final String table, final String column, final String value) {
        if (!_transforms.containsKey(value)) {
            log.error("The transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
            return;
        }
        final Constructor<? extends Callable<String>> constructor = _transforms.get(value);
        try {
            final Callable<String> transform = constructor.newInstance(_db, table, column);
            final String           result    = _db.executeTransaction(transform);
            if (!StringUtils.isBlank(result)) {
                log.info("Something abnormal occurred while executing the transform. This may be OK, but verify the transform result in light of this message: {}", result);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("The transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
        } catch (Exception e) {
            log.error("An error occurred while performing the transform {} specified for column {}.{} was not found. This column will not be transformed!", value, table, column);
        }
    }

    private static final String  SQL_WARNING_TABLE       = "The requested table";
    private static final Pattern COMPOUND_KEY            = Pattern.compile("^(?<prefix>[^:]+)\\.\\.(?<payload>[^:]+)$");
    private static final Pattern COLUMNS_KEY             = Pattern.compile("^columns-(?<columns>[a-z0-9_-]+)$");
    private static final String  QUERY_CONSTRAINT_EXISTS = "SELECT EXISTS(SELECT constraint_name FROM information_schema.table_constraints WHERE table_name = :table AND constraint_name = :constraint AND constraint_type = 'UNIQUE')";
    private static final String  QUERY_ADD_CONSTRAINT    = "ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)";
    private static final String  QUERY_DROP_CONSTRAINT   = "ALTER TABLE %s DROP CONSTRAINT %s";
    private static final String  QUERY_MODIFY_CONSTRAINT = "ALTER TABLE %1$s DROP CONSTRAINT %2$s, ADD CONSTRAINT %2$s UNIQUE (%3$s)";
    private static final String  QUERY_CONSTRAINT_ID     = "WITH " +
                                                           "    unique_constraints AS ( " +
                                                           "        SELECT " +
                                                           "            t.table_name, " +
                                                           "            t.constraint_name, " +
                                                           "            ARRAY_AGG(u.column_name::TEXT) AS columns " +
                                                           "        FROM " +
                                                           "            information_schema.table_constraints t " +
                                                           "                LEFT JOIN information_schema.constraint_column_usage u ON t.table_name = u.table_name AND t.constraint_name = u.constraint_name " +
                                                           "        WHERE " +
                                                           "            constraint_type = 'UNIQUE' " +
                                                           "        GROUP BY " +
                                                           "            t.table_name, " +
                                                           "            t.constraint_name) " +
                                                           "SELECT " +
                                                           "    constraint_name " +
                                                           "FROM " +
                                                           "    unique_constraints " +
                                                           "WHERE " +
                                                           "    table_name = '%1$s' AND " +
                                                           "    columns <@ ARRAY ['%2$s'] AND " +
                                                           "    columns @> ARRAY ['%2$s']";
    /*
    I wrote the query above to work with the parameterized template like so:
                                                           "    table_name = :table AND " +
                                                           "    columns <@ ARRAY [:columns] AND " +
                                                           "    columns @> ARRAY [:columns]";
    This failed with an error saying "No value supplied for the SQL parameter 'columns]': No value registered for key 'columns]'.
    This seems like a bug in Spring JDBC, so I ended up doing the cheap fix with String.format().
    */

    private final DatabaseHelper                                       _db;
    private final Map<String, Constructor<? extends Callable<String>>> _transforms  = new HashMap<>();
    private final Map<String, Map<String, String>>                     _columns     = new HashMap<>();
    private final Map<String, Map<String, List<String>>>               _constraints = new HashMap<>();
}
