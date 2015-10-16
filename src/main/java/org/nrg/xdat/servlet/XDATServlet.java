/*
 * org.nrg.xdat.servlet.XDATServlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.servlet;

import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
public class XDATServlet extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(XDATServlet.class);
    // TODO: Added in to support CustomClasspathResourceLoader on 1.6.5 migration, needs to be removed and refactored.
    public static String WEBAPP_ROOT;

    public void init(ServletConfig config) throws ServletException {
        replaceLogging();

        super.init(config);

        try {
            //stores some paths for convenient access later.
            XDATServlet.WEBAPP_ROOT = getWebAppPath();

            // Migration: Fix this to use NIO Paths.
            XDAT.init(getWebAppPath("WEB-INF", "conf"), true, false);

            //store some  more convenience paths
            XDAT.setScreenTemplatesFolder(getWebAppPath("templates", "screens"));
            XDAT.addScreenTemplatesFolder(getWebAppPath("templates", "screens"));
            XDAT.addScreenTemplatesFolder(getWebAppPath("xnat-templates", "screens"));
            XDAT.addScreenTemplatesFolder(getWebAppPath("xdat-templates", "screens"));

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
     * @param conf    The configuration folder from which to load the SQL scripts.
     * @return Returns true if the database was created or updated, false otherwise.
     * @throws Exception
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
                final List<String> sql = SQLUpdateGenerator.GetSQLCreate()[0];
                if (sql.size() > 0) {
                    System.out.println("===========================");
                    System.out.println("Database out of date... updating");
                    for (String s : sql) System.out.println(s);
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
                    (new DelayedSequenceChecker()).start();//this isn't necessary if we did the du.start();
                    return false;
                }
            } else {
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
            (new DelayedSequenceChecker()).start();
            return false;
        }
    }

    public void destroy() {
        try {
            XFT.closeConnections();
        } catch (SQLException e) {
            logger.warn("An SQL exception occurred trying to close the XFT database connections.", e);
        }
        super.destroy();
    }

    public class DelayedSequenceChecker extends Thread {
        public void run() {
            DBAction.AdjustSequences();
            Reflection.injectDynamicImplementations("org.nrg.xnat.extensions.server.startup.sync", null);
        }
    }

    /**
     * When updating the database, the function and views are always recreated.  The tables are only created if necessary.
     * <p/>
     * The statements to create the tables are passed in via the addStatements method.
     */
    public class DatabaseUpdater extends Thread {
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

                //create the views defined in the display documents
                logger.info("Initializing database views...");
                transaction.execute(DisplayManager.GetCreateViewsSQL());

                transaction.execute(runAfter);

                if (conf != null) {
                    final List<String> initScripts = getInitScripts(conf);
                    if (initScripts.size() > 0) {
                        transaction.execute(initScripts);
                    }
                }

                transaction.commit();
            } catch (Exception e) {
                try {
                    transaction.rollback();
                } catch (SQLException e1) {
                    logger.error("", e1);
                }
                logger.error("", e);
                return;
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

        /**
         * returns a list of sql statements pulled from .sql files in WEB-INF/conf/default-sql/*.sql.
         * <p/>
         * These files should initialize the server, its settings and the users.
         *
         * @param conf The folder containing the SQL scripts.
         * @return A list of SQL statements initialized from the SQL scripts.
         */
        public List<String> getInitScripts(String conf) {
            File sql_conf = new File(conf);
            final List<String> statements = Lists.newArrayList();

            //look for files in the WEB-INF/conf/default-sql/*.sql
            if (sql_conf.exists()) {
                System.out.println("===========================");
                System.out.println("Populating default user list");
                System.out.println("===========================");
                File[] custom_sql = sql_conf.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File arg0, String arg1) {
                        return (arg1.endsWith(".sql"));
                    }
                });

                //files can be sorted by adding _ (integer) to the end of the file name. file_1.sql is run before file_2.sql
                Arrays.sort(custom_sql, new Comparator<File>() {
                    @Override
                    public int compare(File arg0, File arg1) {
                        return findIndex(arg0) - findIndex(arg1);
                    }

                    private int findIndex(File f) {
                        if (f.getName().contains("_")) {
                            final String index = f.getName().substring(f.getName().lastIndexOf("_") + 1, f.getName().indexOf(".sql"));
                            return NumberUtils.toInt(index, 0);
                        } else {
                            return 0;
                        }
                    }
                });

                if (custom_sql.length > 0) {
                    for (File sql : custom_sql) {
                        try {
                            for (final String stmt : FileUtils.FileLinesToArrayList(sql)) {
                                if (!StringUtils.isWhitespace(stmt)) {
                                    statements.add((stmt.contains(";")) ? stmt : stmt + ";");
                                }
                            }
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }
            }
            return statements;
        }
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

