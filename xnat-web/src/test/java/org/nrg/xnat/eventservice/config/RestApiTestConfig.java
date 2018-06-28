package org.nrg.xnat.eventservice.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleServiceI;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@Import({ObjectMapperConfig.class})
public class RestApiTestConfig extends WebMvcConfigurerAdapter {

    @Bean
    public RoleHolder mockRoleHolder(final RoleServiceI roleServiceI, final NamedParameterJdbcTemplate template) {
        return new RoleHolder(roleServiceI, template);
    }

    @Bean
    public RoleServiceI mockRoleService() {
        return Mockito.mock(RoleServiceI.class);
    }

    @Bean
    public NamedParameterJdbcTemplate template() { return Mockito.mock(NamedParameterJdbcTemplate.class);}

    @Bean
    public UserManagementServiceI mockUserManagementServiceI() {
        return Mockito.mock(UserManagementServiceI.class);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }

    @Autowired
    private ObjectMapper objectMapper;
}
