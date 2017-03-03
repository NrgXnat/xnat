/*
 * web: org.nrg.xnat.services.XnatAppInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

@Component
public class XnatAppInfo {
    public static final String NON_RELEASE_VERSION_REGEX  = "(?i:^.*(SNAPSHOT|BETA|RC).*$)";
    public static final String XNAT_PRIMARY_MODE_PROPERTY = "xnat.is_primary_node";

    @Inject
    public XnatAppInfo(final SiteConfigPreferences preferences, final ServletContext context, final Environment environment, final SerializerService serializerService, final JdbcTemplate template, final PluginOpenUrlsPreference openUrlsPref) throws IOException {
        _preferences = preferences;
        _template = template;
        _environment = environment;
        _openUrlsPref = openUrlsPref;
        _serializerService = serializerService;
        _primaryNode = Boolean.parseBoolean(_environment.getProperty(XNAT_PRIMARY_MODE_PROPERTY, "true"));

        final Resource configuredUrls = RESOURCE_LOADER.getResource("classpath:META-INF/xnat/security/configured-urls.yaml");
        try (final InputStream inputStream = configuredUrls.getInputStream()) {
            final JsonNode paths = serializerService.deserializeYaml(inputStream);

            _setupPath = paths.get("setupPath").asText();
            _setupPathPatterns = Arrays.asList(asAntPattern(_setupPath), asAntPattern(_setupPath + "/"));
            _nonAdminErrorPath = paths.get("nonAdminErrorPath").asText();
            _nonAdminErrorPathPatterns = Collections.singletonList(asAntPattern(_nonAdminErrorPath));

            _initUrls.addAll(asAntPatterns(nodeToList(paths.get("initUrls"))));
            _openUrls.addAll(asAntPatterns(nodeToList(paths.get("openUrls"))));
            _openUrls.addAll(openUrlsPref.getAllowedPluginOpenUrls());
            _adminUrls.addAll(asAntPatterns(nodeToList(paths.get("adminUrls"))));
            _adminUrls.addAll(getPluginAdminUrls());
        }

        try (final InputStream input = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            if (input != null) {
                final Manifest manifest = new Manifest(input);
                final Attributes attributes = manifest.getMainAttributes();
                _properties.setProperty("buildNumber", attributes.getValue("Build-Number"));
                _properties.setProperty("buildDate", attributes.getValue("Build-Date"));
                _properties.setProperty("version", attributes.getValue("Implementation-Version"));
                _properties.setProperty("commit", attributes.getValue("Implementation-Sha"));
                if (_log.isDebugEnabled()) {
                    _log.debug("Initialized application build information:\n * Version: {}\n * Build number: {}\n * Build Date: {}\n * Commit: {}",
                               _properties.getProperty("version"),
                               _properties.getProperty("buildNumber"),
                               _properties.getProperty("buildDate"),
                               _properties.getProperty("commit"));
                }
                for (final Object key : attributes.keySet()) {
                    final String name = key.toString();
                    if (!PRIMARY_MANIFEST_ATTRIBUTES.contains(name)) {
                        _properties.setProperty(name, attributes.getValue(name));
                    }
                }
                final Map<String, Attributes> entries = manifest.getEntries();
                for (final String key : entries.keySet()) {
                    final Map<String, String> keyedAttributes = new HashMap<>();
                    _attributes.put(key, keyedAttributes);
                    final Attributes entry = entries.get(key);
                    for (final Object subkey : entry.keySet()) {
                        final String property = (String) subkey;
                        keyedAttributes.put(property, attributes.getValue(property));
                    }
                }
            } else {
                _log.warn("Attempted to load /META-INF/MANIFEST.MF but couldn't find it, all version information is unknown.");
                _properties.setProperty("buildNumber", "Unknown");
                _properties.setProperty("buildDate", FORMATTER.format(new Date()));
                _properties.setProperty("version", "Unknown");
                _properties.setProperty("commit", "Unknown");
                if (_log.isDebugEnabled()) {
                    _log.debug("Initialized application build information:\n * Version: {}\n * Build number: {}\n * Build Date: {}\n * Commit: {}",
                               _properties.getProperty("version"),
                               _properties.getProperty("buildNumber"),
                               _properties.getProperty("buildDate"),
                               _properties.getProperty("commit"));

                }
            }
            if (!isInitialized()) {
                try {
                    final int count = _template.queryForObject("select count(*) from arc_archivespecification", Integer.class);
                    if (count > 0) {
                        // Migrate to preferences map.
                        _template.query("select arc_archivespecification.site_id, arc_archivespecification.site_admin_email, arc_archivespecification.site_url, arc_archivespecification.smtp_host, arc_archivespecification.require_login, arc_archivespecification.enable_new_registrations, arc_archivespecification.enable_csrf_token, arc_pathinfo.archivepath, arc_pathinfo.prearchivepath, arc_pathinfo.cachepath, arc_pathinfo.buildpath, arc_pathinfo.ftppath, arc_pathinfo.pipelinepath from arc_archivespecification LEFT JOIN arc_pathinfo ON arc_archivespecification.globalpaths_arc_pathinfo_id=arc_pathinfo.arc_pathinfo_id", new RowMapper<Object>() {
                            @Override
                            public Object mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
                                addFoundStringPreference(resultSet, "siteId", "site_id");
                                addFoundStringPreference(resultSet, "adminEmail", "site_admin_email");
                                addFoundStringPreference(resultSet, "siteUrl", "site_url");
                                addFoundStringPreference(resultSet, "smtp_host", "smtp_host");
                                addFoundStringPreference(resultSet, "archivePath", "archivepath");
                                addFoundStringPreference(resultSet, "prearchivePath", "prearchivepath");
                                addFoundStringPreference(resultSet, "cachePath", "cachepath");
                                addFoundStringPreference(resultSet, "buildPath", "buildpath");
                                addFoundStringPreference(resultSet, "ftpPath", "ftppath");
                                addFoundStringPreference(resultSet, "pipelinePath", "pipelinepath");
                                addFoundBooleanPreference(resultSet, "requireLogin", "require_login");
                                addFoundBooleanPreference(resultSet, "userRegistration", "enable_new_registrations");
                                addFoundBooleanPreference(resultSet, "enableCsrfToken", "enable_csrf_token");
                                return _foundPreferences;
                            }

                            private void addFoundBooleanPreference(final ResultSet resultSet, final String preference, final String column) throws SQLException {
                                // Get the value for the column.
                                final String value = resultSet.getString(column);

                                // If there was no value, ignore this one, but if there was...
                                if (StringUtils.isNotBlank(value)) {
                                    // Translate from int or string to boolean
                                    final String translatedValue = translateToBoolean(value);
                                    // translateToBoolean returns either "true", "false", or null, no need for empty string check.
                                    if (translatedValue != null) {
                                        _foundPreferences.put(preference, translatedValue);
                                    }
                                }
                            }

                            private void addFoundStringPreference(final ResultSet resultSet, final String preference, final String column) throws SQLException {
                                final String value = resultSet.getString(column);
                                if (value != null) {
                                    _foundPreferences.put(preference, value);
                                }
                            }
                        });
                    }
                } catch (DataAccessException e) {
                    _log.info("Nothing to migrate");
                }
            }
        }
    }

    public void updateOpenUrlList() {
        /*
         * NOTE:  Currently there is no reason to call this method.  The open URL list is not checked for every REST call,
		 * so Tomcat restarts are still required for changes to the openUrl list to take effect.  Leaving this method defined
		 * for documentation of the Tomcat restart requirement, and in case further changes are made that would allow 
		 * these changes to take effect without restart.
		 */
        final Resource configuredUrls = RESOURCE_LOADER.getResource("classpath:META-INF/xnat/security/configured-urls.yaml");
        _openUrls.clear();
        try (final InputStream inputStream = configuredUrls.getInputStream()) {
            final JsonNode paths = _serializerService.deserializeYaml(inputStream);
            _openUrls.addAll(asAntPatterns(nodeToList(paths.get("openUrls"))));
            _openUrls.addAll(_openUrlsPref.getAllowedPluginOpenUrls());
        } catch (IOException e) {
            _log.debug("Could not update open URL list", e);
        }

    }

    /**
     * Gets the plugin admin urls.
     *
     * @return the plugin admin urls
     */
    private List<? extends String> getPluginAdminUrls() {
        return _openUrlsPref.getUrlList(XnatPlugin.PLUGIN_ADMIN_URLS);
    }

    public Map<String, String> getFoundPreferences() {
        if (_foundPreferences.size() == 0) {
            return null;
        }

        return new HashMap<>(_foundPreferences);
    }

    /**
     * Indicates whether the XNAT system has been initialized yet.
     *
     * @return Returns true if the system has been initialized, false otherwise.
     */
    public boolean isInitialized() {
        // If it's not initialized...
        if (!_initialized) {
            // Recheck to see if it has been initialized. We don't need to recheck to see if it's been
            // uninitialized because that's silly.
            //noinspection SqlDialectInspection,SqlNoDataSourceInspection
            try {
                _initialized = _template.queryForObject("select value from xhbm_preference p, xhbm_tool t where t.tool_id = 'siteConfig' and p.tool = t.id and p.name = 'initialized';", Boolean.class);
                if (_initialized) {
                    if (_log.isInfoEnabled()) {
                        _log.info("The site was not flagged as initialized, but found initialized preference set to true. Flagging as initialized.");
                    }
                } else {
                    if (_log.isInfoEnabled()) {
                        _log.info("The site was not flagged as initialized and initialized preference set to false. Setting system for initialization.");
                    }
                    for (final String preference : _foundPreferences.keySet()) {
                        if (_foundPreferences.get(preference) != null) {
                            _template.update(
                                    "UPDATE xhbm_preference SET value = ? WHERE name = ?",
                                    new Object[]{_foundPreferences.get(preference), preference}, new int[]{Types.VARCHAR, Types.VARCHAR}
                                            );
                            try {
                                _preferences.set(_foundPreferences.get(preference), preference);
                            } catch (InvalidPreferenceName e) {
                                _log.error("", e);
                            } catch (NullPointerException e) {
                                _log.error("Error getting site config preferences.", e);
                            }
                        } else {
                            _log.warn("Preference " + preference + " was null.");
                        }
                    }
                }
            } catch (EmptyResultDataAccessException e) {
                //Could not find the initialized preference. Site is still not initialized.
            }

        }
        return _initialized;
    }

    /**
     * Returns the primary XNAT system properties extracted from the installed application's manifest file. These
     * properties are guaranteed to include the following:
     * <p>
     * <ul>
     * <li>version</li>
     * <li>buildNumber</li>
     * <li>buildDate</li>
     * <li>commit</li>
     * </ul>
     * <p>
     * There may be other properties available in the system properties and even more available through the {@link
     * #getSystemAttributes()} method.
     *
     * @return The primary system properties.
     */
    public Properties getSystemProperties() {
        return (Properties) _properties.clone();
    }

    /**
     * Gets the requested environment property. Returns null if the property doesn't exist in the environment.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The value of the property if found, null otherwise.
     */
    public String getConfiguredProperty(final String property) {
        return getConfiguredProperty(property, (String) null);
    }

    /**
     * Gets the requested environment property. Returns the specified default value if the property doesn't exist in the
     * environment.
     *
     * @param property     The name of the property to retrieve.
     * @param defaultValue The default value to return if the property isn't set in the environment.
     *
     * @return The value of the property if found, the specified default value otherwise.
     */
    public String getConfiguredProperty(final String property, final String defaultValue) {
        return _environment.getProperty(property, defaultValue);
    }

    /**
     * Gets the requested environment property. Returns null if the property doesn't exist in the environment.
     *
     * @param property The name of the property to retrieve.
     * @param type     The type of the property to retrieve.
     *
     * @return The value of the property if found, null otherwise.
     */
    public <T> T getConfiguredProperty(final String property, final Class<T> type) {
        return getConfiguredProperty(property, type, null);
    }

    /**
     * Gets the requested environment property. Returns the specified default value if the property doesn't exist in the
     * environment.
     *
     * @param property     The name of the property to retrieve.
     * @param type         The type of the property to retrieve.
     * @param defaultValue The default value to return if the property isn't set in the environment.
     *
     * @return The value of the property if found, the specified default value otherwise.
     */
    public <T> T getConfiguredProperty(final String property, final Class<T> type, final T defaultValue) {
        return _environment.getProperty(property, type, defaultValue);
    }

    /**
     * Gets the version of the application.
     *
     * @return The version of the application.
     */
    public String getVersion() {
        final String version = _properties.getProperty("version");
        return version.matches(NON_RELEASE_VERSION_REGEX) ? version + " (build " + getBuildNumber() + " on " + getBuildDate() + ")" : version;
    }

    /**
     * Gets the build number of the application.
     *
     * @return The build number of the application.
     */
    public String getBuildNumber() {
        return _properties.getProperty("buildNumber");
    }

    /**
     * Gets the date the application was built.
     *
     * @return The date the application was built.
     */
    public String getBuildDate() {
        return _properties.getProperty("buildDate");
    }

    /**
     * Gets the commit number in the source repository from which the application was built.
     *
     * @return The commit number of the application.
     */
    public String getCommit() {
        return _properties.getProperty("commit");
    }

    /**
     * Returns extended XNAT system attributes.
     *
     * @return The XNAT system attributes.
     */
    public Map<String, Map<String, String>> getSystemAttributes() {
        return new HashMap<>(_attributes);
    }

    /**
     * Returns the date indicating the time the system was last started.
     *
     * @return A date representing the last start time.
     */
    public Date getStartTime() {
        return new Date(_startTime.getTime());
    }

    /**
     * Returns the system uptime as a map of strings indicating the number of days, hours, minutes, and seconds since
     * the system was last restarted. The map keys are {@link #DAYS}, {@link #HOURS}, {@link #MINUTES}, and {@link
     * #SECONDS}. You can use these values when creating a custom display with the uptime values. If you want a simple
     * string with the uptime already formatted, you can use {@link #getFormattedUptime()} instead.
     *
     * @return A map of values indicating the system uptime.
     */
    public Map<String, String> getUptime() {
        final long diff = new Date().getTime() - _startTime.getTime();
        final int days = (int) (diff / MILLISECONDS_IN_A_DAY);
        final long daysRemainder = diff % MILLISECONDS_IN_A_DAY;
        final int hours = (int) (daysRemainder / MILLISECONDS_IN_AN_HOUR);
        final long hoursRemainder = daysRemainder % MILLISECONDS_IN_AN_HOUR;
        final int minutes = (int) (hoursRemainder / MILLISECONDS_IN_A_MINUTE);
        final long minutesRemainder = hoursRemainder % MILLISECONDS_IN_A_MINUTE;

        final Map<String, String> uptime = new HashMap<>();
        if (days > 0) {
            uptime.put(DAYS, Integer.toString(days));
        }
        if (hours > 0) {
            uptime.put(HOURS, Integer.toString(hours));
        }
        if (minutes > 0) {
            uptime.put(MINUTES, Integer.toString(minutes));
        }
        uptime.put(SECONDS, SECONDS_FORMAT.format(minutesRemainder / 1000F));

        return uptime;
    }

    /**
     * Indicates whether this is a stand-alone XNAT server or the primary node in a distributed XNAT deployment, as
     * opposed to a secondary node. The return value for this method is determined by the value set for the
     * <b>xnat.is_primary_node</b> property. If no value is set for this property, it defaults to <b>true</b>.
     *
     * @return Returns true if this is a stand-alone XNAT server or the primary node in a distributed XNAT deployment.
     */
    public boolean isPrimaryNode() {
        return _primaryNode;
    }

    /**
     * Returns the system uptime in a formatted display string.
     *
     * @return The formatted system uptime.
     */
    public String getFormattedUptime() {
        final Map<String, String> uptime = getUptime();
        final StringBuilder buffer = new StringBuilder();
        if (uptime.containsKey(DAYS)) {
            buffer.append(uptime.get(DAYS)).append(" days, ");
        }
        if (uptime.containsKey(HOURS)) {
            buffer.append(uptime.get(HOURS)).append(" hours, ");
        }
        if (uptime.containsKey(MINUTES)) {
            buffer.append(uptime.get(MINUTES)).append(" minutes, ");
        }
        buffer.append(uptime.get(SECONDS)).append(" seconds");
        return buffer.toString();
    }

    /**
     * Gets the path where XNAT found its primary configuration file.
     *
     * @return The path where XNAT found its primary configuration file.
     */
    public String getSetupPath() {
        return _setupPath;
    }

    /**
     * Gets the path where non-admin users should be sent when errors occur that require administrator intervention.
     *
     * @return Non-admin users error path.
     */
    public String getNonAdminErrorPath() {
        return _nonAdminErrorPath;
    }

    /**
     * Gets the URLs available to all users, including anonymous users.
     *
     * @return A set of the system's open URLs.
     */
    public List<String> getOpenUrls() {
        return ImmutableList.copyOf(_openUrls);
    }

    /**
     * Gets the URLs available only to administrators.
     *
     * @return A set of administrator-only URLs.
     */
    public List<String> getAdminUrls() {
        return ImmutableList.copyOf(_adminUrls);
    }

    public boolean isInitPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _initUrls);
    }

    public boolean isOpenUrlRequest(final HttpServletRequest request) {
        return checkUrls(request, _openUrls);
    }

    public boolean isSetupPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _setupPathPatterns);
    }

    public boolean isNonAdminErrorPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _nonAdminErrorPathPatterns);
    }

    private String translateToBoolean(final String currentValue) {
        if (currentValue == null) {
            return null;
        }
        if (CHECK_VALID_PATTERN.matcher(currentValue).matches()) {
            return null;
        }
        return Boolean.toString(CHECK_TRUE_PATTERN.matcher(currentValue).matches());
    }

    private List<String> asAntPatterns(final List<String> urls) {
        return Lists.transform(urls, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(@Nullable final String url) {
                if (StringUtils.isBlank(url)) {
                    return null;
                }
                return asAntPattern(url);
            }
        });
    }

    private String asAntPattern(final String url) {
        return url + (url.endsWith("/") ? "**" : "*");
    }

    private List<String> nodeToList(final JsonNode node) {
        final List<String> list = new ArrayList<>();
        if (node.isArray()) {
            final ArrayNode arrayNode = (ArrayNode) node;
            for (final JsonNode item : arrayNode) {
                list.add(item.asText());
            }
        } else if (node.isTextual()) {
            list.add(node.asText());
        } else {
            list.add(node.toString());
        }
        return list;
    }

    private boolean checkUrls(final HttpServletRequest request, final Collection<String> urls) {
        if (checkUrls(StringUtils.removeStart(request.getRequestURI(), request.getContextPath()), urls)) {
            return true;
        }

        final URI referer = getReferer(request);
        return referer != null && checkUrls(StringUtils.removeStart(referer.getPath(), request.getContextPath()), urls);
    }

    private URI getReferer(final HttpServletRequest request) {
        // If there's no referer, there's nothing to check.
        final String referer = request.getHeader("Referer");
        if (StringUtils.isBlank(referer)) {
            return null;
        }

        // If the request URI is a page, then we don't care about the referer.
        final String path = StringUtils.removeStart(request.getRequestURI(), request.getContextPath());
        if (path.matches("^/app/(template|screen).*$") || path.matches("^.*\\.vm$") || path.equals("/") || StringUtils.isBlank(path)) {
            return null;
        }

        try {
            final URI refererUri = new URI(referer);
            final URI requestUri = new URI(request.getRequestURL().toString());
            final URI siteUrl    = new URI(_preferences.getSiteUrl());

            final String refererHost = refererUri.getHost();
            final String requestHost = requestUri.getHost();
            final int refererPort = refererUri.getPort();
            final int requestPort = requestUri.getPort();
            final String refererScheme = refererUri.getScheme();
            final String requestScheme = requestUri.getScheme();

            if (StringUtils.equals(refererHost, requestHost) && refererPort == requestPort) {
                final boolean protocolMismatch = _preferences.getMatchSecurityProtocol() && !StringUtils.equals(refererScheme, requestScheme);
                if (protocolMismatch) {
                    final String message = String.format("The referer URI matched request URI host and port, but did not match the security protocol. This is not permitted with the match security protocol setting set to true:\n * Referer: scheme %s, host %s, port %d\n * Request: scheme %s, host %s, port %d",
                                                         refererScheme, refererHost, refererPort, requestScheme, requestHost, requestPort);
                    throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
                }
                _log.info("Referer host and port matched request host and port, valid referer.");
            } else if (StringUtils.isNotBlank(siteUrl.toString()) && StringUtils.equals(refererHost, siteUrl.getHost()) && refererPort == siteUrl.getPort()) {
                final boolean protocolMismatch = _preferences.getMatchSecurityProtocol() && !StringUtils.equals(refererScheme, siteUrl.getScheme());
                if (protocolMismatch) {
                    final String message = String.format("The referer URI matched the configured site URL host and port, but did not match the security protocol. This is not permitted with the match security protocol setting set to true:\n * Referer: scheme %s, host %s, port %d\n * Site URL: scheme %s, host %s, port %d",
                                                         refererScheme, refererHost, refererPort, siteUrl.getScheme(), siteUrl.getHost(), siteUrl.getPort());
                    throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
                }
                _log.info("Referer host and port matched site URL host and port, valid referer.");
            } else {
                final String message = String.format("The referer URI did not match either the request URI or the configured site URL:\n * Referer: scheme %s, host %s, port %d\n * Request: scheme %s, host %s, port %d\n * Site URL: scheme %s, host %s, port %d",
                                                     refererScheme, refererHost, refererPort, requestScheme, requestHost, requestPort, siteUrl.getScheme(), siteUrl.getHost(), siteUrl.getPort());
                throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
            }

            return refererUri;
        } catch (URISyntaxException e) {
            _log.info("Couldn't check referer URI because of a syntax exception: " + request.getRequestURL().toString(), e);
            return null;
        }
    }

    private boolean checkUrls(final String path, final Collection<String> urls) {
        for (final String url : urls) {
            if (PATH_MATCHER.match(url, path)) {
                return true;
            }
        }
        return false;
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatAppInfo.class);

    private static final List<String>     PRIMARY_MANIFEST_ATTRIBUTES = Arrays.asList("Build-Number", "Build-Date", "Implementation-Version", "Implementation-Sha");
    private static final ResourceLoader   RESOURCE_LOADER             = new DefaultResourceLoader();
    private static final SimpleDateFormat FORMATTER                   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
    private static final AntPathMatcher   PATH_MATCHER                = new AntPathMatcher();
    private static final int              MILLISECONDS_IN_A_DAY       = (24 * 60 * 60 * 1000);
    private static final int              MILLISECONDS_IN_AN_HOUR     = (60 * 60 * 1000);
    private static final int              MILLISECONDS_IN_A_MINUTE    = (60 * 1000);
    private static final DecimalFormat    SECONDS_FORMAT              = new DecimalFormat("##.000");
    private static final String           DAYS                        = "days";
    private static final String           HOURS                       = "hours";
    private static final String           MINUTES                     = "minutes";
    private static final String           SECONDS                     = "seconds";
    private static final Pattern          CHECK_VALID_PATTERN         = Pattern.compile("^(?i)(0|1|false|true|f|t)$");
    private static final Pattern          CHECK_TRUE_PATTERN          = Pattern.compile("^(?i)(1|true|t)$");

    private final JdbcTemplate             _template;
    private final Environment              _environment;
    private final SiteConfigPreferences    _preferences;
    private final SerializerService        _serializerService;
    private final PluginOpenUrlsPreference _openUrlsPref;
    private final String                   _setupPath;
    private final List<String>             _setupPathPatterns;
    private final String                   _nonAdminErrorPath;
    private final List<String>             _nonAdminErrorPathPatterns;
    private final boolean                  _primaryNode;

    private       boolean                          _initialized      = false;
    private final List<String>                     _initUrls         = new ArrayList<>();
    private final List<String>                     _openUrls         = new ArrayList<>();
    private final List<String>                     _adminUrls        = new ArrayList<>();
    private final Map<String, String>              _foundPreferences = new HashMap<>();
    private final Date                             _startTime        = new Date();
    private final Properties                       _properties       = new Properties();
    private final Map<String, Map<String, String>> _attributes       = new HashMap<>();
}
