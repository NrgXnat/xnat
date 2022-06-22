/*
 * web: org.nrg.xnat.initialization.DatabaseConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.postgresql.Driver;
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
@Slf4j
public class DatabaseConfig {
    @Bean
    public DataSource dataSource(final Environment environment) throws NrgServiceException {
        final Properties properties = Beans.getNamespacedProperties(environment, "datasource", true);
        final DataSource dataSource = getConfiguredDataSource(properties);
        return BooleanUtils.toBoolean(properties.getProperty("useLoggingProxy", "false")) ? getProxiedDataSource(dataSource, properties) : dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(final JdbcTemplate template) {
        return new NamedParameterJdbcTemplate(template);
    }

    @Bean(name = "dbUsername")
    public String dbUsername(final Environment environment) {
        final Properties properties = Beans.getNamespacedProperties(environment, "datasource", true);
        return properties.getProperty("username");
    }

    private DataSource getProxiedDataSource(final DataSource dataSource, final Properties properties) {
        final PrettyQueryEntryCreator creator = new PrettyQueryEntryCreator();
        creator.setMultiline(false);

        final SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
        listener.setQueryLogEntryCreator(creator);
        listener.setLogger(LoggerFactory.getLogger("JdbcLogger"));

        final ProxyDataSourceBuilder builder = ProxyDataSourceBuilder
                .create(dataSource)
                .name("dataSource")
                .listener(listener)
                .proxyResultSet();
        if (BooleanUtils.toBoolean(properties.getProperty("logAsJson", "false"))) {
            builder.asJson();
        }
        return builder.build();
    }

    private DataSource getConfiguredDataSource(final Properties properties) throws NrgServiceException {
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

    private static void setDefaultDatasourceProperties(final Properties properties) {
        // Configure some defaults if they're not already set.
        if (!properties.containsKey("class")) {
            log.info("No value set for the XNAT datasource class, using the default setting {}", DEFAULT_DATASOURCE_CLASS);
            properties.setProperty("class", DEFAULT_DATASOURCE_CLASS);
        }
        if (!properties.containsKey("driver")) {
            log.info("No value set for the XNAT datasource driver, using default setting {}", DEFAULT_DATASOURCE_DRIVER);
            properties.setProperty("driver", DEFAULT_DATASOURCE_DRIVER);
        }
        if (!properties.containsKey("username")) {
            log.info("No value set for the XNAT datasource username, using default setting {}. Note that you can set the username to an empty value if you really need an empty string.", DEFAULT_DATASOURCE_USERNAME);
            properties.setProperty("username", DEFAULT_DATASOURCE_USERNAME);
        }
        if (!properties.containsKey("password")) {
            log.info("No value set for the XNAT datasource password, using default setting. Note that you can set the password to an empty value if you really need an empty string.");
            properties.setProperty("password", DEFAULT_DATASOURCE_PASSWORD);
        }

        final String dataSourceClass = properties.getProperty("class");

        if (StringUtils.equals(dataSourceClass, HIKARI_DATASOURCE_CLASS)) {
            // If the HikariDataSource class is specified, then set some default database connection pooling parameters.
            convertDataSourceConfigProperty(properties, "jdbcUrl", "url", DEFAULT_DATASOURCE_URL);
            convertDataSourceConfigProperty(properties, "minimumIdle", "initialSize", DEFAULT_DATASOURCE_INITIAL_SIZE);
            convertDataSourceConfigProperty(properties, "maximumPoolSize", "maxTotal", DEFAULT_DATASOURCE_MAX_TOTAL);
        } else {
            if (!StringUtils.equals(dataSourceClass, DEFAULT_DATASOURCE_CLASS)) {
                log.warn("Unrecognized data source class {}, setting default values corresponding to DBCP2's settings", dataSourceClass);
            }
            // If HikariDataSource is NOT specified, then set some default database connection pooling parameters for DBCP2.
            convertDataSourceConfigProperty(properties, "url", "jdbcUrl", DEFAULT_DATASOURCE_URL);
            convertDataSourceConfigProperty(properties, "initialSize", "minimumIdle", DEFAULT_DATASOURCE_INITIAL_SIZE);
            convertDataSourceConfigProperty(properties, "maxTotal", "maximumPoolSize", DEFAULT_DATASOURCE_MAX_TOTAL);
            // There's no directly correspondence for DBCP2's maxIdle in HikariCP
            setDataSourceConfigProperty(properties, "maxIdle", DEFAULT_DATASOURCE_MAX_IDLE);
        }
    }

    private static void setDataSourceConfigProperty(final Properties properties, final String property, final String defaultValue) {
        if (!properties.containsKey(property)) {
            log.info("No value set for datasource.{}, using default setting {}", property, defaultValue);
            properties.setProperty(property, defaultValue);
        }
    }

    private static void convertDataSourceConfigProperty(final Properties properties, final String property, final String alias, final String defaultValue) {
        if (properties.containsKey(property)) {
            return;
        }
        if (!properties.containsKey(alias)) {
            setDataSourceConfigProperty(properties, property, defaultValue);
            return;
        }
        final String aliasValue = properties.getProperty(alias);
        log.info("No value set for datasource.{}, but found datasource.{} {}, converting that", property, alias, aliasValue);
        properties.setProperty(property, aliasValue);
    }

    private static class PrettyQueryEntryCreator extends DefaultQueryLogEntryCreator {
        @Override
        protected String formatQuery(String query) {
            return FORMATTER.format(query);
        }

        private static final Formatter FORMATTER = FormatStyle.BASIC.getFormatter();
    }

    private static final String DEFAULT_DATASOURCE_URL          = "jdbc:postgresql://localhost/xnat";
    private static final String DEFAULT_DATASOURCE_USERNAME     = "xnat";
    private static final String DEFAULT_DATASOURCE_PASSWORD     = "xnat";
    private static final String DEFAULT_DATASOURCE_CLASS        = BasicDataSource.class.getName();
    private static final String DEFAULT_DATASOURCE_DRIVER       = Driver.class.getName();
    private static final String DEFAULT_DATASOURCE_INITIAL_SIZE = "20";
    private static final String DEFAULT_DATASOURCE_MAX_TOTAL    = "40";
    private static final String DEFAULT_DATASOURCE_MAX_IDLE     = "10";
    private static final String HIKARI_DATASOURCE_CLASS         = "com.zaxxer.hikari.HikariDataSource";
}
