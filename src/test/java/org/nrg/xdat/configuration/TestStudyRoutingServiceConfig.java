package org.nrg.xdat.configuration;

import org.nrg.framework.datacache.DataCacheService;
import org.nrg.framework.datacache.SerializerRegistry;
import org.nrg.framework.datacache.impl.hibernate.DataCacheItemDAO;
import org.nrg.framework.datacache.impl.hibernate.HibernateDataCacheService;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xdat.services.impl.hibernate.DataCacheStudyRoutingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class})
public class TestStudyRoutingServiceConfig {
    @Bean
    public StudyRoutingService studyRoutingService() {
        return new DataCacheStudyRoutingService();
    }

    @Bean
    public DataCacheService dataCacheService() {
        return new HibernateDataCacheService();
    }

    @Bean
    public DataCacheItemDAO dataCacheItemDAO() {
        return new DataCacheItemDAO();
    }

    @Bean
    public SerializerRegistry serializerRegistry() {
        return new SerializerRegistry();
    }

    @Bean
    public HibernateEntityPackageList dataCacheEntities() {
        return new HibernateEntityPackageList("org.nrg.framework.datacache");
    }
}
