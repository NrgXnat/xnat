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

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.exceptions.ConfigServiceException;
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
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.modules.actions.XDATLoginUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.services.XftFieldExclusionService;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
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
    private static String _configFilesLocation = null;
    private String instanceSettingsLocation = null;
    private static File _screenTemplatesFolder;
    private static List<File> _screenTemplatesFolders=new ArrayList<File>();
    private static boolean _hasRefreshedSiteConfiguration = false;

    /**
     * Gets the site configuration as a Java {@link Properties} object.
     * @return The initialized Java {@link Properties} object.
     * @throws ConfigServiceException Thrown when an error occurs resolving or accessing the configuration service.
     */
    public static Properties getSiteConfiguration() throws ConfigServiceException {
        if (!_hasRefreshedSiteConfiguration) {
            return refreshSiteConfiguration();
        }

        return convertStringToProperties(getConfigService().getConfig("site", "siteConfiguration").getContents());
    }

    /**
     * Sets the site configuration from the submitted Java {@link Properties} object. This updates the data stored in the
     * configuration service, but does not modify or update the source properties bundle stored on the local disk.
     * @param properties    The initialized Java {@link Properties} object.
     * @throws ConfigServiceException Thrown when an error occurs resolving or accessing the configuration service.
     */
    public static void setSiteConfiguration(Properties properties) throws ConfigServiceException {
        setSiteConfiguration(properties, "Setting site configuration");
    }

    /**
     * Refreshes the site configuration from the <b>siteConfiguration.properties</b> file. This should generally be done
     * only once per application start-up.
     * @throws ConfigServiceException Thrown when an error occurs resolving or accessing the configuration service.
     */
    public static Properties refreshSiteConfiguration() throws ConfigServiceException {
        Properties properties = new Properties();
        try {
            String siteConfiguration = FileUtils.ReadFromFile(new File(_configFilesLocation, "siteConfiguration.properties"));
            properties.load(new ByteArrayInputStream(siteConfiguration.getBytes()));
        } catch (IOException exception) {
            logger.error("Error occurred trying to load site configuration properties bundle from " + (new File(_configFilesLocation, "siteConfiguration.properties")).getAbsolutePath(), exception);
        }

        org.nrg.config.entities.Configuration configuration = getConfigService().getConfig("site", "siteConfiguration");
        if (configuration == null) {
            setSiteConfiguration(properties);
        } else {
            int hash = properties.hashCode();
            properties.putAll(convertStringToProperties(configuration.getContents()));
            if (hash != properties.hashCode()) {
                setSiteConfiguration(properties);
            }
        }

        _hasRefreshedSiteConfiguration = true;
        return properties;
    }

    public static String getSiteConfigurationProperty(String property) throws ConfigServiceException {
        Properties properties = getSiteConfiguration();
        return properties.getProperty(property);
    }

    public static void setSiteConfigurationProperty(String property, String value) throws ConfigServiceException {
        Properties properties = getSiteConfiguration();
        properties.setProperty(property, value);
        setSiteConfiguration(properties, "Setting site configuration property value: " + property);
    }

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
	
	public static void setNewUserDetails(XDATUserDetails userDetails, RunData data, Context context) {
		//SecurityContextHolder.getContext().setAuthentication(null);
		String username = userDetails.getLogin();
		String password = userDetails.getPrimaryPassword();
		XDATUser user;
		try {
			user = Authenticator.Authenticate(new Authenticator.Credentials(
                    username, password));

			XFTItem item = XFTItem.NewItem("xdat:user_login", user);
			Date today = java.util.Calendar.getInstance(TimeZone.getDefault()).getTime();
			item.setProperty("xdat:user_login.user_xdat_user_id", user.getID());
			item.setProperty("xdat:user_login.login_date", today);
			item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(data.getRequest()));
			SaveItemHelper.authorizedSave(item, null, true, false, null);

			HttpSession session = data.getSession();
			session.setAttribute("user", user);
			session.setAttribute("loggedin", true);

			session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());

			AccessLogger.LogActionAccess(data, "Valid Login:" + user.getLogin());

		} catch (Exception exception) {
            logger.error("Error performing su operation", exception);
		}
		XDATLoginUser newlogin = new XDATLoginUser();
		try {
			newlogin.doRedirect(data, context, userDetails);
		} catch (Exception exception) {
			logger.error("Error performing su redirect", exception);
		}
	}

	/**
	 * configure torque
	 *
	 * @param configuration Configuration
	 * @see org.apache.stratum.lifecycle.Configurable
	 */
	public void configure(Configuration configuration) 	{
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
		} catch (Exception exception) {
            System.out.println(exception);
			exception.printStackTrace();
		}
	}

	public static void init(String location) throws Exception
	{
		XDAT.init(location, true, true);
	}

	public static void RefreshDisplay()
	{
		DisplayManager.clean();
		DisplayManager.GetInstance();
	}

	public static void init(String location,boolean allowDBAccess) throws Exception
	{
		init(location, allowDBAccess, true);
	}

	public static void init(String location,boolean allowDBAccess, boolean initLog4j) throws Exception
	{
		DisplayManager.clean();
        if (StringUtils.isBlank(_configFilesLocation)) {
            _configFilesLocation = FileUtils.AppendSlash(location);
        }

		if (initLog4j)
		{
			PropertyConfigurator.configure(_configFilesLocation + "log4j.properties");
			initLog4j= false;
		}

		XFT.init(_configFilesLocation, allowDBAccess, initLog4j);
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
                // This is actually OK, it just means there are no overrides, so return null.
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
        boolean created = false;

        Category category = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event.toString());
        if (category == null) {
            category = getNotificationService().getCategoryService().newEntity();
            category.setScope(CategoryScope.Site);
            category.setEvent(event.toString());
            getNotificationService().getCategoryService().create(category);
            created = true;
        }

        Definition definition;
        List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
        if (definitions == null || definitions.size() == 0) {
            definition = getNotificationService().getDefinitionService().newEntity();
            definition.setCategory(category);
            getNotificationService().getDefinitionService().create(definition);
            created = true;
        } else {
            definition = definitions.get(0);
        }

        // If we created either the category or the definition, obviously there aren't any current subscribers, so we
        // don't even bother to check. If we *didn't* create the category or the definition, there may be subscribers,
        // so let's check that.
        if (!created) {
            List<Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);
            if (subscriptions != null && subscriptions.size() > 0) {
                // There are subscribers! Our work here is done.
                return;
            }
        }

        // If we made it this far, there are no subscribers to the indicated site-wide event, so create a subscriber and
        // set it to the system administrator.
        Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(ADMIN_USERNAME_FOR_SUBSCRIPTION);
        if (subscriber == null) {
            try {
                subscriber = getNotificationService().getSubscriberService().createSubscriber(ADMIN_USERNAME_FOR_SUBSCRIPTION, adminEmail);
            } catch (DuplicateSubscriberException exception) {
                // This shouldn't happen, since we just checked for the subscriber's existence.
            }
        }

        assert subscriber != null : "Unable to create or retrieve subscriber for the admin user";

        // We have an event and subscriber, let's bring them together.
        getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
    }

    /**
     * Retrieves the HTML mail channel to be used for default subscription configuration.
     * @return The HTML mail channel definition.
     */
    public static Channel getHtmlMailChannel() {
        Channel channel = getNotificationService().getChannelService().getChannel("htmlMail");
        if (channel == null) {
            channel = getNotificationService().getChannelService().newEntity();
            channel.setName("htmlMail");
            channel.setFormat("text/html");
            getNotificationService().getChannelService().create(channel);
        }
        return channel;
    }

    private static void setSiteConfiguration(Properties properties, String message) throws ConfigServiceException {
        XDATUserDetails user = getUserDetails();
        String username = "";
        if (user != null) {
            username = user.getUsername();
}

        if (logger.isInfoEnabled()) {
            if (user == null) {
                logger.info(message + ", no user details available");
            } else {
                logger.info(message + ", user: " + username);
            }
        }

        synchronized (XDAT.class) {
            getConfigService().replaceConfig(username, message, "site", "siteConfiguration", convertPropertiesToString(properties, message));
        }
    }

    private static String convertPropertiesToString(final Properties properties, String message) {
        StringWriter writer = new StringWriter();
        try {
            properties.store(new PrintWriter(writer), message);
        } catch (IOException ignored) {
            // Ignore this, we're not writing to a file so it should be fine.
        }
        return writer.getBuffer().toString();
    }

    private static Properties convertStringToProperties(final String contents) {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(contents.getBytes()));
        } catch (IOException ignored) {
            // We ignore this, it just can't happen since we're dealing with a flat string.
        }
        return properties;
    }
}
