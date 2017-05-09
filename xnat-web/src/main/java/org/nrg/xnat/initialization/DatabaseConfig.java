/*
 * web: org.nrg.xnat.initialization.DatabaseConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.beans.Beans;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Sets up the database configuration for XNAT.
 */
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource(final Environment environment) throws NrgServiceException {
        final Properties properties = Beans.getNamespacedProperties(environment, "datasource", true);
        setDefaultDatasourceProperties(properties);
        final String dataSourceClassName = properties.getProperty("class");
        try {
            final Class<? extends DataSource> dataSourceClazz = Class.forName(dataSourceClassName).asSubclass(DataSource.class);
            if (properties.containsKey("driver")) {
                final String driver = properties.getProperty("driver");
                try {
                    properties.put("driver", Class.forName(driver).newInstance());
                } catch (ClassNotFoundException e) {
                    throw new NrgServiceException(NrgServiceError.ConfigurationError, "Couldn't find the specified JDBC driver class name: " + driver);
                }
            }
            return Beans.getInitializedBean(properties, dataSourceClazz);
        } catch (ClassNotFoundException e) {
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "Couldn't find the specified data-source class name: " + dataSourceClassName);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "An error occurred trying to access a property in the specified data-source class: " + dataSourceClassName, e);
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) throws NrgServiceException {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(final DataSource dataSource) throws NrgServiceException {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name="dbUsername")
    public String dbUsername(final Environment environment) throws NrgServiceException {
        final Properties properties = Beans.getNamespacedProperties(environment, "datasource", true);
        return properties.getProperty("username");
    }

    private static void setDefaultDatasourceProperties(final Properties properties) {
        // Configure some defaults if they're not already set.
        if (!properties.containsKey("class")) {
            _log.info("No value set for the XNAT datasource class, using the default setting {}", DEFAULT_DATASOURCE_CLASS);
            properties.setProperty("class", DEFAULT_DATASOURCE_CLASS);
        }
        // If the BasicDataSource class is specified, then set some default database connection pooling parameters.
        if (StringUtils.equals(properties.getProperty("class"), DEFAULT_DATASOURCE_CLASS)) {
            if (!properties.containsKey("initialSize")) {
                _log.info("No value set for the XNAT datasource initial connection pool size, using default setting {}", DEFAULT_DATASOURCE_INITIAL_SIZE);
                properties.setProperty("initialSize", DEFAULT_DATASOURCE_INITIAL_SIZE);
            }
            if (!properties.containsKey("maxTotal")) {
                _log.info("No value set for the XNAT datasource maximum connection pool size, using default setting {}", DEFAULT_DATASOURCE_MAX_TOTAL);
                properties.setProperty("maxTotal", DEFAULT_DATASOURCE_MAX_TOTAL);
            }
            if (!properties.containsKey("maxIdle")) {
                _log.info("No value set for the XNAT datasource connection pool idle size, using default setting {}", DEFAULT_DATASOURCE_MAX_IDLE);
                properties.setProperty("maxIdle", DEFAULT_DATASOURCE_MAX_IDLE);
            }
        }
        if (!properties.containsKey("driver")) {
            _log.info("No value set for the XNAT datasource driver, using default setting {}", DEFAULT_DATASOURCE_DRIVER);
            properties.setProperty("driver", DEFAULT_DATASOURCE_DRIVER);
        }
        if (!properties.containsKey("url")) {
            _log.info("No value set for the XNAT datasource URL, using default setting {}", DEFAULT_DATASOURCE_URL);
            properties.setProperty("url", DEFAULT_DATASOURCE_URL);
        }
        if (!properties.containsKey("username")) {
            _log.info("No value set for the XNAT datasource username, using default setting {}. Note that you can set the username to an empty value if you really need an empty string.", DEFAULT_DATASOURCE_USERNAME);
            properties.setProperty("username", DEFAULT_DATASOURCE_USERNAME);
        }
        if (!properties.containsKey("password")) {
            _log.info("No value set for the XNAT datasource password, using default setting. Note that you can set the password to an empty value if you really need an empty string.");
            properties.setProperty("password", DEFAULT_DATASOURCE_PASSWORD);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final String DEFAULT_DATASOURCE_URL          = "jdbc:postgresql://localhost/xnat";
    private static final String DEFAULT_DATASOURCE_USERNAME     = "xnat";
    private static final String DEFAULT_DATASOURCE_PASSWORD     = "xnat";
    private static final String DEFAULT_DATASOURCE_CLASS        = BasicDataSource.class.getName();
    private static final String DEFAULT_DATASOURCE_DRIVER       = Driver.class.getName();
    private static final String DEFAULT_DATASOURCE_INITIAL_SIZE = "20";
    private static final String DEFAULT_DATASOURCE_MAX_TOTAL    = "40";
    private static final String DEFAULT_DATASOURCE_MAX_IDLE     = "10";
}
