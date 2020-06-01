package org.nrg.xnat.config;

import org.mockito.Mockito;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.framework.test.OrmTestConfiguration;
import org.nrg.prefs.services.NrgPreferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(OrmTestConfiguration.class)
@ComponentScan({"org.nrg.xnat.processor.services.impl", "org.nrg.xnat.processor.dao"})
public class TestArchiveProcessorInstanceServiceConfig {
    @Bean
    public HibernateEntityPackageList fileStoreEntityPackages() {
        return new HibernateEntityPackageList("org.nrg.xnat.entities");
    }

    @Bean
    public NrgPreferenceService nrgPreferenceService() {
        return Mockito.mock(NrgPreferenceService.class);
    }
}
