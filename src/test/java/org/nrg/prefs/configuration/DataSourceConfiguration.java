package org.nrg.prefs.configuration;

import org.h2.Driver;
import org.hibernate.SessionFactory;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.nrg.framework.orm.hibernate.AggregatedAnnotationSessionFactoryBean;
import org.nrg.framework.orm.hibernate.PrefixedTableNamingStrategy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.support.ResourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DataSourceConfiguration {
    @Bean
    public DataSource dataSource() {
        return new SimpleDriverDataSource() {{
            setDriverClass(Driver.class);
            setUrl("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            setUsername("sa");
            setPassword("");
        }};
    }

    @Bean
    public ImprovedNamingStrategy namingStrategy() {
        return new PrefixedTableNamingStrategy("xhbm");
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
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
                                                      final ImprovedNamingStrategy namingStrategy) {
        final AggregatedAnnotationSessionFactoryBean bean = new AggregatedAnnotationSessionFactoryBean();
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
}
