/*
 * framework: org.nrg.framework.test.OrmTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.test;

import org.h2.Driver;
import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

/**
 * Provides a re-usable test configuration for setting up in-memory ORM environment. This shouldn't be used in
 * production code and should eventually be refactored into an NRG test tools library.
 */
@Configuration
@EnableTransactionManagement
public class OrmTestConfiguration {
    @Bean
    public DataSource dataSource() {
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(Driver.class);
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");

        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public RegionFactory regionFactory(@Qualifier("hibernateProperties") final Properties properties) {
        return new SingletonEhCacheRegionFactory(properties);
    }

    @Bean
    public FactoryBean<SessionFactory> sessionFactory(final RegionFactory factory,
                                                      final DataSource dataSource,
                                                      @Qualifier("hibernateProperties") final Properties properties,
                                                      final ImprovedNamingStrategy namingStrategy,
                                                      @Autowired(required = false) final List<HibernateEntityPackageList> packageLists) {
        final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean();
        bean.setEntityPackageLists(packageLists);
        bean.setCacheRegionFactory(factory);
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setNamingStrategy(namingStrategy);
        return bean;
    }

    @Bean
    public ResourceTransactionManager transactionManager(final FactoryBean<SessionFactory> sessionFactory) throws Exception {
        return new HibernateTransactionManager(sessionFactory.getObject());
    }

    @Bean
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    public DatabaseHelper databaseHelper(final NamedParameterJdbcTemplate template, final TransactionTemplate transactionTemplate) {
        return new DatabaseHelper(template, transactionTemplate);
    }
}

