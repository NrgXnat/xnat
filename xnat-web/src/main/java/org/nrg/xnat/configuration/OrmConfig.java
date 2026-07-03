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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.nrg.framework.beans.XnatPluginBeanManager;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.nrg.framework.jcache.JCacheHelper.JCACHE_PROVIDER_DEFAULT;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_PROVIDER_ENV;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_URI_DEFAULT;
import static org.nrg.framework.jcache.JCacheHelper.JCACHE_URI_ENV;
import static org.nrg.framework.jcache.JCacheHelper.REDISSON_URI_DEFAULT;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EnableCaching
@ComponentScan("org.nrg.framework.jcache")
@Slf4j
public class OrmConfig {
    @Value("${hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}")
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
    public KeyGenerator defaultHibernateEntityCacheKeyGenerator() {
        return new DefaultHibernateEntityCacheKeyGenerator();
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
        properties.setProperty(JCACHE_PROVIDER_ENV, _cacheProvider);
        properties.setProperty(JCACHE_URI_ENV, JCacheHelper.IS_REDISSON.test(_cacheProvider) && StringUtils.equals(JCACHE_URI_DEFAULT, _cacheUri) ? REDISSON_URI_DEFAULT : _cacheUri);
        properties.setProperty("hibernate.javax.cache.missing_cache_strategy", "create");

        final PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setProperties(properties);
        return bean;
    }

    @Bean
    public PhysicalNamingStrategy physicalNamingStrategy() {
        return new PrefixedPhysicalNamingStrategy("xhbm");
    }

    @Bean
    public FactoryBean<SessionFactory> sessionFactory(final DataSource dataSource, final XnatPluginBeanManager manager, @Autowired(required = false) final List<HibernateEntityPackageList> packageLists) {
        final Properties properties;
        try {
            properties = hibernateProperties().getObject();
        } catch (IOException e) {
            throw new NrgServiceRuntimeException("An error occurred trying to get the Hibernate properties", e);
        }
        // Naming strategies are configured as Hibernate properties instead of LocalSessionFactoryBean setters:
        // Spring's orm.hibernate5 support was compiled against Hibernate 5's void setter signatures, which became
        // fluent in Hibernate 6 and would fail with NoSuchMethodError at build time (see XnatPhysicalNamingStrategy).
        properties.setProperty("hibernate.implicit_naming_strategy", "org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyHbmImpl");
        properties.setProperty("hibernate.physical_naming_strategy", "org.nrg.framework.orm.hibernate.XnatPhysicalNamingStrategy");
        final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean(manager, XNAT_ENTITIES_PACKAGES);
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setEntityPackageLists(packageLists);
        return bean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    // TODO: Convert instances of DatabaseHelper in the code to use singleton instance.
    @Bean
    public DatabaseHelper databaseHelper(final NamedParameterJdbcTemplate template, final TransactionTemplate transactionTemplate) {
        return new DatabaseHelper(template, transactionTemplate);
    }

    @Bean
    public EntityManager entityManager(final FactoryBean<SessionFactory> sessionFactory) throws Exception {
        return sessionFactory.getObject().createEntityManager();
    }

    private static final String XNAT_ENTITIES_PACKAGES = "META-INF/xnat/entities/**/*-entity-packages.txt";
}
