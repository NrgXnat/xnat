/*
 * core: org.nrg.xdat.XDAT
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.stratum.lifecycle.Configurable;
import org.apache.stratum.lifecycle.Initializable;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.config.entities.Configuration;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.event.EventI;
import org.nrg.framework.exceptions.NrgRuntimeException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.services.ContextService;
import org.nrg.framework.services.SerializerService;
import org.nrg.mail.api.NotificationType;
import org.nrg.mail.services.MailService;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.api.SubscriberType;
import org.nrg.notify.entities.*;
import org.nrg.notify.exceptions.DuplicateSubscriberException;
import org.nrg.notify.services.NotificationService;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.turbine.modules.actions.XDATLoginUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.generators.SQLUpdateGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.cache.CacheManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nullable;
import javax.jms.Destination;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.nrg.xdat.security.helpers.Users.*;

/**
 * @author Tim
 */
// TODO: Remove all @SuppressWarnings() annotations.
@SuppressWarnings({"unused", "WeakerAccess"})
@Slf4j
public class XDAT implements Initializable, Configurable{
	public static final String IP_WHITELIST_TOOL               = "ipWhitelist";
	public static final String IP_WHITELIST_PATH               = "/system/ipWhitelist";
	public static final String ADMIN_USERNAME_FOR_SUBSCRIPTION = "ADMIN_USER";

	private static final String ELEMENT_NOT_FOUND_MESSAGE = "Element not found: {}. The data type may not be configured or may be missing. Check the xdat_element_security table for invalid entries or data types that should be installed or re-installed.";

	private static ContextService             _contextService;
	private static DataSource                 _dataSource;
	private static NamedParameterJdbcTemplate _namedParameterJdbcTemplate;
	private static JdbcTemplate               _jdbcTemplate;
	private static MailService                _mailService;
	private static ThemeService               _themeService;
	private static NotificationService        _notificationService;
	private static XdatUserAuthService        _xdatUserAuthService;
	private static ConfigService              _configurationService;
	private static SerializerService          _serializerService;
	private static DataTypeAwareEventService  _eventService;
	private static SiteConfigPreferences      _siteConfigPreferences;
	private static CacheManager               _cacheManager;
	private static NotificationsPreferences   _notificationsPreferences;
	private static File                       _screenTemplatesFolder;

	private static String            _configFilesLocation    = null;
	private static Map<String, File> _screenTemplatesFolders = new HashMap<>();

	private String instanceSettingsLocation = null;

    /**
	 * configure torque
	 *
	 * @param configuration Configuration
	 * @see org.apache.stratum.lifecycle.Configurable
	 */
	@Override
	public void configure(org.apache.commons.configuration.Configuration configuration) 	{
		instanceSettingsLocation = configuration.getString("instance_settings_directory");
	}

	/**
	 * initialize Torque
	 *
	 * @see org.apache.stratum.lifecycle.Initializable
	 */
	@Override
	public void initialize()
	{
		try {
			log.info("Starting Service XDAT");
			init(instanceSettingsLocation);
		} catch (Exception exception) {
			log.error("An error occurred trying to start the XDAT service", exception);
		}
	}

	public static Map<String, Long> getTotalCounts() {
		final GroupsAndPermissionsCache cache = getContextService().getBeanSafely(GroupsAndPermissionsCache.class);
		return cache != null ? cache.getTotalCounts(): Collections.<String, Long>emptyMap();
	}

    public static List<String> getWhitelistedIPs(UserI user) throws ConfigServiceException {
        return Arrays.asList(getWhitelistConfiguration(user).split("[\\s]+"));
    }

    public static String getWhitelistConfiguration(UserI user) throws ConfigServiceException {
        final Configuration whitelist = XDAT.getConfigService().getConfig(IP_WHITELIST_TOOL, IP_WHITELIST_PATH);
        if (whitelist != null && StringUtils.isNotBlank(whitelist.getContents())) {
			return whitelist.getContents();
        }
        return createDefaultWhitelist(user).getContents();
    }

	public static String getSiteConfigurationProperty(final String property) throws ConfigServiceException {
    	return getSiteConfigurationProperty(property, null);
	}

