package org.nrg.xnat.services;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Component
public class XnatAppInfo {
    public XnatAppInfo(final ServletContext context) throws IOException {
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
    }

    /**
     * Indicates whether the XNAT system has been initialized yet.
     * @return Returns true if the system has been initialized, false otherwise.
     */
    public boolean isInitialized() {
        if (!_initialized) {
            // The value for siteUrl is a proxy for whether the system has been initialized: siteUrl can NOT be blank,
            // so if it is this hasn't been initialized.
            //noinspection SqlDialectInspection,SqlNoDataSourceInspection
            _initialized = StringUtils.isNotBlank(_template.queryForObject("select value from xhbm_preference p, xhbm_tool t where t.tool_id = 'siteConfig' and p.tool = t.id and p.name = 'siteUrl';", String.class));
            if (_log.isInfoEnabled()) {
                _log.info("The site was not flagged as initialized, but a valid siteUrl setting was found. Flagging as initialized.");
            }
        }
        return _initialized;
    }

    /**
     * Returns the primary XNAT system properties extracted from the installed application's manifest file. These
     * properties are guaranteed to include the following:
     *
     * <ul>
     *     <li>version</li>
     *     <li>buildNumber</li>
     *     <li>buildDate</li>
     *     <li>commit</li>
     * </ul>
     *
     * There may be other properties available in the system properties and even more available through the {@link
     * #getSystemAttributes()} method.
     *
     * @return The primary system properties.
     */
    public Properties getSystemProperties() {
        return new Properties(_properties);
    }

    /**
     * Returns extended XNAT system attributes.
     *
     * @return The XNAT system attributes.
     */
    public Map<String, Map<String, String>> getSystemAttributes() {
        return new HashMap<>(_attributes);
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatAppInfo.class);

    private static final List<String> PRIMARY_MANIFEST_ATTRIBUTES = Arrays.asList("Build-Number", "Build-Date", "Implementation-Version", "Implementation-Sha");

    @Inject
    private JdbcTemplate _template;

    private final Properties                       _properties  = new Properties();
    private final Map<String, Map<String, String>> _attributes  = new HashMap<>();
    private       boolean                          _initialized = false;
}

