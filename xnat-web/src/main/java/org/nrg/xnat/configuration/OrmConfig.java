/*
 * web: org.nrg.xnat.configuration.OrmConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.beans.XnatPluginBeanManager;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.nrg.framework.beans.Beans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class OrmConfig {
    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties(final Environment environment) {
        final PropertiesFactoryBean bean       = new PropertiesFactoryBean();
        final Properties            properties = Beans.getNamespacedProperties(environment, "hibernate", false);
        if (properties.size() == 0) {
            if (_log.isDebugEnabled()) {
                final StringBuilder message = new StringBuilder("No Hibernate properties specified, using default properties:\n");
                for (final String property : DEFAULT_HIBERNATE_PROPERTIES.stringPropertyNames()) {
                    message.append(" * ").append(property).append(": ").append(DEFAULT_HIBERNATE_PROPERTIES.getProperty(property)).append("\n");
                }
                _log.debug(message.toString());
            }
            properties.putAll(DEFAULT_HIBERNATE_PROPERTIES);
        }
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public RegionFactory regionFactory(final Environment environment) throws NrgServiceException {
        try {
            return new SingletonEhCacheRegionFactory(hibernateProperties(environment).getObject());
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final Environment environment, final DataSource dataSource, final XnatPluginBeanManager manager) throws NrgServiceException {
        try {
            final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean(manager, XNAT_ENTITIES_PACKAGES);
            bean.setDataSource(dataSource);
            bean.setCacheRegionFactory(regionFactory(environment));
            bean.setHibernateProperties(hibernateProperties(environment).getObject());
            bean.setNamingStrategy(namingStrategy());
            return bean;
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager(final Environment environment, final DataSource dataSource, final XnatPluginBeanManager manager) throws NrgServiceException {
        return new HibernateTransactionManager(sessionFactory(environment, dataSource, manager).getObject());
    }

    @Bean
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    private static final String     XNAT_ENTITIES_PACKAGES       = "META-INF/xnat/entities/**/*-entity-packages.txt";
    private static final Properties DEFAULT_HIBERNATE_PROPERTIES = new Properties() {{
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        setProperty("hibernate.hbm2ddl.auto", "update");
        setProperty("hibernate.show_sql", "false");
        setProperty("hibernate.cache.use_second_level_cache", "true");
        setProperty("hibernate.cache.use_query_cache", "true");
    }};

    private static final Logger _log = LoggerFactory.getLogger(OrmConfig.class);
}
