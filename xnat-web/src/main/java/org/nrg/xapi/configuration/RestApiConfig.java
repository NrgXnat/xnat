/*
 * web: org.nrg.xapi.configuration.RestApiConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.spawner.configuration.SpawnerConfig;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Locale;

/**
 * Configures the XAPI OpenAPI documentation. XNAT is a plain Spring MVC application (not Spring Boot), so the
 * springdoc configuration classes are imported explicitly per the springdoc "spring without spring-boot" setup.
 */
@Configuration
@ComponentScan(value = {"org.nrg.xapi.model.users", "org.nrg.xapi.rest", "org.nrg.xnat.eventservice.rest", "org.nrg.xnat.snapshot.rest"}, includeFilters = @Filter(ControllerAdvice.class))
@Import({SpawnerConfig.class,
         org.springdoc.core.configuration.SpringDocConfiguration.class,
         org.springdoc.core.properties.SpringDocConfigProperties.class,
         org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
         org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration.class})
@Slf4j
public class RestApiConfig {
    // NOTE: springdoc's swagger-ui integration (webmvc-ui SwaggerConfig) is deliberately NOT imported: its
    // welcome/redirect controllers build URLs from the context path only and break behind XNAT's /xapi/* servlet
    // mapping. The UI is served directly from the swagger-ui webjar by WebConfig instead, and the generated spec
    // is a verified superset of the old springfox output (546 vs 530 operations, 0 missing).

    @Bean
    public OpenAPI openApi(final XnatAppInfo info, final MessageSource messageSource) {
        return new OpenAPI().info(new Info().title(getMessage(messageSource, "apiInfo.title"))
                                            .description(getMessage(messageSource, "apiInfo.description"))
                                            .version(info.getVersion())
                                            .termsOfService(getMessage(messageSource, "apiInfo.termsOfServiceUrl"))
                                            .contact(new Contact().name(getMessage(messageSource, "apiInfo.contactName"))
                                                                  .url(getMessage(messageSource, "apiInfo.contactUrl"))
                                                                  .email(getMessage(messageSource, "apiInfo.contactEmail")))
                                            .license(new License().name(getMessage(messageSource, "apiInfo.license"))
                                                                  .url(getMessage(messageSource, "apiInfo.licenseUrl"))));
    }

    private String getMessage(final MessageSource messageSource, final String messageId) {
        return messageSource.getMessage(messageId, null, Locale.getDefault());
    }
}
