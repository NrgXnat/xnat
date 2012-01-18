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

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.MarshallerCacheService;
import org.nrg.mail.services.MailService;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.services.XftFieldExclusionService;
import org.nrg.xft.utils.FileUtils;

import java.io.File;

/**
 * @author Tim
 *
 */
public class XDAT implements Initializable,Configurable{
	static Logger logger = Logger.getLogger(XDAT.class);
	private static ContextService _contextService;
	private static MailService _mailService;
    private static NotificationService _notificationService;
    private static XftFieldExclusionService _exclusionService;
    private static MarshallerCacheService _marshallerCacheService;
    private static ConfigService _configurationService;
	private String instanceSettingsLocation = null;
    private static File _screenTemplatesFolder;

	/**
	 * configure torque
	 *
	 * @param configuration Configuration
	 * @see org.apache.stratum.lifecycle.Configurable
	 */
	public void configure(Configuration configuration)
	{
		instanceSettingsLocation = configuration.getString("instance_settings_directory");
	}

	/**
	 * initialize Torque
	 *
	 * @see org.apache.stratum.lifecycle.Initializable
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
                for (ElementSecurity es : ElementSecurity.GetQuarantinedElements()) {
                    GenericWrapperElement.GetElement(es.getElementName()).setQuarantineSetting(es.getBooleanProperty("quarantine", false));
                }

                for (ElementSecurity es : ElementSecurity.GetPreLoadElements()) {
                    GenericWrapperElement.GetElement(es.getElementName()).setPreLoad(es.getBooleanProperty("pre_load", false));
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

	public static void GenerateUpdateSQL(String file) throws Exception
	{
        StringBuilder buffer = new StringBuilder();
	    buffer.append("-- Generated SQL File for updating an existing XNAT database.\n");
	    buffer.append("-- This script is created by the update XNAT feature, which reviews an existing database and only specifies create statements for missing TABLES and COLUMNS.  It will also drop and recreate any necessary functions or views.\n");
	    buffer.append("-- If you are running from pgAdmin, remove the following line to stop on errors (pgAdmin does not recognize the statement)\n");
	    buffer.append("\\set ON_ERROR_STOP on;\n");
	    buffer.append("\n-- start transaction (if an error occurs, the database will be rolled back to its state before this file was executed)\n");
	    buffer.append("BEGIN;\n");

	    for (Object item : SQLUpdateGenerator.GetSQLCreate())
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
		buffer.append("\n\n-- REMOVE OLD VIEWS FOR DISPLAY DOCS\n\n");
		buffer.append("\n\nSELECT removeViews();\n--BR\n");
		buffer.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
	    for (Object item : DisplayManager.GetCreateViewsSQL(true))
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
	    buffer.append("\n-- commit transaction\n");
	    buffer.append("COMMIT;");
		FileUtils.OutputToFile(buffer.toString(),file);

		logger.info("File Created: " + file);
	}

	public static void GenerateCreateSQL(String file) throws Exception
	{
        StringBuilder buffer = new StringBuilder();
	    buffer.append("-- Generated SQL File for creating an XNAT database from scratch.\n");
	    buffer.append("-- If you are running from pgAdmin, remove the following line to stop on errors (pgAdmin does not recognize the statement)\n");
	    buffer.append("\\set ON_ERROR_STOP on;\n");
	    buffer.append("\n-- start transaction (if an error occurs, the database will be rolled back to its state before this file was executed)\n");
	    buffer.append("BEGIN;\n");

	    for (Object item : SQLCreateGenerator.GetSQLCreate())
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
		buffer.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
	    for (Object item : DisplayManager.GetCreateViewsSQL(false))
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
	    buffer.append("\n-- commit transaction\n");
	    buffer.append("COMMIT;");
		FileUtils.OutputToFile(buffer.toString(),file);

		ViewManager.OutputFieldNames();
		logger.info("File Created: " + file);
	}

	/**
     * Returns an instance of the Spring application context service. This provides a wrapper around the Spring
     * application context to allow non-Spring components in XNAT to access the context.
     * @return An instance of the {@link ContextService Spring application context service}.
	 */
	public static ContextService getContextService() {
		if (_contextService == null) {
		    _contextService = ContextService.getInstance();
		}
    	return _contextService;
	}

	/**
	 * Returns an instance of the currently supported mail service.
	 * @return An instance of the {@link MailService} service.
	 */
	public static MailService getMailService() {
	    if (_mailService == null) {
	        _mailService = getContextService().getBean(MailService.class);
	    }
	    return _mailService;
	}

	/**
	 * Returns an instance of the currently supported {@link NotificationService notification service}.
	 * @return An instance of the {@link NotificationService notification service}.
	 */
	public static NotificationService getNotificationService() {
	    if (_notificationService == null) {
	        _notificationService = getContextService().getBean(NotificationService.class);
	    }
	    return _notificationService;
	}
	
	/**
	 * Returns an instance of the currently supported {@link XftFieldExclusionService exclusion service}.
	 * @return An instance of the {@link XftFieldExclusionService exclusion service}.
	 */
	public static XftFieldExclusionService getExclusionService() {
	    if (_exclusionService == null) {
	        _exclusionService = getContextService().getBean(XftFieldExclusionService.class);
	    }
	    return _exclusionService;
	}

    /**
     * Returns an instance of the currently supported {@link MarshallerCacheService XML marshaler cache service}.
     * @return An instance of the {@link MarshallerCacheService XML marshaller cache service}.
     */
    public static MarshallerCacheService getMarshallerCacheService() {
        if (_marshallerCacheService == null) {
            _marshallerCacheService = getContextService().getBean(MarshallerCacheService.class);
}
        return _marshallerCacheService;
    }

    /**
     * Returns the folder containing screen templates. These are installed by custom datatypes, modules, and other
     * customizations that extend or override the default application behavior.
     * @return The full path to the screen templates folder.
     */
    public static String getScreenTemplatesFolder() {
        return _screenTemplatesFolder.getAbsolutePath();
}

    public static void setScreenTemplatesFolder(String screenTemplatesFolder) {
        _screenTemplatesFolder = new File(screenTemplatesFolder);
    }

    public static File getScreenTemplatesSubfolder(String subfolder) {
        if (StringUtils.isBlank(subfolder)) {
            return new File(getScreenTemplatesFolder());
        }

        File current = new File(getScreenTemplatesFolder(), "");

        String[] subfolders = subfolder.split("/");
        for (String folder : subfolders) {
            current = new File(current, folder);
            if (!current.exists()) {
                throw new NrgRuntimeException("The folder indicated by " + current.getAbsolutePath() + " doesn't exist.");
            }
            if (!current.isDirectory()) {
                throw new NrgRuntimeException("The path indicated by " + current.getAbsolutePath() + " isn't a folder.");
            }
        }

        return current;
    }
    
	/**
	 * Returns an instance of the currently supported configuration service.
	 * @return An instance of the {@link ConfigService} service.
	 */
	public static ConfigService getConfigService() {
	    if (_configurationService == null) {
	    	_configurationService = getContextService().getBean(ConfigService.class);
	    }
	    return _configurationService;
	}
}
