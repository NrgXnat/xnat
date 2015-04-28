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

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Handler;
import java.util.logging.LogManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class XDATServlet extends HttpServlet{
	static org.apache.log4j.Logger logger = Logger.getLogger(XDATServlet.class);
	public void init(ServletConfig config) throws ServletException
	{
		replaceLogging();

		super.init(config);
		
		try {
            String path = this.getServletContext().getRealPath("WEB-INF" + File.separator + "conf" + File.separator);

            String webapp = path.substring(0,path.lastIndexOf(File.separator + "WEB-INF" + File.separator + "conf"));
            webapp = webapp.substring(webapp.lastIndexOf(File.separator));
            System.out.println("WEBAPP:" + webapp);
            //XFT.setWEBAPP_NAME(webapp);
            
			XDAT.init(path,true,false);
            XDAT.setScreenTemplatesFolder(this.getServletContext().getRealPath("templates" + File.separator + "screens" + File.separator));
            
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("templates" + File.separator + "screens" + File.separator));
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("xnat-templates" + File.separator + "screens" + File.separator));
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("xdat-templates" + File.separator + "screens" + File.separator));

            Reflection.injectDynamicImplementations("org.nrg.xnat.extensions.server.startup.sync", null);
            
            Thread t = new DelayedSequenceChecker();
            t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void destroy()
	{
	    try {
            XFT.closeConnections();
        } catch (SQLException e) {
        }
        super.destroy();
	}
    
    public class DelayedSequenceChecker extends Thread 
    {                      
            public void run()                       
            {              
                DBAction.AdjustSequences();
                
                Reflection.injectDynamicImplementations("org.nrg.xnat.extensions.server.startup.async", null);

            }
    }
//    
//    public class DelayedGroupLoader extends Thread 
//    {                      
//            public void run()                       
//            {              
//                UserGroupManager.GetAllUserGroups();
//            }
//    }
//    
//    public class DelayedSearchLoader extends Thread 
//    {                      
//            public void run()                       
//            {              
//                XdatStoredSearch.GetPreLoadedSearches();
//            }
//    }

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
}

