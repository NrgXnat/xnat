/*
 * org.nrg.xdat.XDAT
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 12:24 PM
 */

package org.nrg.xdat;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.config.services.SiteConfigurationService;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.ContextService;
import org.nrg.mail.api.NotificationType;
import org.nrg.mail.services.MailService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.preferences.InitializerSiteConfiguration;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.modules.actions.XDATLoginUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.jms.Destination;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author Tim
 */
public class XDAT implements Initializable,Configurable{

    public static final String IP_WHITELIST_TOOL = "ipWhitelist";
    public static final String IP_WHITELIST_PATH = "/system/ipWhitelist";

    private static final Logger logger = Logger.getLogger(XDAT.class);
	private static ContextService _contextService;
    private static DataSource _dataSource;
	private static MailService _mailService;
	private static ThemeService _themeService;
    private static NotificationService _notificationService;
	private static XdatUserAuthService _xdatUserAuthService;
    private static ConfigService _configurationService;
    private static SiteConfigurationService _siteConfigurationService;
	private static SiteConfigPreferences _siteConfigPreferences;
	public static final String ADMIN_USERNAME_FOR_SUBSCRIPTION = "ADMIN_USER";
	private static String _configFilesLocation = null;
	private String instanceSettingsLocation = null;
    private static File _screenTemplatesFolder;
    private static List<File> _screenTemplatesFolders= new ArrayList<>();

    public static List<String> getWhitelistedIPs(UserI user) throws ConfigServiceException {
        return Arrays.asList(getWhitelistConfiguration(user).split("[\\s]+"));
    }

    public static String getWhitelistConfiguration(UserI user) throws ConfigServiceException {
        org.nrg.config.entities.Configuration whitelist = XDAT.getConfigService().getConfig(IP_WHITELIST_TOOL, IP_WHITELIST_PATH);
        if (whitelist == null || StringUtils.isBlank(whitelist.getContents())) {
            whitelist = createDefaultWhitelist(user);
        }
        return whitelist.getContents();
    }

    public static String getSiteConfigurationProperty(String property,String _default) throws ConfigServiceException {
    	Properties properties = getSiteConfiguration();
        if(properties.containsKey(property)){
        	return properties.getProperty(property);
        }else{
        	return _default;
        }
    }

    public static boolean getBoolSiteConfigurationProperty(String property,boolean _default) {
    	try {
			Properties properties = getSiteConfiguration();
			if(properties.containsKey(property)){
				return Boolean.valueOf(properties.getProperty(property));
			}else{
				return _default;
			}
		} catch (ConfigServiceException e) {
			return _default;
		}
    }

    public static String safeSiteConfigProperty(String property, String _default){
    	try{
	    	Properties properties = getSiteConfiguration();
	        if(properties.containsKey(property)){
	        	return properties.getProperty(property);
	        }else{
	        	return _default;
	        }
    	}catch(Throwable e){
        	return _default;
    	}
    }

	public static boolean verificationOn() {
		return getBoolSiteConfigurationProperty("emailVerification",false);
	}

