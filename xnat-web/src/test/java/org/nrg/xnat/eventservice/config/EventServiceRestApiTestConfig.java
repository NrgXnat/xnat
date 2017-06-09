package org.nrg.xnat.eventservice.config;


import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.eventservice.rest.EventServiceRestApi;
import org.nrg.xnat.eventservice.services.EventService;
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
@Import({RestApiTestConfig.class, EventServiceTestConfig.class})
public class EventServiceRestApiTestConfig extends WebSecurityConfigurerAdapter{
    @Bean
    public EventServiceRestApi eventServiceRestApi(final EventService eventService,
                                                   final UserManagementServiceI userManagementService,
                                                   final RoleHolder roleHolder) {
        return new EventServiceRestApi(eventService, userManagementService, roleHolder);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new TestingAuthenticationProvider());
    }

}
