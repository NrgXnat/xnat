/*
 * framework: org.nrg.framework.utilities.TestProperties
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
@SuppressWarnings("unused")
public class TestProperties {
    private static final String DEFAULT_TEST_CONFIG               = "local.properties";
    private static final String TEST_CONFIG_PROP                  = "test.config";
    public static final  String MESSAGE_USING_DEFAULT_TEST_CONFIG = TEST_CONFIG_PROP + " variable not specified, using default, " + DEFAULT_TEST_CONFIG;
    private static final String TEST_CONFIG_FOLDER                = "/config/";

    private final String     _config;
    private final Properties _properties;

    public TestProperties() {
        log.debug("Initializing test properties from system property or default test configuration.");
        String config = System.getProperty(TEST_CONFIG_PROP);

        // test ${xnat.config} in case no profile is specified and maven does not filter a value to surefire
        if (config == null || "${test.config}".equals(config)) {
            config = DEFAULT_TEST_CONFIG;
            log.debug(MESSAGE_USING_DEFAULT_TEST_CONFIG);
        } else {
            log.debug("Using test configuration specified in {}: {}", TEST_CONFIG_PROP, config);
        }

        _config     = config;
        _properties = getProperties();
    }

    public TestProperties(final String config) {
        _config     = config;
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
        log.debug("Initializing from the configuration resource: {}", _config);
        try {
            final Properties props        = new Properties();
            final String     configPath   = TEST_CONFIG_FOLDER + _config;
            InputStream      configStream = getClass().getResourceAsStream(configPath);
            if (configStream == null) {
                throw new RuntimeException("Config file, " + configPath + ", not found");
            }
            props.load(configStream);
            log.debug("Loaded properties from classpath location, {}", configPath);
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Error obtaining test config file: " + _config, e);
        }
    }
}
