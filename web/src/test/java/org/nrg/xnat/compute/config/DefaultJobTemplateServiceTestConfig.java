package org.nrg.xnat.compute.config;

import org.nrg.xnat.compute.services.ComputeEnvironmentConfigService;
import org.nrg.xnat.compute.services.ConstraintConfigService;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.nrg.xnat.compute.services.impl.DefaultJobTemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MockConfig.class})
public class DefaultJobTemplateServiceTestConfig {

    @Bean
    public DefaultJobTemplateService defaultJobTemplateService(final ComputeEnvironmentConfigService mockComputeEnvironmentConfigService,
                                                               final HardwareConfigService mockHardwareConfigService,
                                                               final ConstraintConfigService mockConstraintConfigService) {
        return new DefaultJobTemplateService(
                mockComputeEnvironmentConfigService,
                mockHardwareConfigService,
                mockConstraintConfigService
        );
    }

}
