//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.servlet;

import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.db.DBAction;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.logging.Handler;
import java.util.logging.LogManager;

/**
 * @author Tim
 *
 */
@SuppressWarnings("serial")
public class XDATServlet extends HttpServlet{
	public void init(ServletConfig config) throws ServletException
	{
		replaceLogging();

		super.init(config);
		
		try {
            final URI path = this.getServletContext().getResource("WEB-INF" + File.separator + "conf" + File.separator).toURI();

			XDAT.init(path, true, false);

            XDAT.setScreenTemplatesFolder(this.getServletContext().getRealPath("templates" + File.separator + "screens" + File.separator));
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("templates" + File.separator + "screens" + File.separator));
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("xnat-templates" + File.separator + "screens" + File.separator));
            XDAT.addScreenTemplatesFolder(this.getServletContext().getRealPath("xdat-templates" + File.separator + "screens" + File.separator));
			
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
        } catch (SQLException ignored) {
        }
        super.destroy();
	}
    
    public class DelayedSequenceChecker extends Thread 
    {                      
            public void run()                       
            {              
                DBAction.AdjustSequences();
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
    
}

