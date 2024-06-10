package org.nrg.xnat.compute.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigEntityService;
import org.nrg.xnat.compute.services.HardwareConfigEntityService;
import org.nrg.xnat.compute.services.impl.DefaultComputeEnvironmentConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({HibernateEntityServicesConfig.class})
public class DefaultComputeEnvironmentConfigServiceTestConfig {

    @Bean
    public DefaultComputeEnvironmentConfigService defaultComputeEnvironmentConfigService(final ComputeEnvironmentConfigEntityService computeEnvironmentConfigEntityService,
                                                                                         final HardwareConfigEntityService hardwareConfigEntityService) {
        return new DefaultComputeEnvironmentConfigService(
                computeEnvironmentConfigEntityService,
                hardwareConfigEntityService
        );
    }

}
