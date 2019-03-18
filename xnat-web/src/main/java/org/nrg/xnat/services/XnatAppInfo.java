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
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.services.SerializerService;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.DisplayHostName;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.nrg.xnat.preferences.PluginOpenUrlsPreference;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static org.nrg.xdat.preferences.SiteConfigPreferences.SITE_URL;
import static org.nrg.xnat.utils.FileUtils.nodeToList;

@Component
@Slf4j
public class XnatAppInfo {
    @Inject
    public XnatAppInfo(final SiteConfigPreferences preferences, final ServletContext context, final Environment environment, final SerializerService serializerService, final JdbcTemplate template, final PluginOpenUrlsPreference openUrlsPref, final XnatNode node, final XnatNodeInfoService nodeInfoService) throws IOException {
        _preferences = preferences;
        _template = template;
        _environment = environment;
        _openUrlsPref = openUrlsPref;
        _serializerService = serializerService;
        _primaryNode = Boolean.parseBoolean(_environment.getProperty(PROPERTY_XNAT_PRIMARY_MODE, "true"));
        _node = node;

        _siteAddress = new URL(_preferences.getSiteUrl()).getHost();
        _hostName = getXnatNodeHostName(nodeInfoService);
        _displayHostName = shouldDisplayHostName(_preferences.getDisplayHostName());

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

        _properties = new HashMap<>();
        try (final InputStream input = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            // final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            if (input != null) {
                final Manifest   manifest   = new Manifest(input);
                final Attributes attributes = manifest.getMainAttributes();
                final String     rawVersion = attributes.getValue(MANIFEST_VERSION);

                _properties.put(PROPERTY_VERSION, rawVersion);
                _properties.put(PROPERTY_BUILD_NUMBER, attributes.getValue(MANIFEST_BUILD_NUMBER));
                _properties.put(PROPERTY_BUILD_DATE, attributes.getValue(MANIFEST_BUILD_DATE));
                _properties.put(PROPERTY_SHA, attributes.getValue(MANIFEST_SHA));
                _properties.put(PROPERTY_DIRTY, attributes.getValue(MANIFEST_DIRTY));

                for (final Object key : attributes.keySet()) {
                    final String name = key.toString();
                    if (!PRIMARY_MANIFEST_ATTRIBUTES.contains(name) && !MANIFEST_ATTRIBUTE_EXCLUSIONS.contains(name)) {
                        final String propertyKey = MANIFEST_PROPERTY_MAPPING.containsKey(name) ? MANIFEST_PROPERTY_MAPPING.get(name) : name;
                        _properties.put(propertyKey, attributes.getValue(name));
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

                _properties.put(PROPERTY_TIMESTAMP, Long.toString((_buildDate = parseDate(attributes.getValue(MANIFEST_BUILD_DATE))).getTime()));
                _properties.put(PROPERTY_HOSTNAME, _hostName);
                _properties.put(PROPERTY_DISPLAY_HOST_NAME, Boolean.toString(_displayHostName));
                _properties.put(PROPERTY_NODE_ID, _node.getNodeId());

                _versionDisplay = NON_RELEASE_VERSION_REGEX.matcher(rawVersion).matches()
                                  ? rawVersion + "-" + getCommit() + "-" + getCommitHash() + (isDirty() ? ".dirty" : "") + " (build " + getBuildNumber() + " on " + getBuildDate() + ")"
                                  : rawVersion;
            } else {
                log.warn("Attempted to load /META-INF/MANIFEST.MF but couldn't find it, all version information is unknown.");
                _versionDisplay = "Unknown";
                _buildDate = new Date();
                _properties.put(PROPERTY_BUILD_NUMBER, "Unknown");
                _properties.put(PROPERTY_BUILD_DATE, FORMATTER.format(_buildDate));
                _properties.put(PROPERTY_TIMESTAMP, Long.toString(_buildDate.getTime()));
                _properties.put(PROPERTY_VERSION, "Unknown");
                _properties.put(PROPERTY_SHA, "Unknown");
                _properties.put(PROPERTY_DIRTY, "Unknown");
            }

            log.debug("Initialized application build information:\n * Version: {}\n * Build number: {}\n * Build Date: {}\n * Commit: {}\n * Dirty flag: {}",
                      getVersion(),
                      getBuildNumber(),
                      getBuildDate(),
                      getCommitHashFull(),
                      isDirty());

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
                                addFoundStringPreference(resultSet, SITE_URL, "site_url");
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
                    log.info("Nothing to migrate");
                }
            }
        }

    }

    private boolean shouldDisplayHostName(final DisplayHostName displayHostName) {
        return displayHostName == DisplayHostName.always || (displayHostName != DisplayHostName.never && !StringUtils.equals(_hostName, _siteAddress));
    }

    @SuppressWarnings("unused")
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
            log.debug("Could not update open URL list", e);
        }
    }

    /**
     * Returns any found preferences. If no preferences were found, the returned map will be empty.
     *
     * @return A map containing the found preferences.
     */
    @SuppressWarnings("unused")
    public Map<String, String> getFoundPreferences() {
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
            // noinspection SqlNoDataSourceInspection
            try {
                _initialized = _template.queryForObject("select value from xhbm_preference p, xhbm_tool t where t.tool_id = 'siteConfig' and p.tool = t.id and p.name = 'initialized';", Boolean.class);
                if (_initialized) {
                    if (log.isInfoEnabled()) {
                        log.info("The site was not flagged as initialized, but found initialized preference set to true. Flagging as initialized.");
                    }
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("The site was not flagged as initialized and initialized preference set to false. Setting system for initialization.");
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
                                log.error("", e);
                            } catch (NullPointerException e) {
                                log.error("Error getting site config preferences.", e);
                            }
                        } else {
                            log.warn("Preference " + preference + " was null.");
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
    public Map<String, String> getSystemProperties() {
        return _properties;
    }

    /**
     * Gets the requested environment property. Returns null if the property doesn't exist in the environment.
     *
     * @param property The name of the property to retrieve.
     *
     * @return The value of the property if found, null otherwise.
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
        return defaultValue == null ? _environment.getProperty(property, type) : _environment.getProperty(property, type, defaultValue);
    }

    @SuppressWarnings("unused")
    public Set<String> getSystemPropertyNames() {
        return _properties.keySet();
    }

    /**
     * Returns the specified property.
     *
     * @param property The property to retrieve.
     *
     * @return The value of the specified property.
     */
    public String getSystemProperty(final String property) {
        return getSystemProperty(property, null);
    }

    /**
     * Returns the specified property.
     *
     * @param property     The property to retrieve.
     * @param defaultValue The value to return if the property doesn't exist.
     *
     * @return The value of the specified property.
     */
    public String getSystemProperty(final String property, final String defaultValue) {
        return ObjectUtils.defaultIfNull(_properties.get(property), defaultValue);
    }

    /**
     * Gets the version of the application.
     *
     * @return The version of the application.
     */
    public String getVersion() {
        return _versionDisplay;
    }

    /**
     * Gets the build number of the application.
     *
     * @return The build number of the application.
     */
    public String getBuildNumber() {
        return _properties.get(PROPERTY_BUILD_NUMBER);
    }

    /**
     * Gets the date the application was built.
     *
     * @return The date the application was built.
     */
    public Date getBuildDate() {
        return _buildDate;
    }

    /**
     * Gets the abbreviated commit hash in the source repository from which the application was built.
     *
     * @return The commit hash from which the application was built.
     */
    public String getCommitHash() {
        return _properties.get(PROPERTY_SHA);
    }

    /**
     * Indicates whether the application was built from "dirty" code, i.e. where there were changes in the repository that had
     * not yet been committed to the repository.
     *
     * @return Returns true if there were any changes to the code that hadn't yet been committed at build time, false otherwise.
     */
    public boolean isDirty() {
        return BooleanUtils.toBooleanDefaultIfNull(BooleanUtils.toBoolean(_properties.get(PROPERTY_DIRTY)), false);
    }

    /**
     * Gets the full commit hash in the source repository from which the application was built.
     *
     * @return The full commit hash from which the application was built.
     */
    public String getCommitHashFull() {
        return _properties.get(PROPERTY_SHA_FULL);
    }

    /**
     * Gets the commit number in the source repository from which the application was built.
     *
     * @return The commit number of the application.
     */
    public String getCommit() {
        return _properties.get(PROPERTY_COMMIT);
    }

    /**
     * Gets the commit number in the source repository from which the application was built.
     *
     * @return The commit number of the application.
     */
    public String getBranch() {
        return _properties.get(PROPERTY_BRANCH);
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
        final long diff             = new Date().getTime() - _startTime.getTime();
        final int  days             = (int) (diff / MILLISECONDS_IN_A_DAY);
        final long daysRemainder    = diff % MILLISECONDS_IN_A_DAY;
        final int  hours            = (int) (daysRemainder / MILLISECONDS_IN_AN_HOUR);
        final long hoursRemainder   = daysRemainder % MILLISECONDS_IN_AN_HOUR;
        final int  minutes          = (int) (hoursRemainder / MILLISECONDS_IN_A_MINUTE);
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

    @SuppressWarnings("unused")
    public String getHostName() {
        return _hostName;
    }

    @SuppressWarnings("unused")
    public boolean isDisplayHostName() {
        return _displayHostName;
    }

    public void setDisplayHostName(final DisplayHostName displayHostName) {
        _properties.put(PROPERTY_DISPLAY_HOST_NAME, Boolean.toString(_displayHostName = shouldDisplayHostName(displayHostName)));
    }

    public XnatNode getNode() {
        return _node;
    }

    /**
     * Returns the system uptime in a formatted display string.
     *
     * @return The formatted system uptime.
     */
    public String getFormattedUptime() {
        final Map<String, String> uptime = getUptime();
        final StringBuilder       buffer = new StringBuilder();
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
    @Nonnull
    public List<String> getOpenUrls() {
        return ImmutableList.copyOf(_openUrls);
    }

    /**
     * Gets the URLs available only to administrators.
     *
     * @return A set of administrator-only URLs.
     */
    @Nonnull
    public List<String> getAdminUrls() {
        return ImmutableList.copyOf(_adminUrls);
    }

    public boolean isInitPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _initUrls);
    }

    @SuppressWarnings("unused")
    public boolean isOpenUrlRequest(final HttpServletRequest request) {
        return checkUrls(request, _openUrls);
    }

    public boolean isSetupPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _setupPathPatterns);
    }

    public boolean isNonAdminErrorPathRequest(final HttpServletRequest request) {
        return checkUrls(request, _nonAdminErrorPathPatterns);
    }

    private static Date parseDate(final String dateProperty) {
        try {
            return FORMATTER.parse(dateProperty);
        } catch (ParseException e) {
            log.warn("Unable to parse the build date value, returning current date for fail-over: {}", dateProperty);
            return new Date();
        }
    }

    private static List<String> findHostNames() {
        final Set<String> hostNames = XDAT.getHostNames();
        if (hostNames.isEmpty()) {
            return Collections.singletonList("localhost");
        }
        return new ArrayList<>(hostNames);
    }

    private XnatNodeInfo getXnatNodeInfo(final XnatNodeInfoService nodeInfoService) {
        final String             nodeId    = _node.getNodeId();
        final List<XnatNodeInfo> nodeInfos = nodeInfoService.getXnatNodeInfoByNodeId(nodeId);
        final String[]           hostNames = findHostNames().toArray(new String[0]);
        for (final XnatNodeInfo nodeInfo : nodeInfos) {
            if (StringUtils.equalsAny(nodeInfo.getHostName(), hostNames)) {
                return nodeInfo;
            }
        }
        return null;
    }

    private String getXnatNodeHostName(final XnatNodeInfoService nodeInfoService) {
        final XnatNodeInfo nodeInfo = getXnatNodeInfo(nodeInfoService);
        if (nodeInfo != null) {
            return nodeInfo.getHostName();
        }
        final List<String> hostNames = findHostNames();
        if (hostNames.isEmpty()) {
            return "localhost";
        }
        return hostNames.contains(_siteAddress) ? _siteAddress : hostNames.iterator().next();
    }

    /**
     * Gets the plugin admin urls.
     *
     * @return the plugin admin urls
     */
    private List<? extends String> getPluginAdminUrls() {
        return _openUrlsPref.getUrlList(XnatPlugin.PLUGIN_ADMIN_URLS);
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

            final String refererHost   = refererUri.getHost();
            final String requestHost   = requestUri.getHost();
            final int    refererPort   = refererUri.getPort();
            final int    requestPort   = requestUri.getPort();
            final String refererScheme = refererUri.getScheme();
            final String requestScheme = requestUri.getScheme();

            if (StringUtils.equals(refererHost, requestHost) && refererPort == requestPort) {
                final boolean protocolMismatch = _preferences.getMatchSecurityProtocol() && !StringUtils.equals(refererScheme, requestScheme);
                if (protocolMismatch) {
                    final String message = String.format("The referer URI matched request URI host and port, but did not match the security protocol. This is not permitted with the match security protocol setting set to true:\n * Referer: scheme %s, host %s, port %d\n * Request: scheme %s, host %s, port %d",
                                                         refererScheme, refererHost, refererPort, requestScheme, requestHost, requestPort);
                    throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
                }
                log.info("Referer host and port matched request host and port, valid referer.");
            } else if (StringUtils.isNotBlank(siteUrl.toString()) && StringUtils.equals(refererHost, siteUrl.getHost()) && refererPort == siteUrl.getPort()) {
                final boolean protocolMismatch = _preferences.getMatchSecurityProtocol() && !StringUtils.equals(refererScheme, siteUrl.getScheme());
                if (protocolMismatch) {
                    final String message = String.format("The referer URI matched the configured site URL host and port, but did not match the security protocol. This is not permitted with the match security protocol setting set to true:\n * Referer: scheme %s, host %s, port %d\n * Site URL: scheme %s, host %s, port %d",
                                                         refererScheme, refererHost, refererPort, siteUrl.getScheme(), siteUrl.getHost(), siteUrl.getPort());
                    throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
                }
                log.info("Referer host and port matched site URL host and port, valid referer.");
            } else {
                final String message = String.format("The referer URI did not match either the request URI or the configured site URL:\n * Referer: scheme %s, host %s, port %d\n * Request: scheme %s, host %s, port %d\n * Site URL: scheme %s, host %s, port %d",
                                                     refererScheme, refererHost, refererPort, requestScheme, requestHost, requestPort, siteUrl.getScheme(), siteUrl.getHost(), siteUrl.getPort());
                throw new NrgServiceRuntimeException(NrgServiceError.SecurityViolation, message);
            }

            return refererUri;
        } catch (URISyntaxException e) {
            log.info("Couldn't check referer URI because of a syntax exception: " + request.getRequestURL().toString(), e);
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

    private static final String PROPERTY_XNAT_PRIMARY_MODE = "xnat.is_primary_node";
    private static final String MANIFEST_BUILD_NUMBER      = "Build-Number";
    private static final String MANIFEST_BUILD_DATE        = "Build-Date";
    private static final String MANIFEST_VERSION           = "Implementation-Version";
    private static final String MANIFEST_SHA               = "Implementation-Sha";
    private static final String MANIFEST_DIRTY             = "Implementation-Dirty";
    private static final String MANIFEST_BRANCH            = "Implementation-Branch";
    private static final String MANIFEST_COMMIT            = "Implementation-Commit";
    private static final String MANIFEST_LAST_TAG          = "Implementation-LastTag";
    private static final String MANIFEST_SHA_FULL          = "Implementation-Sha-Full";
    private static final String PROPERTY_BUILD_NUMBER      = "buildNumber";
    private static final String PROPERTY_BUILD_DATE        = "buildDate";
    private static final String PROPERTY_VERSION           = "version";
    private static final String PROPERTY_SHA               = "sha";
    private static final String PROPERTY_DIRTY             = "isDirty";
    private static final String PROPERTY_BRANCH            = "branch";
    private static final String PROPERTY_SHA_FULL          = "shaFull";
    private static final String PROPERTY_COMMIT            = "commit";
    private static final String PROPERTY_TIMESTAMP         = "timestamp";
    private static final String PROPERTY_TAG               = "tag";
    private static final String PROPERTY_HOSTNAME          = "hostName";
    private static final String PROPERTY_DISPLAY_HOST_NAME = "displayHostName";
    private static final String PROPERTY_NODE_ID           = "nodeId";

    private static final List<String>        PRIMARY_MANIFEST_ATTRIBUTES   = Arrays.asList(MANIFEST_BUILD_NUMBER, MANIFEST_BUILD_DATE, MANIFEST_VERSION, MANIFEST_SHA, MANIFEST_DIRTY);
    private static final List<String>        MANIFEST_ATTRIBUTE_EXCLUSIONS = Arrays.asList("Application-Name", "Manifest-Version", "Implementation-CleanTag");
    private static final Map<String, String> MANIFEST_PROPERTY_MAPPING     = ImmutableMap.of(MANIFEST_BRANCH, PROPERTY_BRANCH, MANIFEST_COMMIT, PROPERTY_COMMIT, MANIFEST_LAST_TAG, PROPERTY_TAG, MANIFEST_SHA_FULL, PROPERTY_SHA_FULL);

    private static final ResourceLoader   RESOURCE_LOADER           = new DefaultResourceLoader();
    private static final SimpleDateFormat FORMATTER                 = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    private static final AntPathMatcher   PATH_MATCHER              = new AntPathMatcher();
    private static final Pattern          NON_RELEASE_VERSION_REGEX = Pattern.compile("(?i:^.*(SNAPSHOT|BETA|RC).*$)");
    private static final int              MILLISECONDS_IN_A_DAY     = (24 * 60 * 60 * 1000);
    private static final int              MILLISECONDS_IN_AN_HOUR   = (60 * 60 * 1000);
    private static final int              MILLISECONDS_IN_A_MINUTE  = (60 * 1000);
    private static final DecimalFormat    SECONDS_FORMAT            = new DecimalFormat("##.000");
    private static final String           DAYS                      = "days";
    private static final String           HOURS                     = "hours";
    private static final String           MINUTES                   = "minutes";
    private static final String           SECONDS                   = "seconds";
    private static final Pattern          CHECK_VALID_PATTERN       = Pattern.compile("^(?i)(0|1|false|true|f|t)$");
    private static final Pattern          CHECK_TRUE_PATTERN        = Pattern.compile("^(?i)(1|true|t)$");

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
    private final Map<String, String>      _properties;
    private final String                   _versionDisplay;
    private final Date                     _buildDate;
    private final String                   _hostName;
    private final XnatNode                 _node;
    private final String                   _siteAddress;

    private final List<String>                     _initUrls         = new ArrayList<>();
    private final List<String>                     _openUrls         = new ArrayList<>();
    private final List<String>                     _adminUrls        = new ArrayList<>();
    private final Map<String, String>              _foundPreferences = new HashMap<>();
    private final Date                             _startTime        = new Date();
    private final Map<String, Map<String, String>> _attributes       = new HashMap<>();

    private boolean _initialized = false;
    private boolean _displayHostName;
}
