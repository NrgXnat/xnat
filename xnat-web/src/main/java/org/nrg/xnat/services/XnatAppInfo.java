package org.nrg.xnat.services;

import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.python.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Component
public class XnatAppInfo {

    private static final int           MILLISECONDS_IN_A_DAY    = (24 * 60 * 60 * 1000);
    private static final int           MILLISECONDS_IN_AN_HOUR  = (60 * 60 * 1000);
    private static final int           MILLISECONDS_IN_A_MINUTE = (60 * 1000);
    private static final DecimalFormat SECONDS_FORMAT           = new DecimalFormat("##.000");
    private static final String        DAYS                     = "days";
    private static final String        HOURS                    = "hours";
    private static final String        MINUTES                  = "minutes";
    private static final String        SECONDS                  = "seconds";

    @Inject
    public XnatAppInfo(final ServletContext context, final JdbcTemplate template) throws IOException {
        try (final InputStream input = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            final Manifest   manifest   = new Manifest(input);
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
        }
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/**/*-plugin.properties")) {
            final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            _plugins.put(properties.getProperty("name"), properties);
        }
        _template = template;
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
            _initialized = _template.queryForObject("select value from xhbm_preference p, xhbm_tool t where t.tool_id = 'siteConfig' and p.tool = t.id and p.name = 'initialized';", Boolean.class);
            if (_log.isInfoEnabled()) {
                _log.info("The site was not flagged as initialized, but found initialized preference set to true. Flagging as initialized.");
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
     * Gets the version of the application.
     *
     * @return The version of the application.
     */
    public String getVersion() {
        return _properties.getProperty("version");
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
     * Returns the properties for all of the installed and active plugins in the deployed XNAT server.
     *
     * @return A map of all of the plugins installed on the server.
     */
    public Map<String, Properties> getPluginProperties() throws IOException {
        return ImmutableMap.copyOf(_plugins);
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatAppInfo.class);

    private static final List<String> PRIMARY_MANIFEST_ATTRIBUTES = Arrays.asList("Build-Number", "Build-Date", "Implementation-Version", "Implementation-Sha");

    private final JdbcTemplate _template;

    private final Date                             _startTime  = new Date();
    private final Properties                       _properties = new Properties();
    private final Map<String, Map<String, String>> _attributes = new HashMap<>();
    private final Map<String, Properties>          _plugins    = new HashMap<>();

    private boolean _initialized = false;
}

