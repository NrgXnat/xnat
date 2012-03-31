//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;
import org.apache.torque.TorqueException;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.FileUtils;
/**
 * @author Tim
 *
 */
public class XDAT implements Initializable,Configurable{
	static org.apache.log4j.Logger logger = Logger.getLogger(XDAT.class);
	private String instanceSettingsLocation = null;
	/**
	 * configure torque
	 *
	 * @param conf Configuration
	 * @see org.apache.stratum.lifecycle.Configurable
	 * @throws TorqueException Any exceptions caught during processing will be
	 *         rethrown wrapped into a TorqueException.
	 */
	public void configure(Configuration conf)
	{
		instanceSettingsLocation = conf.getString("instance_settings_directory");
	}

	/**
	 * initialize Torque
	 *
	 * @see org.apache.stratum.lifecycle.Initializable
	 * @throws TorqueException Any exceptions caught during processing will be
	 *         rethrown wrapped into a TorqueException.
	 */
	public void initialize()
	{
		try {
			logger.info("Starting Service XDAT");
			init(instanceSettingsLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init(String location) throws Exception
	{
		XDAT.init(location,true,true);
	}
	
	public static void RefreshDisplay()
	{
		DisplayManager.clean();
		DisplayManager.GetInstance();
	}
	
	public static void init(String location,boolean allowDBAccess) throws Exception
	{
		init(location,allowDBAccess,true);
	}
	
	public static void init(String location,boolean allowDBAccess, boolean initLog4j) throws Exception
	{
		DisplayManager.clean();
		if (initLog4j)
		{
			location = FileUtils.AppendSlash(location);
			PropertyConfigurator.configure(location + "log4j.properties");
			initLog4j= false;
		}
		XFT.init(location,allowDBAccess,initLog4j);
		//XFT.LogCurrentTime("XDAT INIT: 1","ERROR");
		if (allowDBAccess)
		{
			try {
	            Iterator quarantineElements = ElementSecurity.GetQuarantinedElements().iterator();
	            while (quarantineElements.hasNext())
	            {
	                ElementSecurity es = (ElementSecurity)quarantineElements.next();
	                GenericWrapperElement.GetElement(es.getElementName()).setQuarantineSetting(es.getBooleanProperty(ViewManager.QUARANTINE,false));
	            }
	            
	            Iterator preloadElements = ElementSecurity.GetPreLoadElements().iterator();
	            while (preloadElements.hasNext())
	            {
	                ElementSecurity es = (ElementSecurity)preloadElements.next();
	                GenericWrapperElement.GetElement(es.getElementName()).setPreLoad(es.getBooleanProperty("pre_load",false));
	            }
	        } catch (Exception e) {
	            logger.error("",e);
	        }
		}

		//XFT.LogCurrentTime("XDAT INIT: 2","ERROR");
		logger.info("Initializing Display Manager");
		DisplayManager.GetInstance();
		//XFT.LogCurrentTime("XDAT INIT: 3","ERROR");
	}
	
	public static void GenerateUpdateSQL(String file) throws XFTInitException,ElementNotFoundException, Exception
	{
	    StringBuffer sb =new StringBuffer();
	    sb.append("-- Generated SQL File for updating an existing XNAT database.\n");
	    sb.append("-- This script is created by the update XNAT feature, which reviews an existing database and only specifies create statements for missing TABLES and COLUMNS.  It will also drop and recreate any necessary functions or views.\n");
	    
	    sb.append("-- If you are running from pgAdmin, remove the following line to stop on errors (pgAdmin does not recognize the statement)\n");
	    sb.append("\\set ON_ERROR_STOP on;\n");
	    sb.append("\n-- start transaction (if an error occurs, the database will be rolled back to its state before this file was executed)\n");
	    sb.append("BEGIN;\n");
	    
	    Iterator iter = SQLUpdateGenerator.GetSQLCreate().iterator();
	    while (iter.hasNext())
	    {
	        sb.append(iter.next() +"\n--BR\n");
	    }
		sb.append("\n\n-- REMOVE OLD VIEWS FOR DISPLAY DOCS\n\n");
		sb.append("\n\nSELECT removeViews();\n--BR\n");
		sb.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
		iter = DisplayManager.GetCreateViewsSQL(true).iterator();
	    while (iter.hasNext())
	    {
	        sb.append(iter.next() +"\n--BR\n");
	    }
	    sb.append("\n-- commit transaction\n");
	    sb.append("COMMIT;");
		FileUtils.OutputToFile(sb.toString(),file);
		
		logger.info("File Created: " + file);
	}
	
	public static void GenerateCreateSQL(String file) throws XFTInitException,ElementNotFoundException, Exception
	{
	    StringBuffer sb =new StringBuffer();
	    sb.append("-- Generated SQL File for creating an XNAT database from scratch.\n");
	    
	    sb.append("-- If you are running from pgAdmin, remove the following line to stop on errors (pgAdmin does not recognize the statement)\n");
	    sb.append("\\set ON_ERROR_STOP on;\n");
	    sb.append("\n-- start transaction (if an error occurs, the database will be rolled back to its state before this file was executed)\n");
	    sb.append("BEGIN;\n");
	    
	    Iterator iter = SQLCreateGenerator.GetSQLCreate().iterator();
	    while (iter.hasNext())
	    {
	        sb.append(iter.next() +"\n--BR\n");
	    }
		sb.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
		iter = DisplayManager.GetCreateViewsSQL(false).iterator();
	    while (iter.hasNext())
	    {
	        sb.append(iter.next() +"\n--BR\n");
	    }
	    sb.append("\n-- commit transaction\n");
	    sb.append("COMMIT;");
		FileUtils.OutputToFile(sb.toString(),file);
		
		ViewManager.OutputFieldNames();
		logger.info("File Created: " + file);
	}
}
