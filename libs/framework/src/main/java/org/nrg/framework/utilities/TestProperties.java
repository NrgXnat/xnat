/*
 * framework: org.nrg.framework.utilities.TestProperties
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@SuppressWarnings("unused")
public class TestProperties {

    private static final Logger _logger = LoggerFactory.getLogger(TestProperties.class);
    private static final String DEFAULT_TEST_CONFIG = "local.properties";
    private static final String TEST_CONFIG_PROP = "test.config";
    private static final String TEST_CONFIG_FOLDER = "/config/";

    private final String _config;
    private final Properties _properties;

    public TestProperties() {
        _logger.debug("Initializing test properties from system property or default test configuration.");
        String config = System.getProperty(TEST_CONFIG_PROP);

        // test ${xnat.config} in case no profile is specified and maven does not filter a value to surefire
        if (config == null || "${test.config}".equals(config)) {
            config = DEFAULT_TEST_CONFIG;
            if (_logger.isDebugEnabled()) {
                _logger.debug(TEST_CONFIG_PROP + " variable not specified, using default, " + DEFAULT_TEST_CONFIG);
            }
        }

        _config = config;
        _properties = getProperties();
    }

    public TestProperties(final String config) {
        _config = config;
        _properties = getProperties();
    }

    public String get(final String property) {
        if (!_properties.containsKey(property)) {
            throw new RuntimeException("Requested property " + property + " not found in configuration resource " + _config);
        }
        return _properties.getProperty(property);
    }

    /*
     * On the first invocation of getProperties, the method will search for a
     * property file on the classpath at /config/<xnat.config>, where
     * xnat.config is a system property. The property file is then cached for
     * subsequent use.
     */
    private Properties getProperties() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Initializing from the configuration resource: " + _config);
        }
        try {
            final Properties props = new Properties();
            final String configPath = TEST_CONFIG_FOLDER + _config;
            InputStream configStream = getClass().getResourceAsStream(configPath);
            if (configStream == null) {
                throw new RuntimeException("Config file, " + configPath + ", not found");
            }
            props.load(configStream);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Loaded properties from classpath location, " + configPath);
            }
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining test config file: " + _config, e);
        }
    }
}
