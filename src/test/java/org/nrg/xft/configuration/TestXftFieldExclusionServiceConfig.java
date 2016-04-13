package org.nrg.xft.configuration;

import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.xft.daos.XftFieldExclusionDAO;
import org.nrg.xft.services.XftFieldExclusionService;
import org.nrg.xft.services.XftTestConfig;
import org.nrg.xft.services.impl.hibernate.HibernateXftFieldExclusionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({OrmTestConfiguration.class, XftTestConfig.class})
public class TestXftFieldExclusionServiceConfig {
    @Bean
    public XftFieldExclusionService xftFieldExclusionService() {
        return new HibernateXftFieldExclusionService();
    }

    @Bean
    public XftFieldExclusionDAO xftFieldExclusionDAO() {
        return new XftFieldExclusionDAO();
    }
}
