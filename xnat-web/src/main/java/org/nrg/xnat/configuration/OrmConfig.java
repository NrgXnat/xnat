package org.nrg.xnat.configuration;

import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.nrg.framework.processors.XnatPluginBean;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.framework.utilities.Beans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class OrmConfig {
    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        final PropertiesFactoryBean bean       = new PropertiesFactoryBean();
        final Properties            properties = Beans.getNamespacedProperties(_environment, "hibernate", false);
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
    public RegionFactory regionFactory() throws NrgServiceException {
        try {
            return new SingletonEhCacheRegionFactory(hibernateProperties().getObject());
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource) throws NrgServiceException {
        try {
            final LocalSessionFactoryBean bean     = new LocalSessionFactoryBean();
            final String[]                packages = getXnatEntityPackages();
            if (_log.isDebugEnabled()) {
                final StringBuilder message = new StringBuilder("The following packages will be scanned for persistent entities:\n");
                for (final String packageName : packages) {
                    message.append(" * ").append(packageName).append("\n");
                }
                _log.debug(message.toString());
            }
            bean.setPackagesToScan(packages);
            bean.setDataSource(dataSource);
            bean.setCacheRegionFactory(regionFactory());
            bean.setHibernateProperties(hibernateProperties().getObject());
            bean.setNamingStrategy(namingStrategy());
            return bean;
        } catch (IOException e) {
            throw new NrgServiceException(NrgServiceError.Unknown, "An error occurred trying to retrieve the Hibernate properties", e);
        }
    }

    @Bean
    public PlatformTransactionManager transactionManager(final DataSource dataSource) throws NrgServiceException {
        return new HibernateTransactionManager(sessionFactory(dataSource).getObject());
    }

    private static String[] getXnatEntityPackages() throws IOException {
        final Set<String> packages = new HashSet<>();
        for (final Resource resource : BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/entities/**/*-entity-packages.txt")) {
            if (_log.isDebugEnabled()) {
                _log.debug("Processing entity packages from the resource: {}", resource.getFilename());
            }
            try (final InputStream input = resource.getInputStream()) {
                packages.addAll(IOUtils.readLines(input, "UTF-8"));
            }
        }
        try {
            for (final XnatPluginBean plugin : XnatPluginBean.findAllXnatPluginBeans()) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Processing entity packages from plugin {}: {}", plugin.getId(), Joiner.on(", ").join(plugin.getEntityPackages()));
                }
                packages.addAll(plugin.getEntityPackages());
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occurred trying to locate XNAT plugin definitions.");
        }
        return packages.toArray(new String[packages.size()]);
    }

    private static final Properties DEFAULT_HIBERNATE_PROPERTIES = new Properties() {{
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL9Dialect");
        setProperty("hibernate.hbm2ddl.auto", "update");
        setProperty("hibernate.show_sql", "false");
        setProperty("hibernate.cache.use_second_level_cache", "true");
        setProperty("hibernate.cache.use_query_cache", "true");
    }};

    private static final Logger _log = LoggerFactory.getLogger(OrmConfig.class);

    @Inject
    private Environment _environment;
}
