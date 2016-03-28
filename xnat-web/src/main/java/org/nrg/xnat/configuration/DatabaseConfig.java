package org.nrg.xnat.configuration;

import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.nrg.framework.utilities.Beans;
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
    @Bean
    public DataSource dataSource() throws NrgServiceException {
        final Properties properties = Beans.getNamespacedProperties(_environment, "datasource", true);
        final String dataSourceClassName = properties.getProperty("class", SimpleDriverDataSource.class.getName());
        try {
            final Class<? extends DataSource> clazz = Class.forName(dataSourceClassName).asSubclass(DataSource.class);
            if (properties.containsKey("driver")) {
                final String driver = (String) properties.get("driver");
                properties.put("driver", Class.forName(driver).newInstance());
            }
            return Beans.getInitializedBean(properties, clazz);
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
        bean.setProperties(Beans.getNamespacedProperties(_environment, "hibernate", false));
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

    @Inject
    private Environment _environment;
}
