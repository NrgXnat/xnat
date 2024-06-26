package org.nrg.xnat.compute.config;

import org.hibernate.SessionFactory;
import org.nrg.xnat.compute.entities.*;
import org.nrg.xnat.compute.repositories.HardwareConfigDao;
import org.nrg.xnat.compute.services.impl.HibernateHardwareConfigEntityService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Import({HibernateConfig.class})
public class HibernateHardwareConfigEntityServiceTestConfig {

    @Bean
    public HibernateHardwareConfigEntityService hibernateHardwareConfigEntityServiceTest() {
        return new HibernateHardwareConfigEntityService();
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

    @Bean
    public HardwareConfigDao hardwareConfigDao(final SessionFactory sessionFactory) {
        return new HardwareConfigDao(sessionFactory);
    }

}