	@SuppressWarnings("RedundantThrows")
	public static String getSiteConfigurationProperty(final String property, final String defaultValue) throws ConfigServiceException {
		final SiteConfigPreferences preferences = getSiteConfigPreferences();
		final String value = preferences.getValue(property);
		return StringUtils.defaultIfBlank(value, defaultValue);
	}

	public static boolean getBoolSiteConfigurationProperty(final String property, final boolean defaultValue) {
		try {
			return BooleanUtils.toBoolean(getSiteConfigurationProperty(property, BooleanUtils.toStringTrueFalse(defaultValue)));
		} catch (ConfigServiceException e) {
			return defaultValue;
		}
	}

	public static String safeSiteConfigProperty(final String property, final String defaultValue) {
		try {
			return getSiteConfigurationProperty(property, defaultValue);
		} catch (Throwable e) {
			return defaultValue;
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
		final boolean isAnonymous = authentication == null || authentication instanceof AnonymousAuthenticationToken;

		final Object principal;
		try {
			if (isAnonymous) {
				log.debug("Attempted to retrieve user object, but found {}. Returning guest user.", authentication == null ? "no stored authentication object" : "an anonymous auth token");
				return Users.getGuest();
			}

			principal = authentication.getPrincipal();

			try {
                if (principal == null) {
					log.debug("Attempted to retrieve user object and found an authentication object of type {}, but it had no associated principal", authentication.getClass());
                    return null;
                }

                if (principal instanceof UserI) {
					log.debug("Found authenticated user object for user {}", ((UserI) principal).getLogin());
                    return (UserI) principal;
                }

                if (principal instanceof String) {
                    if (StringUtils.isBlank((String) principal)) {
						log.debug("Found principal object of type String but it was empty.");
                        return null;
                    }
                    final UserI user = Users.getUser((String) principal);
					log.debug("Found principal object of type String and successfully retrieved the user: {}", principal);
                    return user;
                }
            } catch (UserInitException e) {
                throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the user with login: " + principal, e);
            } catch (UserNotFoundException e) {
				log.debug("Found principal object of type String with value \"{}\", but couldn't find the corresponding user object.", principal);
                return null;
            }
		} catch (UserNotFoundException e) {
			throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Something super weird happened: I can't find the guest user!", e);
		} catch (UserInitException e) {
			throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "An error occurred trying to retrieve the guest user", e);
		}

		log.warn("This is weird. Found principal object of type {}. I don't really know what to do with this. Its value was: {}", principal.getClass(), principal);
		return null;
	}

	public static Authentication setGuestUserDetails() throws UserNotFoundException, UserInitException {
		return new AnonymousAuthenticationToken(Users.ANONYMOUS_AUTH_PROVIDER_KEY, Users.getGuest(), AUTHORITIES_ANONYMOUS);
	}

