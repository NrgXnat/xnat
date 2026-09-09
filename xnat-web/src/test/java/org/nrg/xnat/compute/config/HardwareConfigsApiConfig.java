package org.nrg.xnat.compute.config;

import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.compute.rest.HardwareConfigsApi;
import org.nrg.xnat.compute.services.HardwareConfigService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@Import({MockConfig.class, RestApiTestConfig.class, HibernateConfig.class})
public class HardwareConfigsApiConfig {

    @Bean
    public HardwareConfigsApi hardwareConfigsApi(final UserManagementServiceI mockUserManagementService,
                                                 final RoleHolder mockRoleHolder,
                                                 final HardwareConfigService mockHardwareConfigService) {
        return new HardwareConfigsApi(
                mockUserManagementService,
                mockRoleHolder,
                mockHardwareConfigService
        );
    }

    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(new TestingAuthenticationProvider());
    }

}
