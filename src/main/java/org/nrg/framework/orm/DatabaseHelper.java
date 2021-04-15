/*
 * framework: org.nrg.framework.orm.DatabaseHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.postgresql.util.PGInterval;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Getter
@Accessors(prefix = "_")
@Slf4j
@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class DatabaseHelper {
    public DatabaseHelper(final DataSource dataSource) {
        this(dataSource, null, null, null);
    }

    public DatabaseHelper(final JdbcTemplate template) {
        this(template.getDataSource(), template, null, null);
    }

    public DatabaseHelper(final NamedParameterJdbcTemplate parameterized) {
        this(((JdbcAccessor) parameterized.getJdbcOperations()).getDataSource(), null, parameterized, null);
    }

    public DatabaseHelper(final DataSource dataSource, final TransactionTemplate transactionTemplate) {
        this(dataSource, null, null, transactionTemplate);
    }

    public DatabaseHelper(final JdbcTemplate template, final TransactionTemplate transactionTemplate) {
        this(template.getDataSource(), template, null, transactionTemplate);
    }

    public DatabaseHelper(final NamedParameterJdbcTemplate template, final TransactionTemplate transactionTemplate) {
        this(((JdbcAccessor) template.getJdbcOperations()).getDataSource(), null, template, transactionTemplate);
    }

    private DatabaseHelper(@Nonnull final DataSource dataSource, final JdbcTemplate template, final NamedParameterJdbcTemplate parameterized, final TransactionTemplate transactionTemplate) {
        _jdbcTemplate = ObjectUtils.defaultIfNull(template, new JdbcTemplate(dataSource));
        _parameterizedTemplate = ObjectUtils.defaultIfNull(parameterized, new NamedParameterJdbcTemplate(dataSource));
        _transactionTemplate = transactionTemplate;
    }

    public static long convertPGIntervalToSeconds(final String expression) {
        try {
            final PGInterval interval = new PGInterval(expression);
            return ((long) interval.getYears()) * 31536000L +
                   ((long) interval.getMonths()) * 2592000L +
                   ((long) interval.getDays()) * 86400L +
                   ((long) interval.getHours()) * 3600L +
                   ((long) interval.getMinutes()) * 60L +
                   ((long) interval.getSeconds());
        } catch (SQLException e) {
            // This is ignored because it doesn't happen: there's no database transaction in this call.
            return 0L;
        }
    }

    public static int convertPGIntervalToIntSeconds(final String interval) {
        return (int) convertPGIntervalToSeconds(interval);
    }

    /**
     * Checks whether the indicated tables exist in the database.
     *
     * @param tables The tables for which to test.
     *
     * @return Returns true if all of the specified tables exist, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean tablesExist(final String... tables) throws SQLException {
        return tablesExistInSchema(null, tables);
    }

    /**
     * Checks whether the indicated table exists in the database.
     *
     * @param table The table for which to test.
     *
     * @return Returns true if the table exists, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean tableExists(final String table) throws SQLException {
        return tablesExistInSchema(null, table);
    }

    /**
     * Checks whether the indicated table exists in the specified schema.
     *
     * @param schema The schema to look in.
     * @param table  The table for which to test.
     *
     * @return Returns true if the table exists, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean tableExists(final String schema, final String table) throws SQLException {
        return tablesExistInSchema(schema, table);
    }

    /**
     * Checks whether the indicated table exists in the specified schema.
     *
     * @param schema The schema to look in.
     * @param tables The table for which to test.
     *
     * @return Returns true if all of the specified tables exist in the schema, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean tablesExistInSchema(final @Nullable String schema, final @Nonnull String... tables) throws SQLException {
        return locateDatabaseSchemas(schema, tables).containsAll(Arrays.asList(tables));
    }

    /**
     * Checks whether the indicated functions exist in the database.
     *
     * @param functions The functions for which to test.
     *
     * @return Returns true if all of the specified functions exist, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean functionsExist(final String... functions) throws SQLException {
        return functionsExistInSchema(null, functions);
    }

    /**
     * Checks whether the indicated function exists in the database.
     *
     * @param function The function for which to test.
     *
     * @return Returns true if the function exists, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean functionExists(final String function) throws SQLException {
        return functionsExistInSchema(null, function);
    }

    /**
     * Checks whether the indicated function exists in the specified schema.
     *
     * @param schema   The schema to look in.
     * @param function The function for which to test.
     *
     * @return Returns true if the function exists, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean functionExists(final String schema, final String function) throws SQLException {
        return functionsExistInSchema(schema, function);
    }

    /**
     * Checks whether the indicated function exists in the specified schema.
     *
     * @param schema    The schema to look in.
     * @param functions The function for which to test.
     *
     * @return Returns true if all of the specified functions exist in the schema, false otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    public boolean functionsExistInSchema(final @Nullable String schema, final @Nonnull String... functions) throws SQLException {
        return locateDatabaseSchemas(schema, functions).containsAll(Arrays.asList(functions));
    }

    @Nonnull
    public List<String> locateDatabaseSchemas(final @Nullable String schema, final @Nonnull String... names) throws SQLException {
        final List<String> located       = new ArrayList<>();
        final String       schemaPattern = StringUtils.defaultIfBlank(schema, "public");

        try (final Connection connection = _jdbcTemplate.getDataSource().getConnection()) {
            for (final String name : names) {
                final DatabaseMetaData metaData = connection.getMetaData();
                try (final ResultSet tablesAndViews = metaData.getTables("catalog", schemaPattern, name, SEARCHABLE_TABLE_TYPES)) {
                    if (tablesAndViews.next()) {
                        do {
                            located.add(tablesAndViews.getString("TABLE_NAME"));
                        } while (tablesAndViews.next());
                    } else {
                        try (final ResultSet functions = metaData.getFunctions("catalog", schemaPattern, name)) {
                            if (functions.next()) {
                                final String columnName = findColumnName(functions, "PROCEDURE_NAME", "FUNCTION_NAME");
                                do {
                                    located.add(functions.getString(columnName));
                                } while (functions.next());
                            } else {
                                log.info("Didn't find any tables, views, or functions for name {}", name);
                            }
                        }
                    }
                }
            }
        }
        return located;
    }

    /**
     * Checks whether the indicated column exist in the specified table in the database. Note that this method does NOT
     * test for the existence of the table.
     *
     * @param table  The table for which to test.
     * @param column The column for which to test.
     *
     * @return Returns the column type if the column exists in the table, null otherwise.
     *
     * @throws SQLException If an error occurs while accessing the database.
     */
    @Nullable
    public String columnExists(final String table, final String column) throws SQLException {
        try (final Connection connection = _jdbcTemplate.getDataSource().getConnection();
             final ResultSet results = connection.getMetaData().getColumns("catalog", null, table, column)) {
            // We should only find a single result. If we find that (i.e. next() returns true), then return the type.
            if (results.next()) {
                final String typeName = results.getString("TYPE_NAME");
                if (typeName.equals("varchar")) {
                    return "varchar(" + results.getString("COLUMN_SIZE") + ")";
                } else {
                    return typeName;
                }
            }
            // If we didn't find any results, return null. That's bad, m'kay?
            return null;
        }
    }

    /**
     * Alters the indicated column to set the datatype to the submitted definition. This method checks first for the
     * existence of the table and column and throws an SQLWarning if either of those is not found.
     * <p>
     * Note that this method will fail on columns that are part of a unique or composite key or have other referential
     * dependencies.
     *
     * @param table    The table in which the column can be found.
     * @param column   The column to be altered.
     * @param dataType The datatype to set for the column.
     *
     * @throws SQLWarning   When the specified table or column doesn't exist.
     * @throws SQLException When an error occurs executing the alter query.
     */
    public void setColumnDatatype(final String table, final String column, final String dataType) throws SQLException {
        if (!tableExists(table)) {
            throw new SQLWarning("The requested table " + table + " does not exist.");
        }
        final String type = columnExists(table, column);
        if (type == null) {
            throw new SQLWarning("The requested column " + column + " does not exist in the table " + table + ".");
        }
        if (!StringUtils.equals(type, dataType)) {
            executeTransaction(new SetColumnDataType(table, column, dataType), true);
        } else {
            log.info("Not updating datatype for column {} in the table {}, the datatype is already {}.", column, table, dataType);
        }
    }

    /**
     * Executes the callable's <b>call()</b> method, wrapping it within a transaction if possible. Note that if a
     * transaction manager or template wasn't available to the database helper at instantiation, this method will NOT
     * attempt to execute the transaction! If you want your method to be executed even if no transaction support is
     * available, call the {@link #executeTransaction(Callable, boolean)} method, setting the second parameter to
     * <b>true</b>.
     *
     * @param callable A callable object that implements the desired function.
     *
     * @return A message with the results of the transaction. The value for this depends on your implementation.
     */
    public String executeTransaction(final Callable<String> callable) {
        return executeTransaction(callable, false);
    }

    /**
     * Executes the callable's <b>call()</b> method, wrapping it within a transaction if possible. Note that if a
     * transaction manager or template wasn't available to the database helper at instantiation, this method will check
     * the second parameter: if <b>true</b>, the code will be executed without transaction protection or rollback
     * capabilities.
     *
     * @param callable                         A callable object that implements the desired function.
     * @param executeWithoutTransactionManager Whether the transaction should be executed if no transaction manager is available.
     *
     * @return A message with the results of the transaction. The value for this depends on your implementation.
     */
    public String executeTransaction(final Callable<String> callable, final boolean executeWithoutTransactionManager) {
        if (_transactionTemplate != null) {
            return _transactionTemplate.execute(transactionStatus -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    return logMessage("An error occurred executing the transaction from the callable class " + callable.getClass().getName(), e);
                }
            });
        } else if (executeWithoutTransactionManager) {
            log.warn("No transaction template found in the application context, so I'm performing the requested operation without transactional protection.");
            try {
                return callable.call();
            } catch (Exception e) {
                return logMessage("An error occurred executing the transaction from the callable class " + callable.getClass().getName(), e);
            }
        } else {
            return logMessage("No transaction template found in the application context, will not perform the requested operation without transactional protection.");
        }
    }

    /**
     * Executes the submitted SQL script within a transaction. The script can have multiple lines including comments. The returned string is a
     * JSON-serialized list of pairs. Each row contains a pair consisting of the number of rows affected by a statement in the script and the
     * statement itself.
     *
     * @param script The SQL script to be executed.
     *
     * @return A serialized list of all executed statements along with the number of database rows affected by the statement.
     */
    public String executeScript(final String script) {
        return executeScript(script, Collections.emptyMap());
    }

    /**
     * Executes the submitted SQL script within a transaction. The script can have multiple lines including comments. The returned string is a
     * JSON-serialized list of pairs. Each row contains a pair consisting of the number of rows affected by a statement in the script and the
     * statement itself. The name and value parameters are passed to the template for script execution.
     *
     * @param script The SQL script to be executed.
     * @param name   The name of the parameter to be passed onto the template for script execution.
     * @param value  The value of the parameter to be passed onto the template for script execution.
     *
     * @return A serialized list of all executed statements along with the number of database rows affected by the statement.
     */
    public String executeScript(final String script, final String name, final Object value) {
        return executeScript(script, ImmutableMap.of(name, value));
    }

    /**
     * Executes the submitted SQL script within a transaction. The script can have multiple lines including comments. The returned string is a
     * JSON-serialized list of pairs. Each row contains a pair consisting of the number of rows affected by a statement in the script and the
     * statement itself. The parameters map is passed to the template for script execution.
     *
     * @param script     The SQL script to be executed.
     * @param parameters The parameters to be passed onto the template for script execution.
     *
     * @return A serialized list of all executed statements along with the number of database rows affected by the statement.
     */
    public String executeScript(final String script, final Map<String, Object> parameters) {
        return executeTransaction(new CallableScript(_parameterizedTemplate, script, parameters));
    }

    public <T> T callFunction(final String function, final Class<? extends T> returnType) {
        return callFunction(function, EmptySqlParameterSource.INSTANCE, returnType);
    }

    public <T> T callFunction(final String function, final SqlParameterSource parameters, final Class<? extends T> returnType) {
        final SimpleJdbcCall      call    = new SimpleJdbcCall(getJdbcTemplate()).withFunctionName(function);
        final Map<String, Object> payload = call.execute(parameters);
        if (payload.containsKey("returnvalue")) {
            //noinspection unchecked
            return (T) payload.get("returnvalue");
        }
        return null;
    }

    public List<Map<String, Object>> callFunction(final String function, final LinkedHashMap<String, Object> arguments) {
        return _parameterizedTemplate.queryForList(generateFunctionSql(function, arguments), arguments);
    }

    public <T> List<T> callFunction(final String function, final LinkedHashMap<String, Object> arguments, final Class<T> elementType) {
        return _parameterizedTemplate.query(generateFunctionSql(function, arguments), arguments, BeanPropertyRowMapper.newInstance(elementType));
    }

    public static LinkedHashMap<String, Object> getFunctionParameterSource() {
        return EMPTY_PARAM_SOURCE;
    }

    public static LinkedHashMap<String, Object> getFunctionParameterSource(final String name, final Object value) {
        return getFunctionParameterSource(ImmutablePair.of(name, value));
    }

    public static LinkedHashMap<String, Object> getFunctionParameterSource(final String name1, final Object value1, final String name2, final Object value2) {
        return getFunctionParameterSource(ImmutablePair.of(name1, value1), ImmutablePair.of(name2, value2));
    }

    public static LinkedHashMap<String, Object> getFunctionParameterSource(final String name1, final Object value1, final String name2, final Object value2, final String name3, final Object value3) {
        return getFunctionParameterSource(ImmutablePair.of(name1, value1), ImmutablePair.of(name2, value2), ImmutablePair.of(name3, value3));
    }

    public static LinkedHashMap<String, Object> getFunctionParameterSource(final String name1, final Object value1, final String name2, final Object value2, final String name3, final Object value3, final String name4, final Object value4) {
        return getFunctionParameterSource(ImmutablePair.of(name1, value1), ImmutablePair.of(name2, value2), ImmutablePair.of(name3, value3), ImmutablePair.of(name4, value4));
    }

    @SafeVarargs
    public static LinkedHashMap<String, Object> getFunctionParameterSource(final Pair<String, Object>... pairs) {
        final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        for (final Pair<String, Object> pair : pairs) {
            parameters.put(pair.getKey(), pair.getValue());
        }
        return parameters;
    }

    /**
     * Checks whether the tables and views specified by the <b>tableSpecs</b> parameter(s) exist in the database. If one or more of the
     * specified tables and/or views does not exist, a script is loaded from the specified URL and executed.
     *
     * @param scriptUrl  The location for the script to load if any tables or views are found not to exist.
     * @param tableSpecs The tables and views to check for existence.
     *
     * @throws SQLException When an error occurs running the script.
     * @throws IOException  When an error occurs loading the script.
     */
    public void checkForTablesAndViewsInit(final String scriptUrl, final String... tableSpecs) throws SQLException, IOException {
        if (!tablesExist(tableSpecs)) {
            executeScript(BasicXnatResourceLocator.getResource(scriptUrl));
        }
    }

    public void executeScript(final Resource resource) throws IOException {
        final String script = IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
        log.debug("Executing SQL:\n{}", script);
        executeScript(script);
    }

    // TODO: Convert this to return the List<Pair<Integer,String>> rather than String. The catch is that the executeTransaction() method can be genericized without affecting the returned logMessage() values.
    private static class CallableScript implements Callable<String> {
        CallableScript(final NamedParameterJdbcTemplate template, final String script, final Map<String, Object> parameters) {
            _template = template;
            _script = script;
            _parameters = parameters != null && !parameters.isEmpty() ? new MapSqlParameterSource(parameters) : EmptySqlParameterSource.INSTANCE;
        }

        @Override
        public String call() throws Exception {
            return MAPPER.writeValueAsString(_template.execute(_script, _parameters, PreparedStatement::execute));
        }

        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final NamedParameterJdbcTemplate _template;
        private final String                     _script;
        private final SqlParameterSource         _parameters;
    }

    private class SetColumnDataType implements Callable<String> {
        public SetColumnDataType(final String table, final String column, final String dataType) {
            _table = table;
            _column = column;
            _dataType = dataType;
        }

        @Override
        public String call() {
            // Add the new column with the suffix "_new" and a timestamp.
            final String tempColumnName = _column + "_new_" + new Date().getTime();
            _jdbcTemplate.execute("ALTER TABLE " + _table + " ADD COLUMN " + tempColumnName + " " + _dataType);

            // Copy all values from the existing column into the new column
            _jdbcTemplate.execute("UPDATE " + _table + " SET " + tempColumnName + " = " + _column);

            // Drop the old column
            _jdbcTemplate.execute("ALTER TABLE " + _table + " DROP COLUMN " + _column);

            // Move the new column to the same name as the old.
            _jdbcTemplate.execute("ALTER TABLE " + _table + " RENAME " + tempColumnName + " TO " + _column);
            return null;
        }

        private final String _table;

        private final String _column;
        private final String _dataType;
    }

    @Nonnull
    private static String generateFunctionSql(final @Nonnull String function, final @Nonnull LinkedHashMap<String, Object> arguments) {
        return "SELECT * FROM " + function + " (" + arguments.keySet().stream().map(key -> StringUtils.prependIfMissing(key, ":")).collect(Collectors.joining(", ")) + ")";
    }

    @Nullable
    private static String findColumnName(final @Nonnull ResultSet results, final @Nonnull String... names) throws SQLException {
        final String[] columnNames = getColumnNames(results).toArray(new String[0]);
        for (final String name : names) {
            if (StringUtils.equalsAnyIgnoreCase(name, columnNames)) {
                return name;
            }
        }
        return null;
    }

    private static List<String> getColumnNames(final ResultSet results) throws SQLException {
        final ResultSetMetaData metadata    = results.getMetaData();
        final List<String>      columnNames = new ArrayList<>();
        for (int index = 1; index <= metadata.getColumnCount(); index++) {
            columnNames.add(metadata.getColumnName(index));
        }
        return columnNames;
    }

    @SuppressWarnings("SameParameterValue")
    private String logMessage(final String message) {
        return logMessage(message, null);
    }

    private String logMessage(final String message, final Exception e) {
        if (e != null) {
            log.error(message, e);
            return message + "\n" + "Exception type: " + e.getClass().getName() + "\n" + e.getMessage();
        } else {
            log.warn(message);
            return message;
        }
    }

    private static final String[]                      SEARCHABLE_TABLE_TYPES = {"TABLE", "VIEW"};
    private static final LinkedHashMap<String, Object> EMPTY_PARAM_SOURCE     = new LinkedHashMap<>();

    private final JdbcTemplate               _jdbcTemplate;
    private final NamedParameterJdbcTemplate _parameterizedTemplate;
    private final TransactionTemplate        _transactionTemplate;
}
