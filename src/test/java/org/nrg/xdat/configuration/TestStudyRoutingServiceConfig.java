package org.nrg.xdat.configuration;

import org.nrg.framework.configuration.FrameworkConfig;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xdat.services.StudyRoutingService;
import org.nrg.xdat.services.impl.hibernate.DataCacheStudyRoutingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XdatTestConfig.class, FrameworkConfig.class})
public class TestStudyRoutingServiceConfig {
    @Bean
    public StudyRoutingService studyRoutingService() {
        return new DataCacheStudyRoutingService();
    }
}
