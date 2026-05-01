package org.nrg.xnat.compute.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.nrg.xnat.compute.entities.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
public class HibernateConfig {
    @Bean
    public Properties hibernateProperties() throws IOException {
        Properties properties = new Properties();

        // Use HSQLDialect instead of H2Dialect to work around issue
        //  with h2 version 1.4.200 in hibernate < 5.4 (or so)
        //  where the generated statements to drop tables between tests can't be executed
        //  as they do not cascade.
        // See https://hibernate.atlassian.net/browse/HHH-13711
        // Solution from https://github.com/hibernate/hibernate-orm/pull/3093#issuecomment-562752874
        // See for auto Dialect Resolution https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#portability-dialectresolver
        //properties.put("hibernate.dialect", "org.nrg.framework.test.ImprovedH2Dialect");
        properties.put("hibernate.hbm2ddl.auto", "create");
        properties.put("hibernate.cache.use_second_level_cache", false);
        properties.put("hibernate.cache.use_query_cache", false);

        PropertiesFactoryBean hibernate = new PropertiesFactoryBean();
        hibernate.setProperties(properties);
        hibernate.afterPropertiesSet();
        return hibernate.getObject();
    }

    @Bean
    public DataSource dataSource() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(org.h2.Driver.class.getName());
        basicDataSource.setUrl("jdbc:h2:mem:test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=authorization,key,value");
        basicDataSource.setUsername("sa");
        return basicDataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource, @Qualifier("hibernateProperties") final Properties properties) {
        final LocalSessionFactoryBean bean = new LocalSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setHibernateProperties(properties);
        bean.setAnnotatedClasses(
                ConstraintConfigEntity.class,
                ConstraintEntity.class,
                ConstraintScopeEntity.class,
                ComputeEnvironmentConfigEntity.class,
                ComputeEnvironmentEntity.class,
                ComputeEnvironmentScopeEntity.class,
                ComputeEnvironmentHardwareOptionsEntity.class,
                HardwareConfigEntity.class,
                HardwareEntity.class,
                HardwareScopeEntity.class,
                HardwareConstraintEntity.class,
                EnvironmentVariableEntity.class,
                MountEntity.class,
                GenericResourceEntity.class
        );
        return bean;
    }

    @Bean
    public ResourceTransactionManager transactionManager(final SessionFactory sessionFactory) throws Exception {
        return new HibernateTransactionManager(sessionFactory);
    }

}
