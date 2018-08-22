/*
 * web: org.nrg.xnat.configuration.OrmConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.beans.Beans;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class OrmConfig {
    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties(final Environment environment) {
        final Properties properties = Beans.getNamespacedProperties(environment, "hibernate", false);
        if (properties.size() == 0) {
            if (log.isDebugEnabled()) {
                final StringBuilder message = new StringBuilder("No Hibernate properties specified, using default properties:\n");
                for (final String property : DEFAULT_HIBERNATE_PROPERTIES.stringPropertyNames()) {
                    message.append(" * ").append(property).append(": ").append(DEFAULT_HIBERNATE_PROPERTIES.getProperty(property)).append("\n");
                }
                log.debug(message.toString());
            }
            properties.putAll(DEFAULT_HIBERNATE_PROPERTIES);
        }

        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public RegionFactory regionFactory(final Properties hibernateProperties) throws NrgServiceException {
        final String className = hibernateProperties.getProperty("hibernate.cache.region.factory_class", DEFAULT_REGION_FACTORY_CLASS);
        try {
            final Class<? extends RegionFactory> clazz = Class.forName(className).asSubclass(RegionFactory.class);
            try {
                final Constructor<? extends RegionFactory> constructor = clazz.getConstructor(Properties.class);
                return constructor.newInstance(hibernateProperties);
            } catch (NoSuchMethodException e) {
                return clazz.newInstance();
            }
        } catch (ClassNotFoundException e) {
            log.error("Couldn't find the specified cache region factory class '{}'", className, e);
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "Couldn't find the specified cache region factory class '" + className + "'", e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("An error occurred trying to create an instance of the '{}' class", className, e);
            throw new NrgServiceException(NrgServiceError.ConfigurationError, "An error occurred trying to create an instance of the '" + className + "' class", e);
        }
    }

        @Bean
    public FactoryBean<SessionFactory> sessionFactory(final Environment environment, final DataSource dataSource, final RegionFactory regionFactory, final XnatPluginBeanManager manager) throws NrgServiceException {
        try {
            final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean(manager, XNAT_ENTITIES_PACKAGES);
            bean.setDataSource(dataSource);
            bean.setCacheRegionFactory(regionFactory);
            bean.setHibernateProperties(hibernateProperties(environment).getObject());
            bean.setNamingStrategy(namingStrategy());
            return bean;
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager(final SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    private static final String     XNAT_ENTITIES_PACKAGES       = "META-INF/xnat/entities/**/*-entity-packages.txt";
    private static final String     DEFAULT_REGION_FACTORY_CLASS = "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory";
    private static final Properties DEFAULT_HIBERNATE_PROPERTIES = new Properties() {{
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        setProperty("hibernate.hbm2ddl.auto", "update");
        setProperty("hibernate.show_sql", "false");
        setProperty("hibernate.cache.use_second_level_cache", "true");
        setProperty("hibernate.cache.region.factory_class", DEFAULT_REGION_FACTORY_CLASS);
        setProperty("hibernate.cache.use_query_cache", "true");
    }};
}
