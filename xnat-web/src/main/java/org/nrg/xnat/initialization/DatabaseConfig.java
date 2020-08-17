/*
 * web: org.nrg.xnat.initialization.DatabaseConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import javax.sql.DataSource;
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
        if (!BooleanUtils.toBoolean(properties.getProperty("useLoggingProxy", "false"))) {
            return dataSource;
        }
        return getProxiedDataSource(dataSource, properties);
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
        // If the BasicDataSource class is specified, then set some default database connection pooling parameters.
        if (StringUtils.equals(properties.getProperty("class"), DEFAULT_DATASOURCE_CLASS)) {
            if (!properties.containsKey("initialSize")) {
                log.info("No value set for the XNAT datasource initial connection pool size, using default setting {}", DEFAULT_DATASOURCE_INITIAL_SIZE);
                properties.setProperty("initialSize", DEFAULT_DATASOURCE_INITIAL_SIZE);
            }
            if (!properties.containsKey("maxTotal")) {
                log.info("No value set for the XNAT datasource maximum connection pool size, using default setting {}", DEFAULT_DATASOURCE_MAX_TOTAL);
                properties.setProperty("maxTotal", DEFAULT_DATASOURCE_MAX_TOTAL);
            }
            if (!properties.containsKey("maxIdle")) {
                log.info("No value set for the XNAT datasource connection pool idle size, using default setting {}", DEFAULT_DATASOURCE_MAX_IDLE);
                properties.setProperty("maxIdle", DEFAULT_DATASOURCE_MAX_IDLE);
            }
        }
        if (!properties.containsKey("driver")) {
            log.info("No value set for the XNAT datasource driver, using default setting {}", DEFAULT_DATASOURCE_DRIVER);
            properties.setProperty("driver", DEFAULT_DATASOURCE_DRIVER);
        }
        if (!properties.containsKey("url")) {
            log.info("No value set for the XNAT datasource URL, using default setting {}", DEFAULT_DATASOURCE_URL);
            properties.setProperty("url", DEFAULT_DATASOURCE_URL);
        }
        if (!properties.containsKey("username")) {
            log.info("No value set for the XNAT datasource username, using default setting {}. Note that you can set the username to an empty value if you really need an empty string.", DEFAULT_DATASOURCE_USERNAME);
            properties.setProperty("username", DEFAULT_DATASOURCE_USERNAME);
        }
        if (!properties.containsKey("password")) {
            log.info("No value set for the XNAT datasource password, using default setting. Note that you can set the password to an empty value if you really need an empty string.");
            properties.setProperty("password", DEFAULT_DATASOURCE_PASSWORD);
        }
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
}
