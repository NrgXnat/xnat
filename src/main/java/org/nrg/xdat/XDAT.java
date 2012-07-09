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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

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
import org.nrg.framework.services.PropertiesService;
import org.nrg.mail.api.NotificationType;
import org.nrg.mail.services.MailService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.services.XftFieldExclusionService;
import org.nrg.xft.utils.FileUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Tim
 *
 */
public class XDAT implements Initializable,Configurable{

    static Logger logger = Logger.getLogger(XDAT.class);
	private static ContextService _contextService;
    private static DataSource _dataSource;
	private static MailService _mailService;
    private static NotificationService _notificationService;
    private static PropertiesService _propertiesService;
    private static XftFieldExclusionService _exclusionService;
    private static MarshallerCacheService _marshallerCacheService;
	private static XdatUserAuthService _xdatUserAuthService;
    private static ConfigService _configurationService;
    public static final String ADMIN_USERNAME_FOR_SUBSCRIPTION = "ADMIN_USER";
    private String instanceSettingsLocation = null;
    private static File _screenTemplatesFolder;
    private static List<File> _screenTemplatesFolders=new ArrayList<File>();

	public static boolean isAuthenticated() {
		return SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
	}

	public static XDATUserDetails getUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication != null && authentication.getPrincipal() != null
				&& authentication.getPrincipal() instanceof XDATUserDetails) {
			return (XDATUserDetails) authentication.getPrincipal();
		}

		return null;
	}

	public static void setUserDetails(XDATUserDetails userDetails) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
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
	                GenericWrapperElement.GetElement(es.getElementName()).setQuarantineSetting(es.getBooleanProperty(ViewManager.QUARANTINE,false));
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
	 * Returns an instance of the currently supported {@link NotificationService notification service}.
	 * @return An instance of the {@link NotificationService notification service}.
	 */
	public static PropertiesService getPropertiesService() {
	    if (_propertiesService == null) {
            _propertiesService = getContextService().getBean(PropertiesService.class);
	    }
	    return _propertiesService;
	}

	/**
	 * Returns an instance of the currently supported {@link XftFieldExclusionService exclusion service}.
	 * @return An instance of the {@link XftFieldExclusionService exclusion service}.
	 */
	public static XftFieldExclusionService getExclusionService() {
	    if (_exclusionService == null) {
	        _exclusionService = getContextService().getBean(org.nrg.xft.services.XftFieldExclusionService.class);
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

    
    public static void addScreenTemplatesFolder(String screenTemplatesFolder) {
        _screenTemplatesFolders.add(new File(screenTemplatesFolder));
    }

    public static List<File> getScreenTemplateFolders(){
    	return _screenTemplatesFolders;
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
                logger.error("",new NrgRuntimeException("The folder indicated by " + current.getAbsolutePath() + " doesn't exist."));
                return null;
            }
            if (!current.isDirectory()) {
                logger.error("",new NrgRuntimeException("The path indicated by " + current.getAbsolutePath() + " isn't a folder."));
                return null;
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

	/**
	 * Returns an instance of the currently supported data source.
	 * @return An instance of the {@link DataSource} bean.
	 */
	public static DataSource getDataSource() {
	    if (_dataSource == null) {
	    	_dataSource = getContextService().getBean(DataSource.class);
	    }
	    return _dataSource;
	}

	public static XdatUserAuthService getXdatUserAuthService() {
		if (_xdatUserAuthService == null) {
			_xdatUserAuthService = getContextService().getBean(
					XdatUserAuthService.class);
		}
		return _xdatUserAuthService;
	}

    /**
     * This verifies that a notification and subscriber exists for the indicated site-wide event. If the notification or
     * subscriber does <i>not</i> exist, one is created using the primary administrative user.
     * @param event    The site-wide event to be verified.
     */
    public static void verifyNotificationType(NotificationType event) {
        final String adminEmail = XFT.GetAdminEmail();
        final Channel channel = getHtmlMailChannel();
        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event);
        if (category == null) {
            category = getNotificationService().getCategoryService().newEntity();
            category.setScope(CategoryScope.Site);
            category.setEvent(event);
            getNotificationService().getCategoryService().create(category);
        }
        Definition definition;
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        if (definitions == null || definitions.size() == 0) {
            definition = getNotificationService().getDefinitionService().newEntity();
            definition.setCategory(category);
            getNotificationService().getDefinitionService().create(definition);
        } else {
            definition = definitions.get(0);
        }

        Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(ADMIN_USERNAME_FOR_SUBSCRIPTION);
        if (subscriber == null) {
            try {
                subscriber = getNotificationService().getSubscriberService().createSubscriber(ADMIN_USERNAME_FOR_SUBSCRIPTION, adminEmail);
            } catch (DuplicateSubscriberException exception) {
                // This shouldn't happen, since we just checked for the subscriber's existence.
            }
        }

        assert subscriber != null : "Unable to create or retrieve subscriber for the admin user";

        List<Subscription> subs = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);
        boolean found = false;
        for (Subscription sub : subs) {
            List<Long> ids = new ArrayList<Long>();
            for (Channel ch : sub.getChannels()) {
                ids.add(ch.getId());
            }
            if (sub.getSubscriber().getId() == (subscriber.getId()) && sub.getSubscriberType().equals(SubscriberType.User) && ids.contains(channel.getId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
        }
    }

    /**
     * Retrieves the HTML mail channel to be used for default subscription configuration.
     * @return The HTML mail channel definition.
     */
    private static Channel getHtmlMailChannel() {
        Channel channel = getNotificationService().getChannelService().getChannel("htmlMail");
        if (channel == null) {
            channel = getNotificationService().getChannelService().newEntity();
            channel.setName("htmlMail");
            channel.setFormat("text/html");
            getNotificationService().getChannelService().create(channel);
        }
        return channel;
    }
}
