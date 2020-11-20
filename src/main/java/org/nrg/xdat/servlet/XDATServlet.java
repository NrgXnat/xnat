/*
 * core: org.nrg.xdat.servlet.XDATServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.servlet;

import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.OrderedProperties;
import org.nrg.framework.utilities.OrderedPropertiesLookup;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.DateUtils;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.core.io.Resource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
@Slf4j
public class XDATServlet extends HttpServlet {
    public static final String  INIT_SQL_PATTERN         = "classpath*:META-INF/xnat/**/init_*.sql";
    public static final Pattern ORDERED_INIT_SQL_PATTERN = Pattern.compile("^.*(\\d\\d\\d).sql$");
    public static final Pattern INIT_SCRIPT_SQL_PATTERN  = Pattern.compile("^.*/scripts/([a-z_]+).sql$");

    // TODO: Added in to support CustomClasspathResourceLoader on 1.6.5 migration, needs to be removed and refactored.
    public static String WEBAPP_ROOT;

    public static Boolean shouldUpdateViews() {
        return _shouldUpdateViews;
    }

    public static Boolean isDatabasePopulateOrUpdateCompleted() {
        return _isDatabasePopulateOrUpdateCompleted;
    }

    public void init(ServletConfig config) throws ServletException {
        replaceLogging();

        super.init(config);

        try {
            //stores some paths for convenient access later.
            XDATServlet.WEBAPP_ROOT = getWebAppPath();

            // Migration: Fix this to use NIO Paths.
            XDAT.init(getWebAppPath("WEB-INF", "conf"), true);

            //store some  more convenience paths
            XDAT.setScreenTemplatesFolder(getWebAppPath("templates", "screens"));
            for (final String path : CustomClasspathResourceLoader.TEMPLATE_PATHS) {
                XDAT.addScreenTemplatesFolder(path, new File(getWebAppPath(path, "screens")));
            }

            //call the logic to check if the database is up to date.
            if (updateDatabase(getWebAppPath("resources", "default-sql"))) {
                //reset loaded stuff, because database changed
                ElementSecurity.refresh();
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private String getWebAppPath(final String... paths) {
        return Paths.get(getServletContext().getRealPath(""), paths).toString();
    }

    /**
     * Method used to manage auto-updates to the database.  It will only auto update the database, if the auto-update
     * parameter is null or true. It uses a query on the user table to identify if the user table to see if users have been populated.
     *
     * @param conf The configuration folder from which to load the SQL scripts.
     *
     * @return Returns true if the database was created or updated, false otherwise.
     *
     * @throws Exception When an error occurs.
     */
    private boolean updateDatabase(String conf) throws Exception {
        final DatabaseHelper db        = new DatabaseHelper(XDAT.getDataSource());
        final Integer        userCount = !db.tablesExist("xdat_user") ? null : db.getJdbcTemplate().queryForObject("SELECT COUNT(*) FROM xdat_user", Integer.class);

        //this should use the config service.. but I couldn't get it to work because of servlet init issues.
        final Properties prop = new Properties();
        final File       f    = Paths.get(conf, "properties", "database.properties").toFile();
        if (f.exists()) {
            prop.load(new FileInputStream(f));
        }

        //currently defaults to true, if the file isn't there.
        if (!prop.containsKey("auto-update")) {
            prop.setProperty("auto-update", "true");
        }

        if (prop.containsKey("auto-update") && BooleanUtils.toBoolean(prop.getProperty("auto-update"))) {
            final Path generatedSqlLogPath = getGeneratedSqlLogPath();
            if (userCount != null) {
                final List<String> statements = getInitScripts(INIT_SCRIPT_SQL_PATTERN, null);
                final int          oidCount   = db.getJdbcTemplate().queryForObject(QUERY_FIND_OID_FUNCTIONS, Integer.class);
                if (oidCount > 0 || !statements.isEmpty()) {
                    log.debug("Found {} functions that contained OID references or {} lines in SQL init scripts, preparing to execute", oidCount, statements.size());
                    try (final PoolDBUtils.Transaction transaction = PoolDBUtils.open()) {
                        if (oidCount > 0) {
                            final String dropOidsFunction;
                            try (final Stream<String> stream = db.getJdbcTemplate().queryForList(QUERY_DROP_OID_FUNCTIONS, String.class).stream();
                                 final StringWriter stringWriter = new StringWriter();
                                 final PrintWriter writer = new PrintWriter(stringWriter)) {
                                stream.forEach(writer::println);
                                dropOidsFunction = stringWriter.toString();
                            }
                            transaction.execute(dropOidsFunction);
                            final List<String> restoreOidsFunctions = new ArrayList<>();
                            GenericUtils.convertToTypedList(XFTManager.GetInstance().getOrderedElements(), GenericWrapperElement.class).stream().filter(element -> !StringUtils.endsWithAny(element.getSQLName(), "_history", "_meta_data")).forEach(element -> {
                                try {
                                    final List<String>[] functions = GenericWrapperUtils.GetUpdateFunctions(element);
                                    try (final Stream<String> stream = Stream.concat(functions[0].stream(), functions[1].stream());
                                         final StringWriter stringWriter = new StringWriter();
                                         final PrintWriter writer = new PrintWriter(stringWriter)) {
                                        stream.forEach(writer::println);
                                        restoreOidsFunctions.add(stringWriter.toString());
                                    } catch (IOException e) {
                                        log.error("An error occurred trying to create a string writer", e);
                                    }
                                } catch (XFTInitException e) {
                                    log.error("Error initializing XFT", e);
                                } catch (ElementNotFoundException e) {
                                    log.error("Element not found: {}", e.ELEMENT, e);
                                }
                            });
                            transaction.execute(restoreOidsFunctions);
                        }
                        if (db.tableExists("xs_item_cache") && StringUtils.isBlank(db.columnExists("xs_item_cache", "id"))) {
                            transaction.execute(PoolDBUtils.QUERY_ITEM_CACHE_ADD_ID);
                        }
                        if (!statements.isEmpty()) {
                            transaction.execute(statements);
                        }
                        transaction.commit();
                    } catch (SQLException | DBPoolException e) {
                        log.error("An error occurred trying to run the SQL init scripts", e);
                    }
                }

                //noinspection rawtypes
                final List[]       createSql = SQLUpdateGenerator.GetSQLCreate();
                final List<String> sql       = GenericUtils.convertToTypedList(createSql[0], String.class);
                final List<String> optional  = GenericUtils.convertToTypedList(createSql[1], String.class);
                if (!optional.isEmpty()) {
                    log.error("Preparing create SQL and got {} optional SQL statements. Please review this optional SQL and consider executing it if required:\n\nOptional SQL:\n\n{}", optional.size(), String.join("\n", optional));
                }
                if (!sql.isEmpty()) {
                    final DatabaseUpdater databaseUpdater = new DatabaseUpdater(userCount == 0 ? conf : null, generatedSqlLogPath, "-- Generated SQL for updating XNAT database schema");//user_count==0 means users need to be created.
                    System.out.println("===========================");
                    System.out.println("Database out of date... updating");
                    for (String s : sql) {
                        System.out.println(s);
                    }
                    System.out.println("===========================");
                    // changes have been made to the actual db schema, they
                    // should be done before we continue
                    // the user can't use the site until these are
                    // completed.
                    databaseUpdater.addStatements(sql);
                    //noinspection CallToThreadRun
                    databaseUpdater.run();// use run to prevent a second thread.
                    _shouldUpdateViews = true;
                    return true;
                } else {
                    System.out.println("Database up to date.");
                    // the database tables are up to date. But, we should
                    // still make sure the functions and views are up to
                    // date.
                    // However, this can be done in a separate thread so
                    // that the user can start using the site.

                    //commenting this out because it is very slow.... they certainly don't need to be run every startup.  But, we need some way of getting them updated when it is needed.
                    //du.addStatements(sql);
                    //du.start();// start in a separate thread
                    _shouldUpdateViews = true;
                    _isDatabasePopulateOrUpdateCompleted = true;
                    (new DelayedSequenceChecker()).start();//this isn't necessary if we did the du.start();
                    return false;
                }
            } else {
                _shouldUpdateViews = false;
                System.out.println("===========================");
                System.out.println("New Database -- BEGINNING Initialization");
                System.out.println("===========================");
                // xdat-user table doesn't exist, assume this is an empty
                // database
                final DatabaseUpdater databaseUpdater = new DatabaseUpdater(conf, generatedSqlLogPath, "-- Generated SQL for initializing new XNAT database schema");
                final List<String>    sql             = SQLCreateGenerator.GetSQLCreate(false);
                databaseUpdater.addStatements(sql);
                //noinspection CallToThreadRun
                databaseUpdater.run();// start and wait for it

                System.out.println("===========================");
                System.out.println("Database initialization complete.");
                System.out.println("===========================");
                return true;
            }
        } else {
            _shouldUpdateViews = true;
            _isDatabasePopulateOrUpdateCompleted = true;
            (new DelayedSequenceChecker()).start();
            return false;
        }
    }

    public static class DelayedSequenceChecker extends Thread {
        public void run() {
            DBAction.AdjustSequences();
            Reflection.injectDynamicImplementations("org.nrg.xnat.extensions.server.startup.sync", null);
        }
    }

    /**
     * When updating the database, the function and views are always recreated.  The tables are only created if
     * necessary. The statements to create the tables are passed in via the addStatements method.
     */
    @SuppressWarnings("UnstableApiUsage")
    public class DatabaseUpdater extends Thread {
        /**
         * Creates a new instance of the updater class.
         *
         * @param conf                Location of the WEB-INF/conf.  Set to NULL to skip init scripts.
         * @param generatedSqlLogPath The path to a file where SQL statements are logged. If null, no logging occurs.
         */
        public DatabaseUpdater(final String conf, final Path generatedSqlLogPath, final String... generatedSqlLogHeaders) {
            _conf = conf;
            _generatedSqlLogPath = validateGeneratedSqlLogPath(generatedSqlLogPath);
            _generatedSqlLogHeaders = generatedSqlLogHeaders;
        }

        private Path validateGeneratedSqlLogPath(final Path generatedSqlLogPath) {
            if (generatedSqlLogPath == null) {
                return null;
            }
            final File file = generatedSqlLogPath.toFile();
            if (file.exists()) {
                if (file.isDirectory()) {
                    return generatedSqlLogPath.resolve(getGeneratedSqlLogFilename());
                }
                if (file.canWrite()) {
                    return generatedSqlLogPath;
                }
                log.warn("I was asked to log generated SQL queries to the file {}, but I can't write to that.", generatedSqlLogPath);
                return null;
            }
            final File parent = file.getParentFile();
            if (parent.exists()) {
                if (parent.isFile()) {
                    log.warn("I was asked to log generated SQL queries to the file {}, but the parent is a file (must be a directory).", generatedSqlLogPath);
                    return null;
                }
                return generatedSqlLogPath;
            }
            final boolean success = parent.mkdirs();
            if (!success) {
                log.warn("I was asked to log generated SQL queries to the file {}, but the parent directory doesn't exist and I can't seem to create it.", generatedSqlLogPath);
                return null;
            }
            return generatedSqlLogPath;
        }

        public void addStatements(List<String> more) {
            _sql.addAll(more);
        }

        public void run() {
            final PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction();
            try (final PrintWriter writer = new PrintWriter(_generatedSqlLogPath != null ? new BufferedWriter(new FileWriter(_generatedSqlLogPath.toFile())) : CharStreams.nullWriter())) {
                transaction.start();
                //prep accessory tables, may involve procedure calls
                log.info("Initializing administrative functions...");
                if (_generatedSqlLogPath != null && _generatedSqlLogHeaders.length > 0) {
                    for (final String header : _generatedSqlLogHeaders) {
                        writer.println(StringUtils.prependIfMissing("-- ", header));
                    }
                }
                execute(transaction, writer, GenericWrapperUtils.GetExtensionTables());

                //manually execute create statements
                if (!_sql.isEmpty()) {
                    log.info("Initializing database schema...");
                    try {
                        execute(transaction, writer, _sql);
                    } catch (Throwable t) {
                        String errMessage = t.getMessage();
                        if (CANNOT_DROP_MESSAGE.matcher(errMessage).find()) {
                            //In cases where it is failing to modify a table because one or more views depend on it, simply drop all XNAT views and try again.
                            transaction.rollback();
                            transaction.execute(getViewDropSql(StringUtils.defaultIfBlank(XDAT.getContextService().getBean("dbUsername", String.class), "xnat")));
                            execute(transaction, writer, GenericWrapperUtils.GetExtensionTables());
                            execute(transaction, writer, _sql);
                        } else {
                            throw t;
                        }
                    }
                }

                //update the stored functions used for retrieving XFTItems and manipulating them
                //this used to build one big file of sql... but it was way to big.  So now it will do one element at at time.
                final List<String> runAfter = new ArrayList<>();
                log.info("Initializing database functions...");
                for (final GenericWrapperElement element : GenericUtils.convertToTypedList(XFTManager.GetInstance().getOrderedElements(), GenericWrapperElement.class)) {
                    final List<String>[] func = GenericWrapperUtils.GetFunctionStatements(element);
                    execute(transaction, writer, func[0]);
                    runAfter.addAll(func[1]);
                }

                if (_conf != null) {
                    //create the views defined in the display documents
                    log.info("Initializing database views...");
                    execute(transaction, writer, DisplayManager.GetCreateViewsSQL());
                }

                execute(transaction, writer, runAfter);

                if (_conf != null) {
                    final List<String> initScripts = getInitScripts(ORDERED_INIT_SQL_PATTERN, Comparator.comparing(resource -> ORDERED_INIT_SQL_PATTERN.matcher(resource.getFilename()).group(1)));
                    if (initScripts.size() > 0) {
                        execute(transaction, writer, initScripts);
                    }
                }

                transaction.commit();
            } catch (XFTInitException e) {
                log.error("An error occurred initializing XFT, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (SQLException e) {
                log.error("An error occurred creating or updating the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (DBPoolException e) {
                log.error("An error occurred when trying to start the transaction for creating or updating the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (Exception e) {
                log.error("An unknown error occurred when trying to create or update the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } finally {
                transaction.close();
            }

            try {
                //when the database is updated directly (rather then through XFT), the sequences can get out of sync.  This would cause issues down the road.
                //so we fix them
                DBAction.AdjustSequences();
            } catch (Exception e) {
                log.error("", e);
            }

            // code will dynamically register data types
            try {
                //this is to trigger a review of the Data Types that have been defined in jar configurations.
                //it needs to run after all the db initialization is done.

                final List<String> newTypes = ElementSecurity.registerNewTypes();
                if (!newTypes.isEmpty()) {
                    // When the database is updated directly (rather then through XFT), the sequences can get out of
                    // sync.  This would cause issues down the road so we fix them here.
                    log.info("Created {} new data types in the system: \"{}\"", newTypes.size(), StringUtils.join(newTypes, "\", \""));
                    DBAction.AdjustSequences();
                }
            } catch (Throwable e) {
                log.error("", e);
            }
            _isDatabasePopulateOrUpdateCompleted = true;
        }

        private void rollback(final PoolDBUtils.Transaction transaction) {
            try {
                transaction.rollback();
            } catch (SQLException e1) {
                log.error("", e1);
            }
        }

        private void execute(final PoolDBUtils.Transaction transaction, final PrintWriter writer, final Collection<String> statements) throws SQLException {
            if (_generatedSqlLogPath != null) {
                for (final String statement : statements) {
                    writer.println(statement);
                }
            }
            transaction.execute(statements);
        }

        final private List<String> _sql = new ArrayList<>();
        final private String       _conf;
        final private Path         _generatedSqlLogPath;
        private final String[]     _generatedSqlLogHeaders;
    }

    private List<String> getInitScripts(final Pattern filter, final Comparator<Resource> comparator) {
        // Get the init prefs ordered properties from context and create a substitutor.
        final OrderedProperties properties  = XDAT.getContextService().getBeanSafely("initPrefs", OrderedProperties.class);
        final StringSubstitutor substitutor = new StringSubstitutor(new OrderedPropertiesLookup(properties), "${", "}", '\\');

        final List<String> statements = new ArrayList<>();
        try {
            final List<Resource> resources = filterAndSortInitSqlResources(BasicXnatResourceLocator.getResources(INIT_SQL_PATTERN), filter, comparator);
            log.debug("Found {} resources that match the pattern {}", resources.size(), INIT_SQL_PATTERN);
            for (final Resource resource : resources) {
                log.debug("Now processing the resource {}", resource.getFilename());
                try (final Stream<String> stream = Files.lines(resource.getFile().toPath());
                     final StringWriter stringWriter = new StringWriter();
                     final PrintWriter writer = new PrintWriter(stringWriter)) {
                    stream.map(substitutor::replace).forEach(writer::println);
                    statements.add(stringWriter.toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT module definitions.");
        }
        return statements;
    }

    /**
     * Tests each resource to see if the name matches the pattern for SQL initialization resources, init_XXX_NNN.sql,
     * where XXX is some string to indicate the purpose of the initialization and NNN is a three-digit ordinal value
     * indicating the script's place in the initialization order.
     * <p>
     * Once the resources have been filtered and any non-compliant resources have been removed and logged, the list is
     * sorted based on the resources' ordinal values.
     *
     * @param resources  The array of resources to be sorted and filtered.
     * @param filter     A regex pattern to further refine the resources.
     * @param comparator The comparator to use when sorting the resources (or <b>null</b> if sorting isn't required)
     *
     * @return The list of resources after sorting and filtering.
     */
    private List<Resource> filterAndSortInitSqlResources(final List<Resource> resources, final Pattern filter, final Comparator<Resource> comparator) {
        final List<Resource> filtered = new ArrayList<>();
        for (final Resource resource : resources) {
            try {
                final Matcher matcher = filter.matcher(resource.getURI().toString());
                if (matcher.find()) {
                    filtered.add(resource);
                }
            } catch (IOException e) {
                log.error("An error occurred trying to get the URI for the resource {}", resource, e);
            }
        }
        if (comparator != null) {
            filtered.sort(comparator);
        }
        return filtered;
    }

    /**
     * Checks whether the XNAT configuration property <b>xnat.database.sql.log</b> is set to <b>true</b>. If not, this
     * method returns null and generated SQL shouldn't be logged. The folder for this output can be specified with the
     * <b>xnat.database.sql.log.folder</b> property, but defaults to the <b>xnat.home</b> folder. In either case, the
     * filename is <b>xnat-<i>timestamp</i>.sql</b>. You can specify the full path, including the file name, with the
     * <b>xnat.database.sql.log.file</b> property.
     *
     * @return The path to the log file for generated SQL if specified, null otherwise.
     */
    private static Path getGeneratedSqlLogPath() {
        //noinspection unchecked
        final List<Path> configFiles = (List<Path>) XDAT.getContextService().getBean("configFiles");
        final Properties properties  = new Properties();
        for (final Path path : configFiles) {
            final File file = path.toFile();
            if (file.exists()) {
                try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    properties.load(reader);
                } catch (IOException e) {
                    log.error("An error occurred trying to read the properties file {}", file.toURI(), e);
                }
            }
        }

        final Boolean shouldLogSql = BooleanUtils.toBooleanObject(properties.getProperty("xnat.database.sql.log"));
        final String  sqlLogFile   = properties.getProperty("xnat.database.sql.log.file");
        final String  sqlLogFolder = properties.getProperty("xnat.database.sql.log.folder");

        if (BooleanUtils.isFalse(shouldLogSql) || shouldLogSql == null && StringUtils.isAllBlank(sqlLogFile, sqlLogFolder)) {
            log.debug("Either xnat.database.sql.log is set to false or xnat.database.sql.log isn't set at all and no value is set for xnat.database.sql.log.file or xnat.database.sql.log.folder, so no generated SQL log path considered.");
            return null;
        }
        if (StringUtils.isNoneBlank(sqlLogFile, sqlLogFolder)) {
            log.warn("Found values for both \"xnat.database.sql.log.file\" and \"xnat.database.sql.log.folder\". You should only specify one of these properties. Using the value for \"xnat.database.sql.log.file\": {}", sqlLogFile);
        }

        final Path sqlLogFilePath;
        if (StringUtils.isNotBlank(sqlLogFile)) {
            final Path path = Paths.get(sqlLogFile);
            if (path.isAbsolute()) {
                sqlLogFilePath = path;
            } else {
                sqlLogFilePath = getSqlLogFolder(sqlLogFolder).resolve(sqlLogFile);
            }
        } else {
            sqlLogFilePath = getSqlLogFolder(sqlLogFolder).resolve(getGeneratedSqlLogFilename());
        }

        log.info("Found path specified for generated SQL log path: {}", sqlLogFilePath);
        return sqlLogFilePath;
    }

    private static Path getSqlLogFolder(final String configuredSqlLogFolder) {
        if (StringUtils.isNotBlank(configuredSqlLogFolder)) {
            return Paths.get(configuredSqlLogFolder);
        }
        return ((Path) XDAT.getContextService().getBean("xnatHome")).resolve("sql");
    }

    private static String getGeneratedSqlLogFilename() {
        return "xnat-" + DateUtils.getMsTimestamp() + ".sql";
    }

    private void replaceLogging() {
        // remove the java.util.logging handlers so that nothing is logged to stdout/stderr
        java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
        for (Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }

        try {
            // use the sl4j bridge to redirect restlet logging to log4j
            SLF4JBridgeHandler.install();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private List<String> getViewDropSql(final String user) {
        return Arrays.asList(QUERY_FIND_USER_VIEWS, QUERY_DROP_USER_VIEWS, String.format(QUERY_EXEC_DROP_USER_VIEWS, user));
    }

    private static final String  QUERY_FIND_OID_FUNCTIONS   = "SELECT count(*) FROM information_schema.routines WHERE routine_name LIKE 'update_ls_%' AND routine_definition LIKE '%SELECT oid FROM xs_item_cache%'";
    private static final String  QUERY_DROP_OID_FUNCTIONS   = "WITH " +
                                                              "    fns_and_params AS ( " +
                                                              "        SELECT " +
                                                              "            r.routine_name, " +
                                                              "            p.ordinal_position, " +
                                                              "            p.parameter_name, " +
                                                              "            p.data_type " +
                                                              "        FROM " +
                                                              "            information_schema.parameters p " +
                                                              "                LEFT JOIN information_schema.routines r ON p.specific_name = r.specific_name " +
                                                              "        WHERE " +
                                                              "            p.parameter_mode = 'IN' AND " +
                                                              "            r.routine_catalog = 'xnat' AND " +
                                                              "            r.routine_schema = 'public' AND " +
                                                              "            r.routine_name LIKE 'update_ls_%' " +
                                                              "        ORDER BY " +
                                                              "            r.routine_name, " +
                                                              "            p.ordinal_position) " +
                                                              "SELECT " +
                                                              "    'DROP FUNCTION IF EXISTS ' || routine_name || '(' || array_to_string(array_agg(data_type), ', ') || ');' " +
                                                              "FROM " +
                                                              "    fns_and_params " +
                                                              "GROUP BY " +
                                                              "    routine_name";
    private static final String  QUERY_FIND_USER_VIEWS      = "CREATE OR REPLACE FUNCTION find_user_views(username TEXT) " +
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
                                                              "END$$ LANGUAGE plpgsql;";
    private static final String  QUERY_DROP_USER_VIEWS      = "CREATE OR REPLACE FUNCTION drop_user_views(username TEXT) " +
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
                                                              "END$$ LANGUAGE plpgsql;";
    private static final String  QUERY_EXEC_DROP_USER_VIEWS = "SELECT drop_user_views('%s');";
    private static final Pattern CANNOT_DROP_MESSAGE        = Pattern.compile("^.*cannot\\s+drop\\s+(table|column)(?s).*because other objects depend on it.*$");

    private static Boolean _shouldUpdateViews;
    private static Boolean _isDatabasePopulateOrUpdateCompleted = false;
}