	public static Authentication setUserDetails(UserI user) throws UserNotFoundException, UserInitException {
		if (user.isGuest()) {
			return setGuestUserDetails();
		}

		final Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>(Roles.isSiteAdmin(user) ? AUTHORITIES_ADMIN : AUTHORITIES_USER));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		return authentication;
	}

	public static void setNewUserDetails(final UserI user, final RunData data, final Context context) {
		try {
			XDAT.setUserDetails(user);
			Users.recordUserLogin(user, data.getRequest());
			AccessLogger.LogActionAccess(data, "Valid Login:" + user.getUsername());
		} catch (Exception exception) {
            log.error("Error performing su operation to user {}", user.getUsername(), exception);
		}
		try {
            new XDATLoginUser().doRedirect(data, context, user);
		} catch (Exception exception) {
			log.error("Error performing su redirect for user {}", user.getUsername(), exception);
		}
	}

	public static String getSiteLogoPath() {
        try {
            return getSiteConfigurationProperty("siteLogoPath", "");
        } catch (ConfigServiceException e) {
            log.error("An error occurred trying to retrieve the site logo path setting, using the default", e);
            return "/images/logo.png";
        }
    }

	public static void RefreshDisplay()
	{
		DisplayManager.clean();
		DisplayManager.GetInstance();
	}

	/**
	 * Initializes XDAT from the specified location. This calls the {@link #init(String, boolean, boolean)} version of
	 * this method, passing <b>true</b> for the <b>allowDBAccess</b> parameter and <b>false</b> for the
	 * <b>overrideConfigFilesLocation</b> parameter.
	 *
	 * @param location                    The location to search for XDAT configuration files.
	 *
	 * @throws Exception When an error occurs initializing XDAT.
	 */
	public static void init(String location) throws Exception
	{
		XDAT.init(location, true, false);
	}

	/**
	 * Initializes XDAT from the specified location. This calls the {@link #init(String, boolean, boolean)} version of
	 * this method, passing <b>false</b> for the <b>overrideConfigFilesLocation</b> parameter.
	 *
	 * @param location                    The location to search for XDAT configuration files.
	 * @param allowDBAccess               Indicates whether XDAT should try to check for cached configurations in the
	 *                                    database.
	 *
	 * @throws Exception When an error occurs initializing XDAT.
	 */
	public static void init(String location,boolean allowDBAccess) throws Exception {
		init(location, allowDBAccess, false);
	}

	/**
	 * Initializes XDAT from the specified location.
	 *
	 * @param location                    The location to search for XDAT configuration files.
	 * @param allowDBAccess               Indicates whether XDAT should try to check for cached configurations in the
	 *                                    database.
	 * @param overrideConfigFilesLocation Indicates whether XDAT should used the cached configuration file location if
	 *                                    available.
	 *
	 * @throws Exception When an error occurs initializing XDAT.
	 */
	public static void init(String location, boolean allowDBAccess, boolean overrideConfigFilesLocation) throws Exception {
		DisplayManager.clean();
        if (StringUtils.isBlank(_configFilesLocation)) {
            _configFilesLocation = FileUtils.AppendSlash(location);
        }

		XFT.init(overrideConfigFilesLocation ? FileUtils.AppendSlash(location) : _configFilesLocation);

		if (allowDBAccess && hasUsers()) {
			try {
				for (final ElementSecurity security : ElementSecurity.GetQuarantinedElements()) {
					try {
						final GenericWrapperElement element = GenericWrapperElement.GetElement(security.getElementName());
						if (element != null) {
							element.setQuarantineSetting(security.getBooleanProperty(ViewManager.QUARANTINE, false));
						} else {
							throw new ElementNotFoundException(security.getElementName());
						}
					} catch (ElementNotFoundException e) {
						log.error(ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
					}
				}

				for (final ElementSecurity security : ElementSecurity.GetPreLoadElements()) {
					try {
						final GenericWrapperElement element = GenericWrapperElement.GetElement(security.getElementName());
						if (element != null) {
							element.setPreLoad(security.getBooleanProperty("pre_load", false));
						} else {
							throw new ElementNotFoundException(security.getElementName());
						}
					} catch (ElementNotFoundException e) {
						log.error(ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
					}
				}
			} catch (Exception e) {
				log.error("An error occurred while initializing XDAT", e);
			}
		}

		log.info("Initializing Display Manager");
		DisplayManager.GetInstance(location);
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

		log.info("File Created: {}", file);
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
		log.info("File Created: {}", file);
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
	 * Returns an instance of the cache manager.
	 * @return An instance of the {@link CacheManager} service.
	 */
	public static CacheManager getCacheManager() {
	    if (_cacheManager == null) {
	        _cacheManager = getContextService().getBean(CacheManager.class);
	    }
	    return _cacheManager;
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

	public static void triggerEvent(final EventI event) {
		getEventService().triggerEvent(event);
	}

	public static void triggerEvent(final String description, final EventI event, final boolean notifyClassListeners) {
		getEventService().triggerEvent(description, event, notifyClassListeners);
	}

	public static void triggerEvent(final String description, final EventI event) {
		getEventService().triggerEvent(description, event);
	}

	public static void triggerEvent(final EventI event, final Object replyTo) {
		getEventService().triggerEvent(event, replyTo);
	}

	public static void triggerXftItemEvent(final String xsiType, final String id, final String action) {
		getEventService().triggerXftItemEvent(xsiType, id, action);
	}

	public static void triggerXftItemEvent(final XFTItem item, final String action) {
		getEventService().triggerXftItemEvent(item, action);
	}

	public static void triggerXftItemEvent(final BaseElement baseElement, final String action) {
		getEventService().triggerXftItemEvent(baseElement, action);
	}

	public static void triggerXftItemEvent(final BaseElement[] baseElements, final String action) {
		getEventService().triggerXftItemEvent(baseElements, action);
	}

	public static void triggerXftItemEvent(final String xsiType, final String id, final String action, final Map<String, ?> properties) {
		getEventService().triggerXftItemEvent(xsiType, id, action, properties);
	}

	public static void triggerUserIEvent(final String username, final String action, final Map<String, ?> properties) {
		getEventService().triggerUserIEvent(username, action, properties);
	}

	public static void triggerXftItemEvent(final XFTItem item, final String action, final Map<String, ?> properties) {
		getEventService().triggerXftItemEvent(item, action, properties);
	}

	public static void triggerXftItemEvent(final BaseElement baseElement, final String action, final Map<String, ?> properties) {
		getEventService().triggerXftItemEvent(baseElement, action, properties);
	}

	public static void addScreenTemplatesFolder(final String path, final File screenTemplatesFolder) {
        _screenTemplatesFolders.put(path, screenTemplatesFolder);
    }

    public static List<File> getScreenTemplateFolders(){
    	return new ArrayList<>(_screenTemplatesFolders.values());
    }

    public static File getScreenTemplateFolder(final String folder) {
    	return _screenTemplatesFolders.get(folder);
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

    public static File getScreenTemplatesSubfolder(final String subfolder) {
        if (StringUtils.isBlank(subfolder)) {
            return new File(getScreenTemplatesFolder());
        }

        File current = new File(getScreenTemplatesFolder(), "");

        final String[] subfolders = subfolder.split("/");
        for (final String folder : subfolders) {
            current = new File(current, folder);
            if (!current.exists()) {
                // This is actually OK, it just means there are no overrides, so return null.
                return null;
            }
            if (!current.isDirectory()) {
                log.error("The path indicated by {} isn't a folder.", current.getAbsolutePath(), new NrgRuntimeException());
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
	 * Returns an instance of the serializer service.
	 * @return An instance of the {@link SerializerService} service.
	 */
	public static SerializerService getSerializerService() {
		if (_serializerService == null) {
			final SerializerService service = getContextService().getBean(SerializerService.class);
			if (service != null) {
				_serializerService = service;
			} else {
				final Map<String, String> appInfo = Maps.filterKeys(System.getenv(), new Predicate<String>() {
					@Override
					public boolean apply(@Nullable final String key) {
						return StringUtils.startsWithAny(key, "APP_NAME_", "JAVA_MAIN_CLASS_", "JOB_NAME", "BUILD_DISPLAY_NAME", "BUILD_ID", "BUILD_NUMBER", "BUILD_TAG", "BUILD_URL");
					}
				});
				// We're running an application of some sort, not in a web application, so we need to
				// strap on the serializer service by hand.
				if (!appInfo.isEmpty()) {
					log.info("Found environment variables that indicate this is running in a stand-alone application:");
					for (final String key : appInfo.keySet()) {
						log.info(" * \"{}\": {}", key, appInfo.get(key));
					}
					log.info("Because this is a stand-alone application, I will instantiate SerializerService on its own.");
				} else {
					log.warn("The serializer service was null, which usually happens only in a non-XNAT application context, but I didn't find anything in the environment to indicate the non-XNAT context: {}", System.getenv());
				}
				final SerializerConfig config = new SerializerConfig();
				try {
					_serializerService = new SerializerService(config.objectMapperBuilder(), config.documentBuilderFactory(), config.saxParserFactory(), config.transformerFactory(), config.saxTransformerFactory());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return _serializerService;
	}

	public static DataTypeAwareEventService getEventService() {
		if (_eventService == null) {
			_eventService = XDAT.getContextService().getBean(DataTypeAwareEventService.class);
		}
		return _eventService;
	}

	@SuppressWarnings("RedundantThrows")
	public static Properties getSiteConfiguration() throws ConfigServiceException {
		final SiteConfigPreferences preferences = getSiteConfigPreferences();
		if (preferences == null) {
			throw new NrgServiceRuntimeException(NrgServiceError.Uninitialized, "The site configuration preferences aren't available for some reason.");
		}
		return preferences.asProperties();
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
	 * Returns an instance of the shared NamedParameterJdbcTemplate bean.
	 *
	 * @return An instance of the {@link NamedParameterJdbcTemplate} bean.
	 */
	public static NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		if (_namedParameterJdbcTemplate == null) {
			_namedParameterJdbcTemplate = getContextService().getBean(NamedParameterJdbcTemplate.class);
		}
		return _namedParameterJdbcTemplate;
	}

	/**
	 * Returns an instance of the shared JdbcTemplate bean.
	 *
	 * @return An instance of the {@link NamedParameterJdbcTemplate} bean.
	 */
	public static JdbcTemplate getJdbcTemplate() {
		if (_jdbcTemplate == null) {
			_jdbcTemplate = getContextService().getBean(JdbcTemplate.class);
		}
		return _jdbcTemplate;
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

	/**
	 * Returns an instance of the notifications preferences bean.
	 * @return An instance of the {@link NotificationsPreferences} bean.
	 */
	public static NotificationsPreferences getNotificationsPreferences() {
		if (_notificationsPreferences == null) {
			_notificationsPreferences = getContextService().getBean(NotificationsPreferences.class);
		}
		return _notificationsPreferences;
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
    public static void verifyNotificationType(final NotificationType event) {
		final String  adminEmail = getSiteConfigPreferences().getAdminEmail();
		final Channel channel    = getHtmlMailChannel();

        final Definition definition = getOrCreateDefinition(event);

		final List<Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);
		if (subscriptions != null && !subscriptions.isEmpty()) {
			// There are subscribers! Our work here is done.
			return;
		}

		// If we made it this far, there are no subscribers to the indicated site-wide event, so create a subscriber and
        // set it to the system administrator.
		final Subscriber subscriber = getOrCreateSubscriber(adminEmail);
        assert subscriber != null : "Unable to create or retrieve subscriber for the admin user";

        // We have an event and subscriber, let's bring them together.
        getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
    }

	private static Definition getOrCreateDefinition(final NotificationType event) {
		final Category category = getOrCreateCategory(event);

		final List<Definition> definitions = getNotificationService().getDefinitionService().getDefinitionsForCategory(category);
		if (definitions != null && !definitions.isEmpty()) {
			return definitions.get(0);
		}

		final Definition definition = getNotificationService().getDefinitionService().newEntity();
		definition.setCategory(category);
		getNotificationService().getDefinitionService().create(definition);
		return definition;
	}

	private static Category getOrCreateCategory(final NotificationType event) {
		final Category found = getNotificationService().getCategoryService().getCategoryByScopeAndEvent(CategoryScope.Site, event.toString());
		if (found != null) {
			return found;
		}
		final Category category = getNotificationService().getCategoryService().newEntity();
		category.setScope(CategoryScope.Site);
		category.setEvent(event.toString());
		getNotificationService().getCategoryService().create(category);
		return category;
	}

	private static Subscriber getOrCreateSubscriber(final String adminEmail) {
		final Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(ADMIN_USERNAME_FOR_SUBSCRIPTION);
		if (subscriber != null) {
			return subscriber;
		}
		try {
			return getNotificationService().getSubscriberService().createSubscriber(ADMIN_USERNAME_FOR_SUBSCRIPTION, adminEmail);
		} catch (DuplicateSubscriberException exception) {
			// This shouldn't happen, since we just checked for the subscriber's existence.
			return null;
		}
	}
	/**
	 * This returns a string containing a comma-separated list of all the email addresses subscribing to the given event.
	 * @param event    The site-wide event to find subscribers for.
	 */
	public static String getSubscriberEmailsListAsString(NotificationType event) {
		final Channel channel = getHtmlMailChannel();
		boolean created = false;

		final Definition definition = getOrCreateDefinition(event);

		final List<Subscription> subscriptions = getNotificationService().getSubscriptionService().getSubscriptionsForDefinition(definition);
		final Set<String> emails = new HashSet<>();
		if (subscriptions != null && !subscriptions.isEmpty()) {
			// There are subscribers! Return them.
			for(final Subscription subscription : subscriptions){
				emails.addAll(subscription.getSubscriber().getEmailList());
			}
		}

		return StringUtils.join(emails, ", ");
	}

	/**
	 * This replaces the old list of subscribers to this event with the supplies list of emails.
	 * @param newSubscriberEmails  	A String containing a comma-separated list of all the email addresses to be subscribed to the event.
     * @param event    				The site-wide event to replace subscribers for.
	 */
	public static void replaceSubscriberList(String newSubscriberEmails, NotificationType event, boolean allowNonUserSubscribers) {
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
				for(Subscription sub : subscriptions) {
					getNotificationService().getSubscriptionService().delete(sub);
				}
			}
		}

		// If we made it this far, there are no subscribers to the indicated site-wide event, so create the requested subscribers
		final List<String> newSubscriberEmailList = StringUtils.isBlank(newSubscriberEmails) ? new ArrayList<String>() : Arrays.asList(newSubscriberEmails.split("[\\s]*,[\\s]*"));
		for(String newSubscriberEmailString : newSubscriberEmailList){
			List<? extends UserI> users = Users.getUsersByEmail(newSubscriberEmailString);
			if(users!=null && users.size()>0){
				for(UserI user : users){
					Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(user.getUsername());
					if (subscriber == null) {
						try {
							subscriber = getNotificationService().getSubscriberService().createSubscriber(user.getUsername(), user.getEmail());
						} catch (DuplicateSubscriberException exception) {
							// This shouldn't happen, since we just checked for the subscriber's existence.
						}
					}
					assert subscriber != null : "Unable to create or retrieve subscriber for user "+user.getUsername();

					// We have an event and subscriber, let's bring them together.
					getNotificationService().subscribe(subscriber, SubscriberType.User, definition, channel);
				}
			}
			else if(allowNonUserSubscribers){
				Subscriber subscriber = getNotificationService().getSubscriberService().getSubscriberByName(newSubscriberEmailString);
				if (subscriber == null) {
					try {
						subscriber = getNotificationService().getSubscriberService().createSubscriber(newSubscriberEmailString, newSubscriberEmailString);
					} catch (DuplicateSubscriberException exception) {
						// This shouldn't happen, since we just checked for the subscriber's existence.
					}
				}
				assert subscriber != null : "Unable to create or retrieve subscriber for email "+newSubscriberEmailString;

				// We have an event and subscriber, let's bring them together.
				getNotificationService().subscribe(subscriber, SubscriberType.NonUser, definition, channel);

			}
		}
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

	public static void loginUser(final RunData data, final UserI user, final boolean forcePasswordChange) throws Exception {
		final PopulateItem populator = PopulateItem.Populate(data, XFT.PREFIX + ":user", true);
		final ItemI        found     = populator.getItem();

		data.getSession().setAttribute("forcePasswordChange", forcePasswordChange);

		loginUser(user, data.getRequest(), data.getParameters().getString("xdat:user.primary_password"));
	}

	public static void loginUser(final UserI user, final HttpServletRequest request) throws Exception {
		loginUser(user, request, "");
	}

	public static void loginUser(final UserI user, final HttpServletRequest request, final String password) throws Exception {
		UserHelper.setUserHelper(request, user);
		Users.recordUserLogin(user, request);
		final Authentication authentication = new UsernamePasswordAuthenticationToken(user, password, getGrantedAuthorities(user));
		if (!user.isGuest()) {
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
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

	private static boolean hasUsers() {
		try {
			return (new DatabaseHelper(XDAT.getContextService().getBean(DataSource.class))).tableExists("xdat_user");
		} catch (Throwable e) {
			// xdat_user table doesn't exist
			return false;
		}
	}

	private static synchronized Configuration createDefaultWhitelist(final UserI user) throws ConfigServiceException {
		final String username = user.getUsername();
		final String reason   = Roles.isSiteAdmin(user) ? "Site admin created default IP whitelist from localhost IP values." : "User hit site before default IP whitelist was constructed.";
		return XDAT.getConfigService().replaceConfig(username, reason, IP_WHITELIST_TOOL, IP_WHITELIST_PATH, Joiner.on("\n").join(getLocalhostIPs()));
	}

	public static Set<String> getLocalhostIPs() {
		final Set<String> localhostIPs = new HashSet<>(LOCALHOST_IPS);
		final String      siteUrl      = XDAT.getSiteConfigPreferences().getSiteUrl();
		try {
			final InetAddress[] addresses = ArrayUtils.addAll(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()), InetAddress.getAllByName(new URI(siteUrl).getHost()));
			for (final InetAddress address : addresses) {
				final String hostAddress = address.getHostAddress();
				localhostIPs.add(hostAddress.contains("%") ? hostAddress.substring(0, hostAddress.indexOf("%")) : hostAddress);
			}
		} catch (URISyntaxException e) {
			log.error("The configured site URL {} is an invalid URL.", siteUrl, e);
		} catch (UnknownHostException exception) {
			log.error("Localhost is unknown host... Wha?", exception);
		}
		return localhostIPs;
	}

	public static void logShortStackTrace(final String message, final int depth) {
		logShortStackTrace(log, message, null, depth);
	}

	@SuppressWarnings("SameParameterValue")
	public static void logShortStackTrace(final String message, final Map<String, ?> properties, final int depth) {
        logShortStackTrace(log, message, properties, depth);
    }

	public static void logShortStackTrace(final Logger logger, final String message, final Map<String, ?> properties, final int depth) {
        final StringBuilder buffer = new StringBuilder(message).append("\n");
        if (properties != null) {
            buffer.append("Properties: ").append(properties.toString()).append("\n");
        }
        buffer.append(formatShortStackTrace(Thread.currentThread().getStackTrace(), depth));
        log.error(buffer.toString());
	}

	public static String formatShortStackTrace(final StackTraceElement[] stackTrace, final int depth) {
		final AtomicInteger       start      = new AtomicInteger(0);
		for (final StackTraceElement element : stackTrace) {
			if (StringUtils.equalsAny(element.getMethodName(), "logShortStackTrace", "formatShortStackTrace")) {
				start.incrementAndGet();
				continue;
			}
			start.incrementAndGet();
			break;
		}
		final StringBuilder buffer = new StringBuilder();
		for (final StackTraceElement element : ArrayUtils.subarray(stackTrace, start.get(), start.getAndAdd(depth))) {
			buffer.append("    at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("():").append(element.getLineNumber()).append("\n");
		}
		return buffer.toString();
	}

	public static String formatShortStackTrace(final StackTraceElement[] stackTrace, final String... regExes) {
        final List<Pattern> patterns = Lists.transform(Arrays.asList(regExes), new Function<String, Pattern>() {
            @Override
            public Pattern apply(final String include) {
                return Pattern.compile(include);
            }
        });
        return formatShortStackTrace(stackTrace, new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(final StackTraceElement element) {
                if (StringUtils.equalsAny(element.getMethodName(), "logShortStackTrace", "formatShortStackTrace")) {
                    return false;
                }
                final String fullyQualifiedMethodName = element.getClassName() + "." + element.getMethodName() + "()";
                return Iterables.any(patterns, new Predicate<Pattern>() {
                    @Override
                    public boolean apply(final Pattern pattern) {
                        return pattern.matcher(fullyQualifiedMethodName).matches();
                    }
                });
            }
        });
    }

    /**
     * Convenience method that returns a formatted string containing only elements from classes under the
     * "org.nrg..." package structure.
     *
     * @param stackTrace The stack trace to filter and format.
     *
     * @return A string containing the formatted stack trace elements from classes under the "org.nrg..." package structure.
     */
    public static String formatNrgShortStackTrace(final StackTraceElement[] stackTrace) {
        return formatShortStackTrace(stackTrace, new Predicate<StackTraceElement>() {
            @Override
            public boolean apply(final StackTraceElement element) {
                return StringUtils.startsWith(element.getClassName(), "org.nrg.");
            }
        });
    }

    /**
     * Returns a formatted string with the lines from the submitted stack trace. The predicate parameter is applied to determine
     * whether a particular stack trace line should be included or excluded from the output.
     *
     * @param stackTrace The stack trace to filter and format.
     * @param predicate  The predicate to be applied to each element to determine whether or not it should be included in the output.
     *
     * @return A string containing the formatted stack trace elements that met the predicate criteria.
     */
	public static String formatShortStackTrace(final StackTraceElement[] stackTrace, final Predicate<StackTraceElement> predicate) {
        return StringUtils.join(Lists.transform(Lists.newArrayList(Iterables.filter(Arrays.asList(stackTrace), predicate)), new Function<StackTraceElement, String>() {
            @Override
            public String apply(final StackTraceElement element) {
                return "    at " + element.getClassName() + "." + element.getMethodName() + "():" + element.getLineNumber();
            }
        }), "\n");
	}

	private static final List<String> LOCALHOST_IPS = Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1");
}
