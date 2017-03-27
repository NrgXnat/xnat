/*
 * core: org.nrg.xdat.servlet.XDATServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.servlet;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.nrg.framework.utilities.*;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.core.io.Resource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
public class XDATServlet extends HttpServlet {
    private static final Logger  logger      = LoggerFactory.getLogger(XDATServlet.class);
    private static final Pattern SQL_PATTERN = Pattern.compile("^.*(\\d\\d\\d).sql$");

    private static Boolean _shouldUpdateViews;

    // TODO: Added in to support CustomClasspathResourceLoader on 1.6.5 migration, needs to be removed and refactored.
    public static String WEBAPP_ROOT;

    public static Boolean shouldUpdateViews() {
        return _shouldUpdateViews;
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
            logger.error("", e);
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
        Long user_count;
        try {
            user_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xdat_user", "count", null, null);
        } catch (SQLException e) {
            // xdat_user table doesn't exist
            user_count = null;
        }

        //this should use the config service.. but I couldn't get it to work because of servlet init issues.
        final Properties prop = new Properties();
        final File f = Paths.get(conf, "properties", "database.properties").toFile();
        if (f.exists()) {
            prop.load(new FileInputStream(f));
        }

        //currently defaults to true, if the file isn't there.
        if (!prop.containsKey("auto-update")) {
            prop.setProperty("auto-update", "true");
        }

        if ((prop.containsKey("auto-update")) && (BooleanUtils.toBoolean(prop.getProperty("auto-update")))) {
            if (user_count != null) {
                final DatabaseUpdater du = new DatabaseUpdater((user_count == 0) ? conf : null);//user_count==0 means users need to be created.

                //only interested in the required ones here.
                @SuppressWarnings("unchecked")
                final List<String> sql = SQLUpdateGenerator.GetSQLCreate()[0];
                if (sql.size() > 0) {
                    _shouldUpdateViews = false;
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
                    du.addStatements(sql);
                    du.run();// use run to prevent a second thread.
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
                final DatabaseUpdater du = new DatabaseUpdater(conf);
                du.addStatements(SQLCreateGenerator.GetSQLCreate(false));
                du.run();// start and wait for it

                System.out.println("===========================");
                System.out.println("Database initialization complete.");
                System.out.println("===========================");
                return true;
            }
        } else {
            _shouldUpdateViews = true;
            (new DelayedSequenceChecker()).start();
            return false;
        }
    }

    public class DelayedSequenceChecker extends Thread {
        public void run() {
            DBAction.AdjustSequences();
            Reflection.injectDynamicImplementations("org.nrg.xnat.extensions.server.startup.sync", null);
        }
    }

    /**
     * When updating the database, the function and views are always recreated.  The tables are only created if
     * necessary. The statements to create the tables are passed in via the addStatements method.
     */
    public class DatabaseUpdater extends Thread {
        public static final String INIT_SQL_PATTERN = "classpath*:META-INF/xnat/**/init_*.sql";
        final String conf;
        final List<String> sql = Lists.newArrayList();

        /**
         * @param conf Location of the WEB-INF/conf.  Should be NULL if you don't want to look for init scripts and run them.
         */
        public DatabaseUpdater(String conf) {
            this.conf = conf;
        }

        public void addStatements(List<String> more) {
            sql.addAll(more);
        }

        public void run() {
            PoolDBUtils.Transaction transaction = PoolDBUtils.getTransaction();
            try {
                transaction.start();
                //prep accessory tables, may involve procedure calls
                logger.info("Initializing administrative functions...");
                transaction.execute(GenericWrapperUtils.GetExtensionTables());

                //manually execute create statements
                if (sql.size() > 0) {
                    logger.info("Initializing database schema...");
                    transaction.execute(sql);
                }

                //update the stored functions used for retrieving XFTItems and manipulating them
                //this used to build one big file of sql... but it was way to big.  So now it will do one element at at time.
                List<String> runAfter = Lists.newArrayList();
                logger.info("Initializing database functions...");
                for (Object o : XFTManager.GetInstance().getOrderedElements()) {
                    GenericWrapperElement element = (GenericWrapperElement) o;
                    List<String>[] func = GenericWrapperUtils.GetFunctionStatements(element);
                    transaction.execute(func[0]);
                    runAfter.addAll(func[1]);
                }

                if (conf != null) {
                    //create the views defined in the display documents
                    logger.info("Initializing database views...");
                    transaction.execute(DisplayManager.GetCreateViewsSQL());
                }

                transaction.execute(runAfter);

                if (conf != null) {
                    final List<String> initScripts = getInitScripts();
                    if (initScripts.size() > 0) {
                        transaction.execute(initScripts);
                    }
                }

                transaction.commit();
            } catch (XFTInitException e) {
                logger.error("An error occurred initializing XFT, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (SQLException e) {
                logger.error("An error occurred creating or updating the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (DBPoolException e) {
                logger.error("An error occurred when trying to start the transaction for creating or updating the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } catch (Exception e) {
                logger.error("An unknown error occurred when trying to create or update the database elements schemas and views, now attempting rolling back the current transactions", e);
                rollback(transaction);
            } finally {
                transaction.close();
            }

            try {
                //when the database is updated directly (rather then through XFT), the sequences can get out of sync.  This would cause issues down the road.
                //so we fix them
                DBAction.AdjustSequences();
            } catch (Exception e) {
                logger.error("", e);
            }

            // code will dynamically register data types
            try {
                //this is to trigger a review of the Data Types that have been defined in jar configurations.
                //it needs to run after all the db initialization is done.

                if (ElementSecurity.registerNewTypes()) {
                    //when the database is updated directly (rather then through XFT), the sequences can get out of sync.  This would cause issues down the road.
                    //so we fix them
                    DBAction.AdjustSequences();
                }
            } catch (Throwable e) {
                logger.error("", e);
            }
        }

        private void rollback(final PoolDBUtils.Transaction transaction) {
            try {
                transaction.rollback();
            } catch (SQLException e1) {
                logger.error("", e1);
            }
        }

        /**
         * Returns a list of sql statements pulled from SQL files stored under META-INF/xnat. The files are filtered and
         * sorted by the {@link #filterAndSortInitSqlResources(List)} method. These files should initialize the
         * server, its settings and the users.
         *
         * @return A list of SQL statements initialized from the SQL scripts.
         */
        private List<String> getInitScripts() {
            // Get the init prefs ordered properties from context and create a substitutor.
            final OrderedProperties properties = XDAT.getContextService().getBeanSafely("initPrefs", OrderedProperties.class);
            final StrSubstitutor substitutor = new StrSubstitutor(new PropertiesLookup(properties), "${", "}", '\\');

            final List<String> statements = new ArrayList<>();
            try {
                final List<Resource> resources = filterAndSortInitSqlResources(BasicXnatResourceLocator.getResources(INIT_SQL_PATTERN));
                logger.debug("Found {} resources that match the pattern {}", resources.size(), INIT_SQL_PATTERN);
                for (final Resource resource : resources) {
                    logger.debug("Now processing the resource {}", resource.getFilename());
                    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()), 1024)) {
                        String statement;
                        while ((statement = reader.readLine()) != null) {
                            statements.add(substitutor.replace(statement + (statement.endsWith(";") ? '\n' : ";\n")));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("An error occurred trying to locate XNAT module definitions.");
            }
            return statements;
        }
    }

    /**
     * Tests each resource to see if the name matches the pattern for SQL initialization resources, init_XXX_NNN.sql,
     * where XXX is some string to indicate the purpose of the initialization and NNN is a three-digit ordinal value
     * indicating the script's place in the initialization order.
     *
     * Once the resources have been filtered and any non-compliant resources have been removed and logged, the list is
     * sorted based on the resources' ordinal values.
     *
     * @param resources The array of resources to be sorted and filtered.
     *
     * @return The list of resources after sorting and filtering.
     */
    private List<Resource> filterAndSortInitSqlResources(final List<Resource> resources) {
        final List<Resource> filtered = new ArrayList<>();
        for (final Resource resource : resources) {
            final Matcher matcher = SQL_PATTERN.matcher(resource.getFilename());
            if (matcher.find()) {
                filtered.add(resource);
            }
        }
        Collections.sort(filtered, new Comparator<Resource>() {
            @Override
            public int compare(final Resource resource1, final Resource resource2) {
                final String ordered1 = SQL_PATTERN.matcher(resource1.getFilename()).group(1);
                final String ordered2 = SQL_PATTERN.matcher(resource2.getFilename()).group(1);
                return ordered1.compareTo(ordered2);
            }
        });
        return filtered;
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
}

