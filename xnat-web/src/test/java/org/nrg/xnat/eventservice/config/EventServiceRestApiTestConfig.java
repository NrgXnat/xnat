package org.nrg.xnat.eventservice.config;


import org.mockito.Mockito;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.eventservice.rest.EventServiceRestApi;
import org.nrg.xnat.eventservice.services.EventService;
import org.nrg.xnat.services.archive.CatalogService;
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
@Import({RestApiTestConfig.class, EventServiceTestConfig.class})
public class EventServiceRestApiTestConfig {
    @Bean
    public EventServiceRestApi eventServiceRestApi(final EventService eventService,
                                                   final UserManagementServiceI userManagementService,
                                                   final RoleHolder roleHolder) {
        return new EventServiceRestApi(eventService, userManagementService, roleHolder);
    }

    @Bean
    public CatalogService catalogService() {
        return Mockito.mock(CatalogService.class);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(new TestingAuthenticationProvider());
    }
    @Bean
    public ContextService contextService(final ApplicationContext applicationContext) {
        final ContextService contextService = new ContextService();
        contextService.setApplicationContext(applicationContext);
        return contextService;
    }
}
