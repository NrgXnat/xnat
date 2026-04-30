/*
 * framework: org.nrg.framework.test.OrmTestConfiguration
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.test;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.jcache.DefaultHibernateEntityCacheKeyGenerator;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.orm.hibernate.PrefixedPhysicalNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.jcache.config.JCacheConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Driver;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.nrg.framework.jcache.JCacheHelper.JCACHE_PROVIDER_DEFAULT;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_PROVIDER_ENV;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_URI_DEFAULT;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_URI_ENV;

/**
 * Provides a re-usable test configuration for setting up in-memory ORM environment. This shouldn't be used in
 * production code and should eventually be refactored into an NRG test tools library.
 */
@Configuration
@EnableCaching
@EnableTransactionManagement
@ComponentScan("org.nrg.framework.jcache")
@Slf4j
public class OrmTestConfiguration extends JCacheConfigurerSupport {
    @Value("${jdbc.driver.class:org.h2.Driver}")
    private String  _jdbcDriverClass;
    // Note: if MODE=PostgreSQL is in the URL, the dialect must be a PostgreSQL dialect or some operations will fail.
    @Value("${jdbc.url:jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=authorization,key,value}")
    private String  _jdbcUrl;
    @Value("${jdbc.username:sa}")
    private String  _jdbcUsername;
    @Value("${jdbc.password:}")
    private String  _jdbcPassword;
    // Note: if MODE=PostgreSQL is in the URL, the dialect must be a PostgreSQL dialect or some operations will fail.
    @Value("${hibernate.dialect:org.hibernate.dialect.PostgreSQL10Dialect}")
    private String  _dialect;
    @Value("${hibernate.hbm2ddl.auto:create-drop}")
    private String  _hbm2ddlAuto;
    @Value("${hibernate.show-sql:false}")
    private boolean _showSql;
    @Value("${hibernate.cache.use_second_level_cache:true}")
    private boolean _useSecondLevelCache;
    @Value("${hibernate.cache.use_query_cache:true}")
    private boolean _useQueryCache;
    @Value("${hibernate.cache.region.factory_class:org.hibernate.cache.jcache.internal.JCacheRegionFactory}")
    private String  _regionFactoryClass;
    @Value("${" + JCACHE_PROVIDER_ENV + ":" + JCACHE_PROVIDER_DEFAULT + "}")
    private String  _cacheProvider;
    @Value("${" + JCACHE_URI_ENV + ":" + JCACHE_URI_DEFAULT + "}")
    private String  _cacheUri;

    @Bean
    @Override
    public org.springframework.cache.CacheManager cacheManager() {
        return new JCacheCacheManager(JCacheHelper.getCachingProvider(_cacheProvider).getCacheManager());
    }

    @Bean
    public KeyGenerator defaultHibernateEntityCacheKeyGenerator() {
        return new DefaultHibernateEntityCacheKeyGenerator();
    }

    @Bean
    public DataSource dataSource() {
        final Class<? extends Driver> driverClass;
        try {
            driverClass = Class.forName(_jdbcDriverClass).asSubclass(Driver.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("An error occurred trying to get the driver class " + _jdbcDriverClass, e);
        }
        final SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(driverClass);
        dataSource.setUrl(_jdbcUrl);
        dataSource.setUsername(_jdbcUsername);
        dataSource.setPassword(_jdbcPassword);
        return dataSource;
    }

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new PrefixedPhysicalNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", _dialect);
        properties.setProperty("hibernate.hbm2ddl.auto", _hbm2ddlAuto);
        properties.setProperty("hibernate.show_sql", Boolean.toString(_showSql));
        properties.setProperty("hibernate.cache.use_second_level_cache", Boolean.toString(_useSecondLevelCache));
        properties.setProperty("hibernate.cache.use_query_cache", Boolean.toString(_useQueryCache));
        properties.setProperty("hibernate.cache.region.factory_class", _regionFactoryClass);
        properties.setProperty("hibernate.javax.cache.provider", _cacheProvider);
        properties.setProperty("hibernate.javax.cache.uri", _cacheUri);
        properties.setProperty("hibernate.javax.cache.missing_cache_strategy", "create");

        getExtraHibernateProperties().forEach(properties::setProperty);

        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public FactoryBean<SessionFactory> sessionFactory(@Autowired(required = false) final List<HibernateEntityPackageList> packageLists) {
        final Properties properties;
        try {
            properties = hibernateProperties().getObject();
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to get the Hibernate properties", e);
        }
        final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean();
        bean.setDataSource(dataSource());
        bean.setHibernateProperties(properties);
        bean.setEntityPackageLists(packageLists);
        bean.setImplicitNamingStrategy(new ImplicitNamingStrategyLegacyHbmImpl());
        bean.setPhysicalNamingStrategy(physicalNamingStrategy());
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

    /**
     * This is provided as a hook to allow downstream projects to add properties to the Hibernate properties
     * configuration. The default implementation returns an empty map.
     *
     * @return A map of properties and values to add to the Hibernate properties configuration.
     */
    protected Map<String, String> getExtraHibernateProperties() {
        return Collections.emptyMap();
    }
}
