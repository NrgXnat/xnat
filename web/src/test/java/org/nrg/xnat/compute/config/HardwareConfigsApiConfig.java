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
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@Import({MockConfig.class, RestApiTestConfig.class})
public class HardwareConfigsApiConfig extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new TestingAuthenticationProvider());
    }

}
