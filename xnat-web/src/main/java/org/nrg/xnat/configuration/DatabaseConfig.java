package org.nrg.xnat.configuration;

import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.nrg.framework.utilities.Beans;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * Sets up the database configuration for XNAT.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class DatabaseConfig {

    public static final String DEFAULT_DATASOURCE_URL      = "jdbc:postgresql://localhost/xnat";
    public static final String DEFAULT_DATASOURCE_USERNAME = "xnat";
    public static final String DEFAULT_DATASOURCE_PASSWORD = "xnat";
    public static final String DEFAULT_DATASOURCE_CLASS    = SimpleDriverDataSource.class.getName();
    public static final String DEFAULT_DATASOURCE_DRIVER   = Driver.class.getName();

    @Bean
    public DataSource dataSource() throws NrgServiceException {
        final Properties properties = Beans.getNamespacedProperties(_environment, "datasource", true);
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
    public JdbcTemplate jdbcTemplate() throws NrgServiceException {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        final Properties properties = Beans.getNamespacedProperties(_environment, "hibernate", false);
        if (properties.size() == 0) {
            properties.putAll(DEFAULT_HIBERNATE_PROPERTIES);
        }
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public RegionFactory regionFactory() throws NrgServiceException {
        try {
            return new SingletonEhCacheRegionFactory(hibernateProperties().getObject());
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws NrgServiceException {
        try {
            final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean();
            bean.setDataSource(dataSource());
            bean.setCacheRegionFactory(regionFactory());
            bean.setHibernateProperties(hibernateProperties().getObject());
            bean.setNamingStrategy(namingStrategy());
            return bean;
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws NrgServiceException {
        return new HibernateTransactionManager(sessionFactory().getObject());
    }

    private static Properties setDefaultDatasourceProperties(final Properties properties) {
        // Configure some defaults if they're not already set.
        if (!properties.containsKey("class")) {
            if (_log.isWarnEnabled()) {
                _log.warn("No value set for the XNAT datasource class, using the default value of " + DEFAULT_DATASOURCE_CLASS);
            }
            properties.setProperty("class", DEFAULT_DATASOURCE_CLASS);
        }
        if (!properties.containsKey("driver")) {
            if (_log.isWarnEnabled()) {
                _log.warn("No value set for the XNAT datasource driver, using the default value of " + DEFAULT_DATASOURCE_DRIVER);
            }
            properties.setProperty("driver", DEFAULT_DATASOURCE_DRIVER);
        }
        if (!properties.containsKey("url")) {
            if (_log.isWarnEnabled()) {
                _log.warn("No value set for the XNAT datasource URL, using the default value of " + DEFAULT_DATASOURCE_URL);
            }
            properties.setProperty("url", DEFAULT_DATASOURCE_URL);
        }
        if (!properties.containsKey("username")) {
            if (_log.isWarnEnabled()) {
                _log.warn("No value set for the XNAT datasource username, using the default value of " + DEFAULT_DATASOURCE_USERNAME + ". Note that you can set the username to an empty value if you really need an empty string.");
            }
            properties.setProperty("username", DEFAULT_DATASOURCE_USERNAME);
        }
        if (!properties.containsKey("password")) {
            if (_log.isWarnEnabled()) {
                _log.warn("No value set for the XNAT datasource password, using the default value of " + DEFAULT_DATASOURCE_PASSWORD + ". Note that you can set the password to an empty value if you really need an empty string.");
            }
            properties.setProperty("password", DEFAULT_DATASOURCE_PASSWORD);
        }
        return properties;
    }

    private static final Logger _log = LoggerFactory.getLogger(DatabaseConfig.class);

    private static final Properties DEFAULT_HIBERNATE_PROPERTIES = new Properties() {{
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        setProperty("hibernate.hbm2ddl.auto", "update");
        setProperty("hibernate.show_sql", "false");
        setProperty("hibernate.cache.use_second_level_cache", "true");
        setProperty("hibernate.cache.use_query_cache", "true");
    }};

    @Inject
    private Environment _environment;
}
