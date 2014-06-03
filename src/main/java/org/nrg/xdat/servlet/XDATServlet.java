//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.servlet;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.PoolDBUtils.Transaction;
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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author Tim
 *
 * This is the servlet that initializes XDAT, XFT, and all the other underlying goodness that makes XDAT & XFT work.
 * 
 * MODIFIED (11/20/2013) to auto-update the database when it is out of sync.
 */
@SuppressWarnings("serial")
public class XDATServlet extends HttpServlet{
    private static final Logger logger = LoggerFactory.getLogger(XDATServlet.class);
    private static ServletContext _context;

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		//this was added to XNAT a really really long time ago.  I believe it was because config files on the classpath 
		// were overriding the log4j.properties on the file system.  But, later it was modified to bridge the SLF4J divide.
		replaceLogging();

		super.init(config);

        _context = getServletContext();

		try {
			XDAT.init(true, false); //initializes the XDAT engine (and XFT implicitly)
			
			//store some  more convenience paths
			XDAT.setScreenTemplatesFolder("templates/screens");
            XDAT.addScreenTemplatesFolder("xnat-templates/screens");
            XDAT.addScreenTemplatesFolder("xdat-templates/screens");
			
            //call the logic to check if the database is up to date.
            if(updateDatabase("resources/default-sql")) {
            	//reset loaded stuff, because database changed
            	ElementSecurity.refresh();
            }
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public void destroy()
	{
	    try {
            XFT.closeConnections();
        } catch (SQLException ignored) {
        }
        super.destroy();
	}

    /**
     * Returns the URI of the specified relative path within the web application. If no resource is found at the
     * indicated path, this method returns <b>null</b>.
     * @param relativePaths    The path or path components.
     * @return A URI for the indicated resource or <b>null</b> if not found.
     */
    public static URI getAppRelativeLocation(final String... relativePaths) {
        try {
            return _context.getResource(joinPaths(relativePaths)).toURI();
        } catch (URISyntaxException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Locates the resource at the specified relative path within the web application. If no resource is found at the
     * indicated path, this method returns <b>null</b>.
     * @param relativePaths    The path or path components.
     * @return An input stream for the indicated resource or <b>null</b> if not found.
     */
    public static InputStream getAppRelativeStream(final String... relativePaths) {
        return _context.getResourceAsStream(joinPaths(relativePaths));
    }

    /**
     * Gets all of the contents of the indicated relative path. This returns subfolders and file contents without
     * descending into the subfolders. If you want to find all children, including those in subfolders, use the
     * {@link #getAppRelativeLocationChildren(String...)} method. If you want to filter the filenames returned without
     * descending, use the {@link #getAppRelativeLocationContents(java.io.FilenameFilter, String...)} method.
     * @param relativePaths    The relative paths to search.
     * @return A set of paths to the contents of the indicated relative path.
     */
    public static Set<String> getAppRelativeLocationContents(final String... relativePaths) {
        return getAppRelativeLocationContents(null, relativePaths);
    }

    /**
     * Gets all of the file contents of the indicated relative path that match the submitted filename filter. This
     * return subfolders and filter contents without descending into the subfolders. If you want to find all children,
     * including those in subfolders, use the {@link #getAppRelativeLocationChildren(java.io.FilenameFilter, String...)}
     * method.
     * Note that the <b>File</b> parameter of the {@link java.io.FilenameFilter#accept(java.io.File, String)} method
     * will always be null, so your implementation shouldn't use that parameter and only match the name. If you want to
     * include subfolders in your results, add a condition to match names that {@link java.lang.String#endsWith(String)
     * end with the '/' character}.
     * @param filter           An implementation of the {@link java.io.FilenameFilter} class that filters on filename.
     * @param relativePaths    The relative paths to search.
     * @return A set of paths to the contents of the indicated relative path.
     */
    public static Set<String> getAppRelativeLocationContents(FilenameFilter filter, final String... relativePaths) {
        final Set<String> paths = _context.getResourcePaths(joinPaths(relativePaths));
        if (filter == null) {
            return paths;
        }
        final Set<String> accepted = new HashSet<>();
        for (final String path : paths) {
            if (filter.accept(null, getFileName(path))) {
                accepted.add(path);
            }
        }
        return accepted;
    }

    /**
     * Gets all of the file contents of the indicated relative path and its subfolders. This does not return subfolders,
     * but resolves the contents of all subfolders. If you want to find just the contents of the path, including
     * subfolders, without descending, use the {@link #getAppRelativeLocationContents(String...)} method.
     * @param relativePaths    The relative paths to search.
     * @return A set of paths to the contents of the indicated relative path.
     */
    public static Set<String> getAppRelativeLocationChildren(final String... relativePaths) {
        return getAppRelativeLocationChildren(null, relativePaths);
    }

    /**
     * Gets all of the file contents of the indicated relative path and its subfolders. This does not return subfolders,
     * but resolves the contents of all subfolders. If you want to find just the contents of the path, including
     * subfolders, without descending, use the {@link #getAppRelativeLocationContents(String...)} method.
     * Note that the <b>File</b> parameter of the {@link java.io.FilenameFilter#accept(java.io.File, String)} method
     * will always be null, so your implementation shouldn't use that parameter and only match the name.
     * @param filter           An implementation of the {@link java.io.FilenameFilter} class that filters on filename.
     * @param relativePaths    The relative paths to search.
     * @return A set of paths to the contents of the indicated relative path.
     */
    public static Set<String> getAppRelativeLocationChildren(final FilenameFilter filter, final String... relativePaths) {
        final Set<String> found = getAppRelativeLocationContents(relativePaths);
        final Set<String> children = new HashSet<>();
        for (final String current : found) {
            if (!current.endsWith("/")) {
                if (filter == null || filter.accept(null, getFileName(current))) {
                    children.add(current);
                }
            } else {
                children.addAll(getAppRelativeLocationChildren(current));
            }
        }
        return children;
    }

    /**
     * This is a convenience wrapper around the {@link #getAppRelativeLocation(String...)} method that takes a single
     * resource name and tries to load it from the configuration folder (by default, WEB-INF/conf).
     *
     * @param configuration    The resource to retrieve.
     * @return A URI for the requested resource if found, <b>null</b> if not.
     */
    public static URI getConfigurationLocation(final String configuration) {
        return getAppRelativeLocation("WEB-INF", "conf", configuration);
    }

    /**
     * This is a convenience wrapper around the {@link #getAppRelativeStream(String...)} method that takes a single
     * resource name and tries to load it from the configuration folder (by default, WEB-INF/conf).
     *
     * @param configuration    The resource to retrieve.
     * @return An input stream for the requested resource if found, <b>null</b> if not.
     */
    public static InputStream getConfigurationStream(final String configuration) {
        return getAppRelativeStream("WEB-INF", "conf", configuration);
    }

    public class SequenceUpdater extends Thread{
		public void run(){
			DBAction.AdjustSequences();
		}
	}
	
    /**
     * When updating the database, the function and views are always recreated.  The tables are only created if necessary.
     * 
     * The statements to create the tables are passed in via the addStatements method.
     *
     */
    public class DatabaseUpdater extends Thread 
    {                   
    	final String conf;
    	final List<String> sql=Lists.newArrayList();
    	
    	/**
         * Updates the database from init files in the conf folder.
    	 * @param conf: Location of the WEB-INF/conf.  Should be NULL if you don't want to look for init scripts and run them.
    	 */
    	public DatabaseUpdater(String conf){
    		this.conf=conf;
    	}    	
    	
    	public void addStatements(List<String> more){
    		sql.addAll(more);
    	}
            
        public void run()                       
        {              
        	Transaction transaction=PoolDBUtils.getTransaction();
        	try {        	
        		transaction.start();
        		//prep accessory tables, may involve procedure calls
        		logger.info("Initializing administrative functions...");
        		transaction.execute(GenericWrapperUtils.GetExtensionTables());
				
        		//manually execute create statements
        		if(sql.size()>0){
        			logger.info("Initializing database schema...");
	        		transaction.execute(sql);
        		}
								
				//update the stored functions used for retrieving XFTItems and manipulating them
				//this used to build one big file of sql... but it was way to big.  So now it will do one element at at time.
				List<String> runAfter=Lists.newArrayList();
				logger.info("Initializing database functions...");
                for (Object o : XFTManager.GetInstance().getOrderedElements()) {
	                GenericWrapperElement element = (GenericWrapperElement) o;
	                List<String>[] func=GenericWrapperUtils.GetFunctionStatements(element);
	                transaction.execute(func[0]);
	                runAfter.addAll(func[1]);
	            }
			
                //create the views defined in the display documents
                logger.info("Initializing database views...");
        		transaction.execute(DisplayManager.GetCreateViewsSQL(false));
				
				transaction.execute(runAfter);
								
				if(conf!=null){
					final List<String> inits=getInitScripts(conf);
					if(inits.size()>0){
						transaction.execute(inits);
					}
				}
				
				transaction.commit();
			} catch (Exception e) {
				try {
					transaction.rollback();
				} catch (SQLException e1) {
					logger.error("",e1);
				}
				logger.error("",e);
				return;
			}finally{
				transaction.close();
			}
			
			try{
				//when the database is updated directly (rather then through XFT), the sequences can get out of sync.  This would cause issues down the road.
				//so we fix them
				DBAction.ADJUSTED_SEQUENCES=false;
				DBAction.AdjustSequences();
			} catch (Exception e) {
				logger.error("",e);
			}
	
			// code will dynamically register data types
			try {
				//this is to trigger a review of the Data Types that have been defined in jar configurations.
				//it needs to run after all the db initialization is done.

				if(ElementSecurity.registerNewTypes()){
					//when the database is updated directly (rather then through XFT), the sequences can get out of sync.  This would cause issues down the road.
					//so we fix them
					DBAction.ADJUSTED_SEQUENCES=false;
					DBAction.AdjustSequences();
				}
			} catch (Throwable e) {
				logger.error("",e);
			}
        }
        
    	/**
    	 * returns a list of sql statements pulled from .sql files in WEB-INF/conf/default-sql/*.sql.
    	 * 
    	 * These files should initialize the server, its settings and the users.
    	 * 
    	 * @param conf     The configuration folder.
    	 * @return Any initialization scripts located in the conf folder.
    	 */
    	public List<String> getInitScripts(String conf) {
    		File sql_conf=new File(conf);
			final List<String> stmts=Lists.newArrayList();

    		//look for files in the WEB-INF/conf/default-sql/*.sql
    		if(sql_conf.exists()){
    			System.out.println("===========================");
    			System.out.println("Populating default user list");
    			System.out.println("===========================");
    			File[] custom_sql=sql_conf.listFiles(new FilenameFilter(){
    				@Override
    				public boolean accept(File arg0, String arg1) {
    					return (arg1.endsWith(".sql"));
    				}});

    			//files can be sorted by adding _ (integer) to the end of the file name. file_1.sql is run before file_2.sql
    			Arrays.sort(custom_sql, new Comparator<File>() {
    				@Override
    				public int compare(File arg0, File arg1) {
    					return findIndex(arg0)-findIndex(arg1);
    				}

    				private int findIndex(File f){
    					if(f.getName().contains("_")){
    						final String index=f.getName().substring(f.getName().lastIndexOf("_")+1,f.getName().indexOf(".sql"));
    						return NumberUtils.toInt(index, 0);
    					}else{
    						return 0;
    					}
    				}
    			});

    			if(custom_sql!=null && custom_sql.length>0){
    				for(File sql: custom_sql){
    					try {
    						for(final String stmt:FileUtils.FileLinesToArrayList(sql)){
    							if(!StringUtils.isWhitespace(stmt)){
    								stmts.add((stmt.contains(";"))?stmt:stmt+";");
    							}
    						}
    					} catch (Exception e) {
    						logger.error("",e);
    					}
    				}
    			}
    		}
    		return stmts;
    	}
    }

    /**
     * Method used to manage auto-updates to the database.  It will only auto update the database, if the auto-update parameter is null or true.
     *
     * It uses a query on the user table to identify if the user table to see if users have been populated.
     * @param config    The configuration folder.
     * @return Whether the database update was successful or not.
     * @throws Exception
     */
    private boolean updateDatabase(final String config) throws Exception {
        Long user_count;
        try {
            user_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xdat_user", "count", null, null);
        } catch (SQLException e) {
            // xdat_user table doesn't exist
            user_count = null;
        }

        //this should use the config service.. but I couldn't get it to work because of servlet init issues.
        Properties prop = new Properties();
        String dbProperties = joinPaths(config, "properties", "database.properties");
        File f = new File(dbProperties);
        if(f.exists()){
            prop.load(new FileInputStream(f));
        } else {
            InputStream stream = getAppRelativeStream(config);
            if (stream != null) {
                prop.load(stream);
            }
        }

        //currently defaults to true, if the file isn't there.
        if(!prop.containsKey("auto-update")){
            prop.setProperty("auto-update", "true");
        }

        if((prop.containsKey("auto-update")) && (BooleanUtils.toBoolean(prop.getProperty("auto-update")))){
            if (user_count != null) {
                final DatabaseUpdater du = new DatabaseUpdater((user_count == 0) ? config : null);//user_count==0 means users need to be created.

                //only interested in the required ones here.
                final List<String> sql = SQLUpdateGenerator.GetSQLCreate()[0];
                if (sql.size() > 0) {
                    System.out.println("===========================");
                    System.out.println("Database out of date... updating");
                    for(String s:sql)System.out.println(s);
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
                    (new SequenceUpdater()).start();//this isn't necessary if we did the du.start();
                    return false;
                }
            } else {
                System.out.println("===========================");
                System.out.println("New Database -- BEGINNING Initialization");
                System.out.println("===========================");
                // xdat-user table doesn't exist, assume this is an empty
                // database
                final DatabaseUpdater du = new DatabaseUpdater(config);
                du.addStatements(SQLCreateGenerator.GetSQLCreate(false));
                du.run();// start and wait for it

                System.out.println("===========================");
                System.out.println("Database initialization complete.");
                System.out.println("===========================");
                return true;
            }
        } else {
            (new SequenceUpdater()).start();
            return false;
        }
    }

    private void replaceLogging() {
		// remove the java.util.logging handlers so that nothing is logged to stdout/stderr
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		for (Handler h : rootLogger.getHandlers()){
			rootLogger.removeHandler(h);
		}
		
		try {
			// use the sl4j bridge to redirect restlet logging to log4j
			SLF4JBridgeHandler.install();
		} catch (Throwable e) {
			e.printStackTrace();
		} 
	}

    private static String getFileName(final String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }

    private static String joinPaths(String... elements){
        // MIGRATE: This is not using File.separator, since that causes issues locating files via URI, JNDI, etc.
        return Joiner.on("/").join(elements);
    }
}

