/*
 * web: org.nrg.xnat.node.services.impl.NodeLockConnectionSettings
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import lombok.ToString;
import lombok.Value;

import java.util.Properties;

/**
 * How {@link PostgresNodeLockService} opens its dedicated lock connection. These are the application data source's
 * driver, URL and credentials: the lock connection is opened directly through the configured driver class rather
 * than borrowed from the connection pool, so drivers that do their own authentication (for example IAM token
 * drivers) work the same way for the lock connection as for the pool. The driver properties the pool passes to the
 * driver ({@code datasource.dataSourceProperties.*}, for example SSL settings) are passed to the lock connection
 * too.
 */
@Value
public class NodeLockConnectionSettings {
    /**
     * Prefix of the {@code datasource.*} properties that the connection pool hands to the JDBC driver.
     */
    public static final String DRIVER_PROPERTIES_PREFIX = "dataSourceProperties.";

    String driverClassName;
    String url;
    String username;
    @ToString.Exclude
    String password;
    @ToString.Exclude
    Properties driverProperties;

    public NodeLockConnectionSettings(final String driverClassName, final String url, final String username, final String password) {
        this(driverClassName, url, username, password, null);
    }

    public NodeLockConnectionSettings(final String driverClassName, final String url, final String username, final String password, final Properties driverProperties) {
        this.driverClassName  = driverClassName;
        this.url              = url;
        this.username         = username;
        this.password         = password;
        this.driverProperties = new Properties();
        if (driverProperties != null) {
            this.driverProperties.putAll(driverProperties);
        }
    }

    /**
     * Builds the settings from the application's {@code datasource.*} properties, with the prefix removed and the
     * defaults applied: the driver, URL and credentials, plus every {@code dataSourceProperties.*} entry as a driver
     * property.
     *
     * @param datasource The data source properties.
     *
     * @return The lock connection settings.
     */
    public static NodeLockConnectionSettings fromDatasourceProperties(final Properties datasource) {
        final Properties driverProperties = new Properties();
        for (final String name : datasource.stringPropertyNames()) {
            if (name.startsWith(DRIVER_PROPERTIES_PREFIX)) {
                driverProperties.setProperty(name.substring(DRIVER_PROPERTIES_PREFIX.length()), datasource.getProperty(name));
            }
        }
        return new NodeLockConnectionSettings(datasource.getProperty("driver"), datasource.getProperty("url"), datasource.getProperty("username"), datasource.getProperty("password"), driverProperties);
    }

    /**
     * Gets the driver properties to open the lock connection with.
     *
     * @return A copy of the driver properties.
     */
    public Properties getDriverProperties() {
        final Properties copy = new Properties();
        copy.putAll(driverProperties);
        return copy;
    }
}
