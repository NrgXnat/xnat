package org.nrg.xnat.compute.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultHardwareConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultHardwareConfigServiceTestConfig {

    @Bean
    public DefaultHardwareConfigService defaultHardwareConfigService(final HardwareConfigEntityService hardwareConfigEntityService,
                                                                     final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService) {
        return new DefaultHardwareConfigService(
                hardwareConfigEntityService,
                computeEnvironmentConfigEntityService
        );
    }

}