	public static boolean isAuthenticated() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
	}

	public static UserI getUserDetails() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return null;
		}

		final Object principal = authentication.getPrincipal();
		if (principal == null) {
			return null;
		}

		if (principal instanceof UserI) {
			return (UserI) principal;
		}

		if (principal instanceof String) {
			if (StringUtils.isBlank((String) principal)) {
				return null;
			}
			try {
				return Users.getUser((String) principal);
			} catch (UserInitException e) {
				throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the user with login: " + principal, e);
			} catch (UserNotFoundException e) {
				return null;
			}
		}

		return null;
	}

	public static void setUserDetails(UserI userDetails) {
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public static void setNewUserDetails(UserI userDetails, RunData data, Context context) {
		//SecurityContextHolder.getContext().setAuthentication(null);
		String username = userDetails.getLogin();
		String password = userDetails.getPassword();
		UserI user;
		try {
			user = Authenticator.Authenticate(new Authenticator.Credentials(
                    username, password));

			XDAT.setUserDetails(user);

			XFTItem item = XFTItem.NewItem("xdat:user_login", user);
			Date today = java.util.Calendar.getInstance(TimeZone.getDefault()).getTime();
			item.setProperty("xdat:user_login.user_xdat_user_id", user.getID());
			item.setProperty("xdat:user_login.login_date", today);
			item.setProperty("xdat:user_login.ip_address", AccessLogger.GetRequestIp(data.getRequest()));
	        item.setProperty("xdat:user_login.session_id", data.getSession().getId());
			SaveItemHelper.authorizedSave(item, null, true, false, (EventMetaI)null);

			HttpSession session = data.getSession();
			session.setAttribute("loggedin", true);
			session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());

			AccessLogger.LogActionAccess(data, "Valid Login:" + user.getUsername());

		} catch (Exception exception) {
            logger.error("Error performing su operation", exception);
		}
		try {
            new XDATLoginUser().doRedirect(data, context, userDetails);
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
            logger.error(exception);
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

		XFT.init(_configFilesLocation);

		Long user_count;
		try {
			user_count = (Long) PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(*) FROM xdat_user", "count", null, null);
		} catch (Throwable e) {
			// xdat_user table doesn't exist
			user_count = null;
		}

		if (allowDBAccess && user_count!=null)
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

		logger.info("Initializing Display Manager");
		DisplayManager.GetInstance();
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
	    for (Object item : GenericWrapperUtils.GetExtensionTables())
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
	    for (Object item : GenericWrapperUtils.GetFunctionSQL())
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
		buffer.append("\n\n-- REMOVE OLD VIEWS FOR DISPLAY DOCS\n\n");

		buffer.append("SELECT removeViews();\n--BR\n");

		buffer.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
	    for (Object item : DisplayManager.GetCreateViewsSQL())
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

	    for (Object item : SQLCreateGenerator.GetSQLCreate(true))
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
		buffer.append("\n\n-- ADDED VIEWS FOR DISPLAY DOCS\n\n");
	    for (Object item : DisplayManager.GetCreateViewsSQL())
	    {
	        buffer.append(item).append("\n--BR\n");
	    }
	    buffer.append("\n-- commit transaction\n");
	    buffer.append("COMMIT;");
		FileUtils.OutputToFile(buffer.toString(),file);

		ViewManager.OutputFieldNames();
		logger.info("File Created: " + file);
	}

	@SuppressWarnings("unused")
	public static List<String> GenerateCreateSQL() throws Exception
	{
        List<String> sql=Lists.newArrayList();

        sql.addAll(SQLCreateGenerator.GetSQLCreate(true));

        sql.addAll(DisplayManager.GetCreateViewsSQL());

        return sql;
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
	 * Returns an instance of the currently supported theme service.
	 * @return An instance of the {@link ThemeService}.
	 */
	public static ThemeService getThemeService() {
		if (_themeService == null) {
			_themeService = getContextService().getBean(ThemeService.class);
		}
		return _themeService;
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

    public static void addScreenTemplatesFolder(String screenTemplatesFolder) {
        _screenTemplatesFolders.add(new File(screenTemplatesFolder));
    }

    public static List<File> getScreenTemplateFolders(){
    	return _screenTemplatesFolders;
    }

    /**
     * Returns the folder containing screen templates. These are installed by custom data types, modules, and other
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
	 * Returns an instance of the currently supported site configuration service.
	 * @return An instance of the {@link SiteConfigurationService} service.
	 */
	private static SiteConfigurationService getSiteConfigurationService() {
	    if (_siteConfigurationService == null || _siteConfigurationService instanceof InitializerSiteConfiguration) {
	    	_siteConfigurationService = getContextService().getBean(SiteConfigurationService.class);
	    }
	    return _siteConfigurationService;
	}

    public static String getSiteConfigurationProperty(String property) throws ConfigServiceException {
		final SiteConfigPreferences preferences = getSiteConfigPreferences();
        if (preferences != null) {
            return preferences.getPreferencesAsProperties().getProperty(property);
        }
        return getSiteConfigurationService().getSiteConfigurationProperty(property);
    }

    public static void setSiteConfigurationProperty(String property, String value) throws ConfigServiceException {
		final UserI userDetails = getUserDetails();
		if (userDetails != null) {
			getSiteConfigurationService().setSiteConfigurationProperty(userDetails.getUsername(), property, value);
		}
	}

    public static Properties getSiteConfiguration() throws ConfigServiceException {
    	return getSiteConfigurationService().getSiteConfiguration();
    }

    /**
	 * Returns an instance of the site configuration preferences bean.
	 * @return An instance of the {@link DataSource} bean.
	 */
	public static DataSource getDataSource() {
	    if (_dataSource == null) {
	    	_dataSource = getContextService().getBean(DataSource.class);
	    }
	    return _dataSource;
	}

    /**
	 * Returns an instance of the site configuration preferences bean.
	 * @return An instance of the {@link SiteConfigPreferences} bean.
	 */
	public static SiteConfigPreferences getSiteConfigPreferences() {
	    if (_siteConfigPreferences == null) {
	    	_siteConfigPreferences = getContextService().getBean(SiteConfigPreferences.class);
	    }
	    return _siteConfigPreferences;
	}

	public static XdatUserAuthService getXdatUserAuthService() {
		if (_xdatUserAuthService == null) {
			_xdatUserAuthService = getContextService().getBean(XdatUserAuthService.class);
		}
		return _xdatUserAuthService;
	}

    /**
     * This verifies that a notification and subscriber exists for the indicated site-wide event. If the notification or
     * subscriber does <i>not</i> exist, one is created using the primary administrative user.
     * @param event    The site-wide event to be verified.
     */
    public static void verifyNotificationType(NotificationType event) {
        final String adminEmail = getSiteConfigPreferences().getAdminEmail();
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

    public static boolean loginUser(RunData data, UserI user, boolean forcePasswordChange) throws Exception{
    	PopulateItem populator = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":user",true);
    	ItemI found = populator.getItem();
    	String tempPass = data.getParameters().getString("xdat:user.primary_password");

		setUserDetails(user);

        HttpSession session = data.getSession();
        session.setAttribute("loggedin",true);
        session.setAttribute("forcePasswordChange",forcePasswordChange);
        session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
        XFTItem item = XFTItem.NewItem("xdat:user_login",user);
        java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
        item.setProperty("xdat:user_login.user_xdat_user_id",user.getID());
        item.setProperty("xdat:user_login.login_date",today);
        item.setProperty("xdat:user_login.ip_address",AccessLogger.GetRequestIp(data.getRequest()));
        item.setProperty("xdat:user_login.session_id", data.getSession().getId());
        SaveItemHelper.authorizedSave(item,null,true,false,(EventMetaI)null);

		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (Roles.isSiteAdmin(user)) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
		Object username = found.getProperty("login");
		if(username==null){
			username = user.getLogin();
		}
    	Authentication authentication = new UsernamePasswordAuthenticationToken(username, tempPass, grantedAuthorities);
    	SecurityContext securityContext = SecurityContextHolder.getContext();
    	securityContext.setAuthentication(authentication);
        return true;
    }

    public static void sendJmsRequest(final Object request) {
        sendJmsRequest(XDAT.getContextService().getBean(JmsTemplate.class), request);
	}

    public static void sendJmsRequest(final JmsTemplate jmsTemplate, final Object request) {
        final String simpleName = request.getClass().getSimpleName();
        final String queue = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        final Destination destination = XDAT.getContextService().getBean(queue, Destination.class);
        jmsTemplate.convertAndSend(destination, request);
	}

    private static synchronized org.nrg.config.entities.Configuration createDefaultWhitelist(UserI user) throws ConfigServiceException {
        String username = user.getUsername();
        String reason = Roles.isSiteAdmin(user) ? "Site admin created default IP whitelist from localhost IP values." : "User hit site before default IP whitelist was constructed.";
        return XDAT.getConfigService().replaceConfig(username, reason, IP_WHITELIST_TOOL, IP_WHITELIST_PATH, Joiner.on("\n").join(getLocalhostIPs()));
}

    public static List<String> getLocalhostIPs() {
        List<String> localhostIPs = new ArrayList<>();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            for (InetAddress address : addresses) {
                String hostAddress = address.getHostAddress();
                if (hostAddress.contains("%")) {
                    hostAddress = hostAddress.substring(0, hostAddress.indexOf("%"));
                }
                localhostIPs.add(hostAddress);
            }
        } catch (UnknownHostException exception) {
            logger.error("Localhost is unknown host... Wha?", exception);
        }
            if (!localhostIPs.contains(IP_LOCALHOST_V4)) {
                localhostIPs.add(IP_LOCALHOST_V4);
            }
            if (!localhostIPs.contains(IP_LOCALHOST_V6)) {
                localhostIPs.add(IP_LOCALHOST_V6);
            }
            return localhostIPs;
    }

    private static final String IP_LOCALHOST_V4 = "127.0.0.1";
    private static final String IP_LOCALHOST_V6 = "0:0:0:0:0:0:0:1";
}
